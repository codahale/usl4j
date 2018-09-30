# Change Log

## v0.7.0: 2018-09-29

* Upgraded to DDogleg 0.15, which features a new non-linear solver.

## v0.6.1: 2018-09-26

* JAR now specifies an automatic module name of `com.codahale.usl4j`.

## v0.6.0: 2018-09-25

* Moved away from AutoValue classes to more regular API.
* Dropped JSR-250 annotation dependency.
* Upgraded DDogleg.

## v0.5.4: 2018-03-30

* Dropped dependency on annotations.

## v0.5.3: 2017-10-27

* Fixed argument transposition in `Measurement.ofThroughput().andConcurency(double, double)`.

## v0.5.2: 2017-07-16

* Upgraded DDogleg.

## v0.5.1: 2017-04-27

* Added bounds checking for building measurements from `double[]` instances.

## v0.5.0: 2017-04-26

* Moved measurement builders to their own static classes.

## v0.4.0: 2017-04-26

* Refactored builder methods for measurements.

## v0.3.0: 2017-04-23

* Switched to DDogleg Numerics for least-squares regression.

## v0.2.1: 2017-04-22

* Improved `#toString` implementation of `Measurement` to reflect the changes in v0.2.0.

## v0.2.0: 2017-04-22

* Changed API of `Measurement` to support creation given any two of the parameters of Little's Law.

## v0.1.2: 2017-04-22

* Improved documentation.

## v0.1.0: 2017-04-21

* Initial release.