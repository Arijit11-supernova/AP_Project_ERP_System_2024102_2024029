package com.Arijit_Aditya.erp.auth;

import com.Arijit_Aditya.erp.utils.SystemState;
import com.Arijit_Aditya.erp.database.MySQLConnection;
import org.mindrot.jbcrypt.BCrypt;
import com.Arijit_Aditya.erp.db.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LoginService {
    private static User loggedInUser;

    // store the last detailed error message for GUI
    private static String lastLoginError = null;

    public static String getLastLoginError() {
        return lastLoginError;
    }

    //Load User from Auth DB
    // (still useful in other places, but NOT used for login lockout logic)
    private static User fetchUser(String username) {
        String sql = "SELECT id, username, password_hash, role FROM users WHERE username = ?";

        try (Connection conn = MySQLConnection.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("role")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    //Check if user exists in Auth DB
    public static boolean userExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (Connection conn = MySQLConnection.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    //Get all users by role from Auth DB
    public static List<String> getAllUsersByRole(String role) {
        List<String> users = new ArrayList<>();
        String sql = "SELECT username FROM users WHERE role = ?";

        try (Connection conn = MySQLConnection.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                users.add(rs.getString("username"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    //Get user ID by username from Auth DB
    public static int getUserIdByUsername(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = MySQLConnection.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // not found
    }

    //Add new user to Auth DB
    public static boolean addUser(String username, String password, String role) {
        if (username == null || username.trim().isEmpty()
                || password == null || password.trim().isEmpty()
                || role == null || role.trim().isEmpty()) {
            System.out.println("❌ Username, password, and role are required.");
            return false;
        }

        username = username.trim();
        role = role.trim();
        String hashedPassword = BCrypt.hashpw(password.trim(), BCrypt.gensalt());
        String normalizedRole = role.toUpperCase(); // "INSTRUCTOR", "STUDENT", "ADMIN"

        try (
                Connection authConn = MySQLConnection.getAuthConnection();
                PreparedStatement authStmt = authConn.prepareStatement(
                        "INSERT INTO users (username, password_hash, role, status) VALUES (?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS
                )
        ) {
            // 1) Insert into auth_db.users
            authStmt.setString(1, username);
            authStmt.setString(2, hashedPassword);
            authStmt.setString(3, normalizedRole);
            authStmt.setString(4, "Active");
            authStmt.executeUpdate();

            int newId;
            try (ResultSet keys = authStmt.getGeneratedKeys()) {
                if (!keys.next()) {
                    System.out.println("❌ Could not get generated user id from auth_db.");
                    return false;
                }
                newId = keys.getInt(1);
            }

            // 2) Mirror into erp_db.users + create role profile
            try (Connection erpConn = DBConnection.getConnection()) {

                // 2a) Insert into erp_db.users
                try (PreparedStatement erpStmt = erpConn.prepareStatement(
                        "INSERT INTO erp_db.users (id, username, password_hash, role, status) " +
                                "VALUES (?, ?, ?, ?, ?)"
                )) {
                    erpStmt.setInt(1, newId);
                    erpStmt.setString(2, username);
                    erpStmt.setString(3, hashedPassword);
                    erpStmt.setString(4, normalizedRole);
                    erpStmt.setString(5, "Active");
                    erpStmt.executeUpdate();
                }

                // 2b) If STUDENT → insert into erp_db.students
                if ("STUDENT".equals(normalizedRole)) {
                    try (PreparedStatement ps = erpConn.prepareStatement(
                            "INSERT INTO students (user_id, roll_no, program, year) " +
                                    "VALUES (?, ?, ?, ?)"
                    )) {
                        ps.setInt(1, newId);
                        ps.setString(2, "AUTO-" + newId);   // simple auto roll no, you can change later
                        ps.setString(3, "CSE");             // default program
                        ps.setInt(4, 1);                    // default year
                        ps.executeUpdate();
                        System.out.println("✅ Student profile created for user_id = " + newId);
                    }
                }

                // 2c) If INSTRUCTOR -> insert into erp_db.instructors
                if ("INSTRUCTOR".equals(normalizedRole)) {
                    try (PreparedStatement ps = erpConn.prepareStatement(
                            "INSERT INTO instructors (user_id, department) VALUES (?, ?)"
                    )) {
                        ps.setInt(1, newId);
                        ps.setString(2, "CSE");  // or take from UI later
                        ps.executeUpdate();
                        System.out.println("✅ Instructor profile created for user_id = " + newId);
                    }
                }
            }

            System.out.println("✅ User created fully with role profile.");
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //Login (Console)
    public static User login() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter username: ");
        String username = sc.nextLine().trim();
        System.out.print("Enter password: ");
        String password = sc.nextLine().trim();

        return validateLogin(username, password, true);
    }

    //Login (GUI)
    public static User login(String username, String password) {
        return validateLogin(username, password, false);
    }

    //Validation Logic (with lockout)
    private static User validateLogin(String username, String password, boolean showMessages) {

        // normalize + reset error
        username = (username == null) ? "" : username.trim();
        password = (password == null) ? "" : password.trim();
        lastLoginError = null;

        String sql = "SELECT id, username, password_hash, role, failed_attempts, locked_until " +
                "FROM users WHERE username = ?";

        try (Connection conn = MySQLConnection.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                lastLoginError = "Invalid username or password.";
                if (showMessages) {
                    System.out.println(lastLoginError);
                }
                return null;
            }

            int id = rs.getInt("id");
            String dbUsername = rs.getString("username");
            String hash = rs.getString("password_hash");
            String role = rs.getString("role");
            int failedAttempts = rs.getInt("failed_attempts");
            Timestamp lockedUntil = rs.getTimestamp("locked_until");

            //Maintenance Mode Check
            if (SystemState.isMaintenanceMode() && !role.equalsIgnoreCase("Admin")) {
                lastLoginError = "System under maintenance. Only Admins may log in.";
                if (showMessages) {
                    System.out.println(lastLoginError);
                }
                return null;
            }

            //Account Lock Check
            long now = System.currentTimeMillis();
            if (lockedUntil != null && lockedUntil.getTime() > now) {
                lastLoginError = "Account is locked until: " + lockedUntil;
                if (showMessages) {
                    System.out.println(lastLoginError);
                }
                return null;
            }

            //Password Verification
            boolean passwordOk = BCrypt.checkpw(password, hash);
            if (!passwordOk) {
                failedAttempts++;

                if (failedAttempts >= 5) {
                    // Lock account for 15 minutes
                    long lockMillis = System.currentTimeMillis() + (15L * 60L * 1000L);
                    Timestamp newLockedUntil = new Timestamp(lockMillis);

                    String lockSql = "UPDATE users SET failed_attempts = ?, locked_until = ? WHERE id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(lockSql)) {
                        ps.setInt(1, failedAttempts);
                        ps.setTimestamp(2, newLockedUntil);
                        ps.setInt(3, id);
                        ps.executeUpdate();
                    }

                    lastLoginError = "Too many failed attempts. Account locked for 15 minutes.";
                    if (showMessages) {
                        System.out.println(lastLoginError);
                    }
                } else {
                    String updSql = "UPDATE users SET failed_attempts = ? WHERE id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(updSql)) {
                        ps.setInt(1, failedAttempts);
                        ps.setInt(2, id);
                        ps.executeUpdate();
                    }

                    int attemptsLeft = 5 - failedAttempts;
                    lastLoginError = "Invalid username or password. Attempts left: " + attemptsLeft;
                    if (showMessages) {
                        System.out.println(lastLoginError);
                    }
                }
                return null;
            }

            //Successful Login -> Reset counters
            String resetSql = "UPDATE users SET failed_attempts = 0, locked_until = NULL, last_login = NOW() " +
                    "WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(resetSql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            if (showMessages) {
                System.out.println("Login successful! Welcome " + dbUsername);
            }

            lastLoginError = null; // success -> no error
            return new User(id, dbUsername, hash, role);

        } catch (SQLException e) {
            e.printStackTrace();
            lastLoginError = "Login error: " + e.getMessage();
            if (showMessages) {
                System.out.println(lastLoginError);
            }
            return null;
        }
    }

    public static void setLoggedInUser(User user) {
        loggedInUser = user;
    }

    public static User getLoggedInUser() {
        return loggedInUser;
    }
}









