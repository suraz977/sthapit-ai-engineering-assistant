package com.sthapit.assistant.model;

public record AgentResult(
    String requestId,
    String agent,    // PlannerAgent, AnalyzerAgent, ReportAgent
    String type,     // PLAN_DONE, ANALYSIS_DONE, REPORT_DONE
    String content
) {}