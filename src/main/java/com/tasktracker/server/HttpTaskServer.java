package com.tasktracker.server;

import com.sun.net.httpserver.HttpServer;
import com.tasktracker.server.dispatcher.DispatcherHandler;
import com.tasktracker.server.filter.RequestValidationFilter;
import com.tasktracker.server.handler.*;
import com.tasktracker.server.router.RequestRouter;
import com.tasktracker.task.dto.*;
import com.tasktracker.task.manager.TaskManager;
import com.tasktracker.task.model.implementations.EpicTask;
import com.tasktracker.task.model.implementations.RegularTask;
import com.tasktracker.task.model.implementations.SubTask;
import com.tasktracker.util.Managers;
import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
  private static final String TASKS_PATH = "/tasks";
  private static final String SUBTASKS_PATH = "/subtasks";
  private static final String EPICS_PATH = "/epics";
  private static final String ID_PATH_PARAM = "/{id}";
  private static final String POST_METHOD = "POST";
  private static final String GET_METHOD = "GET";
  private static final String DELETE_METHOD = "DELETE";

  public static void main(String[] args) throws IOException {
    try {
      TaskManager manager = Managers.getDefault();
      RequestRouter requestRouter = new RequestRouter();

      // POST METHODS
      requestRouter.addRoute(
          TASKS_PATH,
          POST_METHOD,
          new TaskPostHandler<>(
              manager,
              RegularTaskCreationDTO.class,
              RegularTaskUpdateDTO.class,
              manager::addTask,
              manager::updateTask));
      requestRouter.addRoute(
          SUBTASKS_PATH,
          POST_METHOD,
          new TaskPostHandler<>(
              manager,
              SubTaskCreationDTO.class,
              SubTaskUpdateDTO.class,
              manager::addTask,
              manager::updateTask));
      requestRouter.addRoute(
          EPICS_PATH,
          POST_METHOD,
          new TaskPostHandler<>(
              manager,
              EpicTaskCreationDTO.class,
              EpicTaskUpdateDTO.class,
              manager::addTask,
              manager::updateTask));

      // GET METHODS
      requestRouter.addRoute(
          TASKS_PATH + ID_PATH_PARAM, GET_METHOD, new TaskGetHandler(manager, RegularTask.class));
      requestRouter.addRoute(
          SUBTASKS_PATH + ID_PATH_PARAM, GET_METHOD, new TaskGetHandler(manager, SubTask.class));
      requestRouter.addRoute(
          EPICS_PATH + ID_PATH_PARAM, GET_METHOD, new TaskGetHandler(manager, EpicTask.class));

      requestRouter.addRoute(
          TASKS_PATH, GET_METHOD, new TaskListGetHandler<>(manager, RegularTask.class));
      requestRouter.addRoute(
          SUBTASKS_PATH, GET_METHOD, new TaskListGetHandler<>(manager, SubTask.class));
      requestRouter.addRoute(
          EPICS_PATH, GET_METHOD, new TaskListGetHandler<>(manager, EpicTask.class));

      // DELETE METHODS
      requestRouter.addRoute(
          TASKS_PATH + ID_PATH_PARAM,
          DELETE_METHOD,
          new TaskDeleteHandler<>(manager, RegularTask.class));
      requestRouter.addRoute(
          SUBTASKS_PATH + ID_PATH_PARAM,
          DELETE_METHOD,
          new TaskDeleteHandler<>(manager, SubTask.class));
      requestRouter.addRoute(
          EPICS_PATH + ID_PATH_PARAM,
          DELETE_METHOD,
          new TaskDeleteHandler<>(manager, EpicTask.class));

      // HISTORY
      requestRouter.addRoute("/history", GET_METHOD, new HistoryGetHandler(manager));

      // PRIORITIZED
      requestRouter.addRoute(
          "/prioritized", GET_METHOD, new TasksPrioritizedListGetHandler(manager));

      DispatcherHandler dispatcherHandler = new DispatcherHandler(requestRouter);
      HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
      server.createContext("/", dispatcherHandler).getFilters().add(new RequestValidationFilter());
      server.start();
    } catch (Exception e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
}
