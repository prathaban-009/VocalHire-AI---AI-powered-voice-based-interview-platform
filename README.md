# AI Interview Agent

An automated, AI-powered interview platform that evaluates candidates using Google Gemini models and analyzes technical responses via speech processing.

## 📂 Spring Boot Project Structure

The backend follows a standard layered architecture for maintainability and scalability which is located in `src/main/java/com/example/ai_interview_agent`:

### 1. **Controller Layer** (`/controller`)
Handles incoming HTTP requests and responses.
*   **`InterviewController.java`**: Manages the core interview lifecycle (Start, Questions, Answers, End).
*   **`HrController.java`**: API endpoints for HR dashboards and job requirements.

### 2. **Service Layer** (`/service`)
Contains the business logic.
*   **`InterviewService.java`**: Orchestrates the interview flow, linking resumes to sessions.
*   **`GeminiService.java`**: Communicates with Google 'Gemini' AI for generating questions and evaluating answers.
*   **`VoiceService.java`**: Bridges Java with Python scripts for Text-to-Speech (TTS) and Speech-to-Text (STT).
*   **`ResumeParserService.java`**: Extracts text from PDF/DOCX resumes.

### 3. **Entity Layer** (`/entity`)
Defines the data models (JPA Entities).
*   **`InterviewSession`**: Represents a candidate's interview session.
*   **`InterviewQuestion`**: Stores question text, difficulty, AI feedback, and audio paths.
*   **`HrRequirement`**: Defines job roles and required skills.

### 4. **Repository Layer** (`/repository`)
Interfaces for database CRUD operations (extends `JpaRepository`).

---

## 🚀 How to Run

### Prerequisites
*   Java 17+
*   Node.js 16+
*   Python 3.8+ (with `openai-whisper`, `piper-tts` dependencies)

### Backend (Spring Boot)
1.  Configure your Gemini API Key in `src/main/resources/application.properties` (or use environment variables).
    ```properties
    gemini.api.key=YOUR_API_KEY
    ```
2.  Run the application:
    ```bash
    ./mvnw.cmd spring-boot:run
    ```
    (Runs on `localhost:8080`)

### Frontend (React)
1.  Navigate to `frontend/`.
2.  Install dependencies: `npm install`.
3.  Start the dev server:
    ```bash
    npm start
    ```
    (Accessible at `localhost:3000`)

---

## 🛠 Features
*   **Resume Parsing**: Automatically tailors questions to the candidate's experience.
*   **Voice Interaction**: Full-duplex voice interview (TTS questions, STT answers).
*   **AI Evaluation**: Real-time scoring and feedback from Gemini.
*   **Audio Archives**: Sessions are recorded and stored in `audio_storage/{candidate}_{session}/`.