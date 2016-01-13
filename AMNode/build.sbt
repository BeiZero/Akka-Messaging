name := "AMNode"

version := "1.0"

scalaVersion := "2.11.7"

val akkaVersion = "2.4.0"


libraryDependencies += "com.typesafe.akka" %% "akka-actor" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-remote" % akkaVersion
    