/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.codahale.usl4j;

import com.google.auto.value.AutoValue;
import javax.annotation.concurrent.Immutable;

/**
 * A measurement of a system's concurrency, throughput, and latency. Given any two properties, the
 * third will be calculated via Little's Law.
 */
@AutoValue
@Immutable
public abstract class Measurement {

  private static final ConcurrencyBuilder WITH_CONCURRENCY = new ConcurrencyBuilder() {
    @Override
    public Measurement andThroughput(double concurrency, double throughput) {
      return new AutoValue_Measurement(concurrency, throughput, concurrency / throughput);
    }

    @Override
    public Measurement andThroughput(double[] point) {
      return andThroughput(point[0], point[1]);
    }

    @Override
    public Measurement andLatency(double concurrency, double latency) {
      return new AutoValue_Measurement(concurrency, concurrency / latency, latency);
    }

    @Override
    public Measurement andLatency(double[] point) {
      return andLatency(point[0], point[1]);
    }
  };

  private static final ThroughputBuilder WITH_THROUGHPUT = new ThroughputBuilder() {
    @Override
    public Measurement andConcurrency(double throughput, double concurrency) {
      return Measurement.ofConcurrency().andThroughput(throughput, concurrency);
    }

    @Override
    public Measurement andConcurrency(double[] point) {
      return andConcurrency(point[0], point[1]);
    }

    @Override
    public Measurement andLatency(double throughput, double latency) {
      return new AutoValue_Measurement(throughput * latency, throughput, latency);
    }

    @Override
    public Measurement andLatency(double[] point) {
      return andLatency(point[0], point[1]);
    }
  };

  /**
   * Returns a builder for measurements of concurrency.
   *
   * @return a builder for measurements of concurrency
   */
  public static ConcurrencyBuilder ofConcurrency() {
    return WITH_CONCURRENCY;
  }

  /**
   * Returns a builder for measurements of throughput
   *
   * @return a builder for measurements of throughput
   */
  public static ThroughputBuilder ofThroughput() {
    return WITH_THROUGHPUT;
  }

  /**
   * The number of concurrent workers at the time of measurement.
   *
   * @return {@code N}
   */
  public abstract double concurrency();

  /**
   * The throughput of events at the time of measurement.
   *
   * @return {@code X}
   */
  public abstract double throughput();

  /**
   * The mean latency at the time of measurement.
   *
   * @return {@code R}
   */
  public abstract double latency();

  public interface ConcurrencyBuilder {

    /**
     * A {@link Measurement} of a system's throughput with a given number of concurrent workers.
     *
     * @param concurrency the number of concurrent workers
     * @param throughput the throughput, in events per second
     * @return a {@link Measurement}
     */
    Measurement andThroughput(double concurrency, double throughput);

    /**
     * A {@link Measurement} of a system's throughput with a given number of concurrent workers.
     *
     * @param point an array of concurrency/throughput pairs
     * @return a {@link Measurement}
     */
    Measurement andThroughput(double[] point);

    /**
     * A {@link Measurement} of a system's throughput with a mean latency.
     *
     * @param concurrency the number of concurrent workers
     * @param latency the mean latency, in seconds
     * @return a {@link Measurement}
     */
    Measurement andLatency(double concurrency, double latency);

    /**
     * A {@link Measurement} of a system's throughput with a mean latency.
     *
     * @param point an array of concurrency/latency pairs
     * @return a {@link Measurement}
     */
    Measurement andLatency(double[] point);
  }

  public interface ThroughputBuilder {

    /**
     * A {@link Measurement} of a system's throughput with a given number of concurrent workers.
     *
     * @param throughput the throughput, in events per second
     * @param concurrency the number of concurrent workers
     * @return a {@link Measurement}
     */
    Measurement andConcurrency(double throughput, double concurrency);

    /**
     * A {@link Measurement} of a system's throughput with a given number of concurrent workers.
     *
     * @param point an array of concurrency/throughput pairs
     * @return a {@link Measurement}
     */
    Measurement andConcurrency(double[] point);

    /**
     * A {@link Measurement} of a system's latency at a given throughput.
     *
     * @param throughput the throughput, in events per second
     * @param latency the mean latency, in seconds
     * @return a {@link Measurement}
     */
    Measurement andLatency(double throughput, double latency);

    /**
     * A {@link Measurement} of a system's latency at a given throughput.
     *
     * @param point an array of throughput/latency points
     * @return a {@link Measurement}
     */
    Measurement andLatency(double[] point);
  }
}
