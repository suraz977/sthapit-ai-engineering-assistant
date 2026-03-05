package com.sthapit.assistant.kafka;

import com.sthapit.assistant.model.AgentResult;
import com.sthapit.assistant.model.AgentTask;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AgentBus {

  private final KafkaTemplate<String, Object> kafka;
  private final String tasksTopic;
  private final String resultsTopic;

  public AgentBus(
      KafkaTemplate<String, Object> kafka,
      @Value("${app.topics.tasks}") String tasksTopic,
      @Value("${app.topics.results}") String resultsTopic
  ) {
    this.kafka = kafka;
    this.tasksTopic = tasksTopic;
    this.resultsTopic = resultsTopic;
  }

  public void sendTask(AgentTask task) {
    kafka.send(tasksTopic, task.requestId(), task);
  }

  public void sendResult(AgentResult result) {
    kafka.send(resultsTopic, result.requestId(), result);
  }
}