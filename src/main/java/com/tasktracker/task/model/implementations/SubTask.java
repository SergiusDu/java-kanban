package com.tasktracker.task.model.implementations;

import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.model.enums.TaskStatus;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a subtask that belongs to a specific epic com.tasktracker.task in a
 * com.tasktracker.task management system. Extends the generic {@link Task} class and adds the
 * association with an epic com.tasktracker.task.
 */
public final class SubTask extends Task {
  private final UUID epicTaskId;

  /**
   * Constructs a new SubTask with the specified parameters.
   *
   * @param id The unique identifier for this subtask
   * @param title The name/title of this subtask
   * @param description A brief description of what this subtask entails
   * @param status The current state of progress of this subtask (e.g. NEW, IN_PROGRESS, etc)
   * @param epicTaskId The identifier of the parent Epic task this subtask belongs to
   * @param creationDateTime The date and time when this subtask was initially created
   * @param updateDateTime The date and time when this subtask was last modified
   * @param startTime When this subtask is scheduled to begin
   * @param duration How long this subtask is expected to take
   * @throws ValidationException If any of the input parameters fail validation checks
   */
  public SubTask(
      final UUID id,
      final String title,
      final String description,
      final TaskStatus status,
      final UUID epicTaskId,
      final LocalDateTime creationDateTime,
      final LocalDateTime updateDateTime,
      final LocalDateTime startTime,
      final Duration duration)
      throws ValidationException {
    super(id, title, description, status, creationDateTime, updateDateTime, startTime, duration);
    this.epicTaskId = Objects.requireNonNull(epicTaskId, "Epic task id can't be null.");
  }

  /**
   * Gets the ID of the epic com.tasktracker.task to which this subtask belongs.
   *
   * @return the ID of the associated epic com.tasktracker.task
   */
  public UUID getEpicTaskId() {
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
        + ", description='"
        + super.getDescription()
        + ", status="
        + super.getStatus()
        + ", epicTaskId="
        + epicTaskId.toString()
        + ", creationDate="
        + super.getCreationDate()
        + ", updateDate="
        + super.getUpdateDate()
        + '}';
  }
}
