# HCatalog
In this lab you will go through a scenario in which a data set is loaded with HCatalog and then accessed separately from Pig and Hive.

## Objectives
1. Upload a data set in HDFS.
2. Add the data set to HCatalog with the `hcat` CLI.
3. Access the data with Hive and Pig.

## Prerequisites
This lab assumes that the student is familiar with the course environment, in particular, the Hortonworks virtual machine.

## Instructions
Our data set for this lab is a small list of classic rock songs.

song_name | band_name | year_released
----------|-----------|--------------
Caught Up in You | .38 Special | 1982
Back In Black | AC/DC | 1980
Janie's Got A Gun | Aerosmith | 1989
Ramblin' Man | Allman Brothers Band | 1973
White Wedding | Billy Idol | 1982

1. First let's copy the data set over to the virtual machine. Open a terminal and run the following commands.

    ```shell
    $ scp lab/songs.csv root@sandbox.hortonworks.com:/tmp
    ```

2. Use ssh to login to the virtual machine.

    ```shell
    ssh root@sandbox.hortonworks.com
    ```

3. Run the following commands on the virtual machine to add the files to HDFS.

    ```shell
    [root@sandbox ~]# sudo -u hdfs hadoop fs -mkdir /tmp/lesson-230
    [root@sandbox ~]# sudo -u hdfs hadoop fs -put /tmp/songs.csv /tmp/lesson-230/songs.csv
    ```

4. Let's play the role of the data administrator who is in charge of adding data to the Hadoop cluster. You have just received this data set of songs that needs to be made available for the Hive and Pig users.

    The `hcat` CLI can execute commands from a file or directly on the command line. Let's create an *external* table for this exercise. Recall that when loading data into a *managed* Hive table, Hive controls the lifecycle of the data by moving it to a subdirectory under the directory defined by `hive.metastore.warehouse.dir`. When you drop a managed table, Hive deletes the data in the table.

    By defining an *external* table, we can create a table that points to the data but doesn't take ownership of it. In a situation where you have potentially multiple teams working with a data set, an *external* table is more appropriate. Also note that when creating an external table, you specify a directory in HDFS and not a specific file name.

    Run the following command on the virtual machine.

    ```shell
    [root@sandbox ~]# sudo -u hdfs hcat -e "CREATE EXTERNAL TABLE songs(song_name STRING, band_name STRING, year_released INT) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE LOCATION '/tmp/lesson-230';"
    ```

5. We can use the following command to describe the table that was just created.

    ```shell
    [root@sandbox ~]# hcat -e "describe songs"
    WARNING: Use "yarn jar" to launch YARN applications.
    OK
    song_name           	string
    band_name           	string
    year_released       	int
    Time taken: 7.957 seconds
    ```

6. Now let's access the table from Hive. You could use the `hive` interactive CLI like in previous labs but this time let's use the `-e` parameter to run a single query.

    ```shell
    [root@sandbox ~]# hive -e "select * from songs where band_name = 'Aerosmith'"

    OK
    Janie's Got A Gun	Aerosmith	1989
    Time taken: 26.176 seconds, Fetched: 1 row(s)
    ```

    > If hive gives you a security exception, start it as `sudo -u hdfs hive`.

7. Next let's access the same data from Pig. Start the `pig` CLI.

    ```shell
    [root@sandbox ~]# pig -useHCatalog
    ```
    >  If pig gives you a security exception at some point, restart it as `sudo -u hdfs pig -useHCatalog`.


    The `-useHCatalog` parameter is important in this case to give Pig access to the HCatalog metastore.

8. Now run the following commands in the CLI to get a count of the number of records in the `songs` table. The details of the Pig commands are not really important for now. We are just demonstrating that it is possible to use this same data.

    ```shell
    grunt> songs = load 'songs' using org.apache.hive.hcatalog.pig.HCatLoader();
    grunt> all_count = FOREACH (GROUP songs ALL) GENERATE COUNT(songs);
    grunt> DUMP all_count;
    ```

    The `DUMP` command will produce a lot of debug information as a MapReduce job is spawned. At the end, you should see the output of `(5)` which is our record count output. As you will see in the next set of lessons focused on Pig, normally data is loaded in the syntax `a = load '/some/data'` but because we are loading from HCatalog, the load is done using the class  `org.apache.hive.hcatalog.pig.HCatLoader`.

You should now see that HCatalog provides a nice way to allow users to use the same data set with multiple tools. It can help separate responsibilities among teams as well if your organization is large enough.

Congratulations, this lab is complete!
