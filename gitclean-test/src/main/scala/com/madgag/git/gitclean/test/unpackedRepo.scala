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

package com.madgag.git.gitclean.test

import com.madgag.git._
import com.madgag.git.test._
import org.eclipse.jgit.internal.storage.file.{GC, ObjectDirectory}
import org.eclipse.jgit.lib.Constants.OBJ_BLOB
import org.eclipse.jgit.lib.{ObjectId, ObjectReader, Repository}
import org.eclipse.jgit.revwalk.{RevCommit, RevTree}
import org.eclipse.jgit.treewalk.TreeWalk
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.{FlatSpec, Inspectors, Matchers}

import scala.collection.convert.ImplicitConversionsToScala._

class unpackedRepo(filePath: String) extends FlatSpec with Matchers {
  implicit val repo = unpackRepo(filePath)
  implicit val objectDirectory = repo.getObjectDatabase.asInstanceOf[ObjectDirectory]
  implicit lazy val (revWalk, reader) = repo.singleThreadedReaderTuple

  def blobOfSize(sizeInBytes: Int): Matcher[ObjectId] = Matcher { (objectId: ObjectId) =>
    val objectLoader = objectId.open
    val hasThatSize = objectLoader.getType == OBJ_BLOB && objectLoader.getSize == sizeInBytes

    def thing(boo: String) = s"${objectId.shortName} $boo size of $sizeInBytes"

    MatchResult(hasThatSize, thing("did not have"), thing("had"))
  }

  def packedBlobsOfSize(sizeInBytes: Long): Set[ObjectId] = {
    implicit val reader = repo.newObjectReader()

    repo.getObjectDatabase.asInstanceOf[ObjectDirectory].packedObjects.filter { objectId =>
      val objectLoader = objectId.open
      objectLoader.getType == OBJ_BLOB && objectLoader.getSize == sizeInBytes
    }.toSet
  }

  def haveFile(name: String): Matcher[ObjectId] = haveTreeEntry(name, !_.isSubtree)

  def haveFolder(name: String): Matcher[ObjectId] = haveTreeEntry(name, _.isSubtree)

  def haveTreeEntry(name: String, p: TreeWalk => Boolean)= new Matcher[ObjectId] {
    def apply(treeish: ObjectId) = {
      treeOrBlobPointedToBy(treeish.asRevObject) match {
        case Right(tree) =>
          def thing(boo: String) = s"tree ${treeish.shortName} $boo a '$name' entry"

          MatchResult(
            treeEntryNames(tree, p).contains(name),
            thing("did not contain"),
            thing("contained")
          )
        case Left(blob) =>
          MatchResult(
            false,
            s"blob ${treeish.shortName} was not a tree containing '$name'",
            s"""When does this happen??!""""
          )
      }
    }
  }

  def treeEntryNames(t: RevTree, p: TreeWalk => Boolean): Seq[String] =
    t.walk(postOrderTraversal = true).withFilter(p).map(_.getNameString).toList

  def commitHist(specificRefs: String*)(implicit repo: Repository): Seq[RevCommit] = {
    val logCommand = repo.git.log

    if (specificRefs.isEmpty) logCommand.all else specificRefs.foldLeft(logCommand)((lc, ref) => lc.add(repo.resolve(ref)))
  }.call.toSeq.reverse

  def haveCommitWhereObjectIds(boom: Matcher[Traversable[ObjectId]])(implicit reader: ObjectReader): Matcher[RevCommit] = boom compose {
    (c: RevCommit) => c.getTree.walk().map(_.getObjectId(0)).toSeq
  }

  def haveRef(refName: String, objectIdMatcher: Matcher[ObjectId]): Matcher[Repository] = objectIdMatcher compose {
    (r: Repository) => r resolve refName // aka s"Ref [$refName]"
  }

  def commitHistory(histMatcher: Matcher[Seq[RevCommit]]) = histMatcher compose {
    r: Repository => commitHist()(r)
  }

  def commitHistoryFor(refs: String*)(histMatcher: Matcher[Seq[RevCommit]]) = histMatcher compose {
    r: Repository => commitHist(refs:_*)(r)
  }

  def ensureRemovalOfBadEggs[S,T](expr : => Traversable[S], exprResultMatcher: Matcher[Traversable[S]])(block: => T) = {
    gc()
    expr should exprResultMatcher

    block

    gc()

    expr shouldBe empty
  }

  def gc() = {
    val gc = new GC(repo)
    gc.setPackExpireAgeMillis(0)
    gc.gc()
  }

  class CheckRemovalFromCommits(commits: => Seq[RevCommit]) extends Inspectors {
    def ofCommitsThat[T](commitM: Matcher[RevCommit])(block: => T): Unit = {
      forAtLeast(1, commits) { commit =>
        commit should commitM
      }

      block

      forAll(commits) { commit =>
        commit shouldNot commitM
      }
    }
  }

  def ensureRemovalFrom(commits: => Seq[RevCommit]): CheckRemovalFromCommits = new CheckRemovalFromCommits(commits)

  def ensureInvariantValue[T, S](f: => S)(block: => T) = {
    val originalValue = f
    block
    f should equal(originalValue)
  }

  def ensureInvariantCondition[T, S](cond: Matcher[Repository])(block: => T) = {
    repo should cond
    block
    repo should cond
  }
}
