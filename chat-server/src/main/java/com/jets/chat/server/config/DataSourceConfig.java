package com.jets.chat.server.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.util.Properties;

public class DataSourceConfig {
    private static HikariDataSource dataSource;

    static {
        try {
            Properties prop =new Properties();
            prop.load(DataSourceConfig.class.getResourceAsStream("/db.properties"));

            HikariConfig config =new HikariConfig(prop);
            dataSource = new HikariDataSource(config);
        } catch (IOException e) {
            System.out.println("Could establish connection with the database.");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static HikariDataSource getDataSource() {
        return dataSource;
    }
}
