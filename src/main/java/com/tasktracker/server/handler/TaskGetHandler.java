package com.tasktracker.server.handler;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.tasktracker.server.utility.ResponseUtility;
import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.manager.TaskManager;
import com.tasktracker.task.model.implementations.Task;
import com.tasktracker.task.store.exception.TaskNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class TaskGetHandler extends BaseHttpHandler {
  private final Class<?> taskClass;

  public TaskGetHandler(TaskManager taskManager, Class<?> taskClass) {
    super(taskManager);
    this.taskClass = taskClass;
  }

  @Override
  public void handleRequest(HttpExchange exchange, Map<String, String> pathParameters)
      throws IOException, ValidationException, JsonSyntaxException, TaskNotFoundException {
    String stringId = pathParameters.get("id");

    if (stringId == null || stringId.isEmpty()) {
      throw new ValidationException("Task ID is required but was either null or empty");
    }

    UUID id;
    try {
      id = UUID.fromString(stringId);
    } catch (IllegalArgumentException e) {
      throw new ValidationException("Invalid UUID format for task ID: " + stringId);
    }

    Optional<Task> taskOptional = taskManager.getTask(id);

    if (taskOptional.isEmpty()) throw new TaskNotFoundException("Task not found with id: " + id);

    Task task = taskOptional.get();

    if (!taskClass.isInstance(task))
      throw new TaskNotFoundException(
          "Task found was not of expected type " + taskClass.getSimpleName());

    ResponseUtility.sendJsonOkResponse(exchange, taskOptional.get());
  }
}
