addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8.3")

addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.1.7")

libraryDependencies ++= Seq(
  "com.rojoma" %% "rojoma-json-v3" % "3.7.2"
)
