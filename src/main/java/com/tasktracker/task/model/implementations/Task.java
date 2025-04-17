package com.tasktracker.task.model.implementations;

import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.model.enums.TaskStatus;
import com.tasktracker.task.validation.CommonValidationUtils;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;

/**
 * Represents a com.tasktracker.task model that forms the base class for different types of tasks.
 * The class is part of the sealed hierarchy permitting specific implementations: {@link EpicTask},
 * {@link RegularTask}, and {@link SubTask}. This class enforces validation for ID, title,
 * description, and status.
 */
public abstract sealed class Task implements Comparable<Task>
    permits EpicTask, RegularTask, SubTask {
  public static final Comparator<Task> UPDATE_DATE_COMPARATOR =
      Comparator.comparing(Task::getUpdateDate);
  private final int id;
  private final String title;
  private final String description;
  private final TaskStatus status;
  private final LocalDateTime creationDate;
  private final LocalDateTime updateTime;

  /**
   * Constructs a Task instance with specified ID, title, description, status, creation date, and
   * update time. All input parameters are validated to ensure they meet the required criteria.
   *
   * @param id the unique identifier for the task; must be greater than 0
   * @param title the title of the task; cannot be null or shorter than the minimum length
   * @param description the description of the task; cannot be null or shorter than the minimum
   *     length
   * @param status the current status of the task; cannot be null
   * @param creationDate the creation date of the task; cannot be null
   * @param updateDate the last update time of the task; cannot be null
   * @throws ValidationException if any of the value validation checks fail
   */
  protected Task(
      final int id,
      final String title,
      final String description,
      final TaskStatus status,
      final LocalDateTime creationDate,
      final LocalDateTime updateDate)
      throws ValidationException {
    this.id = getValidatedId(id);
    this.title = getValidatedTitle(title);
    this.description = getValidatedDescription(description);
    this.status = getValidatedStatus(status);
    this.creationDate = getValidatedCreationDate(creationDate);
    this.updateTime = getValidatedUpdatedTDate(updateDate);
  }

  /**
   * Validates the provided LocalDateTime to ensure it is not null.
   *
   * @param creationDate the LocalDateTime object to validate
   * @return the validated LocalDateTime object
   */
  private static LocalDateTime getValidatedCreationDate(final LocalDateTime creationDate) {
    Objects.requireNonNull(creationDate, "Date can be null." + creationDate);
    return creationDate;
  }

  /**
   * Validates the com.tasktracker.task ID to ensure it is greater than 0.
   *
   * @param id the com.tasktracker.task ID to validate
   * @return the validated com.tasktracker.task ID
   * @throws ValidationException if the com.tasktracker.task ID is less than or equal to 0
   */
  private static int getValidatedId(final int id) {
    if (id < 0) {
      throw new ValidationException("The Task ID must be greater than 0. Provided ID: " + id);
    }
    return id;
  }

  /**
   * Validates the com.tasktracker.task title to ensure it is not null and meets the minimum length
   * requirement.
   *
   * @param title the com.tasktracker.task title to validate
   * @return the validated com.tasktracker.task title
   * @throws ValidationException if the title is shorter than the minimum required length
   */
  private static String getValidatedTitle(final String title) throws ValidationException {
    Objects.requireNonNull(title, "Title can't be null.");
    if (title.length() < CommonValidationUtils.MIN_TITLE_LENGTH) {
      throw new ValidationException(
          "Title length should be greater than "
              + CommonValidationUtils.MIN_TITLE_LENGTH
              + ". Your title is: \""
              + title
              + "\".");
    }
    return title;
  }

  /**
   * Validates the com.tasktracker.task description to ensure it is not null and meets the minimum
   * length requirement.
   *
   * @param description the com.tasktracker.task description to validate
   * @return the validated com.tasktracker.task description
   * @throws ValidationException if the description is shorter than the minimum required length
   */
  private static String getValidatedDescription(final String description)
      throws ValidationException {
    Objects.requireNonNull(description, "Description can't be null.");
    if (description.length() < CommonValidationUtils.MIN_DESCRIPTION_LENGTH) {
      throw new ValidationException(
          "Description length should be greater than "
              + CommonValidationUtils.MIN_DESCRIPTION_LENGTH
              + ". Your description is: \""
              + description
              + "\".");
    }
    return description;
  }

  /**
   * Validates the com.tasktracker.task status to ensure it is not null.
   *
   * @param status the com.tasktracker.task status to validate
   * @return the validated com.tasktracker.task status
   */
  private static TaskStatus getValidatedStatus(final TaskStatus status) {
    Objects.requireNonNull(status, "Task status can't be null.");
    return status;
  }

  /**
   * Validates the provided update date to ensure it is not null and not earlier than the creation
   * date.
   *
   * @param updateDate the update date to validate
   * @return the validated update date
   * @throws ValidationException if the update date is earlier than the creation date
   */
  private LocalDateTime getValidatedUpdatedTDate(final LocalDateTime updateDate)
      throws ValidationException {
    Objects.requireNonNull(updateDate, "Update date can't be null.");
    if (updateDate.isBefore(this.creationDate)) {
      throw new ValidationException("The update date can't be before creation date.");
    }
    return updateDate;
  }

  /**
   * Retrieves the creation date of the task.
   *
   * @return the creation date of the task as a {@link LocalDateTime} object
   */
  public LocalDateTime getCreationDate() {
    return creationDate;
  }

  /**
   * Retrieves the last update time of the task.
   *
   * @return the last update time of the task as a {@link LocalDateTime} object
   */
  public LocalDateTime getUpdateDate() {
    return updateTime;
  }

  /**
   * Retrieves the ID of the com.tasktracker.task.
   *
   * @return the com.tasktracker.task ID
   */
  public int getId() {
    return id;
  }

  /**
   * Retrieves the title of the com.tasktracker.task.
   *
   * @return the com.tasktracker.task title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Retrieves the description of the com.tasktracker.task.
   *
   * @return the com.tasktracker.task description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Retrieves the current status of the com.tasktracker.task.
   *
   * @return the com.tasktracker.task status
   */
  public TaskStatus getStatus() {
    return status;
  }

  /**
   * Compares this com.tasktracker.task to another object for equality based on the
   * com.tasktracker.task ID.
   *
   * @param object the object to compare
   * @return {@code true} if the other object is a Task and has the same ID; {@code false} otherwise
   */
  @Override
  public boolean equals(final Object object) {
    if (object == null || getClass() != object.getClass()) return false;
    Task task = (Task) object;
    return getId() == task.getId();
  }

  /**
   * Returns the hash code for the com.tasktracker.task, based on its ID.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hashCode(getId());
  }

  /**
   * Returns a string representation of the com.tasktracker.task, including its ID, title,
   * description, status, creation date, and last update time.
   *
   * @return a string representation of the com.tasktracker.task
   */
  @Override
  public String toString() {
    return "Task{"
        + "id="
        + id
        + ", title='"
        + title
        + '\''
        + ", description='"
        + description
        + '\''
        + ", status="
        + status
        + ", creationDate="
        + creationDate
        + ", updateTime="
        + updateTime
        + '}';
  }

  /**
   * Compares this task with another task based on their creation dates.
   *
   * @param o the task to compare to
   * @return a negative integer, zero, or a positive integer as this task's creation date is earlier
   *     than, equal to, or later than the specified task's creation date
   */
  @Override
  public int compareTo(Task o) {
    return this.creationDate.compareTo(o.creationDate);
  }
}
