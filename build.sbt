name := "scala-candle-task"

version := "0.1"

scalaVersion := "2.13.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.10",
  "org.scalatest" %% "scalatest" % "3.0.8" % "test",
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.23",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.10"
)