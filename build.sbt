name := """play-akka-messenger"""

organization := "orion.io"

version := "0.1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.8"

libraryDependencies += guice
libraryDependencies += ws
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies += "orion.io" %% "akka-messenger" % "0.1.0"

val akkaVersion = "2.5.19"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion)

libraryDependencies += "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % "0.20.0"
libraryDependencies += "com.lightbend.akka.discovery" %% "akka-discovery-dns" % "0.20.0"
dependencyOverrides += "com.typesafe" %% "akka-slf4j" % akkaVersion
