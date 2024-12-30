package com.tasktracker.task.model.implementations;

import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.model.enums.TaskStatus;

/**
 * Represents a subtask that belongs to a specific epic com.tasktracker.task in a
 * com.tasktracker.task management system. Extends the generic {@link Task} class and adds the
 * association with an epic com.tasktracker.task.
 */
public final class SubTask extends Task {
  private final int epicTaskId;

  /**
   * Creates a new SubTask instance.
   *
   * @param id the unique identifier of the subtask
   * @param title the title of the subtask
   * @param description the description of the subtask
   * @param status the current status of the subtask
   * @param epicTaskId the ID of the epic com.tasktracker.task to which this subtask belongs
   * @throws ValidationException if the provided epic com.tasktracker.task ID is invalid
   */
  public SubTask(
      final int id,
      final String title,
      final String description,
      final TaskStatus status,
      final int epicTaskId) {
    super(id, title, description, status);
    this.epicTaskId = getValidEpicTaskId(epicTaskId);
  }

  /**
   * Gets the ID of the epic com.tasktracker.task to which this subtask belongs.
   *
   * @return the ID of the associated epic com.tasktracker.task
   */
  public int getEpicTaskId() {
    return epicTaskId;
  }

  /**
   * Validates the provided epic com.tasktracker.task ID.
   *
   * @param epicTaskId the ID of the epic com.tasktracker.task to validate
   * @return the validated epic com.tasktracker.task ID
   * @throws ValidationException if the epic com.tasktracker.task ID is negative
   */
  private int getValidEpicTaskId(int epicTaskId) {
    if (epicTaskId < 0) {
      throw new ValidationException("Epic Task ID must be a non-negative integer.");
    }
    return epicTaskId;
  }

  /**
   * Returns a string representation of the SubTask object.
   *
   * @return a string containing the subtask details in a readable format
   */
  @Override
  public String toString() {
    return "SubTask{"
        + "id="
        + super.getId()
        + ", title='"
        + super.getTitle()
        + '\''
        + ", description='"
        + super.getDescription()
        + '\''
        + ", status="
        + super.getStatus()
        + ", epicTaskId="
        + epicTaskId
        + '}';
  }
}
