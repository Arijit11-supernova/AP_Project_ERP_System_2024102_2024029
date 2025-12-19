package com.Arijit_Aditya.erp.ui.swing;

import com.Arijit_Aditya.erp.auth.User;
import com.Arijit_Aditya.erp.models.Course;
import com.Arijit_Aditya.erp.services.AdminService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ViewCourseCatalogGUI {
    private final AdminService adminService;
    private final User loggedInUser;
    private JFrame frame;

    public ViewCourseCatalogGUI(User user, AdminService adminService) {
        this.loggedInUser = user;
        this.adminService = adminService;
        createUI();
    }

    private void createUI() {
        frame = new JFrame("IIITD - Course Catalog");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLocationRelativeTo(null);

        // Background Gradient Panel
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

        // Header
        JLabel header = new JLabel("Available Courses", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 30));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        backgroundPanel.add(header, BorderLayout.NORTH);

        // Course List Area
        JTextArea courseArea = new JTextArea();
        courseArea.setEditable(false);
        courseArea.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        courseArea.setForeground(Color.BLACK);
        courseArea.setBackground(new Color(255, 255, 255, 220));
        courseArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Fetch courses as List<Course>
        List<Course> courses = adminService.getAllCourses();
        StringBuilder sb = new StringBuilder();

        if (courses.isEmpty()) {
            sb.append("No courses available.");
        } else {
            for (Course course : courses) {
                sb.append("Course ID: ").append(course.getCourseId())
                        .append(" | Name: ").append(course.getCourseName())
                        .append("\n");

                //Load sections from DB via AdminService
                List<String> sections = adminService.getSectionsForCourse(course.getCourseId());

                if (sections.isEmpty()) {
                    sb.append("   No sections created yet.\n");
                } else {
                    for (String section : sections) {
                        // Get instructor username (or id) from DB
                        String instructor = adminService.getInstructorForSection(course.getCourseId(), section);

                        sb.append("   Section: ").append(section)
                                .append(" | Instructor: ")
                                .append(instructor != null && !instructor.isEmpty() ? instructor : "Not Assigned")
                                .append("\n");
                    }
                }
                sb.append("\n");
            }
        }

        courseArea.setText(sb.toString());
        JScrollPane scrollPane = new JScrollPane(courseArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 60, 20, 60));
        backgroundPanel.add(scrollPane, BorderLayout.CENTER);

        // Back Button
        JButton btnBack = createAnimatedButton("← Back to Dashboard");
        btnBack.addActionListener(e -> {
            frame.dispose();
            new StudentDashboardGUI(loggedInUser, adminService);
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.add(btnBack);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        backgroundPanel.add(bottomPanel, BorderLayout.SOUTH);

        frame.setContentPane(backgroundPanel);
        frame.setVisible(true);
    }

    // Animated Button
    private JButton createAnimatedButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btn.setBackground(new Color(255, 255, 255, 230));
        btn.setForeground(new Color(0, 77, 64));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

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
        });

        return btn;
    }

    // Color Animation
    private void animateColor(JButton button, Color from, Color to, int steps) {
        for (int i = 0; i < steps; i++) {
            int r = from.getRed() + (to.getRed() - from.getRed()) * i / steps;
            int g = from.getGreen() + (to.getGreen() - from.getGreen()) * i / steps;
            int b = from.getBlue() + (to.getBlue() - from.getBlue()) * i / steps;

            Color stepColor = new Color(r, g, b);
            SwingUtilities.invokeLater(() -> button.setBackground(stepColor));

            try {
                Thread.sleep(15);
            } catch (InterruptedException ignored) {}
        }
    }
}


