package com.example.util;

import java.io.Closeable;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;

public class Helper implements Closeable {

    private Configuration configuration = null;
    private Connection connection = null;
    private Admin admin = null;

    protected Helper(Configuration configuration) throws IOException {
        this.configuration = configuration;
        this.connection = ConnectionFactory.createConnection(configuration);
        this.admin = connection.getAdmin();
    }

    public static Helper getHelper(Configuration configuration) throws IOException {
        return new Helper(configuration);
    }

    @Override
    public void close() throws IOException {
        connection.close();
    }

    public Connection getConnection() {
        return connection;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public boolean existsTable(String table) throws IOException {
        return existsTable(TableName.valueOf(table));
    }

    public boolean existsTable(TableName table) throws IOException {
        return admin.tableExists(table);
    }

    public void createTable(String table, String... colfams) throws IOException {
        createTable(TableName.valueOf(table), 1, null, colfams);
    }

    public void createTable(TableName table, String... colfams) throws IOException {
        createTable(table, 1, null, colfams);
    }

    public void createTable(String table, int maxVersions, String... colfams) throws IOException {
        createTable(TableName.valueOf(table), maxVersions, null, colfams);
    }

    public void createTable(TableName table, int maxVersions, String... colfams) throws IOException {
        createTable(table, maxVersions, null, colfams);
    }

    public void createTable(String table, byte[][] splitKeys, String... colfams) throws IOException {
        createTable(TableName.valueOf(table), 1, splitKeys, colfams);
    }

    public void createTable(TableName table, int maxVersions, byte[][] splitKeys, String... colfams)
            throws IOException {
        HTableDescriptor desc = new HTableDescriptor(table);
        for (String cf : colfams) {
            HColumnDescriptor coldef = new HColumnDescriptor(cf);
            coldef.setMaxVersions(maxVersions);
            desc.addFamily(coldef);
        }
        if (splitKeys != null) {
            admin.createTable(desc, splitKeys);
        } else {
            admin.createTable(desc);
        }
    }

    public void disableTable(String table) throws IOException {
        disableTable(TableName.valueOf(table));
    }

    public void disableTable(TableName table) throws IOException {
        admin.disableTable(table);
    }

    public void dropTable(String table) throws IOException {
        dropTable(TableName.valueOf(table));
    }

    public void dropTable(TableName table) throws IOException {
        if (existsTable(table)) {
            if (admin.isTableEnabled(table))
                disableTable(table);
            admin.deleteTable(table);
        }
    }
}
