import sbtassembly.AssemblyPlugin

ThisBuild / version := "0.1.0-SNAPSHOT"
//ThisBuild / scalaVersion := "2.13.15"
ThisBuild / scalaVersion := "2.12.18"

ThisBuild / libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.5.3",
  "org.apache.spark" %% "spark-sql" % "3.5.3"
)


//ThisBuild / resolvers += Resolver.mavenCentral
//ThisBuild / resolvers += Resolver.url("bintray-sbt-plugins", url("https://scala.jfrog.io/ui/native/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)

//ThisBuild / externalResolvers := Resolver.url("bintray-sbt-plugins", url("https://scala.jfrog.io/ui/native/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)
/*
ThisBuild / externalResolvers := Seq(
  "bintray-sbt-plugins" at "https://scala.jfrog.io/ui/native/sbt-plugin-releases/",
  // some more internal Nexus repositories
)

 */



//ThisBuild / assemblyMergeStrategy



assemblyMergeStrategy in assembly := {
  case PathList("META-INF", _*) => MergeStrategy.discard
  case _                        => MergeStrategy.first
}

lazy val root = (project in file("."))
  .settings(
    name := "scalable-copurchase"
  )



