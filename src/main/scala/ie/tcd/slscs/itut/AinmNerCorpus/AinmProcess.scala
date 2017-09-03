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

import java.io.{File, InputStream}

import ie.tcd.slscs.itut.ainmnercorpus.{EntityBase, ListPartition, SimpleEntity, TextEntity}
import opennlp.tools.sentdetect.SentenceDetectorME
import opennlp.tools.sentdetect.SentenceModel
import opennlp.tools.tokenize.TokenizerME
import opennlp.tools.tokenize.TokenizerModel
import opennlp.tools.util.Span

object AinmProcess {
  import scala.xml.XML
  import java.io.File

  val gasentbin: InputStream = getClass.getResourceAsStream("/ie/tcd/slscs/itut/AinmNerCorpus/ga-sent.bin")
  val gatokbin: InputStream = getClass.getResourceAsStream("/ie/tcd/slscs/itut/AinmNerCorpus/ga-token.bin")
  val sentmodel = new SentenceModel(gasentbin)
  val sentdetect = new SentenceDetectorME(sentmodel)
  val tokmodel = new TokenizerModel(gatokbin)
  val tokdetect = new TokenizerME(tokmodel)

  implicit def spanToTuple(s: Span):(Int, Int) = (s.getStart, s.getEnd)
  implicit def tupleToSpan(t: (Int, Int)): Span = new Span(t._1, t._2)

  /**
   * Get a list of tuples representing the start and ends of each string where
   * the list concatenated can be considered a single string.
   * As this is the representation used by OpenNLP's sentence splitter and
   * tokeniser, it's more convenient to represent entity/text boundaries this
   * way.
   */
  def getSpans(s: List[String]):List[(Int, Int)] = {
    def spaninner(start: Int, l: List[String], acc: List[(Int, Int)]):List[(Int, Int)] = l match {
      case x :: xs => spaninner(start + x.length + 1, xs, acc :+ (start, start + x.length))
      case nil => acc
    }
    spaninner(0, s, List[(Int, Int)]())
  }

  /**
   * Load xml files, taken from ainm.ie
   * These were grabbed with wget -x so the filenames represent the original
   * URIs: www.ainm.ie/Bio.aspx?ID={id}&xml=true
   */
  def getFileList(dir: String): List[File] = {
    import ie.tcd.slscs.itut.gramadanj.FileUtils
    val files = FileUtils.getFileListStartsAndEndsWith(dir, "Bio", "xml=true")
    files.toList
  }

  /**
   * Get the paragraphs from a single file
   */
  def readFile(f: File): List[Paragraph] = {
    import scala.xml.XML
    import scala.xml.Source
    val xml = XML.load(Source.fromFile(f))
    TEIReader.readParagraphs(xml)
  }

  abstract class NERText {
    def toText: String
  }
  case class TextPart(text: String) extends NERText {
    def toText = text
  }
  case class EntityReference(text: String, kind: String) extends NERText {
    def toText = " <START:" + kind + "> " + text + " <END> "
  }

