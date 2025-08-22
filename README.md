# WorkflowRunR POC

## Core Purpose
**WorkflowRunR** is a workflow execution engine that enables complex, observable, and restartable processes with external dependencies.

## Architecture Overview

### **Primary Innovation: Workflow Execution System**
- **Hierarchical Tracking**: Each workflow gets an execution context ID, each step gets a unique action ID
- **Observable Execution**: Real-time visibility into workflow progress with structured logging
- **Error Isolation**: Precise identification of failed steps in complex multi-step processes
- **External Process Integration**: Seamless execution of command-line tools (FFmpeg, Whisper, etc.)

### **Secondary but Critical: Lambda Serialization System**
- **Persistence Mechanism**: Enables workflows to be serialized and stored in database
- **Restart Capability**: Failed or interrupted workflows can be resumed from any point
- **Deferred Execution**: Workflows can be queued and executed asynchronously
- **State Preservation**: Captured variables and context are maintained across restarts

## Key Components

### **Core Workflow Engine** (`internal/`)
- `ExecutionContext`: Manages workflow execution with unique ID tracking
- `WorkflowAction`: Wraps individual steps with observability and error handling
- `Execution`: JPA entity for persisting workflow state
- `ExecutionContextImpl`: Utility for running external processes

### **Serialization Layer** (`lib/lambda/`)
- `WorkflowLambda`: Serializable functional interface for workflow definitions
- `LambdaSerializer`: Converts lambda expressions to byte arrays using Java's `SerializedLambda`
- `LambdaDeserializer`: Recreates lambdas at runtime with captured variables

## Example Workflow
```java
public String run(ExecutionContext ctx, String videoFile) {
    var audioFile = ctx.action("Extract audio track", () -> extractAudio(videoPath));
    return ctx.action("Transcribe audio to text", () -> transcribeAudio(audioFile));
}
```

Each `action()` call creates a trackable step with unique ID for monitoring and debugging.

## Value Proposition

**Problem Solved**: Complex workflows with external dependencies are lost on application restart. No native way to persist and resume multi-step processes.

**Solution**:
- ✅ Workflows persist across application restarts
- ✅ Precise error tracking and debugging
- ✅ Resumable workflows with state preservation
- ✅ Observable execution of long-running processes
- ✅ Seamless integration with external tools

## Current Status
This is a **proof-of-concept** demonstrating the core workflow execution and serialization capabilities. The Spring Boot components are boilerplate for demonstration purposes.

## Potential Applications
- Media processing pipelines (video/audio transcoding)
- Data processing workflows (ETL jobs, report generation)
- Integration workflows (API orchestration)
- Long-running background jobs with resumable state

## Technical Stack
- **Java 21** with Spring Boot 3.5.4
- **Kotlin** for some components
- **MySQL** for persistence with Flyway migrations
- **Docker Compose** for development database

## Getting Started
1. Start the MySQL database: `docker-compose up`
2. Run the application: `./gradlew bootRun`
3. Submit a workflow via API: `POST http://localhost:8080` with `{"file": "video.mp4"}`

The system enables writing workflows as regular Java/Kotlin code while providing persistence, observability, and restartability built-in.
