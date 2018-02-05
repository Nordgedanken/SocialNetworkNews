name := "SocialNetworkNews"
version := "1.0"
// general package information (can be scoped to Windows)
maintainer := "MTRNord <freifunknews@nordgedanken.blog>"
packageSummary := "SocialNetworkNews"
packageDescription := """A paper.li clone"""

// wix build information
wixProductId := "2d7d2fcd-bf87-4daf-99dc-279d1825f089"
wixProductUpgradeId := "76b4ecc2-4d3e-4a59-bd63-e67e6cb29806"
wixProductLicense := Some(file("LICENSE.rtf"))

scalaVersion := "2.12.3"

// set the main class for packaging the main jar
// 'run' will still auto-detect and prompt
// change Compile to Test to set it for the test jar
mainClass in (Compile, packageBin) := Some("Main")

// set the main class for the main 'run' task
// change Compile to Test to set it for 'test:run'
mainClass in (Compile, run) := Some("Main")

resolvers += Resolver.sonatypeRepo("snapshots")
libraryDependencies ++= Seq(
  "com.danielasfregola" %% "twitter4s" % "5.5-SNAPSHOT",
  "com.typesafe" % "config" % "1.3.2",
  "ch.qos.logback" % "logback-classic" % "1.1.9",
  "com.typesafe.slick" %% "slick" % "3.2.1",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.1"
)
enablePlugins(JavaAppPackaging)
scalacOptions in (Compile,doc) := Seq("-groups", "-implicits")