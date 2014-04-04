import sbt._
import Process._
import Keys._

name := "SafeStorm"

version := "1.0"


scalaVersion := "2.9.1"


libraryDependencies += "com.github.velvia" %% "scala-storm" % "0.2.2"

resolvers ++= Seq("clojars" at "http://clojars.org/repo/",
                  "clojure-releases" at "http://build.clojure.org/releases")
