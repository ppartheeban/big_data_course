# Understanding HBase Table Structure

In this lab, you will become familiar with the HBase shell on the Hortonworks virtual machine.

## Objectives
1. Ensure that HBase is running and operational on the Hortonworks virtual machine.
2. Create a test table using the HBase shell and read data from it.

## Prerequisites
This lab assumes that the student is familiar with the course environment, in particular, the Hortonworks virtual machine.

## Instructions
1. Use ssh to login to the virtual machine.

    ```shell
    ssh root@sandbox.hortonworks.com
    ```

2. Start the HBase shell.

    ```shell
    [root@sandbox ~]# hbase shell
    SLF4J: Class path contains multiple SLF4J bindings.
    SLF4J: Found binding in [jar:file:/usr/hdp/2.3.2.0-2950/hadoop/lib/slf4j-log4j12-1.7.10.jar!/org/slf4j/impl/StaticLoggerBinder.class]
    SLF4J: Found binding in [jar:file:/usr/hdp/2.3.2.0-2950/zookeeper/lib/slf4j-log4j12-1.6.1.jar!/org/slf4j/impl/StaticLoggerBinder.class]
    SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
    SLF4J: Actual binding is of type [org.slf4j.impl.Log4jLoggerFactory]
    HBase Shell; enter 'help<RETURN>' for list of supported commands.
    Type "exit<RETURN>" to leave the HBase Shell
    Version 1.1.2.2.3.2.0-2950, r58355eb3c88bded74f382d81cdd36174d68ad0fd, Wed Sep 30 18:56:38 UTC 2015

    hbase(main):001:0>
    ```

3. Run the status command and verify that there are no errors reported. The output should look similar to below.

    ```shell
    hbase(main):002:0> status
    1 servers, 0 dead, 3.0000 average load
    ```

    If you see any errors in the output from the `status` command, it may indicate that HBase has not been started on the virtual machine yet. In this case, navigate to the [admin UI](http://sandbox.hortonworks.com:8080/#/main/services/HBASE/summary) and select Service Actions -> Start on the right hand side. After HBase starts, rerun the `status` command.

4. Create a test table.

    ```shell
    hbase(main):006:0> create 'testtable', 'columnfamily1'
    0 row(s) in 1.2260 seconds

    => Hbase::Table - testtable
    ```

5. Use the `list` command to verify its existence.

    ```shell
    hbase(main):008:0> list 'testtable'
    TABLE
    testtable
    1 row(s) in 0.0340 seconds

    => ["testtable"]
    ```

6. Add a few rows of data using `q1`, `q2`, and `q3` as *column qualifiers*.

    ```shell
    hbase(main):009:0> put 'testtable', 'row-1', 'columnfamily1:q1', 'value-1'
    0 row(s) in 0.1320 seconds

    hbase(main):010:0> put 'testtable', 'row-2', 'columnfamily1:q2', 'value-2'
    0 row(s) in 0.0080 seconds

    hbase(main):011:0> put 'testtable', 'row-2', 'columnfamily1:q3', 'value-3'
    0 row(s) in 0.0060 seconds
    ```

7. Retrieve the data that we just inserted.

    ```shell
    hbase(main):012:0> scan 'testtable'
    ROW                             COLUMN+CELL
    row-1                          column=columnfamily1:q1, timestamp=1448050553319, value=value-1
    row-2                          column=columnfamily1:q2, timestamp=1448050564257, value=value-2
    row-2                          column=columnfamily1:q3, timestamp=1448050578834, value=value-3
    2 row(s) in 0.0370 seconds
    ```

    Notice that the HBase shell outputs each column separately. `row-2` is printed out twice showing the value for each column.
8. Let's retrieve just one row of data. There are many more complicated queries that can be performed with `get` but we will keep it simple for now.

    ```shell
    hbase(main):013:0> get 'testtable', 'row-1'
    COLUMN                          CELL
    columnfamily1:q1               timestamp=1448050553319, value=value-1
    1 row(s) in 0.0320 seconds
    ```

9. Let's now delete a value with the `delete` command. We will start by deleting a single column and verify that it is gone.

  ```shell
  hbase(main):014:0> delete 'testtable', 'row-2', 'columnfamily1:q2'
  0 row(s) in 0.0310 seconds

  hbase(main):015:0> scan 'testtable'
  ROW                             COLUMN+CELL
  row-1                          column=columnfamily1:q1, timestamp=1448050553319, value=value-1
  row-2                          column=columnfamily1:q3, timestamp=1448050578834, value=value-3
  2 row(s) in 0.0200 seconds
  ```

10. Finally, let's clean up our test table.

  ```shell
  hbase(main):016:0> disable 'testtable'
  0 row(s) in 2.2660 seconds

  hbase(main):017:0> drop 'testtable'
  0 row(s) in 1.2380 seconds
  ```

11. Exit the HBase shell and this lab is complete!

  ```shell
  hbase(main):018:0> exit
  [root@sandbox ~]#
  ```
