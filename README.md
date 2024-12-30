# Task Tracker Project

This project implements a simple in-memory task tracker application. It allows for the creation, retrieval, update, and
deletion of three types of tasks: Regular Tasks, Epic Tasks, and Subtasks. The application focuses on maintaining the
relationships between Epic Tasks and their associated Subtasks, including automatic status updates for Epics based on
the status of their Subtasks.

## Architecture

The project follows a layered architecture, promoting separation of concerns and maintainability:

**1. DTO (Data Transfer Objects):**

* This layer defines simple immutable data structures used for transferring task information between layers. It uses
  Java records for concise syntax. Examples include `RegularTaskCreationDTO`, `EpicTaskCreationDTO`,
  `SubTaskCreationDTO`, and their corresponding update DTOs. The `TaskCreationDTO` sealed interface ensures that only
  the defined record types can implement it, improving type safety.

**2. Model:**

* Contains the core domain objects representing tasks. The `Task` class is an abstract sealed class, serving as the base
  for concrete task types: `RegularTask`, `EpicTask`, and `SubTask`. This design enforces a closed hierarchy of task
  types. The `TaskStatus` enum defines the possible states a task can be in (NEW, IN_PROGRESS, DONE). Validation logic
  related to task properties (e.g., title length, description length) is encapsulated within the model classes.

**3. Manager:**

* The `TaskManager` class provides the business logic for managing tasks. It interacts with the `TaskRepository` for
  persistence operations. It handles the creation, retrieval, updating, and deletion of tasks, including the management
  of Epic-Subtask relationships and automatic Epic status updates. The `TaskManager` also leverages the validation layer
  to ensure data integrity before interacting with the repository.

**4. Store/Repository:**

* The `TaskRepository` interface defines the contract for data access. The `InMemoryTaskRepository` provides an
  in-memory implementation using a `TreeMap` for efficient storage and retrieval of tasks by ID. This allows for quick
  prototyping and testing without requiring a persistent database.

**5. Validation:**

* The validation layer ensures data integrity. It uses a `Validator` interface and a `ValidatorFactory` to provide
  specific validators for each DTO type. The `CommonValidationUtils` class houses common validation logic (e.g., minimum
  title and description lengths). This design allows for easy extension with new validation rules and keeps validation
  logic separate from the core business logic.

**6. Util:**

* Contains utility classes for common operations. The `TypeSafeCaster` provides helper methods for safe casting of
  objects and Optionals, improving type safety and handling potential `ClassCastException` scenarios gracefully.

**7. Main:**

* The `Main` class contains a series of test methods demonstrating the functionality of the task tracker. These tests
  cover basic CRUD operations, Epic-Subtask interactions, task removal, status updates, and boundary case handling.

## Dependencies

The project currently has no external dependencies beyond the Java standard library.

## Build and Run

This project can be built and run using a standard Java development environment (e.g., IntelliJ IDEA, Eclipse). Simply
compile the Java source files and run the `Main` class.

## Future Improvements

* **Persistence:** Implement a persistent `TaskRepository` using a database (e.g., PostgreSQL, MySQL) to store tasks
  beyond the application's lifecycle.
* **Serialization:** Add serialization/deserialization capabilities (e.g., using JSON) to save and load tasks from
  files.
* **User Interface:** Develop a user interface (e.g., command-line, web-based) to interact with the task tracker.
* **Search and Filtering:** Implement search functionality and filtering options to manage larger collections of tasks.
* **History Tracking:**  Track changes to tasks over time, allowing for reverting to previous versions.
* **Prioritization:** Add task prioritization to allow for ordering tasks within an Epic or globally.
* **Due Dates:**  Incorporate due dates for tasks and reminders.

## Conclusion

This project provides a solid foundation for a task tracker application. Its layered architecture and focus on type
safety and validation make it maintainable and extensible. The provided test cases demonstrate the core functionality
and serve as a starting point for further development.
