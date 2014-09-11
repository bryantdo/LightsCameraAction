LightsCameraAction
==================

Plugin for Micro-Manger to tightly integrate imaging through the Micro-Manager API with contorl of Heliospectra lights via Telnet.

Description
-----------
This software is a plugin for Micro-Manger (https://www.micro-manager.org/) used to enable control of Heliospectra lights (http://www.heliospectra.com/) and an imaging platform with high time-precision. Using this plugin one can orchestrate timing of lights actions with imaging actions over some period and under an arbitraty schedule.

Assumptions
-----------
We built and tested this software using Micro-Manager versions 1.4.16 and and 1.4.18. One should be careful to compile this software using the same Java version as the version of the Java VM used to run Micro-Manager. For example, if this software is built using version 1.7 of the Java SDK, be sure that your instalation of Micro-Manager is being run on version 1.7 of the Java VM. This can be tricky because, for example, on OS X, one's Netbeans platform may be configured to build using version 1.7 of the Java SDK even though the system default Java version is 1.6.

This project was developed using Maven. As the Micro-Manager and ImageJ Java libraries are imported into the classpath based one's your Micro-Manager install location, be sure to edit the project's pom.xml file to correctly point to these libraries. The only other outside dependency is Joda-Time which should automatically be downloaded from a public Maven repository.

To use this plugin, copy the compiled Jar file to your Micro-Manager-Install/mmplugins directory, which by default on OS X is found at /Applications/Micro-Manager1.4/mmplugins.
