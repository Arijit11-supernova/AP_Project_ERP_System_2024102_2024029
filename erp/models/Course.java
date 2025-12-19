package com.Arijit_Aditya.erp.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Course implements Serializable {
    private static final long serialVersionUID = 1L;

    private String courseId;
    private String courseName;
    private int credits;  // new field
    private List<String> sections;
    private Map<String, String> sectionInstructors; // sectionName -> instructorUsername

    // Constructor including credits
    public Course(String courseId, String courseName, int credits) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.credits = credits;
        this.sections = new ArrayList<>();
        this.sectionInstructors = new HashMap<>();
    }

    // Old constructor for backward compatibility (default credits = 0)
    public Course(String courseId, String courseName) {
        this(courseId, courseName, 0);
    }

    // Getters and setters
    public String getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public List<String> getSections() { return sections; }
    public void setSections(List<String> sections) { this.sections = sections; }

    // Assign instructor to a section
    public boolean assignInstructorToSection(String sectionName, String instructorUsername) {
        if (!sections.contains(sectionName)) return false;       // Section doesn’t exist
        sectionInstructors.put(sectionName, instructorUsername); // Allow overwrite if needed
        return true;
    }

    // Get instructor of a section
    public String getInstructorForSection(String sectionName) {
        return sectionInstructors.get(sectionName);
    }

    @Override
    public String toString() {
        return "Course ID: " + courseId + ", Name: " + courseName +
                ", Credits: " + credits +
                ", Sections: " + (sections.isEmpty() ? "None" : sections) +
                ", Section Instructors: " + (sectionInstructors.isEmpty() ? "None" : sectionInstructors);
    }
}


