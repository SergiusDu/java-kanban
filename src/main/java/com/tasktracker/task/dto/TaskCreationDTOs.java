package com.tasktracker.task.dto;

public sealed interface TaskCreationDTOs
    permits RegularTaskCreationDTO, SubTaskCreationDTO, EpicTaskCreationDTO {}
