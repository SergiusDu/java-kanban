package com.tasktracker.server.handler;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.tasktracker.server.utility.ResponseUtility;
import com.tasktracker.task.exception.OverlapException;
import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.manager.TaskManager;
import com.tasktracker.task.store.exception.TaskNotFoundException;
import java.io.IOException;
import java.util.Map;

public class HistoryGetHandler extends BaseHttpHandler {

  public HistoryGetHandler(TaskManager taskManager) {
    super(taskManager);
  }

  @Override
  protected void handleRequest(HttpExchange exchange, Map<String, String> pathParameters)
      throws IOException,
          ValidationException,
          JsonSyntaxException,
          TaskNotFoundException,
          OverlapException {
    ResponseUtility.sendJsonOkResponse(exchange, taskManager.getHistory());
  }
}
