package com.Arijit_Aditya.erp.auth;

import java.util.Scanner;

public class AuthTest {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter username: ");
        String username = sc.nextLine();

        System.out.print("Enter password: ");
        String password = sc.nextLine();

        User user = AuthService.login(username, password);

        if (user != null) {
            System.out.println("Login successful!");
            System.out.println("Username: " + user.getUsername());
            System.out.println("Role: " + user.getRole());
        } else {
            System.out.println("Login failed!");
        }
    }
}
