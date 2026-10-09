# University ERP – Test Plan

Verifies that all features work correctly for Student, Instructor and Admin users.

**Environment:** Windows 11 Pro · Java 8+ · MySQL 8.0 · IntelliJ IDEA, MySQL Shell
**Data:** `database/sample_data.sql` (demo accounts in the README)

| # | Module | Test case | Steps | Expected result |
|---|--------|-----------|-------|-----------------|
| 1 | Login | Invalid login | Enter wrong username/password | "Invalid credentials" shown |
| 2 | Login | Correct role dashboard | Log in as student, instructor, admin | Matching dashboard opens for each |
| 3 | Student | View courses | Log in as student → open catalog | Courses shown in a table |
| 4 | Student | Register course | Select section with free seats → Register | Success message; section in timetable |
| 5 | Student | Duplicate registration | Register the same section twice | Blocked with "Already registered" |
| 6 | Student | Drop course | Drop a registered section | Section removed |
| 7 | Instructor | View assigned sections | Log in as instructor → My Sections | Only assigned sections visible |
| 8 | Instructor | Enter scores | Enter quiz/assignment/midsem/endsem marks → Save | Scores saved |
| 9 | Instructor | Compute final scores | Press Compute | Score matches weighted formula |
| 10 | Admin | Add user | Add a student/instructor | User created in both databases |
| 11 | Admin | Delete user | Enter username → Delete | User removed from both databases |
| 12 | Admin | Maintenance mode | Turn maintenance ON → student tries to register | Action blocked with message; banner visible |
| 13 | Security | Login lockout | Enter wrong password 5 times | Account locked message |
| 14 | Student | Full section | Register for a section at capacity | Blocked with "Section full" message |
| 15 | Security | Unauthorized access | Instructor tries to edit another instructor's section | Action denied with warning |

All tests were executed manually through the GUI.
