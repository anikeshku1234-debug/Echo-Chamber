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

    @Value("${ai.api.key:DEMO_MOCK_KEY}")
    private String apiKey;

    @Value("${ai.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${ai.api.model:gpt-4o-mini}")
    private String modelName;

    // Direct initialization to prevent any Spring bean injection error
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
                response = callRemoteAiApi(statement);
            } catch (Exception e) {
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

    private AnalysisResponse callRemoteAiApi(String statement) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        String systemPrompt = """
            You are an objective AI perspective analyzer.
            Analyze the user's opinion and output ONLY valid JSON matching this schema:
            {
              "score": <integer 0-100>,
              "positivePoints": ["string", "string"],
              "negativePoints": ["string", "string"],
              "alternativePerspective": "string",
              "biases": [
                { "name": "string", "explanation": "string", "severity": "High|Medium|Low" }
              ],
              "balancedConclusion": "string"
            }
            Do not include markdown ticks. Output pure JSON.
            """;

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", modelName);
        payload.put("messages", List.of(
            Map.of("role", "system", "content", systemPrompt),
            Map.of("role", "user", "content", statement)
        ));
        payload.put("temperature", 0.3);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, String.class);

        JsonNode rootNode = objectMapper.readTree(responseEntity.getBody());
        String content = rootNode.path("choices").get(0).path("message").path("content").asText();
        content = content.replaceAll("```json", "").replaceAll("```", "").trim();

        return objectMapper.readValue(content, AnalysisResponse.class);
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
                "Provides clear focus and straightforward execution criteria",
                "Offers potential efficiency and structured optimization",
                "Fosters innovation when applied within appropriate boundaries"
            ));
            negative.addAll(List.of(
                "Overlooks subtle counter-arguments and specific situational exceptions",
                "May introduce unforeseen trade-offs and secondary costs",
                "Lacks universal adaptability across distinct contexts"
            ));
            flipped = "The opposite premise holds equal weight when examined under conditions where contextual trade-offs dominate.";
            biases.add(new Bias("Confirmation Bias", "You may be focusing more heavily on data points confirming your preliminary intuition.", "Medium"));
            conclusion = "A balanced viewpoint requires weighing situational context rather than applying absolute conclusions.";
            score = 72;
        }

        return new AnalysisResponse(score, positive, negative, flipped, biases, conclusion);
    }
}