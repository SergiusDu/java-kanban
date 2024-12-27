package task.model.implementations;

import java.util.List;
import java.util.Set;
import task.model.enums.TaskStatus;

public final class EpicTask extends Task {
  private final List<Integer> subtaskIds;

  public EpicTask(
      int id, String title, String description, TaskStatus status, Set<Integer> subtaskIds) {
    super(id, title, description, status);
    this.subtaskIds = List.copyOf(subtaskIds);
  }

  public Set<Integer> getSubtaskIds() {
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
