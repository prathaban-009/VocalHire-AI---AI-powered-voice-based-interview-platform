package com.example.ai_interview_agent.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ResumeParserService {

    public String parseResume(MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            if (filename != null && filename.toLowerCase().endsWith(".pdf")) {
                return parsePdf(file);
            } else if (filename != null
                    && (filename.toLowerCase().endsWith(".docx") || filename.toLowerCase().endsWith(".doc"))) {
                return parseWord(file);
            } else if (filename != null && filename.toLowerCase().endsWith(".txt")) {
                return new String(file.getBytes());
            } else {
                throw new RuntimeException("Unsupported file format");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error parsing resume: " + e.getMessage());
        }
    }

    private String parsePdf(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String parseWord(MultipartFile file) throws IOException {
        try (XWPFDocument document = new XWPFDocument(file.getInputStream());
                XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }
}