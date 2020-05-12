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

import java.io.{File, FileOutputStream}

import Dependencies._
import sbt.taskKey

import scala.sys.process.Process
import scala.util.Try

val gitDescription = taskKey[String]("Git description of working dir")

gitDescription := Try[String](Process("git describe --all --always --dirty --long").lineStream.head.replace("heads/","").replace("-0-g","-")).getOrElse("unknown")

libraryDependencies += useNewerJava

mainClass := Some("use.newer.java.Version8")
packageOptions in (Compile, packageBin) +=
  Package.ManifestAttributes( "Main-Class-After-UseNewerJava-Check" -> "com.madgag.git.gitclean.cli.Main" )

// note you don't want the jar name to collide with the non-assembly jar, otherwise confusion abounds.

assemblyJarName in assembly := s"${name.value}-${version.value}-${gitDescription.value}${jgitVersionOverride.map("-jgit-" + _).mkString}.jar"

buildInfoKeys := Seq[BuildInfoKey](version, scalaVersion, gitDescription)

buildInfoPackage := "com.madgag.git.gitclean"

crossPaths := false

publishArtifact in (Compile, packageBin) := false

// replace the conventional main artifact with an uber-jar
addArtifact(artifact in (Compile, packageBin), assembly)

val cliUsageDump = taskKey[File]("Dump the CLI 'usage' output to a file")

cliUsageDump := {
  val usageDumpFile = File.createTempFile("gitclean-usage", "dump.txt")
  val scalaRun = new ForkRun(ForkOptions().withOutputStrategy(CustomOutput(new FileOutputStream(usageDumpFile))))

  val mainClassName = (mainClass in (Compile, run)).value getOrElse sys.error("No main class detected.")
  val classpath = Attributed.data((fullClasspath in Runtime).value)
  val args = Seq.empty

  scalaRun.run(mainClassName, classpath, args, streams.value.log).failed foreach (sys error _.getMessage)
  usageDumpFile
}

addArtifact( Artifact("gitclean", "usage", "txt"), cliUsageDump )

libraryDependencies ++= Seq(
  scopt,
  jgit,
  scalaGitTest % "test"
)

import Tests._
{
  def isolateTestsWhichRequireTheirOwnJvm(tests: Seq[TestDefinition]) = {
    val (testsRequiringIsolation, testsNotNeedingIsolation) = tests.partition(_.name.contains("RequiresOwnJvm"))

    val groups: Seq[Seq[TestDefinition]] = testsRequiringIsolation.map(Seq(_)) :+ testsNotNeedingIsolation

    groups map { group =>
      Group(group.size.toString, group, SubProcess(ForkOptions()))
    }
  }

  testGrouping in Test := isolateTestsWhichRequireTheirOwnJvm( (definedTests in Test).value )
}

fork in Test := true // JGit uses static (ie JVM-wide) config

logBuffered in Test := false

parallelExecution in Test := false

