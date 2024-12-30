package com.tasktracker.task.exception;

import java.util.List;

public class ValidationException extends RuntimeException {
  private final List<String> errors;

  public ValidationException(List<String> errors) {
    super("Validation failed: " + String.join(", ", errors));
    this.errors = errors;
  }

  public ValidationException(String error) {
    super(error);
    this.errors = null;
  }

  public List<String> getErrors() {
    return errors;
  }
}
