package com.tasktracker;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpServer;
import com.tasktracker.json.GsonProvider;
import com.tasktracker.server.dispatcher.DispatcherHandler;
import com.tasktracker.server.filter.RequestValidationFilter;
import com.tasktracker.server.handler.*;
import com.tasktracker.server.router.RequestRouter;
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
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
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
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Main {

  private static final String TEST_SECTION_START_FORMAT = "===== %s =====";
  private static final String TEST_SECTION_END = "====================================\n";
  private static final int SERVER_PORT = 8080;
  private static final String BASE_URL = "http://localhost:" + SERVER_PORT;

  private static final String TASKS_ENDPOINT = "/tasks";
  private static final String SUBTASKS_ENDPOINT = "/subtasks";
  private static final String EPICS_ENDPOINT = "/epics";
  private static final String HISTORY_ENDPOINT = "/history";
  private static final String PRIORITIZED_ENDPOINT = "/prioritized";

  private static final String DEFAULT_TASK_TITLE_PREFIX = "Test Task ";
  private static final String DEFAULT_TASK_DESC_PREFIX = "Test Description long enough for ";
  private static final String UPDATED_TITLE_SUFFIX = " UPDATED";
  private static final String UPDATED_DESC_SUFFIX = " UPDATED - also long enough";


  private static HttpClient httpClient;
  private static Gson gson;
  private static TaskManager taskManagerForServer;

  private static void printTestSectionHeader(String testName) {
    System.out.printf((TEST_SECTION_START_FORMAT) + "%n", testName);
  }

  // --- HTTP Request Helper Methods ---
  private static HttpResponse<String> sendGetRequest(String path) throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + path)).GET().build();
    return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
  }

  private static <T> HttpResponse<String> sendPostRequest(String path, T bodyPayload) throws IOException, InterruptedException {
    String jsonBody = gson.toJson(bodyPayload);
    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + path)).POST(HttpRequest.BodyPublishers.ofString(jsonBody)).header("Content-Type", "application/json").build();
    return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
  }

  private static HttpResponse<String> sendDeleteRequest(String path) throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + path)).DELETE().build();
    return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
  }

  // --- DTO Creation Helper Methods ---
  private static RegularTaskCreationDTO createRegularTaskCreationDTO(String suffix, LocalDateTime startTime, Duration duration) {
    return new RegularTaskCreationDTO(DEFAULT_TASK_TITLE_PREFIX + "Regular " + suffix, DEFAULT_TASK_DESC_PREFIX + "Regular " + suffix, startTime, duration);
  }

  private static SubTaskCreationDTO createSubTaskCreationDTO(String suffix, UUID epicId, LocalDateTime startTime, Duration duration) {
    return new SubTaskCreationDTO(DEFAULT_TASK_TITLE_PREFIX + "SubTask " + suffix, DEFAULT_TASK_DESC_PREFIX + "SubTask " + suffix, epicId, startTime, duration);
  }

  private static EpicTaskCreationDTO createEpicTaskCreationDTO(String suffix) {
    return new EpicTaskCreationDTO(DEFAULT_TASK_TITLE_PREFIX + "Epic " + suffix, DEFAULT_TASK_DESC_PREFIX + "Epic " + suffix, null);
  }

  // --- Response Parsing Helper Methods ---
  private static <T> T parseResponse(HttpResponse<String> response, Class<T> clazz, int expectedStatus) {
    if (response.statusCode() == expectedStatus) {
      try {
        return gson.fromJson(response.body(), clazz);
      } catch (JsonSyntaxException e) {
        System.err.println("Error parsing JSON response for " + clazz.getSimpleName() + " (Status " + response.statusCode() + "): " + e.getMessage() + "\nResponse body: " + response.body());
        return null;
      }
    }
    System.err.println("Request for " + clazz.getSimpleName() + " failed or returned unexpected status. Expected " + expectedStatus + ", Got " + response.statusCode() + ". Body: " + response.body());
    return null;
  }

  private static <T> List<T> parseResponseList(HttpResponse<String> response, Type listType, int expectedStatus) {
    if (response.statusCode() == expectedStatus) {
      try {
        return gson.fromJson(response.body(), listType);
      } catch (JsonSyntaxException e) {
        System.err.println("Error parsing JSON list response (Status " + response.statusCode() + "): " + e.getMessage() + "\nResponse body: " + response.body());
        return Collections.emptyList();
      }
    }
    System.err.println("Request for list failed or returned unexpected status. Expected " + expectedStatus + ", Got " + response.statusCode() + ". Body: " + response.body());
    return Collections.emptyList();
  }

  // --- Task Operation Helper Methods (CRUD via HTTP) ---
  private static <T extends Task> T createTaskViaHttp(String endpoint, Object creationDto, Class<T> taskClass) throws IOException, InterruptedException {
    HttpResponse<String> response = sendPostRequest(endpoint, creationDto);
    System.out.println("Create " + taskClass.getSimpleName() + " response (Status " + response.statusCode() + "): " + response.body());
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
    System.out.println("Get " + taskClass.getSimpleName() + " by ID '" + taskId + "' response (Status " + response.statusCode() + "): " + response.body());
    return parseResponse(response, taskClass, 200);
  }

  private static void getTaskAndExpectStatus(String endpoint, String taskId, int expectedStatus) throws IOException, InterruptedException {
    if (taskId == null) {
      System.err.println("ERROR: Attempted to GET task with null ID (expecting status " + expectedStatus + ") from endpoint " + endpoint);
      return;
    }
    HttpResponse<String> response = sendGetRequest(endpoint + "/" + taskId);
    System.out.println("Attempt Get Task by ID '" + taskId + "' response (Status " + response.statusCode() + ", Expected " + expectedStatus + "): " + response.body());
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
    System.out.println("Update " + taskClass.getSimpleName() + " (ID: " + taskId + ") POST response (Status " + postResponse.statusCode() + ", Body should be pre-update state): " + postResponse.body());
    T taskFromResponse = parseResponse(postResponse, taskClass, 201);
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
    System.out.println("Delete " + taskType + " (ID: " + taskId + ") response (Status " + deleteResponse.statusCode() + "): " + deleteResponse.body());
    if (deleteResponse.statusCode() == 200) {
      System.out.println(taskType + " " + taskId + " deleted successfully.");
    } else {
      System.err.println("ERROR: Failed to delete " + taskType + " " + taskId + ". Status: " + deleteResponse.statusCode());
    }
    getTaskAndExpectStatus(endpoint, taskId, 404);
  }


  public static void main(String[] args) throws Exception {
    httpClient = HttpClient.newHttpClient();
    gson = GsonProvider.getGson();

    HttpServer server = null;
    try {
      taskManagerForServer = new TaskManagerImpl(new InMemoryTaskRepository(), new InMemoryHistoryManager(new InMemoryHistoryStore()));
      server = HttpServer.create(new InetSocketAddress(SERVER_PORT), 0);
      server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

      RequestRouter requestRouter = new RequestRouter();
      requestRouter.addRoute(TASKS_ENDPOINT, "POST", new TaskPostHandler<>(taskManagerForServer, RegularTaskCreationDTO.class, RegularTaskUpdateDTO.class, taskManagerForServer::addTask, taskManagerForServer::updateTask));
      requestRouter.addRoute(TASKS_ENDPOINT + "/{id}", "GET", new TaskGetHandler(taskManagerForServer, RegularTask.class));
      requestRouter.addRoute(TASKS_ENDPOINT, "GET", new TaskListGetHandler<>(taskManagerForServer, RegularTask.class));
      requestRouter.addRoute(TASKS_ENDPOINT + "/{id}", "DELETE", new TaskDeleteHandler<>(taskManagerForServer, RegularTask.class));

      requestRouter.addRoute(SUBTASKS_ENDPOINT, "POST", new TaskPostHandler<>(taskManagerForServer, SubTaskCreationDTO.class, SubTaskUpdateDTO.class, taskManagerForServer::addTask, taskManagerForServer::updateTask));
      requestRouter.addRoute(SUBTASKS_ENDPOINT + "/{id}", "GET", new TaskGetHandler(taskManagerForServer, SubTask.class));
      requestRouter.addRoute(SUBTASKS_ENDPOINT, "GET", new TaskListGetHandler<>(taskManagerForServer, SubTask.class));
      requestRouter.addRoute(SUBTASKS_ENDPOINT + "/{id}", "DELETE", new TaskDeleteHandler<>(taskManagerForServer, SubTask.class));

      requestRouter.addRoute(EPICS_ENDPOINT, "POST", new TaskPostHandler<>(taskManagerForServer, EpicTaskCreationDTO.class, EpicTaskUpdateDTO.class, taskManagerForServer::addTask, taskManagerForServer::updateTask));
      requestRouter.addRoute(EPICS_ENDPOINT + "/{id}", "GET", new TaskGetHandler(taskManagerForServer, EpicTask.class));
      requestRouter.addRoute(EPICS_ENDPOINT, "GET", new TaskListGetHandler<>(taskManagerForServer, EpicTask.class));
      requestRouter.addRoute(EPICS_ENDPOINT + "/{id}", "DELETE", new TaskDeleteHandler<>(taskManagerForServer, EpicTask.class));

      requestRouter.addRoute(HISTORY_ENDPOINT, "GET", new HistoryGetHandler(taskManagerForServer));
      requestRouter.addRoute(PRIORITIZED_ENDPOINT, "GET", new TasksPrioritizedListGetHandler(taskManagerForServer));

      DispatcherHandler dispatcherHandler = new DispatcherHandler(requestRouter);
      server.createContext("/", dispatcherHandler).getFilters().add(new RequestValidationFilter());
      server.start();
      System.out.println("HTTP server started on port " + SERVER_PORT);

      testUserScenario();
      testBasicCrudOperations();
      testAdditionalEpicScenarios();
      testRemovingTasks();
      testUpdateEpicAndSubtaskStatus();
      testBoundaryCases();
      testAdvancedOverlappingScenarios();

    } finally {
      if (server != null) {
        server.stop(1);
        System.out.println("HTTP server stopped.");
      }
      if (taskManagerForServer != null) {
        taskManagerForServer.clearAllTasks();
      }
    }
  }

  private static void clearAllTasksOnServer() throws IOException, InterruptedException {
    System.out.println("Directly clearing taskManagerForServer's internal state (including ScheduleIndex)...");
    if (taskManagerForServer != null) {
      taskManagerForServer.clearAllTasks();
    }

    System.out.println("Attempting to clear any remaining tasks on server via HTTP DELETE requests (belt and suspenders)...");
    HttpResponse<String> tasksResponse = sendGetRequest(TASKS_ENDPOINT);
    if (tasksResponse.statusCode() == 200) {
      List<RegularTask> tasks = parseResponseList(tasksResponse, new TypeToken<List<RegularTask>>() {}.getType(), 200);
      for (RegularTask task : tasks) {
        if(task != null && task.getId() != null) sendDeleteRequest(TASKS_ENDPOINT + "/" + task.getId());
      }
    }
    HttpResponse<String> subtasksResponse = sendGetRequest(SUBTASKS_ENDPOINT);
    if (subtasksResponse.statusCode() == 200) {
      List<SubTask> subtasks = parseResponseList(subtasksResponse, new TypeToken<List<SubTask>>() {}.getType(), 200);
      for (SubTask subtask : subtasks) {
        if(subtask != null && subtask.getId() != null) sendDeleteRequest(SUBTASKS_ENDPOINT + "/" + subtask.getId());
      }
    }
    HttpResponse<String> epicsResponse = sendGetRequest(EPICS_ENDPOINT);
    if (epicsResponse.statusCode() == 200) {
      List<EpicTask> epics = parseResponseList(epicsResponse, new TypeToken<List<EpicTask>>() {}.getType(), 200);
      for (EpicTask epic : epics) {
        if(epic != null && epic.getId() != null) sendDeleteRequest(EPICS_ENDPOINT + "/" + epic.getId());
      }
    }
    System.out.println("Finished clearing tasks on server.");
  }

  private static void testUserScenario() throws IOException, InterruptedException {
    printTestSectionHeader("testUserScenario (HTTP)");
    clearAllTasksOnServer();

    RegularTask regularTask1 = createTaskViaHttp(TASKS_ENDPOINT, createRegularTaskCreationDTO("UserScenario1", null, null), RegularTask.class);
    RegularTask regularTask2 = createTaskViaHttp(TASKS_ENDPOINT, createRegularTaskCreationDTO("UserScenario2", null, null), RegularTask.class);
    EpicTask epicWithSubtasks = createTaskViaHttp(EPICS_ENDPOINT, createEpicTaskCreationDTO("UserScenarioEpic1"), EpicTask.class);
    if (regularTask1 == null || regularTask2 == null || epicWithSubtasks == null) {System.err.println("Setup failed in testUserScenario"); return;}

    SubTask subTask1 = createTaskViaHttp(SUBTASKS_ENDPOINT, createSubTaskCreationDTO("UserScenarioSub1", epicWithSubtasks.getId(), null, null), SubTask.class);
    SubTask subTask2 = createTaskViaHttp(SUBTASKS_ENDPOINT, createSubTaskCreationDTO("UserScenarioSub2", epicWithSubtasks.getId(), null, null), SubTask.class);
    SubTask subTask3 = createTaskViaHttp(SUBTASKS_ENDPOINT, createSubTaskCreationDTO("UserScenarioSub3", epicWithSubtasks.getId(), null, null), SubTask.class);
    EpicTask epicWithoutSubtasks = createTaskViaHttp(EPICS_ENDPOINT, createEpicTaskCreationDTO("UserScenarioEpic2"), EpicTask.class);
    if (subTask1 == null || subTask2 == null || subTask3 == null || epicWithoutSubtasks == null) {System.err.println("Sub-task/Epic creation failed in testUserScenario"); return;}


    System.out.println("Accessing tasks to populate history:");
    getTaskViaHttp(TASKS_ENDPOINT, regularTask1.getId().toString(), RegularTask.class); printHistoryViaHttp();
    getTaskViaHttp(EPICS_ENDPOINT, epicWithSubtasks.getId().toString(), EpicTask.class); printHistoryViaHttp();
    getTaskViaHttp(SUBTASKS_ENDPOINT, subTask2.getId().toString(), SubTask.class); printHistoryViaHttp();
    getTaskViaHttp(TASKS_ENDPOINT, regularTask2.getId().toString(), RegularTask.class); printHistoryViaHttp();
    getTaskViaHttp(EPICS_ENDPOINT, epicWithoutSubtasks.getId().toString(), EpicTask.class); printHistoryViaHttp();
    getTaskViaHttp(SUBTASKS_ENDPOINT, subTask1.getId().toString(), SubTask.class); printHistoryViaHttp();
    getTaskViaHttp(SUBTASKS_ENDPOINT, subTask3.getId().toString(), SubTask.class); printHistoryViaHttp();
    getTaskViaHttp(EPICS_ENDPOINT, epicWithSubtasks.getId().toString(), EpicTask.class); printHistoryViaHttp();

    System.out.println("Final history:"); printHistoryViaHttp();

    deleteTaskViaHttp(TASKS_ENDPOINT, regularTask1.getId().toString(), "RegularTask");
    System.out.println("History after deleting Regular Task 1:"); printHistoryViaHttp();

    deleteTaskViaHttp(EPICS_ENDPOINT, epicWithSubtasks.getId().toString(), "EpicTask");
    System.out.println("History after deleting epic with subtasks:"); printHistoryViaHttp();
    System.out.print(TEST_SECTION_END);
  }

  private static void printHistoryViaHttp() throws IOException, InterruptedException {
    HttpResponse<String> response = sendGetRequest(HISTORY_ENDPOINT);
    System.out.print("History from HTTP: ");
    if (response.statusCode() == 200) {
      List<Map<String, Object>> history = parseResponseList(response, new TypeToken<List<Map<String, Object>>>() {}.getType(), 200);
      if (history.isEmpty()) {
        System.out.print("empty");
      } else {
        String historyString = history.stream().map(taskMap -> taskMap.get("id") + " (" + taskMap.get("title") + ")").collect(Collectors.joining(", "));
        System.out.print(historyString);
      }
    } else {
      System.out.print("Failed to fetch history, status: " + response.statusCode());
    }
    System.out.println();
  }

  private static void printAllTasksFromServer() throws IOException, InterruptedException {
    System.out.println("--- All tasks from server ---");
    HttpResponse<String> tasksRes = sendGetRequest(TASKS_ENDPOINT);
    List<RegularTask> regularTasks = parseResponseList(tasksRes, new TypeToken<List<RegularTask>>() {}.getType(), 200);
    regularTasks.forEach(System.out::println);

    HttpResponse<String> subtasksRes = sendGetRequest(SUBTASKS_ENDPOINT);
    List<SubTask> subTasks = parseResponseList(subtasksRes, new TypeToken<List<SubTask>>() {}.getType(), 200);
    subTasks.forEach(System.out::println);

    HttpResponse<String> epicsRes = sendGetRequest(EPICS_ENDPOINT);
    List<EpicTask> epicTasks = parseResponseList(epicsRes, new TypeToken<List<EpicTask>>() {}.getType(), 200);
    epicTasks.forEach(System.out::println);
    System.out.println("-----------------------------\n");
  }

  private static void testBasicCrudOperations() throws IOException, InterruptedException {
    printTestSectionHeader("testBasicCrudOperations (HTTP)");
    clearAllTasksOnServer();

    RegularTask regTask1 = createTaskViaHttp(TASKS_ENDPOINT, createRegularTaskCreationDTO("CRUD1", null, null), RegularTask.class);
    RegularTask regTask2 = createTaskViaHttp(TASKS_ENDPOINT, createRegularTaskCreationDTO("CRUD2", null, null), RegularTask.class);
    EpicTask epic1 = createTaskViaHttp(EPICS_ENDPOINT, createEpicTaskCreationDTO("CRUDEpic1"), EpicTask.class);
    if (regTask1 == null || regTask2 == null || epic1 == null) {System.err.println("Setup failed in testBasicCrudOperations"); return;}

    SubTask sub1 = createTaskViaHttp(SUBTASKS_ENDPOINT, createSubTaskCreationDTO("CRUDSub1", epic1.getId(), null, null), SubTask.class);
    if (sub1 == null) {System.err.println("SubTask creation failed in testBasicCrudOperations"); return;}


    System.out.println("Initial tasks from server:"); printAllTasksFromServer();

    RegularTaskUpdateDTO regTask1UpdateDto = new RegularTaskUpdateDTO(regTask1.getId(), regTask1.getTitle() + UPDATED_TITLE_SUFFIX, regTask1.getDescription() + UPDATED_DESC_SUFFIX, TaskStatus.IN_PROGRESS, null, null);
    updateTaskViaHttp(TASKS_ENDPOINT, regTask1.getId().toString(), regTask1UpdateDto, RegularTask.class, regTask1.getTitle());

    SubTaskUpdateDTO sub1UpdateDto = new SubTaskUpdateDTO(sub1.getId(), sub1.getTitle() + UPDATED_TITLE_SUFFIX, sub1.getDescription() + UPDATED_DESC_SUFFIX, TaskStatus.DONE, epic1.getId(), null, null);
    updateTaskViaHttp(SUBTASKS_ENDPOINT, sub1.getId().toString(), sub1UpdateDto, SubTask.class, sub1.getTitle());

    System.out.println("After updates (tasks from server):"); printAllTasksFromServer();
    getTaskViaHttp(TASKS_ENDPOINT, regTask2.getId().toString(), RegularTask.class);
    System.out.print(TEST_SECTION_END);
  }

  private static void testAdditionalEpicScenarios() throws IOException, InterruptedException {
    printTestSectionHeader("testAdditionalEpicScenarios (HTTP)");
    clearAllTasksOnServer();

    EpicTask epicA = createTaskViaHttp(EPICS_ENDPOINT, createEpicTaskCreationDTO("EpicAScenarios"), EpicTask.class);
    if (epicA == null) {System.err.println("EpicA creation failed in testAdditionalEpicScenarios"); return;}
    SubTask subA1 = createTaskViaHttp(SUBTASKS_ENDPOINT, createSubTaskCreationDTO("SubA1", epicA.getId(), null, null), SubTask.class);
    SubTask subA2 = createTaskViaHttp(SUBTASKS_ENDPOINT, createSubTaskCreationDTO("SubA2", epicA.getId(), null, null), SubTask.class);
    if (subA1 == null || subA2 == null) {System.err.println("SubTask creation for EpicA failed in testAdditionalEpicScenarios"); return;}


    System.out.println("All tasks after creation (from server):"); printAllTasksFromServer();

    SubTaskUpdateDTO subA1UpdateDto = new SubTaskUpdateDTO(subA1.getId(), subA1.getTitle() + UPDATED_TITLE_SUFFIX, subA1.getDescription(), TaskStatus.DONE, epicA.getId(), null, null);
    updateTaskViaHttp(SUBTASKS_ENDPOINT, subA1.getId().toString(), subA1UpdateDto, SubTask.class, subA1.getTitle());

    SubTaskUpdateDTO subA2UpdateDto = new SubTaskUpdateDTO(subA2.getId(), subA2.getTitle() + UPDATED_TITLE_SUFFIX, subA2.getDescription(), TaskStatus.DONE, epicA.getId(), null, null);
    updateTaskViaHttp(SUBTASKS_ENDPOINT, subA2.getId().toString(), subA2UpdateDto, SubTask.class, subA2.getTitle());

    System.out.println("After setting all subtasks to DONE (tasks from server):"); printAllTasksFromServer();

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
    if (regTaskA == null || epicA == null) {System.err.println("Setup failed in testRemovingTasks"); return;}
    SubTask subA1 = createTaskViaHttp(SUBTASKS_ENDPOINT, createSubTaskCreationDTO("SubA1ForRemoval", epicA.getId(), null, null), SubTask.class);
    if (subA1 == null ) {System.err.println("SubTask creation for EpicA failed in testRemovingTasks"); return;}


    System.out.println("Tasks before removing anything (from server):"); printAllTasksFromServer();

    deleteTaskViaHttp(TASKS_ENDPOINT, regTaskA.getId().toString(), "RegularTask");
    printAllTasksFromServer();

    deleteTaskViaHttp(EPICS_ENDPOINT, epicA.getId().toString(), "EpicTask");
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
    if (epicB == null) {System.err.println("EpicB creation failed in testUpdateEpicAndSubtaskStatus"); return;}
    SubTask subB1 = createTaskViaHttp(SUBTASKS_ENDPOINT, createSubTaskCreationDTO("SubB1Status", epicB.getId(), null, null), SubTask.class);
    SubTask subB2 = createTaskViaHttp(SUBTASKS_ENDPOINT, createSubTaskCreationDTO("SubB2Status", epicB.getId(), null, null), SubTask.class);
    if (subB1 == null || subB2 == null) {System.err.println("SubTask creation for EpicB failed in testUpdateEpicAndSubtaskStatus"); return;}


    System.out.println("Initial tasks (from server):"); printAllTasksFromServer();
    EpicTask epicBInit = getTaskViaHttp(EPICS_ENDPOINT, epicB.getId().toString(), EpicTask.class);
    if (epicBInit != null) System.out.println("Initial EpicB status: " + epicBInit.getStatus());

    SubTaskUpdateDTO subB1UpdateDto = new SubTaskUpdateDTO(subB1.getId(), subB1.getTitle(), subB1.getDescription(), TaskStatus.DONE, epicB.getId(), null, null);
    updateTaskViaHttp(SUBTASKS_ENDPOINT, subB1.getId().toString(), subB1UpdateDto, SubTask.class, subB1.getTitle());
    System.out.println("After subB1 -> DONE (tasks from server):"); printAllTasksFromServer();
    EpicTask epicBAfterSub1Done = getTaskViaHttp(EPICS_ENDPOINT, epicB.getId().toString(), EpicTask.class);
    if (epicBAfterSub1Done != null) {
      System.out.println("EpicB status should be IN_PROGRESS now. Actual: " + epicBAfterSub1Done.getStatus());
      if (epicBAfterSub1Done.getStatus() != TaskStatus.IN_PROGRESS) {
        System.err.println("ERROR: Epic B status is " + epicBAfterSub1Done.getStatus() + ", expected IN_PROGRESS.");
      }
    }

    SubTaskUpdateDTO subB2UpdateDto = new SubTaskUpdateDTO(subB2.getId(), subB2.getTitle(), subB2.getDescription(), TaskStatus.DONE, epicB.getId(), null, null);
    updateTaskViaHttp(SUBTASKS_ENDPOINT, subB2.getId().toString(), subB2UpdateDto, SubTask.class, subB2.getTitle());
    System.out.println("After subB2 -> DONE (tasks from server):"); printAllTasksFromServer();
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
    System.out.println("Create valid RegularTask response status: " + validRegRes.statusCode());

    RegularTaskCreationDTO shortTitleDto = new RegularTaskCreationDTO("Short", DEFAULT_TASK_DESC_PREFIX + "short title", null, null);
    HttpResponse<String> shortTitleRes = sendPostRequest(TASKS_ENDPOINT, shortTitleDto);
    System.out.println("Create RegularTask with short title response status: " + shortTitleRes.statusCode() + " (expected 400), Body: " + shortTitleRes.body());

    EpicTask dummyEpic = createTaskViaHttp(EPICS_ENDPOINT, createEpicTaskCreationDTO("DummyForSubtaskBoundary"), EpicTask.class);
    if (dummyEpic == null) { System.err.println("Failed to create dummy epic for boundary tests."); return; }


    SubTaskCreationDTO shortDescSubDto = new SubTaskCreationDTO(DEFAULT_TASK_TITLE_PREFIX + "SubShortDesc", "Tiny", dummyEpic.getId(), null, null);
    HttpResponse<String> shortDescSubRes = sendPostRequest(SUBTASKS_ENDPOINT, shortDescSubDto);
    System.out.println("Create SubTask with short description response status: " + shortDescSubRes.statusCode() + " (expected 400), Body: " + shortDescSubRes.body());

    SubTaskCreationDTO nonExistentEpicSubDto = createSubTaskCreationDTO("SubNonExistentEpic", UUID.randomUUID(), null, null);
    HttpResponse<String> nonExistentEpicSubRes = sendPostRequest(SUBTASKS_ENDPOINT, nonExistentEpicSubDto);
    System.out.println("Create SubTask with non-existent epicId response status: " + nonExistentEpicSubRes.statusCode() + " (expected 400), Body: " + nonExistentEpicSubRes.body());

    EpicTaskCreationDTO shortEpicDto = new EpicTaskCreationDTO("EpicS", "DescS", null);
    HttpResponse<String> shortEpicRes = sendPostRequest(EPICS_ENDPOINT, shortEpicDto);
    System.out.println("Create Epic with short title/desc response status: " + shortEpicRes.statusCode() + " (expected 400), Body: " + shortEpicRes.body());

    System.out.println("Final state in testBoundaryCases (from server):"); printAllTasksFromServer();
    System.out.print(TEST_SECTION_END);
  }

  private static void testAdvancedOverlappingScenarios() throws IOException, InterruptedException {
    printTestSectionHeader("testAdvancedOverlappingScenarios (HTTP)");
    clearAllTasksOnServer();
    LocalDateTime now = LocalDateTime.now();
    Duration oneHour = Duration.ofHours(1);

    RegularTask task1 = createTaskViaHttp(TASKS_ENDPOINT, createRegularTaskCreationDTO("OverlapA", now, oneHour), RegularTask.class);
    if (task1 == null) {System.err.println("Task1 creation failed in testAdvancedOverlappingScenarios"); return;}


    RegularTaskCreationDTO overlappingTaskDto = createRegularTaskCreationDTO("OverlapB", now.plusMinutes(30), oneHour);
    HttpResponse<String> overlapTaskResponse = sendPostRequest(TASKS_ENDPOINT, overlappingTaskDto);
    System.out.println("Attempt to create overlapping RegularTask response (Status " + overlapTaskResponse.statusCode() + ", Expected 406): " + overlapTaskResponse.body());
    if (overlapTaskResponse.statusCode() != 406) {
      System.err.println("ERROR: Expected 406 for overlapping RegularTask creation, got " + overlapTaskResponse.statusCode());
    }


    EpicTask epicForOverlap = createTaskViaHttp(EPICS_ENDPOINT, createEpicTaskCreationDTO("EpicOverlap"), EpicTask.class);
    if (epicForOverlap == null) {System.err.println("EpicForOverlap creation failed in testAdvancedOverlappingScenarios"); return;}
    SubTask subTask1 = createTaskViaHttp(SUBTASKS_ENDPOINT, createSubTaskCreationDTO("SubOverlap1", epicForOverlap.getId(), now.plusHours(2), oneHour), SubTask.class);
    if (subTask1 == null) {System.err.println("SubTask1 creation failed in testAdvancedOverlappingScenarios"); return;}


    SubTaskCreationDTO overlappingSubDto = createSubTaskCreationDTO("SubOverlap2", epicForOverlap.getId(), now.plusHours(2).plusMinutes(30), oneHour);
    HttpResponse<String> overlapSubResponse = sendPostRequest(SUBTASKS_ENDPOINT, overlappingSubDto);
    System.out.println("Attempt to create overlapping SubTask response (Status " + overlapSubResponse.statusCode() + ", Expected 406): " + overlapSubResponse.body());
    if (overlapSubResponse.statusCode() != 406) {
      System.err.println("ERROR: Expected 406 for overlapping SubTask creation, got " + overlapSubResponse.statusCode());
    }

    RegularTaskUpdateDTO updateTask1ToOverlapDto = new RegularTaskUpdateDTO(task1.getId(), task1.getTitle(), task1.getDescription(), task1.getStatus(), now.plusHours(2).plusMinutes(15), oneHour);
    HttpResponse<String> updateTask1OverlapResponse = sendPostRequest(TASKS_ENDPOINT, updateTask1ToOverlapDto);
    System.out.println("Attempt to update RegularTask to overlap with unrelated SubTask (Status " + updateTask1OverlapResponse.statusCode() + ", Expected 406 if global overlap is checked): " + updateTask1OverlapResponse.body());

    System.out.print(TEST_SECTION_END);
  }
}
