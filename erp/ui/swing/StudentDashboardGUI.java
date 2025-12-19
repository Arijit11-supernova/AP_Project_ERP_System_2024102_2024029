package com.Arijit_Aditya.erp.ui.swing;

import com.Arijit_Aditya.erp.auth.User;
import com.Arijit_Aditya.erp.services.AdminService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class StudentDashboardGUI {

    private final JFrame frame;
    private final User loggedInUser;
    private final AdminService adminService;

    public StudentDashboardGUI(User user, AdminService adminService) {
        this.loggedInUser = user;
        this.adminService = adminService;
        this.frame = new JFrame("IIITD ERP - Student Dashboard");
        createDashboard();
    }

    private void createDashboard() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLocationRelativeTo(null);

        //  Background Gradient Panel
        JPanel backgroundPanel = new JPanel() {
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
        backgroundPanel.setLayout(new BorderLayout());

        //  Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("IIITD ERP System - Student Dashboard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titleLabel.setForeground(Color.WHITE);

        JLabel userLabel = new JLabel("Welcome, " + loggedInUser.getUsername() + "  ", SwingConstants.RIGHT);
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        userLabel.setForeground(Color.WHITE);

        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(userLabel, BorderLayout.EAST);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 200, 20, 40));
        backgroundPanel.add(headerPanel, BorderLayout.NORTH);

        //  Buttons
        String[][] buttons = {
                {"View Course Catalog", "📚"},
                {"Register for a Section", "📝"},
                {"Drop a Section", "❌"},
                {"View Grades", "📊"},
                {"Download Transcript", "📄"},
                {"Logout", "🚪"}
        };

        JPanel buttonPanel = new JPanel(new GridLayout(3, 2, 30, 30));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(60, 250, 60, 250));

        for (String[] info : buttons) {
            JButton btn = createAnimatedButton(info[0], info[1]);
            buttonPanel.add(btn);
        }

        backgroundPanel.add(buttonPanel, BorderLayout.CENTER);

        //  Footer
        JLabel footer = new JLabel("© IIIT Delhi ERP | Student Portal | Made by Arijit & Aditya", SwingConstants.CENTER);
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        footer.setForeground(Color.WHITE);
        footer.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        backgroundPanel.add(footer, BorderLayout.SOUTH);

        frame.setContentPane(backgroundPanel);
        frame.setVisible(true);
    }

    //  Stylish Animated Buttons
    private JButton createAnimatedButton(String text, String emoji) {
        JButton btn = new JButton(emoji + "  " + text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 22));
        btn.setBackground(new Color(255, 255, 255, 230));
        btn.setForeground(new Color(0, 50, 110));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 140, 255), 2, true),
                BorderFactory.createEmptyBorder(18, 30, 18, 30)
        ));

        Color normalBg = btn.getBackground();
        Color hoverBg = new Color(230, 245, 255);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverBg);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(normalBg);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                frame.dispose();
                switch (text) {
                    case "View Course Catalog" -> new ViewCourseCatalogGUI(loggedInUser, adminService);
                    case "Register for a Section" -> new RegisterSectionGUI(loggedInUser, adminService);
                    case "Drop a Section" -> new DropSectionGUI(loggedInUser, adminService);
                    case "View Grades" -> new ViewGradesGUI(loggedInUser, adminService);
                    case "Download Transcript" -> new DownloadTranscriptGUI(loggedInUser, adminService);
                    case "Logout" -> new ERPAppGUI().createAndShowGUI();
                }
            }
        });
        return btn;
    }
}




