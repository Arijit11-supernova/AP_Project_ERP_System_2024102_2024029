package com.Arijit_Aditya.erp.utils;

import com.Arijit_Aditya.erp.database.MySQLConnection;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

public class HashPasswords {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter username to update password: ");
        String username = sc.nextLine().trim();

        System.out.print("Enter new password: ");
        String newPassword = sc.nextLine().trim();

        if (username.isEmpty() || newPassword.isEmpty()) {
            System.out.println("❌ Username or password cannot be empty.");
            return;
        }

        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

        // Use auth_db explicitly
        String sql = "UPDATE users SET password_hash = ? WHERE username = ?";

        try (Connection conn = MySQLConnection.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, hashedPassword);
            stmt.setString(2, username);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("✅ Password updated successfully for user '" + username + "'.");
            } else {
                System.out.println("❌ No user found with username '" + username + "'.");
            }

        } catch (SQLException e) {
            System.out.println("❌ SQL Error while updating password:");
            e.printStackTrace();
        }
    }
}




