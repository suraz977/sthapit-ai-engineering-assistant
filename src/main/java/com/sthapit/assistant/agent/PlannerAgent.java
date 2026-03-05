package com.sthapit.assistant.agent;

import com.sthapit.assistant.kafka.AgentBus;
import com.sthapit.assistant.model.AgentResult;
import com.sthapit.assistant.model.AgentTask;
import com.sthapit.assistant.model.RequestStatus;
import com.sthapit.assistant.repository.RequestRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class PlannerAgent {

  private static final Logger log = LoggerFactory.getLogger(PlannerAgent.class);

  private final AgentBus bus;

  private final RequestRepository requests;

  // constructor
  public PlannerAgent(AgentBus bus, RequestRepository requests) {
    this.bus = bus;
    this.requests = requests;
  }

  @KafkaListener(
          topics = "${app.topics.tasks}",
          groupId = "planner",
          containerFactory = "taskListenerFactory"
  )
  public void onTask(AgentTask task) {
    if (!"PLAN".equals(task.type())) {
      log.info("PlannerAgent ignoring task type={} requestId={}", task.type(), task.requestId());
      return;
    }

    String requestId = task.requestId();
    log.info("PlannerAgent received PLAN requestId={}", requestId);

    // ✅ Mark request RUNNING
    requests.findById(requestId).ifPresent(r -> {
      r.setStatus(RequestStatus.RUNNING);
      requests.save(r);
    });

    // ✅ Emit a plan so ReportAgent can include it
    String plan = """
    - Understand the request and define analysis objectives
    - Run AnalyzerAgent (LLM) to produce risks + recommendations
    - Assemble a markdown report and store it in Postgres
    - Mark request status DONE (or FAILED on error)
    """;

    bus.sendResult(new AgentResult(requestId, "PlannerAgent", "PLAN_DONE", plan));

    // ✅ Kick off next steps
    bus.sendTask(new AgentTask(requestId, "ANALYZE", task.payload()));
    //bus.sendTask(new AgentTask(requestId, "REPORT", task.payload()));
  }
}