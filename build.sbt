name := "coffee-machine"

version := "0.1"

scalaVersion := "2.13.5"


libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.6.13"
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.6.13" % Test
libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.30"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.30" % Test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.5" % Test