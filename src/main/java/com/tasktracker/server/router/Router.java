package com.tasktracker.server.router;

import com.tasktracker.server.dto.RouteMatchResult;
import com.tasktracker.server.exceptions.DuplicateParameterException;
import com.tasktracker.server.handler.BaseHttpHandler;
import java.net.URI;
import java.util.Optional;

public interface Router {
  RequestRouter addRoute(String pathTemplate, String httpMethod, BaseHttpHandler handler)
      throws DuplicateParameterException;

  Optional<RouteMatchResult> findRoute(URI uri, String httpMethod);

  boolean basePathExists(URI uri);
}
