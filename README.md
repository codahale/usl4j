# usl4j

[![Build Status](https://secure.travis-ci.org/codahale/usl4j.svg)](http://travis-ci.org/codahale/usl4j)

usl4j is Java modeler for the Universal Scalability Law, which can be used in system testing and
capacity planning.

## Add to your project

```xml
<dependency>
  <groupId>com.codahale</groupId>
  <artifactId>usl4j</artifactId>
  <version>0.2.1</version>
</dependency>
```

It depends on the [Efficient Java Matrix Library](https://github.com/lessthanoptimal/ejml) for
matrix operations. If someone has a less funky way to emulate [R's `nls`
function](https://github.com/smoeding/usl), I'd love to hear about it.

## How to use this

As an example, consider doing load testing and capacity planning for an HTTP server. To model the
behavior of the system using the USL, you must first gather a set of measurements of the system.
These measurements must be of two of the three parameters of [Little's
Law](https://en.wikipedia.org/wiki/Little%27s_law): mean response time (in seconds), throughput (in
requests per second), and concurrency (i.e. the number of concurrent clients).

Because response time tends to be a property of load (i.e. it rises as throughput or concurrency
rises), the dependent variable in your tests should be mean response time. This leaves either
throughput or concurrency as your independent variable, but thanks to Little's Law it doesn't matter
which one you use. For the purposes of discussion, let's say you measure throughput as a function of
the number of concurrent clients working at a fixed rate (e.g. you used `wrk2`).

After your load testing is done, your should have a set of measurements shaped like this:

|concurrency|throughput|
|-----------|----------|
|          1|      4227|
|          2|      8382|
|          4|     16479|
|          8|     31856|
|         16|     59564|
|         32|    104462|
|         64|    162985|

For simplicity's sake, let's assume your're storing this as a `double[][]`. Now you can build our
model and begin estimating things:

```java
import com.codahale.usl4j.Measurement;
import com.codahale.usl4j.Model;
import java.util.Arrays;

class Example {
  void buildModel() {
    final double[][] points = {{1, 4227}, {2, 8382}, {4, 16479}}; // etc.
  
    // Map the points to measurements of throughput and build a model. If you'd measured mean
    // response times instead of throughput, you could use Measurement::ofConcurrencyAndLatency or
    // even Measurement::ofThroughputAndLatency.
    final Model model = Arrays.stream(points)
                              .map(Measurement::ofConcurrencyAndThroughput)
                              .collect(Model.toModel());
    for (int i = 10; i < 200; i++) {
      System.out.printf("At %d workers, expect %f req/sec\n", i, model.throughputAtConcurrency(i));
    }
  }
}
```

## Further reading

I strongly recommend [Practical Scalability Analysis with the Universal Scalability
Law](https://www.vividcortex.com/resources/universal-scalability-law/), a free e-book by Baron
Schwartz, author of _High Performance MySQL_ and CEO of VividCortex. Trying to use this library
without actually understanding the concepts behind Little's Law, Amdahl's Law, and the Universal
Scalability Law will be difficult and potentially misleading.

## License

Copyright © 2017 Coda Hale

Distributed under the Apache License 2.0.
