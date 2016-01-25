# Business Queries With Pig
In this lab, you will perform the same analysis as `lesson-210/lab-03-partitioned-table` except instead of Hive, we will be using Pig. One table will represent customer data and then it will be joined with another table representing item purchases. The analysis we will perform will be more business oriented than the traditional word count problem used to introduce Hadoop concepts.

## Objectives
1. Copy data sets to HDFS (if not already there as part of previous Hive lab).
2. Load data sets into Pig.
3. Use a `JOIN` query on the two datasets to answer some real world questions that might arise with this kind of data.

## Prerequisites
This lab assumes that the student is familiar with the course environment, in particular, the Hortonworks virtual machine.

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

The business questions we are going to answer are:

* For each product, find the number of locations in which that product was purchased.
* Find the number of purchases per location for each product.

Let's get started!

1.  *If you have already copied this data as part of `lesson-210/lab-03-partitioned-table`, you can proceed to step 4.*

    First let's copy the data sets over to the virtual machine. Open a terminal and run the following commands.

    ```shell
    $ scp lab/customers.tsv root@sandbox.hortonworks.com:/tmp
    $ scp lab/transactions.tsv root@sandbox.hortonworks.com:/tmp
    ```

2. Use ssh to login to the virtual machine.

    ```shell
    ssh root@sandbox.hortonworks.com
    ```

3. Run the following commands on the virtual machine to add the files to HDFS.

    ```shell
    [root@sandbox ~]# sudo -u hdfs hadoop fs -put /tmp/transactions.tsv /tmp/transactions.tsv
    [root@sandbox ~]# sudo -u hdfs hadoop fs -put /tmp/customers.tsv /tmp/customers.tsv
    ```

4. Start the `pig` command line interface on the virtual machine.

    ```shell
    [root@sandbox ~]# pig
    ```

    >  If pig gives you a security exception at some point, restart it as `sudo -u hdfs pig`.

5. Run the following two commands in the CLI to load the data sets into Pig variables.

    ```
    CUSTOMERS = load '/tmp/customers.tsv' using PigStorage('\t') as (customerId:int, email:chararray, first:chararray, last:chararray, state:chararray);
    TRANSACTIONS = load '/tmp/transactions.tsv' using PigStorage('\t') as (transactionId:int, productId:int, customerId:int, purchaseAmount:int, description:chararray);
    ```

6. Now that both data sets are loaded into Pig, let's get to our first question.
    > For each product, find the number of locations in which that product was purchased.

    Run the following Pig commands in the CLI.
    ```
    A = JOIN TRANSACTIONS by customerId LEFT OUTER, CUSTOMERS by customerId;
    B = GROUP A by description;
    C = FOREACH B {
      LOCS = DISTINCT A.state;
      GENERATE group, COUNT(LOCS) as state_count;
    };
    ```

7. For those used to programming in a traditional language like Java or a scripting language such as Python or Ruby, you may appreciate the fact that you can inspect Pig variables in between each step. Run the following command to see the format of B.

    ```
    DESCRIBE B;
    ```

    You should see the following output.

    ```
    B: {group: chararray,A: {(TRANSACTIONS::transactionId: int,TRANSACTIONS::productId: int,TRANSACTIONS::customerId: int,TRANSACTIONS::purchaseAmount: int,TRANSACTIONS::description: chararray,CUSTOMERS::customerId: int,CUSTOMERS::email: chararray,CUSTOMERS::first: chararray,CUSTOMERS::last: chararray,CUSTOMERS::state: chararray)}}
    ```

9. Let's look at the results of this script. Remember that a MapReduce job is not spawned until you call `DUMP` or `STORE`.

    ```
    DUMP C;
    ```

    After the job completes, you should see the following output.

    ```
    (pet rock,3)
    (gorilla costume,2)
    ```

10. Let's now investigate the answer to our next question.
    > Find the number of purchases per location for each product.

    Run the following Pig commands in the CLI.

    ```
    D = JOIN CUSTOMERS by customerId LEFT OUTER, TRANSACTIONS by customerId;
    E = GROUP D by state;
    F = FOREACH E {
      TXS = DISTINCT D.transactionId;
      GENERATE group, COUNT(TXS) as transaction_count;
    };
    DUMP F;
    ```

    After the job completes, you should see the following output.

    ```
    (AL,2)
    (CA,2)
    (TX,3)
    ```

You can see that we arrived at the same answers to these questions in both Pig and Hive in just a few lines of code. Just to reiterate, the lines of code needed in pure Java MapReduce API would be in the hundreds for this type of analysis.

Congratulations, this lab is complete!
