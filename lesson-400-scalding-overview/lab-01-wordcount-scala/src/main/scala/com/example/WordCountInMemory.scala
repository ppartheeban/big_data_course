package com.example

object WordCountInMemory extends App {
  val text =
    """Hello, it's me, I was wondering
      |If after all these years you'd like to meet to go over everything
      |They say that time's supposed to heal, yeah
      |But I ain't done much healing
      |"""".stripMargin

  val wordCount = text
    .split(" ")                           // Array[String]
    .map(word => (word, 1))               // Array[(String, Int)]
    .groupBy(_._1)                        // Map[String,Array[(String, Int)]]
    .map(a => (a._1, a._2.map(_._2).sum)) // Map[String,Int]

  println(wordCount)
}
