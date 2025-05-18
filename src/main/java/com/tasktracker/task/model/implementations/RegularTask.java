package com.tasktracker.task.model.implementations;

import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.model.enums.TaskStatus;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a regular com.tasktracker.task with a unique ID, title, description, and status. It
 * extends the {@link Task} class to provide specific functionality for regular tasks.
 */
public final class RegularTask extends Task {

  /**
   * Constructs a new RegularTask with the specified parameters.
   *
   * @param id the unique identifier for the task; must be a valid UUID
   * @param title the title of the task; cannot be null or empty
   * @param description the description of the task; cannot be null or empty
   * @param status the current status of the task, as defined in {@link TaskStatus}; cannot be null
   * @param creationDateTime the creation date of the task; cannot be null
   * @param updateDateTime the last update date of the task; cannot be null
   * @param startTime when this task is scheduled to begin; can be null
   * @param duration how long this task is expected to take; can be null
   * @throws ValidationException if any input validation fails
   */
  public RegularTask(
      final UUID id,
      final String title,
      final String description,
      final TaskStatus status,
      final LocalDateTime creationDateTime,
      final LocalDateTime updateDateTime,
      final LocalDateTime startTime,
      final Duration duration)
      throws ValidationException {
    super(id, title, description, status, creationDateTime, updateDateTime, startTime, duration);
  }

  /**
   * Returns a string representation of this RegularTask, including its ID, title, description,
   * status, creation date, and update date.
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
        + ", creationDate="
        + super.getCreationDate()
        + ", updateDate="
        + super.getUpdateDate()
        + '}';
  }
}
