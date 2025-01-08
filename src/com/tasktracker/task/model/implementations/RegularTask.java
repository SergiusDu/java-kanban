package com.tasktracker.task.model.implementations;

import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.model.enums.TaskStatus;
import java.time.LocalDateTime;

/**
 * Represents a regular com.tasktracker.task with a unique ID, title, description, and status. It
 * extends the {@link Task} class to provide specific functionality for regular tasks.
 */
public final class RegularTask extends Task {

  /**
   * Constructs a new RegularTask with the specified parameters.
   *
   * @param id the unique identifier for the task; must be greater than 0
   * @param title the title of the task; cannot be null or shorter than the minimum required length
   * @param description the description of the task; cannot be null or shorter than the minimum
   *     required length
   * @param status the current status of the task, as defined in {@link TaskStatus}; cannot be null
   * @param creationDate the creation date of the task; cannot be null
   * @param updateDate the last update date of the task; cannot be null
   * @throws ValidationException if any input validation fails
   * @throws NullPointerException if any parameter is null
   */
  public RegularTask(
      final int id,
      final String title,
      final String description,
      final TaskStatus status,
      final LocalDateTime creationDate,
      final LocalDateTime updateDate)
      throws ValidationException, NullPointerException {
    super(id, title, description, status, creationDate, updateDate);
  }

  /**
   * Returns a string representation of this RegularTask, including its ID, title, description, and
   * status.
   *
   * @return a string representation of the com.tasktracker.task
   */
  @Override
  public String toString() {
    return "RegularTask{"
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
        + '}';
  }
}
