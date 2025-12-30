package com.example.ai_interview_agent.controller;

import com.example.ai_interview_agent.entity.InterviewQuestion;
import com.example.ai_interview_agent.entity.InterviewSession;
import com.example.ai_interview_agent.service.InterviewService;
import com.example.ai_interview_agent.service.VoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/interview")
@CrossOrigin("*") // Allow frontend access
public class InterviewController {

    @Autowired
    private InterviewService interviewService;

    @Autowired
    private VoiceService voiceService;

    // Temporary storage for audio
    private static final String AUDIO_DIR = "audio_storage";

    public InterviewController() {
        try {
            Files.createDirectories(Paths.get(AUDIO_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostMapping(value = "/start", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InterviewSession> startInterview(
            @RequestParam("userId") Long userId,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("resume") MultipartFile resume,
            @RequestParam(value = "requirementId", required = false) Long requirementId) {
        return ResponseEntity.ok(interviewService.startInterview(userId, name, email, resume, requirementId));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllSessions() {
        return ResponseEntity.ok(interviewService.getAllSessions());
    }

    @PostMapping("/test-start")
    public ResponseEntity<InterviewSession> startTestInterview(@RequestParam("userId") Long userId) {
        return ResponseEntity.ok(interviewService.startTestInterview(userId));
    }

    @GetMapping("/{sessionId}/next-question")
    public ResponseEntity<?> getNextQuestion(@PathVariable Long sessionId) {
        try {
            InterviewQuestion question = interviewService.getNextQuestion(sessionId);
            if (question == null) {
                // Check if session is completed
                return ResponseEntity.ok(Map.of("status", "COMPLETED", "message", "Interview Finished"));
            }
            return ResponseEntity.ok(question);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{sessionId}/question-audio")
    public ResponseEntity<Resource> getQuestionAudio(@PathVariable Long sessionId) {
        try {
            InterviewSession session = interviewService.getSession(sessionId);

            // Re-fetch current logic
            InterviewQuestion q = null;
            if (session.getCurrentQuestionId() != null) {
                q = interviewService.getNextQuestion(sessionId);
            } else {
                q = interviewService.getNextQuestion(sessionId);
            }

            if (q == null)
                return ResponseEntity.notFound().build();

            // Construct path: audio_storage/{candidateName}_{sessionId}/q_{id}.wav
            String candidateName = session.getCandidateName();
            String safeName = (candidateName != null ? candidateName.replaceAll("[^a-zA-Z0-9]", "_") : "candidate");
            String sessionFolder = safeName + "_" + session.getId();
            Path folderPath = Paths.get(AUDIO_DIR, sessionFolder);
            Files.createDirectories(folderPath);

            String filename = "q_" + q.getId() + ".wav";
            Path filePath = folderPath.resolve(filename).toAbsolutePath();

            if (!Files.exists(filePath)) {
                System.out.println("Regenerating audio for Q" + q.getId());
                voiceService.textToSpeech(q.getQuestion(), filePath.toString());
            }

            int retries = 0;
            while (!Files.exists(filePath) && retries < 10) {
                Thread.sleep(500);
                retries++;
            }

            if (!Files.exists(filePath)) {
                return ResponseEntity.internalServerError().build();
            }

            Resource resource = new FileSystemResource(filePath.toFile());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("audio/wav"))
                    .contentLength(resource.contentLength())
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{sessionId}/answer")
    public ResponseEntity<Map<String, String>> submitAnswer(
            @PathVariable Long sessionId,
            @RequestParam("audio") MultipartFile audioFile) {

        try {
            InterviewSession session = interviewService.getSession(sessionId);
            // Construct path: audio_storage/{safeCandidatesName}_{sessionId}/
            String candidateName = session.getCandidateName();
            String safeName = (candidateName != null ? candidateName.replaceAll("[^a-zA-Z0-9]", "_") : "candidate");
            String sessionFolder = safeName + "_" + session.getId();
            Path folderPath = Paths.get(AUDIO_DIR, sessionFolder);
            Files.createDirectories(folderPath);

            // Save upload
            String filename = "ans_" + sessionId + "_" + System.currentTimeMillis() + ".wav";
            Path filePath = folderPath.resolve(filename);
            Files.copy(audioFile.getInputStream(), filePath);

            // Use absolute path
            String result = interviewService.processAnswer(sessionId, filePath.toAbsolutePath().toString());

            Map<String, String> response = new java.util.HashMap<>();

            if (result.startsWith("REPEAT:")) {
                String rephrasedText = result.substring(7);
                String rephraseFilename = "rephrase_" + sessionId + "_" + System.currentTimeMillis() + ".wav";
                Path rephrasePath = folderPath.resolve(rephraseFilename);
                voiceService.textToSpeech(rephrasedText, rephrasePath.toAbsolutePath().toString());

                response.put("status", "REPEAT");
                response.put("text", rephrasedText);
                response.put("audioUrl", "/api/interview/audio/" + sessionFolder + "/" + rephraseFilename);
            } else if (result.startsWith("NEXT:")) {
                String feedbackText = result.substring(5);

                String feedbackFilename = "feedback_" + sessionId + "_" + System.currentTimeMillis() + ".wav";
                Path feedbackPath = folderPath.resolve(feedbackFilename);
                voiceService.textToSpeech(feedbackText, feedbackPath.toAbsolutePath().toString());

                response.put("status", "NEXT");
                response.put("feedbackText", feedbackText);
                response.put("feedbackAudioUrl", "/api/interview/audio/" + sessionFolder + "/" + feedbackFilename);
            } else {
                response.put("status", result);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{sessionId}/end")
    public ResponseEntity<?> endInterview(@PathVariable Long sessionId) {
        interviewService.endInterview(sessionId);
        return ResponseEntity.ok(Map.of("message", "Interview ended. Processing results..."));
    }

    @GetMapping("/{sessionId}/result")
    public ResponseEntity<?> getInterviewResult(@PathVariable Long sessionId) {
        return ResponseEntity.ok(interviewService.getSession(sessionId));
    }

    // Helper endpoint to serve audio files for verification/frontend
    // Supports subfolders via "folderName/fileName" or just "fileName"
    // Use Regex to capture filename including slashes: {filename:.+}
    @GetMapping("/audio/{folder}/{filename}")
    public ResponseEntity<Resource> getAudioFileSub(@PathVariable String folder, @PathVariable String filename) {
        try {
            // For security, prevent ".."
            if (folder.contains("..") || filename.contains(".."))
                return ResponseEntity.badRequest().build();

            Path filePath = Paths.get(AUDIO_DIR, folder, filename);
            if (!Files.exists(filePath))
                return ResponseEntity.notFound().build();

            Resource resource = new FileSystemResource(filePath.toFile());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("audio/wav"))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/audio/{filename}")
    public ResponseEntity<Resource> getAudioFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(AUDIO_DIR, filename);
            if (!Files.exists(filePath))
                return ResponseEntity.notFound().build();
            Resource resource = new FileSystemResource(filePath.toFile());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("audio/wav"))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}