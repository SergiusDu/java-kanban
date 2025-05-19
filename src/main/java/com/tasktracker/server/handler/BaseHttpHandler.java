package com.tasktracker.server.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tasktracker.annotation.NotNull;
import com.tasktracker.json.GsonProvider;
import com.tasktracker.server.dto.ApiErrorMessage;
import com.tasktracker.server.utility.ResponseUtility;
import com.tasktracker.task.exception.OverlapException;
import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.manager.TaskManager;
import com.tasktracker.task.store.exception.TaskNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BaseHttpHandler implements HttpHandler {
  protected final TaskManager taskManager;
  protected final Gson gson;

  protected BaseHttpHandler(TaskManager taskManager) {
    this.taskManager = Objects.requireNonNull(taskManager, "Task manager can't be null");
    this.gson = Objects.requireNonNull(GsonProvider.getGson(), "Gson instance can't be null");
  }

  protected abstract void handleRequest(HttpExchange exchange, Map<String, String> pathParameters)
      throws IOException,
          ValidationException,
          JsonSyntaxException,
          TaskNotFoundException,
          OverlapException;

  public final void executeHandleRequest(HttpExchange exchange, Map<String, String> pathParameters)
      throws IOException {
    try {
      handleRequest(exchange, pathParameters);
    } catch (JsonSyntaxException e) {
      if (exchange.getResponseCode() == -1) {
        ResponseUtility.sendBadRequest(
            exchange,
            new ApiErrorMessage(
                "Invalid JSON format. Ensure the request body is correctly formatted. Details: "
                    + e.getMessage()));
      }
    } catch (ValidationException e) {
      if (exchange.getResponseCode() == -1) {
        ResponseUtility.sendBadRequest(exchange, new ApiErrorMessage(e.getMessage()));
      }
    } catch (TaskNotFoundException e) {
      if (exchange.getResponseCode() == -1) {
        ResponseUtility.sendNotFound(exchange, new ApiErrorMessage(e.getMessage()));
      }
    } catch (OverlapException e) {
      if (exchange.getResponseCode() == -1) {
        ResponseUtility.sendNotAcceptableError(exchange, new ApiErrorMessage(e.getMessage()));
      }
    } catch (IOException e) {
      if (exchange.getResponseCode() == -1) {
        ResponseUtility.sendInternalServerError(
            exchange, new ApiErrorMessage("An I/O error occurred."));
      } else {
        throw e;
      }
    } catch (Exception e) {
      e.printStackTrace();
      if (exchange.getResponseCode() == -1) {
        ResponseUtility.sendInternalServerError(
            exchange, new ApiErrorMessage("An unexpected internal server error occurred."));
      }
    }
  }

  @Override
  public final void handle(HttpExchange exchange) throws IOException {
    executeHandleRequest(exchange, Collections.emptyMap());
  }

  private boolean hasAllRequiredFields(JsonObject requestData, Class<?> dtoClass) {
    Objects.requireNonNull(requestData, "requestData cannot be null");
    Objects.requireNonNull(dtoClass, "dtoClass cannot be null");
    Set<String> requiredFields = new HashSet<>();
    if (dtoClass.isRecord()) {
      for (java.lang.reflect.RecordComponent component : dtoClass.getRecordComponents()) {
        if (component.isAnnotationPresent(NotNull.class)) {
          requiredFields.add(component.getName());
        }
      }
    } else {
      for (Field field : dtoClass.getDeclaredFields()) {
        if (field.isAnnotationPresent(NotNull.class)) {
          requiredFields.add(field.getName());
        }
      }
    }
    if (requiredFields.isEmpty()) return true;
    Set<String> presentAndNonNullFields =
        requestData.entrySet().stream()
            .filter(entry -> !entry.getValue().isJsonNull())
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    if (presentAndNonNullFields.size() < requiredFields.size()) return false;
    return presentAndNonNullFields.containsAll(requiredFields);
  }

  protected <T> T convertAndValidateDto(JsonObject requestData, Class<T> dtoType)
      throws ValidationException {
    if (!hasAllRequiredFields(requestData, dtoType)) {
      throw new ValidationException(
          "Отсутствуют обязательные поля для DTO: " + dtoType.getSimpleName());
    }
    try {
      return gson.fromJson(requestData, dtoType);
    } catch (JsonSyntaxException e) {
      throw new ValidationException(
          "Invalid JSON format for DTO fields: "
              + dtoType.getSimpleName()
              + ". Ошибка: "
              + e.getMessage());
    }
  }
}
