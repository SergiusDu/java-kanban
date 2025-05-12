package com.tasktracker.task.store.exception;

public class TaskNotFoundException extends Exception {
  public TaskNotFoundException(String message) {
    super(message);
  }
}
