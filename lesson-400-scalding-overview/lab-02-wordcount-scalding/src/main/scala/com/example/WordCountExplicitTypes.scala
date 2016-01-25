package com.example

import com.twitter.scalding._
import com.twitter.scalding.typed.UnsortedGrouped

/**
 * Step by step example with specified types
 * See the WordCountShort for a typical code that uses type inference:
  * Type declarations are inferred and can be skipped in many places.
 *
 * Run in sbt as:
 * run com.example.WordCountExplicitTypes --local --input data/hello-adele.txt --output data/hello-adele-word-count.tsv
 *
 */
class WordCountExplicitTypes(args: Args) extends Job(args) {

  def tokenize(text: String): Array[String] = {
    text.toLowerCase.replaceAll("[^a-zA-Z0-9\\s]", "").split("\\s+")
  }

  val words: TypedPipe[String] = TypedPipe
    .from(TextLine(args("input")))
    .flatMap(tokenize(_))

  val groups: Grouped[String, String] = words.groupBy(_.toLowerCase)

  val counts: UnsortedGrouped[String, Long] = groups.size

  counts.write(TypedTsv[(String, Long)](args("output")))

}
