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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codahale.usl4j.Measurement;
import com.codahale.usl4j.Model;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import org.assertj.core.data.Offset;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.Test;

class ModelTest {

  static final Offset<Double> EPSILON = Offset.offset(0.00001);

  // data of Cisco benchmark from Practical Scalability by Baron Schwartz
  private static final double[][] CISCO = {
      {1, 955.16}, {2, 1878.91}, {3, 2688.01}, {4, 3548.68}, {5, 4315, 54}, {6, 5130.43},
      {7, 5931.37}, {8, 6531.08}, {9, 7219.8}, {10, 7867.61}, {11, 8278.71}, {12, 8646.7},
      {13, 9047.84}, {14, 9426.55}, {15, 9645.37}, {16, 9897.24}, {17, 10097.6}, {18, 10240.5},
      {19, 10532.39}, {20, 10798.52}, {21, 11151.43}, {22, 11518.63}, {23, 11806}, {24, 12089.37},
      {25, 12075.41}, {26, 12177.29}, {27, 12211.41}, {28, 12158.93}, {29, 12155.27},
      {30, 12118.04}, {31, 12140.4}, {32, 12074.39}};

  // listed values of the fitted model from the book
  private static final double BOOK_KAPPA = 7.690945E-4;
  private static final double BOOK_SIGMA = 0.02671591;
  private static final double BOOK_LAMBDA = 995.6486;
  private static final double BOOK_N_MAX = 35;
  private static final double BOOK_X_MAX = 12341;

  // our numbers should be within 0.02% of those in the book
  private static final Percentage BOOK_FIT = Percentage.withPercentage(0.02);

  // a model built from the Cisco measurements
  private final Model model = Arrays.stream(CISCO).map(Measurement::ofConcurrencyAndThroughput)
                                    .collect(Model.toModel());

  @Test
  void minMeasurements() {
    assertThatThrownBy(() -> Model.build(Collections.emptyList()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Needs at least 6 measurements");
  }

  @Test
  void build() throws Exception {
    final Model other = Model.build(Arrays.stream(CISCO)
                                          .map(Measurement::ofConcurrencyAndThroughput)
                                          .collect(Collectors.toList()));
    assertThat(other.sigma())
        .isCloseTo(model.sigma(), EPSILON);
  }

  @Test
  void sigma() throws Exception {
    assertThat(model.sigma())
        .isCloseTo(BOOK_SIGMA, BOOK_FIT);
  }

  @Test
  void kappa() throws Exception {
    assertThat(model.kappa())
        .isCloseTo(BOOK_KAPPA, BOOK_FIT);
  }

  @Test
  void lambda() throws Exception {
    assertThat(model.lambda())
        .isCloseTo(BOOK_LAMBDA, BOOK_FIT);
  }

  @Test
  void maxConcurrency() throws Exception {
    assertThat(model.maxConcurrency())
        .isCloseTo(BOOK_N_MAX, BOOK_FIT);
  }

  @Test
  void maxThroughput() throws Exception {
    assertThat(model.maxThroughput())
        .isCloseTo(BOOK_X_MAX, BOOK_FIT);
  }

  @Test
  void coherency() throws Exception {
    assertThat(model.isCoherencyConstrained())
        .isFalse();
  }

  @Test
  void contention() throws Exception {
    assertThat(model.isContentionConstrained())
        .isTrue();
  }

  @Test
  void latencyAtConcurrency() throws Exception {
    assertThat(model.latencyAtConcurrency(1))
        .isCloseTo(0.0010043984982923623, EPSILON);
    assertThat(model.latencyAtConcurrency(20))
        .isCloseTo(0.0018077217982978785, EPSILON);
    assertThat(model.latencyAtConcurrency(35))
        .isCloseTo(0.0028359135486017784, EPSILON);
  }

  @Test
  void throughputAtConcurrency() throws Exception {
    assertThat(model.throughputAtConcurrency(1))
        .isCloseTo(995.620763770714, EPSILON);
    assertThat(model.throughputAtConcurrency(20))
        .isCloseTo(11063.649295390296, EPSILON);
    assertThat(model.throughputAtConcurrency(35))
        .isCloseTo(12341.702030111755, EPSILON);
  }

  @Test
  void concurrencyAtThroughput() throws Exception {
    assertThat(model.concurrencyAtThroughput(955))
        .isCloseTo(0.9581277018657741, EPSILON);
    assertThat(model.concurrencyAtThroughput(11048))
        .isCloseTo(15.350185060769837, EPSILON);
    assertThat(model.concurrencyAtThroughput(12201))
        .isCloseTo(17.73178569077728, EPSILON);
  }

  @Test
  void throughputAtLatency() throws Exception {
    final Model model = Model.of(0.06, 0.06, 40);
    assertThat(model.throughputAtLatency(0.03))
        .isCloseTo(69.38886664887109, EPSILON);
    assertThat(model.throughputAtLatency(0.04))
        .isCloseTo(82.91561975888501, EPSILON);
    assertThat(model.throughputAtLatency(0.05))
        .isCloseTo(84.06346808612327, EPSILON);
  }

  @Test
  void latencyAtThroughput() throws Exception {
    final Model model = Model.of(0.06, 0.06, 40);
    assertThat(model.latencyAtThroughput(400))
        .isCloseTo(0.05875, EPSILON);
    assertThat(model.latencyAtThroughput(500))
        .isCloseTo(0.094, EPSILON);
    assertThat(model.latencyAtThroughput(600))
        .isCloseTo(0.235, EPSILON);
  }

  @Test
  void concurrencyAtLatency() throws Exception {
    // going off page 30-31
    final Model model = Arrays.stream(CISCO).limit(10)
                              .map(Measurement::ofConcurrencyAndThroughput)
                              .collect(Model.toModel());
    assertThat(model.concurrencyAtLatency(0.0012))
        .isCloseTo(7.229893153888714, EPSILON);
    assertThat(model.concurrencyAtLatency(0.0016))
        .isCloseTo(20.257571946254153, EPSILON);
    assertThat(model.concurrencyAtLatency(0.0020))
        .isCloseTo(29.9028582842587, EPSILON);
  }

  @Test
  void limitless() throws Exception {
    final Model unlimited = Model.of(1, 0, 40);
    assertThat(unlimited.isLimitless())
        .isTrue();
    assertThat(model.isLimitless())
        .isFalse();
  }
}