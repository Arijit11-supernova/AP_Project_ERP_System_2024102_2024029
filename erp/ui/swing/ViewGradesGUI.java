package com.Arijit_Aditya.erp.ui.swing;

import com.Arijit_Aditya.erp.auth.User;
import com.Arijit_Aditya.erp.services.AdminService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class ViewGradesGUI {

    private final User loggedInUser;
    private final AdminService adminService;
    private JFrame frame;
    private JTable table;

    public ViewGradesGUI(User user, AdminService adminService) {
        this.loggedInUser = user;
        this.adminService = adminService;
        createUI();
    }

    private void createUI() {
        frame = new JFrame("View Grades");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLocationRelativeTo(null);

        // Background
        JPanel background = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0,0,new Color(58,123,213), getWidth(), getHeight(), new Color(58,96,115));
                g2.setPaint(gp);
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };
        background.setLayout(new BorderLayout());

        // Header
        JLabel header = new JLabel("Your Grades", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 30));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(30,0,30,0));
        background.add(header, BorderLayout.NORTH);

        // Table
        table = new JTable();
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder(20,60,20,60));
        background.add(sp, BorderLayout.CENTER);

        // Bottom: GPA + Back
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BorderLayout());

        JLabel gpaLabel = new JLabel("", SwingConstants.CENTER);
        gpaLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        gpaLabel.setForeground(Color.WHITE);
        bottom.add(gpaLabel, BorderLayout.NORTH);

        JButton backBtn = createStyledButton("← Back to Dashboard");
        backBtn.addActionListener(e -> {
            new StudentDashboardGUI(loggedInUser, adminService);
            frame.dispose();
        });
        JPanel btnP = new JPanel();
        btnP.setOpaque(false);
        btnP.add(backBtn);
        bottom.add(btnP, BorderLayout.SOUTH);

        background.add(bottom, BorderLayout.SOUTH);

        frame.setContentPane(background);
        populateTableAndGPA(gpaLabel);
        frame.setVisible(true);
    }

    private void populateTableAndGPA(JLabel gpaLabel) {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Course ID");
        model.addColumn("Section");
        model.addColumn("Score");
        model.addColumn("Grade");
        model.addColumn("Finalized");

        List<String> enrolled = adminService.getStudentEnrollments(loggedInUser.getUsername());
        for (String cs : enrolled) {
            String[] parts = cs.split(":");
            if (parts.length < 2) continue;
            String courseId = parts[0];
            String section = parts[1];

            boolean finalized = adminService.areGradesComputed(courseId, section);
            Map<String, Integer> scores = adminService.getStudentScores(courseId, section);
            Integer score = finalized ? scores.get(loggedInUser.getUsername()) : null;
            String grade = (finalized && score != null) ? adminService.calculateGrade(score) : "Pending";

            model.addRow(new Object[]{
                    courseId,
                    section,
                    score != null ? score : "-",
                    grade,
                    finalized ? "Yes" : "No"
            });
        }

        table.setModel(model);

        // Show GPA if there are finalized grades
        double gpa = adminService.calculateGPA(loggedInUser.getUsername());
        if (gpa > 0) {
            gpaLabel.setText(String.format("Current GPA: %.2f", gpa));
        } else {
            gpaLabel.setText("GPA: N/A (no finalized grades)");
        }
    }

    private JButton createStyledButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 16));
        b.setBackground(new Color(255,255,255,230));
        b.setForeground(new Color(0,77,64));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(10,18,10,18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}


