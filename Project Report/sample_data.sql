-- ADMIN
INSERT INTO users_auth VALUES (1, 'admin1', 'ADMIN', 'hashed_pass', 'ACTIVE', NULL);

-- INSTRUCTOR
INSERT INTO users_auth VALUES (2, 'inst1', 'INSTRUCTOR', 'hashed_pass', 'ACTIVE', NULL);

-- STUDENTS
INSERT INTO users_auth VALUES (3, 'stu1', 'STUDENT', 'hashed_pass', 'ACTIVE', NULL);
INSERT INTO users_auth VALUES (4, 'stu2', 'STUDENT', 'hashed_pass', 'ACTIVE', NULL);

-- STUDENT TABLE
INSERT INTO students VALUES (3, '20241001', 'CSE', 2);
INSERT INTO students VALUES (4, '20241002', 'CSE', 2);

-- INSTRUCTOR TABLE
INSERT INTO instructors VALUES (2, 'CSE');

-- COURSE
INSERT INTO courses VALUES ('CSE101', 'Intro to Programming', 4);

-- SECTION
INSERT INTO sections VALUES (1, 'CSE101', 2, 'Mon 10-11', 'A101', 60, 'Winter', 2025);

-- ENROLLMENTS
INSERT INTO enrollments VALUES (3, 1, 'ENROLLED');
INSERT INTO enrollments VALUES (4, 1, 'ENROLLED');