  /**
   * Simplify the ainm.ie data.
   * For the most part, this consists of discarding all but the text, and in
   * normalising the types.
   * Educational institutions, which may be considered to be both places and
   * organisations, are treated as organisations. Similarly, newspapers and
   * periodicals are treated as organisations, rather than as an "opus",
   * while books, dramas, etc. are just treated as text.
   */
  def ainmTextPieceToNER(txt: TextPiece): NERText = txt match {
    case PersonMention(id, bf, t) => EntityReference(t, "person")
    case Conradh(kind, bf, t) => EntityReference(t, "organization")
    case Opus("newspaper", bf, t) => EntityReference(t, "organization")
    case Opus("periodical", bf, t) => EntityReference(t, "organization")
    case Opus("book", bf, t) => TextPart(t)
    case Opus(_, bf, t) => TextPart(t)
    case RawText(t) => TextPart(t)
    case Anchor(t, _) => TextPart(t)
    case Party(bf, t) => EntityReference(t, "organization")
    case PlaceName(id, bf, t, _, _) => EntityReference(t, "location")
    case EduInst(id, bf, t) => EntityReference(t, "location")
  }
  def simplifyTextPieces(pieces: List[NERText]): List[NERText] = {
    def simplifyInner(pieces: List[NERText], acc: List[NERText]): List[NERText] = pieces match {
      case EntityReference(a, b) :: xs => simplifyInner(xs, acc :+ EntityReference(a,b))
      case TextPart(t) :: xs => xs match {
        case Nil => acc :+ TextPart(t)
        case TextPart(tt) :: xx => simplifyInner(xx :+ TextPart(t + tt), acc)
        case EntityReference(a, b) :: xx => simplifyInner(xx, acc ++ List(TextPart(t), EntityReference(a, b)))
      }
      case Nil => acc
    }
    simplifyInner(pieces, List.empty[NERText])
  }
  def filterNERParagraph(p: Paragraph, filt: String): List[NERText] = {
    if(filt != null && filt != "") {
      filterNERType(filt, p.children.map{ainmTextPieceToNER})
    } else {
      p.children.map{ainmTextPieceToNER}
    }
  }
  def piecesFromFile(f: File, filter: String): List[List[NERText]] = {
    val xmltext = XML.loadFile(f)
    val rawparas = TEIReader.readParagraphs(xmltext)
    rawparas.map{e => simplifyTextPieces(filterNERParagraph(e, filter))}
  }
  def piecesFromFilePath(s: String, filter: String): List[List[NERText]] = {
    piecesFromFile(new File(s), filter)
  }
  def pieceToString(n: NERText): String = n match {
    case TextPart(t) => t
    case EntityReference(a, _) => a
    case _ => throw new Exception("Unknown object " + n.toString)
  }
  def piecesToString(l: List[NERText]): String = l.map{pieceToString}.mkString("")

  /**
   * Filters the NER pieces to only the desired type; OpenNLP (and most other
   * NER systems) generally uses separate models per type.
   */
  def filterNERType(kind: String, l: List[NERText]): List[NERText] = {
    def filterinner(n: NERText, kind: String): NERText = n match {
      case EntityReference(t, k) => {
        if(k == kind) {
          EntityReference(t, k)
        } else {
          TextPart(t)
        }
      }
      case TextPart(t) => TextPart(t)
    }
    l.map{e => filterinner(e, kind)}
  }
  implicit def convertNERTypeToJava(n: NERText): EntityBase = n match {
    case EntityReference(t, k) => new SimpleEntity(t, k)
    case TextPart(t) => new TextEntity(t)
    case _ => throw new Exception("Unknown object " + n.toString)
  }

  def splitParagraph(p: Paragraph): Array[Span] = sentdetect.sentPosDetect(p.getText)
  def splitParagraphs(l: List[Paragraph]): Array[Array[Span]] = l.map{splitParagraph}.toArray
  def tokeniseParagraph(p: Paragraph): Array[Span] = tokdetect.tokenizePos(p.getText)
  def tokeniseParagraphs(l: List[Paragraph]): Array[Array[Span]] = l.map{tokeniseParagraph}.toArray
  def processParagaph(p: Paragraph, filter: String): String = {
    val sentences = splitParagraph(p)
    val tokens = tokeniseParagraph(p)
    val ner = filterNERParagraph(p, filter).toArray.map{convertNERTypeToJava}
    ListPartition.makeText(ner, sentences, tokens)
  }
  def processParagraphs(l: List[Paragraph], filter: String): List[String] = l.map{e => processParagaph(e, filter)}
}

object OpenNLPConverter extends App {
  if(args.size != 1 || args.size != 2) {
    throw new Exception(s"""Usage: OpenNLPConverter directory [filter]
Where directory is a directory containing the downloaded XML
and filter is the NER type: person, organization, or location""")
  }
  val dir = args(0)
  val filter = if(args.size == 2) args(1) else ""
  filter match {
    case "person" | "organization" | "location" =>
    case "" =>
    case _ => throw new Exception("Filter can only be person, organization, or location")
  }
  val outputname = filter match {
    case "person" => "person-ner.txt"
    case "organization" => "org-ner.txt"
    case "location" => "loc-ner.txt"
    case "" => "all-ner.txt"
  }
  val directory = new File(dir)
  if(dir == null || dir == "" || !directory.exists || !directory.isDirectory) {
    throw new Exception("Specify the directory containing the ainm corpus")
  }
  val mydir = "/home/jim/www.ainm.ie/"
  val files = AinmProcess.getFileList(dir)
  val docs = files.map{AinmProcess.readFile}
}

// set tabstop=2
