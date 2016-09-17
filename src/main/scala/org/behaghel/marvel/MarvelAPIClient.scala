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

class MarvelAPIClient {
  val baseUrl    = MarvelConfig.baseUrl
  val privateKey = MarvelConfig.privateKey
  val publicKey  = MarvelConfig.publicKey

  def signatureParam() = {
    val timestamp: Long = System.currentTimeMillis / 1000
    val toBeHashed      = s"$timestamp$privateKey$publicKey"
    val hash            = Utils.md5Digest(toBeHashed)
    s"apikey=$publicKey&ts=$timestamp&hash=$hash"
  }

  def buildFullUrl(apiSpecifics: String) =
    s"$baseUrl/$apiSpecifics&${ signatureParam() }"

  def listCharacterNames(): String =
    io.Source.fromURL(buildFullUrl("characters?orderBy=name")).mkString
}