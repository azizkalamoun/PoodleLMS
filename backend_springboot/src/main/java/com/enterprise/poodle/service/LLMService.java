package com.enterprise.poodle.service;

import com.enterprise.poodle.config.LLMConfig;
import com.enterprise.poodle.dto.response.QCMQuestionResponse;
import com.enterprise.poodle.entity.CourseSection;
import com.enterprise.poodle.entity.QCMQuestion;
import com.enterprise.poodle.enums.QcmType;
import com.enterprise.poodle.enums.ContentType;
import com.enterprise.poodle.entity.Course;
import com.enterprise.poodle.exception.BusinessException;
import com.enterprise.poodle.exception.ResourceNotFoundException;
import com.enterprise.poodle.repository.CourseSectionRepository;
import com.enterprise.poodle.repository.QCMQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.ByteArrayInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class LLMService {
    private final CourseSectionRepository sectionRepository;
    private final QCMQuestionRepository questionRepository;
    private final RestTemplate geminiRestTemplate;
    private final LLMConfig.LLMProperties llmProperties;
    private final PDFReaderService pdfReaderService;

    @Transactional
    public List<QCMQuestionResponse> generateQCMDraft(Long sectionId, Integer numberOfQuestions) {
        if (numberOfQuestions == null || numberOfQuestions <= 0) {
            numberOfQuestions = 5; // Default
        }
        if (numberOfQuestions > 50) {
            numberOfQuestions = 50; // Max limit
        }

        CourseSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));

        if (section.getContentType() != ContentType.QCM) {
            throw new BusinessException("Section content type is not QCM");
        }

        String documentContent = "";
        if (section.getContentUrl() != null && !section.getContentUrl().isEmpty()) {
            try {
                byte[] pdfBytes = pdfReaderService.getPdfContent(section.getContentUrl());
                if (pdfBytes != null && pdfBytes.length > 0) {
                    PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfBytes));
                    PDFTextStripper pdfStripper = new PDFTextStripper();
                    documentContent = pdfStripper.getText(document);
                    document.close();
                    log.info("Successfully extracted {} characters from PDF", documentContent.length());
                } else {
                    log.warn("PDF content is empty, using file description as fallback");
                    documentContent = section.getFileDescription() != null ? section.getFileDescription() : "";
                }
            } catch (Exception e) {
                log.warn("Failed to read PDF content: {}, using file description as fallback", e.getMessage());
                documentContent = section.getFileDescription() != null ? section.getFileDescription() : "";
            }
        } else {
            documentContent = section.getFileDescription() != null ? section.getFileDescription() : "";
        }

        if (documentContent == null || documentContent.trim().isEmpty()) {
            log.warn("No content available for QCM generation, using placeholder questions");
        }

        // Delete existing questions
        List<QCMQuestion> existingQuestions = questionRepository.findBySectionId(sectionId);
        questionRepository.deleteAll(existingQuestions);

        log.info("Generating {} QCM draft questions for section '{}'",
                numberOfQuestions, section.getTitle());

        List<QCMQuestion> draftQuestions;
        
        if (documentContent == null || documentContent.trim().isEmpty()) {
            log.warn("No content available for QCM generation, using placeholder questions");
            draftQuestions = generatePlaceholderDraft(section, numberOfQuestions);
        } else if (llmProperties.getApiKey() != null && !llmProperties.getApiKey().isEmpty()) {
            log.info("Using Gemini API for question generation");
            draftQuestions = generateWithGemini(section, documentContent, numberOfQuestions);
        } else {
            log.warn("Gemini API not configured - using placeholder generation");
            draftQuestions = generatePlaceholderDraft(section, numberOfQuestions);
        }

        List<QCMQuestion> saved = questionRepository.saveAll(draftQuestions);

        return saved.stream()
                .map(q -> QCMQuestionResponse.builder()
                        .id(q.getId())
                        .questionText(q.getQuestionText())
                        .optionA(q.getOptionA())
                        .optionB(q.getOptionB())
                        .optionC(q.getOptionC())
                        .optionD(q.getOptionD())
                        .correctOption(q.getCorrectOption())
                        .llmGenerated(q.isLlmGenerated())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public List<QCMQuestionResponse> generateQCMDraft(Long sectionId) {
        return generateQCMDraft(sectionId, 5); // Default to 5 questions for backward compatibility
    }

    /**
     * Generate QCM questions using Google Gemini API
     */
    private List<QCMQuestion> generateWithGemini(CourseSection section, String documentContent, Integer numberOfQuestions) {
        try {
            String prompt = buildGeminiPrompt(documentContent, numberOfQuestions);
            String model = llmProperties.getModel() != null ? llmProperties.getModel() : "gemini-2.5-flash";
            String apiKey = llmProperties.getApiKey();
            
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;
            
            log.debug("Constructed Gemini API URL: {}", url.replaceAll("key=.*", "key=***REDACTED***"));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                    Map.of("parts", List.of(
                        Map.of("text", prompt)
                    ))
                )
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.debug("Sending prompt to Gemini API...");
            Map<String, Object> response = geminiRestTemplate.postForObject(url, entity, Map.class);
            log.debug("Received response from Gemini API");
            
            String geminiResponse = extractResponseText(response);
            return parseGeminiResponse(geminiResponse, section, numberOfQuestions);

        } catch (Exception e) {
            log.error("Unexpected error during Gemini API call: {}", e.getMessage(), e);
            throw new BusinessException("Error generating questions: " + e.getMessage());
        }
    }

    private String extractResponseText(Map<String, Object> response) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            log.error("Failed to extract text from Gemini response: {}", e.getMessage());
            return "";
        }
    }    /**
     * Build the prompt for Gemini API
     */
    private String buildGeminiPrompt(String documentContent, Integer numberOfQuestions) {
        return String.format("""
                You are an expert educational content creator. Generate exactly %d multiple-choice questions (MCQ) based on the following course content:
                
                %%s

                For each question, provide:
                1. The question text (clear and specific)
                2. Four options (A, B, C, D)
                3. The correct option (A, B, C, or D)

                Format your response EXACTLY like this (JSON format):
                [
                  {
                    "question": "What is...",
                    "optionA": "...",
                    "optionB": "...",
                    "optionC": "...",
                    "optionD": "...",
                    "correctOption": "A"
                  }
                ]
                """, numberOfQuestions, documentContent);
    }    /**
     * Parse the Gemini API response and convert to QCMQuestion objects
     */
    private List<QCMQuestion> parseGeminiResponse(String responseText, CourseSection section, Integer numberOfQuestions) {
        List<QCMQuestion> questions = new ArrayList<>();
        
        try {
            // Extract JSON array from response
            String jsonArray = extractJsonArray(responseText);
            
            // Simple JSON parsing (in production, use a proper JSON library like Jackson)
            String[] questionBlocks = jsonArray.split("\\},");
            
            for (int i = 0; i < questionBlocks.length && i < numberOfQuestions; i++) {
                String block = questionBlocks[i];
                if (!block.trim().isEmpty()) {
                    QCMQuestion question = parseQuestionBlock(block, section);
                    if (question != null) {
                        questions.add(question);
                    }
                }
            }
            
            if (questions.isEmpty()) {
                log.warn("No questions parsed from Gemini response, using placeholder");
                return generatePlaceholderDraft(section, numberOfQuestions);
            }
            
            log.info("Generated {} questions from Gemini", questions.size());
            return questions;
            
        } catch (Exception e) {
            log.error("Error parsing Gemini response: {}", e.getMessage());
            return generatePlaceholderDraft(section, numberOfQuestions);
        }
    }

    /**
     * Extract JSON array from the response text
     */
    private String extractJsonArray(String text) {
        Pattern pattern = Pattern.compile("\\[.*\\]", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        throw new IllegalArgumentException("No JSON array found in response");
    }

    /**
     * Parse a single question block from the JSON response
     */
    private QCMQuestion parseQuestionBlock(String block, CourseSection section) {
        try {
            String question = extractJsonValue(block, "question");
            String optionA = extractJsonValue(block, "optionA");
            String optionB = extractJsonValue(block, "optionB");
            String optionC = extractJsonValue(block, "optionC");
            String optionD = extractJsonValue(block, "optionD");
            String correct = extractJsonValue(block, "correctOption");
            
            if (question.isEmpty() || optionA.isEmpty() || correct.isEmpty()) {
                return null;
            }
            
            return QCMQuestion.builder()
                    .section(section)
                    .questionText(question)
                    .optionA(optionA)
                    .optionB(optionB)
                    .optionC(optionC)
                    .optionD(optionD)
                    .correctOption(correct.toUpperCase())
                    .llmGenerated(true)
                    .build();
                    
        } catch (Exception e) {
            log.debug("Failed to parse question block: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract a value from JSON using regex (simple implementation)
     */
    private String extractJsonValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"\\\\]*(\\\\.[^\"\\\\]*)*)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1)
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
        }
        return "";
    }



    /**
     * Placeholder for LLM integration fallback.
     * Used when Gemini API is not configured or fails.
     */
    private List<QCMQuestion> generatePlaceholderDraft(CourseSection section, Integer numberOfQuestions) {
        List<QCMQuestion> questions = new ArrayList<>();

        for (int i = 1; i <= numberOfQuestions; i++) {
            QCMQuestion question = QCMQuestion.builder()
                    .section(section)
                    .questionText("[PLACEHOLDER #" + i + "] What is the main topic of: " + section.getTitle() + "?")
                    .optionA("Not a valid answer")
                    .optionB("Not a valid answer")
                    .optionC("Not a valid answer")
                    .optionD("Not a valid answer")
                    .correctOption("A")
                    .llmGenerated(false)
                    .build();
            questions.add(question);
        }

        return questions;
    }
}

