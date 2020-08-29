name := "SafeStorm"

version := "1.0"


sbtVersion in Global := "1.3.12"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Maven Repository" at "http://mvnrepository.com/artifact/"

resolvers += "clojars" at "https://clojars.org/repo"


libraryDependencies += "org.apache.storm" % "storm-core" % "1.2.2" % "provided" exclude("junit", "junit")

scalacOptions ++= Seq("-feature", "-deprecation", "-Yresolve-term-conflict:package")

