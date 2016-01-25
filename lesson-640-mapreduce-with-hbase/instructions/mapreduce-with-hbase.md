# MapReduce With HBase
In this lab, you will run a MapReduce job using HBase as the source of the data. The output will be written to the file system.

## Objectives
1. Become familiar with the Mapper and Reducer phase of a MapReduce job.
2. See usage of `TableMapReduceUtil` when setting up a MapReduce job.

## Prerequisites
You should have the following installed:

* Java 1.7+ JDK
* Maven
* Hortonworks virtual machine
* Eclipse, IntelliJ, or your preferred text editor for Java

Ensure that HBase is running and operational on the Hortonworks virtual machine. You can make sure that HBase is running with the `status` command in the HBase shell. The output should look similar to the following:

```shell
hbase(main):002:0> status
1 servers, 0 dead, 3.0000 average load
```

## Instructions
1. In the `lab` directory, make sure you can can run `mvn clean install` successfully to compile the project. You should see a successful build message like the following:

    ```shell
    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    ```

2. In this lab, we will be running a MapReduce job on an existing HBase table. Before we can start looking at the MapReduce portion of the lab, we need to create our table and populate it. We will be using a transaction data set that you may have already used in the Spark labs.

    Open the lab directory, `lesson-640-mapreduce-with-hbase/lab`, in your text editor of choice. If you are using Eclipse, you can use the option to *Import an existing Maven project*. If you are using IntelliJ, you can *Open* an existing Maven project. Once the project is set up, open `Setup.java` in `src/main/java/com/example`.

3. `Setup` creates a table in HBase called `transactions` with one column family: `data`. The transaction data is in a CSV file and we simply loop through each line and create a column for `date`, `description`, and `amount`. In a real world application, the row key would probably be something like a user id + timestamp. To keep the lab focused and simple, our row key is just a MD5 hash of the row elements.

    A script has been provided for you in `bin/run.sh` which will set up the proper `CLASSPATH` when running the lab. Let's run the `Setup` class now to import our data. The output should look like the following:

    ```shell
    $ bin/run.sh com.example.Setup
    log4j:WARN No appenders could be found for logger (org.apache.hadoop.metrics2.lib.MutableMetricsFactory).
    log4j:WARN Please initialize the log4j system properly.
    log4j:WARN See http://logging.apache.org/log4j/1.2/faq.html#noconfig for more info.
    Inserting transaction test data
    All done!
    ```

4. Now that our HBase table has data, open `MapReduceLab.java` in `src/main/java/com/example` and we can begin creating the MapReduce job. For this lab, we are going to be counting occurrences of the same transaction description. You can see that the class `MapReduceLab` has two inner classes: `MyMapper` and `MyReducer`.

    Let's first focus our attention on `MyMapper`. Notice that it extends `TableMapper` from the `org.apache.hadoop.hbase.mapreduce` package. Since the data being processed will come from a HBase table, `TableMapper` is the appropriate class to have access to each row key and columns. If we were instead performing a MapReduce job on data from some other source, we could just use the Hadoop `Mapper` class.

    Copy and paste the following code in the `map` method:
    ```java
    Cell cell = columns.getColumnLatestCell(Bytes.toBytes("data"), Bytes.toBytes("description"));
    String description = Bytes.toString(CellUtil.cloneValue(cell));
    context.write(new Text(description), ONE);
    ```

    Our `map` method extracts the `description` column from each row and outputs the `description` as the key and 1 as the value. In other words, the mapper just found one occurrence of the transaction description.

5. Now let's shift attention to `MyReducer`. Notice that it extends `org.apache.hadoop.mapreduce.Reducer`. As mentioned before, we are going to output the results of the MapReduce job to the file system. If you wanted to output the results to another HBase table, you would instead use `TableReducer`.

    Copy and paste the following code in the `reduce` method:
    ```java
    int sum = 0;
    for (IntWritable count : values) {
        sum += count.get();
    }
    context.write(key, new IntWritable(sum));
    ```

    Our `reduce` method receives the key and an `Iterable` list of values from the `map` method which in our case is the transaction `description` and a list of 1's for every occurrence. The method needs to simply add up all the `values` that are passed in from the mapper. The result will be the `description` key and the sum which represents how many times the key was found.

6. Our mapper and reducer are all ready to go so the only thing left is to set up the `Job` itself. Copy and paste the following code in the `main` method of `MapReduceLab`:

    ```java
    try {
        Configuration conf = HBaseConfiguration.create();
        Job job = Job.getInstance(conf, "TransactionCounts");
        job.setJarByClass(MapReduceLab.class);

        // Create a scan
        Scan scan = new Scan();

        // Configure the Map process to use HBase
        TableMapReduceUtil.initTableMapperJob(

            "transactions", // The name of the table
            scan, // The scan to execute against the table
            MyMapper.class, // The Mapper class
            Text.class, // The Mapper output key class
            IntWritable.class, // The Mapper output value class
            job); // The Hadoop job

        // Configure the reducer process
        job.setReducerClass(MyReducer.class);
        job.setCombinerClass(MyReducer.class);
        job.setNumReduceTasks(1);

        // Setup the output
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        // Write the results to a file in the output directory
        FileOutputFormat.setOutputPath(job, new Path("output"));

        // Execute the job
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    } catch (Exception e) {
      e.printStackTrace();
    }
    ```

    Most of this code should look familiar from a normal Hadoop MapReduce job. The important thing to notice here is the use of `TableMapReduceUtil.initTableMapperJob`. This is the method that tells our MapReduce job to use HBase as the source of the data being processed. As you may have already guessed, there is another method called `initTableReducerJob` that can be used if the *output* of the job would also be going to HBase. In our case, we are instead putting the results in a directory called `output`.

    Also take note of the `Scan` that we create when initializing the mapper. We are creating the `Scan` to operate on the entire table. If we had designed our row key to include a user id + timestamp, you could perform this type of job for all of a user's transactions or a user's transactions in a certain date range.
7. We have added implementation for the mapper, reducer, and job itself. The only thing left is to actually compile and run! In the lab directory, type `mvn clean install` again to compile the lab. Then type `bin/run.sh com.example.MapReduceLab` to execute our MapReduce job.

8. If everything ran successfully, in the `labs` directory, you should now have an `output` directory with results from the job. Open `output/part-r-00000` in a text editor. The results are ordered lexicographically by transaction description with the sum next to each.

    If you want to run the lab again, make sure you delete the `output` directory first or you will get an error that the directory already exists.

Congratulations, this lab is complete!
