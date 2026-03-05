package com.sthapit.assistant.model;

public record AgentTask(
    String requestId,
    String type,     // PLAN, ANALYZE, REPORT
    String payload   // plain text for v1
) {}