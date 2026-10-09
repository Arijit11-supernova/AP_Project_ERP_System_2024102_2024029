-- =====================================================================
-- University ERP - Database Schema
--   auth_db : authentication & login (credentials, lockout)
--   erp_db  : academic data (students, courses, sections, grades, settings)
--
-- Run:  mysql -u root -p < database/schema.sql
-- WARNING: drops and recreates all tables.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS auth_db;
CREATE DATABASE IF NOT EXISTS erp_db;

-- ---------------------------------------------------------------------
-- auth_db
-- ---------------------------------------------------------------------
USE auth_db;

DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id              INT            NOT NULL AUTO_INCREMENT,
    username        VARCHAR(50)    NOT NULL UNIQUE,
    password_hash   VARCHAR(255),
    role            ENUM('Admin','Instructor','Student') NOT NULL,
    status          ENUM('Active','Blocked') DEFAULT 'Active',
    last_login      DATETIME       NULL,
    failed_attempts INT            DEFAULT 0,
    locked_until    DATETIME       NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- erp_db
-- ---------------------------------------------------------------------
USE erp_db;

DROP TABLE IF EXISTS grades;
DROP TABLE IF EXISTS enrollments;
DROP TABLE IF EXISTS sections;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS instructors;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS settings;

-- Mirrors auth_db.users (same ids); role stored in UPPERCASE here.
CREATE TABLE users (
    id            INT          NOT NULL AUTO_INCREMENT,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    role          ENUM('ADMIN','INSTRUCTOR','STUDENT') NOT NULL,
    status        ENUM('Active','Blocked') DEFAULT 'Active',
    last_login    DATETIME     NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE students (
    user_id INT          NOT NULL,
    roll_no VARCHAR(20)  NOT NULL,
    program VARCHAR(50),
    year    INT,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_students_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE instructors (
    user_id    INT          NOT NULL,
    department VARCHAR(50),
    PRIMARY KEY (user_id),
    CONSTRAINT fk_instructors_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE courses (
    course_id VARCHAR(20)  NOT NULL,
    title     VARCHAR(100) NOT NULL,
    credits   INT          NOT NULL,
    PRIMARY KEY (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Stores instructor-defined component weights and quiz/assignment counts.
CREATE TABLE sections (
    section_id        INT          NOT NULL AUTO_INCREMENT,
    course_id         VARCHAR(20),
    section_name      VARCHAR(50)  NOT NULL,
    instructor_id     INT,
    day_time          VARCHAR(50),
    room              VARCHAR(50),
    capacity          INT,
    quiz_weight       INT NOT NULL DEFAULT 0,
    assignment_weight INT NOT NULL DEFAULT 0,
    midsem_weight     INT NOT NULL DEFAULT 0,
    endsem_weight     INT NOT NULL DEFAULT 0,
    quiz_count        INT NOT NULL DEFAULT 0,
    assignment_count  INT NOT NULL DEFAULT 0,
    semester          VARCHAR(20),
    year              INT,
    PRIMARY KEY (section_id),
    CONSTRAINT fk_sections_course
        FOREIGN KEY (course_id) REFERENCES courses(course_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_sections_instructor
        FOREIGN KEY (instructor_id) REFERENCES instructors(user_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE enrollments (
    student_id INT         NOT NULL,
    section_id INT         NOT NULL,
    status     VARCHAR(20) NOT NULL,
    PRIMARY KEY (student_id, section_id),
    CONSTRAINT fk_enrollments_student
        FOREIGN KEY (student_id) REFERENCES students(user_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_enrollments_section
        FOREIGN KEY (section_id) REFERENCES sections(section_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Component-wise marks (summed over all quizzes / assignments) + final score.
-- (course_id, section, student_username) is the logical key used by
-- ON DUPLICATE KEY UPDATE in addStudentScore.
CREATE TABLE grades (
    id                  INT          NOT NULL AUTO_INCREMENT,
    course_id           VARCHAR(20)  NOT NULL,
    section             VARCHAR(50)  NOT NULL,
    student_username    VARCHAR(50)  NOT NULL,
    quiz_obtained       DOUBLE       DEFAULT 0,
    quiz_max            DOUBLE       DEFAULT 0,
    assignment_obtained DOUBLE       DEFAULT 0,
    assignment_max      DOUBLE       DEFAULT 0,
    midsem_obtained     DOUBLE       DEFAULT 0,
    midsem_max          DOUBLE       DEFAULT 0,
    endsem_obtained     DOUBLE       DEFAULT 0,
    endsem_max          DOUBLE       DEFAULT 0,
    score               INT          NOT NULL,
    finalized           TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uq_grade_course_section_student (course_id, section, student_username),
    CONSTRAINT fk_grades_course
        FOREIGN KEY (course_id) REFERENCES courses(course_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- System-wide settings (e.g. maintenance mode).
CREATE TABLE settings (
    setting_key   VARCHAR(50)  NOT NULL,
    setting_value VARCHAR(50)  NOT NULL,
    PRIMARY KEY (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO settings (setting_key, setting_value) VALUES ('maintenance_mode', 'false');
