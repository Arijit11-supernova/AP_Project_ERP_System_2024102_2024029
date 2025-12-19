package com.Arijit_Aditya.erp.ui.swing;

import com.Arijit_Aditya.erp.auth.User;
import com.Arijit_Aditya.erp.services.AdminService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DeleteUserGUI extends JFrame {

    private final User loggedInUser;
    private final AdminService adminService;

    public DeleteUserGUI(User user, AdminService adminService) {
        this.loggedInUser = user;
        this.adminService = adminService;

        setTitle("Delete User");
        setSize(600, 360);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        setVisible(true);

        // Automatically show all users once when screen opens
        SwingUtilities.invokeLater(this::showAllUsersPopup);
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridLayout(5, 1, 15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("Delete User", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JTextField usernameField = new JTextField();
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        usernameField.setBorder(BorderFactory.createTitledBorder("Username"));

        JButton viewBtn = new JButton("View All Users");
        viewBtn.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        JButton deleteBtn = new JButton("Delete User");
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JButton backBtn = new JButton("Back to Dashboard");
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        //View All Users
        viewBtn.addActionListener(e -> showAllUsersPopup());

        //Delete logic
        deleteBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();

            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a username.");
                return;
            }

            if (username.equalsIgnoreCase(loggedInUser.getUsername())) {
                JOptionPane.showMessageDialog(this, "You cannot delete yourself!");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete user: " + username + " ?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            boolean success = adminService.deleteUserCompletely(username);

            if (success) {
                JOptionPane.showMessageDialog(this,
                        " User '" + username + "' deleted successfully.");
                usernameField.setText("");
                // refresh list after delete
                showAllUsersPopup();
            } else {
                JOptionPane.showMessageDialog(this,
                        " User could not be deleted.\n" +
                                "They may not exist, or are still assigned to sections / have active data.",
                        "Delete Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        backBtn.addActionListener(e -> {
            dispose();
            new AdminDashboardGUI(loggedInUser, adminService);
        });

        panel.add(title);
        panel.add(usernameField);
        panel.add(viewBtn);
        panel.add(deleteBtn);
        panel.add(backBtn);

        add(panel);
    }

    //Helper: popup listing all users
    private void showAllUsersPopup() {
        List<String> users = adminService.getAllErpUsers();

        if (users == null || users.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No users found in ERP system.",
                    "All Users",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (String u : users) {
            sb.append(u).append("\n");
        }

        JTextArea area = new JTextArea(sb.toString(), 15, 40);
        area.setEditable(false);
        area.setFont(new Font("Consolas", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(area);

        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "All Users in ERP System",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}


