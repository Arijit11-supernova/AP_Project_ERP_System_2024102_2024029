package com.Arijit_Aditya.erp.ui.swing;

import com.Arijit_Aditya.erp.auth.User;
import com.Arijit_Aditya.erp.services.AdminService;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class InstructorViewSectionsGUI extends JFrame {
    private final User loggedInUser;
    private final AdminService adminService;
    private final JFrame parentFrame;

    public InstructorViewSectionsGUI(JFrame parent, User user, AdminService adminService) {
        this.loggedInUser = user;
        this.adminService = adminService;
        this.parentFrame = parent;

        setTitle("My Sections");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        setVisible(true);
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Title
        JLabel header = new JLabel("Sections Assigned to You", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 26));
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(header, BorderLayout.NORTH);

        // Text area to show sections
        JTextArea sectionArea = new JTextArea();
        sectionArea.setEditable(false);
        sectionArea.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        // Fetch only sections assigned to this instructor
        List<String> sections = adminService.getSectionsByInstructor(loggedInUser.getUsername());

        StringBuilder sb = new StringBuilder();
        if (sections == null || sections.isEmpty()) {
            sb.append("No courses or sections assigned to you yet.");
        } else {
            sb.append("Instructor: ").append(loggedInUser.getUsername()).append("\n\n");
            for (String s : sections) {
                sb.append("• ").append(s).append("\n");
            }
        }

        sectionArea.setText(sb.toString());
        add(new JScrollPane(sectionArea), BorderLayout.CENTER);

        // Back button
        JButton btnBack = new JButton("← Back to Dashboard");
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnBack.addActionListener(e -> {
            this.dispose();
            parentFrame.setVisible(true); // return to dashboard without creating a new one
        });
        add(btnBack, BorderLayout.SOUTH);
    }
}



