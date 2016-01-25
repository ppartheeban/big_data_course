package com.example

import com.twitter.scalding._
import org.scalatest.{Matchers, WordSpec}

class WordCountTest extends WordSpec with Matchers {
  "A WordCount job" should {
    JobTest(new WordCount(_))
      .arg("input", "inputFile")
      .arg("output", "outputFile")
      .source(TextLine("inputFile"), List((0, "hack hack hack and hack")))
      .sink[(String, Int)](TypedTsv[(String, Long)]("outputFile")) {
        outputBuffer =>
          val outMap = outputBuffer.toMap
          "count words correctly" in {
            outMap("hack") shouldBe 4
            outMap("and") shouldBe 1
          }
      }
      .run
      .finish
  }
}