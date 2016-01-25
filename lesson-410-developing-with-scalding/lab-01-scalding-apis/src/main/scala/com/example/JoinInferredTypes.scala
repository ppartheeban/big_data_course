package com.example

import com.twitter.scalding._
import com.twitter.scalding.typed.{CoGrouped, UnsortedGrouped}

class JoinInferredTypes(args: Args) extends Job(args) {

  def tokenize(text: String): Array[String] = {
    text.toLowerCase.replaceAll("[^a-zA-Z0-9\\s]", "").split("\\s+")
  }

  val input = TypedPipe.from(TextLine("data/hello-adele.txt"))
  val selectedWordsInput = TypedPipe.from(TextLine("data/hello-adele-selected-words.txt"))

  val inputWordsPairs = input.flatMap(tokenize(_)).groupBy(_.toLowerCase).size

  val selectedWordsGroup = selectedWordsInput.groupBy(_.toLowerCase)

  val joined = inputWordsPairs.join(selectedWordsGroup)

  val freqOfSelectedWords = joined.toTypedPipe.map { case (key, (freq, str)) => (key, freq) }

  freqOfSelectedWords.write(TypedTsv("data/hello-adele-word-selected-count.tsv"))

}
