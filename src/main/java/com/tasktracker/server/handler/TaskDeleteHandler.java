package com.tasktracker.server.handler;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.tasktracker.server.utility.ResponseUtility;
import com.tasktracker.task.exception.OverlapException;
import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.manager.TaskManager;
import com.tasktracker.task.model.implementations.Task;
import com.tasktracker.task.store.exception.TaskNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class TaskDeleteHandler<T extends Task> extends BaseHttpHandler {
  private final Class<T> taskClass;

  public TaskDeleteHandler(TaskManager taskManager, Class<T> taskClass) {
    super(taskManager);
    this.taskClass = taskClass;
  }

  @Override
  protected void handleRequest(HttpExchange exchange, Map<String, String> pathParameters)
      throws IOException,
          ValidationException,
          JsonSyntaxException,
          TaskNotFoundException,
          OverlapException {

    String stringId = pathParameters.get("id");

    if (stringId == null || stringId.trim().isEmpty()) {
      throw new ValidationException("Task ID is required in the path and cannot be empty.");
    }

    UUID id;
    try {
      id = UUID.fromString(stringId);
    } catch (IllegalArgumentException e) {
      throw new ValidationException("Invalid task ID format. Expected UUID format: " + stringId);
    }

    Optional<Task> optionalTaskBeforeDelete = taskManager.getTask(id);

    if (optionalTaskBeforeDelete.isEmpty()) {
      throw new TaskNotFoundException("Task with id: " + id + " not found for deletion.");
    }

    Task taskToCheck = optionalTaskBeforeDelete.get();

    if (!taskClass.isInstance(taskToCheck)) {
      throw new TaskNotFoundException(
          "Task with id "
              + id
              + " found, but was of type "
              + taskToCheck.getClass().getSimpleName()
              + ", expected type "
              + taskClass.getSimpleName()
              + " for this delete operation.");
    }

    Optional<Task> optionalRemovedTask = taskManager.removeTaskById(id);

    ResponseUtility.sendJsonOkResponse(exchange, optionalRemovedTask.orElse(null));
  }
}
