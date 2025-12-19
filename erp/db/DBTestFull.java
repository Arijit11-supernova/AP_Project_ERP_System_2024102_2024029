package com.Arijit_Aditya.erp.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBTestFull {
    public static void main(String[] args) {
        Connection conn = DBConnection.getConnection();

        if (conn != null) {
            System.out.println("DB Connection OK!\n");

            try (Statement stmt = conn.createStatement()) {

                //users_auth table
                System.out.println("Users_auth table:");
                ResultSet rs = stmt.executeQuery("SELECT * FROM auth_db.users_auth");
                while (rs.next()) {
                    int id = rs.getInt("user_id");
                    String username = rs.getString("username");
                    String role = rs.getString("role");
                    String status = rs.getString("status");
                    String lastLogin = rs.getString("last_login");

                    System.out.println("ID: " + id + ", Username: " + username + ", Role: " + role +
                            ", Status: " + status + ", Last Login: " + lastLogin);
                }
                System.out.println();

                //students table
                System.out.println("Students table:");
                rs = stmt.executeQuery("SELECT * FROM students");
                while (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String rollNo = rs.getString("roll_no");
                    String program = rs.getString("program");
                    int year = rs.getInt("year");

                    System.out.println("UserID: " + userId + ", Roll No: " + rollNo +
                            ", Program: " + program + ", Year: " + year);
                }
                System.out.println();

                //instructors table
                System.out.println("Instructors table:");
                rs = stmt.executeQuery("SELECT * FROM instructors");
                while (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String department = rs.getString("department");

                    System.out.println("UserID: " + userId + ", Department: " + department);
                }
                System.out.println();

                //courses table
                System.out.println("Courses table:");
                rs = stmt.executeQuery("SELECT * FROM courses");
                while (rs.next()) {
                    String courseId = rs.getString("course_id");
                    String title = rs.getString("title");
                    int credits = rs.getInt("credits");

                    System.out.println("CourseID: " + courseId + ", Title: " + title + ", Credits: " + credits);
                }
                System.out.println();

                //sections table
                System.out.println("Sections table:");
                rs = stmt.executeQuery("SELECT * FROM sections");
                while (rs.next()) {
                    int sectionId = rs.getInt("section_id");
                    String courseId = rs.getString("course_id");
                    int instructorId = rs.getInt("instructor_id");
                    String dayTime = rs.getString("day_time");
                    String room = rs.getString("room");
                    int capacity = rs.getInt("capacity");
                    String semester = rs.getString("semester");
                    int year = rs.getInt("year");

                    System.out.println("SectionID: " + sectionId + ", CourseID: " + courseId +
                            ", InstructorID: " + instructorId + ", Day/Time: " + dayTime +
                            ", Room: " + room + ", Capacity: " + capacity +
                            ", Semester: " + semester + ", Year: " + year);
                }
                System.out.println();

                //enrollments table
                System.out.println("Enrollments table:");
                rs = stmt.executeQuery("SELECT * FROM enrollments");
                while (rs.next()) {
                    int enrollmentId = rs.getInt("enrollment_id");
                    int studentId = rs.getInt("student_id");
                    int sectionId = rs.getInt("section_id");
                    String status = rs.getString("status");

                    System.out.println("EnrollmentID: " + enrollmentId + ", StudentID: " + studentId +
                            ", SectionID: " + sectionId + ", Status: " + status);
                }
                System.out.println();

                //grades table
                System.out.println("Grades table:");
                rs = stmt.executeQuery("SELECT * FROM grades");
                while (rs.next()) {
                    int gradeId = rs.getInt("grade_id");
                    int enrollmentId = rs.getInt("enrollment_id");
                    String component = rs.getString("component");
                    int score = rs.getInt("score");
                    String finalGrade = rs.getString("final_grade");

                    System.out.println("GradeID: " + gradeId + ", EnrollmentID: " + enrollmentId +
                            ", Component: " + component + ", Score: " + score + ", Final Grade: " + finalGrade);
                }
                System.out.println();

                //settings table
                System.out.println("Settings table:");
                rs = stmt.executeQuery("SELECT * FROM settings");
                while (rs.next()) {
                    String key = rs.getString("setting_key");
                    String value = rs.getString("setting_value");

                    System.out.println("Key: " + key + ", Value: " + value);
                }
                System.out.println();

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBConnection.closeConnection();
            }
        }
    }
}

