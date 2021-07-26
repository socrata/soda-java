addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.1")

addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.9.1")

libraryDependencies ++= Seq(
  "com.rojoma" %% "rojoma-json-v3" % "3.7.2"
)
