package com.example;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.RegionLocator;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.HFileOutputFormat2;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class BulkImportLab {
    
    public BulkImportLab() {
    }
    
    static class LabMapper extends Mapper<LongWritable, Text, ImmutableBytesWritable, Put> {

        public LabMapper() {
        }
        
        @Override
        public void map(LongWritable key, Text value, Context context) 
                throws IOException, InterruptedException {
            String[] values = value.toString().split(",");
            String rowKey = values[0];

            Put put = new Put(Bytes.toBytes(rowKey));
            put.addColumn(Bytes.toBytes("data"), Bytes.toBytes("first"), Bytes.toBytes(values[1]));
            put.addColumn(Bytes.toBytes("data"), Bytes.toBytes("last"), Bytes.toBytes(values[2]));
            put.addColumn(Bytes.toBytes("data"), Bytes.toBytes("email"), Bytes.toBytes(values[3]));
            put.addColumn(Bytes.toBytes("data"), Bytes.toBytes("city"), Bytes.toBytes(values[4]));

            ImmutableBytesWritable hKey = new ImmutableBytesWritable(Bytes.toBytes(rowKey));
            context.write(hKey, put);
        }
    }
    
    /**
     * args[0]: HDFS input path
     * args[1]: HDFS output path
     */
    public static void main(String[] args) {
        try {
            Configuration conf = HBaseConfiguration.create();
            Connection connection = ConnectionFactory.createConnection(conf);
            Table table = connection.getTable(TableName.valueOf("bulkusers"));
            RegionLocator regionLocator = connection.getRegionLocator(TableName.valueOf("bulkusers"));

            Job job = Job.getInstance(conf, "UserBulkLoad");

            job.setJarByClass(BulkImportLab.class);
            job.setInputFormatClass(TextInputFormat.class);
            job.setMapOutputKeyClass(ImmutableBytesWritable.class);
            job.setMapperClass(LabMapper.class);
            job.setMapOutputValueClass(Put.class);

            HFileOutputFormat2.configureIncrementalLoad(job, table, regionLocator);

            FileInputFormat.addInputPath(job, new Path(args[0]));
            FileOutputFormat.setOutputPath(job, new Path(args[1]));

            job.waitForCompletion(true);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
