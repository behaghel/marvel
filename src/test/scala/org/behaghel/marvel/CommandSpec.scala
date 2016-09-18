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

class CommandSpec extends AsyncFlatSpec with Matchers with BeforeAndAfterEach {

  override def afterEach(): Unit = {
    DummyPrinter.clear()
  }

  val cmd = new ListAllCommand(DummyPrinter)

  "Command" should "list Marvel characters in alphabetical order" in {
    cmd.execute() map { _ =>
      val output = DummyPrinter.toList
      output should contain inOrder ("Agent Zero", "Zzzax")
      // lowercase or "Giant-dok" was not less than or equal to
      // "Giant-Man (Ultimate)"
      output.map(_.toLowerCase) shouldBe sorted
    }
  }

}
