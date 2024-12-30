package com.tasktracker.task.validation;

import com.tasktracker.task.exception.ValidationException;

public interface Validator<T> {
  void validate(T dto) throws ValidationException;
}
