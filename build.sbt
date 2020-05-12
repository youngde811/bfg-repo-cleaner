/*
 * Copyright (c) 2020 David Young (youngde811@pobox.com)
 *
 * This file is part of Gitclean - a tool for removing large or troublesome blobs
 * from Git repositories. It is a fork from the original BFG Repo-Cleaner by
 * Roberto Tyley.
 * 
 * Gitclean is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gitclean is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/ .
 */

/*
 * Copyright (c) 2012, 2013 Roberto Tyley
 *
 * This file is part of 'BFG Repo-Cleaner' - a tool for removing large
 * or troublesome blobs from Git repositories.
 *
 * BFG Repo-Cleaner is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BFG Repo-Cleaner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/ .
 */

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

lazy val gitcleanTest = gitcleanProject("gitclean-test")

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
