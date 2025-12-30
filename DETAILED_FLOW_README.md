# 🔄 Detailed Interview Flow Documentation

This document outlines the step-by-step execution flow of the AI Interview Agent, from the moment a candidate lands on the page to the final evaluation.

---

## 📅 Phase 1: Session Initialization

1.  **User Input**:
    *   Candidate enters Name, Email on `CandidateDashboard.js`.
    *   Candidate uploads a Resume (PDF) and optionally selects a Job ID.
2.  **Request Handling**:
    *   Frontend sends `POST /api/interview/start` with the file.
    *   `InterviewController` receives the multipart request.
3.  **Resume Parsing**:
    *   `InterviewService` calls `ResumeParserService`.
    *   Apache PDFBox extracts raw text from the uploaded PDF.
4.  **Session Creation**:
    *   A new `InterviewSession` entry is saved in the Database with status `STARTED`.

---

## 🧠 Phase 2: AI Question Generation

1.  **Prompt Engineering**:
    *   `InterviewService` constructs a prompt containing the Resume Text and Job Requirements.
    *   Prompt instructs Gemini to "Generate 5 technical questions of varying difficulty".
2.  **Gemini API Call**:
    *   `GeminiService` sends the prompt to Google Gemini 1.5 Flash.
3.  **Data Processing**:
    *   Gemini returns a JSON array of questions.
    *   The system parses this JSON and saves `InterviewQuestion` entities, linked to the `sessionId`.
4.  **Async Audio Prep**:
    *   **Crucial Step:** The system immediately triggers `VoiceService.preGenerateAudio()` in a background thread.
    *   For *every* generated question, `piper_tts.py` is called to generate a `.wav` file.
    *   Files are stored in `audio_storage/{Candidate_Name}_{SessionID}/`.

---

## 🗣️ Phase 3: The Interview Loop (Main Cycle)

The interview proceeds question-by-question:

### Step A: Fetch Question
1.  Frontend polls `GET /api/interview/{sessionId}/next-question`.
2.  Backend finds the next `PENDING` question for this session.
3.  Backend returns the Question Text and ID.

### Step B: Play Audio
1.  Frontend requests the audio file: `GET /api/interview/{sessionId}/question-audio`.
2.  `InterviewController` locates the correct folder (`audio_storage/{User}_{ID}/`) and streams the `q_{id}.wav` file.
3.  The candidate hears the AI speak the question.

### Step C: Record & Submit Answer
1.  Candidate speaks their answer. Frontend records audio (WebM/WAV).
2.  Frontend sends `POST /api/interview/{sessionId}/answer`.
3.  Backend saves the answer audio to the session folder.

### Step D: Transcription & Evaluation
1.  **STT:** Backend calls `whisper_stt.py` to convert the answer audio to text.
2.  **AI Eval:** Backend sends (Question + Answer Text) to Gemini.
3.  **Scoring:** Gemini returns a score (1-10) and feedback.
4.  **Update:** The Question status is updated to `COMPLETED`.

---

## 🏁 Phase 4: Conclusion

1.  When no questions remain, the session status updates to `COMPLETED`.
2.  **HR Dashboard**:
    *   Recruiter logs in to `HrDashboard.js`.
    *   Frontend fetches `GET /api/interview/all`.
    *   Table displays Candidate Name, Date, and **Total Calculated Score**.
