package com.example.ai_interview_agent.service;

import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

@Service
public class VoiceService {

    private static final String SCRIPT_DIR = "scripts";

    public String speechToText(String audioFilePath) {
        try {
            String currentDir = System.getProperty("user.dir");
            String pythonPath = currentDir + File.separator + ".venv" + File.separator + "Scripts" + File.separator
                    + "python.exe";
            String scriptPath = currentDir + File.separator + SCRIPT_DIR + File.separator + "whisper_stt.py";

            System.out.println("STT Command: " + pythonPath + " " + scriptPath + " " + audioFilePath);

            ProcessBuilder pb = new ProcessBuilder(
                    pythonPath,
                    scriptPath,
                    audioFilePath);
            // Do NOT redirect error stream. We want to separate logs (stderr) from result
            // (stdout).
            pb.redirectErrorStream(false);

            Process process = pb.start();

            // Read standard output (transcription)
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = stdInput.readLine()) != null) {
                result.append(line).append(" ");
            }

            // Read error output (logs)
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder logs = new StringBuilder();
            while ((line = stdError.readLine()) != null) {
                logs.append(line).append("\n");
            }

            boolean finished = process.waitFor(300, TimeUnit.SECONDS); // Give Whisper plenty of time for first run
                                                                       // (downloads)
                                                                       // run)
            if (!finished) {
                process.destroy();
                throw new RuntimeException("STT process timed out. Logs:\n" + logs.toString());
            }

            if (process.exitValue() != 0) {
                throw new RuntimeException(
                        "STT process failed with exit code " + process.exitValue() + ". Logs:\n" + logs.toString());
            }

            // Log any stderr output for debugging
            if (logs.length() > 0) {
                System.out.println("STT stderr logs:\n" + logs.toString());
            }

            String transcription = result.toString().trim();
            System.out.println("STT Result: " + transcription);

            return transcription;

        } catch (Exception e) {
            throw new RuntimeException("Failed to transcribe audio: " + e.getMessage(), e);
        }
    }

    public void textToSpeech(String text, String outputFilePath) {
        try {
            String currentDir = System.getProperty("user.dir");
            String pythonPath = currentDir + File.separator + ".venv" + File.separator + "Scripts" + File.separator
                    + "python.exe";
            String scriptPath = currentDir + File.separator + SCRIPT_DIR + File.separator + "piper_tts.py";

            // Sanitize text to avoid command line parsing issues (especially double quotes
            // on Windows)
            String sanitizedText = text.replace("\"", "'").replace("\n", " ").replace("\r", "");

            System.out.println("TTS Command: " + pythonPath + " " + scriptPath + " [Text length: "
                    + sanitizedText.length() + "] " + outputFilePath);
            System.out.println("Sanitized Text for TTS: " + sanitizedText);

            ProcessBuilder pb = new ProcessBuilder(
                    pythonPath,
                    scriptPath,
                    sanitizedText,
                    outputFilePath);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder log = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                log.append(line).append("\n");
            }

            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            System.out.println("Process finished: " + finished);
            System.out.println("Process Output:\n" + log.toString());

            if (!finished) {
                process.destroy();
                throw new RuntimeException("TTS process timed out");
            }

            if (process.exitValue() != 0) {
                throw new RuntimeException("TTS process failed: " + log.toString());
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate speech: " + e.getMessage(), e);
        }
    }
}
