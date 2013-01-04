seq(ProguardPlugin.proguardSettings: _*)

proguardOptions += keepMain("com.goldblastgames.themole.Server")

libraryDependencies ++= Seq(
  "com.chuusai" %% "shapeless" % "1.2.2"
)
