# Pig User Defined Function With Java

In this lab, you will create a Pig User Defined Function (UDF) in Java.

## Objectives

1. Compile the Java class and package it as a JAR file.
2. Copy the JAR to HDFS.
3. Execute a Pig script that uses the JAR containing the UDF.

## Prerequisites

This lab assumes that the student is familiar with the course environment, in particular, the Hortonworks virtual machine.

## Instructions

For this lab, you will be creating a simple UDF in Java that converts all characters to upper case. Then we will write a Pig script that uses the UDF and observe the results.

1. The first thing we need to do is get our data set into HDFS. Normally this would be an easy task with the Ambari HDFS Explorer but at the time of this writing, there is a bug in the current Hortonworks virtual machine that appends garbage characters to the end of files uploaded with Ambari. See [https://issues.apache.org/jira/browse/AMBARI-13786](https://issues.apache.org/jira/browse/AMBARI-13786) for more details. Instead, we must copy the data file using SSH. From your local computer, run the following command from the `lesson-320-pig-udfs` directory:

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

4. In a text editor of your choice, copy and paste the following Java class into a file named `UPPER.java`.

    ```java
    package com.example;

    import java.io.IOException;
    import org.apache.pig.EvalFunc;
    import org.apache.pig.data.Tuple;

    public class UPPER extends EvalFunc<String> {
        public String exec(Tuple input) throws IOException {
            if(input == null || input.size() == 0 || input.get(0) == null)
             return null;
            try {
                String str = (String)input.get(0);
                return str.toUpperCase();
            } catch(Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
    ```

5. Now we need to copy the source to the Hortonworks virtual machine so that we can compile it and put it on HDFS. Use ssh to login to the virtual machine.

    ```shell
    ssh root@sandbox.hortonworks.com
    ```

6. Use the following commands to compile the class, create the directory structure for a JAR file, and then package it up with the `jar` command. The Pig client jar version is accurate at the time of this writing but may be different depending on the version of the Hortonworks virtual machine that you are using. The important thing is that the Pig client jar and Hadoop jars be in the `CLASSPATH` when compiling `UPPER.java`.

    ```shell
    [root@sandbox ~]# javac -cp $(hadoop classpath):/usr/hdp/current/pig-client/pig-0.15.0.2.3.2.0-2950-core-h2.jar UPPER.java
    [root@sandbox ~]# mkdir -p com/example
    [root@sandbox ~]# mv UPPER.* com/example
    [root@sandbox ~]# jar -cf exampleudf.jar com
    ```

7. Now we have our JAR file ready to go so let's put it on HDFS. Run the following commands on the virtual machine to copy it to HDFS.

    ```shell
    [root@sandbox ~]# hadoop fs -mkdir /tmp/pig
    [root@sandbox ~]# hadoop fs -mkdir /tmp/pig/udfs
    [root@sandbox ~]# hadoop fs -put exampleudf.jar /tmp/pig/udfs
    ```

    You can use the following command to verify that the file is indeed on HDFS.

    ```shell
    [root@sandbox ~]# hadoop fs -ls /tmp/pig/udfs
    ```

8. Now we are ready to create a Pig script which will use the UDF. Click on the *Pig* button from the Off-canvas menu.
9. Press the button *New Script* at the top right and fill in a name for your script. For example, `java-udf`.
10. Paste the following contents into the new Pig script.

    ```
    REGISTER 'hdfs:///tmp/pig/udfs/exampleudf.jar';

    typing_line = LOAD '/tmp/now_is_the_time.txt' AS (row:chararray);
    upper_typing_line = FOREACH typing_line GENERATE com.example.UPPER(row);

    DUMP upper_typing_line;
    ```

    You can see that the first thing our script does is register the UDF from our JAR file. Then we load our data file from HDFS. Next we use a `FOREACH` statement that runs the UDF for every line. In our particular case, the data file only contains one line. Finally the output is dumped to the console.
11. Press the blue *Execute* button in the Ambari UI to run the Pig script. If everything runs correctly, you should see the data file output in all capital letters.

    ```
    (NOW IS THE TIME FOR ALL GOOD MEN TO COME TO THE AID OF THEIR COUNTRY. NOW IS THE TIME FOR ALL GOOD MEN TO COME TO THE AID OF THEIR COUNTRY. NOW IS THE TIME FOR ALL GOOD MEN TO COME TO THE AID OF THEIR COUNTRY. NOW IS THE TIME FOR ALL GOOD MEN TO COME TO THE AID OF THEIR COUNTRY. NOW IS THE TIME FOR ALL GOOD MEN TO COME TO THE AID OF THEIR COUNTRY. NOW IS THE TIME FOR ALL GOOD MEN TO COME TO THE AID OF THEIR COUNTRY. NOW IS THE TIME FOR ALL GOOD MEN TO COME TO THE AID OF THEIR COUNTRY. NOW IS THE TIME FOR ALL GOOD MEN TO COME TO THE AID OF THEIR COUNTRY. NOW IS THE TIME FOR ALL GOOD MEN TO COME TO THE AID OF THEIR COUNTRY. NOW IS THE TIME FOR ALL GOOD MEN TO COME TO THE AID OF THEIR COUNTRY.)
    ```
