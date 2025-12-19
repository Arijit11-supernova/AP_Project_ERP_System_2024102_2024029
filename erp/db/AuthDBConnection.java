package com.Arijit_Aditya.erp.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class AuthDBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/auth_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "11@Arijit";

    private static Connection connection = null;

    private AuthDBConnection() {}

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Connected to Auth DB successfully!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Failed to connect to Auth DB!");
            e.printStackTrace();
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔒 Auth DB connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
