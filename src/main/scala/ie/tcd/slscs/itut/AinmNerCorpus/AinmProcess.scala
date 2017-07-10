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
import scala.io.Source
import java.io.FileInputStream; 
import java.io.InputStream; 
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME; 
import opennlp.tools.tokenize.TokenizerModel; 
import opennlp.tools.util.Span; 

object AinmProcess {
  val gasent: InputStream = getClass.getResourceAsStream("/ie/tcd/slscs/itut/AinmNerCorpus/ga-sent.bin")
  val gatok: InputStream = getClass.getResourceAsStream("/ie/tcd/slscs/itut/AinmNerCorpus/ga-token.bin")
  val sentmodel = new SentenceModel(gasent)
  val sentdetect = new SentenceDetectorME(sentmodel)
  val tokmodel = new TokenizerModel(gatok)
  val tokdetect = new TokenizerME(tokmodel)

  implicit def spanToTuple(s: Span):(Int, Int) = (s.s, s.e)
  implicit def tupleToSpan(t: (Int, Int)): Span = new Span(t._1, t._2)

  def spanner(s: List[String]):List[(Int, Int)] = {
    def spaninner(start: Int, l: List[String], acc: List[(Int, Int)]):List[(Int, Int)] = l match {
      case x :: xs => spaninner(start + x.length + 1, xs, acc :+ (start, start + x.length))
      case nil => acc
    }
    spaninner(0, s, List[(Int, Int)]())
  }
}

// set tabstop=2
