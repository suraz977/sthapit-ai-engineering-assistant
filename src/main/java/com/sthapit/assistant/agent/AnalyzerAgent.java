package com.sthapit.assistant.agent;

import com.sthapit.assistant.kafka.AgentBus;
import com.sthapit.assistant.model.AgentResult;
import com.sthapit.assistant.model.AgentTask;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AnalyzerAgent {
  private static final Logger log = LoggerFactory.getLogger(AnalyzerAgent.class);
  private final AgentBus bus;
  private final OpenAiChatModel model;

  public AnalyzerAgent(AgentBus bus, @Value("${app.openai.model}") String modelName) {
    this.bus = bus;

    String apiKey = System.getenv("OPENAI_API_KEY");
    if (apiKey == null || apiKey.isBlank()) {
      throw new IllegalStateException("OPENAI_API_KEY environment variable is not set.");
    }

    this.model = OpenAiChatModel.builder()
        .apiKey(apiKey)
        .modelName(modelName)
        .build();
  }

  @KafkaListener(
          topics = "${app.topics.tasks}",
          groupId = "analyzer",
          containerFactory = "taskListenerFactory"
  )
  public void onTask(AgentTask task) {

    // ✅ Only handle ANALYZE tasks
    if (!"ANALYZE".equals(task.type())) {
      log.info("AnalyzerAgent ignoring task type={} requestId={}", task.type(), task.requestId());
      return;
    }

    log.info("AnalyzerAgent received task type=ANALYZE requestId={}", task.requestId());
    log.info("AnalyzerAgent calling OpenAI requestId={}", task.requestId());

    // ✅ Define prompt BEFORE calling model.generate(...)
    String prompt = """
    You are a senior software architect.
    Analyze the user's request and output:
    - Top 5 risks/issues (scalability, reliability, security, maintainability)
    - Top 8 concrete recommendations (actionable and specific)
    - Suggested implementation steps

    Keep output concise and structured in markdown bullets.

    User request:
    %s
    """.formatted(task.payload());

    String analysis = model.generate(prompt);

    log.info("AnalyzerAgent produced ANALYSIS_DONE requestId={}", task.requestId());

    bus.sendResult(new AgentResult(
            task.requestId(),
            "AnalyzerAgent",
            "ANALYSIS_DONE",
            analysis
    ));
  }
}