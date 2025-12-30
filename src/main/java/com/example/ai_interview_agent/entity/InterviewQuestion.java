package com.example.ai_interview_agent.entity;

import jakarta.persistence.*;

@Entity
public class InterviewQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String level; // EASY, MEDIUM, HARD

    @Column(length = 2000)
    private String question;

    private Long candidateId;
    private Long sessionId;

    @Column(length = 5000)
    private String answerText;

    private Double score;

    @Column(length = 2000)
    private String feedback;

    private String category; // Technical, Problem-Solving, Behavioral

    @Column(length = 4000)
    private String expectedKeyPoints; // JSON string of bullet points

    private String answerAudioPath;

    // Constructors
    public InterviewQuestion() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getExpectedKeyPoints() {
        return expectedKeyPoints;
    }

    public void setExpectedKeyPoints(String expectedKeyPoints) {
        this.expectedKeyPoints = expectedKeyPoints;
    }

    public String getAnswerAudioPath() {
        return answerAudioPath;
    }

    public void setAnswerAudioPath(String answerAudioPath) {
        this.answerAudioPath = answerAudioPath;
    }
}