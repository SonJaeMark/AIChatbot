package com.github.sonjaemark.AIChatbot.controller;

import com.github.sonjaemark.AIChatbot.model.ChatRequest;
import com.github.sonjaemark.AIChatbot.model.UploadContextRequest;
import com.github.sonjaemark.AIChatbot.service.AIService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    // Test Gemini connectivity
    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of("reply", aiService.hello());
    }

    // Initialize a session with context + instruction, returns sessionId
    @PostMapping("/upload-context")
    public Map<String, String> uploadContext(
            @RequestBody UploadContextRequest body,
            @RequestParam String instruction) {

        String sessionId = aiService.uploadContext(body, instruction);
        return Map.of("sessionId", sessionId);
    }

    // Chat within a session (keeps in-memory history so convo stays on topic)
    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody ChatRequest request) {
        String reply = aiService.chat(request);
        return Map.of("reply", reply);
    }
}
