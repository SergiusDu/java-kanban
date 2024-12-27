package task.model.implementations;

import java.util.Objects;
import task.exception.ValidationException;
import task.model.enums.TaskStatus;

public abstract sealed class Task permits EpicTask, RegularTask, SubTask {
  private static final int MIN_TITLE_LENGTH = 10;
  private static final int MIN_DESCRIPTION_LENGTH = 10;
  private final int id;
  private final String title;
  private final String description;
  private final TaskStatus status;

  protected Task(int id, String title, String description, TaskStatus status) {
    this.id = getValidatedId(id);
    this.title = getValidatedTitle(title);
    this.description = getValidatedDescription(description);
    this.status = getValidatedStatus(status);
  }

  private static int getValidatedId(int id) {
    if (id < 0) {
      throw new ValidationException("The Task ID must be greater than 0. Provided ID: " + id);
    }
    return id;
  }

  private static String getValidatedTitle(String title) {
    Objects.requireNonNull(title, "Title can't be null.");
    if (title.length() < MIN_TITLE_LENGTH) {
      throw new ValidationException(
          "Title length should be greater than "
              + MIN_TITLE_LENGTH
              + ". Your title is: \""
              + title
              + "\".");
    }
    return title;
  }

  private static String getValidatedDescription(String description) {
    Objects.requireNonNull(description, "Description can't be null.");
    if (description.length() < MIN_DESCRIPTION_LENGTH) {
      throw new ValidationException(
          "Description length should be greater than "
              + MIN_DESCRIPTION_LENGTH
              + ". Your description is: \""
              + description
              + "\".");
    }
    return description;
  }

  private static TaskStatus getValidatedStatus(TaskStatus status) {
    Objects.requireNonNull(status, "Task status can't be null.");
    return status;
  }

  public int getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public TaskStatus getStatus() {
    return status;
  }

  @Override
  public boolean equals(Object object) {
    if (object == null || getClass() != object.getClass()) return false;
    Task task = (Task) object;
    return getId() == task.getId();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getId());
  }

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
        + '}';
  }
}
