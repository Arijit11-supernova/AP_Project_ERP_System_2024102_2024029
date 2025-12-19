package com.Arijit_Aditya.erp.ui.swing;

import com.Arijit_Aditya.erp.auth.User;
import com.Arijit_Aditya.erp.models.Course;
import com.Arijit_Aditya.erp.services.AdminService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class ViewReportsGUI extends JFrame {

    private final AdminService adminService;
    private final User loggedInUser;

    private JComboBox<String> courseDropdown;
    private JComboBox<String> sectionDropdown;
    private JTable studentTable;

    public ViewReportsGUI(User user, AdminService adminService) {
        this.loggedInUser = user;
        this.adminService = adminService;
        createUI();
    }

    private void createUI() {
        setTitle("View Reports");
        setSize(850, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header
        JLabel header = new JLabel("View Course & Student Reports", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.setForeground(new Color(0, 102, 102));
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(header, BorderLayout.NORTH);

        // Top panel: Course & Section selection
        JPanel topPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));

        topPanel.add(new JLabel("Select Course:"));
        courseDropdown = new JComboBox<>();
        for (Course c : adminService.getAllCourses()) {
            courseDropdown.addItem(c.getCourseId());
        }
        topPanel.add(courseDropdown);

        topPanel.add(new JLabel("Select Section:"));
        sectionDropdown = new JComboBox<>();
        topPanel.add(sectionDropdown);

        add(topPanel, BorderLayout.NORTH);

        // Student table
        studentTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(studentTable);
        add(scrollPane, BorderLayout.CENTER);

        // Dropdown actions
        courseDropdown.addActionListener(e -> updateSections());
        sectionDropdown.addActionListener(e -> updateStudentTable());

        // Bottom panel: Back button
        JPanel bottomPanel = new JPanel();
        JButton btnBack = new JButton("← Back to Dashboard");
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnBack.addActionListener(e -> {
            new AdminDashboardGUI(loggedInUser, adminService);
            dispose();
        });
        bottomPanel.add(btnBack);
        add(bottomPanel, BorderLayout.SOUTH);

        // Initialize sections & table
        updateSections();

        setVisible(true);
    }

    private void updateSections() {
        sectionDropdown.removeAllItems();
        String courseId = (String) courseDropdown.getSelectedItem();
        if (courseId != null) {
            List<String> sections = adminService.getSectionsForCourse(courseId);
            for (String s : sections) {
                sectionDropdown.addItem(s);
            }

            if (sectionDropdown.getItemCount() > 0) {
                sectionDropdown.setSelectedIndex(0);
                updateStudentTable();
            } else {
                // Clear table if no sections
                studentTable.setModel(new DefaultTableModel());
            }
        }
    }

    private void updateStudentTable() {
        if (studentTable == null) return;

        String courseId = (String) courseDropdown.getSelectedItem();
        String section = (String) sectionDropdown.getSelectedItem();

        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Student Name");
        model.addColumn("Score");
        model.addColumn("Grade");

        if (courseId != null && section != null) {
            List<String> enrolledStudents = adminService.getStudentEnrollmentsForSection(courseId, section);
            Map<String, Integer> scores = adminService.getStudentScores(courseId, section);

            for (String student : enrolledStudents) {
                Integer score = scores.get(student);
                String grade = (score != null) ? adminService.calculateGrade(score) : "";
                model.addRow(new Object[]{student, score, grade});
            }
        }

        studentTable.setModel(model);
    }
}






