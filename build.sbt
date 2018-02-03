name := "FreifunkNews"

version := "1.0"

scalaVersion := "2.12.3"

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "com.danielasfregola" %% "twitter4s" % "5.5-SNAPSHOT",
  "com.typesafe" % "config" % "1.3.2",
  "ch.qos.logback" % "logback-classic" % "1.1.9"
)
