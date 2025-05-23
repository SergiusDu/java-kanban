package com.tasktracker.server;

import com.sun.net.httpserver.HttpServer;
import com.tasktracker.server.dispatcher.Dispatcher;
import com.tasktracker.server.dispatcher.DispatcherHandler;
import com.tasktracker.server.exceptions.DuplicateParameterException;
import com.tasktracker.server.filter.RequestValidationFilter;
import com.tasktracker.server.handler.*;
import com.tasktracker.server.router.Router;
import com.tasktracker.task.dto.*;
import com.tasktracker.task.manager.TaskManager;
import com.tasktracker.task.model.implementations.EpicTask;
import com.tasktracker.task.model.implementations.RegularTask;
import com.tasktracker.task.model.implementations.SubTask;
import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
  private static final String TASKS_PATH = "/tasks";
  private static final String SUBTASKS_PATH = "/subtasks";
  private static final String EPICS_PATH = "/epics";
  private static final String HISTORY_PATH = "/history";
  private static final String PRIORITIZED_PATH = "/prioritized";
  private static final String ID_PATH_PARAM = "/{id}";
  private static final String POST_METHOD = "POST";
  private static final String GET_METHOD = "GET";
  private static final String DELETE_METHOD = "DELETE";
  private final TaskManager manager;
  private final Router router;
  private final HttpServer server;

  public HttpTaskServer(TaskManager taskManager, Router router)
      throws IOException, DuplicateParameterException {
    this.manager = taskManager;
    this.router = router;
    Dispatcher dispatcher = new DispatcherHandler(router);
    server = HttpServer.create(new InetSocketAddress(8080), 0);
    server.createContext("/", dispatcher).getFilters().add(new RequestValidationFilter());
    setupRoutes();
  }

  public void start() {
    server.start();
  }

  public void stop() {
    server.stop(1);
  }

  private void setupRoutes() throws DuplicateParameterException {
    // POST METHODS
    router.addRoute(
        TASKS_PATH,
        POST_METHOD,
        new TaskPostHandler<>(
            manager,
            RegularTaskCreationDTO.class,
            RegularTaskUpdateDTO.class,
            manager::addTask,
            manager::updateTask));
    router.addRoute(
        SUBTASKS_PATH,
        POST_METHOD,
        new TaskPostHandler<>(
            manager,
            SubTaskCreationDTO.class,
            SubTaskUpdateDTO.class,
            manager::addTask,
            manager::updateTask));
    router.addRoute(
        EPICS_PATH,
        POST_METHOD,
        new TaskPostHandler<>(
            manager,
            EpicTaskCreationDTO.class,
            EpicTaskUpdateDTO.class,
            manager::addTask,
            manager::updateTask));

    // GET METHODS
    router.addRoute(
        TASKS_PATH + ID_PATH_PARAM, GET_METHOD, new TaskGetHandler(manager, RegularTask.class));
    router.addRoute(
        SUBTASKS_PATH + ID_PATH_PARAM, GET_METHOD, new TaskGetHandler(manager, SubTask.class));
    router.addRoute(
        EPICS_PATH + ID_PATH_PARAM, GET_METHOD, new TaskGetHandler(manager, EpicTask.class));

    router.addRoute(TASKS_PATH, GET_METHOD, new TaskListGetHandler<>(manager, RegularTask.class));
    router.addRoute(SUBTASKS_PATH, GET_METHOD, new TaskListGetHandler<>(manager, SubTask.class));
    router.addRoute(EPICS_PATH, GET_METHOD, new TaskListGetHandler<>(manager, EpicTask.class));

    // DELETE METHODS
    router.addRoute(
        TASKS_PATH + ID_PATH_PARAM,
        DELETE_METHOD,
        new TaskDeleteHandler<>(manager, RegularTask.class));
    router.addRoute(
        SUBTASKS_PATH + ID_PATH_PARAM,
        DELETE_METHOD,
        new TaskDeleteHandler<>(manager, SubTask.class));
    router.addRoute(
        EPICS_PATH + ID_PATH_PARAM,
        DELETE_METHOD,
        new TaskDeleteHandler<>(manager, EpicTask.class));

    // HISTORY
    router.addRoute(HISTORY_PATH, GET_METHOD, new HistoryGetHandler(manager));

    // PRIORITIZED
    router.addRoute(PRIORITIZED_PATH, GET_METHOD, new TasksPrioritizedListGetHandler(manager));
  }
}
