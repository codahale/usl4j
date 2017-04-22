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
 * A measurement of a system's throughput at a given level of concurrency.
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
  public static Measurement throughput(double concurrency, double throughput) {
    return new AutoValue_Measurement(concurrency, throughput);
  }

  /**
   * A {@link Measurement} of a system's throughput with a given number of concurrent workers.
   *
   * @param point an array of concurrency/throughput pairs
   * @return a {@link Measurement}
   * @see #throughput(double, double)
   */
  public static Measurement throughput(double[] point) {
    return throughput(point[0], point[1]);
  }

  /**
   * A {@link Measurement} of a system's throughput with a mean latency
   *
   * @param concurrency the number of concurrent workers
   * @param latency the mean latency, in seconds
   * @return a {@link Measurement}
   */
  public static Measurement latency(double concurrency, double latency) {
    return throughput(concurrency, concurrency / latency);
  }

  /**
   * A {@link Measurement} of a system's throughput with a mean latency
   *
   * @param point an array of concurrency/latency pairs
   * @return a {@link Measurement}
   * @see #latency(double, double)
   */
  public static Measurement latency(double[] point) {
    return latency(point[0], point[1]);
  }

  abstract double x();

  abstract double y();

  /**
   * The number of concurrent workers at the time of measurement.
   *
   * @return {@code N}
   */
  public double concurrency() {
    return x();
  }

  /**
   * The throughput of events at the time of measurement.
   *
   * @return {@code X}
   */
  public double throughput() {
    return y();
  }

  /**
   * The mean latency at the time of measurement.
   *
   * @return {@code R}
   */
  public double latency() {
    return concurrency() / throughput();
  }
}
