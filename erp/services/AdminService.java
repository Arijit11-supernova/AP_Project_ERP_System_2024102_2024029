package com.Arijit_Aditya.erp.services;

import com.Arijit_Aditya.erp.auth.LoginService;
import com.Arijit_Aditya.erp.auth.User;
import com.Arijit_Aditya.erp.db.DBConnection;
import com.Arijit_Aditya.erp.db.AuthDBConnection;
import com.Arijit_Aditya.erp.models.Course;
import com.Arijit_Aditya.erp.utils.SystemState;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.Serializable;
import java.sql.*;
import java.util.*;

public class AdminService implements Serializable {
    private static final long serialVersionUID = 1L;

    private final User currentUser;

    public AdminService(User currentUser) {
        this.currentUser = currentUser;
    }

    private boolean isAdmin() {
        return currentUser.getRole().equalsIgnoreCase("Admin");
    }

    // USERS
    public boolean addUser(String username, String password, String role) {
        if (!isAdmin()) return false;
        if (username.isEmpty() || password.isEmpty() || role.isEmpty()) return false;
        return LoginService.addUser(username, password, role);
    }

    // STUDENTS
    public boolean addStudent(int userId, String rollNo, String program, int year) {
        String sql = "INSERT INTO students (user_id, roll_no, program, year) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, rollNo);
            stmt.setString(3, program);
            stmt.setInt(4, year);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // COURSES
    public boolean createCourse(String courseCode, String courseName, int credits) {
        if (!isAdmin()) return false;
        String sql = "INSERT INTO courses (course_id, title, credits) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseCode);   // e.g. "CS1007"
            stmt.setString(2, courseName);   // e.g. "Machine Learning"
            stmt.setInt(3, credits);         // e.g. 5

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error creating course: " + e.getMessage());
            return false;
        }
    }

    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT course_id, title, credits FROM erp_db.courses";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String courseId = rs.getString("course_id");
                String courseName = rs.getString("title");  // YES, this is correct
                int credits = rs.getInt("credits");

