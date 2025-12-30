package com.example.ai_interview_agent.service;

import com.example.ai_interview_agent.entity.HrRequirement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

        @Value("${gemini.api.key}")
        private String apiKey;

        private final RestTemplate restTemplate = new RestTemplate();

        public String generateQuestions(String resumeText, HrRequirement hr) {
                String prompt = """
                                You are an advanced AI Interview Engine designed for real-world technical and HR interviews.

                                PHASE 1: QUESTION GENERATION

                                INPUTS:
                                - Resume text: %s
                                - Job Role: %s
                                - Required Skills: %s

                                TASK:
                                1. Analyze the resume deeply.
                                2. Generate EXACTLY 15 interview questions:
                                   - 10 Technical questions (based strictly on resume skills & projects)
                                   - 3 Problem-solving / scenario-based questions
                                   - 2 Behavioral / HR questions
                                3. Difficulty distribution:
                                   - 5 Easy
                                   - 6 Medium
                                   - 4 Hard

                                OUTPUT FORMAT (JSON ONLY):
                                {
                                  "questions": [
                                    {
                                      "question_id": 1,
                                      "question_text": "...",
                                      "category": "...",
                                      "difficulty": "...",
                                      "expected_key_points": ["...", "..."]
                                    }
                                  ]
                                }

                                IMPORTANT:
                                - Do NOT add explanations outside JSON
                                - Questions must be interview-realistic
                                - Avoid generic textbook questions
                                """
                                .formatted(resumeText, hr.getRole(), hr.getRequiredSkills());

                return callGemini(prompt);
        }

        public String evaluateAnswer(String question, String answer, String expectedKeyPoints) {
                String prompt = """
                                PHASE 2: ANSWER EVALUATION

                                INPUTS:
                                - Question: %s
                                - Candidate Answer: %s
                                - Expected Key Points: %s

                                TASK:
                                Evaluate the candidate's answer objectively.

                                SCORING RULES:
                                - Score range: 0 to 10
                                - Use whole numbers only
                                - Be strict but fair

                                OUTPUT FORMAT (JSON ONLY):
                                {
                                  "score": 0,
                                  "strengths": ["...", "..."],
                                  "improvements": ["...", "..."],
                                  "brief_feedback": "2–3 line professional feedback"
                                }
                                """.formatted(question, answer, expectedKeyPoints);

                return callGemini(prompt);
        }

        public String generateInterviewSummary(String allEvaluationsJson) {
                String prompt = """
                                PHASE 3: FINAL INTERVIEW SUMMARY

                                INPUT:
                                %s

                                TASK:
                                1. Calculate stats (Total, Average, Category averages).
                                2. Determine overall performance level.
                                3. Generate final feedback.

                                OUTPUT FORMAT (JSON ONLY):
                                {
                                  "overall_score": 0,
                                  "average_score": 0.0,
                                  "category_scores": {
                                    "technical": 0.0,
                                    "problem_solving": 0.0,
                                    "behavioral": 0.0
                                  },
                                  "performance_level": "...",
                                  "final_feedback": {
                                    "strengths": ["...", "..."],
                                    "weaknesses": ["...", "..."],
                                    "skill_gaps": ["...", "..."],
                                    "recommendations": ["...", "..."]
                                  }
                                }
                                """.formatted(allEvaluationsJson);

                return callGemini(prompt);
        }

        public String rephraseQuestion(String question) {
                String prompt = """
                                You are an interviewer. The candidate did not understand this question:
                                "%s"

                                Task:
                                - Rephrase it to be simpler and clearer.
                                - Do not change the core meaning.
                                - Output ONLY the rephrased question.
                                """.formatted(question);

                return callGemini(prompt);
        }

        private String callGemini(String promptText) {
                Map<String, Object> body = Map.of(
                                "contents", List.of(
                                                Map.of("parts", List.of(
                                                                Map.of("text", promptText)))));

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

                try {
                        ResponseEntity<Map> response = restTemplate.postForEntity(
                                        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                                                        + apiKey,
                                        request,
                                        Map.class);

                        Map responseBody = response.getBody();
                        if (responseBody == null || !responseBody.containsKey("candidates")) {
                                return "Error: No response";
                        }

                        Map candidate = (Map) ((List) responseBody.get("candidates")).get(0);
                        Map content = (Map) candidate.get("content");
                        List parts = (List) content.get("parts");

                        String text = ((Map) parts.get(0)).get("text").toString().trim();

                        // Cleanup Markdown block if present (```json ... ```)
                        if (text.startsWith("```json")) {
                                text = text.replace("```json", "").replace("```", "").trim();
                        } else if (text.startsWith("```")) {
                                text = text.replace("```", "").trim();
                        }

                        return text;

                } catch (Exception e) {
                        return "Error calling Gemini: " + e.getMessage();
                }
        }
}