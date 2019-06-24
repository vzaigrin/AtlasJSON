name := "AtlasJSON"

version := "1.0"

scalaVersion := "2.13.0"

libraryDependencies ++= Seq(
  "io.circe" % "circe-core_2.13" % "0.12.0-M3",
  "io.circe" % "circe-generic_2.13" % "0.12.0-M3",
  "io.circe" % "circe-parser_2.13" % "0.12.0-M3",
  "org.yaml" % "snakeyaml" % "1.24",
  "org.snakeyaml" % "snakeyaml-engine" % "1.0",
  "org.apache.poi" % "poi-ooxml" % "4.1.0",
  "org.scalatest" % "scalatest_2.13" % "3.0.8"
)