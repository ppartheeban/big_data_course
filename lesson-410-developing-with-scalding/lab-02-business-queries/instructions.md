# Business Queries With Scalding

In this lab, you will use Scalding to perform a business analysis of customer and sales data.


## Objectives

1. Read data from files.
2. Use Scalding functions for grouping and joining.
3. Use Scalding documentation to determine what functions are available and suitable for the task at hand.


## Prerequisites

SBT is required for this exercise. The student may use any text editor; an IDE (Scala IDE, JetBrains IntelliJ, etc) is recommended, but not required.


## Instructions
For this lab, we have a small data set of customers and transactions. This is the same data set used in the Hive lab. Here they are for reference:

### Customers
customerId | email | first | last | state
---|-------|-------|------|------
1 | test@example.com | John | Doe | AL
2 | test2@example.com | Jane | Smith | AL
3 | test3@example.com | Bob | Dobbs | CA
4 | test4@example.com | Peter | Adams | CA
5 | test5@example.com | Sam | Johnson | TX
6 | test6@example.com | Bill | Lucas | TX

### Transactions
transactionId | productId | customerId | purchaseAmount | description
---|-----------|------------|----------------|------------
1 | 1 | 4 | 30 | gorilla costume
2 | 1 | 5 | 30 | gorilla costume
3 | 2 | 1 | 10 | pet rock
4 | 2 | 2 | 10 | pet rock
5 | 2 | 4 | 10 | pet rock
6 | 2 | 5 | 10 | pet rock
7 | 2 | 6 | 10 | pet rock

The business question we are going to answer is:

* For each product, find the number of locations in which that product was purchased.



### The Results

The result should be in a file with the content like this:
```
gorilla costume	2
pet rock	3
```


## Steps

We suggest that you build the program in small steps. Do not try to write everything at once - that is not likely to work well. Write small fragments, try them out, then write some more.

You may want to consult Scalding reference and ScalaDoc.


If you get stuck, compare your code with the code of the solution.


## Hints

#### Organizing your data: Tuples or Classes

##### Tuples
Simple processing can be effectively done by using tuples. You can then use positional parameters (for example `t._1`). Alternatively, you can access the tuples with the construct called _partial function_.

Imagine that you have a pipe that contains person tuples, with tuple elements having the values for  `name` and `email`.

``` scala
myTypedPipe.map {
  case (name, email) => s"name: $name email:$email"
}
...
```
The first element in the tuple is bound to `name`, the second to `email`. This is usually more elegant and readable then positional parameters.


##### Classes

Case classes are well suited for use with Scalding pipes.

``` scala
case class Person(name: String, email: String)

...

val persons: TypedPipe[Person] = getPersons()
```

> Define the case class outside of the Job class!

#### Creating a tuple from a tsv file.

You need to provide the type of the tuple that you want to read in.

``` scala
val customers = TypedPipe.from(TypedTsv[(Long, String, String, String, String)](args("customerFile")))
```
While this works, we have to access the elements in the tuple by their position ._1, ._2,...
It is easy to lose track of fields!

#### A better option: create instances of a class

We can use plain Scala classes or case classes.

Case classes are convenient, but at the time of writing this, they cannot be used for writing into sinks. (If you a curious, see the open issue [Add support for writing case classes to sinks](https://github.com/twitter/scalding/issues/939).)

The way to create instances is the following:
1. Create a typed pipe Using the  `TextLine` source
2. map the lines in the following way:
 1. Parse the line. String method `split()`, or regular expressions come handy.
 2. Create an instance of the desired class, passing the values to the constructor. If your parameter is a Long, you can use `str.toLong()` method.
 3. The instance is the last value in the `map`.

 For example, for some class `Person`:

 ``` scala
 val persons: TypedPipe[Person] = TypedPipe.from(TextLine("data/persons.tsv"))
  .map { line =>
    val splitLine = line.split("\t")
    (new Person(splitLine(0), splitLine(1)))
  }
```
You can mimic such code for the customers and transactions in our exercise.


### Grouping and Joining

You may want to consult the examples in the Scalding [Type Safe API Reference](https://github.com/twitter/scalding/wiki/Type-safe-api-reference).

> You will probably notice that the Scalding documentation is far from perfect. But, it is getting better! It is often necessary to try out functions in order to understand how they work.

#### Hint: Functions Used

There are couple of functions that you are likely to use:
- `join`
- `groupBy`
- `toTypedPipe`
- `sum` - this is a function that works on groups



### Keeping Track of Types

When you are new to Scalding, it is very helpful to write the type declarations. If you are wrong in understanding the flow of tuples, the compiler will remind you of that.

A useful practice is to write out the intermediate values. They will confirm (or deny!) that processing unfolds as you would expect.

If you want to write out groups, don't forget to call `toTypedPipe` before writing.


## Now it is Your Turn!

You will need to add code to the file `BusinessQueries.scala`. The data files are in the `data` folder.

### If you get really stuck...

You can consult the solution code. Don't copy it! Understand it, try it out in little pieces and then put it into your lab project.


## Conclusion

Congratulations, this lab is complete!
