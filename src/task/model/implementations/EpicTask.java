package task.model.implementations;

import java.util.Set;
import task.exception.ValidationException;
import task.model.enums.TaskStatus;

public final class EpicTask extends Task {
  private final Set<Integer> subtaskIds;

  public EpicTask(
      int id, String title, String description, TaskStatus status, Set<Integer> subtaskIds) {
    super(id, title, description, status);
    this.subtaskIds = getValidatedSubTaskIds(subtaskIds);
  }

  public Set<Integer> getSubtaskIds() {
    return Set.copyOf(subtaskIds);
  }

  private Set<Integer> getValidatedSubTaskIds(final Set<Integer> subtaskIds) {
    if (subtaskIds.isEmpty()) return subtaskIds;
    boolean isValidated = subtaskIds.stream().anyMatch(subtaskId -> subtaskId < 0);
    if (isValidated) {
      throw new ValidationException("Subtask IDs cannot contain negative values.");
    }
    return Set.copyOf(subtaskIds);
  }

  @Override
  public String toString() {
    return "EpicTask{"
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
        + ", subtaskIds="
        + subtaskIds.toString()
        + '}';
  }
}
