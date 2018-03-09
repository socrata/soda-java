import com.typesafe.tools.mima.plugin.MimaPlugin.mimaDefaultSettings

import com.typesafe.tools.mima.plugin.MimaKeys.previousArtifact

mimaDefaultSettings

scalaVersion := "2.10.5"

organization := "com.socrata"

name := "soda-api-java"

// remember to update the README when you change this to a new release
version := "0.10.1"

previousArtifact := Some("com.socrata" % "soda-api-java" % "0.9.13")

javacOptions in compile ++= Seq("-g", "-Xlint:deprecation", "-Xlint:unchecked", "-target", "1.7", "-source", "1.7")

autoScalaLibrary := false

crossPaths := false

libraryDependencies ++= Seq(
  "net.sf.opencsv" % "opencsv" % "2.0",
  "joda-time" % "joda-time" % "2.1",
  "org.glassfish.jersey.core" % "jersey-client" % "2.26",
  "org.glassfish.jersey.media" % "jersey-media-multipart" % "2.26",
  "org.glassfish.jersey.media" % "jersey-media-json-jackson" % "2.26",
  "org.glassfish.jersey.inject" % "jersey-hk2" % "2.26",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.8.6",
  "com.fasterxml.jackson.module" % "jackson-module-jaxb-annotations" % "2.8.6",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-joda" % "2.8.6",
  "com.fasterxml.jackson.jaxrs" % "jackson-jaxrs-json-provider" % "2.8.6",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jaxrs" % "2.8.6",
  "com.google.guava" % "guava" % "12.0",
  "org.apache.commons" % "commons-lang3" % "3.1",
  "commons-beanutils" % "commons-beanutils" % "1.8.3",
  "commons-io" % "commons-io" % "1.3.2",
  "javax.ws.rs" % "javax.ws.rs-api" % "2.1" artifacts(Artifact("javax.ws.rs-api", "jar", "jar")), // this is a workaround for https://github.com/jax-rs/api/issues/571
  "com.novocode" % "junit-interface" % "0.9" % "test"
)

parallelExecution in Test := false

logBuffered in Test := false

testOptions in Test ++= Seq(
  Tests.Argument(TestFrameworks.JUnit, "-v")
)

sourceGenerators in Compile += Def.task {
  val targetDir = (sourceManaged in Compile).value / "com" / "socrata" / "api"
  targetDir.mkdirs()
  val target = targetDir / "APIVersion.java"
  val out = new java.io.FileWriter(target)
  try {
    out.write(s"""
package com.socrata.api;
class APIVersion {
  static final String version = ${com.rojoma.json.v3.ast.JString(version.value)};
}
""")
  } finally {
    out.close()
  }
  Seq(target)
}.taskValue
