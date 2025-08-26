package com.github.sonjaemark.AIChatbot.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ConversationState {
    private String instruction;
    private List<String> context = new ArrayList<>();
    private final List<String> history = new ArrayList<>();
    private Instant lastActivity = Instant.now();

    public String getInstruction() { return instruction; }
    public void setInstruction(String instruction) { this.instruction = instruction; }

    public List<String> getContext() { return context; }
    public void setContext(List<String> context) { this.context = context; }

    public List<String> getHistory() { return history; }

    public Instant getLastActivity() { return lastActivity; }
    public void touch() { this.lastActivity = Instant.now(); }
}
