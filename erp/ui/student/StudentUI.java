package com.Arijit_Aditya.erp.ui.student;

import com.Arijit_Aditya.erp.auth.User;
import com.Arijit_Aditya.erp.models.Course;
import com.Arijit_Aditya.erp.services.AdminService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.Arijit_Aditya.erp.db.DBConnection;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class StudentUI {

    private final Scanner sc = new Scanner(System.in);
    private AdminService adminService;

    public void showMenu(User user) {
        this.adminService = new AdminService(user);

        while (true) {
            System.out.println("\n=== Student Dashboard ===");
            System.out.println("Welcome, " + user.getUsername());
            System.out.println("1. View Course Catalog");
            System.out.println("2. Register for a Section");
            System.out.println("3. Drop a Section");
            System.out.println("4. View Grades");
            System.out.println("5. Download Transcript");
            System.out.println("6. Logout");
            System.out.print("Enter your choice: ");

            int choice = sc.nextInt();
            sc.nextLine(); // consume newline

            switch (choice) {
                case 1 -> viewCourseCatalog();
                case 2 -> registerForSection(user);
                case 3 -> dropSection(user);
                case 4 -> viewGrades(user);
                case 5 -> downloadTranscript(user);
                case 6 -> {
                    System.out.println("Logging out...\n");
                    return;
                }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    //View Course Catalog
    private void viewCourseCatalog() {
        System.out.println("\n=== Course Catalog ===");

        String sql = """
        SELECT c.course_id,
               c.title,
               c.credits,
               s.section_name
        FROM erp_db.courses c
        LEFT JOIN erp_db.sections s
            ON c.course_id = s.course_id
        ORDER BY c.course_id, s.section_name
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            String lastCourseId = null;
            String lastCourseName = null;
            int lastCredits = 0;
            boolean hadSectionForCourse = false;

            while (rs.next()) {
                String courseId = rs.getString("course_id");
                String courseName = rs.getString("title");
                int credits = rs.getInt("credits");
                String sectionName = rs.getString("section_name"); // may be NULL

                // When we encounter a new course, print header for previous course
                if (!courseId.equals(lastCourseId)) {
                    // Finish previous course: if it had no sections, print message
                    if (lastCourseId != null && !hadSectionForCourse) {
                        System.out.println("   No sections created yet.");
                        System.out.println();
                    }

                    // Start new course block
                    System.out.println("Course ID: " + courseId +
                            " | Name: " + courseName +
                            " | Credits: " + credits);
                    lastCourseId = courseId;
                    lastCourseName = courseName;
                    lastCredits = credits;
                    hadSectionForCourse = false;
                }

                // Print section line if there is a section
                if (sectionName != null && !sectionName.isEmpty()) {
                    hadSectionForCourse = true;
                    System.out.println("   Section: " + sectionName);
                }
            }

            // Handle the last course in the result set
            if (lastCourseId != null && !hadSectionForCourse) {
                System.out.println("   No sections created yet.");
            }

        } catch (SQLException e) {
            System.out.println("Error fetching course catalog: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("---------------------------");
    }

    //Register Section
    private void registerForSection(User user) {
        List<Course> courseList = adminService.getAllCourses();
        if (courseList.isEmpty()) {
            System.out.println("No courses available to register.");
            return;
        }

        // Populate sections and instructors for each course
        for (Course c : courseList) {
            List<String> sections = adminService.getSectionsForCourse(c.getCourseId());
            c.setSections(sections);
            for (String section : sections) {
                String instructor = adminService.getInstructorForSection(c.getCourseId(), section);
                c.assignInstructorToSection(section, instructor);
            }
        }

        courseList.sort(Comparator.comparing(Course::getCourseId));

        System.out.println("\nSelect a course to register:");
        for (int i = 0; i < courseList.size(); i++) {
            Course c = courseList.get(i);
            System.out.println((i + 1) + ". " + c.getCourseName() + " (" + c.getCourseId() + ")");
        }

        System.out.print("Enter choice: ");
        int courseChoice = sc.nextInt() - 1;
        sc.nextLine();
        if (courseChoice < 0 || courseChoice >= courseList.size()) {
            System.out.println("Invalid choice!");
            return;
        }

        Course selected = courseList.get(courseChoice);
        if (selected.getSections().isEmpty()) {
            System.out.println("No sections available for this course.");
            return;
        }

        System.out.println("Select a section:");
        for (int i = 0; i < selected.getSections().size(); i++) {
            System.out.println((i + 1) + ". " + selected.getSections().get(i));
        }

        System.out.print("Enter choice: ");
        int sectionChoice = sc.nextInt() - 1;
        sc.nextLine();
        if (sectionChoice < 0 || sectionChoice >= selected.getSections().size()) {
            System.out.println("Invalid choice!");
            return;
        }

        String section = selected.getSections().get(sectionChoice);
        boolean registered = adminService.registerStudent(user.getUsername(), selected.getCourseId(), section);
        if (registered) {
            System.out.println("✅ Registered for " + selected.getCourseName() + " (" + section + ")");
        } else {
            System.out.println("⚠️ Already registered for this section.");
        }
    }


    //Drop Section
    private void dropSection(User user) {
        List<String> enrolled = adminService.getStudentEnrollments(user.getUsername());
        if (enrolled.isEmpty()) {
            System.out.println("You are not registered in any section.");
            return;
        }

        System.out.println("\nYour enrolled sections:");
        for (int i = 0; i < enrolled.size(); i++) {
            System.out.println((i + 1) + ". " + enrolled.get(i));
        }

        System.out.print("Enter the number of the section to drop: ");
        int choice = sc.nextInt() - 1;
        sc.nextLine();
        if (choice < 0 || choice >= enrolled.size()) {
            System.out.println("Invalid choice!");
            return;
        }

        String[] parts = enrolled.get(choice).split(":");
        String courseId = parts[0], section = parts[1];

        boolean dropped = adminService.dropSection(user.getUsername(), courseId, section);
        if (dropped) {
            System.out.println("✅ Dropped " + courseId + " (" + section + ")");
        } else {
            System.out.println("⚠️ Could not drop this section (maybe scores already exist).");
        }
    }

    //View Grades
    private void viewGrades(User user) {
        List<String> enrolled = adminService.getStudentEnrollments(user.getUsername());
        if (enrolled.isEmpty()) {
            System.out.println("No courses registered yet.");
            return;
        }

        System.out.println("\n=== Your Grades ===");
        System.out.printf("%-10s | %-7s | %-5s | %-5s | %-8s%n", "Course ID", "Section", "Score", "Grade", "Finalized");
        System.out.println("-----------+---------+-------+------+--------");

        boolean allFinalized = true;
        for (String cs : enrolled) {
            String[] parts = cs.split(":");
            String courseId = parts[0], section = parts[1];

            boolean finalized = adminService.areGradesComputed(courseId, section);
            Map<String, Integer> scores = adminService.getStudentScores(courseId, section);
            Integer score = finalized ? scores.get(user.getUsername()) : null;
            String grade = (finalized && score != null) ? adminService.calculateGrade(score) : "Pending";

            if (!finalized) allFinalized = false;

            System.out.printf("%-10s | %-7s | %-5s | %-5s | %-8s%n",
                    courseId, section, (score != null ? score : "-"), grade, finalized ? "Yes" : "No");
        }

        if (allFinalized && !enrolled.isEmpty()) {
            double gpa = adminService.calculateGPA(user.getUsername());
            System.out.printf("%nCurrent GPA: %.2f%n", gpa);
        } else {
            System.out.println("\nCurrent GPA: N/A (grades not finalized yet)");
        }
    }

    //Download Transcript
    private void downloadTranscript(User user) {
        List<String> enrolled = adminService.getStudentEnrollments(user.getUsername());
        if (enrolled.isEmpty()) {
            System.out.println("No transcript available.");
            return;
        }

        try (FileWriter writer = new FileWriter(user.getUsername() + "_transcript.txt")) {
            writer.write("Transcript for " + user.getUsername() + "\n\n");
            writer.write(String.format("%-10s | %-7s | %-5s | %-5s | %-8s%n", "Course ID", "Section", "Score", "Grade", "Finalized"));
            writer.write("-----------+---------+-------+------+--------\n");

            boolean allFinalized = true;
            for (String cs : enrolled) {
                String[] parts = cs.split(":");
                String courseId = parts[0], section = parts[1];

                boolean finalized = adminService.areGradesComputed(courseId, section);
                Map<String, Integer> scores = adminService.getStudentScores(courseId, section);
                Integer score = finalized ? scores.get(user.getUsername()) : null;
                String grade = (finalized && score != null) ? adminService.calculateGrade(score) : "Pending";

                if (!finalized) allFinalized = false;

                writer.write(String.format("%-10s | %-7s | %-5s | %-5s | %-8s%n",
                        courseId, section, (score != null ? score : "-"), grade, finalized ? "Yes" : "No"));
            }

            if (allFinalized && !enrolled.isEmpty()) {
                double gpa = adminService.calculateGPA(user.getUsername());
                writer.write(String.format("%nGPA: %.2f%n", gpa));
            } else {
                writer.write("\nGPA: N/A (grades not finalized yet)\n");
            }

            writer.write("\nGenerated by ERP System\n");
            System.out.println("Transcript saved as " + user.getUsername() + "_transcript.txt");

        } catch (IOException e) {
            System.out.println("Error writing transcript: " + e.getMessage());
        }
    }
}










