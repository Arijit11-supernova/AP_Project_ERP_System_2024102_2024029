package com.Arijit_Aditya.erp.ui.swing;

import com.Arijit_Aditya.erp.auth.User;
import com.Arijit_Aditya.erp.services.AdminService;
import javax.swing.*;
import java.awt.*;

public class CreateSectionGUI extends JFrame {
    private final AdminService adminService;
    private final User loggedInUser;

    public CreateSectionGUI(User user, AdminService adminService) {
        this.loggedInUser = user;
        this.adminService = adminService;

        setTitle("Create Section");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Title
        JLabel title = new JLabel("Create New Section", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        add(title, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        JTextField txtCourseId = new JTextField();
        JTextField txtSectionName = new JTextField();
        JTextField txtInstructor = new JTextField();
        JTextField txtDayTime = new JTextField();
        JTextField txtRoom = new JTextField();
        JTextField txtCapacity = new JTextField();
        JTextField txtSemester = new JTextField();
        JTextField txtYear = new JTextField();

        formPanel.add(new JLabel("Course ID:"));
        formPanel.add(txtCourseId);
        formPanel.add(new JLabel("Section Name:"));
        formPanel.add(txtSectionName);
        formPanel.add(new JLabel("Instructor Username:"));
        formPanel.add(txtInstructor);
        formPanel.add(new JLabel("Day/Time:"));
        formPanel.add(txtDayTime);
        formPanel.add(new JLabel("Room:"));
        formPanel.add(txtRoom);
        formPanel.add(new JLabel("Capacity:"));
        formPanel.add(txtCapacity);
        formPanel.add(new JLabel("Semester:"));
        formPanel.add(txtSemester);
        formPanel.add(new JLabel("Year:"));
        formPanel.add(txtYear);

        add(formPanel, BorderLayout.CENTER);

        // Buttons Panel
        JPanel btnPanel = new JPanel();

        JButton btnCreate = new JButton("Create Section");
        JButton btnBack = new JButton("← Back to Dashboard");

        btnPanel.add(btnCreate);
        btnPanel.add(btnBack);

        add(btnPanel, BorderLayout.SOUTH);

        // Create Section Action
        btnCreate.addActionListener(e -> {
            // Check Maintenance Mode
            if (adminService.isMaintenanceMode()) {
                JOptionPane.showMessageDialog(this,
                        "Cannot create section: Maintenance Mode is ON!",
                        "Maintenance Mode",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String courseId = txtCourseId.getText().trim();
            String sectionName = txtSectionName.getText().trim();
            String instructor = txtInstructor.getText().trim();
            String dayTime = txtDayTime.getText().trim();
            String room = txtRoom.getText().trim();
            String capacityText = txtCapacity.getText().trim();
            String semester = txtSemester.getText().trim();
            String yearText = txtYear.getText().trim();

            if (courseId.isEmpty() || sectionName.isEmpty() || instructor.isEmpty()
                    || dayTime.isEmpty() || room.isEmpty() || capacityText.isEmpty()
                    || semester.isEmpty() || yearText.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please fill all fields!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            int capacity, year;
            try {
                capacity = Integer.parseInt(capacityText);
                year = Integer.parseInt(yearText);
                if (capacity <= 0 || year <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Capacity and Year must be positive integers!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean created = adminService.createSection(
                    courseId, sectionName, instructor, dayTime, room, capacity, semester, year
            );

            if (created) {
                JOptionPane.showMessageDialog(this,
                        "Section \"" + sectionName + "\" created successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                // Clear all fields
                txtCourseId.setText("");
                txtSectionName.setText("");
                txtInstructor.setText("");
                txtDayTime.setText("");
                txtRoom.setText("");
                txtCapacity.setText("");
                txtSemester.setText("");
                txtYear.setText("");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to create section. Check course ID, instructor username, or database.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Back to Dashboard
        btnBack.addActionListener(e -> {
            new AdminDashboardGUI(loggedInUser, adminService);
            dispose();
        });

        setVisible(true);
    }
}

