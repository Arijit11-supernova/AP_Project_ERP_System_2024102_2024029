-- =====================================================================
-- University ERP - Sample Data
-- Run AFTER schema.sql:  mysql -u root -p < database/sample_data.sql
--
-- Demo logins (bcrypt hashes below match these passwords):
--   admin_demo / admin123
--   inst_demo  / inst123
--   stud_demo1 / stu123
--   stud_demo2 / stu123
-- =====================================================================

-- ---------- auth_db ----------
USE auth_db;

INSERT INTO users (id, username, password_hash, role, status) VALUES
(1, 'admin_demo', '$2a$10$2TVXUygUiKL3T7EEjf6lOOSf6Dqu39Z2EX5YlG3pyalNIXQfMIYYq', 'Admin',      'Active'),
(2, 'inst_demo',  '$2a$10$ur7fGUs8pVF9GC5fZ8KNgOQiyExM.I/VfdyqOR40Stj510flml.EC', 'Instructor', 'Active'),
(3, 'stud_demo1', '$2a$10$L19ExjA.HxnupzA2JLn8V.yrdJ8lc7B.f6mZ4x6WXxoVOU3ZcuZeW', 'Student',    'Active'),
(4, 'stud_demo2', '$2a$10$L19ExjA.HxnupzA2JLn8V.yrdJ8lc7B.f6mZ4x6WXxoVOU3ZcuZeW', 'Student',    'Active');

-- ---------- erp_db ----------
USE erp_db;

INSERT INTO users (id, username, password_hash, role, status) VALUES
(1, 'admin_demo', '$2a$10$2TVXUygUiKL3T7EEjf6lOOSf6Dqu39Z2EX5YlG3pyalNIXQfMIYYq', 'ADMIN',      'Active'),
(2, 'inst_demo',  '$2a$10$ur7fGUs8pVF9GC5fZ8KNgOQiyExM.I/VfdyqOR40Stj510flml.EC', 'INSTRUCTOR', 'Active'),
(3, 'stud_demo1', '$2a$10$L19ExjA.HxnupzA2JLn8V.yrdJ8lc7B.f6mZ4x6WXxoVOU3ZcuZeW', 'STUDENT',    'Active'),
(4, 'stud_demo2', '$2a$10$L19ExjA.HxnupzA2JLn8V.yrdJ8lc7B.f6mZ4x6WXxoVOU3ZcuZeW', 'STUDENT',    'Active');

INSERT INTO instructors (user_id, department) VALUES
(2, 'Computer Science');

INSERT INTO students (user_id, roll_no, program, year) VALUES
(3, '2024001', 'CSE', 2),
(4, '2024002', 'CSE', 2);

INSERT INTO courses (course_id, title, credits) VALUES
('CS101', 'Introduction to Programming', 4),
('MA101', 'Engineering Mathematics',     3),
('EE101', 'Basic Electronics',           3);

-- Weights: CS101 = 20/20/30/30, MA101 = 10/20/30/40
INSERT INTO sections
(section_id, course_id, section_name, instructor_id, day_time, room, capacity,
 quiz_weight, assignment_weight, midsem_weight, endsem_weight,
 quiz_count, assignment_count, semester, year) VALUES
(1, 'CS101', 'A', 2, 'Mon 10:00-11:30', 'R101', 60, 20, 20, 30, 30, 1, 1, 'Monsoon', 2025),
(2, 'CS101', 'B', 2, 'Wed 11:00-12:30', 'R102', 60, 20, 20, 30, 30, 1, 1, 'Monsoon', 2025),
(3, 'MA101', 'A', 2, 'Tue 09:00-10:30', 'R201', 60, 10, 20, 30, 40, 1, 1, 'Monsoon', 2025);

INSERT INTO enrollments (student_id, section_id, status) VALUES
(3, 1, 'Enrolled'),
(4, 1, 'Enrolled'),
(3, 3, 'Enrolled');

-- CS101-A marks. score = sum of (obtained/max) * weight:
--   stud_demo1: 18 + 18 + 22 + 21   = 79
--   stud_demo2: 15 + 16 + 18 + 19.5 = 68.5 -> 69
INSERT INTO grades
(course_id, section, student_username,
 quiz_obtained, quiz_max, assignment_obtained, assignment_max,
 midsem_obtained, midsem_max, endsem_obtained, endsem_max,
 score, finalized) VALUES
('CS101', 'A', 'stud_demo1', 18, 20, 45, 50, 22, 30, 70, 100, 79, 0),
('CS101', 'A', 'stud_demo2', 15, 20, 40, 50, 18, 30, 65, 100, 69, 0);
