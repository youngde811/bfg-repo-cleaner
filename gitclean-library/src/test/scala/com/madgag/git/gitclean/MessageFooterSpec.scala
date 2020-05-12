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

package com.madgag.git.gitclean

import com.madgag.git.gitclean.model.{CommitNode, Footer}
import org.eclipse.jgit.lib.PersonIdent
import org.scalatest.{FlatSpec, Matchers}

class MessageFooterSpec extends FlatSpec with Matchers {
  val person = new PersonIdent("Frodo Baggins", "frodo@hobbits.com")

  def commit(m: String) = CommitNode(person, person, m)

  "Message footers" should "append footer without new paragraph if footers already present" in {

    val updatedCommit = commit("Sub\n\nmessage\n\nSigned-off-by: Bilbo Baggins <bilgo@hobbits.com>") add Footer("Foo", "Bar")

    updatedCommit.message shouldBe "Sub\n\nmessage\n\nSigned-off-by: Bilbo Baggins <bilbo@hobbits.com>\nFoo: Bar"
  }

  it should "create paragraph break if no footers already present" in {

    val updatedCommit = commit("Sub\n\nmessage") add Footer("Foo", "Bar")

    updatedCommit.message shouldBe "Sub\n\nmessage\n\nFoo: Bar"
  }
}
