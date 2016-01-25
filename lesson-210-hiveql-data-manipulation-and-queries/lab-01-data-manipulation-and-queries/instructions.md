# HiveQL Data Manipulation and Queries

In this lab, you will be performing some basic data analysis with the [Book Crossing dataset](http://www2.informatik.uni-freiburg.de/~cziegler/BX/). (Book Crossing)[http://www.bookcrossing.com/] is a social network in which people put a label on a book and "release it" into the wild for others to read and then track where they end up.

## Objectives

1. Upload the source data file to the Hortonworks HDFS.
2. Create a Hive table using the book data file.
3. Perform a few HiveQL queries.

## Prerequisites

This lab assumes that the student is familiar with the course environment, in particular, the Hortonworks virtual machine.

## Instructions

1. First, let's extract our dataset and take a look at what we are working with. Unzip the file `lab-01-data-manipulation-and-queries/BX-Books.zip`.

2. Open a terminal and type the following to look at the first few lines of the file.

    ```shell
    $ head lab-01-data-manipulation-and-queries/BX-Books.txt
    ```

    The format of the data is *ISBN; Book Title; Author; Year of Publication; Publisher; Image-URL-S; Image-URL-M; Image-URL-L*. The last three fields correspond to small, medium, and large images from Amazon.

3. Next we need to get our data set into HDFS. Normally this would be an easy task with the Ambari HDFS Explorer but at the time of this writing, there is a bug in the current Hortonworks virtual machine that appends garbage characters to the end of files uploaded with Ambari. See [https://issues.apache.org/jira/browse/AMBARI-13786](https://issues.apache.org/jira/browse/AMBARI-13786) for more details. Instead, we must copy the data file using SSH. From the terminal, run the following command:

    ```shell
    $ scp lab-01-data-manipulation-and-queries/BX-Books.txt root@sandbox.hortonworks.com:/tmp
    ```

3. Use ssh to login to the virtual machine.

    ```shell
    ssh root@sandbox.hortonworks.com
    ```

4. Now we need to add the data file to HDFS. Run the following command on the virtual machine:

    ```shell
    [root@sandbox ~]# sudo -u hdfs hadoop fs -put /tmp/BX-Books.txt /tmp/BX-Books.txt
    [root@sandbox ~]# sudo -u hdfs hadoop fs -chown admin /tmp/BX-Books.txt
    ```

5. Click on the *Hive* button from the Off-canvas menu in the Ambari UI.

6. In the drop down selection, choose the `default` database if it is not already selected.

7. In the Query Editor, copy and paste the following command to create the Hive table to store our book data.

    ```sql
    CREATE TABLE IF NOT EXISTS BookData
    (ISBN STRING,
     BookTitle STRING,
     BookAuthor STRING,
     YearOfPublication INT,
     Publisher STRING)
    ROW FORMAT DELIMITED FIELDS TERMINATED BY '\073' STORED AS TEXTFILE;
    ```

    *Note*: `\073` is the Octal representation of the `;` character.

    You'll notice that the image URLs were left off in the schema of this table. They don't really add anything interesting to the exercise so Hive will just ignore them.

8. Now let's load the data set into the table. Execute the following query.

    ```sql
    LOAD DATA INPATH '/tmp/BX-Books.txt' OVERWRITE INTO TABLE BookData;
    ```

9. Now that the data has been loaded into our table, let's run a query. First let's find the number of books by year. Execute the following query.

    ```sql
    SELECT YearOfPublication, COUNT(BookTitle) FROM BookData GROUP BY YearOfPublication;
    ```

    The first few lines of results should look like the following.

    yearofpublication | _c1
    ----------------- | ---
    1378              | 1
    1897              | 1
    1900              | 3
    1901              | 7
    1902              | 2

    We have the year of publication along with the number of books for that year. As you page through the results, you may notice that one row has a year 0 which doesn't make sense of course. Let's fix that in the next step.

10. Let's clean our data by keeping only records where the year of publication is greater than 0. Execute the following query.

    ```sql
    INSERT OVERWRITE TABLE BookData SELECT BookData.* FROM BookData WHERE YearOfPublication > 0;
    ```

11. Now let's run another query to get books published per year by author and group the results by publisher.

    ```sql
    SELECT Publisher, BookAuthor, YearOfPublication, COUNT(BookTitle) FROM BookData GROUP BY Publisher, BookAuthor, YearOfPublication;
    ```

    You should now have data in the format:

    publisher | bookauthor | yearofpublication | _c3
    ----------|------------|-------------------|----
    1stBooks Library | Emy Thomas | 2001 | 1
    1stBooks Library | Eva Dillner | 2003 | 2

Congratulations, this lab is complete!
