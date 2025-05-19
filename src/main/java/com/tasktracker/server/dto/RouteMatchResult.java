package com.tasktracker.server.dto;

import com.tasktracker.server.handler.BaseHttpHandler;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public record RouteMatchResult(BaseHttpHandler handler, Map<String, String> pathParameters) {
  public RouteMatchResult {
    Objects.requireNonNull(handler, "handler cannot be null");
    Objects.requireNonNull(pathParameters, "pathParameters cannot be null");
    pathParameters = Collections.unmodifiableMap(pathParameters);
  }
}
