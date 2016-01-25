
# Scalding APIs

In this lab, we will explore commonly used Scalding APIs.

## Objectives

1. Read and write files.
2. Use Grouped objects.
3. Use joins.


## Prerequisites

SBT is required for this exercise. The student may use any text editor; an IDE (Scala IDE, JetBrains IntelliJ, etc) is recommended, but not required.


## File Copy

We have two versions of the program: one shorter, with inferred types, and another with types spelled out.

We have hardcoded the file name, so the programs are easier to run.

``` scala
package com.example

import com.twitter.scalding._

class FileCopyInferredTypes(args: Args) extends Job(args) {

  val input = TypedPipe.from(TextLine("data/hello-adele.txt"))
  val output = TypedTsv[String]("data/hello-adele-copy.txt")

  input.write(output)
}
```

Run the program in sbt:

```
> run com.example.FileCopyInferredTypes --local
[info] Compiling 4 Scala sources to ...
...
2016-01-19 21:28:44,639 INFO cascading.util.Version: Concurrent, Inc - Cascading 2.6.1
2016-01-19 21:28:44,640 INFO cascading.flow.Flow: [com.example.FileCopyIn...] starting
2016-01-19 21:28:44,640 INFO cascading.flow.Flow: [com.example.FileCopyIn...]  source: FileTap["TextLine[['offset', 'line']->[ALL]]"]["data/hello-adele.txt"]
2016-01-19 21:28:44,640 INFO cascading.flow.Flow: [com.example.FileCopyIn...]  sink: FileTap["TextDelimited[[0]]"]["data/hello-adele-copy.txt"]
2016-01-19 21:28:44,640 INFO cascading.flow.Flow: [com.example.FileCopyIn...]  parallel execution is enabled: true
2016-01-19 21:28:44,640 INFO cascading.flow.Flow: [com.example.FileCopyIn...]  starting jobs: 1
2016-01-19 21:28:44,640 INFO cascading.flow.Flow: [com.example.FileCopyIn...]  allocating threads: 1
2016-01-19 21:28:44,643 INFO cascading.flow.FlowStep: [com.example.FileCopyIn...] starting step: local
[success] Total time: 9 s, completed Jan 19, 2016 9:28:44 PM

```

The version with explicit types:

``` scala
package com.example

import com.twitter.scalding._

class FileCopyExplicitTypes(args: Args) extends Job(args) {

  val input: TypedPipe[String] = TypedPipe.from(TextLine("data/hello-adele.txt"))
  val output: FixedPathTypedDelimited[String] = TypedTsv[String]("data/hello-adele-copy.txt")

  input.write(output)
}
```

For real programs, you would put type declarations where necessary, or where they make program easier to understand.

## Grouping and joins

We calculate word count for the words from the file `data/hello-adele-selected-words.txt`

```
hello
me
you

```

Here is the version with inferred types:
``` scala
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
```

Run the program. The result should be in the file `data/hello-adele-word-selected-count.tsv`.

```
hello	9
me	3
you	15
```

### Experiment with the APIs

Several things you should try out:
- Insert `debug()` calls to print the content of a pipe to the console.
- Filter out words that start with 'm'.
- Check out the ScalaDoc for [TypedPipe](http://twitter.github.io/scalding/#com.twitter.scalding.typed.TypedPipe) and [Grouped](http://twitter.github.io/scalding/#com.twitter.scalding.typed.Grouped) and try out some functions.
- Create a Scala function that operates on strings and apply it to words. Discuss how much easier is that than writing UDFs in Pig or Hive.

> If you are new to Scala, it is a good idea to try out various collection functions in the Worksheet of your IDE. That would help you to become more comfortable with rich set Scalding functions.




## Conclusion

Congratulations! You are now filtering, grouping, and joining data in Scalding. This is the foundation for all Scalding programs.
