package com.example

import com.twitter.scalding._
import com.twitter.scalding.typed.{UnsortedGrouped, CoGrouped}

class JoinExplicitTypes(args: Args) extends Job(args) {

  def tokenize(text: String): Array[String] = {
    text.toLowerCase.replaceAll("[^a-zA-Z0-9\\s]", "").split("\\s+")
  }

  val input: TypedPipe[(String)] = TypedPipe.from(TextLine("data/hello-adele.txt"))
  val selectedWordsInput: TypedPipe[(String)] = TypedPipe.from(TextLine("data/hello-adele-selected-words.txt"))

  val inputWordsPairs: UnsortedGrouped[String, Long] = input.flatMap(tokenize(_)).groupBy(_.toLowerCase).size

  val selectedWordsGroup: Grouped[String, String] = selectedWordsInput.groupBy(_.toLowerCase)

  val joined: CoGrouped[String, (Long, String)] = inputWordsPairs.join(selectedWordsGroup)

  val freqOfSelectedWords: TypedPipe[(String, Long)] = joined.toTypedPipe.map { case (key, (freq, str)) => (key, freq) }

  freqOfSelectedWords.write(TypedTsv[(String, Long)]("data/hello-adele-word-selected-count.tsv"))

}
