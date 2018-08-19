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
package com.codahale.usl4j.tests;

import static com.codahale.usl4j.tests.ModelTest.EPSILON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codahale.usl4j.Measurement;
import org.junit.jupiter.api.Test;

class MeasurementTest {

  private final Measurement measurement = Measurement.ofConcurrency().andThroughput(3, 5);

  @Test
  void badPoints() {
    final double[] p = new double[3];

    assertThatThrownBy(() -> Measurement.ofConcurrency().andLatency(p))
        .isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(() -> Measurement.ofConcurrency().andThroughput(p))
        .isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(() -> Measurement.ofThroughput().andLatency(p))
        .isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(() -> Measurement.ofThroughput().andConcurrency(p))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void concurrency() {
    assertThat(measurement.concurrency()).isCloseTo(3, EPSILON);
  }

  @Test
  void throughput() {
    assertThat(measurement.throughput()).isCloseTo(5, EPSILON);
  }

  @Test
  void latency() {
    assertThat(measurement.latency()).isCloseTo(0.6, EPSILON);
  }

  @Test
  void measurements() {
    final Measurement cl1 = Measurement.ofConcurrency().andLatency(3, 0.6);
    assertThat(cl1.concurrency()).isCloseTo(3, EPSILON);
    assertThat(cl1.latency()).isCloseTo(0.6, EPSILON);
    assertThat(cl1.throughput()).isCloseTo(5, EPSILON);

    final Measurement cl2 = Measurement.ofConcurrency().andLatency(new double[] {3, 0.6});
    assertThat(cl2.concurrency()).isCloseTo(3, EPSILON);
    assertThat(cl2.latency()).isCloseTo(0.6, EPSILON);
    assertThat(cl2.throughput()).isCloseTo(5, EPSILON);

    final Measurement ct1 = Measurement.ofConcurrency().andThroughput(3, 5);
    assertThat(ct1.concurrency()).isCloseTo(3, EPSILON);
    assertThat(ct1.latency()).isCloseTo(0.6, EPSILON);
    assertThat(ct1.throughput()).isCloseTo(5, EPSILON);

    final Measurement ct2 = Measurement.ofConcurrency().andThroughput(new double[] {3, 5});
    assertThat(ct2.concurrency()).isCloseTo(3, EPSILON);
    assertThat(ct2.latency()).isCloseTo(0.6, EPSILON);
    assertThat(ct2.throughput()).isCloseTo(5, EPSILON);

    final Measurement tl1 = Measurement.ofThroughput().andLatency(5, 0.6);
    assertThat(tl1.concurrency()).isCloseTo(3, EPSILON);
    assertThat(tl1.latency()).isCloseTo(0.6, EPSILON);
    assertThat(tl1.throughput()).isCloseTo(5, EPSILON);

    final Measurement tl2 = Measurement.ofThroughput().andLatency(new double[] {5, 0.6});
    assertThat(tl2.concurrency()).isCloseTo(3, EPSILON);
    assertThat(tl2.latency()).isCloseTo(0.6, EPSILON);
    assertThat(tl2.throughput()).isCloseTo(5, EPSILON);

    final Measurement tc1 = Measurement.ofThroughput().andConcurrency(5, 3);
    assertThat(tc1.concurrency()).isCloseTo(3, EPSILON);
    assertThat(tc1.latency()).isCloseTo(0.6, EPSILON);
    assertThat(tc1.throughput()).isCloseTo(5, EPSILON);

    final Measurement tc2 = Measurement.ofThroughput().andConcurrency(new double[] {5, 3});
    assertThat(tc2.concurrency()).isCloseTo(3, EPSILON);
    assertThat(tc2.latency()).isCloseTo(0.6, EPSILON);
    assertThat(tc2.throughput()).isCloseTo(5, EPSILON);
  }
}
