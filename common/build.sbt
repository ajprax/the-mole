resolvers += "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots"

resolvers += "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases"

libraryDependencies += "com.github.oetzi" %% "echo" % "1.1.0"

libraryDependencies += "commons-io" % "commons-io" % "2.4"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.6.6"

libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.6.6"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "6.0.4"

libraryDependencies += "org.specs2" %% "specs2" % "1.12.1" % "test"

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.10.0" % "test"

libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.1" % "test"
