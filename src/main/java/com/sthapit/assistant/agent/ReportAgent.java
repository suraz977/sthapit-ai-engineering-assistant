package com.sthapit.assistant.agent;

import com.sthapit.assistant.db.ReportEntity;
import com.sthapit.assistant.db.ReportRepository;
import com.sthapit.assistant.kafka.AgentBus;
import com.sthapit.assistant.model.AgentResult;
import com.sthapit.assistant.model.AgentTask;
import com.sthapit.assistant.model.RequestStatus;
import com.sthapit.assistant.repository.RequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ReportAgent {
  private static final Logger log = LoggerFactory.getLogger(ReportAgent.class);

  private final AgentBus bus;
  private final ReportRepository reports;

  private final Map<String, String> analysisByRequest = new ConcurrentHashMap<>();
  private final Map<String, String> planByRequest = new ConcurrentHashMap<>();


  private final RequestRepository requests;



  // ✅ prevents infinite REPORT loops
  private final Map<String, Integer> reportRetryCount = new ConcurrentHashMap<>();

  public ReportAgent(AgentBus bus, ReportRepository reports, RequestRepository requests) {
    this.bus = bus;
    this.reports = reports;
      this.requests = requests;
  }

  @KafkaListener(
          topics = "${app.topics.results}",
          groupId = "reporter-results",
          containerFactory = "resultListenerFactory"
  )
  public void onResult(AgentResult result) {
    log.info("ReportAgent received result type={} requestId={}", result.type(), result.requestId());

    String requestId = result.requestId();

    if ("PLAN_DONE".equals(result.type())) {
      planByRequest.put(requestId, result.content());
    } else if ("ANALYSIS_DONE".equals(result.type())) {
      analysisByRequest.put(requestId, result.content());
    } else {
      return;
    }

    // ✅ Only save when both are present (order-independent)
    String plan = planByRequest.get(requestId);
    String analysis = analysisByRequest.get(requestId);

    if (plan == null || analysis == null) {
      log.info("ReportAgent waiting for missing data requestId={} (plan={}, analysis={})",
              requestId, plan != null, analysis != null);
      return;
    }

    String report = """
    # AI Engineering Assistant Report

    ## Plan
    %s

    ## Analysis & Recommendations
    %s
    """.formatted(plan, analysis);

    log.info("ReportAgent saving report requestId={}", requestId);
    reports.save(new ReportEntity(requestId, report));
    // Update request status → DONE
    requests.findById(requestId).ifPresent(r -> {
      r.setStatus(RequestStatus.DONE);
      requests.save(r);
    });
    bus.sendResult(new AgentResult(requestId, "ReportAgent", "REPORT_DONE", "Report stored in Postgres."));

    // cleanup to avoid memory growth
    planByRequest.remove(requestId);
    analysisByRequest.remove(requestId);
  }

}