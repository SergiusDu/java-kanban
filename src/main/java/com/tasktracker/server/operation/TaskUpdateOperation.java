package com.tasktracker.server.operation;

import com.tasktracker.task.dto.TaskUpdateDTOs;
import com.tasktracker.task.exception.OverlapException;
import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.model.implementations.Task;
import com.tasktracker.task.store.exception.TaskNotFoundException;

@FunctionalInterface
public interface TaskUpdateOperation<T extends TaskUpdateDTOs, R extends Task> {
  R apply(T t) throws ValidationException, TaskNotFoundException, OverlapException;
}
