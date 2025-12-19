package com.Arijit_Aditya.erp;

import org.mindrot.jbcrypt.BCrypt;

public class GenerateHash {
    public static void main(String[] args) {
        // Plain text passwords
        String adminPassword = "admin123";
        String instructorPassword = "instr123";
        String student1Password = "student123";
        String student2Password = "student234";

        // Generate BCrypt hashes
        String adminHash = BCrypt.hashpw(adminPassword, BCrypt.gensalt());
        String instructorHash = BCrypt.hashpw(instructorPassword, BCrypt.gensalt());
        String student1Hash = BCrypt.hashpw(student1Password, BCrypt.gensalt());
        String student2Hash = BCrypt.hashpw(student2Password, BCrypt.gensalt());

        // Print the hashes
        System.out.println("Admin password hash: " + adminHash);
        System.out.println("Instructor password hash: " + instructorHash);
        System.out.println("Student1 password hash: " + student1Hash);
        System.out.println("Student2 password hash: " + student2Hash);
    }
}
