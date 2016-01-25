
# WordCount in Scala

In this lab, we will use plain Scala to do word count for in-memory collections. If you are familiar with Scala, you can skim over this lab.

## Objectives

1. Understand the data manipulation in Scala.
2. Set the stage for a desired way of processing Big Data in Scalding.


## Prerequisites

SBT is required for this exercise. If you don't have SBT, we provide instructions on how to get it. The student may use any text editor; an IDE (Scala IDE, JetBrains IntelliJ, etc) is recommended, but not required.


## Setup

### Java

You will need to have Java on your machine. You can get it from [here](http://www.oracle.com/technetwork/java/javase/downloads/index.html).

### SBT

You can download SBT from [SBT home](http://www.scala-sbt.org). Follow the installation instruction for your platform.

### Development Environment

An IDE is optional, but recommended. Two popular choices are:

- *IntelliJ*. The Community Edition is free and sufficient. You will need to install Scala plugin. Get IntelliJ from [here](https://www.jetbrains.com/idea/).

- *Scala IDE*. Eclipse based IDE. Get it from [here](http://scala-ide.org).

Alternatively, you could use an editor and command line. If you don't have a favorite editor yet, you can try out [Atom](https://atom.io).

## Inspect the Program

Inspect the source code. Try to understand how it works.

``` scala
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

```

It is not to hard! You will quickly get used to the Scala way of coding.

These types of data manipulation will be used in Scalding, only they will run on Hadoop cluster.

## Run the Scala Program

Open the terminal in the folder that has the `build.sbt` file. Run `sbt`:

```
$ sbt
```

You should see output like this (long path names omitted and replaced with ...):

```
$ sbt
[info] Loading global plugins from /Users/vladimir/.sbt/0.13/plugins
[info] Updating {file:/Users/vladimir/.sbt/0.13/plugins/}global-plugins...
[info] Resolving org.fusesource.jansi#jansi;1.4 ...
[info] Done updating.
[info] Loading project definition from ...lab-01-wordcount-scala/project
[info] Set current project to lab-01-wordcount-scala (in build file: .../lab-01-wordcount-scala/)

```

`run` task compiles everything needed. We invoke it with the program we want to run. There are other tasks like `clear`, `compile`, and `test`.

```
> run com.example.WordCountInMemory
[info] Compiling 1 Scala source to ...lab-01-wordcount-scala/target/scala-2.11/classes...
[info] 'compiler-interface' not yet compiled for Scala 2.11.7. Compiling...
[info]   Compilation completed in 14.564 s
[info] Running com.example.WordCountInMemory com.example.WordCountInMemory
Map(it's -> 1, time's -> 1, heal, -> 1, years -> 1, all -> 1, everything
They -> 1, me, -> 1, ain't -> 1, yeah
But -> 1, I -> 2, that -> 1, to -> 3, these -> 1, was -> 1, go -> 1, over -> 1, after -> 1, Hello, -> 1, supposed -> 1, much -> 1, done -> 1, you'd -> 1, say -> 1, meet -> 1, like -> 1, wondering
If -> 1, healing
" -> 1)
[success] Total time: 17 s, completed Jan 9, 2016 4:33:29 PM

```

The output prints the map with word and frequencies.


## Conclusion

Now you can run Scala programs! If you are new to Scala, it is a good idea to use Worksheet in an IDE and experiment with the language constructs.
