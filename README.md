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


---

Perfect 👍
Below is a **clear, end-to-end AI Interview System flow**, written in **simple language**, and **explicitly mentioning the technologies you used** (Whisper, Piper, local LLM, Spring Boot, React).

You can use this for:

* Project explanation
* README
* Viva / interview explanation
* Architecture diagram reference

---

# 🧠 AI-POWERED INTERVIEW SYSTEM – COMPLETE FLOW



To conduct a **real-time AI-driven technical interview** where:

* Questions are asked via **human-like voice**
* Candidate answers via **speech**
* Answers are **evaluated automatically**
* Scores are generated **without human intervention**

---

## 🏗️ TECHNOLOGY STACK USED

### 🔹 Backend

* **Spring Boot (Java)** – Core interview logic & APIs
* **MySQL** – Store HR requirements, questions, answers, scores

### 🔹 AI / ML

* **Whisper (faster-whisper)** – Speech → Text (STT)
* **Piper TTS** – Text → Speech (TTS)
* **Mistral 7B (via Ollama)** – Gemini alternative (LLM)

### 🔹 Frontend

* **React.js** – UI, mic input, audio playback

### 🔹 Communication

* **REST APIs**
* **Local process execution** (Python & CLI tools)

---



---

## 🟢 PHASE 1: HR SETUP

### 1️⃣ HR logs in

* HR creates **job role**
* HR sets **required skills**
* HR defines difficulty mix

📌 Stored in MySQL

---

## 🟢 PHASE 2: CANDIDATE REGISTRATION

### 2️⃣ Candidate uploads resume

* Spring Boot parses resume (PDF/DOC)
* Extracted text is stored

---

## 🟢 PHASE 3: QUESTION GENERATION (ONE-TIME)

### 3️⃣ Local LLM generates interview questions

* **Mistral 7B (Ollama)** is used instead of Gemini
* Generates:

  * 3 EASY
  * 4 MEDIUM
  * 2 HARD questions
* Questions are stored in DB

📌 This happens **once per interview**

---

## 🟢 PHASE 4: INTERVIEW START

### 4️⃣ Candidate clicks “Start Interview”

Spring Boot:

* Creates `InterviewSession`
* Starts **30-minute timer**
* Selects first EASY question

---

## 🟢 PHASE 5: QUESTION → VOICE (TEXT TO SPEECH)

### 5️⃣ Question is spoken to candidate

* Spring Boot sends question text to **Piper**
* Piper converts **Text → WAV audio**
* Audio is sent to frontend
* Frontend plays interviewer voice 🎙️

📌 Technology:

```
Spring Boot → Piper TTS → Audio
```

---

## 🟢 PHASE 6: ANSWER → TEXT (SPEECH TO TEXT)

### 6️⃣ Candidate answers via microphone

* Frontend records audio
* Audio sent to backend
* Backend invokes **Whisper**
* Whisper converts **Speech → Text**

📌 Technology:

```
Audio → Whisper → Text
```

---

## 🟢 PHASE 7: CONFUSION HANDLING

### 7️⃣ If candidate says:

* “I don’t understand”
* “Repeat the question”

Spring Boot:

* Calls **Mistral**
* Rephrases question politely
* Piper speaks the simplified question again

📌 Max repeat = **2 times**

---

## 🟢 PHASE 8: ANSWER EVALUATION

### 8️⃣ Answer is evaluated

* Spring Boot sends:

  * Question
  * Candidate answer
* To **Mistral 7B**
* Mistral returns:

```json
{
  "score": 1–10,
  "feedback": "Short evaluation"
}
```

📌 Stored in MySQL

---

## 🟢 PHASE 9: NEXT QUESTION SELECTION

### 9️⃣ Backend decides next step

Rules:

* EASY → MEDIUM → HARD
* Never repeat same question > 2 times
* Stop if 30 minutes exceeded

Loop continues until:

* All questions asked OR
* Time runs out

---

## 🟢 PHASE 10: INTERVIEW END

### 🔟 Interview ends

Spring Boot:

* Calculates final score
* Generates summary
* Stores results

Frontend:

* Displays performance report

---

# 🧠 OVERALL SYSTEM FLOW (ONE-LINE)

```
HR → Resume → Questions → Voice → Answer → Evaluation → Score
```

---

# 🔁 TECH FLOW DIAGRAM (TEXTUAL)

```
React UI
   ↓ (audio)
Spring Boot
   ↓
Whisper (STT)
   ↓
Text Answer
   ↓
Mistral (Evaluation)
   ↓
Score
   ↓
Spring Boot
   ↓
Piper (TTS)
   ↓
Voice Question
   ↓
React UI
```

---

# 🏆 WHY THIS DESIGN IS STRONG

✔ Fully local (no quota limits)
✔ No paid APIs
✔ Fast response
✔ Scalable
✔ Real interview experience
✔ Production-ready architecture

---

