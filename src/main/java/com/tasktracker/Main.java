package com.tasktracker;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.tasktracker.json.GsonProvider;
import com.tasktracker.server.HttpTaskServer;
import com.tasktracker.server.router.RequestRouter;
import com.tasktracker.server.router.Router;
import com.tasktracker.task.dto.*;
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
// Assuming Managers.getDefault() might be an option for TaskManager
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
import java.util.stream.Collectors;

public class Main {

  private static final String TEST_SECTION_START_FORMAT = "===== %s =====";
  private static final String TEST_SECTION_END = "====================================\n";
  private static final int SERVER_PORT = 8080; // Ensure this matches HttpTaskServer's port
  private static final String BASE_URL = "http://localhost:" + SERVER_PORT;
  private static final String TASKS_ENDPOINT = "/tasks";
  private static final String SUBTASKS_ENDPOINT = "/subtasks";
  private static final String EPICS_ENDPOINT = "/epics";
  private static final String HISTORY_ENDPOINT = "/history";
  private static final String PRIORITIZED_ENDPOINT = "/prioritized";

  private static final String DEFAULT_TASK_TITLE_PREFIX = "Test Task ";
  private static final String DEFAULT_TASK_DESC_PREFIX = "Test Description long enough for ";
  private static final String UPDATED_TITLE_SUFFIX = " UPDATED";

  private static HttpClient httpClient;
  private static Gson gson;
  private static TaskManager taskManagerForServer; // TaskManager instance for the server
  private static HttpTaskServer server;

  private static void printTestSectionHeader(String testName) {
    System.out.printf((TEST_SECTION_START_FORMAT) + "%n", testName);
  }

