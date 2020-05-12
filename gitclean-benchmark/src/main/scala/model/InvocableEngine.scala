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

package model

import scala.sys.process.{Process, ProcessBuilder}
import scalax.file.ImplicitConversions._
import scalax.file.Path
import scalax.file.defaultfs.DefaultPath
import scalax.io.Input

trait EngineInvocation

case class GitcleanInvocation(args: String) extends EngineInvocation

case class GFBInvocation(args: Seq[String]) extends EngineInvocation

trait InvocableEngine[InvocationArgs <: EngineInvocation] {
    def processFor(invocation: InvocationArgs)(repoPath: DefaultPath): ProcessBuilder
}

case class InvocableGitclean(java: Java, gitcleanJar: GitcleanJar) extends InvocableEngine[GitcleanInvocation] {
  def processFor(invocation: GitcleanInvocation)(repoPath: DefaultPath) =
    Process(s"${java.javaCmd} -jar ${gitcleanJar.path.path} ${invocation.args}", repoPath)
}

object InvocableGitFilterBranch extends InvocableEngine[GFBInvocation] {
  def processFor(invocation: GFBInvocation)(repoPath: DefaultPath) =
    Process(Seq("git", "filter-branch") ++ invocation.args, repoPath)
}

/*
We want to allow the user to vary:
 - Gitcleans (jars, javas)
 - Tasks (delete a file, replace text) in [selection of repos]

 Tasks will have a variety of different invocations for different engines
 */

trait EngineType[InvocationType <: EngineInvocation] {
  val configName: String

  def argsFor(config: Input): InvocationType

  def argsOptsFor(commandDir: Path): Option[InvocationType] = {
    val paramsPath = commandDir / s"$configName.txt"

    if (paramsPath.exists) Some(argsFor(paramsPath)) else None
  }
}

case object Gitclean extends EngineType[GitcleanInvocation] {
  val configName = "gitclean"

  def argsFor(config: Input) = GitcleanInvocation(config.string)
}

case object GitFilterBranch extends EngineType[GFBInvocation] {
  val configName = "gfb"

  def argsFor(config: Input) = GFBInvocation(config.lines().toSeq)
}
