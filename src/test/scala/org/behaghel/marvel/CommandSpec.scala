/*
 * Copyright (c) 2016 Hubert Behaghel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.behaghel.marvel

import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest._
import org.scalacheck._
import MarvelAPIClient._

class CommandSpec
    extends AsyncFlatSpec
    with Matchers
    with BeforeAndAfterEach
    with prop.PropertyChecks {

  override def afterEach(): Unit = {
    DummyPrinter.clear()
  }

  val listAll = new ListAllCommand(DummyPrinter)
  val top10   = new Top10Command(DummyPrinter)

  "ListAllCommand.execute()" should "list all Marvel characters in alphabetical order" in {
    listAll.execute() map { _ =>
      val output = DummyPrinter.toList
      output should contain inOrder ("Agent Zero", "Zzzax")
      // lowercase or "Giant-dok" was not less than or equal to
      // "Giant-Man (Ultimate)"
      output.map(_.toLowerCase) shouldBe sorted
    }
  }

  val superHeroGen = for {
    name <- Gen.oneOf("Thor",
                      "Iron Man",
                      "Captain America",
                      "Wolverine",
                      "Hulk",
                      "Spider-Man",
                      "Daredevil",
                      "Batman",
                      "Hubert")
    issues <- Gen.choose(0, 2000)
  } yield MarvelCharacter(name = name, issues = issues)

  val listSuperHeroesGen = Gen.containerOf[List, MarvelCharacter](superHeroGen)

  "Top10Command.mostPopular(n)" should "retain the most popular characters" in {
    forAll(listSuperHeroesGen, Gen.choose(2, 20)) {
      (cs: List[MarvelCharacter], n: Int) =>
        whenever(n <= cs.size) {
          val tops   = top10.mostPopular(n)(cs)
          val losers = tops.toSet diff cs.toSet
          assert(losers forall { loser =>
            tops.forall(_.issues >= loser.issues)
          })
        }
    }
  }

  it should "sort character by decreasing order of popularity" in {
    forAll(listSuperHeroesGen, Gen.choose(2, 20)) {
      (cs: List[MarvelCharacter], n: Int) =>
        whenever(n <= cs.size) {
          val tops = top10.mostPopular(n)(cs)
          tops.map(_.issues).reverse shouldBe sorted
        }
    }
  }

  it should "only retain the specified number of superheroes" in {
    forAll(listSuperHeroesGen, Gen.choose(2, 20)) {
      (cs: List[MarvelCharacter], n: Int) =>
        whenever(n <= cs.size) {
          val tops = top10.mostPopular(n)(cs)
          tops.size should be(n)
        }
    }
  }

  "Top10Command.execute()" should "list 10 names" in {
    top10.execute() map { _ =>
      val output = DummyPrinter.toList
      assert(output.size === 10)
    }
  }
}
