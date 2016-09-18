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
import io.circe._
import io.circe.parser._
import cats.data.Xor

class MarvelAPIClient {
  val baseUrl    = MarvelConfig.baseUrl
  val privateKey = MarvelConfig.privateKey
  val publicKey  = MarvelConfig.publicKey
  val pageSize   = MarvelConfig.pageSize

  object CharacterResponse {
    def buildFrom(json: Json) = CharacterResponse(
      Character.buildSeqFrom(extractCharacters(json)),
      extractIntData("offset")(json),
      extractIntData("count")(json),
      extractIntData("total")(json)
    )
  }
  case class CharacterResponse(characters: Seq[Character],
                               offset: Int,
                               count: Int,
                               total: Int) {
    def characterNames = characters.map(_.name)
  }
  object Character {
    def buildSeqFrom(jsonArray: Seq[Json]) = jsonArray map { characterJson =>
      Character(extractName(characterJson), extractIssues(characterJson))
    }
  }
  case class Character(name: String, issues: Int)

  def listTop10(printer: Printer): Future[Unit] = {
    Future(())
  }

  def foreachCharacterInAlphaOrder(f: Character => Unit): Future[Unit] =
    withCharactersStreamAlpha(f).map(_.head)

  def withCharactersStreamAlpha[T](f: Character => T): Future[Seq[T]] = {
    val futureFirstPage = requestCharacterPage(0, pageSize)
    futureFirstPage flatMap { firstPage =>
      val offsets = (firstPage.count + 1) to firstPage.total by pageSize // start from count
      val batchesOfCharacters = offsets map { offset =>
        requestCharacterPage(offset, pageSize).map(_.characters)
      }
      batchesOfCharacters.foldLeft(futureFirstPage.map(_.characters.map(f)))(
        (processed, charactersBatch) =>
          charactersBatch.flatMap { characters =>
            processed.map(_ ++ characters.map(f))
        }
      )
    }
  }

  private def requestCharacterPage(offset: Int,
                                   limit: Int): Future[CharacterResponse] =
    Future {
      val url        = buildFullUrl("characters?orderBy=name", offset, limit)
      val jsonString = scala.io.Source.fromURL(url).mkString
      CharacterResponse.buildFrom(parse(jsonString).getOrElse(Json.Null)) // TODO: handle errors
    }

  private def signatureParam() = {
    val timestamp: Long = System.currentTimeMillis / 1000
    val toBeHashed      = s"$timestamp$privateKey$publicKey"
    val hash            = Utils.md5Digest(toBeHashed)
    s"apikey=$publicKey&ts=$timestamp&hash=$hash"
  }

  private def paginationParam(offset: Int, limit: Int) =
    s"limit=$limit&offset=$offset"

  private def buildFullUrl(apiSpecifics: String, offset: Int, limit: Int) =
    s"$baseUrl/$apiSpecifics&${ paginationParam(offset, limit) }&${ signatureParam() }"

  def extractIntData(field: String): Json => Int =
    _.hcursor.downField("data").downField(field).as[Int].getOrElse(0)

  def extractCharacters(characterResponse: Json): Seq[Json] =
    characterResponse.hcursor
      .downField("data")
      .downField("results")
      .focus
      .flatMap(_.asArray)
      .getOrElse(Nil)

  def extractName(character: Json): String =
    character.cursor.get[String]("name").getOrElse("no name")

  def extractIssues(character: Json): Int =
    character.hcursor.downField("comics").get[Int]("available").getOrElse(-1)
}
