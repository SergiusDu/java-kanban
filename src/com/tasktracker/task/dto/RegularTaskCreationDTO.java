package com.tasktracker.task.dto;

public record RegularTaskCreationDTO(String title, String description) implements TaskCreationDTO {}
