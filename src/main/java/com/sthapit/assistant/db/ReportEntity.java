package com.sthapit.assistant.db;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "reports")
public class ReportEntity {

  @Id
  private String requestId;

  @Column(columnDefinition = "text", nullable = false)
  private String markdown;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  protected ReportEntity() {}

  public ReportEntity(String requestId, String markdown) {
    this.requestId = requestId;
    this.markdown = markdown;
  }

  public String getRequestId() { return requestId; }
  public String getMarkdown() { return markdown; }
  public Instant getCreatedAt() { return createdAt; }

  public void setMarkdown(String markdown) { this.markdown = markdown; }
}