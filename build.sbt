import sbt._
import Process._
import Keys._

name := "SafeStorm"

version := "1.0"


scalaVersion := "2.11.4"

resolvers += "Maven Repository" at "http://mvnrepository.com/artifact/"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "clojars" at "https://clojars.org/repo"

libraryDependencies += "com.github.velvia" %% "scala-storm" % "0.2.5-SNAPSHOT"

libraryDependencies += "org.apache.storm" % "storm-core" % "0.9.3" % "provided" exclude("junit", "junit")

// resolvers ++= Seq("clojars" at "http://clojars.org/repo/",
//                  "clojure-releases" at "http://build.clojure.org/releases")
