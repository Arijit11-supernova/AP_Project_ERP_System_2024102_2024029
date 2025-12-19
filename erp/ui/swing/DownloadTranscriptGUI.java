package com.Arijit_Aditya.erp.ui.swing;

import com.Arijit_Aditya.erp.auth.User;
import com.Arijit_Aditya.erp.models.Course;
import com.Arijit_Aditya.erp.services.AdminService;
import javax.swing.*;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class DownloadTranscriptGUI extends JFrame {
    private final User loggedInUser;
    private final AdminService adminService;

    public DownloadTranscriptGUI(User user, AdminService adminService) {
        this.loggedInUser = user;
        this.adminService = adminService;
        createUI();
    }

    private void createUI() {
        setTitle("Transcript - " + loggedInUser.getUsername());
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel header = new JLabel("Student Transcript", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(header, BorderLayout.NORTH);

        JTextArea transcriptArea = new JTextArea();
        transcriptArea.setEditable(false);
        transcriptArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        String transcriptText = generateTranscript();
        transcriptArea.setText(transcriptText);

        add(new JScrollPane(transcriptArea), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        JButton btnSave = new JButton("Save Transcript");
        JButton btnBack = new JButton("← Back");

        btnSave.addActionListener(e -> saveTranscript(transcriptText));
        btnBack.addActionListener(e -> {
            new StudentDashboardGUI(loggedInUser, adminService);
            dispose();
        });

        bottomPanel.add(btnSave);
        bottomPanel.add(btnBack);

        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private String generateTranscript() {
        String student = loggedInUser.getUsername();
        StringBuilder sb = new StringBuilder();

        sb.append("IIIT Delhi - Official Transcript\n");
        sb.append("Student: ").append(student).append("\n");
        sb.append("-------------------------------------------\n\n");

        List<String> enrollments = adminService.getStudentEnrollments(student);

        if (enrollments.isEmpty()) {
            sb.append("You are not enrolled in any courses.\n");
            return sb.toString();
        }

        boolean allGradesFinalized = true;

        for (String entry : enrollments) {
            String[] parts = entry.split(":");
            String courseId = parts[0];
            String section = parts[1];

            Course course = adminService.getCourseById(courseId);
            String courseName = course != null ? course.getCourseName() : "Unknown";

            sb.append("Course: ").append(courseName)
                    .append(" (").append(courseId).append(")")
                    .append("\nSection: ").append(section)
                    .append("\n");

            Map<String, Integer> scores = adminService.getStudentScores(courseId, section);
            boolean finalized = adminService.areGradesComputed(courseId, section);

            if (scores.containsKey(student) && finalized) {
                int score = scores.get(student);
                String grade = adminService.calculateGrade(score);

                sb.append("Score: ").append(score).append("\n");
                sb.append("Grade: ").append(grade).append("\n");
            } else {
                sb.append("Score: Pending\n");
                sb.append("Grade: Pending\n");
                allGradesFinalized = false;
            }

            sb.append("-------------------------------------------\n");
        }

        if (allGradesFinalized) {
            double gpa = adminService.calculateGPA(student);
            sb.append("\nCumulative GPA: ").append(String.format("%.2f", gpa)).append("\n");
        } else {
            sb.append("\nCumulative GPA: N/A (grades not finalized yet)\n");
        }

        return sb.toString();
    }

    private void saveTranscript(String text) {
        String filename = loggedInUser.getUsername() + "_transcript.txt";

        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(text);
            JOptionPane.showMessageDialog(this,
                    "Transcript saved as " + filename,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving file: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}



