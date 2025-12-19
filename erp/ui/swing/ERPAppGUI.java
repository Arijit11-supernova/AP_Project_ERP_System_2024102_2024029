package com.Arijit_Aditya.erp.ui.swing;

import com.Arijit_Aditya.erp.services.AdminService;
import com.Arijit_Aditya.erp.auth.LoginService;
import com.Arijit_Aditya.erp.auth.User;
import javax.swing.*;
import java.awt.*;

public class ERPAppGUI {
    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;

    //Main GUI
    public void createAndShowGUI() {
        frame = new JFrame("ERP System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setMinimumSize(new Dimension(800, 600));
        frame.setLayout(new GridBagLayout());

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = new Color(58, 123, 213);
                Color color2 = new Color(58, 96, 115);
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel lblTitle = new JLabel("Welcome to ERP System", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblTitle.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(lblTitle, gbc);

        // Username
        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setForeground(Color.WHITE);
        lblUsername.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        panel.add(lblUsername, gbc);

        usernameField = new JTextField();
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        // Password
        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setForeground(Color.WHITE);
        lblPassword.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        gbc.gridy++;
        gbc.gridx = 0;
        panel.add(lblPassword, gbc);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        // Login button
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 22));
        loginButton.setBackground(new Color(0, 123, 255));
        loginButton.setForeground(Color.WHITE);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(loginButton, gbc);
        loginButton.addActionListener(e -> handleLogin());

        frame.add(panel);
        frame.setVisible(true);
    }

    //Handle login
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                    "Please enter both username and password.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Use LoginService (with lockout + maintenance logic)
        User user = LoginService.login(username, password);

        if (user == null) {
            // get the detailed reason from LoginService
            String detail = LoginService.getLastLoginError();
            if (detail == null || detail.isBlank()) {
                detail = "Login failed. Please try again.";
            }

            JOptionPane.showMessageDialog(
                    frame,
                    detail,
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // store globally if you want
        LoginService.setLoggedInUser(user);

        JOptionPane.showMessageDialog(frame,
                "Login successful! Welcome, " + user.getUsername(),
                "Login Successful",
                JOptionPane.INFORMATION_MESSAGE);

        frame.dispose();

        AdminService adminService = new AdminService(user);

        switch (user.getRole().toLowerCase()) {
            case "admin" -> new AdminDashboardGUI(user, adminService);
            case "instructor" -> new InstructorDashboardGUI(user, adminService);
            case "student" -> new StudentDashboardGUI(user, adminService);
            default -> JOptionPane.showMessageDialog(null, "Unknown role!");
        }
    }

    //Main
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ERPAppGUI app = new ERPAppGUI();
            app.createAndShowGUI();
        });
    }
}





