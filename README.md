biz.dfch.j.graylog.plugin.filter.metricsvalidation
==================================================

Plugin: biz.dfch.j.graylog.plugin.filter.metricsvalidation

d-fens GmbH, General-Guisan-Strasse 6, CH-6300 Zug, Switzerland

This Graylog Filter Plugin lets you validate data type and value ranges on GELF message properties.


You can [download the binary](https://drone.io/github.com/dfch/biz.dfch.j.graylog.plugin.filter.metricsvalidation/files) [![Build Status](https://drone.io/github.com/dfch/biz.dfch.j.graylog.plugin.filter.metricsvalidation/status.png)](https://drone.io/github.com/dfch/biz.dfch.j.graylog.plugin.filter.metricsvalidation/latest) from our [drone.io](https://drone.io/github.com/dfch) account.

Getting started for users
-------------------------

This project is using Maven and requires Java 7 or higher.

* Clone this repository.
* Run `mvn package` to build a JAR file.
* Optional: Run `mvn jdeb:jdeb` and `mvn rpm:rpm` to create a DEB and RPM package respectively.
* Copy generated jar file in target directory to your graylog server plugin directory.
* Restart the graylog server.

Configuration file
------------------

You need to define a configuration file that resides in the same directory as the plugin jar. This file must have the same ase name as the jar file and the suffix `conf`. You can find an example in the `resources` folder.

For each metric you want to validate you define an entry like this one:

```
    "cpu.maximum" :
    {
        "minValue":  0
        ,
        "maxValue":  100
        ,
        "type":  "double"
    }
```

In case the field/property of the GELF message does not match the data type or the value of the field/property is outside the defined range the message will be dropped.
