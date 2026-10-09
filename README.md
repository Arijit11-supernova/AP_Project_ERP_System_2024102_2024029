# University ERP System

A desktop University ERP built with **Java Swing** and **MySQL** for the *Application Programming* course. It manages enrollment, courses, sections, grading and administration for three roles — Admin, Instructor and Student — with role-based access control, bcrypt-hashed passwords, login lockout and a maintenance mode.

## Features

**Student** — browse the course catalog, register for sections with free seats, drop sections, view component-wise grades, export transcript as CSV.

**Instructor** — see only their assigned sections, set the number of quizzes and assignments, enter and update quiz / assignment / midsem / endsem marks, compute final scores, view section statistics.

**Admin** — add and delete users (in both databases), create courses and sections, assign instructors, toggle maintenance mode, view reports.

**Security & extras** — bcrypt password hashing, account lockout after 5 failed logins with automatic unlock, maintenance mode that blocks all write operations (with a UI banner), CSV report export.

## Final score calculation

Each section has instructor-defined weights (summing to 100):

```
score = (quiz_obtained / quiz_max)             * quiz_weight
      + (assignment_obtained / assignment_max) * assignment_weight
      + (midsem_obtained / midsem_max)         * midsem_weight
      + (endsem_obtained / endsem_max)         * endsem_weight
```

Quiz and assignment marks are summed over all quizzes/assignments in the section before dividing.

## Project structure

```
.
├── src/com/Arijit_Aditya/erp/
│   └── ui/swing/ERPAppGUI.java     # entry point (main)
├── lib/                            # MySQL Connector/J, jBCrypt jars
├── database/
│   ├── schema.sql                  # creates auth_db + erp_db and all tables
│   └── sample_data.sql             # demo users, courses, sections, marks
└── docs/
    ├── Project_Report.md
    ├── Test_Plan.md
    └── Test_Summary.md
```

## Setup

**Requirements:** Java JDK 8+, MySQL Server 8.0, any Java IDE (IntelliJ IDEA / Eclipse / VS Code).

1. **Create the databases and tables, then load sample data**

   ```bash
   mysql -u root -p < database/schema.sql
   mysql -u root -p < database/sample_data.sql
   ```

   `schema.sql` creates `auth_db` and `erp_db` itself and drops existing tables, so don't run it against data you want to keep.

2. **Configure the connection** in `DBConnection.java` (→ `erp_db`) and `AuthDBConnection.java` (→ `auth_db`):

   ```
   URL:      jdbc:mysql://localhost:3306/erp_db   (auth_db for AuthDBConnection)
   Username: root
   Password: <your MySQL password>
   ```

3. **Add the jars in `lib/` to the project classpath** and run `main()` in
   `src/com/Arijit_Aditya/erp/ui/swing/ERPAppGUI.java`. The login window should appear.

## Demo credentials

| Role       | Username     | Password   |
|------------|--------------|------------|
| Admin      | `admin_demo` | `admin123` |
| Instructor | `inst_demo`  | `inst123`  |
| Student    | `stud_demo1` | `stu123`   |
| Student    | `stud_demo2` | `stu123`   |

These are demo accounts for evaluation only.

## Testing

See [`docs/Test_Plan.md`](docs/Test_Plan.md) and [`docs/Test_Summary.md`](docs/Test_Summary.md).

## Team

| Name | Roll No. | Contribution |
|------|----------|--------------|
| Aditya Dev | 2024029 | UI design, Student module, database integration |
| Arijit Chowdhary | 2024102 | Admin module, Instructor module, maintenance mode |
