package com.tasktracker.server;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpServer;
import com.tasktracker.json.GsonProvider;
import com.tasktracker.server.dispatcher.DispatcherHandler;
import com.tasktracker.server.filter.RequestValidationFilter;
import com.tasktracker.server.handler.*;
import com.tasktracker.server.router.RequestRouter;
import com.tasktracker.task.dto.*;
import com.tasktracker.task.exception.OverlapException;
import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.manager.InMemoryHistoryManager;
import com.tasktracker.task.manager.TaskManager;
import com.tasktracker.task.manager.TaskManagerImpl;
import com.tasktracker.task.model.enums.TaskStatus;
import com.tasktracker.task.model.implementations.EpicTask;
import com.tasktracker.task.model.implementations.RegularTask;
import com.tasktracker.task.model.implementations.SubTask;
import com.tasktracker.task.model.implementations.Task;
import com.tasktracker.task.store.InMemoryHistoryStore;
import com.tasktracker.task.store.InMemoryTaskRepository;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class HttpTaskServerTest {

  private static final int TEST_PORT = 8088;
  private static final String BASE_URL = "http://localhost:" + TEST_PORT;
  private TaskManager taskManager;
  private HttpServer server;
  private Gson gson;
  private HttpClient client;

  @BeforeEach
  void setUp() throws IOException {
    taskManager =
        new TaskManagerImpl(
            new InMemoryTaskRepository(), new InMemoryHistoryManager(new InMemoryHistoryStore()));
    gson = GsonProvider.getGson();
    client = HttpClient.newHttpClient();

    server = HttpServer.create(new InetSocketAddress(TEST_PORT), 0);
    server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

    RequestRouter requestRouter = new RequestRouter();
    try {
      requestRouter.addRoute(
          "/tasks",
          "POST",
          new TaskPostHandler<>(
              taskManager,
              RegularTaskCreationDTO.class,
              RegularTaskUpdateDTO.class,
              taskManager::addTask,
              taskManager::updateTask));
      requestRouter.addRoute(
          "/tasks/{id}", "GET", new TaskGetHandler(taskManager, RegularTask.class));
      requestRouter.addRoute(
          "/tasks", "GET", new TaskListGetHandler<>(taskManager, RegularTask.class));
      requestRouter.addRoute(
          "/tasks/{id}", "DELETE", new TaskDeleteHandler<>(taskManager, RegularTask.class));

      requestRouter.addRoute(
          "/subtasks",
          "POST",
          new TaskPostHandler<>(
              taskManager,
              SubTaskCreationDTO.class,
              SubTaskUpdateDTO.class,
              taskManager::addTask,
              taskManager::updateTask));
      requestRouter.addRoute(
          "/subtasks/{id}", "GET", new TaskGetHandler(taskManager, SubTask.class));
      requestRouter.addRoute(
          "/subtasks", "GET", new TaskListGetHandler<>(taskManager, SubTask.class));
      requestRouter.addRoute(
          "/subtasks/{id}", "DELETE", new TaskDeleteHandler<>(taskManager, SubTask.class));

      requestRouter.addRoute(
          "/epics",
          "POST",
          new TaskPostHandler<>(
              taskManager,
              EpicTaskCreationDTO.class,
              EpicTaskUpdateDTO.class,
              taskManager::addTask,
              taskManager::updateTask));
      requestRouter.addRoute("/epics/{id}", "GET", new TaskGetHandler(taskManager, EpicTask.class));
      requestRouter.addRoute(
          "/epics", "GET", new TaskListGetHandler<>(taskManager, EpicTask.class));
      requestRouter.addRoute(
          "/epics/{id}", "DELETE", new TaskDeleteHandler<>(taskManager, EpicTask.class));
      // Note: GET /epics/{id}/subtasks is in requirements but not implemented in
      // HttpTaskServer.java

      requestRouter.addRoute("/history", "GET", new HistoryGetHandler(taskManager));
      requestRouter.addRoute(
          "/prioritized", "GET", new TasksPrioritizedListGetHandler(taskManager));
    } catch (Exception e) {
      throw new RuntimeException("Failed to setup router", e);
    }

    DispatcherHandler dispatcherHandler = new DispatcherHandler(requestRouter);
    server.createContext("/", dispatcherHandler).getFilters().add(new RequestValidationFilter());
    server.start();
  }

  @AfterEach
  void tearDown() {
    server.stop(0);
  }

  private HttpResponse<String> sendGetRequest(String path)
      throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + path)).GET().build();
    return client.send(request, HttpResponse.BodyHandlers.ofString());
  }

  private HttpResponse<String> sendPostRequest(String path, String body)
      throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + path))
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .header("Content-Type", "application/json")
            .build();
    return client.send(request, HttpResponse.BodyHandlers.ofString());
  }

  private HttpResponse<String> sendPostRequestWithCustomContentType(
      String path, String body, String contentType) throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + path))
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .header("Content-Type", contentType)
            .build();
    return client.send(request, HttpResponse.BodyHandlers.ofString());
  }

  private HttpResponse<String> sendDeleteRequest(String path)
      throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder().uri(URI.create(BASE_URL + path)).DELETE().build();
    return client.send(request, HttpResponse.BodyHandlers.ofString());
  }

  private RegularTaskCreationDTO createRegularTaskCreationDTO(
      String suffix, LocalDateTime startTime, Duration duration) {
    return new RegularTaskCreationDTO(
        "Regular Task " + suffix, "Description " + suffix, startTime, duration);
  }

  private SubTaskCreationDTO createSubTaskCreationDTO(
      String suffix, UUID epicId, LocalDateTime startTime, Duration duration) {
    return new SubTaskCreationDTO(
        "SubTask " + suffix, "Description " + suffix, epicId, startTime, duration);
  }

  private EpicTaskCreationDTO createEpicTaskCreationDTO(String suffix) {
    return new EpicTaskCreationDTO("Epic Task " + suffix, "Description " + suffix, null);
  }

  @Test
  @DisplayName("POST /tasks - Create RegularTask successfully")
  void createRegularTask_Success() throws IOException, InterruptedException {
    RegularTaskCreationDTO dto = createRegularTaskCreationDTO("CreateSuccess", null, null);
    HttpResponse<String> response = sendPostRequest("/tasks", gson.toJson(dto));

    assertEquals(201, response.statusCode());
    RegularTask createdTask = gson.fromJson(response.body(), RegularTask.class);
    assertNotNull(createdTask.getId());
    assertEquals(dto.title(), createdTask.getTitle());
  }

  @Test
  @DisplayName("POST /tasks - Create RegularTask with time overlap")
  void createRegularTask_Overlapping()
      throws IOException, InterruptedException, ValidationException, OverlapException {
    LocalDateTime start = LocalDateTime.now();
    Duration duration = Duration.ofHours(1);
    taskManager.addTask(createRegularTaskCreationDTO("Existing", start, duration));

    RegularTaskCreationDTO overlappingDto =
        createRegularTaskCreationDTO("Overlapping", start.plusMinutes(30), duration);
    HttpResponse<String> response = sendPostRequest("/tasks", gson.toJson(overlappingDto));
    assertEquals(406, response.statusCode());
  }

  @Test
  @DisplayName("POST /tasks - Update RegularTask successfully")
  void updateRegularTask_Success()
      throws IOException, InterruptedException, ValidationException, OverlapException {
    RegularTask existingTask =
        taskManager.addTask(createRegularTaskCreationDTO("UpdateSuccessInitial", null, null));
    RegularTaskUpdateDTO updateDto =
        new RegularTaskUpdateDTO(
            existingTask.getId(),
            "Updated Title",
            "Updated Desc",
            TaskStatus.IN_PROGRESS,
            null,
            null);

    HttpResponse<String> postResponse = sendPostRequest("/tasks", gson.toJson(updateDto));
    assertEquals(201, postResponse.statusCode());

    RegularTask taskFromResponse = gson.fromJson(postResponse.body(), RegularTask.class);
    assertEquals(existingTask.getId(), taskFromResponse.getId());
    assertEquals(
        existingTask.getTitle(),
        taskFromResponse.getTitle(),
        "Response body should contain the task state BEFORE update");
    assertEquals(
        existingTask.getStatus(),
        taskFromResponse.getStatus(),
        "Response body should contain the task state BEFORE update");

    Optional<Task> taskAfterUpdateOptional = taskManager.getTask(existingTask.getId());
    assertTrue(taskAfterUpdateOptional.isPresent(), "Task should exist in manager after update");
    RegularTask taskAfterUpdate = (RegularTask) taskAfterUpdateOptional.get();

    assertEquals(
        updateDto.title(), taskAfterUpdate.getTitle(), "Task in manager should have updated title");
    assertEquals(
        updateDto.status(),
        taskAfterUpdate.getStatus(),
        "Task in manager should have updated status");
  }

  @Test
  @DisplayName("POST /tasks - Update non-existent RegularTask")
  void updateRegularTask_NotFound() throws IOException, InterruptedException {
    RegularTaskUpdateDTO updateDto =
        new RegularTaskUpdateDTO(
            UUID.randomUUID(), "NonExistent", "Desc", TaskStatus.NEW, null, null);
    HttpResponse<String> response = sendPostRequest("/tasks", gson.toJson(updateDto));
    assertEquals(400, response.statusCode());
  }

  @Test
  @DisplayName("GET /tasks/{id} - Get RegularTask by ID successfully")
  void getRegularTaskById_Success()
      throws IOException, InterruptedException, ValidationException, OverlapException {
    RegularTask task =
        taskManager.addTask(createRegularTaskCreationDTO("GetByIdSuccess", null, null));
    HttpResponse<String> response = sendGetRequest("/tasks/" + task.getId());
    assertEquals(200, response.statusCode());
    RegularTask fetchedTask = gson.fromJson(response.body(), RegularTask.class);
    assertEquals(task.getId(), fetchedTask.getId());
  }

  @Test
  @DisplayName("GET /tasks/{id} - Get non-existent RegularTask by ID")
  void getRegularTaskById_NotFound() throws IOException, InterruptedException {
    HttpResponse<String> response = sendGetRequest("/tasks/" + UUID.randomUUID());
    assertEquals(404, response.statusCode());
  }

  @Test
  @DisplayName("GET /tasks - Get all RegularTasks successfully (empty list)")
  void getAllRegularTasks_Empty() throws IOException, InterruptedException {
    HttpResponse<String> response = sendGetRequest("/tasks");
    assertEquals(200, response.statusCode());
    Type listType = new TypeToken<List<RegularTask>>() {}.getType();
    List<RegularTask> tasks = gson.fromJson(response.body(), listType);
    assertTrue(tasks.isEmpty());
  }

  @Test
  @DisplayName("GET /tasks - Get all RegularTasks successfully (with data)")
  void getAllRegularTasks_WithData()
      throws IOException, InterruptedException, ValidationException, OverlapException {
    taskManager.addTask(createRegularTaskCreationDTO("GetAll1", null, null));
    taskManager.addTask(createRegularTaskCreationDTO("GetAll2", null, null));
    HttpResponse<String> response = sendGetRequest("/tasks");
    assertEquals(200, response.statusCode());
    Type listType = new TypeToken<List<RegularTask>>() {}.getType();
    List<RegularTask> tasks = gson.fromJson(response.body(), listType);
    assertEquals(2, tasks.size());
  }

  @Test
  @DisplayName("DELETE /tasks/{id} - Delete RegularTask successfully")
  void deleteRegularTask_Success()
      throws IOException, InterruptedException, ValidationException, OverlapException {
    RegularTask task =
        taskManager.addTask(createRegularTaskCreationDTO("DeleteSuccess", null, null));
    HttpResponse<String> response = sendDeleteRequest("/tasks/" + task.getId());
    assertEquals(200, response.statusCode());
    RegularTask deletedTask = gson.fromJson(response.body(), RegularTask.class);
    assertEquals(task.getId(), deletedTask.getId());
    assertTrue(taskManager.getTask(task.getId()).isEmpty());
  }

  @Test
  @DisplayName("DELETE /tasks/{id} - Delete non-existent RegularTask")
  void deleteRegularTask_NotFound() throws IOException, InterruptedException {
    HttpResponse<String> response = sendDeleteRequest("/tasks/" + UUID.randomUUID());
    assertEquals(404, response.statusCode());
  }

  @Test
  @DisplayName("POST /subtasks - Create SubTask successfully")
  void createSubTask_Success()
      throws IOException, InterruptedException, ValidationException, OverlapException {
    EpicTask epic = taskManager.addTask(createEpicTaskCreationDTO("EpicForSubtask"));
    SubTaskCreationDTO dto = createSubTaskCreationDTO("CreateSuccess", epic.getId(), null, null);
    HttpResponse<String> response = sendPostRequest("/subtasks", gson.toJson(dto));

    assertEquals(201, response.statusCode());
    SubTask createdTask = gson.fromJson(response.body(), SubTask.class);
    assertNotNull(createdTask.getId());
    assertEquals(dto.title(), createdTask.getTitle());
    assertEquals(epic.getId(), createdTask.getEpicTaskId());
  }

  @Test
  @DisplayName("POST /subtasks - Create SubTask for non-existent Epic")
  void createSubTask_EpicNotFound() throws IOException, InterruptedException {
    SubTaskCreationDTO dto =
        createSubTaskCreationDTO("EpicNotFound", UUID.randomUUID(), null, null);
    HttpResponse<String> response = sendPostRequest("/subtasks", gson.toJson(dto));
    assertEquals(400, response.statusCode());
  }

  @Test
  @DisplayName("POST /epics - Create EpicTask successfully")
  void createEpicTask_Success() throws IOException, InterruptedException {
    EpicTaskCreationDTO dto = createEpicTaskCreationDTO("CreateEpicSuccess");
    HttpResponse<String> response = sendPostRequest("/epics", gson.toJson(dto));
    assertEquals(201, response.statusCode());
    EpicTask createdEpic = gson.fromJson(response.body(), EpicTask.class);
    assertNotNull(createdEpic.getId());
    assertEquals(dto.title(), createdEpic.getTitle());
  }

  @Test
  @DisplayName("POST /epics - Update EpicTask successfully")
  void updateEpicTask_Success()
      throws IOException, InterruptedException, ValidationException, OverlapException {
    EpicTask existingEpic = taskManager.addTask(createEpicTaskCreationDTO("UpdateEpicInitial"));
    EpicTaskUpdateDTO updateDto =
        new EpicTaskUpdateDTO(existingEpic.getId(), "Updated Epic Title", "Updated Epic Desc");

    HttpResponse<String> postResponse = sendPostRequest("/epics", gson.toJson(updateDto));
    assertEquals(201, postResponse.statusCode());
    EpicTask epicFromResponse = gson.fromJson(postResponse.body(), EpicTask.class);
    assertEquals(existingEpic.getId(), epicFromResponse.getId());
    assertEquals(
        existingEpic.getTitle(),
        epicFromResponse.getTitle(),
        "Response body should contain the epic state BEFORE update");

    Optional<Task> epicAfterUpdateOptional = taskManager.getTask(existingEpic.getId());
    assertTrue(epicAfterUpdateOptional.isPresent(), "Epic should exist in manager after update");
    EpicTask epicAfterUpdate = (EpicTask) epicAfterUpdateOptional.get();

    assertEquals(
        updateDto.title(), epicAfterUpdate.getTitle(), "Epic in manager should have updated title");
    assertEquals(
        updateDto.description(),
        epicAfterUpdate.getDescription(),
        "Epic in manager should have updated description");
  }

  @Test
  @DisplayName("GET /history - Get history successfully (empty)")
  void getHistory_Empty() throws IOException, InterruptedException {
    HttpResponse<String> response = sendGetRequest("/history");
    assertEquals(200, response.statusCode());
    Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
    List<Map<String, Object>> history = gson.fromJson(response.body(), listType);
    assertTrue(history.isEmpty());
  }

  @Test
  @DisplayName("GET /history - Get history successfully (with data)")
  void getHistory_WithData()
      throws IOException, InterruptedException, ValidationException, OverlapException {
    RegularTask task1 = taskManager.addTask(createRegularTaskCreationDTO("History1", null, null));
    EpicTask task2 = taskManager.addTask(createEpicTaskCreationDTO("History2"));
    taskManager.getTask(task1.getId());
    taskManager.getTask(task2.getId());

    HttpResponse<String> response = sendGetRequest("/history");
    assertEquals(200, response.statusCode());
    Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
    List<Map<String, Object>> historyMaps = gson.fromJson(response.body(), listType);
    assertEquals(2, historyMaps.size());
    assertTrue(
        historyMaps.stream().anyMatch(map -> map.get("id").equals(task1.getId().toString())));
    assertTrue(
        historyMaps.stream().anyMatch(map -> map.get("id").equals(task2.getId().toString())));
  }

  @Test
  @DisplayName("GET /prioritized - Get prioritized tasks successfully (empty list)")
  void getPrioritized_Empty() throws IOException, InterruptedException {
    HttpResponse<String> response = sendGetRequest("/prioritized");
    assertEquals(200, response.statusCode());
    Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
    List<Map<String, Object>> prioritized = gson.fromJson(response.body(), listType);
    assertTrue(prioritized.isEmpty());
  }

  @Test
  @DisplayName("GET /prioritized - Get prioritized tasks successfully (with data)")
  void getPrioritized_WithData()
      throws IOException, InterruptedException, ValidationException, OverlapException {
    LocalDateTime now = LocalDateTime.now();
    RegularTask taskPrio1 =
        taskManager.addTask(createRegularTaskCreationDTO("Prio1", now, Duration.ofHours(1)));
    RegularTask taskPrio2 =
        taskManager.addTask(
            createRegularTaskCreationDTO("Prio2", now.plusHours(2), Duration.ofHours(1)));

    HttpResponse<String> response = sendGetRequest("/prioritized");
    assertEquals(200, response.statusCode());
    Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
    List<Map<String, Object>> prioritizedMaps = gson.fromJson(response.body(), listType);
    assertEquals(2, prioritizedMaps.size());

    assertTrue(
        prioritizedMaps.stream()
            .anyMatch(map -> map.get("id").equals(taskPrio1.getId().toString())));
    assertTrue(
        prioritizedMaps.stream()
            .anyMatch(map -> map.get("id").equals(taskPrio2.getId().toString())));

    if (prioritizedMaps.size() == 2) {
      Map<String, Object> taskMap1 = prioritizedMaps.get(0);
      Map<String, Object> taskMap2 = prioritizedMaps.get(1);

      assertNotNull(taskMap1.get("startTime"), "Task 1 startTime should not be null");
      assertNotNull(taskMap2.get("startTime"), "Task 2 startTime should not be null");

      LocalDateTime startTime1 = LocalDateTime.parse((String) taskMap1.get("startTime"));
      LocalDateTime startTime2 = LocalDateTime.parse((String) taskMap2.get("startTime"));

      assertTrue(
          startTime1.isBefore(startTime2) || startTime1.isEqual(startTime2),
          "Tasks should be ordered by startTime. Task1: " + startTime1 + ", Task2: " + startTime2);
    }
  }

  @Test
  @DisplayName("POST /tasks - Invalid Content-Type")
  void postTask_UnsupportedContentType() throws IOException, InterruptedException {
    RegularTaskCreationDTO dto = createRegularTaskCreationDTO("ContentTypeTest", null, null);
    HttpResponse<String> response =
        sendPostRequestWithCustomContentType("/tasks", gson.toJson(dto), "text/plain");
    assertEquals(415, response.statusCode());
  }

  @Test
  @DisplayName("POST /tasks - Empty request body")
  void postTask_EmptyBody() throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/tasks"))
            .POST(HttpRequest.BodyPublishers.ofString(""))
            .header("Content-Type", "application/json")
            .build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(400, response.statusCode());
  }

  @Test
  @DisplayName("POST /tasks - Invalid JSON")
  void postTask_InvalidJson() throws IOException, InterruptedException {
    HttpResponse<String> response = sendPostRequest("/tasks", "{not a json}");
    assertEquals(400, response.statusCode());
  }

  @Test
  @DisplayName("POST /tasks - Missing required DTO fields")
  void postTask_MissingRequiredFields() throws IOException, InterruptedException {
    String jsonWithMissingTitle = "{\"description\":\"Valid description for missing fields test\"}";
    HttpResponse<String> response = sendPostRequest("/tasks", jsonWithMissingTitle);
    assertEquals(400, response.statusCode());
    assertTrue(response.body().contains("Отсутствуют обязательные поля для DTO"));
  }

  @Test
  @DisplayName("GET /unknown_endpoint - Request to non-existent endpoint")
  void getUnknownEndpoint_NotFound() throws IOException, InterruptedException {
    HttpResponse<String> response = sendGetRequest("/unknown/path");
    assertEquals(404, response.statusCode());
  }

  @Test
  @DisplayName("PUT /tasks - Method not allowed")
  void putTasks_MethodNotAllowed() throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/tasks"))
            .PUT(
                HttpRequest.BodyPublishers.ofString(
                    gson.toJson(createRegularTaskCreationDTO("PutTest", null, null))))
            .header("Content-Type", "application/json")
            .build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(405, response.statusCode());
  }
}
