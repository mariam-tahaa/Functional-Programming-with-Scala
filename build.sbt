ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.13"

lazy val root = (project in file("."))
  .settings(
    name := "MainFunctions"
  )
libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.2.12" )

libraryDependencies += "com.oracle.database.jdbc" % "ojdbc8" % "21.1.0.0"

