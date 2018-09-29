/*
 * Copyright © 2017 Coda Hale (coda.hale@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.codahale.usl4j;

import static java.lang.Math.floor;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collector;
import org.ddogleg.optimization.FactoryOptimization;
import org.ddogleg.optimization.UnconstrainedLeastSquares;
import org.ddogleg.optimization.UtilOptimize;
import org.ddogleg.optimization.functions.FunctionNtoM;

/** A parametrized model of the Universal Scalability Law. */
public class Model {

  private static final int MIN_MEASUREMENTS = 6;
  private final double sigma;
  private final double kappa;
  private final double lambda;

  /**
   * Creates a model given the three parameters: σ, κ, and λ.
   *
   * @param sigma the coefficient of contention
   * @param kappa the coefficient of crosstalk (coherence)
   * @param lambda the throughput of the system given a single worker
   */
  public Model(double sigma, double kappa, double lambda) {
    this.sigma = sigma;
    this.kappa = kappa;
    this.lambda = lambda;
  }

  /**
   * A collector which will convert a stream of {@link Measurement} instances into a {@link Model}.
   *
   * @return a {@link Collector} instance
   */
  public static Collector<Measurement, List<Measurement>, Model> toModel() {
    return Collector.of(
        ArrayList::new,
        List::add,
        (a, b) -> {
          a.addAll(b);
          return a;
        },
        Model::build);
  }

  /**
   * Given a collection of measurements, builds a {@link Model}.
   *
   * <p>Finds a set of coefficients for the equation {@code y = λx/(1+σ(x-1)+κx(x-1))} which best
   * fit the observed values using unconstrained least-squares regression. The resulting values for
   * λ, κ, and σ are the parameters of the returned {@link Model}.
   *
   * @param measurements a collection of measurements
   * @return a {@link Model} instance
   */
  public static Model build(List<Measurement> measurements) {
    if (measurements.size() < MIN_MEASUREMENTS) {
      throw new IllegalArgumentException("Needs at least 6 measurements");
    }
    // use Levenberg-Marquardt least-squares to determine best fitting coefficients for the model
    final UnconstrainedLeastSquares<?> lm = FactoryOptimization.levenbergMarquardt(null, true);
    lm.setFunction(
        new FunctionNtoM() {

          @Override
          public int getNumOfInputsN() {
            return 3;
          }

          @Override
          public int getNumOfOutputsM() {
            return measurements.size();
          }

          @Override
          public void process(double[] input, double[] output) {
            // calculates and returns the residuals for each observation
            final Model model = new Model(input[0], input[1], input[2]);
            for (int i = 0; i < measurements.size(); i++) {
              final Measurement m = measurements.get(i);
              output[i] = m.throughput() - model.throughputAtConcurrency(m.concurrency());
            }
          }
        },
        null);

    // calculate a best guess of lambda
    final double l =
        measurements
            .stream()
            .mapToDouble(m -> m.throughput() / m.concurrency())
            .max()
            .orElseThrow(IllegalArgumentException::new);
    lm.initialize(new double[] {0.1, 0.01, l}, 1e-12, 1e-12);

    // run iterations until we converge or get bored
    if (!UtilOptimize.process(lm, 5_000)) {
      throw new IllegalArgumentException("Unable to build a model for these values");
    }

    final double[] parameters = lm.getParameters();
    return new Model(parameters[0], parameters[1], parameters[2]);
  }

  /**
   * The model's coefficient of contention.
   *
   * @return {@code σ}
   */
  public double sigma() {
    return sigma;
  }

  /**
   * The model's coefficient of crosstalk/coherency.
   *
   * @return {@code κ}
   */
  public double kappa() {
    return kappa;
  }

  /**
   * The model's coefficient of performance.
   *
   * @return {@code λ}
   */
  public double lambda() {
    return lambda;
  }

  /**
   * The expected throughput given a number of concurrent workers.
   *
   * @param n the number of concurrent workers
   * @return {@code X(N)}
   * @see "Practical Scalability Analysis with the Universal Scalability Law, Equation 3"
   */
  public double throughputAtConcurrency(double n) {
    return (lambda * n) / (1 + (sigma * (n - 1)) + (kappa * n * (n - 1)));
  }

  /**
   * The expected mean latency given a number of concurrent workers.
   *
   * @param n the number of concurrent workers
   * @return {@code R(N)}
   * @see "Practical Scalability Analysis with the Universal Scalability Law, Equation 6"
   */
  public double latencyAtConcurrency(double n) {
    return (1 + (sigma * (n - 1)) + (kappa * n * (n - 1))) / lambda;
  }

  /**
   * The maximum expected number of concurrent workers the system can handle.
   *
   * @return {@code N}<sub>max</sub>
   * @see "Practical Scalability Analysis with the Universal Scalability Law, Equation 4"
   */
  public double maxConcurrency() {
    return floor(sqrt((1 - sigma) / kappa));
  }

  /**
   * The maximum expected throughput the system can handle.
   *
   * @return {@code X}<sub>max</sub>
   */
  public double maxThroughput() {
    return throughputAtConcurrency(maxConcurrency());
  }

  /**
   * The expected mean latency given a throughput.
   *
   * @param x the throughput of requests
   * @return {@code R(X)}
   * @see "Practical Scalability Analysis with the Universal Scalability Law, Equation 8"
   */
  public double latencyAtThroughput(double x) {
    return (sigma - 1) / (sigma * x - lambda);
  }

  /**
   * The expected throughput given a mean latency.
   *
   * @param r the mean latency of requests
   * @return {@code X(R)}
   * @see "Practical Scalability Analysis with the Universal Scalability Law, Equation 9"
   */
  public double throughputAtLatency(double r) {
    final double a = 2 * kappa * (2 * lambda * r + sigma - 2);
    final double b = sqrt(pow(sigma, 2) + pow(kappa, 2) + a);
    return (b - kappa + sigma) / (2.0 * kappa * r);
  }

  /**
   * The expected number of concurrent workers given a mean latency.
   *
   * @param r the mean latency of requests
   * @return {@code N(R)}
   * @see "Practical Scalability Analysis with the Universal Scalability Law, Equation 10"
   */
  public double concurrencyAtLatency(double r) {
    final double a = (2 * kappa * ((2 * lambda * r) + sigma - 2));
    final double b = sqrt(pow(sigma, 2) + pow(kappa, 2) + a);
    return (kappa - sigma + b) / (2 * kappa);
  }

  /**
   * The expected number of concurrent workers at a particular throughput.
   *
   * @param x the throughput of requests
   * @return {@code N(X)}
   */
  public double concurrencyAtThroughput(double x) {
    return latencyAtThroughput(x) * x;
  }

  /**
   * Whether or not the system is constrained by coherency costs.
   *
   * @return σ {@literal <} κ
   */
  public boolean isCoherencyConstrained() {
    return sigma < kappa;
  }

  /**
   * Whether or not the system is constrained by contention.
   *
   * @return σ {@literal >} κ
   */
  public boolean isContentionConstrained() {
    return sigma > kappa;
  }

  /**
   * Whether or not the system is linearly scalable.
   *
   * @return κ = 0
   */
  public boolean isLimitless() {
    return kappa == 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Model model = (Model) o;
    return Double.compare(model.sigma, sigma) == 0
        && Double.compare(model.kappa, kappa) == 0
        && Double.compare(model.lambda, lambda) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(sigma, kappa, lambda);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Model.class.getSimpleName() + "[", "]")
        .add("sigma=" + sigma)
        .add("kappa=" + kappa)
        .add("lambda=" + lambda)
        .toString();
  }
}
