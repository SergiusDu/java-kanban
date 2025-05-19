package com.tasktracker.server.utility;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.tasktracker.json.GsonProvider;
import com.tasktracker.server.dto.ApiErrorMessage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ResponseUtility {
  public static final int HTTP_OK = 200;
  public static final int HTTP_CREATED = 201;
  public static final int HTTP_NO_CONTENT = 204;
  public static final int HTTP_BAD_REQUEST = 400;
  public static final int HTTP_NOT_FOUND = 404;
  public static final int HTTP_METHOD_NOT_ALLOWED = 405;
  public static final int HTTP_NOT_ACCEPTABLE = 406;
  public static final int HTTP_UNSUPPORTED_MEDIA_TYPE = 415;
  public static final int HTTP_INTERNAL_SERVER_ERROR = 500;
  public static final String CONTENT_TYPE_PLAIN_TEXT = "text/plain";
  public static final String CONTENT_TYPE_APPLICATION_JSON =
      "application/json; charset=" + StandardCharsets.UTF_8;
  private static final Gson gson = GsonProvider.getGson();

  public static void sendJsonResponse(HttpExchange exchange, int code, Object responseObject)
      throws IOException {
    sendResponse(exchange, code, CONTENT_TYPE_APPLICATION_JSON, objectToResponse(responseObject));
  }

  public static void sendBadRequest(HttpExchange exchange, ApiErrorMessage apiErrorMessage)
      throws IOException {
    sendResponse(
        exchange, HTTP_BAD_REQUEST, CONTENT_TYPE_PLAIN_TEXT, objectToResponse(apiErrorMessage));
  }

  public static void sendResponse(
      HttpExchange exchange, int statusCode, String contentType, byte[] responseBytes)
      throws IOException {
    exchange.getResponseHeaders().set("Content-Type", contentType);
    if (statusCode == HTTP_NO_CONTENT || responseBytes.length == 0) {
      exchange.sendResponseHeaders(statusCode, -1);
      return;
    }
    exchange.sendResponseHeaders(statusCode, responseBytes.length);
    try (OutputStream outputStream = exchange.getResponseBody()) {
      outputStream.write(responseBytes);
    }
  }

  public static void sendInternalServerError(HttpExchange exchange, ApiErrorMessage apiErrorMessage)
      throws IOException {
    sendResponse(
        exchange,
        HTTP_INTERNAL_SERVER_ERROR,
        CONTENT_TYPE_PLAIN_TEXT,
        objectToResponse(apiErrorMessage));
  }

  public static void sendNotAcceptableError(HttpExchange exchange, ApiErrorMessage apiErrorMessage)
      throws IOException {
    sendResponse(
        exchange, HTTP_NOT_ACCEPTABLE, CONTENT_TYPE_PLAIN_TEXT, objectToResponse(apiErrorMessage));
  }

  public static void sendJsonOkResponse(HttpExchange exchange, Object responseObject)
      throws IOException {
    sendJsonResponse(exchange, HTTP_OK, responseObject);
  }

  public static void sendNotFound(HttpExchange exchange, ApiErrorMessage apiErrorMessage)
      throws IOException {
    sendResponse(
        exchange, HTTP_NOT_FOUND, CONTENT_TYPE_PLAIN_TEXT, objectToResponse(apiErrorMessage));
  }

  public static void sendMethodNotAllowed(HttpExchange exchange, ApiErrorMessage apiErrorMessage)
      throws IOException {
    sendResponse(
        exchange,
        HTTP_METHOD_NOT_ALLOWED,
        CONTENT_TYPE_PLAIN_TEXT,
        objectToResponse(apiErrorMessage));
  }

  private static byte[] objectToResponse(Object responseObject) {
    return gson.toJson(responseObject, responseObject.getClass()).getBytes();
  }
}
