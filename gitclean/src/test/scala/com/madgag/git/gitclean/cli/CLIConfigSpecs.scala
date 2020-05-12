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

package com.madgag.git.gitclean.cli

import com.madgag.git.gitclean.model.FileName

import org.scalatest.{FlatSpec, Matchers}

class CLIConfigSpecs extends FlatSpec with Matchers {
  def parse(args: String) = CLIConfig.parser.parse(args.split(' ') :+ "my-repo.git", CLIConfig()).get.filterContentPredicate

  "CLI config" should "understand lone include" in {
    val predicate = parse("-fi *.txt")

    predicate(FileName("panda")) shouldBe false
    predicate(FileName("foo.txt")) shouldBe true
    predicate(FileName("foo.java")) shouldBe false
  }

  it should "understand lone exclude" in {
    val predicate = parse("-fe *.txt")

    predicate(FileName("panda")) shouldBe true
    predicate(FileName("foo.txt")) shouldBe false
    predicate(FileName("foo.java")) shouldBe true
  }

  it should "understand include followed by exclude" in {
    val predicate = parse("-fi *.txt -fe Poison.*")

    predicate(FileName("panda")) shouldBe false
    predicate(FileName("foo.txt")) shouldBe true
    predicate(FileName("foo.java")) shouldBe false
    predicate(FileName("Poison.txt")) shouldBe false
  }

  it should "understand exclude followed by include" in {
    val predicate = parse("-fe *.xml -fi hbm.xml")

    predicate(FileName("panda")) shouldBe true
    predicate(FileName("foo.xml")) shouldBe false
    predicate(FileName("hbm.xml")) shouldBe true
  }
}
