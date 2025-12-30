# AI Interview Agent - Project Structure & Flow

## 1. High-Level Architecture
This project is a Resume-based AI Interviewer designed to automate the initial screening process.
- **Frontend**: React.js (User Interface for Candidate & HR)
- **Backend**: Spring Boot (Business Logic, API, Database)
- **AI Engine**: Google Gemini (Question Generation & Answer Evaluation)
- **TTS Engine**: Python Scripts (Piper/Coqui for Voice Generation)

## 2. Directory Structure

### 📂 Backend (`src/main/java/com/example/ai_interview_agent`)
The backend is organized into standard Spring Boot layers:

- **Controller Layer** (`/controller`):
  - `InterviewController.java`: Main entry point. Handles `start`, `next-question`, `submit-answer`, and `end` endpoints. serving audio files from dynamic session folders.
  
- **Service Layer** (`/service`):
  - `InterviewService.java`: The core brain. Orchestrates the flow:
    - Calls `ResumeParserService` to read files.
    - Calls `GeminiService` to generate questions.
    - Manages `InterviewSession` state.
    - Triggers `VoiceService` for audio generation.
  - `GeminiService.java`: Handles all interactions with Google Gemini API (Prompt Engineering).
  - `VoiceService.java`: Bridges Java and Python to execute TTS scripts.
  - `ResumeParserService.java`: Uses Apache PDFBox to extract text from PDF resumes.

- **Entity Layer** (`/entity`):
  - `InterviewSession`: Tracks the candidate's progress using `sessionId`. Stores status and scores.
  - `InterviewQuestion`: Stores individual question text, predicted difficulty, AI-generated key points, and the candidate's recorded answer path.
  - `HrRequirement`: Stores the job description and required skills.

- **Repository Layer** (`/repository`):
  - Standard JPA repositories for database interaction.

### 📂 Frontend (`frontend/src`)
- **Pages**:
  - `Home.js`: Landing page selection (Candidate vs HR).
  - `CandidateDashboard.js`: Interactive interview interface. Handles:
    - Resume Upload.
    - Audio Recording & Visualization.
    - Polling for Next Question.
  - `HrDashboard.js`: Dashboard for recruiters to view session statuses and scores.

### 📂 Scripts (`scripts/`)
- `piper_tts.py`: Python script used for high-quality, low-latency Text-to-Speech generation.

## 3. Execution Flow

### Phase 1: Registration & Preparation
1. **User Action**: Candidate enters Name, Email, and uploads Resume on Frontend.
2. **Backend**:
   - Creates a new `InterviewSession`.
   - Parses Resume text.
   - Sends Resume + Job Description to **Gemini**.
   - Gemini generates 10-15 tailored questions.
   - Questions are saved to DB with `sessionId`.
   - **VoiceService** asynchronously starts generating audio files for these questions in `audio_storage/{candidate_name}_{session_id}/`.

### Phase 2: The Interview
1. **Loop**:
   - Frontend requests `/next-question`.
   - Backend determines the next unanswered question for this session.
   - Backend returns Question Text and Audio URL.
   - Frontend plays audio and records user's microphone.
   - User submits Audio.
   - Backend saves Answer Audio.
2. **Conversation**:
   - AI can optionally generate filler phrases ("Okay, next...") using TTS between questions.

### Phase 3: Evaluation
1. **Completion**: When all questions are asked, session moves to `COMPLETED`.
2. **Processing**:
   - Backend transcribes user audio (STT).
   - Backend sends (Question + Answer Transcript) to Gemini.
   - Gemini evaluates answer against "Expected Key Points" and assigns a Score (1-10).
3. **HR View**:
   - HR Dashboard fetches all sessions and displays User Name, Email, and Total Score.

## 4. Key Configurations
- `application.properties`: Contains DB config and `gemini.api.key`.
- `audio_storage/`: Directory where all session audio is isolated by candidate name/session ID.
