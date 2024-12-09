package task.model;

import java.util.List;
import java.util.Objects;

public final class EpicTask extends Task {
    private final List<Integer> subtaskIds;

    public EpicTask(int id,
                       String title,
                       String description,
                       TaskStatus status,
                       List<Integer> subtaskIds) {
        super(id, title, description, status);
        this.subtaskIds = List.copyOf(getValidatedSubtaskIds(subtaskIds));
    }

    public List<Integer> getSubtaskIds() {
        return List.copyOf(subtaskIds);
    }

    private List<Integer> getValidatedSubtaskIds(List<Integer> subtaskIds) {
        Objects.requireNonNull(subtaskIds, "Subtasks IDs can't be null");
        if(subtaskIds.isEmpty()) {
            throw new IllegalArgumentException("EpicTask must have at least one subtask id");
        }
        subtaskIds.forEach(taskId -> {
            if(taskId < 0) {
                throw new IllegalArgumentException("All tasks IDs should be positive");
            }
        });
        return subtaskIds;
    }

    @Override
    public String toString() {
        return "EpicTask{" +
                "id=" + super.getId() +
                ", title='" + super.getTitle() + '\'' +
                ", description='" + super.getDescription() + '\'' +
                ", status=" + super.getStatus()  +
                ", subtaskIds=" + subtaskIds.toString() +
                '}';
    }
}
