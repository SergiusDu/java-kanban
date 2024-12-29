package task.dto;

import task.model.enums.TaskStatus;

public record RegularTaskUpdateDTO(int id, String title, String description, TaskStatus status) {}
