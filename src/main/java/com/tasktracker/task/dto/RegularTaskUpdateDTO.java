package com.tasktracker.task.dto;

import com.tasktracker.annotation.NotNull;
import com.tasktracker.task.model.enums.TaskStatus;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data transfer object for updating existing regular tasks. Contains all the fields necessary to
 * modify a regular task's properties.
 */
public record RegularTaskUpdateDTO(
    /* The unique identifier of the task to update. Cannot be null. */
    @NotNull UUID id,
    /* The new title for the task. Cannot be null. */
    @NotNull String title,
    /* The new description for the task. Cannot be null. */
    @NotNull String description,
    /* The new status for the task. Cannot be null. */
    @NotNull TaskStatus status,
    /* The new start time when the task is scheduled to begin. Optional. */
    LocalDateTime startTime,
    /* The new planned duration of the task. Optional. */
    Duration duration)
    implements TaskUpdateDTOs {}
