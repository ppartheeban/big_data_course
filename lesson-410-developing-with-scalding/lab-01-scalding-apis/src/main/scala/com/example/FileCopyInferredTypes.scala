package com.example

import com.twitter.scalding._

class FileCopyInferredTypes(args: Args) extends Job(args) {

  val input = TypedPipe.from(TextLine("data/hello-adele.txt"))
  val output = TypedTsv[String]("data/hello-adele-copy.txt")

  input.write(output)
}
