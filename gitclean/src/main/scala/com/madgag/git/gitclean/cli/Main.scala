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

package com.madgag.git.gitclean.cli

import com.madgag.git._
import com.madgag.git.gitclean.GitUtil._
import com.madgag.git.gitclean.cleaner._

object Main extends App {
  if (args.isEmpty) {
    CLIConfig.parser.showUsage
  } else {
    CLIConfig.parser.parse(args, CLIConfig()) map {
      config =>
      tweakStaticJGitConfig(config.massiveNonFileObjects)

        if (config.gitdir.isEmpty) {
          CLIConfig.parser.showUsage
          Console.err.println("Main(): aborting : " + config.repoLocation + " is not a valid Git repository!")
        } else {
          implicit val repo = config.repo

          println("Using repo : " + repo.getDirectory.getAbsolutePath)

          // do this before implicitly initiating big-blob search

          if (hasBeenProcessedByGitcleanBefore(repo)) {
            println("This repo has been processed by Gitclean before! Will prune repo before proceeding to avoid unnecessary work on unused objects.")
            repo.git.gc.call()
            println("Completed prune of old objects.")
          }

          if (config.definesNoWork) {
            Console.err.println("Main(): please specify tasks for Gitclean:")
            CLIConfig.parser.showUsage
          } else {
            println("Found " + config.objectProtection.fixedObjectIds.size + " objects to protect")

            RepoRewriter.rewrite(repo, config.objectIdCleanerConfig)

            repo.close()
          }
        }
    }
  }
}
