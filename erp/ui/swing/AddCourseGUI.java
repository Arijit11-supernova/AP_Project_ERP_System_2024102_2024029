package com.Arijit_Aditya.erp.ui.swing;

import com.Arijit_Aditya.erp.auth.User;
import com.Arijit_Aditya.erp.services.AdminService;

import javax.swing.*;
import java.awt.*;

public class AddCourseGUI {

    private JFrame frame;
    private JTextField txtCourseId;
    private JTextField txtCourseName;
    private JTextField txtCredits;

    private final User loggedInUser;
    private final AdminService adminService;

    public AddCourseGUI(User user, AdminService adminService) {
        this.loggedInUser = user;
        this.adminService = adminService;
        createUI();
    }

    private void createUI() {
        frame = new JFrame("Add Course");
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel header = new JLabel("Add New Course", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 26));
        header.setForeground(new Color(0, 102, 102));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(header, gbc);

        // Course ID
        gbc.gridy++;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Course ID:"), gbc);

        txtCourseId = new JTextField();
        gbc.gridx = 1;
        panel.add(txtCourseId, gbc);

        // Course Name
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Course Name:"), gbc);

        txtCourseName = new JTextField();
        gbc.gridx = 1;
        panel.add(txtCourseName, gbc);

        // Credits
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Credits:"), gbc);

        txtCredits = new JTextField();
        gbc.gridx = 1;
        panel.add(txtCredits, gbc);

        // Button: Add Course
        JButton btnAdd = new JButton("Create Course");
        btnAdd.setBackground(new Color(0, 153, 153));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnAdd.addActionListener(e -> handleCreateCourse());

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        panel.add(btnAdd, gbc);

        // Back Button
        JButton btnBack = new JButton("← Back to Dashboard");
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnBack.addActionListener(e -> {
            frame.dispose();
            new AdminDashboardGUI(loggedInUser, adminService);
        });

        gbc.gridy++;
        panel.add(btnBack, gbc);

        frame.add(panel);
        frame.setVisible(true);
    }

    private void handleCreateCourse() {
        //  Check Maintenance Mode
        if (adminService.isMaintenanceMode()) {
            JOptionPane.showMessageDialog(frame,
                    "Cannot create course: Maintenance Mode is ON!",
                    "Maintenance Mode",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String courseId = txtCourseId.getText().trim();
        String courseName = txtCourseName.getText().trim();
        String creditsText = txtCredits.getText().trim();

        if (courseId.isEmpty() || courseName.isEmpty() || creditsText.isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                    "All fields are required!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int credits;
        try {
            credits = Integer.parseInt(creditsText);
            if (credits <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame,
                    "Credits must be a positive integer!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if course already exists in DB
        if (adminService.getCourseById(courseId) != null) {
            JOptionPane.showMessageDialog(frame,
                    "A course with this ID already exists!",
                    "Duplicate Course",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        //  Call AdminService to create course with credits
        boolean created = adminService.createCourse(courseId, courseName, credits);

        if (created) {
            JOptionPane.showMessageDialog(frame,
                    "Course created successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            txtCourseId.setText("");
            txtCourseName.setText("");
            txtCredits.setText("");
        } else {
            JOptionPane.showMessageDialog(frame,
                    "Failed to create course. Check database or logs.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}




