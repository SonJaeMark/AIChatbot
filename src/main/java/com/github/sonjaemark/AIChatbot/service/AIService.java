package com.github.sonjaemark.AIChatbot.service;

import com.github.sonjaemark.AIChatbot.model.ChatRequest;
import com.github.sonjaemark.AIChatbot.model.ConversationState;
import com.github.sonjaemark.AIChatbot.model.GeminiResponse;
import com.github.sonjaemark.AIChatbot.model.UploadContextRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AIService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.endpoint}")
    private String geminiEndpoint;

    private final RestTemplate restTemplate = new RestTemplate();

    // sessionId -> state (memory is temporary; lost when app restarts)
    private final Map<String, ConversationState> sessions = new ConcurrentHashMap<>();

    // Keep convo focused: include only the last N turns when calling Gemini
    private static final int MAX_TURNS = 12;

    // Build the text we send to Gemini
    private String composePrompt(ConversationState state) {
        StringBuilder sb = new StringBuilder();
        // "System prompt" style instruction
        if (state.getInstruction() != null && !state.getInstruction().isBlank()) {
            sb.append("INSTRUCTION:\n")
              .append(state.getInstruction().trim())
              .append("\n\n");
        }
        if (state.getContext() != null && !state.getContext().isEmpty()) {
            sb.append("CONTEXT:\n");
            for (String c : state.getContext()) {
                sb.append("- ").append(c).append("\n");
            }
            sb.append("\n");
        }
        sb.append("CONVERSATION (most recent first):\n");

        List<String> history = state.getHistory();
        int from = Math.max(0, history.size() - MAX_TURNS);
        for (int i = from; i < history.size(); i++) {
            sb.append(history.get(i)).append("\n");
        }

        sb.append("\nPlease respond concisely and stay on topic.\n");
        sb.append("If you are unsure, ask a clarifying question.\n");
        return sb.toString();
    }

    private String callGemini(String text) {
        Map<String, Object> payload = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", text)))
                )
        );
        String url = geminiEndpoint + "?key=" + geminiApiKey;

        GeminiResponse response = restTemplate.postForObject(url, payload, GeminiResponse.class);

        try {
            return response.getCandidates()
                           .get(0)
                           .getContent()
                           .getParts()
                           .get(0)
                           .getText();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Failed to parse Gemini response: " + e.getMessage());
        }
    }

    // GET /ai/hello
    public String hello() {
        return callGemini("Say hello in a friendly, human-like way.");
    }

    // POST /ai/upload-context?instruction=...
    public String uploadContext(UploadContextRequest req, String instruction) {
        String sessionId = UUID.randomUUID().toString();

        ConversationState state = new ConversationState();
        state.setInstruction(instruction != null ? instruction : "");
        state.setContext(req.getContext());

        // seed history with a short primer (optional)
        state.getHistory().add("System: Context uploaded. Follow the instruction and use context when answering.");
        state.touch();

        sessions.put(sessionId, state);
        return sessionId;
    }

    // POST /ai/chat
    public String chat(ChatRequest chat) {
        if (chat.getSessionId() == null || chat.getSessionId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sessionId is required");
        }
        ConversationState state = sessions.get(chat.getSessionId());
        if (state == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown sessionId");
        }

        // Add user message
        state.getHistory().add("User: " + chat.getMessage());
        state.touch();

        // Compose prompt and call Gemini
        String prompt = composePrompt(state);
        String reply = callGemini(prompt);

        // Save AI reply for continuity
        state.getHistory().add("AI: " + reply);
        state.touch();

        return reply;
    }
}
