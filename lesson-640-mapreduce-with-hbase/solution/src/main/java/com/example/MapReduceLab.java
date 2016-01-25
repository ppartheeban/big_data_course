package com.example;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class MapReduceLab {

    public MapReduceLab() {
    }

    static class MyMapper extends TableMapper<Text, IntWritable> {
        private IntWritable ONE = new IntWritable(1);

        public MyMapper() {
        }

        @Override
        protected void map(ImmutableBytesWritable rowkey, Result columns, Context context)
                throws IOException, InterruptedException {
            Cell cell = columns.getColumnLatestCell(Bytes.toBytes("data"), Bytes.toBytes("description"));

            String description = Bytes.toString(CellUtil.cloneValue(cell));
            context.write(new Text(description), ONE);
        }
    }

    static class MyReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
        public MyReducer() {
        }

        @Override
        protected void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable count : values) {
                sum += count.get();
            }
            context.write(key, new IntWritable(sum));
        }
    }

    public static void main(String[] args) {
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
    }
}
