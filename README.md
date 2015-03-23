biz.dfch.j.graylog.plugin.filter.metricsvalidation
==================================================

Plugin: biz.dfch.j.graylog.plugin.filter.metricsvalidation

d-fens GmbH, General-Guisan-Strasse 6, CH-6300 Zug, Switzerland

This Graylog Filter Plugin lets you validate data type and value ranges on GELF message properties.


You can [download the binary](https://drone.io/github.com/dfch/biz.dfch.j.graylog.plugin.filter.metricsvalidationfiles) [![Build Status](https://drone.io/github.com/dfch/biz.dfch.j.graylog.plugin.filter.metricsvalidation/status.png)](https://drone.io/github.com/dfch/biz.dfch.j.graylog.plugin.filter.metricsvalidation/latest) from our [drone.io](https://drone.io/github.com/dfch) account.

Getting started for users
-------------------------

This project is using Maven and requires Java 7 or higher.

* Clone this repository.
* Run `mvn package` to build a JAR file.
* Optional: Run `mvn jdeb:jdeb` and `mvn rpm:rpm` to create a DEB and RPM package respectively.
* Copy generated jar file in target directory to your graylog server plugin directory.
* Restart the graylog server.
