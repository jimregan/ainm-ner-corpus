name := "AinmNerCorpus"

organization := "ie.tcd.slscs.itut"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.2"

libraryDependencies +=  "org.apache.opennlp" % "opennlp-tools" % "1.8.1"

libraryDependencies ++= {
        Seq(
            "org.scalatest" % "scalatest_2.10" % "2.0" % "test",
            "junit" % "junit" % "4.12" % "test"
        )
}

