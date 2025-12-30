package com.example.ai_interview_agent.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class InterviewSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private LocalDateTime startTime;

    private String candidateName;
    private String candidateEmail;

    @Enumerated(EnumType.STRING)
    private InterviewStatus status;

    private Double totalScore;

    private Long currentQuestionId;
    private int currentQuestionAttempts;

    // To track how many questions of each type asked, or just total.
    // Requirement: 3 Easy, 4 Medium, 2 Hard.
    // We can simply track the list of specific Question IDs asked to avoid
    // repetition
    @ElementCollection
    private List<Long> askedQuestionIds = new ArrayList<>();

    @Column(length = 5000)
    private String finalFeedback;

    public enum InterviewStatus {
        RUNNING, COMPLETED, ABORTED
    }

    // Constructors
    public InterviewSession() {
    }

    public InterviewSession(Long userId, LocalDateTime startTime, InterviewStatus status) {
        this.userId = userId;
        this.startTime = startTime;
        this.status = status;
        this.totalScore = 0.0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public InterviewStatus getStatus() {
        return status;
    }

    public void setStatus(InterviewStatus status) {
        this.status = status;
    }

    public Double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Double totalScore) {
        this.totalScore = totalScore;
    }

    public Long getCurrentQuestionId() {
        return currentQuestionId;
    }

    public void setCurrentQuestionId(Long currentQuestionId) {
        this.currentQuestionId = currentQuestionId;
    }

    public int getCurrentQuestionAttempts() {
        return currentQuestionAttempts;
    }

    public void setCurrentQuestionAttempts(int currentQuestionAttempts) {
        this.currentQuestionAttempts = currentQuestionAttempts;
    }

    public List<Long> getAskedQuestionIds() {
        return askedQuestionIds;
    }

    public void setAskedQuestionIds(List<Long> askedQuestionIds) {
        this.askedQuestionIds = askedQuestionIds;
    }

    public String getFinalFeedback() {
        return finalFeedback;
    }

    public void setFinalFeedback(String finalFeedback) {
        this.finalFeedback = finalFeedback;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public String getCandidateEmail() {
        return candidateEmail;
    }

    public void setCandidateEmail(String candidateEmail) {
        this.candidateEmail = candidateEmail;
    }
}
