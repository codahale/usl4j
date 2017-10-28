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

package com.codahale.usl4j.benchmarks;

import com.codahale.usl4j.Measurement;
import com.codahale.usl4j.Model;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class Benchmarks {

  private List<Measurement> input = new ArrayList<>();

  @Param({"10", "100", "1000", "10000"})
  private int size = 10;

  public static void main(String[] args) throws IOException, RunnerException {
    Main.main(args);
  }

  @Setup
  public void setup() {
    this.input = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      input.add(Measurement.ofConcurrency().andThroughput(i, Math.random() * i));
    }
  }

  @Benchmark
  public Model build() {
    return Model.build(input);
  }
}
