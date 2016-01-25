# Hello World, Pig

In this lab, you will perform a ["Hello World"](https://en.wikipedia.org/wiki/%22Hello,_World!%22_program) program in Pig by performing a word count of *Mary Had a Little Lamb*.

## Objectives

1. Upload the source data file to the Hortonworks HDFS.
2. Run the provided Pig script.
3. Observe the word count results.

## Prerequisites

This lab assumes that the student is familiar with the course environment, in particular, the Hortonworks virtual machine.

## Instructions

1. The first thing we need to do is get our data set into HDFS. Normally this would be an easy task with the Ambari HDFS Explorer but at the time of this writing, there is a bug in the current Hortonworks virtual machine that appends garbage characters to the end of files uploaded with Ambari. See [https://issues.apache.org/jira/browse/AMBARI-13786](https://issues.apache.org/jira/browse/AMBARI-13786) for more details. Instead, we must copy the data file using SSH. From your local computer, run the following command from the `lesson-300-pig-overview` directory:
    
    ```shell
    $ scp lab/mary.txt root@sandbox.hortonworks.com:/tmp
    ```

2. Use ssh to login to the virtual machine.

    ```shell
    ssh root@sandbox.hortonworks.com
    ```

3. Now we need to add the data file to HDFS. Run the following command on the virtual machine:

    ```shell
    [root@sandbox ~]# sudo -u hdfs hadoop fs -put /tmp/mary.txt /tmp/mary.txt
    ```

4. Click on the *Pig* button from the Off-canvas menu in the Ambari UI.
5. Press the button *New Script* at the top right and fill in a name for your script.
6. Just in case you are not familiar with this nursery rhyme, here is the text of *Mary Had a Little Lamb*.

    > Mary had a little lamb
    > its fleece was white as snow
    > and everywhere that Mary went
    > the lamb was sure to go.

    Paste the following code into the new Pig script.

    ```
    theinput = load '/tmp/mary.txt' as (line);

    words = foreach theinput generate flatten(TOKENIZE(line)) as word;

    grpd  = group words by word;

    cntd  = foreach grpd generate group, COUNT(words);
    dump cntd;
    ```

    The first line loads input from the file named `mary.txt` and call the single field in the record `line`. Next, `TOKENIZE` splits the line into a field for each word. `flatten` will take the collection of records returned by `TOKENIZE` and produce a separate record for each one. Then they are grouped together by each word and counted. Finally, the results are printed out directly to the console.
7. Execute the script.
8. After a short wait, you should see the results of the word count!
