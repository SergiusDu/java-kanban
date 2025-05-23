package com.tasktracker.server;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.tasktracker.json.GsonProvider;
import com.tasktracker.server.dto.ApiErrorMessage;
import com.tasktracker.server.exceptions.DuplicateParameterException;
import com.tasktracker.server.router.RequestRouter;
import com.tasktracker.server.router.Router;
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
import com.tasktracker.task.store.InMemoryHistoryStore;
import com.tasktracker.task.store.InMemoryTaskRepository;
import com.tasktracker.task.store.exception.TaskNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HttpTaskServerTest {

  private static final int SERVER_PORT = 8080;
  private static final String BASE_URL = "http://localhost:" + SERVER_PORT;
  private static final String TASKS_ENDPOINT = "/tasks";
  private static final String SUBTASKS_ENDPOINT = "/subtasks";
  private static final String EPICS_ENDPOINT = "/epics";
  private static final String HISTORY_ENDPOINT = "/history";
  private static final String PRIORITIZED_ENDPOINT = "/prioritized";

  private HttpTaskServer httpTaskServer;
  private TaskManager taskManager;
  private HttpClient httpClient;
  private Gson gson;

  @BeforeEach
  void setUp() throws IOException, DuplicateParameterException {
    taskManager =
        new TaskManagerImpl(
            new InMemoryTaskRepository(), new InMemoryHistoryManager(new InMemoryHistoryStore()));
    Router router = new RequestRouter();
    httpTaskServer = new HttpTaskServer(taskManager, router);
    httpTaskServer.start();

    httpClient = HttpClient.newHttpClient();
    gson = GsonProvider.getGson();
  }

  @AfterEach
  void tearDown() {
    if (httpTaskServer != null) {
      httpTaskServer.stop();
    }
    if (taskManager != null) {
      taskManager.clearAllTasks();
    }
  }

  private HttpResponse<String> sendGetRequest(String path)
      throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + path)).GET().build();
    return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
  }

  private <T> HttpResponse<String> sendPostRequest(String path, T bodyPayload)
      throws IOException, InterruptedException {
    String jsonBody = gson.toJson(bodyPayload);
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + path))
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .header("Content-Type", "application/json")
            .build();
    return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
  }

  private HttpResponse<String> sendDeleteRequest(String path)
      throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder().uri(URI.create(BASE_URL + path)).DELETE().build();
    return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
  }

  private <T> T parseResponse(HttpResponse<String> response, Class<T> clazz) {
    try {
      return gson.fromJson(response.body(), clazz);
    } catch (JsonSyntaxException e) {
      fail("Failed to parse JSON response: " + response.body() + ", Error: " + e.getMessage());
      return null;
    }
  }

  private <T> List<T> parseResponseList(HttpResponse<String> response, Type listType) {
    try {
      return gson.fromJson(response.body(), listType);
    } catch (JsonSyntaxException e) {
      fail("Failed to parse JSON list response: " + response.body() + ", Error: " + e.getMessage());
      return Collections.emptyList();
    }
  }

  private RegularTaskCreationDTO createRegularTaskCreationDTO(
      String suffix, LocalDateTime startTime, Duration duration) {
    return new RegularTaskCreationDTO(
        "Test RegularTask " + suffix,
        "Description for RegularTask " + suffix + " (long enough)",
        startTime,
        duration);
  }

  private EpicTaskCreationDTO createEpicTaskCreationDTO(String suffix) {
    return new EpicTaskCreationDTO(
        "Test EpicTask " + suffix, "Description for EpicTask " + suffix + " (long enough)", null);
  }

  private SubTaskCreationDTO createSubTaskCreationDTO(
      String suffix, UUID epicId, LocalDateTime startTime, Duration duration) {
    return new SubTaskCreationDTO(
        "Test SubTask " + suffix,
        "Description for SubTask " + suffix + " (long enough)",
        epicId,
        startTime,
        duration);
  }

  @Test
  @DisplayName("Create RegularTask successfully")
  void createRegularTask_shouldReturn201AndTask() throws IOException, InterruptedException {
    RegularTaskCreationDTO dto = createRegularTaskCreationDTO("Create", null, null);
    HttpResponse<String> response = sendPostRequest(TASKS_ENDPOINT, dto);

    assertEquals(201, response.statusCode());
    RegularTask createdTask = parseResponse(response, RegularTask.class);
    assertNotNull(createdTask);
    assertEquals(dto.title(), createdTask.getTitle());
    assertEquals(dto.description(), createdTask.getDescription());
    assertEquals(TaskStatus.NEW, createdTask.getStatus());
  }

  @Test
  @DisplayName("Create RegularTask with short title should return 400")
  void createRegularTask_withShortTitle_shouldReturn400() throws IOException, InterruptedException {
    RegularTaskCreationDTO dto =
        new RegularTaskCreationDTO("Short", "Valid description (long enough)", null, null);
    HttpResponse<String> response = sendPostRequest(TASKS_ENDPOINT, dto);

    assertEquals(400, response.statusCode());
    ApiErrorMessage error = parseResponse(response, ApiErrorMessage.class);
    assertNotNull(error);
    assertTrue(error.error().contains("Title length should be at least"));
  }

  @Test
  @DisplayName("Get existing RegularTask should return 200 and task")
  void getRegularTask_existing_shouldReturn200AndTask()
      throws IOException, InterruptedException, ValidationException, OverlapException {
    RegularTask task = taskManager.addTask(createRegularTaskCreationDTO("GetExisting", null, null));

    HttpResponse<String> response = sendGetRequest(TASKS_ENDPOINT + "/" + task.getId());
    assertEquals(200, response.statusCode());
    RegularTask fetchedTask = parseResponse(response, RegularTask.class);
    assertNotNull(fetchedTask);
    assertEquals(task.getId(), fetchedTask.getId());
    assertEquals(task.getTitle(), fetchedTask.getTitle());
  }

  @Test
  @DisplayName("Get non-existing RegularTask should return 404")
  void getRegularTask_nonExisting_shouldReturn404() throws IOException, InterruptedException {
    HttpResponse<String> response = sendGetRequest(TASKS_ENDPOINT + "/" + UUID.randomUUID());
    assertEquals(404, response.statusCode());
  }

  @Test
  @DisplayName("Get all RegularTasks should return 200 and list")
  void getAllRegularTasks_shouldReturn200AndListOfTasks()
      throws IOException, InterruptedException, ValidationException, OverlapException {
    taskManager.addTask(createRegularTaskCreationDTO("GetAll1", null, null));
    taskManager.addTask(createRegularTaskCreationDTO("GetAll2", null, null));

    HttpResponse<String> response = sendGetRequest(TASKS_ENDPOINT);
    assertEquals(200, response.statusCode());
    List<RegularTask> tasks =
        parseResponseList(response, new TypeToken<List<RegularTask>>() {}.getType());
    assertNotNull(tasks);
    assertEquals(2, tasks.size());
  }

  @Test
  @DisplayName("Update existing RegularTask should return 201, then GET should return updated task")
  void updateRegularTask_shouldReturn201AndThenUpdatedTask()
      throws IOException,
          InterruptedException,
          ValidationException,
          OverlapException,
          TaskNotFoundException {
    RegularTask existingTask =
        taskManager.addTask(createRegularTaskCreationDTO("Update", null, null));
    RegularTaskUpdateDTO updateDTO =
        new RegularTaskUpdateDTO(
            existingTask.getId(),
            "Updated Title for RegularTask",
            "Updated Description for RegularTask (long enough)",
            TaskStatus.IN_PROGRESS,
            LocalDateTime.now().plusDays(1).withNano(0), // Remove nanos for comparison
            Duration.ofHours(2));

    HttpResponse<String> postResponse = sendPostRequest(TASKS_ENDPOINT, updateDTO);
    assertEquals(201, postResponse.statusCode());
    RegularTask taskFromPostResponse = parseResponse(postResponse, RegularTask.class);
    assertNotNull(taskFromPostResponse);
    assertEquals(existingTask.getId(), taskFromPostResponse.getId());
    assertEquals(existingTask.getTitle(), taskFromPostResponse.getTitle()); // Should be OLD title

    HttpResponse<String> getResponse = sendGetRequest(TASKS_ENDPOINT + "/" + existingTask.getId());
    assertEquals(200, getResponse.statusCode());
    RegularTask updatedTaskFromGet = parseResponse(getResponse, RegularTask.class);
    assertNotNull(updatedTaskFromGet);
    assertEquals(updateDTO.id(), updatedTaskFromGet.getId());
    assertEquals(updateDTO.title(), updatedTaskFromGet.getTitle());
    assertEquals(updateDTO.description(), updatedTaskFromGet.getDescription());
    assertEquals(updateDTO.status(), updatedTaskFromGet.getStatus());
    // Compare LocalDateTime without nanoseconds if they might differ due to precision
    if (updateDTO.startTime() != null) {
      assertNotNull(updatedTaskFromGet.getStartTime());
      assertEquals(
          updateDTO.startTime().withNano(0), updatedTaskFromGet.getStartTime().withNano(0));
    } else {
      assertNull(updatedTaskFromGet.getStartTime());
    }
    assertEquals(updateDTO.duration(), updatedTaskFromGet.getDuration());
  }

  @Test
  @DisplayName("Delete existing RegularTask should return 200")
  void deleteRegularTask_existing_shouldReturn200()
      throws IOException, InterruptedException, ValidationException, OverlapException {
    RegularTask task = taskManager.addTask(createRegularTaskCreationDTO("Delete", null, null));

    HttpResponse<String> response = sendDeleteRequest(TASKS_ENDPOINT + "/" + task.getId());
    assertEquals(200, response.statusCode());
    RegularTask deletedTask = parseResponse(response, RegularTask.class);
    assertNotNull(deletedTask);
    assertEquals(task.getId(), deletedTask.getId());

    HttpResponse<String> getResponse = sendGetRequest(TASKS_ENDPOINT + "/" + task.getId());
    assertEquals(404, getResponse.statusCode());
  }

  @Test
  @DisplayName("Create EpicTask successfully")
  void createEpicTask_shouldReturn201AndTask() throws IOException, InterruptedException {
    EpicTaskCreationDTO dto = createEpicTaskCreationDTO("CreateEpic");
    HttpResponse<String> response = sendPostRequest(EPICS_ENDPOINT, dto);

    assertEquals(201, response.statusCode());
    EpicTask createdEpic = parseResponse(response, EpicTask.class);
    assertNotNull(createdEpic);
    assertEquals(dto.title(), createdEpic.getTitle());
    assertEquals(TaskStatus.NEW, createdEpic.getStatus());
    assertTrue(createdEpic.getSubtaskIds().isEmpty());
  }

  @Test
  @DisplayName("Get existing EpicTask should return 200 and task")
  void getEpicTask_existing_shouldReturn200AndTask()
      throws IOException, InterruptedException, ValidationException {
    EpicTask epic = taskManager.addTask(createEpicTaskCreationDTO("GetExistingEpic"));

    HttpResponse<String> response = sendGetRequest(EPICS_ENDPOINT + "/" + epic.getId());
    assertEquals(200, response.statusCode());
    EpicTask fetchedEpic = parseResponse(response, EpicTask.class);
    assertNotNull(fetchedEpic);
    assertEquals(epic.getId(), fetchedEpic.getId());
  }

  @Test
  @DisplayName("Update existing EpicTask should return 201, then GET should return updated task")
  void updateEpicTask_shouldReturn201AndThenUpdatedTask()
      throws IOException, InterruptedException, ValidationException, TaskNotFoundException {
    EpicTask existingEpic = taskManager.addTask(createEpicTaskCreationDTO("UpdateEpic"));
    EpicTaskUpdateDTO updateDTO =
        new EpicTaskUpdateDTO(
            existingEpic.getId(),
            "Updated Title for EpicTask",
            "Updated Description for EpicTask (long enough)");

    HttpResponse<String> postResponse = sendPostRequest(EPICS_ENDPOINT, updateDTO);
    assertEquals(201, postResponse.statusCode());
    EpicTask epicFromPostResponse = parseResponse(postResponse, EpicTask.class);
    assertNotNull(epicFromPostResponse);
    assertEquals(existingEpic.getId(), epicFromPostResponse.getId());
    assertEquals(existingEpic.getTitle(), epicFromPostResponse.getTitle()); // Should be OLD title

    HttpResponse<String> getResponse = sendGetRequest(EPICS_ENDPOINT + "/" + existingEpic.getId());
    assertEquals(200, getResponse.statusCode());
    EpicTask updatedEpicFromGet = parseResponse(getResponse, EpicTask.class);
    assertNotNull(updatedEpicFromGet);
    assertEquals(updateDTO.id(), updatedEpicFromGet.getId());
    assertEquals(updateDTO.title(), updatedEpicFromGet.getTitle());
    assertEquals(updateDTO.description(), updatedEpicFromGet.getDescription());
    assertEquals(existingEpic.getStatus(), updatedEpicFromGet.getStatus());
  }

  @Test
  @DisplayName("Delete existing EpicTask should return 200 and delete its SubTasks")
  void deleteEpicTask_existing_shouldReturn200AndCascadeDelete()
      throws IOException,
          InterruptedException,
          ValidationException,
          TaskNotFoundException,
          OverlapException {
    EpicTask epic = taskManager.addTask(createEpicTaskCreationDTO("DeleteEpicCascade"));
    SubTask subtask =
        taskManager.addTask(createSubTaskCreationDTO("SubForDeleteEpic", epic.getId(), null, null));

    assertNotNull(
        taskManager.getTask(subtask.getId()).orElse(null),
        "Subtask should exist before epic deletion");

    HttpResponse<String> response = sendDeleteRequest(EPICS_ENDPOINT + "/" + epic.getId());
    assertEquals(200, response.statusCode());

    HttpResponse<String> getEpicResponse = sendGetRequest(EPICS_ENDPOINT + "/" + epic.getId());
    assertEquals(404, getEpicResponse.statusCode());

    HttpResponse<String> getSubTaskResponse =
        sendGetRequest(SUBTASKS_ENDPOINT + "/" + subtask.getId());
    assertEquals(404, getSubTaskResponse.statusCode());
  }

  @Test
  @DisplayName("Create SubTask successfully")
  void createSubTask_shouldReturn201AndTask()
      throws IOException, InterruptedException, ValidationException {
    EpicTask epic = taskManager.addTask(createEpicTaskCreationDTO("EpicForSub"));
    SubTaskCreationDTO dto = createSubTaskCreationDTO("CreateSub", epic.getId(), null, null);

    HttpResponse<String> response = sendPostRequest(SUBTASKS_ENDPOINT, dto);
    assertEquals(201, response.statusCode());
    SubTask createdSubTask = parseResponse(response, SubTask.class);
    assertNotNull(createdSubTask);
    assertEquals(dto.title(), createdSubTask.getTitle());
    assertEquals(dto.epicId(), createdSubTask.getEpicTaskId());
    assertEquals(TaskStatus.NEW, createdSubTask.getStatus());

    EpicTask updatedEpic = taskManager.getTask(epic.getId()).map(EpicTask.class::cast).orElse(null);
    assertNotNull(updatedEpic);
    assertTrue(updatedEpic.getSubtaskIds().contains(createdSubTask.getId()));
  }

  @Test
  @DisplayName("Create SubTask for non-existing Epic should return 400")
  void createSubTask_forNonExistingEpic_shouldReturn400() throws IOException, InterruptedException {
    SubTaskCreationDTO dto = createSubTaskCreationDTO("SubNonEpic", UUID.randomUUID(), null, null);
    HttpResponse<String> response = sendPostRequest(SUBTASKS_ENDPOINT, dto);
    assertEquals(400, response.statusCode());
    ApiErrorMessage error = parseResponse(response, ApiErrorMessage.class);
    assertNotNull(error);
    assertTrue(
        error.error().contains("Task with ID") && error.error().contains("is not an Epic Task")
            || error.error().contains("exists but is not a EpicTask")
            || error.error().contains("does not exist"));
  }

  @Test
  @DisplayName("Update SubTask status should update Epic status")
  void updateSubTask_statusChanges_shouldUpdateEpicStatus()
      throws IOException,
          InterruptedException,
          ValidationException,
          TaskNotFoundException,
          OverlapException {
    EpicTask epic = taskManager.addTask(createEpicTaskCreationDTO("EpicForSubStatusUpdate"));
    SubTask sub1 =
        taskManager.addTask(
            createSubTaskCreationDTO("Sub1ForEpicStatus", epic.getId(), null, null));
    SubTask sub2 =
        taskManager.addTask(
            createSubTaskCreationDTO("Sub2ForEpicStatus", epic.getId(), null, null));

    EpicTask currentEpic =
        taskManager.getTask(epic.getId()).map(EpicTask.class::cast).orElseThrow();
    assertEquals(TaskStatus.NEW, currentEpic.getStatus());

    SubTaskUpdateDTO sub1Update =
        new SubTaskUpdateDTO(
            sub1.getId(),
            sub1.getTitle(),
            sub1.getDescription(),
            TaskStatus.DONE,
            epic.getId(),
            null,
            null);
    HttpResponse<String> responseSub1Update = sendPostRequest(SUBTASKS_ENDPOINT, sub1Update);
    assertEquals(201, responseSub1Update.statusCode());

    currentEpic = taskManager.getTask(epic.getId()).map(EpicTask.class::cast).orElseThrow();
    assertEquals(
        TaskStatus.IN_PROGRESS,
        currentEpic.getStatus(),
        "Epic should be IN_PROGRESS after one subtask is DONE");

    SubTaskUpdateDTO sub2Update =
        new SubTaskUpdateDTO(
            sub2.getId(),
            sub2.getTitle(),
            sub2.getDescription(),
            TaskStatus.DONE,
            epic.getId(),
            null,
            null);
    HttpResponse<String> responseSub2Update = sendPostRequest(SUBTASKS_ENDPOINT, sub2Update);
    assertEquals(201, responseSub2Update.statusCode());

    currentEpic = taskManager.getTask(epic.getId()).map(EpicTask.class::cast).orElseThrow();
    assertEquals(
        TaskStatus.DONE,
        currentEpic.getStatus(),
        "Epic should be DONE after all subtasks are DONE");
  }

  @Test
  @DisplayName("Get history after accessing tasks")
  void getHistory_afterAccessingTasks_shouldReturnHistory()
      throws IOException,
          InterruptedException,
          ValidationException,
          OverlapException,
          TaskNotFoundException {
    RegularTask task1 = taskManager.addTask(createRegularTaskCreationDTO("Hist1", null, null));
    EpicTask epic1 = taskManager.addTask(createEpicTaskCreationDTO("HistEpic1"));
    SubTask sub1 =
        taskManager.addTask(createSubTaskCreationDTO("HistSub1", epic1.getId(), null, null));

    sendGetRequest(TASKS_ENDPOINT + "/" + task1.getId());
    sendGetRequest(EPICS_ENDPOINT + "/" + epic1.getId());
    sendGetRequest(SUBTASKS_ENDPOINT + "/" + sub1.getId());
    sendGetRequest(TASKS_ENDPOINT + "/" + task1.getId());

    HttpResponse<String> historyResponse = sendGetRequest(HISTORY_ENDPOINT);
    assertEquals(200, historyResponse.statusCode());

    Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
    List<Map<String, Object>> history = parseResponseList(historyResponse, listType);

    assertNotNull(history);
    assertEquals(3, history.size(), "History should contain 3 unique tasks");
    assertEquals(
        task1.getId().toString(), history.get(2).get("id"), "Task1 should be last in history");
    assertEquals(
        epic1.getId().toString(),
        history.get(0).get("id"),
        "Epic1 should be first accessed among these three");
    assertEquals(
        sub1.getId().toString(), history.get(1).get("id"), "Sub1 should be second accessed");
  }

  @Test
  @DisplayName("Get prioritized tasks")
  void getPrioritizedTasks_shouldReturnOrderedList()
      throws IOException, InterruptedException, ValidationException, OverlapException {
    LocalDateTime now = LocalDateTime.now();
    RegularTask taskC =
        taskManager.addTask(
            createRegularTaskCreationDTO("PrioC", now.plusHours(2), Duration.ofHours(1)));
    RegularTask taskA =
        taskManager.addTask(createRegularTaskCreationDTO("PrioA", now, Duration.ofHours(1)));
    RegularTask taskB =
        taskManager.addTask(
            createRegularTaskCreationDTO("PrioB", now.plusHours(1), Duration.ofHours(1)));
    taskManager.addTask(createEpicTaskCreationDTO("PrioEpic"));

    HttpResponse<String> response = sendGetRequest(PRIORITIZED_ENDPOINT);
    assertEquals(200, response.statusCode());

    Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
    List<Map<String, Object>> prioritized = parseResponseList(response, listType);

    assertNotNull(prioritized);
    assertEquals(3, prioritized.size(), "Should have 3 tasks with start times");
    assertEquals(taskA.getId().toString(), prioritized.get(0).get("id"));
    assertEquals(taskB.getId().toString(), prioritized.get(1).get("id"));
    assertEquals(taskC.getId().toString(), prioritized.get(2).get("id"));
  }

  @Test
  @DisplayName("POST request without Content-Type should return 415")
  void post_withoutContentType_shouldReturn415() throws IOException, InterruptedException {
    RegularTaskCreationDTO dto = createRegularTaskCreationDTO("FilterTest", null, null);
    String jsonBody = gson.toJson(dto);
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + TASKS_ENDPOINT))
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(415, response.statusCode());
    ApiErrorMessage error = parseResponse(response, ApiErrorMessage.class);
    assertTrue(error.error().contains("Content-Type must be application/json"));
  }

  @Test
  @DisplayName("POST request with incorrect Content-Type should return 415")
  void post_withIncorrectContentType_shouldReturn415() throws IOException, InterruptedException {
    RegularTaskCreationDTO dto = createRegularTaskCreationDTO("FilterTest", null, null);
    String jsonBody = gson.toJson(dto);
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + TASKS_ENDPOINT))
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .header("Content-Type", "text/plain")
            .build();
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(415, response.statusCode());
    ApiErrorMessage error = parseResponse(response, ApiErrorMessage.class);
    assertTrue(error.error().contains("Content-Type must be application/json"));
  }

  @Test
  @DisplayName("POST request with empty body (Content-Length 0) should return 400")
  void post_withEmptyBody_shouldReturn400() throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + TASKS_ENDPOINT))
            .POST(HttpRequest.BodyPublishers.noBody())
            .header("Content-Type", "application/json")
            .build();
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(400, response.statusCode());
    ApiErrorMessage error = parseResponse(response, ApiErrorMessage.class);
    assertTrue(error.error().contains("Request body cannot be empty"));
  }

  @Test
  @DisplayName("Create overlapping RegularTask should return 406")
  void createRegularTask_withOverlap_shouldReturn406()
      throws IOException, InterruptedException, ValidationException, OverlapException {
    LocalDateTime startTime = LocalDateTime.now();
    Duration duration = Duration.ofHours(2);
    taskManager.addTask(createRegularTaskCreationDTO("OverlapBase", startTime, duration));

    RegularTaskCreationDTO overlappingDto =
        createRegularTaskCreationDTO("OverlapNew", startTime.plusHours(1), duration);
    HttpResponse<String> response = sendPostRequest(TASKS_ENDPOINT, overlappingDto);

    assertEquals(406, response.statusCode());
    ApiErrorMessage error = parseResponse(response, ApiErrorMessage.class);
    assertNotNull(error);
    assertTrue(error.error().toLowerCase().contains("overlap"));
  }

  @Test
  @DisplayName("PUT request to /tasks (undefined method) should return 405")
  void putTasks_undefinedMethod_shouldReturn405() throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + TASKS_ENDPOINT))
            .PUT(HttpRequest.BodyPublishers.ofString("{}"))
            .header("Content-Type", "application/json")
            .build();
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(405, response.statusCode());
    ApiErrorMessage error = parseResponse(response, ApiErrorMessage.class);
    assertNotNull(error);
    assertTrue(error.error().contains("Method PUT not allowed"));
  }

  @Test
  @DisplayName("GET request to non-existent sub-path should return 404")
  void getNonExistentSubPath_shouldReturn404() throws IOException, InterruptedException {
    HttpResponse<String> response = sendGetRequest(TASKS_ENDPOINT + "/someid/extrapath");
    assertEquals(404, response.statusCode());
    ApiErrorMessage error = parseResponse(response, ApiErrorMessage.class);
    assertNotNull(error);
    assertTrue(error.error().contains("Resource not found"));
  }
}
