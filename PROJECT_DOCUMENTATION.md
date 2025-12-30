# AI Interview Agent - Project Documentation

## 1. Project Overview
The **AI Interview Agent** is a full-stack automated interview platform. It acts as an intelligent technical interviewer, conducting voice-based interviews, evaluating candidates in real-time using Large Language Models (LLM), and providing structured feedback.

## 2. Technology Stack

### Backend
*   **Framework:** Spring Boot 3.2.6 (Java 17)
*   **Database:** H2 (In-Memory) / MySQL (Production ready via properties)
*   **AI Integration:** Google Gemini 2.5 Flash API (via REST)
*   **Build Tool:** Maven

### Frontend
*   **Framework:** React.js
*   **Styling:** Bootstrap + Custom CSS (Glassmorphism design)
*   **State Management:** React Hooks directly (Context API where needed)

### Voice Services (Python Bridge)
The backend invokes Python scripts for heavy audio processing.
*   **Speech-to-Text (STT):** `openai-whisper` (Model: `tiny.en` for performance)
*   **Text-to-Speech (TTS):** `Piper TTS` (Model: `en_US-joe-medium`)
*   **Audio Processing:** `static-ffmpeg` (Auto-managed FFmpeg binaries)

## 3. Architecture & Data Flow

### 3.1. High-Level Flow
1.  **HR Setup:** HR posts a job requirement (Role & Skills).
2.  **Candidate Onboarding:** Candidate uploads a Resume (PDF/DOCX).
3.  **Question Generation:** System parses the resume and generates tailored questions using Gemini (Easy -> Medium -> Hard).
4.  **Interview Session:**
    *   **Ask:** Backend generates audio for the question (TTS).
    *   **Listen:** Frontend records candidate's answer.
    *   **Transcribe:** Backend converts answer audio to text (STT).
    *   **Evaluate:** Gemini rates the answer (1-10) and provides feedback.
    *   **Repeat:** Cycle continues until all questions are asked or time runs out as per logic.

### 3.2. Folder Structure
```
ai-interview-agent/
├── frontend/                   # React Application
│   ├── src/components/         # UI Components (InterviewRoom, etc.)
│   ├── src/services/           # API Integration (api.js)
│   └── public/                 # Static Assets
├── scripts/                    # Python Voice Scripts
│   ├── piper_tts.py            # TTS Wrapper (Piper)
│   └── whisper_stt.py          # STT Wrapper (OpenAI Whisper)
├── piper/                      # Piper TTS Executable & Models
├── src/main/java/              # Spring Boot Backend
│   ├── controller/             # REST Endpoints
│   ├── service/                # Business Logic (Interview, Voice, Gemini)
│   ├── entity/                 # DB Models
│   └── ...
├── audio_storage/              # Temp directory for runtime audio files
├── .venv/                      # Python Virtual Environment
├── pom.xml                     # Backend Dependencies
└── requirements.txt            # Python Dependencies
```

## 4. Key Services Detail

### 4.1. InterviewService (`InterviewService.java`)
*   **Flow Control:** Manages the progression of questions (3 Easy -> 4 Medium -> 2 Hard).
*   **Session Management:** Tracks current question, score, and timer (30 min limit).
*   **Test Mode:** Supports a "Test Interview" mode with hardcoded questions to bypass Gemini for debugging.

### 4.2. VoiceService (`VoiceService.java`)
*   **Robust Execution:** Uses `ProcessBuilder` with absolute paths to execute Python scripts.
*   **TTS:** Calls `scripts/piper_tts.py`. Sanitizes text input to prevent command-line errors.
*   **STT:** Calls `scripts/whisper_stt.py`.
    *   Uses `tiny.en` model for speed.
    *   Includes a 300s timeout to handle initial model downloads.
    *   Separates `stdout` (transcription) from `stderr` (logs) for clean parsing.

### 4.3. GeminiService (`GeminiService.java`)
*   **Integration:** Direct HTTP calls to `generativelanguage.googleapis.com`.
*   **Prompts:**
    *   *Question Generation:* Strict format enforcement for parsing.
    *   *Evaluation:* Scores answers and provides feedback.
    *   *Rephrasing:* Simplifies questions if the candidate asks to repeat.

## 5. Setup & Running

### Prerequisites
1.  **Java JDK 17+**
2.  **Node.js & npm**
3.  **Python 3.8+**

### Step 1: Python Environment
The system relies on a local Python environment for voice features.
```bash
python -m venv .venv
.\.venv\Scripts\activate
# Install specific dependencies (openai-whisper, static-ffmpeg)
pip install -r requirements.txt
```

### Step 2: Backend (Spring Boot)
Ensure `application.properties` has your valid `gemini.api.key`.
```bash
./mvnw.cmd spring-boot:run
```
*Server runs on port 8080.*

### Step 3: Frontend (React)
```bash
cd frontend
npm start
```
*App runs on port 3000.*

## 6. API Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **POST** | `/api/interview/start` | Start full interview (Resume + Gemini) |
| **POST** | `/api/interview/test-start` | Start test interview (Hardcoded Qs) |
| **GET** | `/api/interview/{id}/next-question` | Get current question details |
| **GET** | `/api/interview/{id}/question-audio` | Get audio stream for validation |
| **POST** | `/api/interview/{id}/answer` | Submit answer audio (WAV) |

## 7. Known Issues & Troubleshooting
*   **First Run Latency:** The first STT request downloads the Whisper model (~75MB). The timeout is set to 5 minutes to accommodate this.
*   **Audio Format:** The browser must record in a format compatible with FFmpeg/Whisper (usually WebM/WAV). The backend saves it as `.wav`.
