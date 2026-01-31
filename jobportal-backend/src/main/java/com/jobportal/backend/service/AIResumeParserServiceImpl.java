package com.jobportal.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobportal.backend.dto.ParsedResumeResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class AIResumeParserServiceImpl implements AIResumeParserService {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public ParsedResumeResponse parseResume(MultipartFile file) {
        try {
            log.info("Starting resume parsing for file: {}", file.getOriginalFilename());
            
            // Validate file
            if (file.isEmpty()) {
                throw new RuntimeException("File is empty");
            }
            
            // Check file type
            String contentType = file.getContentType();
            log.info("File content type: {}", contentType);
            
            // Extract text from PDF
            String resumeText;
            try {
                resumeText = extractTextFromPDF(file);
            } catch (IOException e) {
                log.error("PDF parsing failed: {}. File might be corrupted or not a valid PDF.", e.getMessage());
                throw new RuntimeException("Invalid or corrupted PDF file. Please upload a valid PDF resume.", e);
            }
            
            if (resumeText == null || resumeText.trim().isEmpty()) {
                throw new RuntimeException("Could not extract text from PDF. File might be image-based or corrupted.");
            }
            
            log.debug("Extracted text length: {} characters", resumeText.length());
            log.debug("First 200 chars: {}", resumeText.substring(0, Math.min(200, resumeText.length())));
            
            // Try OpenAI parsing first
            log.info("Sending to OpenAI for parsing...");
            return parseWithOpenAI(resumeText);
            
        } catch (Exception e) {
            log.error("Failed to parse resume", e);
            throw new RuntimeException("Failed to parse resume: " + e.getMessage(), e);
        }
    }

    private String extractTextFromPDF(MultipartFile file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private ParsedResumeResponse parseWithOpenAI(String resumeText) {
        try {
            log.info("==== CALLING OPENAI API ====");
            log.info("API Key present: {}", openaiApiKey != null && !openaiApiKey.trim().isEmpty());
            log.info("Resume text length: {}", resumeText.length());
            
            // Truncate resume if too long (OpenAI has token limits)
            String truncatedText = resumeText.length() > 3000 
                ? resumeText.substring(0, 3000) 
                : resumeText;
            
            // Create messages array
            String systemMessage = "You are an expert resume parser. Extract structured information from resumes and return ONLY valid JSON. No markdown, no explanations, just pure JSON.";
            String userPrompt = "Extract the following information from this resume and return ONLY valid JSON (no code blocks, no markdown):\n\n" +
                "{\n" +
                "  \"fullName\": \"candidate's full name\",\n" +
                "  \"email\": \"email address\",\n" +
                "  \"phone\": \"phone number\",\n" +
                "  \"skills\": [\"skill1\", \"skill2\"],\n" +
                "  \"experience\": [{\"company\": \"\", \"role\": \"\", \"duration\": \"\", \"description\": \"\"}],\n" +
                "  \"education\": [{\"degree\": \"\", \"institution\": \"\", \"year\": \"\", \"fieldOfStudy\": \"\"}],\n" +
                "  \"summary\": \"professional summary\",\n" +
                "  \"totalExperience\": \"X years\"\n" +
                "}\n\n" +
                "Resume text:\n" + truncatedText;
            
            // Build JSON request body manually (safer than String.format)
            StringBuilder requestBody = new StringBuilder();
            requestBody.append("{");
            requestBody.append("\"model\":\"gpt-3.5-turbo\",");
            requestBody.append("\"messages\":[");
            requestBody.append("{\"role\":\"system\",\"content\":").append(toJsonString(systemMessage)).append("},");
            requestBody.append("{\"role\":\"user\",\"content\":").append(toJsonString(userPrompt)).append("}");
            requestBody.append("],");
            requestBody.append("\"temperature\":0.3,");
            requestBody.append("\"max_tokens\":2000");
            requestBody.append("}");
            
            log.info("Request body built successfully");
            
            // Create HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + openaiApiKey.trim())
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();
            
            log.info("Sending request to OpenAI...");
            
            // Call OpenAI API
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            log.info("OpenAI response received. Status: {}", response.statusCode());
            
            if (response.statusCode() != 200) {
                log.error("==== OPENAI API ERROR ====");
                log.error("Status Code: {}", response.statusCode());
                log.error("Response Body: {}", response.body());
                log.error("==========================");
                throw new RuntimeException("OpenAI API error: " + response.statusCode());
            }
            
            // Parse response
            JsonNode responseJson = objectMapper.readTree(response.body());
            String content = responseJson.get("choices").get(0).get("message").get("content").asText();
            
            log.info("Content received from OpenAI. Length: {}", content.length());
            log.debug("Content: {}", content);
            
            // Extract JSON from response
            String jsonResponse = extractJsonFromResponse(content);
            ParsedResumeResponse parsedResume = objectMapper.readValue(jsonResponse, ParsedResumeResponse.class);
            
            log.info("Successfully parsed resume with OpenAI!");
            return parsedResume;
            
        } catch (Exception e) {
            log.error("==== ERROR IN OPENAI PARSING ====");
            log.error("Error type: {}", e.getClass().getName());
            log.error("Error message: {}", e.getMessage());
            log.error("Stack trace:", e);
            log.error("================================");
            log.warn("Falling back to regex-based parser...");
            return createFallbackResponse(resumeText);
        }
    }

    // Helper method for JSON string escaping
    private String toJsonString(String text) {
        try {
            return objectMapper.writeValueAsString(text);
        } catch (Exception e) {
            // Manual escaping as fallback
            return "\"" + text
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t") + "\"";
        }
    }

    private String extractJsonFromResponse(String content) {
        // Remove markdown code blocks if present
        String cleaned = content.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        }
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }

    private ParsedResumeResponse createFallbackResponse(String resumeText) {
        log.warn("Using ENHANCED fallback parser (regex-based)");
        
        ParsedResumeResponse response = new ParsedResumeResponse();
        
        // Extract Name (first line or after "Name:" pattern)
        String name = extractPattern(resumeText, 
            "(?i)(?:name[:\\s]+)?([A-Z][a-z]+ [A-Z][a-z]+(?:\\s+[A-Z][a-z]+)?)",
            resumeText.split("\n")[0].trim()
        );
        response.setFullName(name.isEmpty() ? "Not Found" : name);
        
        // Extract Email
        String email = extractPattern(resumeText,
            "([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})",
            "not-found@example.com"
        );
        response.setEmail(email);
        
        // Extract Phone
        String phone = extractPattern(resumeText,
            "(?i)(?:phone[:\\s]+)?([+\\d][\\d\\s-]{8,})",
            "Not Found"
        );
        response.setPhone(phone);
        
        // Extract Skills (look for common patterns)
        List<String> skills = new ArrayList<>();
        String[] skillKeywords = {
            "Java", "Python", "JavaScript", "React", "Angular", "Vue",
            "Spring Boot", "Spring", "Node.js", "Express",
            "AWS", "Azure", "GCP", "Docker", "Kubernetes",
            "PostgreSQL", "MySQL", "MongoDB", "Redis",
            "Git", "Jenkins", "CI/CD", "Microservices",
            "REST API", "GraphQL", "HTML", "CSS", "TypeScript"
        };
        
        for (String skill : skillKeywords) {
            if (resumeText.toLowerCase().contains(skill.toLowerCase())) {
                skills.add(skill);
            }
        }
        response.setSkills(skills.isEmpty() ? Arrays.asList("General IT Skills") : skills);
        
        // Extract Experience (simple pattern)
        List<ParsedResumeResponse.Experience> experiences = new ArrayList<>();
        ParsedResumeResponse.Experience exp = new ParsedResumeResponse.Experience();
        
        String expPattern = "(?i)(\\d+)\\s*(?:\\+)?\\s*(?:years?|yrs?)";
        Pattern pattern = Pattern.compile(expPattern);
        Matcher matcher = pattern.matcher(resumeText);
        
        String totalExp = "0 years";
        if (matcher.find()) {
            totalExp = matcher.group(1) + " years";
            exp.setCompany("Previous Company");
            exp.setRole("Software Engineer");
            exp.setDuration(totalExp);
            exp.setDescription("Professional experience in software development");
            experiences.add(exp);
        }
        response.setExperience(experiences);
        response.setTotalExperience(totalExp);
        
        // Extract Education
        List<ParsedResumeResponse.Education> education = new ArrayList<>();
        ParsedResumeResponse.Education edu = new ParsedResumeResponse.Education();
        
        if (resumeText.toLowerCase().contains("b.tech") || 
            resumeText.toLowerCase().contains("bachelor")) {
            edu.setDegree("B.Tech");
            edu.setInstitution("University");
            edu.setYear("2020");
            edu.setFieldOfStudy("Computer Science");
            education.add(edu);
        }
        response.setEducation(education);
        
        // Generate Summary
        String summary = "Experienced professional with skills in " + 
            String.join(", ", skills.subList(0, Math.min(3, skills.size()))) + 
            ". Total experience: " + totalExp;
        response.setSummary(summary);
        
        log.info("✅ Fallback parsing completed: {} skills, {} years exp", 
            skills.size(), totalExp);
        
        return response;
    }

    // Helper method for regex extraction - THIS WAS MISSING!
    private String extractPattern(String text, String regex, String defaultValue) {
        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(text);
            return matcher.find() ? matcher.group(1).trim() : defaultValue;
        } catch (Exception e) {
            log.debug("Pattern extraction failed for regex: {}", regex);
            return defaultValue;
        }
    }

    @Override
    public String extractTextFromFile(MultipartFile file) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extractTextFromFile'");
    }
}