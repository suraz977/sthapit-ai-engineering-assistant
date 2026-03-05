package com.sthapit.assistant.api;

import com.sthapit.assistant.db.ReportRepository;
import com.sthapit.assistant.kafka.AgentBus;
import com.sthapit.assistant.model.AgentTask;
import com.sthapit.assistant.model.RequestEntity;
import com.sthapit.assistant.model.RequestStatus;
import com.sthapit.assistant.repository.RequestRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class RequestController {

  private final AgentBus bus;
  private final RequestRepository requests;
  private final ReportRepository reports;

  public RequestController(AgentBus bus, RequestRepository requests, ReportRepository reports) {
    this.bus = bus;
    this.requests = requests;
    this.reports = reports;
  }

  // ✅ define request DTO (this was missing in your file)
  public record CreateRequest(@NotBlank String text) {}

  @PostMapping("/requests")
  public ResponseEntity<?> create(@Valid @RequestBody CreateRequest req) {
    String requestId = UUID.randomUUID().toString();

    requests.save(new RequestEntity(
            requestId,
            RequestStatus.PENDING,
            req.text()
    ));

    bus.sendTask(new AgentTask(requestId, "PLAN", req.text()));

    return ResponseEntity.ok(Map.of(
            "requestId", requestId,
            "status", RequestStatus.PENDING.name()
    ));
  }

  @GetMapping("/reports/{requestId}")
  public ResponseEntity<?> getReport(@PathVariable String requestId) {

    RequestEntity req = requests.findById(requestId).orElse(null);
    if (req == null) {
      return ResponseEntity.notFound().build();
    }

    if (req.getStatus() == RequestStatus.FAILED) {
      return ResponseEntity.ok(Map.of(
              "requestId", requestId,
              "status", RequestStatus.FAILED.name(),
              "error", req.getErrorMessage() == null ? "Unknown error" : req.getErrorMessage()
      ));
    }

    if (req.getStatus() != RequestStatus.DONE) {
      return ResponseEntity.ok(Map.of(
              "requestId", requestId,
              "status", req.getStatus().name(),
              "hint", "Try again in a few seconds."
      ));
    }

    // DONE → return markdown
    return reports.findById(requestId)
            .<ResponseEntity<?>>map(r -> ResponseEntity.ok(Map.of(
                    "requestId", r.getRequestId(),
                    "status", RequestStatus.DONE.name(),
                    "createdAt", r.getCreatedAt(),
                    "markdown", r.getMarkdown()
            )))
            // rare case: status DONE but report missing
            .orElseGet(() -> ResponseEntity.ok(Map.of(
                    "requestId", requestId,
                    "status", RequestStatus.RUNNING.name(),
                    "hint", "Finalizing report—try again in a few seconds."
            )));
  }
}