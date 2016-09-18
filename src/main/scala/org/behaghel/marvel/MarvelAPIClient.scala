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

  def listCharacterNames(printer: Printer): Future[Unit] = {
    val futureFirstPage = requestCharacterPage(0, pageSize)
    futureFirstPage flatMap { firstPage =>
      val total   = extractTotal(firstPage)
      val count   = extractCount(firstPage)
      val offsets = (count + 1) to total by pageSize // start from count
      val requests = offsets map { offset =>
        requestCharacterPage(offset, pageSize).map(
          extractNames andThen (_.mkString("\n"))
        )
      }
      val firstPageNames = extractNames(firstPage).mkString("\n")
      val init           = Future(printer.print(firstPageNames))
      requests.foldLeft(init)(
        (futurePrinted, futureNames) =>
          futureNames.flatMap { names =>
            futurePrinted.map(_ => printer.print(names))
        }
      )
    }
  }

  private def requestCharacterPage(offset: Int, limit: Int): Future[Json] =
    Future {
      val url        = buildFullUrl("characters?orderBy=name", offset, limit)
      val jsonString = scala.io.Source.fromURL(url).mkString
      parse(jsonString).getOrElse(Json.Null) // TODO: handle errors
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

  val extractCount: Json => Int =
    _.hcursor.downField("data").downField("count").as[Int].getOrElse(0)

  val extractTotal: Json => Int =
    _.hcursor.downField("data").downField("total").as[Int].getOrElse(0)

  val extractNames: Json => Seq[String] = json => {
    val characters = json.hcursor
      .downField("data")
      .downField("results")
      .focus
      .flatMap(_.asArray)
      .getOrElse(Nil)
    characters.flatMap(_.cursor.get[String]("name").toOption)
  }
}
