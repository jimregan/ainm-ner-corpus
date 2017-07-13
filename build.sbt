name := "AinmNerCorpus"

organization := "ie.tcd.slscs.itut"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.2"

libraryDependencies +=  "org.apache.opennlp" % "opennlp-tools" % "1.8.2-SNAPSHOT"

testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v")

libraryDependencies ++= {
        Seq(
            "org.scalatest" % "scalatest_2.10" % "2.0" % Test,
            "com.novocode" % "junit-interface" % "0.11" % Test,
            "junit" % "junit" % "4.12" % Test
        )
}

