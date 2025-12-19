package com.Arijit_Aditya.erp.main;
import com.Arijit_Aditya.erp.auth.LoginService;
import com.Arijit_Aditya.erp.auth.User;
import com.Arijit_Aditya.erp.ui.admin.AdminUI;
import com.Arijit_Aditya.erp.ui.instructor.InstructorUI;
import com.Arijit_Aditya.erp.ui.student.StudentUI;
import java.util.Scanner;

public class ERPApp {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("=== Welcome to ERP System ===");
            System.out.println("1. Login as Admin");
            System.out.println("2. Login as Instructor");
            System.out.println("3. Login as Student");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1, 2, 3 -> {
                    User user = LoginService.login();
                    if(user != null){
                        switch(user.getRole()){
                            case "Admin" -> new AdminUI().showMenu(user);
                            case "Instructor" -> new InstructorUI().showMenu(user);
                            case "Student" -> new StudentUI().showMenu(user);
                            default -> System.out.println("Unknown role!");
                        }
                    }
                    else{
                        System.out.println("Login failed. Returning to main menu.\n");
                    }
                }
                case 4 -> {
                    System.out.println("Exiting...");
                    System.exit(0);
                }
                default -> System.out.println("Invalid choice!");
            }
        }
    }
}

