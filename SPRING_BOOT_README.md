# Spring Boot Backend Documentation

## 🏗️ Architecture Overview
This application uses a standard Spring Boot layered architecture aimed at separation of concerns.

### 📁 Directory Structure (`src/main/java/com/example/ai_interview_agent`)

#### 1. Controller Layer (`/controller`)
**Purpose:** Handles incoming HTTP REST requests and returns standard JSON responses.
*   `InterviewController.java`: The main entry point. Handles text/audio uploads, serving questions, and receiving answers.
*   `HrController.java`: Endpoints for the HR dashboard (creating requirements, viewing results).

#### 2. Service Layer (`/service`)
**Purpose:** Contains valid business logic, data processing, and integration with external APIs.
*   `InterviewService.java`: Core logic. Links questions to sessions, manages state (started, completed), and coordinates audio generation.
*   `GeminiService.java`: Manages the Google Gemini AI integration (Generates questions, evaluates answers).
*   `VoiceService.java`: Handles "Text-to-Speech" (Piper) and "Speech-to-Text" (Whisper) via Python script execution.
*   `ResumeParserService.java`: Logic to extract text from candidate PDF resumes.

#### 3. Entity Layer (`/entity`)
**Purpose:** Represents the database schema using JPA/Hibernate annotations.
*   `InterviewSession`: Tracks the interview state (User, Score, Progress).
*   `InterviewQuestion`: Represents individual questions linked to a session.
*   `HrRequirement`: Stores job descriptions and expected skills.

#### 4. Repository Layer (`/repository`)
**Purpose:** Interfaces for CRUD operations on the Entities (extends `JpaRepository`).

---

## ⚙️ Configuration
Configuration is managed in `src/main/resources/application.properties`.
*   **Database:** Configured for MySQL (Production) or H2 (Dev).
*   **API Keys:** `gemini.api.key` must be set here.
*   **File Storage:** `spring.servlet.multipart.max-file-size` handles upload limits.

## 🏃 how to Run
1.  **Build:** `./mvnw clean install`
2.  **Run:** `./mvnw spring-boot:run`
3.  **Access:** API is available at `http://localhost:8080/api/interview/...`
