/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Jim O'Regan
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

case class Paragraph(children: List[TextPiece]) {
  def getText: String = children.map{e => e.getText}.mkString("")
}
trait TextPiece {
  def getText: String
}
case class RawText(t: String) extends TextPiece {
  def getText: String = t
}
abstract class Mention(text: String) extends TextPiece {
  def getText: String = text
}
trait PlaceRec
case class PersonMention(id: String, baseform: String, text: String) extends Mention(text)
case class Opus(kind: String, baseform: String, text: String) extends Mention(text)
case class Conradh(kind: String, baseform: String, text: String) extends Mention(text)
case class Party(baseform: String, text: String) extends Mention(text)
case class PlaceName(id: String, baseform: String, text: String, geonames: String, foreign: Boolean = false) extends Mention(text) with PlaceRec
case class EduInst(baseform: String, text: String, geonames: String) extends Mention(text)
case class Place(id: String, text: String) extends PlaceRec
case class Date(date: String, circa: Boolean = false)


case class TEIHeader(id: String, title: String, titlenote: String,
                     forename: String, surname: String, birth: Date, death: Date,
                     sex: String, floruit: String, birthplace: PlaceName,
                     faith: String, schools: List[String], 
                     universities: List[String], occupations: List[String],
                     authors: List[String])
object TEIReader {
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
    val bplaceforeign = if ((bio \\ "biography" \ "header" \ "fileDesc" \ "titleStmt" \ "birthPlace" \ "@type").text == "foreign") true else false
    val bplacetxt = (bio \\ "biography" \ "header" \ "fileDesc" \ "titleStmt" \ "birthPlace").text
    val birthplace = PlaceName(bplaceid, "", bplacetxt, bplacegeonames, bplaceforeign)
    val authors = (bio \\ "biography" \ "header" \ "fileDesc" \ "titleStmt" \ "author").map{ c => c.text}.toList
    TEIHeader(id, title, titlenote, forename, surname, birth, death, sex, floruit,
              birthplace, faith, schools, universities, occupations, authors)
  }
  def readParagraphs(n: Node): List[Paragraph] = (n \\ "p").toList.map{readParagraph}.toList
  def readParagraph(n: Node): Paragraph = n match {
    case <p>{children @ _* }</p> => Paragraph(children.map{readParagraphPiece}.toList)
    case _ => throw new Exception("Unexpected element" + n.toString)
  }
  def readParagraphPiece(n: Node): TextPiece = n match {
    case scala.xml.Text(t) => RawText(t)
    case <em>{em}</em> => RawText(em.text)
    case <blockquote>{bq}</blockquote> => RawText(bq.text)
    case <hide>{h}</hide> => RawText("")
    case e @ Elem(_, "persName", attribs, _, _) => PersonMention(attribs.get("id").toString, attribs.get("baseform").toString, e.text)
    case e @ Elem(_, "placeName", attribs, _, _) => PlaceName(attribs.get("id").toString, attribs.get("baseform").toString, e.text, attribs.get("geonames").toString, attribs.get("type").toString == "foreign")
    case e @ Elem(_, "party", attribs, _, _) => Party(attribs.get("baseform").toString, e.text)
    case e @ Elem(_, "opus", attribs, _, _) => Opus(attribs.get("type").toString, attribs.get("baseform").toString, e.text)
    case e @ Elem(_, "conradh", attribs, _, _) => Opus(attribs.get("type").toString, attribs.get("baseform").toString, e.text)
    case e @ Elem(_, "eduInst", attribs, _, _) => EduInst(attribs.get("baseform").toString, e.text, attribs.get("geonames").toString)
    case _ => throw new Exception("Unexpected element" + n.toString)
  }
}
// set tabstop=2
