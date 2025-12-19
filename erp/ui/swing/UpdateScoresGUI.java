package com.Arijit_Aditya.erp.ui.swing;

import com.Arijit_Aditya.erp.auth.User;
import com.Arijit_Aditya.erp.services.AdminService;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.LinkedHashMap;

public class UpdateScoresGUI extends JFrame {

    private final User instructor;
    private final AdminService adminService;
    private final JFrame parentFrame;

    private JComboBox<String> courseBox;
    private JComboBox<String> sectionBox;
    private JComboBox<String> studentBox;

    // current final score display
    private JLabel currentScoreLabel;

    // component totals
    private JTextField quizObtField;
    private JTextField quizMaxField;
    private JTextField assignmentObtField;
    private JTextField assignmentMaxField;
    private JTextField midsemObtField;
    private JTextField midsemMaxField;
    private JTextField endsemObtField;
    private JTextField endsemMaxField;

    private JButton btnQuizDetails;
    private JButton btnAssignmentDetails;

    private final Map<String, List<String>> courseToSections = new LinkedHashMap<>();

    public UpdateScoresGUI(JFrame parent, User instructor, AdminService adminService) {
        this.parentFrame = parent;
        this.instructor = instructor;
        this.adminService = adminService;

        setTitle("Update Scores - Instructor");
        setSize(700, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        setVisible(true);
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel header = new JLabel("Update Student Scores (Components)", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setBorder(BorderFactory.createEmptyBorder(8, 0, 18, 0));
        root.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Course
        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Course:"), gbc);
        courseBox = new JComboBox<>();
        gbc.gridx = 1; gbc.weightx = 1.0;
        form.add(courseBox, gbc);
        gbc.weightx = 0;

        // Section
        gbc.gridx = 0; gbc.gridy++;
        form.add(new JLabel("Section:"), gbc);
        sectionBox = new JComboBox<>();
        gbc.gridx = 1;
        form.add(sectionBox, gbc);

        // Student
        gbc.gridx = 0; gbc.gridy++;
        form.add(new JLabel("Student:"), gbc);
        studentBox = new JComboBox<>();
        gbc.gridx = 1;
        form.add(studentBox, gbc);

        // Current final score
        gbc.gridx = 0; gbc.gridy++;
        form.add(new JLabel("Current Final Score:"), gbc);
        currentScoreLabel = new JLabel("-");
        gbc.gridx = 1;
        form.add(currentScoreLabel, gbc);

        // Quiz totals + button
        gbc.gridx = 0; gbc.gridy++;
        form.add(new JLabel("Quizzes (total) [Obt / Max]:"), gbc);
        quizObtField = new JTextField(5);
        quizMaxField = new JTextField(5);
        JPanel quizPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        quizPanel.add(new JLabel("Obt:"));
        quizPanel.add(quizObtField);
        quizPanel.add(new JLabel("Max:"));
        quizPanel.add(quizMaxField);
        gbc.gridx = 1;
        form.add(quizPanel, gbc);

        btnQuizDetails = new JButton("Enter Quiz Marks...");
        gbc.gridx = 2;
        form.add(btnQuizDetails, gbc);

        // Assignment totals + button
        gbc.gridx = 0; gbc.gridy++;
        form.add(new JLabel("Assignments (total) [Obt / Max]:"), gbc);
        assignmentObtField = new JTextField(5);
        assignmentMaxField = new JTextField(5);
        JPanel assignmentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        assignmentPanel.add(new JLabel("Obt:"));
        assignmentPanel.add(assignmentObtField);
        assignmentPanel.add(new JLabel("Max:"));
        assignmentPanel.add(assignmentMaxField);
        gbc.gridx = 1;
        form.add(assignmentPanel, gbc);

        btnAssignmentDetails = new JButton("Enter Assignment Marks...");
        gbc.gridx = 2;
        form.add(btnAssignmentDetails, gbc);

        // Midsem
        gbc.gridx = 0; gbc.gridy++;
        form.add(new JLabel("Midsem [Obt / Max]:"), gbc);
        midsemObtField = new JTextField(5);
        midsemMaxField = new JTextField(5);
        JPanel midsemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        midsemPanel.add(new JLabel("Obt:"));
        midsemPanel.add(midsemObtField);
        midsemPanel.add(new JLabel("Max:"));
        midsemPanel.add(midsemMaxField);
        gbc.gridx = 1;
        form.add(midsemPanel, gbc);

        // Endsem
        gbc.gridx = 0; gbc.gridy++;
        form.add(new JLabel("Endsem [Obt / Max]:"), gbc);
        endsemObtField = new JTextField(5);
        endsemMaxField = new JTextField(5);
        JPanel endsemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        endsemPanel.add(new JLabel("Obt:"));
        endsemPanel.add(endsemObtField);
        endsemPanel.add(new JLabel("Max:"));
        endsemPanel.add(endsemMaxField);
        gbc.gridx = 1;
        form.add(endsemPanel, gbc);

        root.add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        JButton btnLoad = new JButton("Load Students");
        JButton btnUpdate = new JButton("Update Scores");
        JButton btnBack = new JButton("← Back to Dashboard");
        bottom.add(btnLoad);
        bottom.add(btnUpdate);
        bottom.add(btnBack);
        root.add(bottom, BorderLayout.SOUTH);

        add(root);

        // Build course → sections map
        buildCourseSectionMap();
        courseToSections.keySet().forEach(courseBox::addItem);

        courseBox.addActionListener(e -> onCourseSelected());
        btnLoad.addActionListener(e -> onLoadStudents());
        studentBox.addActionListener(e -> onStudentSelected());
        btnUpdate.addActionListener(e -> onUpdateScore());
        btnBack.addActionListener(e -> {
            this.dispose();
            parentFrame.setVisible(true);
        });

        btnQuizDetails.addActionListener(e -> openQuizDialog());
        btnAssignmentDetails.addActionListener(e -> openAssignmentDialog());

        if (courseBox.getItemCount() > 0) {
            courseBox.setSelectedIndex(0);
            onCourseSelected();
        }
    }

    private void clearComponentFields() {
        if (quizObtField != null) {
            quizObtField.setText("");
            quizMaxField.setText("");
            assignmentObtField.setText("");
            assignmentMaxField.setText("");
            midsemObtField.setText("");
            midsemMaxField.setText("");
            endsemObtField.setText("");
            endsemMaxField.setText("");
        }
    }

    private void buildCourseSectionMap() {
        courseToSections.clear();
        List<String> sections = adminService.getSectionsByInstructor(instructor.getUsername());

        for (String s : sections) {
            if (s == null) continue;

            // Handle courses with no sections
            if (s.contains("→ No Sections")) {
                String courseId = s.substring("Course:".length(), s.indexOf("→")).trim();
                courseToSections.putIfAbsent(courseId, new ArrayList<>());
                continue;
            }

            String courseId = "";
            String section = "";

            if (s.contains("|")) {
                String[] parts = s.split("\\|");
                if (parts.length >= 2) {
                    courseId = parts[0].replace("Course:", "").trim();
                    section = parts[1].replace("Section:", "").trim();
                }
            } else {
                int courseIdx = s.indexOf("Course:");
                int secIdx = s.indexOf("Section:");
                if (courseIdx >= 0) {
                    if (secIdx > courseIdx) {
                        courseId = s.substring(courseIdx + 7, secIdx).trim();
                        section = s.substring(secIdx + 8).trim();
                    } else {
                        courseId = s.substring(courseIdx + 7).trim();
                    }
                }
            }

            if (!courseId.isEmpty()) {
                courseToSections.putIfAbsent(courseId, new ArrayList<>());
                if (!section.isEmpty()) {
                    List<String> secs = courseToSections.get(courseId);
                    if (!secs.contains(section)) secs.add(section);
                }
            }
        }
    }

    private void onCourseSelected() {
        sectionBox.removeAllItems();
        studentBox.removeAllItems();
        currentScoreLabel.setText("-");
        clearComponentFields();

        String courseId = (String) courseBox.getSelectedItem();
        if (courseId == null) return;

        List<String> secs = courseToSections.getOrDefault(courseId, Collections.emptyList());
        for (String sec : secs) sectionBox.addItem(sec);

        if (sectionBox.getItemCount() > 0) {
            sectionBox.setSelectedIndex(0);
            onLoadStudents();
        }
    }

    private void onLoadStudents() {
        studentBox.removeAllItems();
        currentScoreLabel.setText("-");
        clearComponentFields();

        String courseId = (String) courseBox.getSelectedItem();
        String section = (String) sectionBox.getSelectedItem();
        if (courseId == null || section == null) return;

        List<String> students = adminService.getStudentEnrollmentsForSection(courseId, section);
        for (String s : students) studentBox.addItem(s);

        if (studentBox.getItemCount() > 0) {
            studentBox.setSelectedIndex(0);
            onStudentSelected();
        }
    }

    private void onStudentSelected() {
        clearComponentFields();
        currentScoreLabel.setText("-");

        String courseId = (String) courseBox.getSelectedItem();
        String section = (String) sectionBox.getSelectedItem();
        String student = (String) studentBox.getSelectedItem();
        if (courseId == null || section == null || student == null) return;

        // final score
        Map<String, Integer> scores = adminService.getStudentScores(courseId, section);
        if (scores != null && scores.containsKey(student)) {
            currentScoreLabel.setText(String.valueOf(scores.get(student)));
        }

        // component totals
        double[] comps = adminService.getStudentComponentTotals(courseId, section, student);
        if (comps != null && comps.length == 8) {
            if (comps[0] != 0 || comps[1] != 0) {
                quizObtField.setText(String.valueOf(comps[0]));
                quizMaxField.setText(String.valueOf(comps[1]));
            }
            if (comps[2] != 0 || comps[3] != 0) {
                assignmentObtField.setText(String.valueOf(comps[2]));
                assignmentMaxField.setText(String.valueOf(comps[3]));
            }
            if (comps[4] != 0 || comps[5] != 0) {
                midsemObtField.setText(String.valueOf(comps[4]));
                midsemMaxField.setText(String.valueOf(comps[5]));
            }
            if (comps[6] != 0 || comps[7] != 0) {
                endsemObtField.setText(String.valueOf(comps[6]));
                endsemMaxField.setText(String.valueOf(comps[7]));
            }
        }
    }

    // Dialogs for per-quiz / per-assignment marks

    private void openQuizDialog() {
        String courseId = (String) courseBox.getSelectedItem();
        String section = (String) sectionBox.getSelectedItem();
        String student = (String) studentBox.getSelectedItem();
        if (courseId == null || section == null || student == null) {
            JOptionPane.showMessageDialog(this, "Select course, section and student first.");
            return;
        }

        int[] counts = adminService.getAssessmentStructure(courseId, section);
        int quizCount = counts[0];
        int assignmentCount = counts[1];

        if (quizCount <= 0) {
            String input = JOptionPane.showInputDialog(
                    this,
                    "How many quizzes for this section?",
                    "Set Quiz Count",
                    JOptionPane.QUESTION_MESSAGE
            );
            if (input == null) return;
            try {
                quizCount = Integer.parseInt(input.trim());
                if (quizCount < 0) throw new NumberFormatException();
                adminService.setAssessmentStructure(courseId, section, quizCount, assignmentCount);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid non-negative integer.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        ComponentMarksDialog dialog =
                new ComponentMarksDialog(this, "Enter Quiz Marks (" + student + ")", quizCount);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            quizObtField.setText(String.valueOf(dialog.getTotalObt()));
            quizMaxField.setText(String.valueOf(dialog.getTotalMax()));
        }
    }

    private void openAssignmentDialog() {
        String courseId = (String) courseBox.getSelectedItem();
        String section = (String) sectionBox.getSelectedItem();
        String student = (String) studentBox.getSelectedItem();
        if (courseId == null || section == null || student == null) {
            JOptionPane.showMessageDialog(this, "Select course, section and student first.");
            return;
        }

        int[] counts = adminService.getAssessmentStructure(courseId, section);
        int quizCount = counts[0];
        int assignmentCount = counts[1];

        if (assignmentCount <= 0) {
            String input = JOptionPane.showInputDialog(
                    this,
                    "How many assignments for this section?",
                    "Set Assignment Count",
                    JOptionPane.QUESTION_MESSAGE
            );
            if (input == null) return;
            try {
                assignmentCount = Integer.parseInt(input.trim());
                if (assignmentCount < 0) throw new NumberFormatException();
                adminService.setAssessmentStructure(courseId, section, quizCount, assignmentCount);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid non-negative integer.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        ComponentMarksDialog dialog =
                new ComponentMarksDialog(this, "Enter Assignment Marks (" + student + ")", assignmentCount);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            assignmentObtField.setText(String.valueOf(dialog.getTotalObt()));
            assignmentMaxField.setText(String.valueOf(dialog.getTotalMax()));
        }
    }

    //Update and save scores

    private void onUpdateScore() {
        String courseId = (String) courseBox.getSelectedItem();
        String section = (String) sectionBox.getSelectedItem();
        String student = (String) studentBox.getSelectedItem();

        if (courseId == null || section == null || student == null) {
            JOptionPane.showMessageDialog(this, "Please select course, section, and student.");
            return;
        }

        if (adminService.areGradesComputed(courseId, section)) {
            JOptionPane.showMessageDialog(this,
                    "Final grades for this section are already computed. Scores cannot be updated.",
                    "Locked", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Ensure grade scheme exists
        int[] weights = adminService.getSectionGradeScheme(courseId, section);
        int totalW = weights[0] + weights[1] + weights[2] + weights[3];

        if (totalW != 100) {
            JOptionPane.showMessageDialog(this,
                    "Grade scheme (component weights) is not set properly (must sum to 100). " +
                            "Please set it using Enter Scores UI.",
                    "Grade Scheme Missing",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Parse component totals
        double quizObt, quizMax, assignmentObt, assignmentMax, midsemObt, midsemMax, endsemObt, endsemMax;
        try {
            quizObt = parseDoubleOrZero(quizObtField.getText());
            quizMax = parseDoubleOrZero(quizMaxField.getText());
            assignmentObt = parseDoubleOrZero(assignmentObtField.getText());
            assignmentMax = parseDoubleOrZero(assignmentMaxField.getText());
            midsemObt = parseDoubleOrZero(midsemObtField.getText());
            midsemMax = parseDoubleOrZero(midsemMaxField.getText());
            endsemObt = parseDoubleOrZero(endsemObtField.getText());
            endsemMax = parseDoubleOrZero(endsemMaxField.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Please enter valid numeric values for marks.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        adminService.addStudentScore(
                courseId,
                section,
                student,
                quizObt,
                quizMax,
                assignmentObt,
                assignmentMax,
                midsemObt,
                midsemMax,
                endsemObt,
                endsemMax
        );

        // Refresh current score display
        Map<String, Integer> scores = adminService.getStudentScores(courseId, section);
        if (scores != null && scores.containsKey(student)) {
            currentScoreLabel.setText(String.valueOf(scores.get(student)));
        }

        JOptionPane.showMessageDialog(this, "Scores updated successfully.");
    }

    private double parseDoubleOrZero(String text) throws NumberFormatException {
        String t = text.trim();
        if (t.isEmpty()) return 0.0;
        return Double.parseDouble(t);
    }

    //Helper dialog for per-item marks

    private static class ComponentMarksDialog extends JDialog {
        private double totalObt = 0.0;
        private double totalMax = 0.0;
        private boolean confirmed = false;

        public ComponentMarksDialog(JFrame parent, String title, int count) {
            super(parent, title, true);
            setSize(400, 120 + count * 40);
            setLocationRelativeTo(parent);

            JPanel panel = new JPanel(new BorderLayout(5, 5));
            JPanel grid = new JPanel(new GridLayout(count + 1, 3, 5, 5));

            grid.add(new JLabel("Item"));
            grid.add(new JLabel("Obtained"));
            grid.add(new JLabel("Max"));

            JTextField[] obtFields = new JTextField[count];
            JTextField[] maxFields = new JTextField[count];

            for (int i = 0; i < count; i++) {
                grid.add(new JLabel(" #" + (i + 1) + ": "));
                obtFields[i] = new JTextField(5);
                maxFields[i] = new JTextField(5);
                grid.add(obtFields[i]);
                grid.add(maxFields[i]);
            }

            panel.add(grid, BorderLayout.CENTER);

            JPanel buttons = new JPanel();
            JButton ok = new JButton("OK");
            JButton cancel = new JButton("Cancel");
            buttons.add(ok);
            buttons.add(cancel);
            panel.add(buttons, BorderLayout.SOUTH);

            ok.addActionListener(e -> {
                try {
                    double obtSum = 0.0;
                    double maxSum = 0.0;
                    for (int i = 0; i < count; i++) {
                        String oText = obtFields[i].getText().trim();
                        String mText = maxFields[i].getText().trim();
                        if (!oText.isEmpty()) obtSum += Double.parseDouble(oText);
                        if (!mText.isEmpty()) maxSum += Double.parseDouble(mText);
                    }
                    totalObt = obtSum;
                    totalMax = maxSum;
                    confirmed = true;
                    dispose();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Please enter valid numbers.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            cancel.addActionListener(e -> {
                confirmed = false;
                dispose();
            });

            setContentPane(panel);
        }

        public boolean isConfirmed() { return confirmed; }
        public double getTotalObt() { return totalObt; }
        public double getTotalMax() { return totalMax; }
    }
}






