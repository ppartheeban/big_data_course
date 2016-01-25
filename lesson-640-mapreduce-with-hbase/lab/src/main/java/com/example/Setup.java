package com.example;

import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import com.example.util.Helper;
import com.opencsv.CSVReader;

/**
 * Helper class that inserts data for us to analyze
 */
public class Setup
{
    public static String TABLE_NAME = "transactions";

    public static void main(String[] args) throws IOException {
        Configuration conf = HBaseConfiguration.create();

        Helper helper = Helper.getHelper(conf);
        helper.dropTable(TABLE_NAME);
        helper.createTable(TABLE_NAME, "data");

        try (
            Connection connection = ConnectionFactory.createConnection(conf);
            Table txTable = connection.getTable(TableName.valueOf(TABLE_NAME));
            CSVReader reader = new CSVReader(new FileReader("../../lesson-5xx-resources/tx.csv"));
        ) {
            System.out.println("Inserting transaction test data");
            String[] nextLine;
            reader.readNext(); // skip ahead one line to skip column descriptions
            while ((nextLine = reader.readNext()) != null) {
                byte[] rowKey = DigestUtils.md5(nextLine[0]+nextLine[1]+nextLine[2]);
                
                Put put = new Put(rowKey);
                
                put.addColumn(Bytes.toBytes("data"), Bytes.toBytes("date"), Bytes.toBytes(nextLine[0]));
                put.addColumn(Bytes.toBytes("data"), Bytes.toBytes("description"), Bytes.toBytes(nextLine[1]));
                put.addColumn(Bytes.toBytes("data"), Bytes.toBytes("amount"), Bytes.toBytes(nextLine[2]));
                txTable.put(put);
            }
            // Close the connection to the table
            txTable.close();
            
            System.out.println("All done!");
        }
    }
}
