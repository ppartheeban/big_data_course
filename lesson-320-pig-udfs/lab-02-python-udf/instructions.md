# Pig User Defined Function With Python

In this lab, you will create a Pig User Defined Function (UDF) in Python.

## Objectives

1. Copy a Python UDF script to HDFS.
2. Execute a Pig script that uses the UDF.

## Prerequisites

This lab assumes that the student is familiar with the course environment, in particular, the Hortonworks virtual machine.

## Instructions

For this lab, you will be creating a UDF in Python that converts a row of characters to upper case and calculates the length. Then we will write a Pig script that uses the UDF and observe the results. You will find working Python a little easier because you can avoid the compilation and packaging steps needed with Java.

1. The first thing we need to do is get our data set into HDFS. If you have already performed this task as part of lab-01, you can skip to step 4.

    Normally this would be an easy task with the Ambari HDFS Explorer but at the time of this writing, there is a bug in the current Hortonworks virtual machine that appends garbage characters to the end of files uploaded with Ambari. See [https://issues.apache.org/jira/browse/AMBARI-13786](https://issues.apache.org/jira/browse/AMBARI-13786) for more details. Instead, we must copy the data file using SSH. From your local computer, run the following command from the `lesson-320-pig-udfs` directory:
    
    ```shell
    $ scp resources/now_is_the_time.txt root@sandbox.hortonworks.com:/tmp
    ```

2. Use ssh to login to the virtual machine.

    ```shell
    ssh root@sandbox.hortonworks.com
    ```

3. Now we need to add the data file to HDFS. Run the following command on the virtual machine:

    ```shell
    [root@sandbox ~]# sudo -u hdfs hadoop fs -put /tmp/now_is_the_time.txt /tmp/now_is_the_time.txt
    ```

4. Next we need to create our Python UDF. Open a text editor of your choice and paste the following code in a file called `udf.py`:

    ```python
    @outputSchema("word:chararray")
    def get_upper(data):
        return data.upper()

    @outputSchema("num:long")
    def get_length(data):
        return len(data)
    ```

    We have defined two functions called `get_upper` and `get_length`. The first function, `get_upper` simply returns the input given to it and runs the Python `.upper()` function to convert it to uppercase. The `@outputSchema` decorator is the secret sauce here that declares this to be a UDF when Pig evaluates it. Similarly, the `get_length` function returns the length of the data passed to it.

4. After you have saved the Python file, we must now copy it to HDFS. Run the following command:

    ```shell
    $ scp udf.py root@sandbox.hortonworks.com:/tmp
    ```

5. Now switch over to a terminal on the virtual machine and run the following command to put `udf.py` in HDFS:

    ```shell
    [root@sandbox ~]# sudo -u hdfs hadoop fs -put /tmp/udf.py /tmp/udf.py
    ```

7. Now we are ready to create a Pig script which will use the UDF. Click on the *Pig* button from the Off-canvas menu in Ambari.
8. Press the button *New Script* at the top right and fill in a name for your script. For example, `python-udf`.
9. Paste the following contents into the new Pig script.

    ```
    REGISTER 'hdfs:///tmp/udf.py' USING jython as pyudf
    
    A = LOAD '/tmp/now_is_the_time.txt' AS (row:chararray);
    B = FOREACH A GENERATE pyudf.get_upper(row);
    C = FOREACH A GENERATE pyudf.get_length(row);

    DUMP B;
    DUMP C;
    ```

    First we load the Python UDF from HDFS. Next we load our data file just like the Java UDF script. We assign `B` to the output of running `pyudf.get_upper` and `C` to the result of `pyudf.get_length`. Finally we display the results in the console.
    
10. Press the blue *Execute* button in the Ambari UI to run the Pig script. If everything runs correctly, you should see the data file output in all capital letters along with the length of the data file. The results should look like the following:

    ```
    (NOW IS THE TIME FOR ALL GOOD MEN TO COME TO THE AID OF THEIR COUNTRY. NOW IS THE TIME FOR ALL GOOD MEN TO COME TO THE AID OF THEIR COUNTRY. NOW IS THE TIME FOR ALL GOOD MEN TO COME TO THE AID OF THEIR COUNTRY. NOW IS THE TIME FOR ALL GOOD MEN TO COME TO THE AID OF THEIR COUNTRY. NOW IS THE TIME FOR ALL GOOD MEN TO COME TO THE AID OF THEIR COUNTRY. NOW IS THE TIME FOR ALL GOOD MEN TO COME TO THE AID OF THEIR COUNTRY. NOW IS THE TIME FOR ALL GOOD MEN TO COME TO THE AID OF THEIR COUNTRY. NOW IS THE TIME FOR ALL GOOD MEN TO COME TO THE AID OF THEIR COUNTRY. NOW IS THE TIME FOR ALL GOOD MEN TO COME TO THE AID OF THEIR COUNTRY. NOW IS THE TIME FOR ALL GOOD MEN TO COME TO THE AID OF THEIR COUNTRY.)
    (699)
    ```
