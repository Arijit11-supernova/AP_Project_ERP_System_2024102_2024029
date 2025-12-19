package com.Arijit_Aditya.erp.ui.swing;

import com.Arijit_Aditya.erp.auth.User;
import com.Arijit_Aditya.erp.services.AdminService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DropSectionGUI {

    private final User loggedInUser;
    private final AdminService adminService;
    private JFrame frame;
    private JComboBox<String> enrolledDropdown;

    public DropSectionGUI(User user, AdminService adminService) {
        this.loggedInUser = user;
        this.adminService = adminService;
        createUI();
    }

    private void createUI() {
        frame = new JFrame("Drop a Section");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLocationRelativeTo(null);

        // Background gradient
        JPanel background = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0,0,new Color(58,123,213), getWidth(), getHeight(), new Color(58,96,115));
                g2.setPaint(gp);
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };
        background.setLayout(new BorderLayout());

        // Header
        JLabel header = new JLabel("Drop a Section", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 30));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(30,0,30,0));
        background.add(header, BorderLayout.NORTH);

        // Center form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12,12,12,12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Select enrolled section to drop:"), gbc);

        gbc.gridx = 1;
        enrolledDropdown = new JComboBox<>();
        refreshEnrolled();
        form.add(enrolledDropdown, gbc);

        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);

        JButton dropBtn = createStyledButton("Drop Section");
        dropBtn.addActionListener(e -> handleDrop());
        JButton backBtn = createStyledButton("← Back to Dashboard");
        backBtn.addActionListener(e -> {
            new StudentDashboardGUI(loggedInUser, adminService);
            frame.dispose();
        });

        btnPanel.add(dropBtn);
        btnPanel.add(backBtn);
        form.add(btnPanel, gbc);

        background.add(form, BorderLayout.CENTER);
        frame.setContentPane(background);
        frame.setVisible(true);
    }

    private void refreshEnrolled() {
        enrolledDropdown.removeAllItems();
        List<String> enrolled = adminService.getStudentEnrollments(loggedInUser.getUsername());
        if (enrolled.isEmpty()) {
            enrolledDropdown.addItem("No enrolled sections");
        } else {
            for (String key : enrolled) {
                // format: courseId:section
                enrolledDropdown.addItem(key);
            }
        }
    }

    private void handleDrop() {
        String sel = (String) enrolledDropdown.getSelectedItem();
        if (sel == null || sel.equals("No enrolled sections")) {
            JOptionPane.showMessageDialog(frame, "No section selected to drop.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] parts = sel.split(":");
        if (parts.length < 2) {
            JOptionPane.showMessageDialog(frame, "Invalid selection.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String courseId = parts[0];
        String section = parts[1];

        boolean ok = adminService.dropSection(loggedInUser.getUsername(), courseId, section);
        if (ok) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Dropped " + sel + " successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );
            refreshEnrolled();
        } else {
            JOptionPane.showMessageDialog(
                    frame,
                    "Cannot drop this section because final grades have already been computed\n" +
                            "or grades have been assigned.",
                    "Drop Not Allowed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private JButton createStyledButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 16));
        b.setBackground(new Color(255,255,255,230));
        b.setForeground(new Color(0,77,64));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(10,18,10,18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}


