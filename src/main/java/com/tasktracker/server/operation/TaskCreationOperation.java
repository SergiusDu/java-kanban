package com.tasktracker.server.operation;

import com.tasktracker.task.dto.TaskCreationDTOs;
import com.tasktracker.task.exception.OverlapException;
import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.model.implementations.Task;
import com.tasktracker.task.store.exception.TaskNotFoundException;

@FunctionalInterface
public interface TaskCreationOperation<T extends TaskCreationDTOs, R extends Task> {
  R apply(T t) throws ValidationException, TaskNotFoundException, OverlapException;
}
