import sbt._
import Process._
import Keys._

name := "SafeStorm"

version := "1.0"

// scalaVersion := "2.12.6"

resolvers += "Maven Repository" at "http://mvnrepository.com/artifact/"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "clojars" at "https://clojars.org/repo"

libraryDependencies += "org.apache.storm" % "storm-core" % "1.2.2" % "provided" exclude("junit", "junit")

scalacOptions ++= Seq("-feature", "-deprecation", "-Yresolve-term-conflict:package")

