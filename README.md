# AI Engineering Assistant

Event-Driven Multi-Agent AI System built with **Spring Boot, Apache Kafka, OpenAI, and PostgreSQL**

---

# Project Overview

AI Engineering Assistant is a distributed backend system that analyzes software engineering requests using multiple AI agents.

The system receives a developer request such as:

> "Analyze my Spring Boot microservice architecture and suggest improvements."

Instead of processing the request synchronously, the system uses **Apache Kafka as an event bus** to coordinate multiple agents.

Each agent performs a specialized responsibility and communicates asynchronously through Kafka topics.

The final output is a structured **AI-generated engineering report** containing:

- Architecture risks
- Scalability improvements
- Engineering recommendations
- Implementation steps

---

# System Architecture

The application follows an **event-driven microservices architecture**.

```
Client
   |
   v
RequestController (REST API)
   |
Kafka Topic: agent.tasks
   |
PlannerAgent
   |
AnalyzerAgent (OpenAI)
   |
Kafka Topic: agent.results
   |
ReportAgent
   |
PostgreSQL
   |
GET /api/reports/{requestId}
```

---

# Technologies Used

| Technology | Purpose |
|--------|--------|
| Java 21 | Core programming language |
| Spring Boot | Backend framework |
| Apache Kafka | Event-driven communication |
| OpenAI API | AI architecture analysis |
| PostgreSQL | Persistent storage |
| Docker | Infrastructure setup |
| Maven | Build system |
| Git & GitHub | Version control |

---

# Core Functionalities

## Submit Engineering Request

API endpoint:

```
POST /api/requests
```

Example request:

```json
{
  "text": "Analyze my Spring Boot microservice architecture"
}
```

The API:

1. Generates a unique `requestId`
2. Saves the request in the database
3. Sends a task to Kafka

```
AgentTask(requestId, "PLAN", payload)
```

---

# Planner Agent

The **PlannerAgent** orchestrates the agent workflow.

It listens to the Kafka topic:

```
agent.tasks
```

When a `PLAN` task is received it creates the next task:

```
PLAN -> ANALYZE
```

PlannerAgent acts as the **workflow orchestrator**.

---


```

Kafka topic:

```
agent.results
```

---

# Report Agent

The **ReportAgent** aggregates results from multiple agents.

It listens to:

```
agent.results
```

The report is generated only after both results arrive:

```
PLAN_DONE
ANALYSIS_DONE
```

This pattern is called the **Event Aggregation Pattern**.

Once both are available:

1. Report is generated
2. Saved in PostgreSQL
3. API can return the result

---

# Retrieve Final Report

```
GET /api/reports/{requestId}
```

If the report is not ready:

```json
{
  "requestId": "...",
  "status": "not_found_yet",
  "hint": "Try again in a few seconds"
}
```

When completed:

```json
{
  "requestId": "...",
  "createdAt": "...",
  "markdown": "AI generated architecture report"
}
```

---

# Key Engineering Concepts Demonstrated

This project demonstrates several advanced backend concepts.

### Event-Driven Architecture

Services communicate using Kafka events instead of direct calls.

Benefits:

- Loose coupling
- Scalability
- Resilience
- Asynchronous processing

### Asynchronous Processing

Request lifecycle:

```
PENDING → PROCESSING → DONE
```

The API does not block while AI analysis runs.

---

### Event Aggregation Pattern

ReportAgent waits for multiple events before producing the final result.

This pattern is widely used in distributed systems.

---

# Challenges Faced During Implementation

During development several technical challenges occurred.

---

## Docker / Kafka Setup Issues

### Problem

Kafka failed to start because Docker daemon was not running.

Example error:

```
failed to connect to docker API
```

### Solution

Started Docker Desktop and verified Kafka containers.

---

## Kafka Consumer Configuration Error

### Error

```
No group.id found in consumer config
```

### Cause

Kafka consumers require a `groupId`.

### Fix

Added groupId to `@KafkaListener`:

```
@KafkaListener(
  topics = "${app.topics.tasks}",
  groupId = "planner"
)
```

---

## JSON Deserializer Configuration Error

### Error

```
JsonDeserializer must be configured with property setters or configuration properties
```

### Fix

Removed duplicate deserializer configuration.

---

## OpenAI API Configuration Errors

### Error

```
OPENAI_API_KEY environment variable is not set
```

### Fix

Exported environment variable:

```
export OPENAI_API_KEY=sk-xxxx
```

---

## Invalid API Key / Quota Errors

### Error

```
invalid_api_key
```

and

```
insufficient_quota
```

### Fix

Corrected API key formatting and verified billing.

---

## Agent Workflow Bug

AnalyzerAgent initially processed incorrect task types.

Fix:

```
if (!"ANALYZE".equals(task.type())) return;
```

---

## Race Condition in Report Generation

The report was generated before both results arrived.

Solution:

Used aggregation maps:

```
planByRequest
analysisByRequest
```

Report is generated only when both exist.

---

## GitHub Authentication Issues

Problems encountered while pushing code:

```
Permission denied (publickey)
Password authentication not supported
```

Solution:

Switched to HTTPS authentication and GitHub CLI login.

---

# Final Working Flow

```
Client Request
      |
      v
RequestController
      |
Kafka agent.tasks
      |
PlannerAgent
      |
AnalyzerAgent (OpenAI)
      |
Kafka agent.results
      |
ReportAgent
      |
PostgreSQL
      |
GET /api/reports/{requestId}
```

---

# Example AI Output

The generated report includes:

- Top engineering risks
- Scalability improvements
- Architecture recommendations
- Implementation steps


# Future Improvements

Potential enhancements:

- Add SecurityAgent and PerformanceAgent
- Use Kafka Streams for aggregation
- Build a frontend dashboard
- Deploy using Kubernetes
- Add monitoring with Prometheus and Grafana
