package com.Arijit_Aditya.erp.ui.swing;

import com.Arijit_Aditya.erp.auth.User;
import com.Arijit_Aditya.erp.services.AdminService;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Collections;

public class ComputeFinalGradesGUI extends JFrame {

    private final User instructor;
    private final AdminService adminService;

    private JComboBox<String> courseBox;
    private JComboBox<String> sectionBox;

    private final Map<String, List<String>> courseToSections = new LinkedHashMap<>();

    public ComputeFinalGradesGUI(User instructor, AdminService adminService) {
        this.instructor = instructor;
        this.adminService = adminService;

        setTitle("Compute Final Grades");
        setSize(500, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        setVisible(true);
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        JLabel header = new JLabel("Compute Final Grades", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(2, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));

        form.add(new JLabel("Select Course:"));
        courseBox = new JComboBox<>();
        form.add(courseBox);

        form.add(new JLabel("Select Section:"));
        sectionBox = new JComboBox<>();
        form.add(sectionBox);

        add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        JButton btnCompute = new JButton("Compute Grades");
        JButton btnBack = new JButton("← Back to Dashboard");

        bottom.add(btnCompute);
        bottom.add(btnBack);
        add(bottom, BorderLayout.SOUTH);

        // Load courses and sections
        buildCourseSectionMap();
        courseToSections.keySet().forEach(courseBox::addItem);

        courseBox.addActionListener(e -> loadSections());

        btnCompute.addActionListener(e -> computeGrades());
        btnBack.addActionListener(e -> {
            dispose();
        });

        if (courseBox.getItemCount() > 0) {
            courseBox.setSelectedIndex(0);
            loadSections();
        }
    }

    // Replace buildCourseSectionMap() with this
    private void buildCourseSectionMap() {
        courseToSections.clear();
        Map<String, List<String>> assigned = adminService.getInstructorCourseSections(instructor.getUsername());

        for (Map.Entry<String, List<String>> entry : assigned.entrySet()) {
            courseToSections.put(entry.getKey(), entry.getValue());
        }
    }

    private void loadSections() {
        sectionBox.removeAllItems();
        String courseId = (String) courseBox.getSelectedItem();
        if (courseId == null) return;

        List<String> secs = courseToSections.getOrDefault(courseId, Collections.emptyList());
        for (String sec : secs) sectionBox.addItem(sec);

        if (sectionBox.getItemCount() > 0) sectionBox.setSelectedIndex(0);
    }

    private void computeGrades() {
        String courseId = (String) courseBox.getSelectedItem();
        String section = (String) sectionBox.getSelectedItem();

        if (courseId == null || section == null) {
            JOptionPane.showMessageDialog(this, "Please select course and section.");
            return;
        }

        if (adminService.areGradesComputed(courseId, section)) {
            JOptionPane.showMessageDialog(this,
                    "Final grades have already been computed for this section.",
                    "Locked",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Optionally: run in background thread
        SwingUtilities.invokeLater(() -> {
            boolean success = adminService.computeFinalGrades(courseId, section);
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Final grades computed successfully for " + courseId + " - " + section + "!");
            }else {
                JOptionPane.showMessageDialog(this,
                        "Failed to compute grades. Check database or try again.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}




