ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.13"

lazy val root = (project in file("."))
  .settings(
    name := "MainFunctions"
  )
// oracle database
libraryDependencies += "com.oracle.database.jdbc" % "ojdbc8" % "21.1.0.0"

// configuration data
libraryDependencies += "com.typesafe" % "config" % "1.4.3"

// Parallel Collection
libraryDependencies += "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4"
