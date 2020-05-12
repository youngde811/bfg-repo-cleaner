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
 * Copyright (c) 2012 Roberto Tyley
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

import sbt._

object Dependencies {
  val scalaGitVersion = "4.0"
  val jgitVersionOverride = Option(System.getProperty("jgit.version"))
  val jgitVersion = jgitVersionOverride.getOrElse("4.4.1.201607150455-r")
  val jgit = "org.eclipse.jgit" % "org.eclipse.jgit" % jgitVersion

  // the 1.7.2 here matches slf4j-api in jgit's dependencies

  val slf4jSimple = "org.slf4j" % "slf4j-simple" % "1.7.2"

  val scalaGit = "com.madgag.scala-git" %% "scala-git" % scalaGitVersion exclude("org.eclipse.jgit", "org.eclipse.jgit")
  val scalaGitTest = "com.madgag.scala-git" %% "scala-git-test" % scalaGitVersion
  val scalatest = "org.scalatest" %% "scalatest" % "3.0.4"
  val madgagCompress = "com.madgag" % "util-compress" % "1.33"
  val textmatching = "com.madgag" %% "scala-textmatching" % "2.3"
  val scopt = "com.github.scopt" %% "scopt" % "3.5.0"
  val guava = Seq("com.google.guava" % "guava" % "19.0", "com.google.code.findbugs" % "jsr305" % "2.0.3")
  val scalaIoFile = "com.madgag" %% "scala-io-file" % "0.4.9"
  val useNewerJava =  "com.madgag" % "use-newer-java" % "0.1"
}
