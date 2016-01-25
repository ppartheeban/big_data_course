package com.example

import com.twitter.scalding._

/**
 * See the WordCountExplicitTypes for a version with explicit types
 *
 * Run in sbt as:
 * run com.example.WordCountShort --local --input data/hello-adele.txt --output data/hello-adele-word-count.tsv
 *
 */
class WordCountShort(args: Args) extends Job(args) {

  def tokenize(text: String): Array[String] = {
    text.toLowerCase.replaceAll("[^a-zA-Z0-9\\s]", "").split("\\s+")
  }

  TypedPipe
    .from(TextLine(args("input")))
    .flatMap(tokenize(_))
    .groupBy(word => word)
    .size
    .write(TypedTsv(args("output")))

}