                Course course = new Course(courseId, courseName, credits);
                courses.add(course);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching courses: " + e.getMessage());
        }

        return courses;
    }

    public Course getCourseById(String courseId) {
        String sql = "SELECT course_id, title, credits FROM erp_db.courses WHERE course_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Course(
                        rs.getString("course_id"),
                        rs.getString("title"),      // <-- here
                        rs.getInt("credits")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error fetching course: " + e.getMessage());
        }
        return null;
    }

    // SECTIONS
    public boolean createSection(String courseId, String sectionName, String instructorUsername,
                                 String dayTime, String room, int capacity, String semester, int year) {
        if (!isAdmin()) return false;

        // Optional: still validate that this username is an instructor using LoginService
        List<String> instructors = LoginService.getAllUsersByRole("Instructor");
        if (!instructors.contains(instructorUsername)) {
            System.out.println("Error: Instructor does not exist or is not valid.");
            return false;
        }

        String findInstructorSql = "SELECT id FROM users WHERE username = ? AND role = 'INSTRUCTOR'";

        String insertSql = """
                INSERT INTO sections
                (course_id, section_name, instructor_id, day_time, room, capacity, semester, year)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection()) {

            // 1) Look up instructor's numeric ID
            Integer instructorId = null;
            try (PreparedStatement findStmt = conn.prepareStatement(findInstructorSql)) {
                findStmt.setString(1, instructorUsername);
                try (ResultSet rs = findStmt.executeQuery()) {
                    if (rs.next()) {
                        instructorId = rs.getInt("id");
                    }
                }
            }

            if (instructorId == null) {
                System.out.println("Error: Could not find instructor user_id for username: " + instructorUsername);
                return false;
            }

            // 2) Insert section using the numeric instructor_id
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, courseId);
                stmt.setString(2, sectionName);
                stmt.setInt(3, instructorId);    // ✅ now an INT, correct for instructor_id column
                stmt.setString(4, dayTime);
                stmt.setString(5, room);
                stmt.setInt(6, capacity);
                stmt.setString(7, semester);
                stmt.setInt(8, year);

                return stmt.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            System.out.println("Error creating section: " + e.getMessage());
            return false;
        }
    }

    public List<String> getSectionsForCourse(String courseId) {
        List<String> sections = new ArrayList<>();

        // Debug: see what we are looking for
        System.out.println("DEBUG: Looking for sections for courseId = [" + courseId + "]");

        // Fully qualify the table with DB name, so even if connection points to auth_db,
        // it will still fetch from erp_db.sections.
        String sql = "SELECT section_name FROM erp_db.sections WHERE course_id = ?";

        try (
                Connection conn = DBConnection.getConnection();   // your ERP connection class
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, courseId);
            ResultSet rs = stmt.executeQuery();

            boolean any = false;
            while (rs.next()) {
                any = true;
                String sec = rs.getString("section_name");
                System.out.println("DEBUG: Found section [" + sec + "] for courseId [" + courseId + "]");
                sections.add(sec);
            }

            if (!any) {
                System.out.println("DEBUG: No sections returned from DB for courseId = [" + courseId + "]");
            }

        } catch (SQLException e) {
            System.out.println("SQL ERROR in getSectionsForCourse: " + e.getMessage());
            e.printStackTrace();
        }

        return sections;
    }

    // Fetch instructor username for a specific course and section
    public String getInstructorForSection(String courseId, String sectionName) {
        String instructorName = null;

        String sql = """
                    SELECT u.username
                    FROM erp_db.sections s
                    JOIN erp_db.users u
                      ON s.instructor_id = u.id
                    WHERE s.course_id = ? AND s.section_name = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseId);
            stmt.setString(2, sectionName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    instructorName = rs.getString("username");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error fetching instructor name: " + e.getMessage());
            e.printStackTrace();
        }

        return instructorName;
    }

    // INSTRUCTOR-RELATED METHODS
    public boolean assignInstructor(String courseId, String sectionName, String instructorUsername) {
        if (!isAdmin()) return false;

        List<String> instructors = LoginService.getAllUsersByRole("Instructor");
        if (!instructors.contains(instructorUsername)) {
            System.out.println("Error: Instructor does not exist or is not valid.");
            return false;
        }

        String findInstructorSql = "SELECT id FROM users WHERE username = ? AND role = 'INSTRUCTOR'";
        String updateSql = "UPDATE sections SET instructor_id = ? WHERE course_id = ? AND section_name = ?";

        try (Connection conn = DBConnection.getConnection()) {

            Integer instructorId = null;
            try (PreparedStatement findStmt = conn.prepareStatement(findInstructorSql)) {
                findStmt.setString(1, instructorUsername);
                try (ResultSet rs = findStmt.executeQuery()) {
                    if (rs.next()) {
                        instructorId = rs.getInt("id");
                    }
                }
            }

            if (instructorId == null) {
                System.out.println("Error: Could not find instructor user_id for username: " + instructorUsername);
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setInt(1, instructorId);
                stmt.setString(2, courseId);
                stmt.setString(3, sectionName);
                return stmt.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            System.out.println("Error assigning instructor: " + e.getMessage());
            return false;
        }
    }

    public List<String> getSectionsByInstructor(String instructorUsername) {
        List<String> result = new ArrayList<>();
        Map<String, List<String>> map = getInstructorCourseSections(instructorUsername);
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            String courseId = entry.getKey();
            List<String> sections = entry.getValue();
            if (sections.isEmpty()) {
                result.add("Course:" + courseId + " → No Sections");
            } else {
                for (String sec : sections) {
                    result.add("Course:" + courseId + " | Section:" + sec);
                }
            }
        }
        return result;
    }

    public Map<String, List<String>> getInstructorCourseSections(String instructorUsername) {
        Map<String, List<String>> courseToSections = new LinkedHashMap<>();

        String sql = """
                SELECT s.course_id, s.section_name
                FROM sections s
                JOIN users u ON s.instructor_id = u.id
                WHERE u.username = ?
                ORDER BY s.course_id, s.section_name
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, instructorUsername);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String courseId = rs.getString("course_id");
                    String sectionName = rs.getString("section_name");

                    courseToSections
                            .computeIfAbsent(courseId, k -> new java.util.ArrayList<>())
                            .add(sectionName);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return courseToSections;
    }

    // STUDENT ENROLLMENTS
    public boolean registerStudent(String username, String courseId, String sectionName) {
        // Block registration if grades are already finalized
        if (areGradesComputed(courseId, sectionName)) {
            System.out.println("❌ Cannot register. Grades already finalized for this section.");
            return false;
        }
        try (Connection conn = DBConnection.getConnection()) {

            Integer studentId = null;

            // 0) Try to find existing student profile in students
            String findStudentSql = """
                        SELECT s.user_id
                        FROM students s
                        JOIN users u ON s.user_id = u.id
                        WHERE u.username = ?
                    """;

            try (PreparedStatement ps = conn.prepareStatement(findStudentSql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        studentId = rs.getInt("user_id");
                    }
                }
            }

            //  If no student profile, auto-create one
            if (studentId == null) {
                System.out.println("No student profile found for username: " + username + " → auto-creating.");

                // Look up user id & role from users
                String findUserSql = """
                            SELECT id, role
                            FROM users
                            WHERE username = ?
                        """;

                Integer userId = null;
                String role = null;

                try (PreparedStatement ps = conn.prepareStatement(findUserSql)) {
                    ps.setString(1, username);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            userId = rs.getInt("id");
                            role = rs.getString("role");
                        }
                    }
                }

                // Only auto-create profile if this is actually a STUDENT
                if (userId == null || role == null || !role.equalsIgnoreCase("STUDENT")) {
                    System.out.println("❌ Cannot auto-create student profile: user not found or not a STUDENT.");
                    return false;
                }

                // Insert into students table with some default values
                String insertStudentSql = """
                            INSERT INTO students (user_id, roll_no, program, year)
                            VALUES (?, ?, ?, ?)
                        """;

                try (PreparedStatement ps = conn.prepareStatement(insertStudentSql)) {
                    ps.setInt(1, userId);
                    ps.setString(2, "AUTO-" + userId); // simple auto roll no
                    ps.setString(3, "CSE");             // default program
                    ps.setInt(4, 1);                    // default year
                    ps.executeUpdate();
                }

                studentId = userId;  // now studentId is valid
            }

            // 1) Find section_id from courseId + sectionName
            Integer sectionId = null;
            String findSectionSql = """
                        SELECT section_id
                        FROM sections
                        WHERE course_id = ? AND section_name = ?
                    """;

            try (PreparedStatement ps = conn.prepareStatement(findSectionSql)) {
                ps.setString(1, courseId);
                ps.setString(2, sectionName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        sectionId = rs.getInt("section_id");
                    }
                }
            }

            if (sectionId == null) {
                System.out.println("❌ No section found for " + courseId + " - " + sectionName);
                return false;
            }

            // 2) Check for duplicate enrollment
            String checkSql = """
                        SELECT COUNT(*)
                        FROM enrollments
                        WHERE student_id = ? AND section_id = ?
                    """;

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, studentId);
                checkStmt.setInt(2, sectionId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("⚠️ Already enrolled in this section.");
                        return false;
                    }
                }
            }

            // 3) Insert enrollment
            String insertSql = """
                        INSERT INTO enrollments (student_id, section_id, status)
                        VALUES (?, ?, 'Enrolled')
                    """;

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setInt(1, studentId);
                insertStmt.setInt(2, sectionId);
                int rows = insertStmt.executeUpdate();
                return rows > 0;
            }

        } catch (SQLException e) {
            System.out.println("❌ Error in registerStudent: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean dropSection(String username, String courseId, String sectionName) {
        // 0) Block if a finalized grade already exists
        if (hasFinalizedGrade(username, courseId, sectionName)) {
            System.out.println("Cannot drop: finalized grade already exists for "
                    + username + " in " + courseId + " - " + sectionName);
            return false;
        }

        try (Connection conn = DBConnection.getConnection()) {

            // 1) Find student's user_id from students + users
            Integer studentId = null;
            String studentSql = """
                    SELECT s.user_id
                    FROM students s
                    JOIN users u ON s.user_id = u.id
                    WHERE u.username = ?
                    """;

            try (PreparedStatement ps = conn.prepareStatement(studentSql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        studentId = rs.getInt("user_id");
                    }
                }
            }
            if (studentId == null) {
                System.out.println("No student profile for username: " + username);
                return false;
            }

            // 2) Find section_id from courseId + sectionName
            Integer sectionId = null;
            String sectionSql = """
                    SELECT section_id
                    FROM sections
                    WHERE course_id = ? AND section_name = ?
                    """;

            try (PreparedStatement ps = conn.prepareStatement(sectionSql)) {
                ps.setString(1, courseId);
                ps.setString(2, sectionName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        sectionId = rs.getInt("section_id");
                    }
                }
            }
            if (sectionId == null) {
                System.out.println("No section found for " + courseId + " - " + sectionName);
                return false;
            }

            // 3) Delete from enrollments
            String deleteSql = """
                    DELETE FROM enrollments
                    WHERE student_id = ? AND section_id = ?
                    """;

            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setInt(1, studentId);
                ps.setInt(2, sectionId);
                int rows = ps.executeUpdate();
                return rows > 0;
            }

        } catch (SQLException e) {
            System.out.println("Error dropping section: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // STUDENT SCORES & GRADES
    public boolean addStudentScore(String courseId,
                                   String section,
                                   String studentUsername,
                                   double quizObtained,
                                   double quizMax,
                                   double assignmentObtained,
                                   double assignmentMax,
                                   double midsemObtained,
                                   double midsemMax,
                                   double endsemObtained,
                                   double endsemMax) {

        //  Block edits if this section is already finalized
        if (areGradesComputed(courseId, section)) {
            System.out.println("Grades already finalized for " + courseId + " - " + section);
            return false;
        }

        // 1) Get weights for this section
        int[] weights = getSectionGradeScheme(courseId, section);
        int quizW       = weights[0];
        int assignmentW = weights[1];
        int midsemW     = weights[2];
        int endsemW     = weights[3];

        int totalW = quizW + assignmentW + midsemW + endsemW;
        if (totalW != 100) {
            System.out.println("❌ Invalid weight configuration (sum != 100) for " +
                    courseId + " - " + section);
            return false;
        }

        // 2) Compute final score 0–100
        double finalScoreDouble = computeFinalScoreFromComponents(
                quizObtained, quizMax,
                assignmentObtained, assignmentMax,
                midsemObtained, midsemMax,
                endsemObtained, endsemMax,
                quizW, assignmentW, midsemW, endsemW
        );

        // because score column is INT
        int finalScore = (int) Math.round(finalScoreDouble);

        String sql = """
        INSERT INTO grades (
            course_id,
            section,
            student_username,
            quiz_obtained,
            quiz_max,
            assignment_obtained,
            assignment_max,
            midsem_obtained,
            midsem_max,
            endsem_obtained,
            endsem_max,
            score,
            finalized
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
        ON DUPLICATE KEY UPDATE
            quiz_obtained       = VALUES(quiz_obtained),
            quiz_max            = VALUES(quiz_max),
            assignment_obtained = VALUES(assignment_obtained),
            assignment_max      = VALUES(assignment_max),
            midsem_obtained     = VALUES(midsem_obtained),
            midsem_max          = VALUES(midsem_max),
            endsem_obtained     = VALUES(endsem_obtained),
            endsem_max          = VALUES(endsem_max),
            score               = VALUES(score),
            finalized           = 0
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseId);
            stmt.setString(2, section);
            stmt.setString(3, studentUsername);

            stmt.setDouble(4,  quizObtained);
            stmt.setDouble(5,  quizMax);
            stmt.setDouble(6,  assignmentObtained);
            stmt.setDouble(7,  assignmentMax);
            stmt.setDouble(8,  midsemObtained);
            stmt.setDouble(9,  midsemMax);
            stmt.setDouble(10, endsemObtained);
            stmt.setDouble(11, endsemMax);

            stmt.setInt(12, finalScore);

            stmt.executeUpdate();
            return true;

        } catch (Exception e) {
            System.out.println("❌ Error adding student component scores: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, Integer> getStudentScores(String courseId, String section) {
        Map<String, Integer> scores = new HashMap<>();

        String sql = """
                SELECT student_username, score
                FROM grades
                WHERE course_id = ?
                  AND section   = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseId);
            stmt.setString(2, section);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String username = rs.getString("student_username");
                    int score = rs.getInt("score");
                    scores.put(username, score);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error fetching student scores: " + e.getMessage());
            e.printStackTrace();
        }

        return scores;
    }

    public boolean areGradesComputed(String courseId, String sectionName) {
        String sql = """
                SELECT COUNT(*)
                FROM grades
                WHERE course_id = ?
                  AND section   = ?
                  AND finalized = TRUE
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseId);
            stmt.setString(2, sectionName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // At least one finalized grade for this course+section
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.out.println("Error checking if grades are computed: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public boolean computeFinalGrades(String courseId, String section) {
        String sql = "UPDATE grades SET finalized = TRUE WHERE course_id = ? AND section = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            stmt.setString(2, section);
            int updated = stmt.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Helper: check if this student already has a finalized grade in this course+section
    private boolean hasFinalizedGrade(String username, String courseId, String sectionName) {
        String sql = """
                SELECT COUNT(*)
                FROM grades
                WHERE course_id        = ?
                  AND section          = ?
                  AND student_username = ?
                  AND finalized        = TRUE
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseId);
            stmt.setString(2, sectionName);
            stmt.setString(3, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.out.println("Error checking finalized grade: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public String calculateGrade(int score) {
        if (score >= 90) return "A+";
        if (score >= 80) return "A";
        if (score >= 70) return "B+";
        if (score >= 60) return "B";
        if (score >= 50) return "C";
        return "F";
    }

    // STUDENT ENROLLMENTS
    public List<String> getStudentEnrollments(String studentUsername) {
        List<String> enrollments = new ArrayList<>();

        String sql = """
                SELECT c.course_id,
                       s.section_name
                FROM enrollments e
                JOIN students st ON e.student_id = st.user_id
                JOIN users u    ON st.user_id = u.id
                JOIN sections s ON e.section_id = s.section_id
                JOIN courses c  ON s.course_id = c.course_id
                WHERE u.username = ?
                  AND e.status = 'Enrolled'
                ORDER BY c.course_id, s.section_name
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentUsername);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String courseId = rs.getString("course_id");
                    String sectionName = rs.getString("section_name");

                    // format: "CS1001:B"
                    String key = courseId + ":" + sectionName;
                    enrollments.add(key);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error fetching student enrollments: " + e.getMessage());
            e.printStackTrace();
        }

        return enrollments;
    }

    public List<String> getStudentEnrollmentsForSection(String courseId, String sectionName) {
        List<String> students = new ArrayList<>();

        String sql = """
                SELECT u.username
                FROM enrollments e
                JOIN students st ON e.student_id = st.user_id
                JOIN users u     ON st.user_id = u.id
                JOIN sections s  ON e.section_id = s.section_id
                WHERE s.course_id   = ?
                  AND s.section_name = ?
                  AND e.status       = 'Enrolled'
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseId);
            stmt.setString(2, sectionName);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    students.add(rs.getString("username"));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error fetching student enrollments for section: " + e.getMessage());
            e.printStackTrace();
        }

        return students;
    }
    // MAINTENANCE MODE
    public boolean isMaintenanceMode() {
        return SystemState.isMaintenanceMode();
    }

    public void toggleMaintenanceMode() {
        if (isAdmin()) SystemState.toggleMaintenanceMode();
    }

    // GPA
    public double calculateGPA(String studentUsername) {
        List<String> enrollments = getStudentEnrollments(studentUsername);
        double totalPoints = 0;
        int count = 0;

        for (String entry : enrollments) {
            String[] parts = entry.split(":");
            String courseId = parts[0];
            String section = parts[1];

            Map<String, Integer> scores = getStudentScores(courseId, section);
            Integer score = scores.get(studentUsername);
            if (score != null) {
                totalPoints += gradeToPoint(score);
                count++;
            }
        }

        return count > 0 ? totalPoints / count : 0.0;
    }

    private double gradeToPoint(int score) {
        if (score >= 90) return 10.0;
        if (score >= 80) return 9.0;
        if (score >= 70) return 8.0;
        if (score >= 60) return 7.0;
        if (score >= 50) return 6.0;
        return 0.0;
    }

    /**
     * Generates a section summary report as a text file and returns the file path.
     * Returns null if something goes wrong.
     */
    public String generateSectionSummaryReport(String instructorUsername,
                                               String courseId,
                                               String sectionName) {

        // Optional: ensure this instructor actually teaches this section
        String teachSql = """
                    SELECT COUNT(*)
                    FROM sections s
                    JOIN users u ON s.instructor_id = u.id
                    WHERE s.course_id = ?
                      AND s.section_name = ?
                      AND u.username = ?
                """;

        String courseTitle = null;

        String dataSql = """
                    SELECT u.username,
                           st.roll_no,
                           g.score,
                           g.finalized
                    FROM enrollments e
                    JOIN sections sec   ON e.section_id = sec.section_id
                    JOIN users u        ON e.student_id = u.id
                    LEFT JOIN students st ON st.user_id = u.id
                    LEFT JOIN grades g  ON g.course_id = sec.course_id
                                       AND g.section   = sec.section_name
                                       AND g.student_username = u.username
                    WHERE sec.course_id   = ?
                      AND sec.section_name = ?
                      AND e.status = 'Enrolled'
                    ORDER BY u.username
                """;

        String courseSql = "SELECT title FROM courses WHERE course_id = ?";

        try (Connection conn = DBConnection.getConnection()) {

            // 0) Verify instructor teaches this section (optional but nice)
            try (PreparedStatement ps = conn.prepareStatement(teachSql)) {
                ps.setString(1, courseId);
                ps.setString(2, sectionName);
                ps.setString(3, instructorUsername);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        System.out.println("❌ Instructor " + instructorUsername +
                                " does not teach " + courseId + " - " + sectionName);
                        return null;
                    }
                }
            }

            // 1) Get course title
            try (PreparedStatement ps = conn.prepareStatement(courseSql)) {
                ps.setString(1, courseId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        courseTitle = rs.getString("title");
                    }
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append("IIITD ERP - Section Summary Report\n");
            sb.append("Instructor : ").append(instructorUsername).append("\n");
            sb.append("Course ID  : ").append(courseId).append("\n");
            sb.append("Course     : ").append(courseTitle != null ? courseTitle : "N/A").append("\n");
            sb.append("Section    : ").append(sectionName).append("\n");
            sb.append("Generated  : ")
                    .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .append("\n\n");

            sb.append(String.format("%-20s %-12s %-8s %-8s %-10s%n",
                    "Username", "Roll No", "Score", "Grade", "Finalized"));
            sb.append("---------------------------------------------------------------------\n");

            try (PreparedStatement ps = conn.prepareStatement(dataSql)) {
                ps.setString(1, courseId);
                ps.setString(2, sectionName);

                try (ResultSet rs = ps.executeQuery()) {
                    boolean any = false;
                    while (rs.next()) {
                        any = true;
                        String username = rs.getString("username");
                        String roll = rs.getString("roll_no");
                        Integer score = (Integer) rs.getObject("score"); // can be null
                        boolean finalized = rs.getBoolean("finalized");

                        String grade;
                        if (score == null) {
                            grade = "-";
                        } else {
                            grade = calculateGrade(score);
                        }

                        sb.append(String.format("%-20s %-12s %-8s %-8s %-10s%n",
                                username,
                                roll != null ? roll : "-",
                                score != null ? score : "-",
                                grade,
                                finalized ? "Yes" : "No"));
                    }

                    if (!any) {
                        sb.append("(No enrolled students in this section)\n");
                    }
                }
            }

            // 3) Write to file under ./reports/
            File dir = new File("reports");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String safeCourse = courseId.replaceAll("[^A-Za-z0-9]", "_");
            String safeSection = sectionName.replaceAll("[^A-Za-z0-9]", "_");

            File outFile = new File(dir,
                    "section_summary_" + safeCourse + "_" + safeSection + "_" + timestamp + ".txt");

            try (FileWriter fw = new FileWriter(outFile)) {
                fw.write(sb.toString());
            }

            System.out.println("✅ Section summary report written to: " + outFile.getAbsolutePath());
            return outFile.getAbsolutePath();

        } catch (SQLException | IOException e) {
            System.out.println("❌ Error generating section summary report: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

// ASSESSMENT STRUCTURE (how many quizzes / assignments)
    public boolean setAssessmentStructure(String courseId,
                                          String sectionName,
                                          int quizCount,
                                          int assignmentCount) {

        if (quizCount < 0 || assignmentCount < 0) {
            System.out.println("❌ Counts cannot be negative.");
            return false;
        }

        String sql = """
        UPDATE sections
        SET quiz_count = ?,
            assignment_count = ?
        WHERE course_id = ?
          AND section_name = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, quizCount);
            stmt.setInt(2, assignmentCount);
            stmt.setString(3, courseId);
            stmt.setString(4, sectionName);

            int updated = stmt.executeUpdate();
            if (updated == 0) {
                System.out.println("⚠️ No section found for " + courseId + " - " + sectionName);
                return false;
            }
            return true;

        } catch (SQLException e) {
            System.out.println("❌ Error setting assessment structure: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public int[] getAssessmentStructure(String courseId, String sectionName) {
        String sql = """
        SELECT quiz_count, assignment_count
        FROM sections
        WHERE course_id = ?
          AND section_name = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseId);
            stmt.setString(2, sectionName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new int[] {
                            rs.getInt("quiz_count"),
                            rs.getInt("assignment_count")
                    };
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching assessment structure: " + e.getMessage());
            e.printStackTrace();
        }

        // default if nothing set
        return new int[] {0, 0};
    }

    // Instructor sets weights from UI
    public boolean setGradeSchemeByInstructor(String courseId,
                                              String sectionName,
                                              int quizW,
                                              int assignmentW,
                                              int midsemW,
                                              int endsemW) {

        // basic validation
        if (quizW < 0 || assignmentW < 0 || midsemW < 0 || endsemW < 0) {
            System.out.println("❌ Weights cannot be negative.");
            return false;
        }

        int total = quizW + assignmentW + midsemW + endsemW;
        if (total != 100) {
            System.out.println("❌ Total weight must be exactly 100. Currently: " + total);
            return false;
        }

        String sql = """
                UPDATE sections
                SET quiz_weight = ?,
                    assignment_weight = ?,
                    midsem_weight = ?,
                    endsem_weight = ?
                WHERE course_id = ?
                  AND section_name = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, quizW);
            stmt.setInt(2, assignmentW);
            stmt.setInt(3, midsemW);
            stmt.setInt(4, endsemW);
            stmt.setString(5, courseId);
            stmt.setString(6, sectionName);

            int updated = stmt.executeUpdate();
            if (updated == 0) {
                System.out.println("⚠️ No section found for " + courseId + " - " + sectionName);
                return false;
            }

            return true;

        } catch (Exception e) {
            System.out.println("❌ Error setting grade scheme: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public int[] getSectionGradeScheme(String courseId, String sectionName) {
        String sql = """
        SELECT quiz_weight, assignment_weight, midsem_weight, endsem_weight
        FROM sections
        WHERE course_id = ?
          AND section_name = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseId);
            stmt.setString(2, sectionName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new int[] {
                            rs.getInt("quiz_weight"),
                            rs.getInt("assignment_weight"),
                            rs.getInt("midsem_weight"),
                            rs.getInt("endsem_weight")
                    };
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error fetching grade scheme: " + e.getMessage());
            e.printStackTrace();
        }

        // default if not found
        return new int[] {0, 0, 0, 0};
    }

    private double computeFinalScoreFromComponents(
            double quizObt, double quizMax,
            double assignmentObt, double assignmentMax,
            double midsemObt, double midsemMax,
            double endsemObt, double endsemMax,
            int quizWeight, int assignmentWeight,
            int midsemWeight, int endsemWeight
    ) {
        double quizPart = 0.0;
        if (quizMax > 0) {
            quizPart = (quizObt / quizMax) * quizWeight;
        }

        double assignmentPart = 0.0;
        if (assignmentMax > 0) {
            assignmentPart = (assignmentObt / assignmentMax) * assignmentWeight;
        }

        double midsemPart = 0.0;
        if (midsemMax > 0) {
            midsemPart = (midsemObt / midsemMax) * midsemWeight;
        }

        double endsemPart = 0.0;
        if (endsemMax > 0) {
            endsemPart = (endsemObt / endsemMax) * endsemWeight;
        }

        double total = quizPart + assignmentPart + midsemPart + endsemPart;

        // clamp to [0, 100]
        if (total < 0) total = 0;
        if (total > 100) total = 100;

        return total;
    }
    // Get stored component totals for a specific student in a section
    public double[] getStudentComponentTotals(String courseId,
                                              String sectionName,
                                              String studentUsername) {
        String sql = """
        SELECT quiz_obtained,
               quiz_max,
               assignment_obtained,
               assignment_max,
               midsem_obtained,
               midsem_max,
               endsem_obtained,
               endsem_max
        FROM grades
        WHERE course_id = ?
          AND section   = ?
          AND student_username = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseId);
            stmt.setString(2, sectionName);
            stmt.setString(3, studentUsername);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new double[] {
                            rs.getDouble("quiz_obtained"),
                            rs.getDouble("quiz_max"),
                            rs.getDouble("assignment_obtained"),
                            rs.getDouble("assignment_max"),
                            rs.getDouble("midsem_obtained"),
                            rs.getDouble("midsem_max"),
                            rs.getDouble("endsem_obtained"),
                            rs.getDouble("endsem_max")
                    };
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching student component totals: " + e.getMessage());
            e.printStackTrace();
        }

        // default if no row yet
        return new double[] {0,0,0,0,0,0,0,0};
    }

    public double[] getStudentComponentScores(String courseId,
                                              String section,
                                              String studentUsername) {

        double[] data = new double[8];

        String sql = """
        SELECT quiz_obtained, quiz_max,
               assignment_obtained, assignment_max,
               midsem_obtained, midsem_max,
               endsem_obtained, endsem_max
        FROM grades
        WHERE course_id = ?
          AND section = ?
          AND student_username = ?
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseId);
            stmt.setString(2, section);
            stmt.setString(3, studentUsername);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    data[0] = rs.getDouble("quiz_obtained");
                    data[1] = rs.getDouble("quiz_max");
                    data[2] = rs.getDouble("assignment_obtained");
                    data[3] = rs.getDouble("assignment_max");
                    data[4] = rs.getDouble("midsem_obtained");
                    data[5] = rs.getDouble("midsem_max");
                    data[6] = rs.getDouble("endsem_obtained");
                    data[7] = rs.getDouble("endsem_max");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    public boolean deleteUserCompletely(String username) {

        String findUserSql = "SELECT id, role FROM users WHERE username = ?";
        String deleteAuthSql = "DELETE FROM users WHERE username = ?";

        String deleteErpUserSql = "DELETE FROM erp_db.users WHERE username = ?";
        String deleteStudentSql = "DELETE FROM students WHERE user_id = ?";
        String deleteInstructorSql = "DELETE FROM instructors WHERE user_id = ?";

        Connection authConn = null;
        Connection erpConn = null;

        try {
            authConn = AuthDBConnection.getConnection();
            erpConn = DBConnection.getConnection();

            authConn.setAutoCommit(false);
            erpConn.setAutoCommit(false);

            int userId = -1;
            String role = null;

            // Step 1: get ID and role from auth DB
            try (PreparedStatement stmt = authConn.prepareStatement(findUserSql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    userId = rs.getInt("id");
                    role = rs.getString("role");
                } else {
                    return false;
                }
            }

            // Step 2: delete role-specific ERP data
            if ("STUDENT".equalsIgnoreCase(role)) {
                try (PreparedStatement ps = erpConn.prepareStatement(deleteStudentSql)) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }
            } else if ("INSTRUCTOR".equalsIgnoreCase(role)) {
                try (PreparedStatement ps = erpConn.prepareStatement(deleteInstructorSql)) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }
            }

            // Step 3: delete from ERP users table
            try (PreparedStatement ps = erpConn.prepareStatement(deleteErpUserSql)) {
                ps.setString(1, username);
                ps.executeUpdate();
            }

            // Step 4: delete from AUTH users table
            try (PreparedStatement ps = authConn.prepareStatement(deleteAuthSql)) {
                ps.setString(1, username);
                ps.executeUpdate();
            }

            authConn.commit();
            erpConn.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            try { if (authConn != null) authConn.rollback(); } catch (Exception ignored) {}
            try { if (erpConn != null) erpConn.rollback(); } catch (Exception ignored) {}
            return false;

        } finally {
            try { if (authConn != null) authConn.close(); } catch (Exception ignored) {}
            try { if (erpConn != null) erpConn.close(); } catch (Exception ignored) {}
        }
    }

    // USERS LIST (for delete popup)
    public java.util.List<String> getAllErpUsers() {
        java.util.List<String> users = new java.util.ArrayList<>();

        String sql = "SELECT id, username, role FROM users ORDER BY id";

        try (java.sql.Connection conn = com.Arijit_Aditya.erp.db.DBConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String role = rs.getString("role");
                users.add(id + " - " + username + " (" + role + ")");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }
}














