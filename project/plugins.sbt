//addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.1.3")
addSbtPlugin("org.jmotor.sbt" % "sbt-dependency-updates" % "1.2.2")
addSbtPlugin("ch.epfl.scala"  % "sbt-scalafix"           % "0.9.32")
addSbtPlugin("org.scalameta"  % "sbt-scalafmt"           % "2.4.3")
addSbtPlugin("io.spray"       % "sbt-revolver"           % "0.9.1")
addSbtPlugin("com.github.sbt" % "sbt-native-packager"    % "1.9.6")
addSbtPlugin("com.thesamet"   % "sbt-protoc"             % "1.0.4")

libraryDependencies +=
  "com.thesamet.scalapb.zio-grpc" %% "zio-grpc-codegen" % "0.5.1"
