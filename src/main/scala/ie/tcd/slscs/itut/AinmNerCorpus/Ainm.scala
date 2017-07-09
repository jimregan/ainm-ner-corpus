/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Trinity College, Dublin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ie.tcd.slscs.itut.AinmNerCorpus

import scala.xml._
import java.io.FileInputStream; 
import java.io.InputStream; 
import opennlp.tools.tokenize.TokenizerME; 
import opennlp.tools.tokenize.TokenizerModel; 
import opennlp.tools.util.Span; 

case class Paragraph(children: List[TextPiece])
trait TextPiece
case class RawText(t: String) extends TextPiece
abstract class Mention(text: String) extends TextPiece
trait PlaceRec
case class PersonMention(id: String, text: String) extends Mention(text)
case class Opus(kind: String, BaseForm: String, text: String) extends Mention(text)
case class PlaceName(id: String, BaseForm: String, text: String, ainm: Boolean = true, geonames: Boolean = false) extends Mention(text) with PlaceRec
case class Place(id: String, text: String) extends PlaceRec
case class Date(date: String, circa: Boolean = false)


case class TEIHeader(id: String, title: String, titlenote: String,
                     forename: String, surname: String, birth: Date, death: Date,
                     sex: String, floruit: String, birthplace: PlaceName,
                     faith: String, schools: List[String], 
                     universities: List[String], occupations: List[String],
                     authors: List[String])
object TEIHeader {
  def readHeader(bio: Node): TEIHeader = {
    def fixupdate(txt: String, att: String, ctxt: String): String = {
      if (att == "" || att == "yyyy-mm-dd") {
        if(ctxt.trim != "" && txt.trim.startsWith(ctxt.trim)) {
          txt.trim.substring(ctxt.trim.length)
        } else {
          txt
        }
      } else {
        att
      }
    }
    val title = (bio \\ "biography" \ "header" \ "fileDesc" \ "titleStmt" \ "title").text
    val id = (bio \\ "biography" \ "@id").text
    val titlenote = (bio \\ "biography" \ "header" \ "fileDesc" \ "titleStmt" \ "titleNote").text
    val forename = (bio \\ "biography" \ "header" \ "fileDesc" \ "titleStmt" \ "persName" \ "forename").text
    val surname = (bio \\ "biography" \ "header" \ "fileDesc" \ "titleStmt" \ "persName" \ "surname").text
    val bcirca = (bio \\ "biography" \ "header" \ "fileDesc" \ "titleStmt" \ "birth" \ "circa").text
    val dcirca = (bio \\ "biography" \ "header" \ "fileDesc" \ "titleStmt" \ "death" \ "circa").text
    val braw = (bio \\ "biography" \ "header" \ "fileDesc" \ "titleStmt" \ "birth").text
    val draw = (bio \\ "biography" \ "header" \ "fileDesc" \ "titleStmt" \ "death").text
    val batt = (bio \\ "biography" \ "header" \ "fileDesc" \ "titleStmt" \ "birth" \ "@date").text
    val datt = (bio \\ "biography" \ "header" \ "fileDesc" \ "titleStmt" \ "death" \ "@date").text
    val birth = Date(fixupdate(braw, batt, bcirca), bcirca.trim == "c.")
    val death = Date(fixupdate(draw, datt, dcirca), dcirca.trim == "c.")
    val sex = (bio \\ "biography" \ "header" \ "fileDesc" \ "titleStmt" \ "sex").text
    val floruit = (bio \\ "biography" \ "header" \ "fileDesc" \ "titleStmt" \ "floruit").text
    val schools = (bio \\ "biography" \ "header" \ "fileDesc" \ "titleStmt" \ "school").map{ c => c.text}.toList
    val universities = (bio \\ "biography" \ "header" \ "fileDesc" \ "titleStmt" \ "university").map{ c => c.text}.toList
    val occupations = (bio \\ "biography" \ "header" \ "fileDesc" \ "titleStmt" \ "occupation").map{ c => c.text}.toList
    val faith = (bio \\ "biography" \ "header" \ "fileDesc" \ "titleStmt" \ "faith").text
    val bplaceid = (bio \\ "biography" \ "header" \ "fileDesc" \ "titleStmt" \ "birthPlace" \ "@id").text
    val bplacegeonames = (bio \\ "biography" \ "header" \ "fileDesc" \ "titleStmt" \ "birthPlace" \ "@geonames").text
    val bplacetxt = (bio \\ "biography" \ "header" \ "fileDesc" \ "titleStmt" \ "birthPlace").text
    val birthplace = PlaceName(bplaceid, "", bplacetxt, bplaceid != "", bplacegeonames != "")
    val authors = (bio \\ "biography" \ "header" \ "fileDesc" \ "titleStmt" \ "author").map{ c => c.text}.toList
    TEIHeader(id, title, titlenote, forename, surname, birth, death, sex, floruit,
              birthplace, faith, schools, universities, occupations, authors)
  }
  /*
  def readParagraphs(n: Node): List[Paragraph] = {
    val paragraphs = (n \\ "p")
  }
  */
}
// set tabstop=2
