# AI & Python Documentation

This document explicitly covers the AI models (Gemini) and Python-based voice services used in the application.

## 🧠 AI Model: Google Gemini

The application uses Google's **Gemini 1.5 Flash** model for all cognitive tasks. This model was chosen for its balance of speed and reasoning capability.

### Integration Logic
*   **Service:** `GeminiService.java`
*   **API:** Interacts via standard REST calls to `generativelanguage.googleapis.com`.
*   **Responsibilities:**
    1.  **Resume Parsing & Question Generation:**
        *   *Input:* Candidate Resume Text + Job Description.
        *   *Prompt:* "Generate 5 interview questions (Easy/Medium/Hard) based on this resume..."
        *   *Output:* JSON array of questions with expected key points.
    2.  **Answer Evaluation:**
        *   *Input:* Question + Candidate's Transcribed Answer.
        *   *Prompt:* "Rate this answer 1-10 based on these key points. Provide constructive feedback."

---

## 🐍 Python Voice Services

Since Java has limited support for modern Neural Text-to-Speech (TTS) and Speech-to-Text (STT), the project uses specialized Python scripts.

### 1. Architecture
The Spring Boot backend (`VoiceService.java`) executes Python scripts as external processes using `ProcessBuilder`. It passes text/audio paths as command-line arguments.

### 2. Components

#### A. Text-to-Speech (TTS)
*   **Script:** `scripts/piper_tts.py`
*   **Engine:** [Piper TTS](https://github.com/rhasspy/piper) (Fast, local Neural TTS).
*   **Model:** `en_US-joe-medium` (Stored in `piper/` folder).
*   **Why Piper?** It generates realistic voices in milliseconds, suitable for real-time conversation.

#### B. Speech-to-Text (STT)
*   **Script:** `scripts/whisper_stt.py`
*   **Engine:** [OpenAI Whisper](https://github.com/openai/whisper).
*   **Model:** `tiny.en` (Downloaded automatically on first run).
*   **Function:** Converts the candidate's `.wav` answer file into text for Gemini to analyze.

### 3. Setup & Dependencies
All Python dependencies are defined in `requirements.txt`.
*   `openai-whisper`: For STT.
*   `static-ffmpeg`: For audio format conversion without system-level install.
*   `webrtcvad` (Optional): Voice Activity Detection.

**To install manually:**
```bash
pip install -r requirements.txt
```
