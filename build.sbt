import Dependencies._
import common._
import Defaults._
import com.typesafe.sbt.pgp.PgpKeys._

organization in ThisBuild := "com.madgag"

scalaVersion in ThisBuild := "2.12.4"

scalacOptions in ThisBuild ++= Seq("-deprecation", "-feature", "-language:postfixOps")

licenses in ThisBuild := Seq("GPLv3" -> url("http://www.gnu.org/licenses/gpl-3.0.html"))

homepage in ThisBuild := Some(url("https://github.com/youngde811/gitclean"))

resolvers in ThisBuild ++= jgitVersionOverride.map(_ => Resolver.mavenLocal).toSeq

libraryDependencies in ThisBuild += scalatest % "test"

lazy val root = Project(id = "gitclean-parent", base = file(".")) aggregate (gitclean, gitcleanTest, gitcleanLibrary)

releaseSignedArtifactsSettings

publishSigned := {}

lazy val gitcleanTest = bfgProject("gitclean-test")

lazy val gitcleanLibrary = gitcleanProject("gitclean-library") dependsOn(gitcleanTest % "test")

lazy val gitclean = gitcleanProject("gitclean") enablePlugins(BuildInfoPlugin) dependsOn(gitcleanLibrary, gitcleanTest % "test")

lazy val gitcleanBenchmark = gitcleanProject("gitclean-benchmark")

publishMavenStyle in ThisBuild := true

publishTo in ThisBuild :=
  Some(if (isSnapshot.value) Opts.resolver.sonatypeSnapshots else Opts.resolver.sonatypeStaging)

pomIncludeRepository in ThisBuild := { _ => false }

pomExtra in ThisBuild := (
  <scm>
    <url>git@github.com:youngde811/gitclean.git</url>
    <connection>scm:git:git@github.com:youngde811/gitclean.git</connection>
  </scm>
    <developers>
      <developer>
        <id>youngde811</id>
        <name>David Young</name>
        <url>https://github.com/youngde811</url>
      </developer>
    </developers>
)
