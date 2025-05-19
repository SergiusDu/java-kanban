package com.tasktracker.server.dispatcher;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tasktracker.server.dto.ApiErrorMessage;
import com.tasktracker.server.dto.RouteMatchResult;
import com.tasktracker.server.handler.BaseHttpHandler;
import com.tasktracker.server.router.RequestRouter;
import com.tasktracker.server.utility.ResponseUtility;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

public class DispatcherHandler implements HttpHandler {
  private final RequestRouter router;

  public DispatcherHandler(RequestRouter router) {
    this.router = router;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    URI requestUri = exchange.getRequestURI();
    String requestPathString = requestUri.getPath();
    String requestMethod = exchange.getRequestMethod().toUpperCase();

    try {
      Optional<RouteMatchResult> matchResultOptional = router.findRoute(requestUri, requestMethod);

      if (matchResultOptional.isPresent()) {
        RouteMatchResult matchResult = matchResultOptional.get();
        HttpHandler handler = matchResult.handler();
        Map<String, String> pathParameters = matchResult.pathParameters();

        if (handler != null) {
          if (handler instanceof BaseHttpHandler) {
            ((BaseHttpHandler) handler).executeHandleRequest(exchange, pathParameters);
          } else {
            if (pathParameters != null && !pathParameters.isEmpty()) {
              for (Map.Entry<String, String> param : pathParameters.entrySet()) {
                exchange.setAttribute("pathParam_" + param.getKey(), param.getValue());
              }
            }
            handler.handle(exchange);
          }
          return;
        }
      }

      if (router.basePathExists(requestUri)) {
        ResponseUtility.sendMethodNotAllowed(
            exchange,
            new ApiErrorMessage(
                "Method " + requestMethod + " not allowed for " + requestPathString));
      } else {
        ResponseUtility.sendNotFound(
            exchange, new ApiErrorMessage("Resource not found: " + requestPathString));
      }
    } catch (Exception e) {
      System.err.println(
          "Exception during request dispatch for "
              + requestMethod
              + " "
              + requestPathString
              + ": "
              + e.getMessage());
      e.printStackTrace();

      if (exchange.getResponseCode() == -1) {
        try {
          ResponseUtility.sendInternalServerError(
              exchange,
              new ApiErrorMessage("An internal server error occurred during request processing."));
        } catch (Exception responseException) {
          System.err.println("Error sending error response: " + responseException.getMessage());
        }
      }
    }
  }
}
