package com.tasktracker.task.dto;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public record SubTaskCreationDTO(
    String title, String description, UUID epicId, LocalDateTime startTime, Duration duration)
    implements TaskCreationDTO {}
