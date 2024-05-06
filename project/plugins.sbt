//addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.1.3")
addSbtPlugin("org.jmotor.sbt" % "sbt-dependency-updates" % "1.2.2")
addSbtPlugin("ch.epfl.scala"  % "sbt-scalafix"           % "0.12.1")
addSbtPlugin("org.scalameta"  % "sbt-scalafmt"           % "2.4.6")
addSbtPlugin("io.spray"       % "sbt-revolver"           % "0.9.1")
addSbtPlugin("com.github.sbt" % "sbt-native-packager"    % "1.9.9")
addSbtPlugin("com.thesamet"   % "sbt-protoc"             % "1.0.6")

libraryDependencies +=
  "com.thesamet.scalapb.zio-grpc" %% "zio-grpc-codegen" % "0.5.2"
