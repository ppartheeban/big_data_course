package com.example

import com.twitter.scalding._

/**
 * Step by step example with specified types
 * See the WordCountShort for a typical code that uses type inference
 *
 * Run in sbt as:
 * run com.example.WordCount --local --input data/hello-adele.txt --output data/hello-adele-word-count.tsv
 *
 */
class WordCount(args: Args) extends Job(args) {

  def tokenize(text: String): Array[String] = {
    text.toLowerCase.replaceAll("[^a-zA-Z0-9\\s]", "").split("\\s+")
  }

  val words: TypedPipe[String] = TypedPipe
    .from(TextLine(args("input")))
    .flatMap(tokenize(_))

  val groups: Grouped[String, String] = words.groupBy(_.toLowerCase)

  val counts = groups.size

  counts.write(TypedTsv[(String, Long)](args("output")))

}
