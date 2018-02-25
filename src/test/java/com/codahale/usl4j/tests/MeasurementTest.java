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
import org.junit.Test;

public class MeasurementTest {

  private final Measurement measurement = Measurement.ofConcurrency().andThroughput(3, 5);

  @Test
  public void badPoints() {
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
  public void concurrency() {
    assertThat(measurement.concurrency()).isCloseTo(3, EPSILON);
  }

  @Test
  public void throughput() {
    assertThat(measurement.throughput()).isCloseTo(5, EPSILON);
  }

  @Test
  public void latency() {
    assertThat(measurement.latency()).isCloseTo(0.6, EPSILON);
  }

  @Test
  public void latencyMeasurement() {
    final Measurement a = Measurement.ofConcurrency().andLatency(3, 0.6);
    assertThat(a.throughput()).isCloseTo(5, EPSILON);

    final Measurement b = Measurement.ofConcurrency().andLatency(new double[] {3, 0.6});
    assertThat(b.throughput()).isCloseTo(5, EPSILON);
  }

  @Test
  public void throughputMeasurement() {
    final Measurement a = Measurement.ofThroughput().andLatency(5, 0.6);
    assertThat(a.concurrency()).isCloseTo(3, EPSILON);

    final Measurement b = Measurement.ofThroughput().andLatency(new double[] {5, 0.6});
    assertThat(b.concurrency()).isCloseTo(3, EPSILON);

    final Measurement c = Measurement.ofThroughput().andConcurrency(5, 3);
    assertThat(c.latency()).isCloseTo(0.6, EPSILON);
  }
}
