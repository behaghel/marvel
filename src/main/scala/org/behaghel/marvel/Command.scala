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

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import MarvelAPIClient._

object Command {
  def from(arg: String, printer: Printer) = arg match {
    case "top10" => new Top10Command(printer)
    case _       => new ListAllCommand(printer)
  }

}

trait Command {
  def execute: Future[Unit]
}
class ListAllCommand(printer: Printer) extends Command {
  lazy val client = new MarvelAPIClient
  override def execute(): Future[Unit] =
    client foreachCharacterInAlphaOrder { character =>
      printer.print(character.name)
    }
}
class Top10Command(printer: Printer) extends Command {
  lazy val client = new MarvelAPIClient
  override def execute(): Future[Unit] = {
    val futureCharacters = client.withCharactersStreamAlpha(identity)
    futureCharacters.map(
      mostPopular(10) _ andThen (_.map(_.name)) andThen (printer.printlnAll _)
    )
  }

  def mostPopular(
      n: Int
  )(characters: Seq[MarvelCharacter]): Seq[MarvelCharacter] =
    characters.sortBy(_.issues).reverse.take(n)
}
