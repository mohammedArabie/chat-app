package com.jets.chat.server.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DataSourceConfig {
    private static HikariDataSource dataSource;

    static {
        Properties props = new Properties();
        try (InputStream input = DataSourceConfig.class.getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (input == null) {
                throw new RuntimeException("Unable to find database.properties");
            }

            props.load(input);

            HikariConfig config = new HikariConfig(props);
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
