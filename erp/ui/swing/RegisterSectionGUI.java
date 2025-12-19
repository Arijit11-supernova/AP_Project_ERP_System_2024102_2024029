package com.Arijit_Aditya.erp.ui.swing;

import com.Arijit_Aditya.erp.auth.User;
import com.Arijit_Aditya.erp.models.Course;
import com.Arijit_Aditya.erp.services.AdminService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RegisterSectionGUI {
    private final User loggedInUser;
    private final AdminService adminService;
    private final JFrame frame;

    private JComboBox<String> courseDropdown;
    private JComboBox<String> sectionDropdown;

    public RegisterSectionGUI(User user, AdminService adminService) {
        this.loggedInUser = user;
        this.adminService = adminService;
        this.frame = new JFrame("Register for a Section");
        createUI();
    }

    private void createUI() {
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLocationRelativeTo(null);

        //  Background Gradient
        JPanel background = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(58, 123, 213),
                        getWidth(), getHeight(), new Color(58, 96, 115)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        background.setLayout(new BorderLayout());

        // Header
        JLabel header = new JLabel("Register for a Section", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 30));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        background.add(header, BorderLayout.NORTH);

        //  Form Panel
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Course Dropdown
        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Select Course:"), gbc);

        gbc.gridx = 1;
        courseDropdown = new JComboBox<>();
        populateCourses();
        form.add(courseDropdown, gbc);

        // Section Dropdown
        gbc.gridx = 0;
        gbc.gridy++;
        form.add(new JLabel("Select Section:"), gbc);

        gbc.gridx = 1;
        sectionDropdown = new JComboBox<>();
        updateSections();
        form.add(sectionDropdown, gbc);

        courseDropdown.addActionListener(e -> updateSections());

        // Buttons
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;

        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);

        JButton registerBtn = createStyledButton("Register");
        registerBtn.addActionListener(e -> handleRegister());

        JButton backBtn = createStyledButton("← Back to Dashboard");
        backBtn.addActionListener(e -> {
            new StudentDashboardGUI(loggedInUser, adminService);
            frame.dispose();
        });

        btnPanel.add(registerBtn);
        btnPanel.add(backBtn);
        form.add(btnPanel, gbc);

        background.add(form, BorderLayout.CENTER);
        frame.setContentPane(background);
        frame.setVisible(true);
    }

    private void populateCourses() {
        courseDropdown.removeAllItems();
        List<Course> courses = adminService.getAllCourses();
        for (Course c : courses) {
            courseDropdown.addItem(c.getCourseId() + " - " + c.getCourseName());
        }
    }

    private void updateSections() {
        sectionDropdown.removeAllItems();
        String selected = (String) courseDropdown.getSelectedItem();
        if (selected == null) return;
        String courseId = selected.split(" - ")[0];
        List<String> sections = adminService.getSectionsForCourse(courseId);
        for (String s : sections) sectionDropdown.addItem(s);
    }

    private void handleRegister() {
        String courseStr = (String) courseDropdown.getSelectedItem();
        String section = (String) sectionDropdown.getSelectedItem();

        if (courseStr == null || section == null) {
            JOptionPane.showMessageDialog(frame, "Please select both course and section.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String courseId = courseStr.split(" - ")[0];
        boolean success = adminService.registerStudent(loggedInUser.getUsername(), courseId, section);

        if (success) {
            JOptionPane.showMessageDialog(frame, "Registered successfully for " + courseStr + " (" + section + ").",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(frame,
                    "Registration failed. You may already be enrolled or the selection is invalid.",
                    "Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBackground(new Color(255, 255, 255, 230));
        btn.setForeground(new Color(0, 77, 64));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Hover effect
        Color normal = btn.getBackground();
        Color hover = new Color(230, 245, 255);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(hover);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(normal);
            }
        });

        return btn;
    }
}



