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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.codahale.usl4j.Measurement;
import com.codahale.usl4j.Model;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class ModelTest {

  static final double EPSILON = 0.00001;
  // data of Cisco benchmark from Practical Scalability by Baron Schwartz
  private static final double[][] CISCO = {
    {1, 955.16},
    {2, 1878.91},
    {3, 2688.01},
    {4, 3548.68},
    {5, 4315.54},
    {6, 5130.43},
    {7, 5931.37},
    {8, 6531.08},
    {9, 7219.8},
    {10, 7867.61},
    {11, 8278.71},
    {12, 8646.7},
    {13, 9047.84},
    {14, 9426.55},
    {15, 9645.37},
    {16, 9897.24},
    {17, 10097.6},
    {18, 10240.5},
    {19, 10532.39},
    {20, 10798.52},
    {21, 11151.43},
    {22, 11518.63},
    {23, 11806},
    {24, 12089.37},
    {25, 12075.41},
    {26, 12177.29},
    {27, 12211.41},
    {28, 12158.93},
    {29, 12155.27},
    {30, 12118.04},
    {31, 12140.4},
    {32, 12074.39}
  };
  // listed values of the fitted model from the book
  private static final double BOOK_KAPPA = 7.690945E-4;
  private static final double BOOK_SIGMA = 0.02671591;
  private static final double BOOK_LAMBDA = 995.6486;
  private static final double BOOK_N_MAX = 35;
  private static final double BOOK_X_MAX = 12341;

  // a model built from the Cisco measurements
  private final Model model =
      Arrays.stream(CISCO).map(Measurement.ofConcurrency()::andThroughput).collect(Model.toModel());

  @Test
  void minMeasurements() {
    assertThrows(IllegalArgumentException.class, () -> Model.build(Collections.emptyList()));
  }

  @Test
  void build() {
    final Model other =
        Model.build(
            Arrays.stream(CISCO)
                .map(Measurement.ofConcurrency()::andThroughput)
                .collect(Collectors.toList()));
    assertEquals(other.sigma(), model.sigma(), EPSILON);
  }

  @Test
  void sigma() {
    assertClose(BOOK_SIGMA, model.sigma());
  }

  @Test
  void kappa() {
    assertClose(BOOK_KAPPA, model.kappa());
  }

  @Test
  void lambda() {
    assertClose(BOOK_LAMBDA, model.lambda());
  }

  @Test
  void maxConcurrency() {
    assertClose(BOOK_N_MAX, model.maxConcurrency());
  }

  @Test
  void maxThroughput() {
    assertClose(BOOK_X_MAX, model.maxThroughput());
  }

  @Test
  void coherency() {
    assertFalse(model.isCoherencyConstrained());
  }

  @Test
  void contention() {
    assertTrue(model.isContentionConstrained());
  }

  @Test
  void latencyAtConcurrency() {
    assertEquals(0.0010043984982923623, model.latencyAtConcurrency(1), EPSILON);
    assertEquals(0.0018077217982978785, model.latencyAtConcurrency(20), EPSILON);
    assertEquals(0.0028359135486017784, model.latencyAtConcurrency(35), EPSILON);
  }

  @Test
  void throughputAtConcurrency() {
    assertEquals(995.648772003358, model.throughputAtConcurrency(1), EPSILON);
    assertEquals(11063.633137626028, model.throughputAtConcurrency(20), EPSILON);
    assertEquals(12341.7456205207, model.throughputAtConcurrency(35), EPSILON);
  }

  @Test
  void concurrencyAtThroughput() {
    assertEquals(0.9580998829620233, model.concurrencyAtThroughput(955), EPSILON);
    assertEquals(15.350435172752203, model.concurrencyAtThroughput(11048), EPSILON);
    assertEquals(17.73220762025387, model.concurrencyAtThroughput(12201), EPSILON);
  }

  @Test
  void throughputAtLatency() {
    final Model model = Model.of(0.06, 0.06, 40);
    assertEquals(69.38886664887109, model.throughputAtLatency(0.03), EPSILON);
    assertEquals(82.91561975888501, model.throughputAtLatency(0.04), EPSILON);
    assertEquals(84.06346808612327, model.throughputAtLatency(0.05), EPSILON);
  }

  @Test
  void latencyAtThroughput() {
    final Model model = Model.of(0.06, 0.06, 40);
    assertEquals(0.05875, model.latencyAtThroughput(400), EPSILON);
    assertEquals(0.094, model.latencyAtThroughput(500), EPSILON);
    assertEquals(0.235, model.latencyAtThroughput(600), EPSILON);
  }

  @Test
  void concurrencyAtLatency() {
    // going off page 30-31
    final Model model =
        Arrays.stream(CISCO)
            .limit(10)
            .map(Measurement.ofConcurrency()::andThroughput)
            .collect(Model.toModel());
    assertEquals(7.230628979597649, model.concurrencyAtLatency(0.0012), EPSILON);
    assertEquals(20.25106409917121, model.concurrencyAtLatency(0.0016), EPSILON);
    assertEquals(29.888882633013246, model.concurrencyAtLatency(0.0020), EPSILON);
  }

  @Test
  void limitless() {
    final Model unlimited = Model.of(1, 0, 40);
    assertTrue(unlimited.isLimitless());
    assertFalse(model.isLimitless());
  }

  // assert that the actual value is within 0.02% of the expected value
  private void assertClose(double expected, double actual) {
    assertEquals(expected, actual, expected * 2.0E-4);
  }
}
