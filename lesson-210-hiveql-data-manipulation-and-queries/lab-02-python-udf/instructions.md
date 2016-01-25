# Hive User Defined Function in Python

In this lab, you will create a Hive User Defined Function (UDF) in Python.

## Objectives
1. Upload the source data file to the Hortonworks HDFS.
2. Create a Hive table using the data file.
3. Create a new table to store the output of the Python UDF.

## Prerequisites

This lab assumes that the student is familiar with the course environment, in particular, the Hortonworks virtual machine.

## Instructions

Hive allows users to extend its built in functionality with User Defined Functions that can be executed in a variety of different languages. (Pig also supports UDFs, as you will see later.) For this lab we will be creating a UDF in Python. Our data set is a small subset of the [MovieLens 100k Dataset](http://grouplens.org/datasets/movielens) which consists of user generated ratings of movies from [http://movielens.org](http://movielens.org). We'll be using a sample of data in the format:

user id | item id | rating | timestamp
--------|---------|--------|----------
196 | 242 | 3 | 881250949

The time stamps are in [Unix time](https://en.wikipedia.org/wiki/Unix_time) and our Python UDF will convert the time stamp to days of the week so we know what day a user watched the movie.

1. First let's create the UDF. Open a text editor of your choice and copy the following contents to it.

    ```python
    import sys
    import datetime

    for line in sys.stdin:
        # strip the line dataset into individual line
        line = line.strip()

        # split the lines into words with tab delimiter
        userid, movieid, rating, unixtime = line.split('\t')

        # call an inbuilt class in Python to convert the unix timestamp to a weekday
        weekday = datetime.datetime.fromtimestamp(float(unixtime)).isoweekday()

        # create a new row replacing the unix timestamp with the weekday calculated in the previous line
        print '\t'.join([userid, movieid, rating, str(weekday)])
    ```

    A couple of important points:
    * `import sys` is required for all Python UDFs
    * Python UDFs (along with other non-JVM languages) utilize *Hadoop Streaming*. The UDF sends its results to stdout with the `print` statement.

    Save the file as `lab-02-python-udf-python-udf/weekday-udf.py`.

2. Now let's copy the UDF and our data file to the virtual machine. Run the following commands.

    ```shell
    $ scp lab-02-python-udf/weekday-udf.py root@sandbox.hortonworks.com:/tmp
    $ scp lab-02-python-udf/userdata.txt root@sandbox.hortonworks.com:/tmp
    ```

3. Use ssh to login to the VM.

    ```shell
    ssh root@sandbox.hortonworks.com
    ```

4. Run the following commands on the virtual machine to add the files to HDFS.

    ```shell
    [root@sandbox ~]# sudo -u hdfs hadoop fs -put /tmp/weekday-udf.py /tmp/weekday-udf.py
    [root@sandbox ~]# sudo -u hdfs hadoop fs -put /tmp/userdata.txt /tmp/userdata.txt
    ```

5. There are some security features turned on when running a Python UDF in the Ambari UI so we'll be using the Hive command line interface for the HiveQL steps. Start the `hive` CLI on the virtual machine.

    ```shell
    [root@sandbox ~]# hive
    ```
    > On some installations, starting `hive` resulted in a security exception. If that happens, a workaround is to start hive as:

      ```shell
      sudo -u hdfs hive
      ```

6. Create a Hive table that will be used to load the data file.

    ```sql
    hive> CREATE TABLE user_data (userid INT, movieid INT, rating INT, unixtime STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t';
    ```

7. Next, add data to the table from the data file.

    ```sql
    hive> LOAD DATA LOCAL INPATH '/tmp/userdata.txt' OVERWRITE INTO TABLE user_data;
    ```

8. Now we must add the UDF as a resource before we can use it.

    ```sql
    hive> ADD FILE hdfs://sandbox.hortonworks.com:8020/tmp/weekday-udf.py;
    ```

9. Create a new table that will store the output of our data transformation.

    ```sql
    hive> CREATE TABLE user_data_new (userid INT, movieid INT, rating INT, weekday INT) ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t';
    ```

10. Populate the new table with data by invoking the UDF.

    ```sql
    hive> INSERT OVERWRITE TABLE user_data_new SELECT TRANSFORM (userid, movieid, rating, unixtime) USING 'python weekday-udf.py' AS (userid, movieid, rating, weekday) FROM user_data;
    ```

11. Finally, run the following query to view the results of users watching each movie by weekday.

    ```sql
    hive> SELECT weekday, COUNT(*) FROM user_data_new GROUP BY weekday;
    ```

    You should see the following results.

    weekday | count
    --------|------
    1 | 115
    2 | 171
    3 | 182
    4 | 122
    5 | 157
    6 | 159
    7 | 94

Congratulations, this lab is complete!
