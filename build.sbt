name := "AinmNerCorpus"

organization := "ie.tcd.slscs.itut"

version := "0.3-SNAPSHOT"

scalaVersion := "2.10.2"

libraryDependencies +=  "org.apache.opennlp" % "opennlp-tools" % "1.8.1"

testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v")

mainClass in (Compile, run) := Some("ie.tcd.slscs.itut.AinmNerCorpus.OpenNLPConverter")

libraryDependencies ++= {
        Seq(
            "org.scalatest" % "scalatest_2.10" % "2.0" % Test,
            "com.novocode" % "junit-interface" % "0.11" % Test,
            "junit" % "junit" % "4.12" % Test
        )
}

