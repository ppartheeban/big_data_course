# MapReduce Development With Java
In this lab, you will run a MapReduce application using the Hadoop Java API. The ["Hello World"](https://en.wikipedia.org/wiki/%22Hello,_World!%22_program) of MapReduce is traditionally a word count program. You will perform a word count on the text of *A Tale of Two Cities*, a classic novel by Charles Dickens.

## Objectives
1. Become familiar with the Mapper and Reducer phase of a MapReduce job.
2. Submit the job on the Hortonworks virtual machine and examine the output.

## Prerequisites
This lab assumes that the student has a working Hortonworks virtual machine environment.

Windows users may need to use [PuTTY](http://www.chiark.greenend.org.uk/~sgtatham/putty/) in order to run `scp` and `ssh` commands.

## Instructions
1. We will be executing the MapReduce job on the Hortonworks virtual machine (VM) so let's first start by copying our data file and Java program to the VM. Open a terminal and in the `lesson-160-mapreduce-development-with-java` directory, execute the following commands.

    ```shell
    scp lab/a-tale-of-two-cities.txt root@sandbox.hortonworks.com:/tmp
    scp lab/WordCount.java root@sandbox.hortonworks.com:/tmp
    ```

2. Use ssh to login to the VM.

    ```shell
    ssh root@sandbox.hortonworks.com
    ```

3. Now we need to add the data file to HDFS. Run the following command on the VM.

    ```shell
    [root@sandbox ~]# sudo -u hdfs hadoop fs -put /tmp/a-tale-of-two-cities.txt /tmp/a-tale-of-two-cities.txt
    ```

4. Now let's compile our Java class and prepare a JAR file to be executed by Hadoop. Run the following commands on the VM.

    ```shell
    [root@sandbox ~]# sudo su - hdfs
    [hdfs@sandbox ~]$ mkdir -p lesson-160/com/example
    [hdfs@sandbox ~]$ cp /tmp/WordCount.java lesson-160/com/example/
    [hdfs@sandbox ~]$ cd lesson-160/
    [hdfs@sandbox lesson-160]$ javac -cp `hadoop classpath` com/example/WordCount.java
    [hdfs@sandbox lesson-160]$ jar cvf wordcount.jar com/
    ```

`hadoop classpath` is a command that prints the class path needed to get the Hadoop jar and the required libraries.


5. We are all set to execute the MapReduce job now but before we do so, open `lab/WordCount.java` locally in a text editor of your choice and let's take a look at what is going on. You can see that we have a public Java class called `WordCount` with two inner classes named `Map` and `Reduce`.

    ```java
    public static class Map extends Mapper<LongWritable, Text, Text, IntWritable> {
        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();

        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            StringTokenizer tokenizer = new StringTokenizer(line);
            while (tokenizer.hasMoreTokens()) {
                word.set(tokenizer.nextToken());
                context.write(word, one);
            }
        }
    }
    ```

    The `Map` class extends `Mapper` which is part of the Hadoop API. Within the angle brackets are the data types for the *input* key and value and the *emitted* key and value. You then are required to override a method called `map` that performs the actual work of the mapper function.

    ```java
    public static class Reduce extends Reducer<Text, IntWritable, Text, IntWritable> {

        public void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            context.write(key, new IntWritable(sum));
        }
    }
    ```

    The `Reduce` class extends Hadoop's `Reducer` API. Again, within the angle brakcets are the data types first for the *input* key and value followed by the *emitted* key and value. The `reduce` method is overridden for the reducer work.

    After that, the main method just sets up the configuration of the Hadoop `Job` object which is the driver of the whole operation. There are two parameters required which are the HDFS location of the *input* file and then the HDFS *output* location.

    ```java
    /**
     * args[0] - HDFS input path
     * args[1] - HDFS output path
     */
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf, "WordCount");

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        job.setMapperClass(Map.class);
        job.setReducerClass(Reduce.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.setJar("wordcount.jar");
        job.waitForCompletion(true);
    }
    ```

6. Let's run the example! In the VM terminal, run the following command.

    ```shell
    [hdfs@sandbox lesson-160]$ yarn jar wordcount.jar com.example.WordCount /tmp/a-tale-of-two-cities.txt /tmp/lesson-160
    ```

    After the job completes running, use the Ambari HDFS Explorer to navigate to `/tmp/lesson-160`. Inside that directory, you should see a file named `part-r-00000` which contains the results of the word count analysis. Download it to your local machine and take a look!

Congratulations, this lab is complete!
