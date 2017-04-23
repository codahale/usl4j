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

  /**
   * A {@link Measurement} of a system's throughput with a given number of concurrent workers.
   *
   * @param concurrency the number of concurrent workers
   * @param throughput the throughput, in events per second
   * @return a {@link Measurement}
   */
  public static Measurement ofConcurrencyAndThroughput(double concurrency, double throughput) {
    return new AutoValue_Measurement(concurrency, throughput, concurrency / throughput);
  }

  /**
   * A {@link Measurement} of a system's throughput with a given number of concurrent workers.
   *
   * @param point an array of concurrency/throughput pairs
   * @return a {@link Measurement}
   * @see #ofConcurrencyAndThroughput(double, double)
   */
  public static Measurement ofConcurrencyAndThroughput(double[] point) {
    return ofConcurrencyAndThroughput(point[0], point[1]);
  }

  /**
   * A {@link Measurement} of a system's throughput with a mean latency.
   *
   * @param concurrency the number of concurrent workers
   * @param latency the mean latency, in seconds
   * @return a {@link Measurement}
   */
  public static Measurement ofConcurrencyAndLatency(double concurrency, double latency) {
    return new AutoValue_Measurement(concurrency, concurrency / latency, latency);
  }

  /**
   * A {@link Measurement} of a system's throughput with a mean latency.
   *
   * @param point an array of concurrency/latency pairs
   * @return a {@link Measurement}
   * @see #ofConcurrencyAndLatency(double, double)
   */
  public static Measurement ofConcurrencyAndLatency(double[] point) {
    return ofConcurrencyAndLatency(point[0], point[1]);
  }

  /**
   * A {@link Measurement} of a system's latency at a given throughput.
   *
   * @param throughput the throughput, in events per second
   * @param latency the mean latency, in seconds
   * @return a {@link Measurement}
   */
  public static Measurement ofThroughputAndLatency(double throughput, double latency) {
    return new AutoValue_Measurement(throughput * latency, throughput, latency);
  }

  /**
   * A {@link Measurement} of a system's latency at a given throughput.
   *
   * @param points an array of throughput/latency points
   * @return a {@link Measurement}
   */
  public static Measurement ofThroughputAndLatency(double[] points) {
    return ofThroughputAndLatency(points[0], points[1]);
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
}
