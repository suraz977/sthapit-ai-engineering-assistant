# AI Engineering Assistant (Java + Kafka + OpenAI)

A multi-agent, event-driven AI assistant that turns engineering requests into structured architecture recommendations.

## Architecture
- **PlannerAgent**: creates a task plan
- **AnalyzerAgent**: calls OpenAI to generate risks + recommendations
- **ReportAgent**: compiles a markdown report and stores it in Postgres

Agents communicate via **Kafka** topics.

## Tech Stack
- Java 21 (works fine with newer JDKs; target 21)
- Spring Boot 3
- Kafka
- PostgreSQL + pgvector (Docker)
- LangChain4j + OpenAI

## Run Locally

### 1) Start infrastructure
```bash
cd docker
docker compose up -d
cd ..