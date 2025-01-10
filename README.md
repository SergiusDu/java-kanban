# Task Tracker Project

This project implements a simple in-memory task tracker application in Java. It supports three types of tasks: Regular
Tasks, Epic Tasks, and Subtasks (which belong to Epic Tasks). The project emphasizes clean code, testability, and
follows best practices for object-oriented design. It includes comprehensive unit tests to ensure correctness and
robustness.

## Project Overview

The Task Tracker application allows users to perform CRUD (Create, Read, Update, Delete) operations on tasks. Epic Tasks
act as containers for Subtasks, and their status is dynamically calculated based on the status of their constituent
Subtasks. The application also maintains a history of viewed tasks.

## Architectural Approach

The project adopts a layered architecture, promoting separation of concerns and maintainability:

* **Model Layer (`com.tasktracker.task.model`):**  Defines the core data structures representing tasks (Task,
  RegularTask, EpicTask, SubTask) and task views (TaskView). The `Task` class is an abstract sealed class forming the
  base for the different task types. The model enforces validation rules to ensure data integrity. Immutable data
  structures (records) are used where appropriate for DTOs, enhancing thread safety and simplifying code.
* **Data Transfer Object (DTO) Layer (`com.tasktracker.task.dto`):** Contains DTOs (e.g., `RegularTaskCreationDTO`,
  `EpicTaskUpdateDTO`) used for transferring data between layers, decoupling the model from external interfaces. A
  sealed interface `TaskCreationDTO` defines common properties for task creation DTOs, improving type safety and
  clarity.
* **Validation Layer (`com.tasktracker.task.validation`):**  Implements validation logic for DTOs using a `Validator`
  interface and a `ValidatorFactory`. This layer ensures that incoming data meets the required criteria before being
  processed. Common validation logic is centralized in `CommonValidationUtils`.
* **Store Layer (`com.tasktracker.task.store`):** Provides interfaces (`TaskRepository`, `HistoryRepository`) and
  in-memory implementations (`InMemoryTaskRepository`, `InMemoryHistoryRepository`) for persistent storage. The
  in-memory implementation uses `NavigableMap` and `NavigableSet` for efficient data retrieval and management.
* **Manager Layer (`com.tasktracker.task.manager`):**  Contains the core application logic.  `TaskManager` interface
  defines the operations available on tasks. `InMemoryTaskManager` provides an implementation of the TaskManager,
  handling task creation, updates, deletion, retrieval, and status management.  `HistoryManager` and its in-memory
  implementation manage the viewing history of tasks, adhering to a configurable history limit.
* **Utility Layer (`com.tasktracker.util`):**  Contains utility classes like `TypeSafeCaster` for type-safe casting and
  `Managers` for providing default configurations of managers. This layer helps keep other layers focused on their core
  responsibilities.
* **Main Application (`com.tasktracker.Main`):** Demonstrates the usage of the Task Tracker application through a series
  of test scenarios.

## Key Features and Design Choices

* **In-Memory Storage:**  The current implementation uses in-memory storage. The `TaskRepository` and
  `HistoryRepository` interfaces are designed to allow for easy replacement with other storage mechanisms (e.g.,
  database persistence) in the future.
* **History Management:** The `HistoryManager` keeps track of recently accessed tasks using a doubly linked list
  implementation, ensuring efficient addition and removal while respecting the history size limit.
* **Epic Task Status Calculation:**  The status of an Epic Task is automatically updated whenever a Subtask's status
  changes, ensuring data consistency.
* **Validation:**  Input validation is performed using a dedicated validation layer, preventing invalid data from
  corrupting the application state.
* **Unit Testing:** Extensive unit tests cover all core functionalities and edge cases, ensuring code quality and
  reliability. JUnit 5 is used for testing.
* **Clear Exception Handling:**  Exceptions are used to signal errors and exceptional conditions, aiding in debugging
  and troubleshooting. A custom `ValidationException` is used to handle data validation errors, providing detailed error
  messages.
* **Atomic Operations:**  The code utilizes atomic operations where appropriate (e.g., updating the task store) to
  enhance thread safety, although the current application is single-threaded.

## Future Enhancements

* **Persistence:** Implement persistent storage using a database or file system.
* **User Interface:**  Develop a user interface (e.g., command-line, web-based) for interacting with the Task Tracker.
* **Task Dependencies:**  Introduce the ability to define dependencies between tasks.
* **Prioritization:** Allow users to prioritize tasks.
* **Search and Filtering:** Implement more sophisticated search and filtering capabilities.
