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

import lib.Timing.measureTask
import lib._
import model._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration.Duration
import scala.sys.process._
import scalax.file.PathMatcher.IsDirectory
import scalax.io.Codec

/*
 * Vary Gitclean runs by:
 * Java version
 * Gitclean version (JGit version?)
 *
 */
object Benchmark extends App {
  implicit val codec = Codec.UTF8

  BenchmarkConfig.parser.parse(args, BenchmarkConfig()) map {
    config =>
      println(s"Using resources dir : ${config.resourcesDir.path}")

      require(config.resourcesDir.exists, s"Resources dir not found : ${config.resourcesDir.path}")
      require(config.jarsDir.exists, s"Jars dir not found : ${config.jarsDir.path}")
      require(config.reposDir.exists, s"Repos dir not found : ${config.reposDir.path}")

      val missingJars = config.gitcleanJars.filterNot(_.exists).map(_.toAbsolute.path)

      require(missingJars.isEmpty, s"Missing Gitclean jars : ${missingJars.mkString(",")}")

      val tasksFuture = for {
        gitcleanInvocableEngineSet <- gitcleanInvocableEngineSet(config)
      } yield {
        val gfbInvocableEngineSetOpt =
          if (config.onlyGitclean) None else Some(InvocableEngineSet[GFBInvocation](GitFilterBranch, Seq(InvocableGitFilterBranch)))
        boogaloo(config, new RepoExtractor(config.scratchDir), Seq(gitcleanInvocableEngineSet) ++ gfbInvocableEngineSetOpt.toSeq)
      }

      Await.result(tasksFuture, Duration.Inf)
  }

  def gitcleanInvocableEngineSet(config: BenchmarkConfig): Future[InvocableEngineSet[GitcleanInvocation]] = for {
      javas <- Future.traverse(config.javaCmds)(jc => JavaVersion.version(jc).map(v => Java(jc, v)))
    } yield {
      val invocables = for {
        java <- javas
        gitcleanJar <- config.gitcleanJars
      } yield InvocableGitclean(java, GitcleanJar.from(gitcleanJar))

      InvocableEngineSet[GitcleanInvocation](Gitclean, invocables)
    }

  /*
   * A Task says "here is something you can do to a given repo, and here is how to do
   * it with Gitclean, and with git-filter-branch"
   */

  def boogaloo(config: BenchmarkConfig, repoExtractor: RepoExtractor, invocableEngineSets: Seq[InvocableEngineSet[_ <: EngineInvocation]]) = {
    for {
      repoSpecDir <- config.repoSpecDirs.toList
      availableCommandDirs = (repoSpecDir / "commands").children().filter(IsDirectory).toList
      commandDir <- availableCommandDirs.filter(p => config.commands(p.name))
    } yield {

      val repoName = repoSpecDir.name
      val commandName = commandDir.name
      
      commandName -> (for {
        invocableEngineSet <- invocableEngineSets
      } yield for {
          (invocable, processMaker) <- invocableEngineSet.invocationsFor(commandDir)
        } yield {
        val cleanRepoDir = repoExtractor.extractRepoFrom(repoSpecDir / "repo.git.zip")

        commandDir.children().foreach(p => p.copyTo(cleanRepoDir / p.name))

        val process = processMaker(cleanRepoDir)
        val duration = measureTask(s"$commandName - $invocable") {
          process ! ProcessLogger(_ => Unit)
        }

        if (config.dieIfTaskTakesLongerThan.exists(_ < duration.toMillis)) {
          throw new Exception("This took too long: "+duration)
        }

        invocable -> duration
      })
    }
  }

  println(s"\n...benchmark finished.")
}
