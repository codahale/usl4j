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

package com.codahale.usl4j.tests;

import static com.codahale.usl4j.tests.ModelTest.EPSILON;
import static org.assertj.core.api.Assertions.assertThat;

import com.codahale.usl4j.Measurement;
import org.junit.jupiter.api.Test;

class MeasurementTest {

  private final Measurement measurement = Measurement.ofConcurrencyAndThroughput(3, 5);

  @Test
  void concurrency() throws Exception {
    assertThat(measurement.concurrency())
        .isCloseTo(3, EPSILON);
  }

  @Test
  void throughput() throws Exception {
    assertThat(measurement.throughput())
        .isCloseTo(5, EPSILON);
  }

  @Test
  void latency() throws Exception {
    assertThat(measurement.latency())
        .isCloseTo(0.6, EPSILON);
  }

  @Test
  void latencyMeasurement() throws Exception {
    final Measurement a = Measurement.ofConcurrencyAndLatency(3, 0.6);
    assertThat(a.throughput())
        .isCloseTo(5, EPSILON);

    final Measurement b = Measurement.ofConcurrencyAndLatency(new double[]{3, 0.6});
    assertThat(b.throughput())
        .isCloseTo(5, EPSILON);
  }

  @Test
  void throughputMeasurement() throws Exception {
    final Measurement a = Measurement.ofThroughputAndLatency(5, 0.6);
    assertThat(a.concurrency())
        .isCloseTo(3, EPSILON);

    final Measurement b = Measurement.ofThroughputAndLatency(new double[]{5, 0.6});
    assertThat(b.concurrency())
        .isCloseTo(3, EPSILON);
  }
}