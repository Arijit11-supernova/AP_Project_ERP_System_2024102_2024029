package com.Arijit_Aditya.erp.ui.swing;

import com.Arijit_Aditya.erp.auth.User;
import com.Arijit_Aditya.erp.services.AdminService;

import javax.swing.*;
import java.awt.*;
import java.awt.Desktop;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.*;

public class InstructorReportsGUI extends JFrame {
    private final User loggedInUser;
    private final AdminService adminService;

    public InstructorReportsGUI(User user, AdminService adminService) {
        this.loggedInUser = user;
        this.adminService = adminService;

        setTitle("Instructor Reports");
        setSize(650, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        setVisible(true);
    }

    private void initUI() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel title = new JLabel("Instructor Reports", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        panel.add(title, BorderLayout.NORTH);

        // Center Section
        JPanel center = new JPanel(new GridLayout(4, 1, 20, 20));

        JLabel label = new JLabel("Select Report Type:", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        String[] reports = {
                "Section Summary Report",
                "Score Sheet Report",
                "Final Grade Distribution",
                "Student Performance Report"
        };

        JComboBox<String> reportDropdown = new JComboBox<>(reports);
        reportDropdown.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        JButton generateBtn = new JButton("Generate Report");
        generateBtn.setFont(new Font("Segoe UI", Font.BOLD, 20));

        generateBtn.addActionListener(e ->
                generateReport((String) reportDropdown.getSelectedItem()));

        center.add(label);
        center.add(reportDropdown);
        center.add(generateBtn);

        panel.add(center, BorderLayout.CENTER);

        // Back button
        JButton backBtn = new JButton("Back");
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        backBtn.addActionListener(e -> dispose());

        JPanel bottom = new JPanel();
        bottom.add(backBtn);

        panel.add(bottom, BorderLayout.SOUTH);

        add(panel);
    }

    //REPORT DISPATCHER

    private void generateReport(String reportType) {
        try {
            Path reportsDir = Path.of("reports");
            Files.createDirectories(reportsDir);

            String safeName = reportType.replaceAll("\\s+", "_");
            String filename = safeName + "_" + loggedInUser.getUsername().replaceAll("\\s+", "_") + ".csv";
            Path filePath = reportsDir.resolve(filename);

            switch (reportType) {
                case "Section Summary Report" -> generateSectionSummaryReport(filePath);
                case "Score Sheet Report" -> generateScoreSheetReport(filePath);
                case "Final Grade Distribution" -> generateFinalGradeDistributionReport(filePath);
                case "Student Performance Report" -> generateStudentPerformanceReport(filePath);
                default -> throw new IllegalArgumentException("Unknown report type: " + reportType);
            }

            JOptionPane.showMessageDialog(
                    this,
                    reportType + " generated successfully!\nSaved at: " + filePath.toAbsolutePath(),
                    "Report Ready",
                    JOptionPane.INFORMATION_MESSAGE
            );

            // Try to open automatically
            try {
                Desktop.getDesktop().open(filePath.toFile());
            } catch (IOException ignored) {
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Error generating report: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    //HELPER: COMMON HEADER

    private void writeStandardHeader(PrintWriter out, String reportType) {
        out.println("Instructor Report");
        out.println("Instructor," + loggedInUser.getUsername());
        out.println("Report Type," + reportType);
        out.println("Generated At," + LocalDateTime.now());
        out.println(); // blank
    }

    //1) SECTION SUMMARY (now with components)

    private void generateSectionSummaryReport(Path filePath) throws IOException {
        Map<String, List<String>> courseSections =
                adminService.getInstructorCourseSections(loggedInUser.getUsername());

        try (PrintWriter out = new PrintWriter(new FileWriter(filePath.toFile()))) {
            writeStandardHeader(out, "Section Summary Report");

            // New header with component averages
            out.println("Course ID,Section,Enrolled Students,Students with Score," +
                    "Avg Quiz %,Avg Assignment %,Avg Midsem %,Avg Endsem %,Average Score,Grades Finalized");

            for (Map.Entry<String, List<String>> e : courseSections.entrySet()) {
                String courseId = e.getKey();
                for (String section : e.getValue()) {
                    List<String> students =
                            adminService.getStudentEnrollmentsForSection(courseId, section);
                    Map<String, Integer> scores =
                            adminService.getStudentScores(courseId, section);

                    int enrolled = students.size();
                    int scoredCount = 0;
                    int sum = 0;

                    double sumQuiz = 0.0;
                    double sumAssign = 0.0;
                    double sumMid = 0.0;
                    double sumEnd = 0.0;

                    for (String s : students) {
                        Integer sc = scores.get(s);
                        if (sc != null) {
                            scoredCount++;
                            sum += sc;
                        }

                        // component percentages per student
                        double[] c = adminService.getStudentComponentScores(courseId, section, s);
                        double qObt = c[0], qMax = c[1];
                        double aObt = c[2], aMax = c[3];
                        double mObt = c[4], mMax = c[5];
                        double eObt = c[6], eMax = c[7];

                        if (qMax > 0) sumQuiz += (qObt / qMax) * 100.0;
                        if (aMax > 0) sumAssign += (aObt / aMax) * 100.0;
                        if (mMax > 0) sumMid += (mObt / mMax) * 100.0;
                        if (eMax > 0) sumEnd += (eObt / eMax) * 100.0;
                    }

                    double finalAvg = scoredCount > 0 ? (sum * 1.0 / scoredCount) : 0.0;
                    int denom = Math.max(enrolled, 1); // avoid /0

                    double avgQuiz = denom > 0 ? (sumQuiz / denom) : 0.0;
                    double avgAssign = denom > 0 ? (sumAssign / denom) : 0.0;
                    double avgMid = denom > 0 ? (sumMid / denom) : 0.0;
                    double avgEnd = denom > 0 ? (sumEnd / denom) : 0.0;

                    boolean finalized = adminService.areGradesComputed(courseId, section);

                    out.printf("%s,%s,%d,%d,%.2f,%.2f,%.2f,%.2f,%.2f,%s%n",
                            courseId, section, enrolled, scoredCount,
                            avgQuiz, avgAssign, avgMid, avgEnd, finalAvg,
                            finalized ? "Yes" : "No");
                }
            }
        }
    }

    // ===================== 2) SCORE SHEET (now with components) =====================

    private void generateScoreSheetReport(Path filePath) throws IOException {
        Map<String, List<String>> courseSections =
                adminService.getInstructorCourseSections(loggedInUser.getUsername());

        try (PrintWriter out = new PrintWriter(new FileWriter(filePath.toFile()))) {
            writeStandardHeader(out, "Score Sheet Report");

            // New header with components
            out.println("Course ID,Section,Student Username," +
                    "Quiz Obtained,Quiz Max," +
                    "Assignment Obtained,Assignment Max," +
                    "Midsem Obtained,Midsem Max," +
                    "Endsem Obtained,Endsem Max," +
                    "Score,Grade,Finalized");

            for (Map.Entry<String, List<String>> e : courseSections.entrySet()) {
                String courseId = e.getKey();
                for (String section : e.getValue()) {

                    List<String> students =
                            adminService.getStudentEnrollmentsForSection(courseId, section);
                    Map<String, Integer> scores =
                            adminService.getStudentScores(courseId, section);
                    boolean finalized = adminService.areGradesComputed(courseId, section);

                    if (students.isEmpty()) {
                        // still write a line for empty section
                        out.printf("%s,%s,%s,%s,%s,%s%n",
                                courseId, section, "(no students)", "", "", finalized ? "Yes" : "No");
                        continue;
                    }

                    for (String stu : students) {

                        // fetch component marks
                        double[] components = adminService.getStudentComponentScores(courseId, section, stu);
                        double qObt = components[0];
                        double qMax = components[1];
                        double aObt = components[2];
                        double aMax = components[3];
                        double mObt = components[4];
                        double mMax = components[5];
                        double eObt = components[6];
                        double eMax = components[7];

                        Integer score = scores.get(stu);
                        String grade = (score != null)
                                ? adminService.calculateGrade(score)
                                : "";
                        String scoreStr = (score != null) ? score.toString() : "";

                        out.printf(
                                "%s,%s,%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%s,%s,%s%n",
                                courseId, section, stu,
                                qObt, qMax, aObt, aMax, mObt, mMax, eObt, eMax,
                                scoreStr, grade,
                                finalized ? "Yes" : "No"
                        );
                    }
                }
            }
        }
    }

    // ===================== 3) FINAL GRADE DISTRIBUTION =====================

    private void generateFinalGradeDistributionReport(Path filePath) throws IOException {
        Map<String, List<String>> courseSections =
                adminService.getInstructorCourseSections(loggedInUser.getUsername());

        // Buckets for grades
        String[] grades = {"A+", "A", "B+", "B", "C", "F"};

        try (PrintWriter out = new PrintWriter(new FileWriter(filePath.toFile()))) {
            writeStandardHeader(out, "Final Grade Distribution");

            // header
            out.print("Course ID,Section,Total Students");
            for (String g : grades) out.print("," + g);
            out.println();

            for (Map.Entry<String, List<String>> e : courseSections.entrySet()) {
                String courseId = e.getKey();
                for (String section : e.getValue()) {
                    List<String> students =
                            adminService.getStudentEnrollmentsForSection(courseId, section);
                    Map<String, Integer> scores =
                            adminService.getStudentScores(courseId, section);

                    Map<String, Integer> counts = new LinkedHashMap<>();
                    for (String g : grades) counts.put(g, 0);

                    for (String stu : students) {
                        Integer sc = scores.get(stu);
                        if (sc != null) {
                            String g = adminService.calculateGrade(sc);
                            if (!counts.containsKey(g)) {
                                counts.put(g, 0);
                            }
                            counts.put(g, counts.get(g) + 1);
                        }
                    }

                    out.printf("%s,%s,%d", courseId, section, students.size());
                    for (String g : grades) {
                        out.printf(",%d", counts.getOrDefault(g, 0));
                    }
                    out.println();
                }
            }
        }
    }

    // ===================== 4) STUDENT PERFORMANCE =====================

    private void generateStudentPerformanceReport(Path filePath) throws IOException {
        Map<String, List<String>> courseSections =
                adminService.getInstructorCourseSections(loggedInUser.getUsername());

        // Map student -> list of scores in this instructor's sections
        Map<String, List<Integer>> studentScores = new TreeMap<>();

        for (Map.Entry<String, List<String>> e : courseSections.entrySet()) {
            String courseId = e.getKey();
            for (String section : e.getValue()) {
                List<String> students =
                        adminService.getStudentEnrollmentsForSection(courseId, section);
                Map<String, Integer> scores =
                        adminService.getStudentScores(courseId, section);

                for (String stu : students) {
                    Integer sc = scores.get(stu);
                    if (sc != null) {
                        studentScores
                                .computeIfAbsent(stu, k -> new ArrayList<>())
                                .add(sc);
                    }
                }
            }
        }

        try (PrintWriter out = new PrintWriter(new FileWriter(filePath.toFile()))) {
            writeStandardHeader(out, "Student Performance Report");

            out.println("Student Username,Number of Assessments,Average Score");

            if (studentScores.isEmpty()) {
                out.println("No data,0,0");
                return;
            }

            for (Map.Entry<String, List<Integer>> e : studentScores.entrySet()) {
                String stu = e.getKey();
                List<Integer> scores = e.getValue();
                int count = scores.size();
                int sum = 0;
                for (int sc : scores) sum += sc;
                double avg = count > 0 ? (sum * 1.0 / count) : 0.0;

                out.printf("%s,%d,%.2f%n", stu, count, avg);
            }
        }
    }
}




