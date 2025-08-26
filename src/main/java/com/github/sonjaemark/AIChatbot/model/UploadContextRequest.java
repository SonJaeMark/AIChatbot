package com.github.sonjaemark.AIChatbot.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class UploadContextRequest {
    // We accept either a single string or a list of strings for convenience.
    private List<String> context = new ArrayList<>();

    public List<String> getContext() {
        return context;
    }

    // Allow both { "context": "text" } and { "context": ["a","b"] }
    @JsonProperty("context")
    public void setContext(Object ctx) {
        if (ctx == null) {
            this.context = new ArrayList<>();
        } else if (ctx instanceof String) {
            this.context = List.of((String) ctx);
        } else if (ctx instanceof List<?>) {
            List<String> list = new ArrayList<>();
            for (Object o : (List<?>) ctx) {
                if (o != null) list.add(o.toString());
            }
            this.context = list;
        } else {
            this.context = new ArrayList<>();
        }
    }
}
