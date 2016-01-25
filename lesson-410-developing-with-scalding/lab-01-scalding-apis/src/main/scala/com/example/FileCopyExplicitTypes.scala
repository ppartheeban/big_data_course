package com.example

import com.twitter.scalding._

class FileCopyExplicitTypes(args: Args) extends Job(args) {

  val input: TypedPipe[String] = TypedPipe.from(TextLine("data/hello-adele.txt"))
  val output: FixedPathTypedDelimited[String] = TypedTsv[String]("data/hello-adele-copy.txt")

  input.write(output)
}
