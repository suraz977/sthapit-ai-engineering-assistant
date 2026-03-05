package com.sthapit.assistant.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "requests")
public class RequestEntity {

  @Id
  private String requestId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RequestStatus status;

  @Column(columnDefinition = "TEXT")
  private String requestText;

  @Column(columnDefinition = "TEXT")
  private String errorMessage;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  protected RequestEntity() {}

  public RequestEntity(String requestId, RequestStatus status, String requestText) {
    this.requestId = requestId;
    this.status = status;
    this.requestText = requestText;
    this.createdAt = Instant.now();
    this.updatedAt = this.createdAt;
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = Instant.now();
  }

  public void touch() {
    this.updatedAt = Instant.now();
  }

  // getters/setters

  public String getRequestId() { return requestId; }
  public RequestStatus getStatus() { return status; }
  public void setStatus(RequestStatus status) { this.status = status; touch(); }

  public String getRequestText() { return requestText; }
  public void setRequestText(String requestText) { this.requestText = requestText; touch(); }

  public String getErrorMessage() { return errorMessage; }
  public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; touch(); }

  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}