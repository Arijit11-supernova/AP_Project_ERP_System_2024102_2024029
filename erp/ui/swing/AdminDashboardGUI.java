package com.Arijit_Aditya.erp.ui.swing;

import com.Arijit_Aditya.erp.auth.User;
import com.Arijit_Aditya.erp.services.AdminService;

import javax.swing.*;
import java.awt.*;

public class AdminDashboardGUI {
    private JFrame frame;
    private final User loggedInUser;
    private final AdminService adminService;

    public AdminDashboardGUI(User user, AdminService adminService) {
        this.loggedInUser = user;
        this.adminService = adminService; // Use existing AdminService instance
        createDashboard();
    }

    private void createDashboard() {

        frame = new JFrame("IIITD ERP - Admin Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLocationRelativeTo(null);

        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(38, 166, 154),
                        getWidth(), getHeight(), new Color(0, 105, 92)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        backgroundPanel.setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("IIITD ERP System - Admin Dashboard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JLabel userLabel = new JLabel("Welcome, " + loggedInUser.getUsername() + "  ");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        userLabel.setForeground(Color.WHITE);
        headerPanel.add(userLabel, BorderLayout.EAST);

        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        backgroundPanel.add(headerPanel, BorderLayout.NORTH);

        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(3, 3, 30, 30));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(60, 200, 60, 200));

        // Button names and icons
        String[][] buttons = {
                {"Add User", "➕"},
                {"Delete User", "🗑️"},
                {"Add Course", "📚"},
                {"Create Section", "📘"},
                {"Assign Instructor", "👨‍🏫"},
                {"Toggle Maintenance Mode", "⚙️"},
                {"View Reports", "📊"},
                {"Logout", "🚪"}
        };

        boolean maintenance = adminService.isMaintenanceMode();

        for (String[] info : buttons) {
            JButton btn = createAnimatedButton(info[0], info[1]);

            // Disable certain buttons if Maintenance Mode is ON
            if (maintenance && (
                    info[0].equals("Add User") ||
                            info[0].equals("Add Course") ||
                            info[0].equals("Create Section") ||
                            info[0].equals("Assign Instructor")
            )) {
                btn.setEnabled(false);
                btn.setToolTipText("Disabled: Maintenance Mode is ON");
            }

            buttonPanel.add(btn);
        }

        backgroundPanel.add(buttonPanel, BorderLayout.CENTER);

        // Footer
        JLabel footer = new JLabel("© IIIT Delhi ERP System | Developed by Arijit & Aditya", SwingConstants.CENTER);
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        footer.setForeground(Color.WHITE);
        footer.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        backgroundPanel.add(footer, BorderLayout.SOUTH);

        frame.setContentPane(backgroundPanel);
        frame.setVisible(true);
    }

    private JButton createAnimatedButton(String text, String emoji) {
        JButton btn = new JButton(emoji + "  " + text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btn.setBackground(new Color(255, 255, 255, 230));
        btn.setForeground(new Color(0, 77, 64));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> handleAction(text));

        return btn;
    }

    private void handleAction(String action) {

        switch (action) {
            case "Add User" -> {
                frame.dispose();
                new AddUserGUI(loggedInUser, adminService);
            }

            case "Delete User" -> {
                frame.dispose();
                new DeleteUserGUI(loggedInUser, adminService);
            }

            case "Add Course" -> {
                frame.dispose();
                new AddCourseGUI(loggedInUser, adminService);
            }

            case "Create Section" -> {
                frame.dispose();
                new CreateSectionGUI(loggedInUser, adminService);
            }

            case "Assign Instructor" -> {
                frame.dispose();
                new AssignInstructorGUI(loggedInUser, adminService);
            }

            case "Toggle Maintenance Mode" -> {
                adminService.toggleMaintenanceMode();
                frame.dispose();
                new AdminDashboardGUI(loggedInUser, adminService);
            }

            case "View Reports" -> {
                frame.dispose();
                new ViewReportsGUI(loggedInUser, adminService);
            }

            case "Logout" -> {
                frame.dispose();
                new ERPAppGUI().createAndShowGUI();
            }
        }
    }
}







