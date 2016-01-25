# HBase API Access

In this lab, you will become familiar with some basic HBase API components. We will focus on CRUD (create read update delete) operations as they pertain to HBase.

## Objective
Use the `Put`, `Scan`, `Get`, `Delete` APIs to insert some data into a HBase table, read it, and delete it.

## Prerequisites
You should have the following installed:

* Java 1.7+ JDK
* Maven
* Hortonworks virtual machine
* Eclipse, IntelliJ, or your preferred text editor for Java

Ensure that HBase is running and operational on the Hortonworks virtual machine.  You can make sure that HBase is running with the `status` command in the HBase shell (You can start the HBase shell with `hbase shell`). The output should look similar to the following:

```shell
hbase(main):002:0> status
1 servers, 0 dead, 3.0000 average load
```

Keep a terminal session open with the HBase shell so that we can see the results of the API actions in the lab.

## Instructions
1. In the `lab` directory, make sure you can can run `mvn clean install` successfully to compile the project. You should see a successful build message like the following:

    ```shell
    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    ```

2. A script has been provided for you in `bin/run.sh` which will set up the proper `CLASSPATH` when running the lab. Try running it now and the output should look similar to the following:

    ```shell
    $ bin/run.sh com.example.CRUDExample
    log4j:WARN No appenders could be found for logger (org.apache.hadoop.metrics2.lib.MutableMetricsFactory).
    log4j:WARN Please initialize the log4j system properly.
    log4j:WARN See http://logging.apache.org/log4j/1.2/faq.html#noconfig for more info.
    *******************************
    Lab implementation will go here!
    *******************************
    ```

3. Open the lab directory, `lesson-620-hbase-api-access/lab`, in your text editor of choice. If you are using Eclipse, you can use the option to *Import an existing Maven project*. If you are using IntelliJ, you can *Open* an existing Maven project. Once the project is set up, open `CRUDExample.java` in `src/main/java/com/example`.

4. The first thing that happens in `CRUDExample` is we establish a connection to HBase through `HBaseConfiguration`. The connection coordinates are located in `src/main/resources/hbase-site.xml` and should be sufficient as written to connect to the Hortonworks virtual machine. In a real world client application, this XML file would have a lot more configuration to connect to the HBase cluster.

    There is a helper class (`com.example.util.Helper`) provided which makes the process of dropping and creating HBase tables simple. You can see that we are using this helper class to create a table called `testtable` with two column families: `colfam1` and `colfam2`.

    Run the command `scan 'testtable'` in the HBase shell to see that our table exists but there is no data yet.

    ```shell
    hbase(main):006:0> scan 'testtable'
    ROW                         COLUMN+CELL
    0 row(s) in 0.5180 seconds
    ```

5. Now let's insert some data using the `Put` class. Add the following lines after the `System.out.println()` calls in `CRUDExample`.

    ```java
    Put put = new Put(Bytes.toBytes("row1"));
    put.addColumn(Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"), Bytes.toBytes("val1"));
    put.addColumn(Bytes.toBytes("colfam2"), Bytes.toBytes("qual2"), Bytes.toBytes("val2"));
    table.put(put);
    ```

    In the constructor for `Put`, we are declaring that our row key will be named `row1`. Then we use the method  `addColumn(byte[] family, byte[] qualifier, byte[] value)` to add two columns. Recall that column family names, qualifiers, and values are all just arrays of bytes in HBase. There is a utility class provided by HBase in `org.apache.hadoop.hbase.util.Bytes` that provides a lot of convenience methods for converting data to bytes and vice versa.

    After saving the changes you made, run `mvn clean install` and then run the example again with `bin/run.sh com.example.CRUDExample`. Now run `scan 'testtable'` in the HBase shell and you should see output similar to the following:

    ```shell
    hbase(main):007:0> scan 'testtable'
    ROW                         COLUMN+CELL
     row1                       column=colfam1:qual1, timestamp=1450452311412, value=val1
     row1                       column=colfam2:qual2, timestamp=1450452311412, value=val2
    1 row(s) in 1.2230 seconds
    ```

    Notice that since we did not specify a timestamp, one was created for us automatically.

6. Now that we have inserted some data in the table, let's use a `Scan` instance to iterate on the results. Append the following code after the `Put` statements:

    ```java
    Scan scan = new Scan();
    ResultScanner scanner = table.getScanner(scan);
    for (Result result : scanner) {
        while (result.advance())
            System.out.println("Cell: " + result.current());
    }
    ```

    Without any arguments to the constructor, a `Scan` instance will iterate over all rows. In a real world scenario this is probably not what you want because HBase tables can be huge. Normally you would use the `byte[] startRow` and `byte[] stopRow` to limit the scanner. You see that we pass our `Scan` object into a `getScanner` call on the HBase table instance. From there we can iterate across all rows and use the data in our client application. Compile and run with these changes and you should see output similar to the following:

    ```shell
    $ bin/run.sh com.example.CRUDExample
    Cell: row1/colfam1:qual1/1450452585697/Put/vlen=4/seqid=0
    Cell: row1/colfam2:qual2/1450452585697/Put/vlen=4/seqid=0
    ```

7. Now let's use a `Get` instance to retrieve a specific row, column family, and column qualifier. Append the following code to the lab:

    ```java
    Get get = new Get(Bytes.toBytes("row1"));
    get.addColumn(Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"));
    Result result = table.get(get);
    System.out.println("Get result: " + result);
    byte[] val = result.getValue(Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"));
    System.out.println("Value only: " + Bytes.toString(val));
    ```

    We are narrowing down exactly what column we want to retrieve. If you are only interested in the specific value in the column, use `getValue()` on the `Result` object and convert the byte array. If you run this code, you should see the following output:

    ```shell
    $ bin/run.sh com.example.CRUDExample
    *******************************
    Lab implementation will go here!
    *******************************
    Cell: row1/colfam1:qual1/1450453538784/Put/vlen=4/seqid=0
    Cell: row1/colfam2:qual2/1450453538784/Put/vlen=4/seqid=0
    Get result: keyvalues={row1/colfam1:qual1/1450453538784/Put/vlen=4/seqid=0}
    Value only: val1
    ```

8. Finally, let's use `Delete` to remove a column. Append the following code to the lab:

    ```java
    Delete delete = new Delete(Bytes.toBytes("row1"));
    delete.addColumn(Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"));
    table.delete(delete);

    Scan scan2 = new Scan();
    ResultScanner scanner2 = table.getScanner(scan2);
    for (Result result2 : scanner2) {
      System.out.println("Scan: " + result2);
    }
    ```

    You can see that we are deleting one of the two columns that we created. After the delete operation, we create another `Scan` to verify that our table only contains one column. Compile and run the lab and the output should look similar to the following:

    ```shell
    $ bin/run.sh com.example.CRUDExample
    *******************************
    Lab implementation will go here!
    *******************************
    Cell: row1/colfam1:qual1/1450454079283/Put/vlen=4/seqid=0
    Cell: row1/colfam2:qual2/1450454079283/Put/vlen=4/seqid=0
    Get result: keyvalues={row1/colfam1:qual1/1450454079283/Put/vlen=4/seqid=0}
    Value only: val1
    Scan: keyvalues={row1/colfam2:qual2/1450454079283/Put/vlen=4/seqid=0}
    ```

    You can also verify through the HBase shell that our test table only contains one column now:

    ```shell
    hbase(main):008:0> scan 'testtable'
    ROW                         COLUMN+CELL
     row1                       column=colfam2:qual2, timestamp=1450454079283, value=val2
    1 row(s) in 0.4790 seconds
    ```
