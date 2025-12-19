package com.Arijit_Aditya.erp.ui.swing;

import com.Arijit_Aditya.erp.auth.User;
import com.Arijit_Aditya.erp.models.Course;
import com.Arijit_Aditya.erp.services.AdminService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AssignInstructorGUI extends JFrame {

    private final AdminService adminService;
    private final User loggedInUser;

    private JComboBox<String> courseDropdown;
    private JComboBox<String> sectionDropdown;
    private JTextField txtInstructorName;

    public AssignInstructorGUI(User user, AdminService adminService) {
        this.loggedInUser = user;
        this.adminService = adminService;
        createUI();
    }

    private void createUI() {
        setTitle("Assign Instructor");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header
        JLabel header = new JLabel("Assign Instructor to Course Section", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.setForeground(new Color(0, 102, 102));
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(header, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        // Course dropdown
        formPanel.add(new JLabel("Select Course:"));
        courseDropdown = new JComboBox<>();
        populateCourses();
        formPanel.add(courseDropdown);

        // Section dropdown
        formPanel.add(new JLabel("Select Section:"));
        sectionDropdown = new JComboBox<>();
        updateSections(); // populate sections for selected course
        formPanel.add(sectionDropdown);

        // Instructor name
        formPanel.add(new JLabel("Instructor Username:"));
        txtInstructorName = new JTextField();
        formPanel.add(txtInstructorName);

        add(formPanel, BorderLayout.CENTER);

        // Update sections when course changes
        courseDropdown.addActionListener(e -> updateSections());

        // Buttons
        JPanel btnPanel = new JPanel();
        JButton btnAssign = new JButton("Assign Instructor");
        JButton btnBack = new JButton("← Back to Dashboard");

        btnAssign.addActionListener(e -> handleAssignInstructor());
        btnBack.addActionListener(e -> {
            new AdminDashboardGUI(loggedInUser, adminService);
            dispose();
        });

        btnPanel.add(btnAssign);
        btnPanel.add(btnBack);

        add(btnPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void populateCourses() {
        courseDropdown.removeAllItems();
        List<Course> courses = adminService.getAllCourses();
        for (Course course : courses) {
            courseDropdown.addItem(course.getCourseId());
        }
    }

    private void updateSections() {
        sectionDropdown.removeAllItems();
        String courseId = (String) courseDropdown.getSelectedItem();
        if (courseId != null) {
            List<String> sections = adminService.getSectionsForCourse(courseId);
            for (String section : sections) {
                sectionDropdown.addItem(section);
            }
        }
    }

    private void handleAssignInstructor() {
        if (adminService.isMaintenanceMode()) {
            JOptionPane.showMessageDialog(this,
                    "Cannot assign instructor: Maintenance Mode is ON!",
                    "Maintenance Mode",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String courseId = (String) courseDropdown.getSelectedItem();
        String section = (String) sectionDropdown.getSelectedItem();
        String instructorName = txtInstructorName.getText().trim();

        if (courseId == null || section == null || instructorName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "All fields are required!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success = adminService.assignInstructor(courseId, section, instructorName);

        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Instructor \"" + instructorName + "\" assigned to " + courseId + " - " + section + " successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to assign instructor. Check if the instructor exists or if the section already has an instructor.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        txtInstructorName.setText("");
    }
}



