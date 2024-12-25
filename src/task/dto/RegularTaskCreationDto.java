package task.dto;

import task.model.TaskStatus;

public record RegularTaskCreationDto(String title, String description) {
  public static TaskStatus status = TaskStatus.NEW;
}
