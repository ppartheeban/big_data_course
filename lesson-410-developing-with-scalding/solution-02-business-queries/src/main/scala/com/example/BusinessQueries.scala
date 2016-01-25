package com.example

import com.twitter.scalding._
import com.twitter.scalding.typed.CoGrouped

// Classes used for the Scalding job should be defined outside the Job
// At the time of writing, case classes cannot be used for writing into sinks
// Therefore, write either tuples or primitive types (Long, String, ...) into sinks
// when working with TypedTsv and similar sinks.


case class Customer(
                     customerId: Long,
                     email: String,
                     first: String,
                     last: String,
                     state: String
                   )


case class Transaction(
                        transactionId: Long,
                        productId: Long,
                        customerId: Long,
                        purchaseAmount: String,
                        description: String
                      )



/**
  * See the WordCountExplicitTypes for a version with explicit types
  *
  * Run in sbt as:
  * run com.example.BusinessQueries --local --customerFile data/customers.tsv --transactionFile data/transactions.tsv --output1 data/query1.tsv
  *
  */
class BusinessQueries(args: Args) extends Job(args) {

  val customers: TypedPipe[Customer] = TypedPipe.from(TextLine(args("customerFile")))
    .map { line =>
      val splitLine = line.split("\t")
      (Customer(splitLine(0).toInt, splitLine(1), splitLine(2), splitLine(3), splitLine(4)))
    }

  val transactions: TypedPipe[Transaction] = TypedPipe.from(TextLine(args("transactionFile")))
    .map { line =>
      val splitLine = line.split("\t")
      (Transaction(splitLine(0).toInt, splitLine(1).toInt, splitLine(2).toInt, splitLine(3), splitLine(4)))
    }

  val txG: Grouped[Long, Transaction] = transactions.groupBy(_.customerId)
  val csG: Grouped[Long, Customer] = customers.groupBy(_.customerId)
  val joined: CoGrouped[Long, (Transaction, Customer)] = txG.join(csG)

  // Check out the values at this point. e.g.:
  // (1,(Transaction(3,2,1,10,pet rock),Customer(1,test@example.com,John,Doe,AL)))
  // ...
  // joined.toTypedPipe.map(r => r.toString()).write(TypedTsv("data/_test.tsv"))

  val joinedPipe: TypedPipe[(Long, (Transaction, Customer))] = joined.toTypedPipe

  val groupedByDescr: Grouped[String, (Long, (Transaction, Customer))] = joinedPipe.groupBy{  case (pid, (tx, cust)) => tx.description }


  // pet rock	(1,(Transaction(3,2,1,10,pet rock),Customer(1,test@example.com,John,Doe,AL)))
  // ...
  //  groupedByDescr.toTypedPipe.write(TypedTsv("data/_test.tsv"))

  groupedByDescr.toTypedPipe.map {
    case (pd, (custid, (tx, cust))) => (pd, Set(cust.state))
  }

  // the result of the map is a pipe with tuples (String*, Set(String**))
  // * = product description
  // ** = state
  // the implements the "distinct" functionality

  .groupBy(_._1).sum.toTypedPipe

  // we have grouped by the product description
  // the sum will perform a union of sets (duplicates are ignored)

  .map { case(pd, (pds, st)) => (pd, st.size)}

  // Finally, we write out the result
  .write(TypedTsv(args("output1")))

}