  private static HttpResponse<String> sendGetRequest(String path) throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + path)).GET().build();
    return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
  }

  private static <T> HttpResponse<String> sendPostRequest(String path, T bodyPayload) throws IOException, InterruptedException {
    String jsonBody = gson.toJson(bodyPayload);
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + path))
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .header("Content-Type", "application/json")
            .build();
    return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
  }

  private static HttpResponse<String> sendDeleteRequest(String path) throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder().uri(URI.create(BASE_URL + path)).DELETE().build();
    return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
  }

  private static RegularTaskCreationDTO createRegularTaskCreationDTO(String suffix, LocalDateTime startTime, Duration duration) {
    return new RegularTaskCreationDTO(
        DEFAULT_TASK_TITLE_PREFIX + "Regular " + suffix,
        DEFAULT_TASK_DESC_PREFIX + "Regular " + suffix,
        startTime,
        duration);
  }

  private static SubTaskCreationDTO createSubTaskCreationDTO(String suffix, UUID epicId, LocalDateTime startTime, Duration duration) {
    return new SubTaskCreationDTO(
        DEFAULT_TASK_TITLE_PREFIX + "SubTask " + suffix,
        DEFAULT_TASK_DESC_PREFIX + "SubTask " + suffix,
        epicId,
        startTime,
        duration);
  }

  private static EpicTaskCreationDTO createEpicTaskCreationDTO(String suffix) {
    return new EpicTaskCreationDTO(
        DEFAULT_TASK_TITLE_PREFIX + "Epic " + suffix,
        DEFAULT_TASK_DESC_PREFIX + "Epic " + suffix,
        null);
  }

  private static EpicTaskCreationDTO createEpicTaskCreationDTO(
      String suffix, LocalDateTime startTime) {
    return new EpicTaskCreationDTO(
        DEFAULT_TASK_TITLE_PREFIX + "Epic " + suffix,
        DEFAULT_TASK_DESC_PREFIX + "Epic " + suffix,
        startTime);
  }


  private static <T> T parseResponse(HttpResponse<String> response, Class<T> clazz, int expectedStatus) {
    if (response.statusCode() != expectedStatus) {
      System.err.println(
          "Request for "
              + clazz.getSimpleName()
              + " failed or returned unexpected status. Expected "
              + expectedStatus
              + ", Got "
              + response.statusCode()
              + ". Body: "
              + response.body());
      return null;
    }
    try {
      return gson.fromJson(response.body(), clazz);
    } catch (JsonSyntaxException e) {
      System.err.println(
          "Error parsing JSON response for "
              + clazz.getSimpleName()
              + " (Status "
              + response.statusCode()
              + "): "
              + e.getMessage()
              + "\nResponse body: "
              + response.body());
      return null;
    }
  }

  private static <T> List<T> parseResponseList(HttpResponse<String> response, Type listType, int expectedStatus) {
    if (response.statusCode() != expectedStatus) {
      System.err.println(
          "Request for list failed or returned unexpected status. Expected "
              + expectedStatus
              + ", Got "
              + response.statusCode()
              + ". Body: "
              + response.body());
      return Collections.emptyList();
    }
    try {
      return gson.fromJson(response.body(), listType);
    } catch (JsonSyntaxException e) {
      System.err.println(
          "Error parsing JSON list response (Status "
              + response.statusCode()
              + "): "
              + e.getMessage()
              + "\nResponse body: "
              + response.body());
      return Collections.emptyList();
    }
  }

  private static <T extends Task> T createTaskViaHttp(String endpoint, Object creationDto, Class<T> taskClass) throws IOException, InterruptedException {
    HttpResponse<String> response = sendPostRequest(endpoint, creationDto);
    System.out.println(
        "Create "
            + taskClass.getSimpleName()
            + " response (Status "
            + response.statusCode()
            + "): "
            + response.body().lines().collect(Collectors.joining()));
    T createdTask = parseResponse(response, taskClass, 201);
    if (createdTask == null) {
      System.err.println("ERROR: Failed to create and parse " + taskClass.getSimpleName() + " from DTO: " + gson.toJson(creationDto));
    }
    return createdTask;
  }

  private static <T extends Task> T getTaskViaHttp(String endpoint, String taskId, Class<T> taskClass) throws IOException, InterruptedException {
    if (taskId == null) {
      System.err.println("ERROR: Attempted to GET task with null ID from endpoint " + endpoint);
      return null;
    }
    HttpResponse<String> response = sendGetRequest(endpoint + "/" + taskId);
    System.out.println(
        "Get "
            + taskClass.getSimpleName()
            + " by ID '"
            + taskId
            + "' response (Status "
            + response.statusCode()
            + "): "
            + response.body().lines().collect(Collectors.joining()));
    return parseResponse(response, taskClass, 200);
  }

  private static void getTaskAndExpectStatus(String endpoint, String taskId, int expectedStatus) throws IOException, InterruptedException {
    if (taskId == null) {
      System.err.println("ERROR: Attempted to GET task with null ID (expecting status " + expectedStatus + ") from endpoint " + endpoint);
      return;
    }
    HttpResponse<String> response = sendGetRequest(endpoint + "/" + taskId);
    System.out.println(
        "Attempt Get Task by ID '"
            + taskId
            + "' response (Status "
            + response.statusCode()
            + ", Expected "
            + expectedStatus
            + "): "
            + response.body().lines().collect(Collectors.joining()));
    if (response.statusCode() != expectedStatus) {
      System.err.println("ERROR: Expected status " + expectedStatus + " but got " + response.statusCode());
    }
  }

  private static <U, T extends Task> void updateTaskViaHttp(String endpoint, String taskId, U updateDto, Class<T> taskClass, String originalTitle) throws IOException, InterruptedException {
    if (taskId == null) {
      System.err.println("ERROR: Attempted to UPDATE task with null ID at endpoint " + endpoint);
      return;
    }
    HttpResponse<String> postResponse = sendPostRequest(endpoint, updateDto);
    System.out.println(
        "Update "
            + taskClass.getSimpleName()
            + " (ID: "
            + taskId
            + ") POST response (Status "
            + postResponse.statusCode()
            + "): "
            + postResponse.body().lines().collect(Collectors.joining()));

    T taskFromResponse = parseResponse(postResponse, taskClass, 201); // Expecting 201 for update
    if (taskFromResponse != null && originalTitle != null && !taskFromResponse.getTitle().equals(originalTitle)) {
      System.err.println("ERROR: Update response body did not contain pre-update title. Expected: '" + originalTitle + "', Got: '" + taskFromResponse.getTitle() + "'");
    }

    T taskAfterUpdate = getTaskViaHttp(endpoint, taskId, taskClass);
    if (taskAfterUpdate != null) {
      System.out.println("State of " + taskClass.getSimpleName() + " (ID: " + taskId + ") after update (from GET): " + gson.toJson(taskAfterUpdate));
    } else {
      System.err.println("ERROR: Could not retrieve " + taskClass.getSimpleName() + " (ID: " + taskId + ") after update attempt.");
    }
  }

  private static void deleteTaskViaHttp(String endpoint, String taskId, String taskType) throws IOException, InterruptedException {
    if (taskId == null) {
      System.err.println("ERROR: Attempted to DELETE task with null ID (" + taskType + ") from endpoint " + endpoint);
      return;
    }
    HttpResponse<String> deleteResponse = sendDeleteRequest(endpoint + "/" + taskId);
    System.out.println(
        "Delete "
            + taskType
            + " (ID: "
            + taskId
            + ") response (Status "
            + deleteResponse.statusCode()
            + "): "
            + deleteResponse.body().lines().collect(Collectors.joining()));
    if (deleteResponse.statusCode() == 200) {
      System.out.println(taskType + " " + taskId + " deleted successfully.");
    } else {
      System.err.println("ERROR: Failed to delete " + taskType + " " + taskId + ". Status: " + deleteResponse.statusCode());
    }
    getTaskAndExpectStatus(endpoint, taskId, 404);
  }

  public static void main(String[] args) {
    httpClient = HttpClient.newHttpClient();
    gson = GsonProvider.getGson();

    try {
      taskManagerForServer =
          new TaskManagerImpl(
              new InMemoryTaskRepository(), new InMemoryHistoryManager(new InMemoryHistoryStore()));
      // Or use Managers.getDefault() if it's configured for in-memory operations for testing
      // taskManagerForServer = Managers.getDefault();

      Router router = new RequestRouter(); // HttpTaskServer will use this to setup its routes
      server = new HttpTaskServer(taskManagerForServer, router);
      server.start();
      System.out.println("HTTP server started on port " + SERVER_PORT);

      runAllTestScenarios();

    } catch (Exception e) {
      System.err.println("An error occurred: " + e.getMessage());
      e.printStackTrace();
    } finally {
      if (server != null) {
        server.stop();
        System.out.println("HTTP server stopped.");
      }
      if (taskManagerForServer != null) {
        // Clear tasks from the manager if needed, though server stop might handle resources
        taskManagerForServer.clearAllTasks();
      }
    }
  }

  private static void runAllTestScenarios() throws IOException, InterruptedException {
    testUserScenario();
    testBasicCrudOperations();
    testAdditionalEpicScenarios();
    testRemovingTasks();
    testUpdateEpicAndSubtaskStatus();
    testBoundaryCases();
    testAdvancedOverlappingScenarios();
    testPrioritizedTasksEndpoint(); // Added new test
  }

  private static void clearAllTasksOnServer() throws IOException, InterruptedException {
    System.out.println("Clearing all tasks on the server...");
    if (taskManagerForServer != null) {
      taskManagerForServer.clearAllTasks(); // Direct clear for test isolation
    }
    System.out.println(
        "Attempting to clear any remaining tasks on server via HTTP DELETE requests...");

    Type regularTaskListType = new TypeToken<List<RegularTask>>() {}.getType();
    Type subTaskListType = new TypeToken<List<SubTask>>() {}.getType();
    Type epicTaskListType = new TypeToken<List<EpicTask>>() {}.getType();

    HttpResponse<String> tasksResponse = sendGetRequest(TASKS_ENDPOINT);
    if (tasksResponse.statusCode() == 200) {
      List<RegularTask> tasks = parseResponseList(tasksResponse, regularTaskListType, 200);
      for (RegularTask task : tasks) {
        if (task != null && task.getId() != null)
          sendDeleteRequest(TASKS_ENDPOINT + "/" + task.getId());
      }
    }

    HttpResponse<String> subtasksResponse = sendGetRequest(SUBTASKS_ENDPOINT);
    if (subtasksResponse.statusCode() == 200) {
      List<SubTask> subtasks = parseResponseList(subtasksResponse, subTaskListType, 200);
      for (SubTask subtask : subtasks) {
        if (subtask != null && subtask.getId() != null)
          sendDeleteRequest(SUBTASKS_ENDPOINT + "/" + subtask.getId());
      }
    }
    HttpResponse<String> epicsResponse = sendGetRequest(EPICS_ENDPOINT);
    if (epicsResponse.statusCode() == 200) {
      List<EpicTask> epics = parseResponseList(epicsResponse, epicTaskListType, 200);
      for (EpicTask epic : epics) {
        if (epic != null && epic.getId() != null)
          sendDeleteRequest(EPICS_ENDPOINT + "/" + epic.getId());
      }
    }
    System.out.println("Finished attempting to clear tasks on server.");
  }


  private static void testUserScenario() throws IOException, InterruptedException {
    printTestSectionHeader("testUserScenario (HTTP)");
    clearAllTasksOnServer();

    RegularTask regularTask1 = createTaskViaHttp(TASKS_ENDPOINT, createRegularTaskCreationDTO("UserScenario1", null, null), RegularTask.class);
    RegularTask regularTask2 = createTaskViaHttp(TASKS_ENDPOINT, createRegularTaskCreationDTO("UserScenario2", null, null), RegularTask.class);
    EpicTask epicWithSubtasks = createTaskViaHttp(EPICS_ENDPOINT, createEpicTaskCreationDTO("UserScenarioEpic1"), EpicTask.class);

    if (regularTask1 == null || regularTask2 == null || epicWithSubtasks == null) {
      System.err.println("Setup failed in testUserScenario");
      return;
    }

    SubTask subTask1 = createTaskViaHttp(SUBTASKS_ENDPOINT, createSubTaskCreationDTO("UserScenarioSub1", epicWithSubtasks.getId(), null, null), SubTask.class);
    SubTask subTask2 = createTaskViaHttp(SUBTASKS_ENDPOINT, createSubTaskCreationDTO("UserScenarioSub2", epicWithSubtasks.getId(), null, null), SubTask.class);
    SubTask subTask3 = createTaskViaHttp(SUBTASKS_ENDPOINT, createSubTaskCreationDTO("UserScenarioSub3", epicWithSubtasks.getId(), null, null), SubTask.class);
    EpicTask epicWithoutSubtasks = createTaskViaHttp(EPICS_ENDPOINT, createEpicTaskCreationDTO("UserScenarioEpic2"), EpicTask.class);

    if (subTask1 == null || subTask2 == null || subTask3 == null || epicWithoutSubtasks == null) {
      System.err.println("Sub-task/Epic creation failed in testUserScenario");
      return;
    }

    System.out.println("Accessing tasks to populate history:");
    getTaskViaHttp(TASKS_ENDPOINT, regularTask1.getId().toString(), RegularTask.class);
    printHistoryViaHttp();
    getTaskViaHttp(EPICS_ENDPOINT, epicWithSubtasks.getId().toString(), EpicTask.class);
    printHistoryViaHttp();
    getTaskViaHttp(SUBTASKS_ENDPOINT, subTask2.getId().toString(), SubTask.class);
    printHistoryViaHttp();
    getTaskViaHttp(TASKS_ENDPOINT, regularTask2.getId().toString(), RegularTask.class);
    printHistoryViaHttp();
    getTaskViaHttp(EPICS_ENDPOINT, epicWithoutSubtasks.getId().toString(), EpicTask.class);
    printHistoryViaHttp();
    getTaskViaHttp(SUBTASKS_ENDPOINT, subTask1.getId().toString(), SubTask.class);
    printHistoryViaHttp();
    getTaskViaHttp(SUBTASKS_ENDPOINT, subTask3.getId().toString(), SubTask.class);
    printHistoryViaHttp();
    getTaskViaHttp(
        EPICS_ENDPOINT, epicWithSubtasks.getId().toString(), EpicTask.class); // Access epic again
    printHistoryViaHttp();

    System.out.println("Final history:");
    printHistoryViaHttp();

    deleteTaskViaHttp(TASKS_ENDPOINT, regularTask1.getId().toString(), "RegularTask");
    System.out.println("History after deleting Regular Task 1:");
    printHistoryViaHttp();

    deleteTaskViaHttp(EPICS_ENDPOINT, epicWithSubtasks.getId().toString(), "EpicTask");
    System.out.println(
        "History after deleting epic with subtasks (subtasks should also be gone from history if manager removes them):");
    printHistoryViaHttp();

    System.out.print(TEST_SECTION_END);
  }

  private static void printHistoryViaHttp() throws IOException, InterruptedException {
    HttpResponse<String> response = sendGetRequest(HISTORY_ENDPOINT);
    System.out.print("History from HTTP: ");
    if (response.statusCode() == 200) {
      Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
      List<Map<String, Object>> history = parseResponseList(response, listType, 200);
      if (history.isEmpty()) {
        System.out.print("empty");
      } else {
        String historyString =
            history.stream()
                .map(taskMap -> taskMap.get("id") + " (" + taskMap.get("title") + ")")
                .collect(Collectors.joining(", "));
        System.out.print(historyString);
      }
    } else {
      System.out.print("Failed to fetch history, status: " + response.statusCode());
    }
    System.out.println();
  }

  private static void printAllTasksFromServer() throws IOException, InterruptedException {
    System.out.println("--- All tasks from server ---");
    Type regularTaskListType = new TypeToken<List<RegularTask>>() {}.getType();
    Type subTaskListType = new TypeToken<List<SubTask>>() {}.getType();
    Type epicTaskListType = new TypeToken<List<EpicTask>>() {}.getType();

    HttpResponse<String> tasksRes = sendGetRequest(TASKS_ENDPOINT);
    List<RegularTask> regularTasks = parseResponseList(tasksRes, regularTaskListType, 200);
    regularTasks.forEach(System.out::println);

    HttpResponse<String> subtasksRes = sendGetRequest(SUBTASKS_ENDPOINT);
    List<SubTask> subTasks = parseResponseList(subtasksRes, subTaskListType, 200);
    subTasks.forEach(System.out::println);

    HttpResponse<String> epicsRes = sendGetRequest(EPICS_ENDPOINT);
    List<EpicTask> epicTasks = parseResponseList(epicsRes, epicTaskListType, 200);
    epicTasks.forEach(System.out::println);
    System.out.println("-----------------------------\n");
  }

  private static void testBasicCrudOperations() throws IOException, InterruptedException {
    printTestSectionHeader("testBasicCrudOperations (HTTP)");
    clearAllTasksOnServer();

    RegularTask regTask1 = createTaskViaHttp(TASKS_ENDPOINT, createRegularTaskCreationDTO("CRUD1", null, null), RegularTask.class);
    RegularTask regTask2 = createTaskViaHttp(TASKS_ENDPOINT, createRegularTaskCreationDTO("CRUD2", null, null), RegularTask.class);
    EpicTask epic1 = createTaskViaHttp(EPICS_ENDPOINT, createEpicTaskCreationDTO("CRUDEpic1"), EpicTask.class);

    if (regTask1 == null || regTask2 == null || epic1 == null) {
      System.err.println("Setup failed in testBasicCrudOperations");
      return;
    }
    SubTask sub1 = createTaskViaHttp(SUBTASKS_ENDPOINT, createSubTaskCreationDTO("CRUDSub1", epic1.getId(), null, null), SubTask.class);
    if (sub1 == null) {
      System.err.println("SubTask creation failed in testBasicCrudOperations");
      return;
    }

    System.out.println("Initial tasks from server:");
    printAllTasksFromServer();

    RegularTaskUpdateDTO regTask1UpdateDto =
        new RegularTaskUpdateDTO(
            regTask1.getId(),
            regTask1.getTitle() + UPDATED_TITLE_SUFFIX,
            regTask1.getDescription() + " " + UPDATED_TITLE_SUFFIX,
            TaskStatus.IN_PROGRESS,
            null,
            null);
    updateTaskViaHttp(TASKS_ENDPOINT, regTask1.getId().toString(), regTask1UpdateDto, RegularTask.class, regTask1.getTitle());

    SubTaskUpdateDTO sub1UpdateDto =
        new SubTaskUpdateDTO(
            sub1.getId(),
            sub1.getTitle() + UPDATED_TITLE_SUFFIX,
            sub1.getDescription() + " " + UPDATED_TITLE_SUFFIX,
            TaskStatus.DONE,
            epic1.getId(),
            null,
            null);
    updateTaskViaHttp(SUBTASKS_ENDPOINT, sub1.getId().toString(), sub1UpdateDto, SubTask.class, sub1.getTitle());

    System.out.println("After updates (tasks from server):");
    printAllTasksFromServer();

    getTaskViaHttp(TASKS_ENDPOINT, regTask2.getId().toString(), RegularTask.class);
    System.out.print(TEST_SECTION_END);
  }

  private static void testAdditionalEpicScenarios() throws IOException, InterruptedException {
    printTestSectionHeader("testAdditionalEpicScenarios (HTTP)");
    clearAllTasksOnServer();

    EpicTask epicA = createTaskViaHttp(EPICS_ENDPOINT, createEpicTaskCreationDTO("EpicAScenarios"), EpicTask.class);
    if (epicA == null) {
      System.err.println("EpicA creation failed in testAdditionalEpicScenarios");
      return;
    }
    SubTask subA1 = createTaskViaHttp(SUBTASKS_ENDPOINT, createSubTaskCreationDTO("SubA1", epicA.getId(), null, null), SubTask.class);
    SubTask subA2 = createTaskViaHttp(SUBTASKS_ENDPOINT, createSubTaskCreationDTO("SubA2", epicA.getId(), null, null), SubTask.class);
    if (subA1 == null || subA2 == null) {
      System.err.println("SubTask creation for EpicA failed in testAdditionalEpicScenarios");
      return;
    }

    System.out.println("All tasks after creation (from server):");
    printAllTasksFromServer();
    EpicTask fetchedEpicA =
        getTaskViaHttp(EPICS_ENDPOINT, epicA.getId().toString(), EpicTask.class);
    System.out.println(
        "Initial Epic A status: "
            + (fetchedEpicA != null ? fetchedEpicA.getStatus() : "NOT FOUND"));

    SubTaskUpdateDTO subA1UpdateDto = new SubTaskUpdateDTO(subA1.getId(), subA1.getTitle() + UPDATED_TITLE_SUFFIX, subA1.getDescription(), TaskStatus.DONE, epicA.getId(), null, null);
    updateTaskViaHttp(SUBTASKS_ENDPOINT, subA1.getId().toString(), subA1UpdateDto, SubTask.class, subA1.getTitle());
    fetchedEpicA = getTaskViaHttp(EPICS_ENDPOINT, epicA.getId().toString(), EpicTask.class);
    System.out.println(
        "Epic A status after SubA1 DONE: "
            + (fetchedEpicA != null ? fetchedEpicA.getStatus() : "NOT FOUND"));

    SubTaskUpdateDTO subA2UpdateDto = new SubTaskUpdateDTO(subA2.getId(), subA2.getTitle() + UPDATED_TITLE_SUFFIX, subA2.getDescription(), TaskStatus.DONE, epicA.getId(), null, null);
    updateTaskViaHttp(SUBTASKS_ENDPOINT, subA2.getId().toString(), subA2UpdateDto, SubTask.class, subA2.getTitle());

    System.out.println("After setting all subtasks to DONE (tasks from server):");
    printAllTasksFromServer();

    System.out.println("Epic A should now have status DONE");
    EpicTask updatedEpicA = getTaskViaHttp(EPICS_ENDPOINT, epicA.getId().toString(), EpicTask.class);
    if (updatedEpicA != null) {
      System.out.println("Actual Epic A status: " + updatedEpicA.getStatus());
      if (updatedEpicA.getStatus() != TaskStatus.DONE) {
        System.err.println("ERROR: Epic A status is " + updatedEpicA.getStatus() + ", expected DONE.");
      }
    }
    System.out.print(TEST_SECTION_END);
  }

  private static void testRemovingTasks() throws IOException, InterruptedException {
    printTestSectionHeader("testRemovingTasks (HTTP)");
    clearAllTasksOnServer();

    RegularTask regTaskA = createTaskViaHttp(TASKS_ENDPOINT, createRegularTaskCreationDTO("RegAForRemoval", null, null), RegularTask.class);
    EpicTask epicA = createTaskViaHttp(EPICS_ENDPOINT, createEpicTaskCreationDTO("EpicAForRemoval"), EpicTask.class);
    if (regTaskA == null || epicA == null) {
      System.err.println("Setup failed in testRemovingTasks");
      return;
    }
    SubTask subA1 = createTaskViaHttp(SUBTASKS_ENDPOINT, createSubTaskCreationDTO("SubA1ForRemoval", epicA.getId(), null, null), SubTask.class);
    if (subA1 == null) {
      System.err.println("SubTask creation for EpicA failed in testRemovingTasks");
      return;
    }

    System.out.println("Tasks before removing anything (from server):");
    printAllTasksFromServer();

    deleteTaskViaHttp(TASKS_ENDPOINT, regTaskA.getId().toString(), "RegularTask");
    System.out.println("After deleting RegularTask A:");
    printAllTasksFromServer();

    deleteTaskViaHttp(EPICS_ENDPOINT, epicA.getId().toString(), "EpicTask");
    System.out.println("After deleting EpicTask A (and its subtasks implicitly):");
    printAllTasksFromServer();

    System.out.println("Subtask subA1 should be removed as its Epic was deleted.");
    getTaskAndExpectStatus(SUBTASKS_ENDPOINT, subA1.getId().toString(), 404);

    System.out.println("Attempting to remove the same regularTask again: " + regTaskA.getId());
    HttpResponse<String> deleteRegAAgainRes = sendDeleteRequest(TASKS_ENDPOINT + "/" + regTaskA.getId());
    System.out.println("Delete RegularTaskA again response status: " + deleteRegAAgainRes.statusCode());
    if (deleteRegAAgainRes.statusCode() != 404) {
      System.err.println("Error: Should have gotten 404 for deleting non-existent task, but got " + deleteRegAAgainRes.statusCode());
    }
    System.out.print(TEST_SECTION_END);
  }

  private static void testUpdateEpicAndSubtaskStatus() throws IOException, InterruptedException {
    printTestSectionHeader("testUpdateEpicAndSubtaskStatus (HTTP)");
    clearAllTasksOnServer();

    EpicTask epicB = createTaskViaHttp(EPICS_ENDPOINT, createEpicTaskCreationDTO("EpicBStatusTest"), EpicTask.class);
    if (epicB == null) {
      System.err.println("EpicB creation failed in testUpdateEpicAndSubtaskStatus");
      return;
    }
    SubTask subB1 = createTaskViaHttp(SUBTASKS_ENDPOINT, createSubTaskCreationDTO("SubB1Status", epicB.getId(), null, null), SubTask.class);
    SubTask subB2 = createTaskViaHttp(SUBTASKS_ENDPOINT, createSubTaskCreationDTO("SubB2Status", epicB.getId(), null, null), SubTask.class);
    if (subB1 == null || subB2 == null) {
      System.err.println("SubTask creation for EpicB failed in testUpdateEpicAndSubtaskStatus");
      return;
    }

    System.out.println("Initial tasks (from server):");
    printAllTasksFromServer();
    EpicTask epicBInit = getTaskViaHttp(EPICS_ENDPOINT, epicB.getId().toString(), EpicTask.class);
    if (epicBInit != null) System.out.println("Initial EpicB status: " + epicBInit.getStatus());

    SubTaskUpdateDTO subB1UpdateDto = new SubTaskUpdateDTO(subB1.getId(), subB1.getTitle(), subB1.getDescription(), TaskStatus.DONE, epicB.getId(), null, null);
    updateTaskViaHttp(SUBTASKS_ENDPOINT, subB1.getId().toString(), subB1UpdateDto, SubTask.class, subB1.getTitle());
    System.out.println("After subB1 -> DONE (tasks from server):");
    printAllTasksFromServer();
    EpicTask epicBAfterSub1Done = getTaskViaHttp(EPICS_ENDPOINT, epicB.getId().toString(), EpicTask.class);
    if (epicBAfterSub1Done != null) {
      System.out.println("EpicB status should be IN_PROGRESS now. Actual: " + epicBAfterSub1Done.getStatus());
      if (epicBAfterSub1Done.getStatus() != TaskStatus.IN_PROGRESS) {
        System.err.println("ERROR: Epic B status is " + epicBAfterSub1Done.getStatus() + ", expected IN_PROGRESS.");
      }
    }

    SubTaskUpdateDTO subB2UpdateDto = new SubTaskUpdateDTO(subB2.getId(), subB2.getTitle(), subB2.getDescription(), TaskStatus.DONE, epicB.getId(), null, null);
    updateTaskViaHttp(SUBTASKS_ENDPOINT, subB2.getId().toString(), subB2UpdateDto, SubTask.class, subB2.getTitle());
    System.out.println("After subB2 -> DONE (tasks from server):");
    printAllTasksFromServer();
    EpicTask epicBAfterSub2Done = getTaskViaHttp(EPICS_ENDPOINT, epicB.getId().toString(), EpicTask.class);
    if (epicBAfterSub2Done != null) {
      System.out.println("EpicB status should be DONE now. Actual: " + epicBAfterSub2Done.getStatus());
      if (epicBAfterSub2Done.getStatus() != TaskStatus.DONE) {
        System.err.println("ERROR: Epic B status is " + epicBAfterSub2Done.getStatus() + ", expected DONE.");
      }
    }
    System.out.print(TEST_SECTION_END);
  }

  private static void testBoundaryCases() throws IOException, InterruptedException {
    printTestSectionHeader("testBoundaryCases (HTTP)");
    clearAllTasksOnServer();

    HttpResponse<String> validRegRes = sendPostRequest(TASKS_ENDPOINT, createRegularTaskCreationDTO("BoundaryValid", null, null));
    System.out.println(
        "Create valid RegularTask response status: "
            + validRegRes.statusCode()
            + ", Body: "
            + validRegRes.body().lines().collect(Collectors.joining()));

    RegularTaskCreationDTO shortTitleDto = new RegularTaskCreationDTO("Short", DEFAULT_TASK_DESC_PREFIX + "short title", null, null);
    HttpResponse<String> shortTitleRes = sendPostRequest(TASKS_ENDPOINT, shortTitleDto);
    System.out.println(
        "Create RegularTask with short title response status: "
            + shortTitleRes.statusCode()
            + " (expected 400), Body: "
            + shortTitleRes.body().lines().collect(Collectors.joining()));

    EpicTask dummyEpic = createTaskViaHttp(EPICS_ENDPOINT, createEpicTaskCreationDTO("DummyForSubtaskBoundary"), EpicTask.class);
    if (dummyEpic == null) {
      System.err.println("Failed to create dummy epic for boundary tests.");
      SubTaskCreationDTO nonExistentEpicSubDtoAlt =
          createSubTaskCreationDTO("SubNonExistentEpicAlt", UUID.randomUUID(), null, null);
      HttpResponse<String> nonExistentEpicSubResAlt =
          sendPostRequest(SUBTASKS_ENDPOINT, nonExistentEpicSubDtoAlt);
      System.out.println(
          "Create SubTask with non-existent epicId (alternative) response status: "
              + nonExistentEpicSubResAlt.statusCode()
              + " (expected 400), Body: "
              + nonExistentEpicSubResAlt.body().lines().collect(Collectors.joining()));
    } else {
      SubTaskCreationDTO shortDescSubDto =
          new SubTaskCreationDTO(
              DEFAULT_TASK_TITLE_PREFIX + "SubShortDesc", "Tiny", dummyEpic.getId(), null, null);
      HttpResponse<String> shortDescSubRes = sendPostRequest(SUBTASKS_ENDPOINT, shortDescSubDto);
      System.out.println(
          "Create SubTask with short description response status: "
              + shortDescSubRes.statusCode()
              + " (expected 400), Body: "
              + shortDescSubRes.body().lines().collect(Collectors.joining()));
    }

    SubTaskCreationDTO nonExistentEpicSubDto = createSubTaskCreationDTO("SubNonExistentEpic", UUID.randomUUID(), null, null);
    HttpResponse<String> nonExistentEpicSubRes = sendPostRequest(SUBTASKS_ENDPOINT, nonExistentEpicSubDto);
    System.out.println(
        "Create SubTask with non-existent epicId response status: "
            + nonExistentEpicSubRes.statusCode()
            + " (expected 400), Body: "
            + nonExistentEpicSubRes.body().lines().collect(Collectors.joining()));

    EpicTaskCreationDTO shortEpicDto = new EpicTaskCreationDTO("EpicS", "DescS", null);
    HttpResponse<String> shortEpicRes = sendPostRequest(EPICS_ENDPOINT, shortEpicDto);
    System.out.println(
        "Create Epic with short title/desc response status: "
            + shortEpicRes.statusCode()
            + " (expected 400), Body: "
            + shortEpicRes.body().lines().collect(Collectors.joining()));

    System.out.println("Final state in testBoundaryCases (from server):");
    printAllTasksFromServer();
    System.out.print(TEST_SECTION_END);
  }

  private static void testAdvancedOverlappingScenarios() throws IOException, InterruptedException {
    printTestSectionHeader("testAdvancedOverlappingScenarios (HTTP)");
    clearAllTasksOnServer();

    LocalDateTime now = LocalDateTime.now();
    Duration oneHour = Duration.ofHours(1);

    RegularTask task1 = createTaskViaHttp(TASKS_ENDPOINT, createRegularTaskCreationDTO("OverlapA", now, oneHour), RegularTask.class);
    if (task1 == null) {
      System.err.println("Task1 creation failed in testAdvancedOverlappingScenarios");
      return;
    }

    RegularTaskCreationDTO overlappingTaskDto = createRegularTaskCreationDTO("OverlapB", now.plusMinutes(30), oneHour);
    HttpResponse<String> overlapTaskResponse = sendPostRequest(TASKS_ENDPOINT, overlappingTaskDto);
    System.out.println(
        "Attempt to create overlapping RegularTask response (Status "
            + overlapTaskResponse.statusCode()
            + ", Expected 406): "
            + overlapTaskResponse.body().lines().collect(Collectors.joining()));
    if (overlapTaskResponse.statusCode() != 406) {
      System.err.println("ERROR: Expected 406 for overlapping RegularTask creation, got " + overlapTaskResponse.statusCode());
    }

    EpicTask epicForOverlap = createTaskViaHttp(EPICS_ENDPOINT, createEpicTaskCreationDTO("EpicOverlap"), EpicTask.class);
    if (epicForOverlap == null) {
      System.err.println("EpicForOverlap creation failed in testAdvancedOverlappingScenarios");
      return;
    }

    SubTask subTask1 = createTaskViaHttp(SUBTASKS_ENDPOINT, createSubTaskCreationDTO("SubOverlap1", epicForOverlap.getId(), now.plusHours(2), oneHour), SubTask.class);
    if (subTask1 == null) {
      System.err.println("SubTask1 creation failed in testAdvancedOverlappingScenarios");
      return;
    }

    SubTaskCreationDTO overlappingSubDto = createSubTaskCreationDTO("SubOverlap2", epicForOverlap.getId(), now.plusHours(2).plusMinutes(30), oneHour);
    HttpResponse<String> overlapSubResponse = sendPostRequest(SUBTASKS_ENDPOINT, overlappingSubDto);
    System.out.println(
        "Attempt to create overlapping SubTask response (Status "
            + overlapSubResponse.statusCode()
            + ", Expected 406): "
            + overlapSubResponse.body().lines().collect(Collectors.joining()));
    if (overlapSubResponse.statusCode() != 406) {
      System.err.println("ERROR: Expected 406 for overlapping SubTask creation, got " + overlapSubResponse.statusCode());
    }

    RegularTaskUpdateDTO updateTask1ToOverlapDto =
        new RegularTaskUpdateDTO(
            task1.getId(),
            task1.getTitle(),
            task1.getDescription(),
            task1.getStatus(),
            now.plusHours(2).plusMinutes(15),
            oneHour);
    HttpResponse<String> updateTask1OverlapResponse = sendPostRequest(TASKS_ENDPOINT, updateTask1ToOverlapDto);
    System.out.println(
        "Attempt to update RegularTask to overlap with unrelated SubTask (Status "
            + updateTask1OverlapResponse.statusCode()
            + ", Expected 406): "
            + updateTask1OverlapResponse.body().lines().collect(Collectors.joining()));
    if (updateTask1OverlapResponse.statusCode() != 406) {
      System.err.println(
          "ERROR: Expected 406 for updating RegularTask to overlap, got "
              + updateTask1OverlapResponse.statusCode());
    }
    System.out.print(TEST_SECTION_END);
  }

  private static void testPrioritizedTasksEndpoint() throws IOException, InterruptedException {
    printTestSectionHeader("testPrioritizedTasksEndpoint (HTTP)");
    clearAllTasksOnServer();

    LocalDateTime now = LocalDateTime.now().withNano(0);

    // Create tasks with varying start times and some without
    RegularTask rt1 =
        createTaskViaHttp(
            TASKS_ENDPOINT,
            createRegularTaskCreationDTO("RT1_Now", now, Duration.ofHours(1)),
            RegularTask.class);
    EpicTask epic1 =
        createTaskViaHttp(
            EPICS_ENDPOINT,
            createEpicTaskCreationDTO("EP1_WithSubs"),
            EpicTask.class); // No initial time, will get from subs
    RegularTask rt2 =
        createTaskViaHttp(
            TASKS_ENDPOINT,
            createRegularTaskCreationDTO("RT2_NowPlus1", now.plusHours(1), Duration.ofMinutes(30)),
            RegularTask.class);

    // These subtasks will define epic1's time
    SubTask st1_ForEpic1 =
        createTaskViaHttp(
            SUBTASKS_ENDPOINT,
            createSubTaskCreationDTO(
                "ST1_ForEp1_NowPlus2h", epic1.getId(), now.plusHours(2), Duration.ofHours(1)),
            SubTask.class);
    SubTask st2_ForEpic1 =
        createTaskViaHttp(
            SUBTASKS_ENDPOINT,
            createSubTaskCreationDTO(
                "ST2_ForEp1_NowPlus1h45",
                epic1.getId(),
                now.plusHours(1).plusMinutes(45),
                Duration.ofMinutes(15)),
            SubTask.class);

    RegularTask rt3_NullST =
        createTaskViaHttp(
            TASKS_ENDPOINT,
            createRegularTaskCreationDTO("RT3_NullST", null, null),
            RegularTask.class);
    EpicTask epic2_NoSubsNullST =
        createTaskViaHttp(
            EPICS_ENDPOINT,
            createEpicTaskCreationDTO("EP2_NoSubsNullST_WontBeInPrioritized"),
            EpicTask
                .class); // This epic won't have subtasks with time and won't be updated to be in
                         // schedule index

    // Create another epic with an explicit start time but no subtasks initially
    EpicTask epic3_ExplicitST =
        createTaskViaHttp(
            EPICS_ENDPOINT,
            createEpicTaskCreationDTO("EP3_ExplicitST_NoSubs", now.plusHours(5)),
            EpicTask.class);
    // To get epic3_ExplicitST into the schedule index, we need to "update" it or add a subtask.
    // A simple update of description will trigger the TaskManager's update path which can add it to
    // the index.
    if (epic3_ExplicitST != null) {
      EpicTaskUpdateDTO updateEpic3Dto =
          new EpicTaskUpdateDTO(
              epic3_ExplicitST.getId(),
              epic3_ExplicitST.getTitle(),
              epic3_ExplicitST.getDescription() + " updated slightly");
      sendPostRequest(EPICS_ENDPOINT, updateEpic3Dto); // This should place it in the schedule index
    }

    System.out.println("Tasks created for prioritized test:");
    if (rt1 != null) System.out.println("RT1: " + rt1.getId() + " ST: " + rt1.getStartTime());
    if (rt2 != null) System.out.println("RT2: " + rt2.getId() + " ST: " + rt2.getStartTime());
    if (st1_ForEpic1 != null)
      System.out.println(
          "ST1_ForEpic1: " + st1_ForEpic1.getId() + " ST: " + st1_ForEpic1.getStartTime());
    if (st2_ForEpic1 != null)
      System.out.println(
          "ST2_ForEpic1: " + st2_ForEpic1.getId() + " ST: " + st2_ForEpic1.getStartTime());
    if (rt3_NullST != null)
      System.out.println("RT3_NullST: " + rt3_NullST.getId() + " ST: " + rt3_NullST.getStartTime());
    if (epic1 != null) {
      EpicTask fetchedEpic1 =
          getTaskViaHttp(EPICS_ENDPOINT, epic1.getId().toString(), EpicTask.class);
      if (fetchedEpic1 != null)
        System.out.println(
            "Epic1: "
                + fetchedEpic1.getId()
                + " ST: "
                + fetchedEpic1.getStartTime()
                + " ET: "
                + fetchedEpic1.getEndTime());
    }
    if (epic3_ExplicitST != null) {
      EpicTask fetchedEpic3 =
          getTaskViaHttp(EPICS_ENDPOINT, epic3_ExplicitST.getId().toString(), EpicTask.class);
      if (fetchedEpic3 != null)
        System.out.println(
            "Epic3: "
                + fetchedEpic3.getId()
                + " ST: "
                + fetchedEpic3.getStartTime()
                + " ET: "
                + fetchedEpic3.getEndTime());
    }

    HttpResponse<String> response = sendGetRequest(PRIORITIZED_ENDPOINT);
    System.out.println(
        "Prioritized tasks response (Status "
            + response.statusCode()
            + "): "
            + response.body().lines().collect(Collectors.joining()));

    if (response.statusCode() != 200) {
      System.err.println(
          "ERROR: Failed to get prioritized tasks, status: " + response.statusCode());
      return;
    }

    Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
    List<Map<String, Object>> prioritizedTasks = parseResponseList(response, listType, 200);

    System.out.println("Number of prioritized tasks received: " + prioritizedTasks.size());
    prioritizedTasks.forEach(
        taskMap ->
            System.out.println(
                "ID: "
                    + taskMap.get("id")
                    + ", Title: "
                    + taskMap.get("title")
                    + ", StartTime: "
                    + taskMap.get("startTime")));

    // Expected order based on logic: rt1, rt2, st2_ForEpic1, epic1, st1_ForEpic1, epic3_ExplicitST,
    // rt3_NullST
    // epic2_NoSubsNullST should not be present as it's not added to schedule index
    // Total 7 tasks expected in prioritized list if epic3 is correctly indexed.

    // IDs of tasks that are expected to be in the prioritized list, in order.
    // Ensure all these tasks were successfully created.
    String[] expectedOrderIds = new String[7];
    int count = 0;
    if (rt1 != null) expectedOrderIds[count++] = rt1.getId().toString();
    if (rt2 != null) expectedOrderIds[count++] = rt2.getId().toString();
    if (st2_ForEpic1 != null) expectedOrderIds[count++] = st2_ForEpic1.getId().toString();
    if (epic1 != null) expectedOrderIds[count++] = epic1.getId().toString();
    if (st1_ForEpic1 != null) expectedOrderIds[count++] = st1_ForEpic1.getId().toString();
    if (epic3_ExplicitST != null) expectedOrderIds[count++] = epic3_ExplicitST.getId().toString();
    if (rt3_NullST != null) expectedOrderIds[count++] = rt3_NullST.getId().toString();

    if (prioritizedTasks.size() != count) {
      System.err.println(
          "ERROR: Expected " + count + " prioritized tasks, but got " + prioritizedTasks.size());
    } else {
      boolean orderCorrect = true;
      for (int i = 0; i < count; i++) {
        if (!prioritizedTasks.get(i).get("id").equals(expectedOrderIds[i])) {
          System.err.println(
              "ERROR: Prioritized task order mismatch at index "
                  + i
                  + ". Expected ID: "
                  + expectedOrderIds[i]
                  + ", Got ID: "
                  + prioritizedTasks.get(i).get("id")
                  + " (Title: "
                  + prioritizedTasks.get(i).get("title")
                  + ")");
          orderCorrect = false;
          // break; // Uncomment to stop at first mismatch
        }
      }
      if (orderCorrect) {
        System.out.println("Prioritized tasks order is correct.");
      } else {
        System.err.println("Full expected order: " + String.join(", ", expectedOrderIds));
        System.err.println(
            "Full actual order: "
                + prioritizedTasks.stream()
                    .map(m -> m.get("id").toString())
                    .collect(Collectors.joining(", ")));
      }
    }

    // Check that epic2_NoSubsNullST is NOT in the list
    if (epic2_NoSubsNullST != null) {
      boolean epic2Found =
          prioritizedTasks.stream()
              .anyMatch(taskMap -> taskMap.get("id").equals(epic2_NoSubsNullST.getId().toString()));
      if (epic2Found) {
        System.err.println(
            "ERROR: epic2_NoSubsNullST (ID: "
                + epic2_NoSubsNullST.getId()
                + ") was found in prioritized list but should not be.");
      } else {
        System.out.println(
            "epic2_NoSubsNullST (ID: "
                + epic2_NoSubsNullST.getId()
                + ") correctly not found in prioritized list.");
      }
    }

    System.out.print(TEST_SECTION_END);
  }
}

