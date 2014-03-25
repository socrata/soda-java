publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

// Random stuff sonatype wants
pomExtra := (
  <url>http://www.github.com/socrata/soda-java</url>
  <licenses>
    <license>
      <name>The MIT License (MIT)</name>
      <url>http://opensource.org/licenses/mit-license.php</url>
       <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <connection>scm:git:git@github.com:socrata/soda-java.git</connection>
    <url>https://github.com/socrata/soda-java</url>
  </scm>
  <developers>
    <developer>
      <name>Will Pugh</name>
      <email>will.pugh@socrata.com</email>
      <organization>Socrata</organization>
    </developer>
  </developers>
)

// Random stuff sonatype does not want
pomIncludeRepository := { _ => false }

// These are the defaults, but set them manually just in case

publishMavenStyle := true

publishArtifact in Test := false
