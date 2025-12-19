package com.Arijit_Aditya.erp.ui.instructor;

import com.Arijit_Aditya.erp.auth.User;
import com.Arijit_Aditya.erp.models.Course;
import com.Arijit_Aditya.erp.services.AdminService;

import java.util.*;

public class InstructorUI {

    private final Scanner sc = new Scanner(System.in);
    private AdminService adminService;

    public void showMenu(User user) {
        this.adminService = new AdminService(user);

        while (true) {
            System.out.println("\n=== Instructor Dashboard ===");
            System.out.println("Welcome, " + user.getUsername());
            System.out.println("1. View My Sections");
            System.out.println("2. Enter Scores");
            System.out.println("3. View / Update Scores");
            System.out.println("4. Compute Final Grades");
            System.out.println("5. Logout");
            System.out.print("Enter your choice: ");

            int choice = sc.nextInt();
            sc.nextLine(); // consume newline

            switch (choice) {
                case 1 -> viewMySections(user);
                case 2 -> enterScores(user);
                case 3 -> viewAndUpdateScores(user);
                case 4 -> computeFinalGrades(user);
                case 5 -> {
                    System.out.println("Logging out...");
                    return;
                }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    //View My Sections
    private void viewMySections(User user) {
        System.out.println("\n=== My Courses and Sections ===");
        List<Course> allCourses = adminService.getAllCourses();
        boolean hasSection = false;

        for (Course course : allCourses) {
            List<String> mySections = new ArrayList<>();
            for (String section : course.getSections()) {
                String instructor = course.getInstructorForSection(section);
                if (instructor != null && instructor.equalsIgnoreCase(user.getUsername())) {
                    mySections.add(section);
                }
            }
            if (!mySections.isEmpty()) {
                System.out.println("Course ID: " + course.getCourseId() + ", Name: " + course.getCourseName());
                for (String sec : mySections) {
                    boolean finalized = adminService.areGradesComputed(course.getCourseId(), sec);
                    System.out.println(" - Section: " + sec + (finalized ? " (Finalized)" : ""));
                }
                System.out.println("---------------------------");
                hasSection = true;
            }
        }

        if (!hasSection) System.out.println("No sections assigned to you yet.");
    }

    //Enter Scores
    private void enterScores(User user) {
        CourseSection selected = selectMySection(user);
        if (selected == null) return;

        String courseId = selected.course.getCourseId();
        String section = selected.section;

        // 1) Ensure grade scheme is set for this section
        int[] weights = adminService.getSectionGradeScheme(courseId, section);
        int totalW = weights[0] + weights[1] + weights[2] + weights[3];

        if (totalW != 100) {
            System.out.println("\nNo valid grade scheme set for this section yet.");
            System.out.println("Please enter component weightages (they must sum to 100):");

            System.out.print("Quiz weight: ");
            int quizW = sc.nextInt();
            System.out.print("Assignment weight: ");
            int assignmentW = sc.nextInt();
            System.out.print("Midsem weight: ");
            int midsemW = sc.nextInt();
            System.out.print("Endsem weight: ");
            int endsemW = sc.nextInt();
            sc.nextLine(); // consume newline

            boolean ok = adminService.setGradeSchemeByInstructor(
                    courseId, section, quizW, assignmentW, midsemW, endsemW
            );
            if (!ok) {
                System.out.println("❌ Could not set grade scheme. Aborting score entry.");
                return;
            }
            weights = adminService.getSectionGradeScheme(courseId, section);
        }

        System.out.print("Enter student username: ");
        String student = sc.nextLine().trim();
        String key = courseId + ":" + section;

        List<String> enrollments = adminService.getStudentEnrollments(student);
        if (!enrollments.contains(key)) {
            System.out.println("⚠️ Student is not registered in this section!");
            return;
        }

        // 2) Collect quiz marks (any number)
        System.out.print("Enter number of quizzes (0 if none): ");
        int numQuizzes = sc.nextInt();
        double quizObtained = 0.0;
        double quizMax = 0.0;
        for (int i = 1; i <= numQuizzes; i++) {
            System.out.print("Quiz " + i + " obtained marks: ");
            double obt = sc.nextDouble();
            System.out.print("Quiz " + i + " maximum marks: ");
            double max = sc.nextDouble();
            quizObtained += obt;
            quizMax += max;
        }

        // 3) Collect assignment marks (any number)
        System.out.print("Enter number of assignments (0 if none): ");
        int numAssignments = sc.nextInt();
        double assignmentObtained = 0.0;
        double assignmentMax = 0.0;
        for (int i = 1; i <= numAssignments; i++) {
            System.out.print("Assignment " + i + " obtained marks: ");
            double obt = sc.nextDouble();
            System.out.print("Assignment " + i + " maximum marks: ");
            double max = sc.nextDouble();
            assignmentObtained += obt;
            assignmentMax += max;
        }

        // 4) Midsem
        System.out.print("Enter midsem obtained marks (0 if not yet taken): ");
        double midsemObtained = sc.nextDouble();
        System.out.print("Enter midsem maximum marks (0 if not applicable): ");
        double midsemMax = sc.nextDouble();

        // 5) Endsem
        System.out.print("Enter endsem obtained marks (0 if not yet taken): ");
        double endsemObtained = sc.nextDouble();
        System.out.print("Enter endsem maximum marks (0 if not applicable): ");
        double endsemMax = sc.nextDouble();

        sc.nextLine(); // consume newline

        boolean added = adminService.addStudentScore(
                courseId,
                section,
                student,
                quizObtained,
                quizMax,
                assignmentObtained,
                assignmentMax,
                midsemObtained,
                midsemMax,
                endsemObtained,
                endsemMax
        );

        if (added) {
            // fetch final score from DB to show to user
            Map<String, Integer> scores = adminService.getStudentScores(courseId, section);
            Integer finalScore = scores.get(student);
            System.out.println("✅ Scores saved for " + student +
                    " in " + key +
                    (finalScore != null ? (" | Final Score: " + finalScore) : ""));
        } else {
            System.out.println("⚠️ Cannot enter scores (maybe grades already finalized).");
        }
    }

    //View / Update Scores
    private void viewAndUpdateScores(User user) {
        CourseSection selected = selectMySection(user);
        if (selected == null) return;

        String courseId = selected.course.getCourseId();
        String section = selected.section;

        Map<String, Integer> scores = adminService.getStudentScores(courseId, section);
        if (scores.isEmpty()) {
            System.out.println("No scores entered yet for this section.");
            return;
        }

        System.out.println("\n=== Scores for " + selected.course.getCourseName() +
                " Section " + section + " ===");
        List<String> students = new ArrayList<>(scores.keySet());
        for (int i = 0; i < students.size(); i++) {
            String stu = students.get(i);
            System.out.println((i + 1) + ". " + stu + " -> Score: " + scores.get(stu));
        }

        if (adminService.areGradesComputed(courseId, section)) {
            System.out.println("\n⚠️ Final grades computed. Cannot update scores.");
            return;
        }

        System.out.print("Enter student number to update scores (0 to cancel): ");
        int choice = sc.nextInt();
        sc.nextLine();
        if (choice == 0) return;
        if (choice < 1 || choice > students.size()) {
            System.out.println("Invalid choice!");
            return;
        }

        String student = students.get(choice - 1);
        System.out.println("\nUpdating scores for: " + student);

        // Re-enter component marks for this student
        System.out.print("Enter number of quizzes (0 if none): ");
        int numQuizzes = sc.nextInt();
        double quizObtained = 0.0;
        double quizMax = 0.0;
        for (int i = 1; i <= numQuizzes; i++) {
            System.out.print("Quiz " + i + " obtained marks: ");
            double obt = sc.nextDouble();
            System.out.print("Quiz " + i + " maximum marks: ");
            double max = sc.nextDouble();
            quizObtained += obt;
            quizMax += max;
        }

        System.out.print("Enter number of assignments (0 if none): ");
        int numAssignments = sc.nextInt();
        double assignmentObtained = 0.0;
        double assignmentMax = 0.0;
        for (int i = 1; i <= numAssignments; i++) {
            System.out.print("Assignment " + i + " obtained marks: ");
            double obt = sc.nextDouble();
            System.out.print("Assignment " + i + " maximum marks: ");
            double max = sc.nextDouble();
            assignmentObtained += obt;
            assignmentMax += max;
        }

        System.out.print("Enter midsem obtained marks (0 if not yet taken): ");
        double midsemObtained = sc.nextDouble();
        System.out.print("Enter midsem maximum marks (0 if not applicable): ");
        double midsemMax = sc.nextDouble();

        System.out.print("Enter endsem obtained marks (0 if not yet taken): ");
        double endsemObtained = sc.nextDouble();
        System.out.print("Enter endsem maximum marks (0 if not applicable): ");
        double endsemMax = sc.nextDouble();

        sc.nextLine(); // consume newline

        boolean updated = adminService.addStudentScore(
                courseId,
                section,
                student,
                quizObtained,
                quizMax,
                assignmentObtained,
                assignmentMax,
                midsemObtained,
                midsemMax,
                endsemObtained,
                endsemMax
        );

        if (updated) {
            Map<String, Integer> updatedScores = adminService.getStudentScores(courseId, section);
            Integer finalScore = updatedScores.get(student);
            System.out.println("✅ Updated scores for " + student +
                    (finalScore != null ? (" | New Final Score: " + finalScore) : ""));
        } else {
            System.out.println("⚠️ Cannot update scores (maybe grades finalized).");
        }
    }

    //Compute Final Grades
    private void computeFinalGrades(User user) {
        CourseSection selected = selectMySection(user);
        if (selected == null) return;

        Map<String, Integer> scores = adminService.getStudentScores(selected.course.getCourseId(), selected.section);
        if (scores.isEmpty()) {
            System.out.println("No scores entered for this section yet.");
            return;
        }

        if (adminService.areGradesComputed(selected.course.getCourseId(), selected.section)) {
            System.out.println("⚠️ Final grades already computed!");
            return;
        }

        System.out.println("\n=== Final Grades for " + selected.course.getCourseName() + " Section " + selected.section + " ===");
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            String student = entry.getKey();
            int score = entry.getValue();
            String grade = adminService.calculateGrade(score);
            System.out.println(student + " -> Score: " + score + ", Grade: " + grade);
        }

        // Use new method computeFinalGrades() instead of markGradesAsComputed()
        adminService.computeFinalGrades(selected.course.getCourseId(), selected.section);
        System.out.println("✅ Final grades computed successfully.");
    }

    //Helper Methods
    private class CourseSection {
        Course course;
        String section;
        CourseSection(Course c, String s) { course = c; section = s; }
    }

    private List<CourseSection> getMySections(User user) {
        List<CourseSection> mySections = new ArrayList<>();
        List<Course> allCourses = adminService.getAllCourses();
        for (Course course : allCourses) {
            for (String section : course.getSections()) {
                String instructor = course.getInstructorForSection(section);
                if (instructor != null && instructor.equalsIgnoreCase(user.getUsername())) {
                    mySections.add(new CourseSection(course, section));
                }
            }
        }
        return mySections;
    }

    private CourseSection selectMySection(User user) {
        List<CourseSection> sections = getMySections(user);
        if (sections.isEmpty()) {
            System.out.println("You have no assigned sections.");
            return null;
        }

        System.out.println("\nSelect a section:");
        for (int i = 0; i < sections.size(); i++) {
            CourseSection cs = sections.get(i);
            boolean finalized = adminService.areGradesComputed(cs.course.getCourseId(), cs.section);
            System.out.println((i + 1) + ". " + cs.course.getCourseName() + " | Section: " + cs.section + (finalized ? " (Finalized)" : ""));
        }

        System.out.print("Enter choice: ");
        int choice = sc.nextInt() - 1;
        sc.nextLine();
        if (choice < 0 || choice >= sections.size()) {
            System.out.println("Invalid choice!");
            return null;
        }
        return sections.get(choice);
    }
}

















