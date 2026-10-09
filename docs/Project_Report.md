# University ERP System – Project Report

**Course:** Application Programming
**Team:** Aditya Dev (2024029), Arijit Chowdhary (2024102)

## 1. Overview

A desktop University ERP built with Java Swing and MySQL that manages student enrollment, courses, sections, grading and administration. It supports three roles — Admin, Instructor and Student — each restricted to its own permissions. The code follows a layered design separating UI, business logic and database access. Passwords are bcrypt-hashed and accounts lock after repeated failed logins.

## 2. Features

### 2.1 Student
- View available courses and sections
- Register for sections with available seats
- Drop sections before the deadline
- View component-wise grades
- Download transcript (CSV)

### 2.2 Instructor
- View only their assigned sections
- Define the number of quizzes and assignments
- Enter marks for multiple quizzes and assignments, midsem and endsem
- Update marks later if required
- Compute final scores
- View student performance statistics

### 2.3 Admin
- Add users (students, instructors, admins) and delete them from both databases
- Create courses and sections
- Assign instructors to sections
- Toggle maintenance mode
- View reports

## 3. Final Score Calculation

Each section stores four instructor-defined weights (quiz, assignment, midsem, endsem) summing to 100. For each component:

    component score = (total obtained / total max) × component weight

Quiz and assignment totals are summed across all quizzes/assignments. The final score is the sum of the four component scores.

## 4. Role-Based Access Control

- **Admin:** full system access.
- **Instructor:** can view and modify only their own sections.
- **Student:** can view and modify only their own data.

Unauthorized actions show a warning in the UI.

## 5. Maintenance Mode

Toggled by the admin. While ON, students and instructors can log in and view data, but all write operations (registration, dropping, grade entry/editing) are blocked through a central maintenance check, and a banner is shown in the UI.

## 6. Database Design

Two databases (full DDL in `database/schema.sql`):

**auth_db**
- `users(id, username, password_hash, role, status, last_login, failed_attempts, locked_until)`

**erp_db**
- `users(id, username, password_hash, role, status, last_login)` — mirrors auth_db.users
- `students(user_id, roll_no, program, year)`
- `instructors(user_id, department)`
- `courses(course_id, title, credits)`
- `sections(section_id, course_id, section_name, instructor_id, day_time, room, capacity, quiz_weight, assignment_weight, midsem_weight, endsem_weight, quiz_count, assignment_count, semester, year)`
- `enrollments(student_id, section_id, status)`
- `grades(id, course_id, section, student_username, quiz/assignment/midsem/endsem obtained & max, score, finalized)`
- `settings(setting_key, setting_value)`

Foreign keys enforce integrity between users, students, instructors, courses, sections and enrollments.

## 7. Extra Features

- Login lockout after 5 failed attempts, with automatic unlock
- CSV export for transcripts and reports
- Dynamic quiz and assignment counts per section
- Score update feature for instructors
- Interactive UI with animations and gradients

## 8. Team Contribution

- **Aditya Dev:** UI design, Student module, database integration
- **Arijit Chowdhary:** Admin module, Instructor module, maintenance mode

## 9. Conclusion

The system implements secure authentication, role-based access control, academic management, data integrity and a usable UI, and meets the project specification.
