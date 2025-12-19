package com.Arijit_Aditya.erp.ui.swing;

import com.Arijit_Aditya.erp.auth.User;
import com.Arijit_Aditya.erp.services.AdminService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class InstructorDashboardGUI {
    private JFrame frame;
    private final User loggedInUser;
    private final AdminService adminService;

    public InstructorDashboardGUI(User user, AdminService adminService) {
        this.loggedInUser = user;
        this.adminService = adminService;
        createDashboard();
    }

    private void createDashboard() {
        frame = new JFrame("IIITD ERP - Instructor Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLocationRelativeTo(null);

        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(0, 180, 170),
                        getWidth(), getHeight(), new Color(0, 120, 255)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        backgroundPanel.setLayout(new BorderLayout());

        // Header
        JLabel titleLabel = new JLabel("IIITD ERP System - Instructor Dashboard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titleLabel.setForeground(Color.WHITE);

        JLabel userLabel = new JLabel("Welcome, " + loggedInUser.getUsername(), SwingConstants.RIGHT);
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        userLabel.setForeground(Color.WHITE);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(userLabel, BorderLayout.EAST);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 200, 20, 40));
        backgroundPanel.add(headerPanel, BorderLayout.NORTH);

        // Buttons
        String[][] buttons = {
                {"View My Sections", "📚"},
                {"Enter Scores", "✏️"},
                {"Update Scores", "📝"},
                {"Compute Final Grades", "📊"},
                {"View Reports", "📄"},
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

        // Footer
        JLabel footer = new JLabel("© IIIT Delhi ERP | Instructor Portal | Made by Arijit & Aditya", SwingConstants.CENTER);
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        footer.setForeground(Color.WHITE);
        footer.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        backgroundPanel.add(footer, BorderLayout.SOUTH);

        frame.setContentPane(backgroundPanel);
        frame.setVisible(true);
    }

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

        btn.addMouseListener(new MouseAdapter() {
            Color normalBg = btn.getBackground();
            Color hoverBg = new Color(230, 245, 255);

            @Override
            public void mouseEntered(MouseEvent e) {
                new Thread(() -> animateColor(btn, normalBg, hoverBg, 15)).start();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                new Thread(() -> animateColor(btn, hoverBg, normalBg, 15)).start();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                switch (text) {
                    case "View My Sections" ->
                            new InstructorViewSectionsGUI(frame, loggedInUser, adminService);

                    case "Enter Scores" ->
                            new EnterScoresGUI(frame, loggedInUser, adminService);

                    case "Update Scores" ->
                            new UpdateScoresGUI(frame, loggedInUser, adminService);

                    case "Compute Final Grades" ->
                            new ComputeFinalGradesGUI(loggedInUser, adminService);

                    case "View Reports" ->
                            new InstructorReportsGUI(loggedInUser, adminService);

                    case "Logout" -> {
                        frame.dispose();
                        new ERPAppGUI().createAndShowGUI();
                    }

                    default -> JOptionPane.showMessageDialog(frame, "Button not implemented: " + text);
                }
            }
        });

        return btn;
    }

    private void animateColor(JButton button, Color from, Color to, int steps) {
        for (int i = 0; i < steps; i++) {
            int r = from.getRed() + (to.getRed() - from.getRed()) * i / steps;
            int g = from.getGreen() + (to.getGreen() - from.getGreen()) * i / steps;
            int b = from.getBlue() + (to.getBlue() - from.getBlue()) * i / steps;

            Color stepColor = new Color(r, g, b);
            SwingUtilities.invokeLater(() -> button.setBackground(stepColor));

            try { Thread.sleep(15); } catch (InterruptedException ignored) {}
        }
    }
}




