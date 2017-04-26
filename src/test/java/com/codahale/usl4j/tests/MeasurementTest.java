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
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.codahale.usl4j.Measurement;
import org.junit.jupiter.api.Test;

class MeasurementTest {

  private final Measurement measurement = Measurement.ofConcurrency().andThroughput(3, 5);

  @Test
  void concurrency() throws Exception {
    assertEquals(3, measurement.concurrency(), EPSILON);
  }

  @Test
  void throughput() throws Exception {
    assertEquals(5, measurement.throughput(), EPSILON);
  }

  @Test
  void latency() throws Exception {
    assertEquals(0.6, measurement.latency(), EPSILON);
  }

  @Test
  void latencyMeasurement() throws Exception {
    final Measurement a = Measurement.ofConcurrency().andLatency(3, 0.6);
    assertEquals(5, a.throughput(), EPSILON);

    final Measurement b = Measurement.ofConcurrency().andLatency(new double[]{3, 0.6});
    assertEquals(5, b.throughput(), EPSILON);
  }

  @Test
  void throughputMeasurement() throws Exception {
    final Measurement a = Measurement.ofThroughput().andLatency(5, 0.6);
    assertEquals(3, a.concurrency(), EPSILON);

    final Measurement b = Measurement.ofThroughput().andLatency(new double[]{5, 0.6});
    assertEquals(3, b.concurrency(), EPSILON);

    final Measurement c = Measurement.ofThroughput().andConcurrency(5, 3);
    assertEquals(0.6, measurement.latency());
  }
}