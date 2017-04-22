# usl4j

[![Build Status](https://secure.travis-ci.org/codahale/usl4j.svg)](http://travis-ci.org/codahale/usl4j)

usl4j is Java modeler for the Universal Scalability Law, which can be used in system testing and
capacity planning.

## Add to your project

```xml
<dependency>
  <groupId>com.codahale</groupId>
  <artifactId>usl4j</artifactId>
  <version>0.1.0</version>
</dependency>
```

It depends on the [Efficient Java Matrix Library](https://github.com/lessthanoptimal/ejml) for matrix
operations. If someone has a less funky way to emulate [R's `nls`
function](https://github.com/smoeding/usl), I'd love to hear about it.

## How to use this

As an example, consider doing load testing and capacity planning for an HTTP server. To use USL, we
must first gather a set of measurements of the system. These measurements will consist of pairs of
simultaneous measurements of the independent and dependent variables. With an HTTP server, it might
be tempting to use the rate as the independent variable, but this is a mistake. The rate of requests
being handled by the server is actually itself a dependent variable of two other independent
variables: the number of concurrent users and the rate at which users send requests.

As we do our capacity planning, we make the observation that users of our system do ~10 req/sec.
(Or, more commonly, we assume this based on a hunch.) By holding this constant, we leave the number
of concurrent users as the single remaining independent variable.

Our load testing, then, should consist of running a series of tests with an increasing number of
simulated users, each performing ~10 req/sec. While the number of users to test with depends heavily
on your system, you should be testing at least six different concurrency levels. You should do one
test with a single user in order to determine the performance of an uncontended system.

After our load testing is done, we should have a set of measurements shaped like this:

|concurrency|throughput|
|-----------|----------|
|          1|      4227|
|          2|      8382|
|          4|     16479|
|          8|     31856|
|         16|     59564|
|         32|    104462|
|         64|    162985|

For simplicity's sake, let's assume we're storing this as a `double[][]`. Now we can build our
model and begin estimating things:

```java
import com.codahale.usl4j.Measurement;
import com.codahale.usl4j.Model;
import java.util.Arrays;

class Example {
  void buildModel() {
    double[][] points = {{1, 4227}, {2, 8382}, {4, 16479}}; // etc.
  
    // Map the points to measurements of throughput and build a model. If you'd measured average
    // response times instead of throughput, you could use Measurement::latency.
    Model model = Arrays.stream(points).map(Measurement::throughput).collect(Model.toModel());
    
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
