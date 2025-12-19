package com.Arijit_Aditya.erp.ui.swing;

import com.Arijit_Aditya.erp.auth.User;
import com.Arijit_Aditya.erp.services.AdminService;

import javax.swing.*;
import java.awt.*;

public class ToggleMaintenanceModeGUI extends JFrame {

    private final User loggedInUser;
    private final AdminService adminService;

    public ToggleMaintenanceModeGUI(User user, AdminService adminService) {
        this.loggedInUser = user;
        this.adminService = adminService;
        createUI();
    }

    private void createUI() {
        setTitle("Toggle Maintenance Mode");
        setSize(450, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Header
        JLabel header = new JLabel("Maintenance Mode Control", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(header, BorderLayout.NORTH);

        // Status Label
        JLabel statusLabel = new JLabel(
                "Current status: " + (adminService.isMaintenanceMode() ? "ON" : "OFF"),
                SwingConstants.CENTER
        );
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        add(statusLabel, BorderLayout.CENTER);

        // Buttons Panel
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton toggleBtn = new JButton("Toggle Maintenance Mode");
        toggleBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        toggleBtn.setBackground(new Color(0, 153, 153));
        toggleBtn.setForeground(Color.WHITE);

        toggleBtn.addActionListener(e -> {
            adminService.toggleMaintenanceMode();
            statusLabel.setText("Current status: " + (adminService.isMaintenanceMode() ? "ON" : "OFF"));
            JOptionPane.showMessageDialog(this,
                    "Maintenance Mode is now " + (adminService.isMaintenanceMode() ? "ON" : "OFF"),
                    "Status Updated",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        JButton backBtn = new JButton("← Back to Dashboard");
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        backBtn.addActionListener(e -> {
            new AdminDashboardGUI(loggedInUser, adminService);
            dispose();
        });

        btnPanel.add(toggleBtn);
        btnPanel.add(backBtn);

        add(btnPanel, BorderLayout.SOUTH);

        setVisible(true);
    }
}



