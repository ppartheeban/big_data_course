# Hive Partitioned Table
In this lab, you will create a partitioned table of customers and then join it with another table representing item purchases. The analysis we will perform will be more business oriented than the traditional word count problem used to introduce Hadoop concepts.

## Objectives
1. Copy data sets to HDFS.
2. Create a partitioned table in Hive representing customers and a table for transactions.
3. Use a `JOIN` query on the two datasets to answer some real world questions that might arise with this kind of data.

## Prerequisites
This lab assumes that the student is familiar with the course environment, in particular, the Hortonworks virtual machine.

## Instructions
For this lab, we have a small data set of customers and transactions. Here they are for reference:

### Customers
id | email | first | last | state
---|-------|-------|------|------
1 | test@example.com | John | Doe | AL
2 | test2@example.com | Jane | Smith | AL
3 | test3@example.com | Bob | Dobbs | CA
4 | test4@example.com | Peter | Adams | CA
5 | test5@example.com | Sam | Johnson | TX
6 | test6@example.com | Bill | Lucas | TX

### Transactions
id | productId | customerId | purchaseAmount | description
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

1. First let's copy the data sets over to the virtual machine. Open a terminal and run the following commands.

    ```shell
    $ scp lab-03-partitioned-table/customers.tsv root@sandbox.hortonworks.com:/tmp
    $ scp lab-03-partitioned-table/transactions.tsv root@sandbox.hortonworks.com:/tmp
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

4. Start the `hive` command line interface on the virtual machine.

    ```shell
    [root@sandbox ~]# hive
    ```

5. Now let's begin setting up our Hive tables. First create the table where we will initially load our customer data.

    ```sql
    hive> create table staged_customers (id int, email string, first string, last string, state string) row format delimited fields terminated by '\t';
    hive> load data local inpath '/tmp/customers.tsv' overwrite into table staged_customers;
    ```

6. Make sure all the values look like the table from the Introduction earlier.

    ```sql
    hive> select * from staged_customers;
    ```

7. Next let's create the final `customers` table that is partitioned by *state*. In Hive, partitioning is a common strategy for dividing a table into related parts based on the partitioned column. In this case we are using *state* but you might also partition by city, department, date range, etc. Hive only has to scan the partition instead of a full table scan which results in much better performance -- especially when dealing with large amounts of data.

    ```sql
    hive> create table customers (id int, email string, first string, last string) partitioned by (state string);
    ```

8. Run the following query to see the partition listed.

    ```sql
    hive> describe customers;
    ```

    You should see results similar to the following.

    ```code
    OK
    id                  	int
    email               	string
    first               	string
    last                	string
    state               	string

    # Partition Information
    # col_name            	data_type           	comment
    state               	string
    ```

9. Now we need to load data from `staged_customers` to `customers`. In our particular set of test data, we only have three states and thus, three partitions. The query to create each partition would not be too big. However, consider a more substantial data set with all states represented and then creating the query would be quite tedious. We are going to take advantage of a Hive feature known as *dynamic partition inserts* for this task as if we were dealing with many partitions. Hive can infer the partition to create based on the query. Run the following query.

    ```sql
    hive> insert overwrite table customers partition (state) select id, email, first, last, sc.state from staged_customers sc;
    ```

    You should see results similar to the following.

    ```code
    Loading data to table default.customers partition (state=null)
    	 Time taken for load dynamic partitions : 4308
    	Loading partition {state=CA}
    	Loading partition {state=AL}
    	Loading partition {state=TX}
    	 Time taken for adding to write entity : 6
    Partition default.customers{state=AL} stats: [numFiles=1, numRows=2, totalSize=59, rawDataSize=57]
    Partition default.customers{state=CA} stats: [numFiles=1, numRows=2, totalSize=62, rawDataSize=60]
    Partition default.customers{state=TX} stats: [numFiles=1, numRows=2, totalSize=63, rawDataSize=61]
    ```

10. Let's create the `transactions` table now. Run the following queries to create the table and load data into it.

    ```sql
    hive> create table transactions (id int, productId int, customerId int, purchaseAmount int, description string) row format delimited fields terminated by '\t';
    hive> load data local inpath '/tmp/transactions.tsv' overwrite into table transactions;
    ```

11. Double check that the data got loaded correctly.

    ```sql
    hive> select * from transactions;
    ```

12. Now that the tables are set up and loaded, we can get on with the business questions at hand. Our first question is:
    > For each product, find the number of locations in which that product was purchased.

    Enter the following query to find the answer.

    ```sql
    hive> select description, count(distinct state) from transactions t left outer join customers c on t.customerId = c.id group by description;
    ```

    After a period of time, you should see the following results. The data set is small enough that you can also quickly double check the results by inference too.

    description | count
    ------------|------
    gorilla costume | 2
    pet rock | 3


13. Finally, let's examine this last question.
    > Find the number of purchases per location for each product.

    Enter the following query.

    ```sql
    hive> select state, count(distinct t.id) from customers c left outer join transactions t on t.customerId = c.id group by state;
    ```

    The results should show the following.

    state | count
    ------|------
    AL | 2
    CA | 2
    TX | 3

    Again, the data set is small enough that you can confirm these results by inspecting the values.

    With just a few lines of HiveQL, we are able to perform some powerful analysis on Hadoop. To achieve the same results in Java, the lines of code required would be in the hundreds.

Congratulations, this lab is complete!
