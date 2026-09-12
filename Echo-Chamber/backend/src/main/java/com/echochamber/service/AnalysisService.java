package com.echochamber.service;

import com.echochamber.model.AnalysisHistoryItem;
import com.echochamber.model.AnalysisResponse;
import com.echochamber.model.Bias;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AnalysisService {

    @Value("${gemini.api.key:${AI_API_KEY:DEMO_MOCK_KEY}}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, AnalysisHistoryItem> historyStorage = new ConcurrentHashMap<>();

    public AnalysisService() {
    }

    public AnalysisResponse analyzeStatement(String statement) {
        AnalysisResponse response;

        if (apiKey == null || apiKey.isBlank() || "DEMO_MOCK_KEY".equalsIgnoreCase(apiKey)) {
            response = generateLocalCognitiveAnalysis(statement);
        } else {
            try {
                response = callGeminiApi(statement);
            } catch (Exception e) {
                System.err.println("Gemini API call failed: " + e.getMessage());
                response = generateLocalCognitiveAnalysis(statement);
            }
        }

        String id = UUID.randomUUID().toString();
        AnalysisHistoryItem item = new AnalysisHistoryItem(id, statement, response.getScore(), LocalDate.now(), response);
        historyStorage.put(id, item);

        return response;
    }

    public List<AnalysisHistoryItem> getHistory() {
        return new ArrayList<>(historyStorage.values());
    }

    public void clearHistory() {
        historyStorage.clear();
    }

    private AnalysisResponse callGeminiApi(String statement) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String prompt = """
            You are an objective perspective analyzer and cognitive bias detector.
            Analyze the following user statement: "%s"
            
            Return ONLY a raw, valid JSON object matching exactly this schema:
            {
              "score": <integer between 40 and 95>,
              "positivePoints": ["point 1", "point 2", "point 3"],
              "negativePoints": ["point 1", "point 2", "point 3"],
              "alternativePerspective": "A clear, thought-provoking counter viewpoint",
              "biases": [
                { "name": "Bias Name", "explanation": "Why this statement exhibits it", "severity": "High" }
              ],
              "balancedConclusion": "A synthesized, neutral summary concluding the argument"
            }
            Do not enclose in markdown code blocks like ```json. Output raw JSON text only.
            """.formatted(statement.replace("\"", "\\\""));

        Map<String, Object> textPart = Map.of("text", prompt);
        Map<String, Object> contentPart = Map.of("parts", List.of(textPart));
        Map<String, Object> requestBody = Map.of("contents", List.of(contentPart));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        JsonNode root = objectMapper.readTree(response.getBody());
        String rawText = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

        // Strip backticks if the model still wrapped JSON
        String cleanedJson = rawText.replaceAll("(?s)```json\\s*", "").replaceAll("```", "").trim();

        return objectMapper.readValue(cleanedJson, AnalysisResponse.class);
    }

    private AnalysisResponse generateLocalCognitiveAnalysis(String statement) {
        String lower = statement.toLowerCase();
        List<String> positive = new ArrayList<>();
        List<String> negative = new ArrayList<>();
        List<Bias> biases = new ArrayList<>();
        String flipped;
        String conclusion;
        int score = 75;

        if (lower.contains("programmer") || lower.contains("ai") || lower.contains("code")) {
            positive.addAll(List.of(
                "AI automates repetitive coding tasks and syntax boilerplate",
                "Developers become significantly more productive and ship faster",
                "AI assists in intelligent debugging and test generation"
            ));
            negative.addAll(List.of(
                "AI struggles with complex system architecture and ambiguous requirements",
                "Overdependence may lead to unverified security flaws",
                "May reduce deep fundamental problem-solving intuition"
            ));
            flipped = "AI may not replace programmers; instead, programmers who use AI will outperform and replace those who do not.";
            biases.add(new Bias("All-or-Nothing Thinking", "Viewing the outcome as complete job loss rather than a workflow evolution.", "Medium"));
            conclusion = "AI is set to transform software development drastically, shifting the developer's role toward architecture, problem decomposition, and critical review.";
            score = 78;
        } else if (lower.contains("social media") || lower.contains("harmful")) {
            positive.addAll(List.of(
                "Enables global community building and instant knowledge sharing",
                "Empowers independent creators and small businesses",
                "Gives voice to underrepresented causes and ideas"
            ));
            negative.addAll(List.of(
                "Algorithm design often amplifies outrage and polarization",
                "Associated with reduced attention spans and mental fatigue",
                "Drives superficial validation loops and echo chambers"
            ));
            flipped = "Social media is merely a communication magnifier; whether it harms or helps depends on algorithmic transparency and intentional usage.";
            biases.add(new Bias("Generalization", "Assigning universal negative traits across distinct platforms and use cases.", "High"));
            conclusion = "Social media delivers unparalleled global reach, but intentional usage habits and healthy digital boundaries are essential.";
            score = 68;
        } else {
            positive.addAll(List.of(
                "Highlights specific priorities and execution criteria for: " + statement,
                "Brings attention to a distinct, actionable viewpoint",
                "Encourages active discussion on the core subject"
            ));
            negative.addAll(List.of(
                "Risk of oversimplifying nuanced real-world complexities",
                "Potential unmeasured trade-offs if adopted unconditionally",
                "May not apply universally across distinct circumstances"
            ));
            flipped = "An alternate perspective reveals valid counter-considerations when tested under varied constraints.";
            biases.add(new Bias("Confirmation Bias", "Leaning heavily into assumptions that validate an initial premise.", "Medium"));
            conclusion = "A complete assessment requires balancing practical upsides against situational trade-offs.";
            score = 70;
        }

        return new AnalysisResponse(score, positive, negative, flipped, biases, conclusion);
    }
}
