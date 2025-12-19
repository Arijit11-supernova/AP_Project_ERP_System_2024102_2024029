package com.Arijit_Aditya.erp.auth;

import com.Arijit_Aditya.erp.db.AuthDBConnection;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {
    public static User login(String username, String password) {
        if (username == null || password == null) {
            System.out.println("Username or password is null!");
            return null;
        }
        username = username.trim();
        password = password.trim();  // Important: trim input password
        String sql = "SELECT id, username, password_hash, role FROM users WHERE username = ?";
        try (Connection conn = AuthDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password_hash"); // Use password_hash column
                // Debug info (remove in production)
                System.out.println("Input username: '" + username + "'");
                System.out.println("Input password: '" + password + "'");
                System.out.println("Stored hash: '" + storedHash + "'");
                if (BCrypt.checkpw(password, storedHash)) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            storedHash,
                            rs.getString("role")
                    );
                }
                else{
                    System.out.println("Invalid password!");
                }
            }
            else{
                System.out.println("Username not found!");
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        return null;
    }
}




