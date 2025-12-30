package com.example.ai_interview_agent.service;

import com.example.ai_interview_agent.entity.InterviewQuestion;
import com.example.ai_interview_agent.entity.InterviewSession;
import com.example.ai_interview_agent.repository.InterviewQuestionRepository;
import com.example.ai_interview_agent.repository.InterviewSessionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.example.ai_interview_agent.entity.HrRequirement;
import com.example.ai_interview_agent.repository.HrRequirementRepository;
import org.springframework.web.multipart.MultipartFile;

@Service
public class InterviewService {

    @Autowired
    private InterviewSessionRepository sessionRepository;

    @Autowired
    private InterviewQuestionRepository questionRepository;

    @Autowired
    private VoiceService voiceService;

    @Autowired
    private ResumeParserService resumeParserService;

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private HrRequirementRepository hrRequirementRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public InterviewSession getSession(Long sessionId) {
        return sessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));
    }

    public List<InterviewSession> getAllSessions() {
        return sessionRepository.findAll();
    }

    public InterviewSession startInterview(Long userId, String name, String email, MultipartFile resumeFile,
            Long requirementId) {
        System.out.println("Starting interview for user: " + userId + " Name: " + name);
        try {
            // 1. Create Session FIRST to get ID
            InterviewSession session = new InterviewSession(userId, LocalDateTime.now(),
                    InterviewSession.InterviewStatus.RUNNING);
            session.setCandidateName(name);
            session.setCandidateEmail(email);
            sessionRepository.save(session);

            // 2. Parse Resume
            String resumeText = resumeParserService.parseResume(resumeFile);

            // 3. Get HR Requirement
            HrRequirement hrRequirement;
            if (requirementId != null) {
                hrRequirement = hrRequirementRepository.findById(requirementId)
                        .orElseGet(this::createDefaultRequirement);
            } else {
                List<HrRequirement> requirements = hrRequirementRepository.findAll();
                hrRequirement = requirements.isEmpty() ? createDefaultRequirement() : requirements.get(0);
            }

            // 4. Generate Questions (Phase 1)
            String jsonContent = geminiService.generateQuestions(resumeText, hrRequirement);
            System.out.println("Gemini JSON: " + jsonContent);

            // 5. Parse JSON and Save Questions LINKED TO SESSION
            List<InterviewQuestion> questions = parseQuestionsJson(jsonContent, userId, session.getId());
            List<InterviewQuestion> savedQuestions = questionRepository.saveAll(questions); // Ensure IDs are set

            // 6. Trigger Async Audio Generation with Dedicated Folder
            // Clean candidate name for folder usage
            String safeName = (name != null ? name.replaceAll("[^a-zA-Z0-9]", "_") : "candidate");
            String sessionFolder = "audio_storage/" + safeName + "_" + session.getId();
            CompletableFuture.runAsync(() -> preGenerateAudio(savedQuestions, sessionFolder));

            return session;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to start interview: " + e.getMessage());
        }
    }

    public InterviewSession startTestInterview(Long userId) {
        // Simple test initialization without Gemini
        InterviewSession session = new InterviewSession(userId, LocalDateTime.now(),
                InterviewSession.InterviewStatus.RUNNING);
        return sessionRepository.save(session);
    }

    private void preGenerateAudio(List<InterviewQuestion> questions, String audioDir) {
        // Ensure directory exists
        new File(audioDir).mkdirs();

        for (InterviewQuestion q : questions) {
            String filename = "q_" + q.getId() + ".wav";
            File f = new File(audioDir, filename);
            if (!f.exists()) {
                try {
                    System.out.println("Pre-generating audio for Q: " + q.getId() + " at " + f.getAbsolutePath());
                    voiceService.textToSpeech(q.getQuestion(), f.getAbsolutePath());
                } catch (Exception e) {
                    System.err.println("Failed to generate audio for Q " + q.getId());
                }
            }
        }
    }

    private HrRequirement createDefaultRequirement() {
        HrRequirement hr = new HrRequirement();
        hr.setRole("Software Engineer");
        hr.setRequiredSkills(List.of("Java", "Communication", "Problem Solving"));
        return hr;
    }

    private List<InterviewQuestion> parseQuestionsJson(String jsonContent, Long userId, Long sessionId) {
        List<InterviewQuestion> questions = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(jsonContent);
            JsonNode questionsNode = root.path("questions");
            if (questionsNode.isArray()) {
                for (JsonNode node : questionsNode) {
                    InterviewQuestion q = new InterviewQuestion();
                    q.setQuestion(node.path("question_text").asText());
                    q.setCategory(node.path("category").asText());
                    q.setLevel(node.path("difficulty").asText().toUpperCase());
                    q.setCandidateId(userId);
                    q.setSessionId(sessionId); // Set Session ID

                    // Store key points as JSON string
                    if (node.has("expected_key_points")) {
                        q.setExpectedKeyPoints(node.path("expected_key_points").toString());
                    }

                    questions.add(q);
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse Gemini JSON: " + e.getMessage());
        }
        return questions;
    }

    public InterviewQuestion getNextQuestion(Long sessionId) {
        InterviewSession session = getSession(sessionId);

        if (session.getStatus() != InterviewSession.InterviewStatus.RUNNING) {
            return null;
        }

        // Fetch questions strictly for THIS session
        List<InterviewQuestion> candidates = new ArrayList<>(questionRepository.findBySessionId(sessionId));

        // Sort by ID to preserve Gemini's generation order
        candidates.sort((q1, q2) -> q1.getId().compareTo(q2.getId()));

        for (InterviewQuestion q : candidates) {
            if (!session.getAskedQuestionIds().contains(q.getId())) {
                session.setCurrentQuestionId(q.getId());
                session.setCurrentQuestionAttempts(0);
                sessionRepository.save(session);

                return q;
            }
        }

        // No more questions
        session.setStatus(InterviewSession.InterviewStatus.COMPLETED);
        sessionRepository.save(session);
        return null;
    }

    public String processAnswer(Long sessionId, String audioFilePath) {
        InterviewSession session = sessionRepository.findById(sessionId).orElseThrow();

        if (session.getCurrentQuestionId() != null) {
            InterviewQuestion q = questionRepository.findById(session.getCurrentQuestionId()).orElseThrow();

            // 1. FAST: Just save the audio path
            q.setAnswerAudioPath(audioFilePath);
            q.setAnswerText("AUDIO_PENDING"); // Marker
            questionRepository.save(q);

            session.getAskedQuestionIds().add(session.getCurrentQuestionId());
            session.setCurrentQuestionId(null);
            session.setCurrentQuestionAttempts(0);
            sessionRepository.save(session);

            // 2. Return Generic Filler
            return "NEXT:" + getRandomFiller();
        }

        return "NEXT:Okay.";
    }

    private String getRandomFiller() {
        String[] fillers = {
                "Thank you.",
                "Got it, moving on.",
                "Okay, next question.",
                "Thanks for sharing that.",
                "Noted. Let's proceed."
        };
        return fillers[(int) (Math.random() * fillers.length)];
    }

    // NEW: End Interview & Process Everything
    // NEW: End Interview & Process Everything
    public void endInterview(Long sessionId) {
        System.out.println("Ending Interview & Processing Results for Session: " + sessionId);
        InterviewSession session = sessionRepository.findById(sessionId).orElseThrow();
        session.setStatus(InterviewSession.InterviewStatus.COMPLETED);
        sessionRepository.save(session);

        List<Long> askedIds = new ArrayList<>(session.getAskedQuestionIds());

        // Process in background
        CompletableFuture.runAsync(() -> processInterviewResults(sessionId, askedIds));
    }

    private void processInterviewResults(Long sessionId, List<Long> askedQuestionIds) {
        try {
            List<InterviewQuestion> questions = questionRepository.findAllById(askedQuestionIds);
            List<String> allEvaluations = new ArrayList<>();

            double totalScore = 0;
            int count = 0;

            for (InterviewQuestion q : questions) {
                if (q.getAnswerAudioPath() != null && new File(q.getAnswerAudioPath()).exists()) {
                    // 1. STT
                    System.out.println("Transcribing Q: " + q.getId());
                    String text = voiceService.speechToText(q.getAnswerAudioPath());
                    q.setAnswerText(text);

                    // 2. Evaluate (Phase 2)
                    System.out.println("Evaluating Q: " + q.getId());
                    String evalJson = geminiService.evaluateAnswer(q.getQuestion(), text, q.getExpectedKeyPoints());

                    // Parse Score from JSON
                    try {
                        JsonNode node = objectMapper.readTree(evalJson);
                        double score = node.path("score").asDouble();
                        String feedback = node.path("brief_feedback").asText();

                        q.setScore(score);
                        q.setFeedback(feedback);

                        totalScore += score;
                        count++;

                        allEvaluations.add(evalJson);
                    } catch (Exception e) {
                        System.err.println("Failed to parse evaluation for Q " + q.getId() + ": " + e.getMessage());
                    }
                    questionRepository.save(q);
                }
            }

            // 3. Summary (Phase 3)
            // Combine all evaluations into one big JSON array string
            String combinedJson = "[" + String.join(",", allEvaluations) + "]";
            String summaryJson = geminiService.generateInterviewSummary(combinedJson);

            // Store summary in session (we need a field for it, or just log it for now)
            System.out.println("FINAL SUMMARY:\n" + summaryJson);
            // Ideally we save this to a new field in Session 'reportJson'

            InterviewSession session = sessionRepository.findById(sessionId).orElseThrow();
            session.setTotalScore(totalScore);
            sessionRepository.save(session);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}