package com.Arijit_Aditya.erp.ui.swing;

import com.Arijit_Aditya.erp.auth.User;
import com.Arijit_Aditya.erp.services.AdminService;
import javax.swing.*;
import java.awt.*;

public class AddUserGUI {

    private JFrame frame;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JComboBox<String> roleDropdown;

    private final User loggedInUser;
    private final AdminService adminService;

    public AddUserGUI(User user, AdminService adminService) {
        this.loggedInUser = user;
        this.adminService = adminService;
        createUI();
    }

    private void createUI() {
        frame = new JFrame("Add User");
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel header = new JLabel("Add New User", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 26));
        header.setForeground(new Color(0, 102, 102));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(header, gbc);

        // Username
        gbc.gridy++;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Username:"), gbc);

        txtUsername = new JTextField();
        gbc.gridx = 1;
        panel.add(txtUsername, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Password:"), gbc);

        txtPassword = new JPasswordField();
        gbc.gridx = 1;
        panel.add(txtPassword, gbc);

        // Role
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Role:"), gbc);

        roleDropdown = new JComboBox<>(new String[]{"Admin", "Instructor", "Student"});
        gbc.gridx = 1;
        panel.add(roleDropdown, gbc);

        // Button: Add User
        JButton btnAdd = new JButton("Add User");
        btnAdd.setBackground(new Color(0, 153, 153));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 18));

        btnAdd.addActionListener(e -> handleAddUser());

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        panel.add(btnAdd, gbc);

        // Back Button
        JButton btnBack = new JButton("← Back to Dashboard");
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        btnBack.addActionListener(e -> {
            frame.dispose();
            new AdminDashboardGUI(loggedInUser, adminService);
        });

        gbc.gridy++;
        panel.add(btnBack, gbc);

        frame.add(panel);
        frame.setVisible(true);
    }

    private void handleAddUser() {
        //  Maintenance Mode check
        if (adminService.isMaintenanceMode()) {
            JOptionPane.showMessageDialog(frame,
                    "Cannot add user: Maintenance Mode is ON!",
                    "Maintenance Mode",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String role = (String) roleDropdown.getSelectedItem();

        if (username.isEmpty() || password.isEmpty() || role.isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                    "All fields are required!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean added = adminService.addUser(username, password, role);

        if (added) {
            JOptionPane.showMessageDialog(frame,
                    "User \"" + username + "\" added successfully as " + role + "!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            txtUsername.setText("");
            txtPassword.setText("");
            roleDropdown.setSelectedIndex(0);
        } else {
            JOptionPane.showMessageDialog(frame,
                    "Failed to add user. It may already exist or input was invalid.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}



