# Bulk Loading With MapReduce
In this lab, you will use the bulk loading capabilities of HBase using a MapReduce job on a data set of users.

## Objectives
1. Upload the data set file to HDFS.
2. Use a MapReduce job to generate HFiles
3. Use `completebulkload` to load the HFiles to HBase

## Prerequisites
This lab assumes that the student has a working Hortonworks virtual machine environment.

Ensure that HBase is running and operational on the Hortonworks virtual machine. You can make sure that HBase is running with the `status` command in the HBase shell. The output should look similar to the following:

```shell
hbase(main):002:0> status
1 servers, 0 dead, 3.0000 average load
```

## Instructions
For this lab, we're going to be bulk importing the same data set as `lab-01` but this time we will use a MapReduce job instead of `ImportTsv`. Just in case you skipped `lab-01`, there is a file in the `resources` directory of this lesson called `users.csv` that contains the following:

```
johndoe,John,Doe,john@doe.com,Dallas
bobdobbs,Bob,Dobbs,bob@dobbs.com,Houston
janesmith,Jane,Smith,jane@smith.com,Austin
```

The format of this data is `Row Key,First Name,Last Name,Email,City`. Let's get to the bulk import!

1. The first thing we need to do is get our data set into HDFS. *If you have already moved `users.csv` to HDFS as part of `lab-01`, you can skip ahead to step 4.*

    Normally this would be an easy task with the Ambari HDFS Explorer but at the time of this writing, there is a bug in the current Hortonworks virtual machine that appends garbage characters to the end of files uploaded with Ambari. See [https://issues.apache.org/jira/browse/AMBARI-13786](https://issues.apache.org/jira/browse/AMBARI-13786) for more details. Instead, we must copy the data file using SSH. From your local computer, run the following command from the `lesson-650-bulk-lading-with-hbase` directory:

    ```shell
    $ scp resources/users.csv root@sandbox.hortonworks.com:/tmp
    ```

2. Use ssh to login to the virtual machine.

    ```shell
    ssh root@sandbox.hortonworks.com
    ```

3. Now we need to add the data file to HDFS. Run the following command on the virtual machine:

    ```shell
    [root@sandbox ~]# sudo -u hdfs hadoop fs -put /tmp/users.csv /tmp/users.csv
    ```

4. In preparation for importing this data to HBase, let's create our table. In the virtual machine shell, start the HBase shell.

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

5. Create a table called `bulkusers` with one column family called `data`. Then exit out of the HBase shell.

    ```shell
    hbase(main):003:0> create 'bulkusers', 'data'
    0 row(s) in 2.3690 seconds

    => Hbase::Table - bulkusers
    hbase(main):004:0> exit
    [root@sandbox home]#
    ```

6. In the `lab-02-mapreduce` directory, make sure you can can run `mvn clean install` successfully to compile the project. You should see a successful build message like the following:

    ```shell
    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    ```

7. Open the lab directory, `lab-02-mapreduce`, in your text editor of choice. If you are using Eclipse, you can use the option to *Import an existing Maven project*. Once the project is set up, open `BulkImportLab.java` in `src/main/java/com/example`.

8. The structure of this class should look familiar from the earlier MapReduce lab. We only need to provide implementation for the mapper and then the reducer is handled for us by `HFileOutputFormat2.configureIncrementalLoad()`. Copy and paste the following code in the `map` method:

    ```java
    String[] values = value.toString().split(",");
    String rowKey = values[0];

    Put put = new Put(Bytes.toBytes(rowKey));
    put.addColumn(Bytes.toBytes("data"), Bytes.toBytes("first"), Bytes.toBytes(values[1]));
    put.addColumn(Bytes.toBytes("data"), Bytes.toBytes("last"), Bytes.toBytes(values[2]));
    put.addColumn(Bytes.toBytes("data"), Bytes.toBytes("email"), Bytes.toBytes(values[3]));
    put.addColumn(Bytes.toBytes("data"), Bytes.toBytes("city"), Bytes.toBytes(values[4]));

    ImmutableBytesWritable hKey = new ImmutableBytesWritable(Bytes.toBytes(rowKey));
    context.write(hKey, put);
    ```

    Our `map` method takes each line of input and splits the `String` up into an array based on the "," separator. Then we simply create a new `Put` and add a column for each value represented in the row.

9. Now let's set up our job configuration. Copy and paste the following code in the `main` method:

    ```java
    try {
      Configuration conf = HBaseConfiguration.create();
      Connection connection = ConnectionFactory.createConnection(conf);
      Table table = connection.getTable(TableName.valueOf("bulkusers"));
      RegionLocator regionLocator = connection.getRegionLocator(TableName.valueOf("bulkusers"));

      Job job = Job.getInstance(conf, "UserBulkLoad");

      job.setJarByClass(BulkImportLab.class);
      job.setInputFormatClass(TextInputFormat.class);
      job.setMapOutputKeyClass(ImmutableBytesWritable.class);
      job.setMapperClass(LabMapper.class);
      job.setMapOutputValueClass(Put.class);

      HFileOutputFormat2.configureIncrementalLoad(job, table, regionLocator);

      FileInputFormat.addInputPath(job, new Path(args[0]));
      FileOutputFormat.setOutputPath(job, new Path(args[1]));

      job.waitForCompletion(true);
    } catch(Exception e) {
      e.printStackTrace();
    }
    ```

    This should look familiar from the previous MapReduce labs. Notice that we don't want to use `TableMapReduceUtil` in this lab because we are not importing directly to HBase. Instead, we set an output path in HDFS for the HFiles to be created. The `HFileOutputFormat2.configureIncrementalLoad` call instructs the job to generate HFiles which can later be loaded.

10. Save the lab file and run `mvn clean install` again making sure that there are no compilation errors.

11. To execute the MapReduce job, we need to supply the HDFS input and HDFS output location as arguments. Run the job with the following command.

    ```shell
    bin/run.sh com.example.BulkImportLab  hdfs://sandbox.hortonworks.com:8020/tmp/users.csv hdfs://sandbox.hortonworks.com:8020/tmp/lesson-650
    ```

12. Now we should have the HFiles in `/tmp/lesson-650` ready to be added to HBase. Just like in `lab-01`, our final step is to use `completebulkload` utility for this task. We will be specifying the class name directly from the utility to run which is `LoadIncrementalHFiles`. Run the following command in the virtual machine terminal.

    ```shell
    [root@sandbox ~]# sudo -u hdfs hbase org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles /tmp/lesson-650 bulkusers
    ```

13. Now simply run `hbase shell` and scan the `bulkusers` table to see the final results.

    ```shell
    hbase(main):001:0> scan 'bulkusers'
    ROW                              COLUMN+CELL
     bobdobbs                        column=data:city, timestamp=1450966528378, value=Houston
     bobdobbs                        column=data:email, timestamp=1450966528378, value=bob@dobbs.com
     bobdobbs                        column=data:first, timestamp=1450966528378, value=Bob
     bobdobbs                        column=data:last, timestamp=1450966528378, value=Dobbs
     janesmith                       column=data:city, timestamp=1450966528378, value=Austin
     janesmith                       column=data:email, timestamp=1450966528378, value=jane@smith.com
     janesmith                       column=data:first, timestamp=1450966528378, value=Jane
     janesmith                       column=data:last, timestamp=1450966528378, value=Smith
     johndoe                         column=data:city, timestamp=1450966528378, value=Dallas
     johndoe                         column=data:email, timestamp=1450966528378, value=john@doe.com
     johndoe                         column=data:first, timestamp=1450966528378, value=John
     johndoe                         column=data:last, timestamp=1450966528378, value=Doe
    3 row(s) in 0.2900 seconds
    ```

Congratulations, this lab is complete!
