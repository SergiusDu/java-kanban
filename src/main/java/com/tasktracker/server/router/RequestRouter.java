package com.tasktracker.server.router;

import com.tasktracker.server.dto.RouteMatchResult;
import com.tasktracker.server.exceptions.DuplicateParameterException;
import com.tasktracker.server.handler.BaseHttpHandler;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class RequestRouter implements Router {

  private final TrieNode root = new TrieNode();

  @Override
  public RequestRouter addRoute(String pathTemplate, String httpMethod, BaseHttpHandler handler)
      throws DuplicateParameterException {
    Objects.requireNonNull(pathTemplate, "Path template cannot be null");
    Objects.requireNonNull(httpMethod, "HTTP method cannot be null");
    Objects.requireNonNull(handler, "Handler cannot be null");
    String[] segments =
        Arrays.stream(pathTemplate.split("/")).filter(s -> !s.isEmpty()).toArray(String[]::new);

    TrieNode currentNode = root;
    for (String segment : segments) {
      if (segment.startsWith("{") && segment.endsWith("}")) {
        String currentParameterName = segment.substring(1, segment.length() - 1);
        if (currentNode.parametrizedChild == null) {
          currentNode.parametrizedChild = new TrieNode();
          currentNode.parameterName = currentParameterName;
          currentNode = currentNode.parametrizedChild;
        } else if (currentNode.parameterName.equals(currentParameterName)) {
          currentNode = currentNode.parametrizedChild;
        } else {
          throw new DuplicateParameterException(
              "Conflict: trying to define parameter {"
                  + currentParameterName
                  + "} where {"
                  + currentNode.parameterName
                  + "} is already defined at the same level.");
        }
      } else {
        currentNode = currentNode.children.computeIfAbsent(segment, k -> new TrieNode());
      }
    }
    currentNode.handlers.put(httpMethod.toUpperCase(), handler);
    return this;
  }

  @Override
  public Optional<RouteMatchResult> findRoute(URI uri, String httpMethod) {
    Objects.requireNonNull(uri, "uri не может быть null");
    Objects.requireNonNull(httpMethod, "httpMethod не может быть null");

    String path = uri.getPath();
    String[] segments =
        Arrays.stream(path.split("/")).filter(s -> !s.isEmpty()).toArray(String[]::new);

    TrieNode currentNode = root;
    Map<String, String> pathParameters = new HashMap<>();

    for (String segment : segments) {
      TrieNode nextNode = currentNode.children.get(segment);
      if (nextNode != null) {
        currentNode = nextNode;
      } else if (currentNode.parametrizedChild != null) {
        pathParameters.put(currentNode.parameterName, segment);
        currentNode = currentNode.parametrizedChild;
      } else {
        return Optional.empty();
      }
    }

    BaseHttpHandler handler = currentNode.handlers.get(httpMethod.toUpperCase());
    if (handler != null) {
      return Optional.of(new RouteMatchResult(handler, pathParameters));
    }
    return Optional.empty();
  }

  @Override
  public boolean basePathExists(URI uri) {
    Objects.requireNonNull(uri, "uri не может быть null");
    String path = uri.getPath();
    String[] segments =
        Arrays.stream(path.split("/")).filter(s -> !s.isEmpty()).toArray(String[]::new);

    TrieNode currentNode = root;
    for (String segment : segments) {
      TrieNode nextNode = currentNode.children.get(segment);
      if (nextNode != null) {
        currentNode = nextNode;
      } else if (currentNode.parametrizedChild != null) {
        currentNode = currentNode.parametrizedChild;
      } else {
        return false;
      }
    }
    return hasAnyHandlersRecursive(currentNode);
  }

  private boolean hasAnyHandlersRecursive(TrieNode node) {
    if (node == null) {
      return false;
    }
    if (!node.handlers.isEmpty()) {
      return true;
    }
    for (TrieNode child : node.children.values()) {
      if (hasAnyHandlersRecursive(child)) {
        return true;
      }
    }
    if (node.parametrizedChild != null) {
      return hasAnyHandlersRecursive(node.parametrizedChild);
    }
    return false;
  }

  private static class TrieNode {
    Map<String, TrieNode> children = new HashMap<>();
    TrieNode parametrizedChild = null;
    String parameterName = null;
    Map<String, BaseHttpHandler> handlers = new HashMap<>();
  }
}
