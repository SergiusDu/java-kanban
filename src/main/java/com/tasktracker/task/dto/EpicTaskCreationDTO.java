package com.tasktracker.task.dto;

import java.time.LocalDateTime;

public record EpicTaskCreationDTO(String title, String description, LocalDateTime startTime)
    implements TaskCreationDTO {}
