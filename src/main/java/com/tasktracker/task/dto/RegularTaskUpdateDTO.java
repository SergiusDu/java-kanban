package com.tasktracker.task.dto;

import com.tasktracker.task.model.enums.TaskStatus;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public record RegularTaskUpdateDTO(
    UUID id,
    String title,
    String description,
    TaskStatus status,
    LocalDateTime startTime,
    Duration duration)
    implements TaskModificationDto {}
