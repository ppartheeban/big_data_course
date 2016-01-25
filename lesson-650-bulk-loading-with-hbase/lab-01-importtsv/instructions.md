# Bulk Loading With ImportTsv
In this lab, you will use the bulk loading capabilities of HBase using ImportTsv on a data set of users.

## Objectives
1. Upload the data set file to HDFS.
2. Use `ImportTsv` to generate HFiles
3. Use `completebulkload` to load the HFiles to HBase

## Prerequisites
This lab assumes that the student has a working Hortonworks virtual machine environment.

Ensure that HBase is running and operational on the Hortonworks virtual machine. You can make sure that HBase is running with the `status` command in the HBase shell. The output should look similar to the following:

```shell
hbase(main):002:0> status
1 servers, 0 dead, 3.0000 average load
```

## Instructions
For this lab, we're going to be bulk importing a data set in CSV format to HBase. The data set is quite small but the same steps would apply if it were hundreds of megabytes. There is a file in the `resources` directory of this lesson called `users.csv` that contains the following:

```
johndoe,John,Doe,john@doe.com,Dallas
bobdobbs,Bob,Dobbs,bob@dobbs.com,Houston
janesmith,Jane,Smith,jane@smith.com,Austin
```

The format of this data is `Row Key,First Name,Last Name,Email,City`. Let's get to the bulk import!

1. The first thing we need to do is get our data set into HDFS. 

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

5. Create a table called `users` with one column family called `data`. Then exit out of the HBase shell.

    ```shell
    hbase(main):001:0> create 'users', 'data'
    0 row(s) in 1.4620 seconds

    => Hbase::Table - users
    hbase(main):002:0> exit
    [root@sandbox home]#
    ```

6. So now we have our data file in HDFS and our table has been created in HBase. Let's change the permissions on the `hdfs` user's home directory so that we can easily browse to it with the Ambari HDFS Explorer and see the results of our actions later.

    ```shell
    [root@sandbox ~]# sudo -u hdfs hadoop fs -chmod 777 /user/hdfs
    ```

7. We are now ready to run `ImportTsv`. The command line arguments we will be using are:

    * **-Dimporttsv.separator=,** -- By default, `ImportTsv` operates on tab separated files. Our file is comma separated so we need to use this option.
    * **-Dimporttsv.columns=HBASE_ROW_KEY,data:first,data:last,data:email,data:city** -- Here you define the columns that should be created for your specific data file. The first column is our row key and the rest are specified in the form *column family:qualifier*.
    * **-Dimporttsv.bulk.output=output** -- This option tells `ImportTsv` to create HFiles and store them in the `output` directory. The `output` directory will be created relative to the user who is running the process so the actual HDFS location will be `/user/hdfs/output` because we will be running as the `hdfs` user. If you do *not* specify this option, `ImportTsv` will import the data directly into HBase with the normal write path. Obviously that is exactly what we do *not* want to do so it is critical that this option be specified.

    Use the following command line to run our `ImportTsv` process:

    ```shell
    [root@sandbox ~]# sudo -u hdfs hbase org.apache.hadoop.hbase.mapreduce.ImportTsv -Dimporttsv.separator=, -Dimporttsv.columns=HBASE_ROW_KEY,data:first,data:last,data:email,data:city -Dimporttsv.bulk.output=output users /tmp/users.csv
    ```

    The last two arguments are the *table name* and the *HDFS input directory*. This command will produce quite a bit of output. Look for the following message that indicates a successful execution:

    ```shell
    20XX-XX-XX XX:XX:XX,XXX INFO  [main] mapreduce.Job: Job job_1450108371115_0004 completed successfully
    ```

8. Using the HDFS explorer in Ambari, navigate to the `/user/hdfs/output` directory and you can now see the results of the `ImportTsv` execution. The `data` subdirectory contains the HFiles that are ready to be loaded into HBase.

9. Our final step is to use `completebulkload` utility to move the HFiles into HBase. There are two ways to invoke this utility, with explicit classname and via a driver. We will be using the explicit classname because the command line is a bit shorter and specifying jar files is not required.

    The syntax is:
    ```shell
    $ hbase org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles <hdfs://storefileoutput> <tablename>
    ```

    We will be using `output` as the `hdfs://storefileoutput` parameter and recall that this directory is relative to the user's directory running the command so this would be `/user/hdfs/output`. Our table name is `users` so now the command you should run is:

    ```shell
    [root@sandbox ~]# sudo -u hdfs hbase org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles output users
    ```

    This command will produce quite a bit of output. Scan the log messages and make sure that there are no messages at the `ERROR` level.

10. Finally, run `hbase shell` and let's examine our table now that the HFiles have been loaded into HBase. Run `scan 'users'` and you should see the following output:

    ```shell
    hbase(main):001:0> scan 'users'
    ROW                                    COLUMN+CELL
     bobdobbs                              column=data:city, timestamp=1450876372576, value=Houston
     bobdobbs                              column=data:email, timestamp=1450876372576, value=bob@dobbs.com
     bobdobbs                              column=data:first, timestamp=1450876372576, value=Bob
     bobdobbs                              column=data:last, timestamp=1450876372576, value=Dobbs
     janesmith                             column=data:city, timestamp=1450876372576, value=Austin
     janesmith                             column=data:email, timestamp=1450876372576, value=jane@smith.com
     janesmith                             column=data:first, timestamp=1450876372576, value=Jane
     janesmith                             column=data:last, timestamp=1450876372576, value=Smith
     johndoe                               column=data:city, timestamp=1450876372576, value=Dallas
     johndoe                               column=data:email, timestamp=1450876372576, value=john@doe.com
     johndoe                               column=data:first, timestamp=1450876372576, value=John
     johndoe                               column=data:last, timestamp=1450876372576, value=Doe
    3 row(s) in 0.3050 seconds
    ```

Congratulations, this lab is complete!
