package com.tasktracker.server.dispatcher;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public interface Dispatcher extends HttpHandler {
  @Override
  void handle(HttpExchange exchange) throws IOException;
}
