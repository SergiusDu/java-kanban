package com.tasktracker.server.handler;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.tasktracker.server.dto.ApiErrorMessage;
import com.tasktracker.server.operation.TaskCreationOperation;
import com.tasktracker.server.operation.TaskUpdateOperation;
import com.tasktracker.server.utility.ResponseUtility;
import com.tasktracker.task.dto.*;
import com.tasktracker.task.exception.OverlapException;
import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.manager.TaskManager;
import com.tasktracker.task.model.implementations.Task;
import com.tasktracker.task.store.exception.TaskNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class TaskPostHandler<C extends TaskCreationDTOs, U extends TaskUpdateDTOs>
    extends BaseHttpHandler {
  private final Class<C> createDtoClass;
  private final Class<U> updateDtoClass;
  private final TaskCreationOperation<C, Task> addOperation;
  private final TaskUpdateOperation<U, Task> updateOperation;

  public TaskPostHandler(
      TaskManager taskManager,
      Class<C> createDtoClass,
      Class<U> updateDtoClass,
      TaskCreationOperation<C, Task> addOperation,
      TaskUpdateOperation<U, Task> updateOperation) {
    super(taskManager);
    this.createDtoClass = createDtoClass;
    this.updateDtoClass = updateDtoClass;
    this.addOperation = addOperation;
    this.updateOperation = updateOperation;
  }

  @Override
  protected void handleRequest(HttpExchange exchange, Map<String, String> pathParameters)
      throws IOException {
    try (InputStreamReader in =
        new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {

      JsonObject json = gson.fromJson(in, JsonObject.class);

      Task taskToReturn;

      if (json.has("id") && !json.get("id").isJsonNull()) {
        U updateDto = convertAndValidateDto(json, this.updateDtoClass);
        taskToReturn = updateOperation.apply(updateDto);
      } else {
        C createDto = convertAndValidateDto(json, this.createDtoClass);
        taskToReturn = addOperation.apply(createDto);
      }

      if (taskToReturn == null) {
        ResponseUtility.sendInternalServerError(
            exchange, new ApiErrorMessage("Task operation did not return the expected result."));
        return;
      }

      ResponseUtility.sendJsonResponse(exchange, ResponseUtility.HTTP_CREATED, taskToReturn);
    } catch (ValidationException | JsonSyntaxException e) {
      ResponseUtility.sendBadRequest(exchange, new ApiErrorMessage(e.getMessage()));
    } catch (TaskNotFoundException e) {
      ResponseUtility.sendNotFound(exchange, new ApiErrorMessage(e.getMessage()));
    } catch (OverlapException e) {
      ResponseUtility.sendNotAcceptableError(exchange, new ApiErrorMessage(e.getMessage()));
    } finally {
      exchange.close();
    }
  }
}
