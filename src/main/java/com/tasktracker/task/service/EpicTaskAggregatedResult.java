package com.tasktracker.task.service;

import com.tasktracker.task.model.enums.TaskStatus;
import java.time.Duration;
import java.time.LocalDateTime;

public record EpicTaskAggregatedResult(
    TaskStatus status, LocalDateTime startTime, Duration duration) {}
