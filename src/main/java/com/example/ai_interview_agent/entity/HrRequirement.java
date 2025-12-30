package com.example.ai_interview_agent.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class HrRequirement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String role;
    
    @ElementCollection
    private List<String> requiredSkills;
    
    private String difficultyPolicy; // EASY_MEDIUM_HARD
    
    // Constructors
    public HrRequirement() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills; }
    
    public String getDifficultyPolicy() { return difficultyPolicy; }
    public void setDifficultyPolicy(String difficultyPolicy) { this.difficultyPolicy = difficultyPolicy; }
}