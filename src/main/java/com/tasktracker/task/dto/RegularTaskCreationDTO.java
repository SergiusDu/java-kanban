package com.tasktracker.task.dto;

import com.tasktracker.annotation.NotNull;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Data transfer object for creating new regular tasks. Regular tasks are standalone tasks that are
 * not part of an epic.
 */
public record RegularTaskCreationDTO(
    /* The title of the task. Cannot be null. */
    @NotNull String title,
    /* The description of the task. Cannot be null. */
    @NotNull String description,
    /* The start time when the task is scheduled to begin. Optional. */
    LocalDateTime startTime,
    /* The planned duration of the task. Optional. */
    Duration duration)
    implements TaskCreationDTOs {}
