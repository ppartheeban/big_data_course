package com.example

import com.twitter.scalding._
import com.twitter.scalding.mathematics.Matrix

class GraphOutDegreeJob(args: Args) extends Job(args) {

  import Matrix._

  val adjacencyMatrix = Tsv("data/graph.tsv", ('user1, 'user2, 'rel))
    .read
    .toMatrix[Long, Long, Double]('user1, 'user2, 'rel)

  // each row i represents all of the outgoing edges from i
  // by summing out all of the columns we get the outdegree of i
  adjacencyMatrix.sumColVectors.write(Tsv("data/outdegree.tsv"))
}
