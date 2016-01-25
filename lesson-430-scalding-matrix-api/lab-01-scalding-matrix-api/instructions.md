
# Testing Scalding

In this lab, we will explore testing of Scalding programs with JobTest.

## Objectives

1. Understand the organization of Scalding unit tests.
1. Apply `JobTest` with ScalaTest framework
1. Understand the dependencies in the `build.sbt` file.
2. Execute the tests.



## Prerequisites

SBT is required for this exercise. The student may use any text editor; an IDE (Scala IDE, JetBrains IntelliJ, etc) is recommended, but not required.


## Testing the Word Count Program

We have our `WordCount` Scalding program in the folder for the Scala source code: `src/main/scala`. We will test if it works as expected.

The tests should be placed in the folder `src/test/scala`.

Open the files and understand what they do.

#### WordCount.scala
``` scala
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
```



#### ScaldingTest.scala
``` scala
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
```

Notice the connection between the parameters `input` and `output` in the Scalding program and in the test.

### Run the Test
To run the test, first make sure that the `sbt` is running. The enter the command `test`.

```
> test
[info] Compiling 1 Scala source to /.../scala-2.11/test-classes...
[info] WordCountTest:
[info] A WordCount job
[info] - should count words correctly
[info] Run completed in 765 milliseconds.
[info] Total number of tests run: 1
[info] Suites: completed 1, aborted 0
[info] Tests: succeeded 1, failed 0, canceled 0, ignored 0, pending 0
[info] All tests passed.
[success] Total time: 2 s, completed Jan 2, 2016 12:48:25 PM
```

The word count program works!


## Conclusion

Congratulations! You are now running Scalding tests. If you are new to Scala, you may want to brush up on your Scala testing skills.
