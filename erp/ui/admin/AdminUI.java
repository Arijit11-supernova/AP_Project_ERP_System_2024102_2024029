package com.Arijit_Aditya.erp.ui.admin;

import com.Arijit_Aditya.erp.auth.User;
import com.Arijit_Aditya.erp.models.Course;
import com.Arijit_Aditya.erp.services.AdminService;

import java.util.List;
import java.util.Scanner;

public class AdminUI {
    private final Scanner sc = new Scanner(System.in);
    private AdminService adminService;

    public void showMenu(User adminUser) {
        // Initialize AdminService with the logged-in admin
        adminService = new AdminService(adminUser);

        while (true) {
            System.out.println("\n=== Admin Dashboard ===");
            System.out.println("Welcome, " + adminUser.getUsername());
            System.out.println("1. Add User");
            System.out.println("2. Create Course");
            System.out.println("3. Create Section");
            System.out.println("4. Assign Instructor");
            System.out.println("5. Toggle Maintenance Mode");
            System.out.println("6. View Courses and Finalized Grades Status");
            System.out.println("7. Logout");
            System.out.print("Enter your choice: ");

            int choice = sc.nextInt();
            sc.nextLine(); // consume newline

            switch (choice) {
                case 1 -> {
                    // Add User
                    System.out.print("Enter username: ");
                    String username = sc.nextLine().trim();

                    System.out.print("Enter password: ");
                    String password = sc.nextLine().trim();

                    System.out.print("Enter role: ");
                    String role = sc.nextLine().trim();

                    boolean success = adminService.addUser(username, password, role);

                    if (success) System.out.println("User added successfully.");
                    else System.out.println("Failed to add user.");
                }
                case 2 -> {
                    // Create Course
                    System.out.print("Enter course ID: ");
                    String courseId = sc.nextLine().trim();
                    System.out.print("Enter course name: ");
                    String courseName = sc.nextLine().trim();
                    System.out.print("Enter course credits: ");
                    int credits = sc.nextInt();
                    sc.nextLine();
                    adminService.createCourse(courseId, courseName, credits);
                    System.out.println("Course created successfully.");
                }
                case 3 -> {
                    System.out.print("Enter course ID to add section: ");
                    String courseId = sc.nextLine().trim();
                    System.out.print("Enter section name: ");
                    String sectionName = sc.nextLine().trim();

                    // Default values so code compiles and works
                    String instructorId = null;     // no instructor assigned yet
                    String dayTime = "";            // empty schedule
                    String room = "";               // no room assigned
                    int capacity = 0;               // unlimited or unset
                    String semester = "Fall";       // default semester
                    int year = 2025;                // default year

                    adminService.createSection(courseId, sectionName, instructorId, dayTime, room, capacity, semester, year);
                    System.out.println("Section added successfully.");
                }
                case 4 -> {
                    // Assign Instructor
                    System.out.print("Enter course ID to assign instructor: ");
                    String courseId = sc.nextLine().trim();
                    System.out.print("Enter section name: ");
                    String sectionName = sc.nextLine().trim();
                    System.out.print("Enter instructor username: ");
                    String instructor = sc.nextLine().trim();

                    boolean assigned = adminService.assignInstructor(courseId, sectionName, instructor);
                    if (assigned) System.out.println("Instructor assigned successfully.");
                    else System.out.println("Failed to assign instructor.");
                }
                case 5 -> {
                    // Toggle Maintenance Mode
                    adminService.toggleMaintenanceMode();
                    System.out.println("Maintenance mode toggled. Current state: " +
                            (adminService.isMaintenanceMode() ? "ON" : "OFF"));
                }
                case 6 -> viewCoursesWithGradesStatus();
                case 7 -> {
                    System.out.println("Logging out...");
                    return;
                }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    //View Courses with Finalized Grades Status
    private void viewCoursesWithGradesStatus() {
        List<Course> allCourses = adminService.getAllCourses();

        if (allCourses.isEmpty()) {
            System.out.println("No courses available.");
            return;
        }

        System.out.println("\n=== Courses and Sections Status ===");
        for (Course course : allCourses) {
            System.out.println("Course ID: " + course.getCourseId() + " | Name: " + course.getCourseName());

            List<String> sections = course.getSections();
            if (sections.isEmpty()) {
                System.out.println("   No sections created yet.");
            } else {
                for (String section : sections) {
                    String instructor = course.getInstructorForSection(section);
                    boolean finalized = adminService.areGradesComputed(course.getCourseId(), section);

                    System.out.println("   Section: " + section +
                            " | Instructor: " + (instructor != null ? instructor : "Unassigned") +
                            " | Finalized Grades: " + (finalized ? "Yes" : "No"));
                }
            }
            System.out.println("---------------------------");
        }
    }
}




