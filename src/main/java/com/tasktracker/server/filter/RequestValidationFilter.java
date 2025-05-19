package com.tasktracker.server.filter;

import com.google.gson.Gson;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.tasktracker.json.GsonProvider;
import com.tasktracker.server.dto.ApiErrorMessage;
import com.tasktracker.server.utility.ResponseUtility;
import java.io.IOException;

public class RequestValidationFilter extends Filter {
  Gson gson = GsonProvider.getGson();

  @Override
  public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
    Headers headers = exchange.getRequestHeaders();
    String method = exchange.getRequestMethod().toUpperCase();
    if (method.equals("POST") || method.equals("PUT") || method.equals("PATCH")) {
      String contentType = headers.getFirst("Content-Type");
      if (contentType == null
          || contentType.isEmpty()
          || !contentType.toLowerCase().startsWith("application/json")) {
        ResponseUtility.sendJsonResponse(
            exchange,
            ResponseUtility.HTTP_UNSUPPORTED_MEDIA_TYPE,
            new ApiErrorMessage("Content-Type must be application/json"));
        return;
      }
      String contentLength = headers.getFirst("Content-Length");
      if ("0".equals(contentLength)) {
        ResponseUtility.sendJsonResponse(
            exchange,
            ResponseUtility.HTTP_BAD_REQUEST,
            new ApiErrorMessage("Request body cannot be empty"));
        return;
      }
    }
    chain.doFilter(exchange);
  }

  @Override
  public String description() {
    return "Base filter for validating incoming HTTP requests and ensuring they meet required criteria";
  }
}
