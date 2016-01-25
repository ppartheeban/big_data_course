package com.example;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import com.example.util.Helper;

public class CRUDExample {

    public static void main(String[] args) throws IOException {
        Configuration conf = HBaseConfiguration.create();

        Helper helper = Helper.getHelper(conf);
        helper.dropTable("testtable");
        helper.createTable("testtable", "colfam1", "colfam2");

        try (
            Connection connection = ConnectionFactory.createConnection(conf);
            Table table = connection.getTable(TableName.valueOf("testtable"));
        ) {
            Put put = new Put(Bytes.toBytes("row1"));
            put.addColumn(Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"), Bytes.toBytes("val1"));
            put.addColumn(Bytes.toBytes("colfam2"), Bytes.toBytes("qual2"), Bytes.toBytes("val2"));
            table.put(put);

            Scan scan = new Scan();
            ResultScanner scanner = table.getScanner(scan);
            for (Result result : scanner) {
                while (result.advance())
                    System.out.println("Cell: " + result.current());
            }

            Get get = new Get(Bytes.toBytes("row1"));
            get.addColumn(Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"));
            Result result = table.get(get);
            System.out.println("Get result: " + result);
            byte[] val = result.getValue(Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"));
            System.out.println("Value only: " + Bytes.toString(val));

            Delete delete = new Delete(Bytes.toBytes("row1"));
            delete.addColumn(Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"));
            table.delete(delete);

            Scan scan2 = new Scan();
            ResultScanner scanner2 = table.getScanner(scan2);
            for (Result result2 : scanner2) {
                System.out.println("Scan: " + result2);
            }
        }
        helper.close();
    }
}
