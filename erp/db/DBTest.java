package com.Arijit_Aditya.erp.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DBTest {
    public static void main(String[] args) {
        // Step 1: Get connection
        Connection conn = DBConnection.getConnection();

        if (conn != null) {
            System.out.println("Connection is working!");
        }

        try {
            // Step 2: Run a simple query to test
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM courses");

            System.out.println("Courses table contents:");
            while (rs.next()) {
                System.out.println(
                        "Code: " + rs.getString("course_id") +
                                ", Title: " + rs.getString("title") +
                                ", Credits: " + rs.getInt("credits")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Step 3: Close connection
            DBConnection.closeConnection();
        }
    }
}
