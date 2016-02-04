name := "pagerank"

version := "0.1"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "org.apache.spark" % "spark-core_2.10" % "1.5.2",
  "org.apache.spark" % "spark-graphx_2.10" % "1.5.2"
)

unmanagedBase <<= baseDirectory { base => base / "libs" }
