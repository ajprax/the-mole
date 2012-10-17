libraryDependencies ++= Seq(
  "net.databinder" %% "unfiltered-filter" % "0.6.4",
  "net.databinder" %% "unfiltered-jetty" % "0.6.4",
  "org.clapper" %% "avsl" % "0.4"
)

resolvers ++= Seq(
  "java m2" at "http://download.java.net/maven/2"
)
