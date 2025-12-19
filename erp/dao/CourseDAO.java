package com.Arijit_Aditya.erp.dao;

import java.sql.*;

public class CourseDAO {
    private final Connection conn;
    public CourseDAO(Connection conn) {
        this.conn = conn;
    }
    // CHECK IF COURSE EXISTS
    public boolean courseExists(String courseId) {
        String sql = "SELECT course_id FROM courses WHERE course_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, courseId);
            ResultSet rs = ps.executeQuery();
            return rs.next(); // true if found
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // CREATE A NEW COURSE
    public boolean createCourse(String courseId, String name) {
        String sql = "INSERT INTO courses(course_id, course_name) VALUES(?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, courseId);
            ps.setString(2, name);
            return ps.executeUpdate() > 0; // inserted successfully
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
