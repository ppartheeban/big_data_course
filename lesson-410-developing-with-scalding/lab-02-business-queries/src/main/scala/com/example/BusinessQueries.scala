package com.example

import com.twitter.scalding._
import com.twitter.scalding.typed.CoGrouped

// Classes used for the Scalding job should be defined outside the Job
// At the time of writing, case classes cannot be used for writing into sinks
// Therefore, write either tuples or primitive types (Long, String, ...) into sinks
// when working with TypedTsv and similar sinks.

// TODO: add classes for customers and transactions


/**
  * See the WordCountExplicitTypes for a version with explicit types
  *
  * Run in sbt as:
  * run com.example.BusinessQueries --local --customerFile data/customers.tsv --transactionFile data/transactions.tsv --output1 data/query1.tsv
  *
  */
class BusinessQueries(args: Args) extends Job(args) {

  // TODO: Your code here! Read, join, group, ... write


}
