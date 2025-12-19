package com.Arijit_Aditya.erp.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnection {

    // URLs for both databases
    private static final String AUTH_URL = "jdbc:mysql://localhost:3306/auth_db";
    private static final String ERP_URL = "jdbc:mysql://localhost:3306/erp_db";
    private static final String USER = "root";       // your MySQL username
    private static final String PASSWORD = "11@Arijit";   // your MySQL password

    // Separate shared connections
    private static Connection authConnection = null;
    private static Connection erpConnection = null;

    //Get connection for auth_db
    public static Connection getAuthConnection() throws SQLException {
        if (authConnection == null || authConnection.isClosed()) {
            authConnection = DriverManager.getConnection(AUTH_URL, USER, PASSWORD);
        }
        return authConnection;
    }

    //Get connection for erp_db
    public static Connection getERPConnection() throws SQLException {
        if (erpConnection == null || erpConnection.isClosed()) {
            erpConnection = DriverManager.getConnection(ERP_URL, USER, PASSWORD);
        }
        return erpConnection;
    }

    // Close auth_db connection
    public static void closeAuthConnection() {
        try {
            if (authConnection != null && !authConnection.isClosed()) {
                authConnection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Close erp_db connection
    public static void closeERPConnection() {
        try {
            if (erpConnection != null && !erpConnection.isClosed()) {
                erpConnection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


