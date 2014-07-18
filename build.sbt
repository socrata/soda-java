import com.typesafe.tools.mima.plugin.MimaPlugin.mimaDefaultSettings

import com.typesafe.tools.mima.plugin.MimaKeys.previousArtifact

mimaDefaultSettings

organization := "com.socrata"

name := "soda-api-java"

// remember to update the README when you change this to a new release
version := "0.9.13-SNAPSHOT"

previousArtifact := Some("com.socrata" % "soda-api-java" % "0.9.11")

javacOptions in compile ++= Seq("-g", "-Xlint:deprecation", "-Xlint:unchecked")

autoScalaLibrary := false

crossPaths := false

libraryDependencies ++= Seq(
  "net.sf.opencsv" % "opencsv" % "2.0",
  "joda-time" % "joda-time" % "2.1",
  "com.sun.jersey" % "jersey-bundle" % "1.9.1",
  "com.sun.jersey.contribs" % "jersey-multipart" % "1.9.1",
  "org.codehaus.jackson" % "jackson-mapper-asl" % "1.8.3",
  "org.codehaus.jackson" % "jackson-jaxrs" % "1.8.3",
  "org.codehaus.jackson" % "jackson-xc" % "1.8.3",
  "com.google.guava" % "guava" % "12.0",
  "org.apache.commons" % "commons-lang3" % "3.1",
  "commons-beanutils" % "commons-beanutils" % "1.8.3",
  "commons-io" % "commons-io" % "1.3.2",
  "com.novocode" % "junit-interface" % "0.9" % "test",
  "junit-addons" % "junit-addons" % "1.4" % "test"
)

parallelExecution in Test := false

logBuffered in Test := false

testOptions in Test ++= Seq(
  Tests.Argument(TestFrameworks.JUnit, "-v")
)
