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

import java.io.File

import com.madgag.textmatching.{Glob, TextMatcher}
import scopt.OptionParser

import scalax.file.ImplicitConversions._
import scalax.file.Path
import scalax.file.defaultfs.DefaultPath

object BenchmarkConfig {
  val parser = new OptionParser[BenchmarkConfig]("benchmark") {
    opt[File]("resources-dir").text("benchmark resources folder - contains jars and repos").action {
      (v, c) => c.copy(resourcesDirOption = v)
    }
    opt[String]("java").text("Java command paths").action {
      (v, c) => c.copy(javaCmds = v.split(',').toSeq)
    }
    opt[String]("versions").text("Gitclean versions to time - gitclean-[version].jar - eg 1.4.0,1.5.0,1.6.0").action {
      (v, c) => c.copy(gitcleanVersions = v.split(",").toSeq)
    }
    opt[Int]("die-if-longer-than").text("Useful for git-bisect").action {
      (v, c) => c.copy(dieIfTaskTakesLongerThan = Some(v))
    }
    opt[String]("repos").text("Sample repos to test, eg github-gems,jgit,git").action {
      (v, c) => c.copy(repoNames = v.split(",").toSeq)
    }
    opt[String]("commands").valueName("<glob>").text("commands to exercise").action {
      (v, c) => c.copy(commands = TextMatcher(v, defaultType = Glob))
    }
    opt[File]("scratch-dir").text("Temp-dir for job runs - preferably ramdisk, eg tmpfs.").action {
      (v, c) => c.copy(scratchDir = v)
    }
    opt[Unit]("only-gitclean") action { (_, c) => c.copy(onlyGitclean = true) } text "Don't benchmark git-filter-branch"
  }
}
case class BenchmarkConfig(resourcesDirOption: Path = Path.fromString(System.getProperty("user.dir")) / "gitclean-benchmark" / "resources",
                           scratchDir: DefaultPath = Path.fromString("/dev/shm/"),
                           javaCmds: Seq[String] = Seq("java"),
                           gitcleanVersions: Seq[String] = Seq.empty,
                           commands: TextMatcher = Glob("*"),
                           onlyGitclean: Boolean = false,
                           dieIfTaskTakesLongerThan: Option[Int] = None,
                           repoNames: Seq[String] = Seq.empty) {

  lazy val resourcesDir = Path.fromString(resourcesDirOption.path).toAbsolute
  lazy val jarsDir = resourcesDir / "jars"
  lazy val reposDir = resourcesDir / "repos"
  lazy val gitcleanJars = gitcleanVersions.map(version => jarsDir / s"gitclean-$version.jar")
  lazy val repoSpecDirs = repoNames.map(reposDir / _)
}
