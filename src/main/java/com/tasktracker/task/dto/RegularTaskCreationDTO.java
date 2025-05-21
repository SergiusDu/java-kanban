package com.tasktracker.task.dto;

import java.time.Duration;
import java.time.LocalDateTime;

public record RegularTaskCreationDTO(
    String title, String description, LocalDateTime startTime, Duration duration)
    implements TaskModificationDto {}
