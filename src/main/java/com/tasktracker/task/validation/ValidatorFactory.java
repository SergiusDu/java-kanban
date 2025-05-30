package com.tasktracker.task.validation;

import com.tasktracker.task.dto.*;
import com.tasktracker.task.validation.validator.*;
import java.util.Map;
import java.util.Objects;

public final class ValidatorFactory {
  private static final Map<Class<?>, Validator<?>> VALIDATORS;

  static {
    VALIDATORS =
        Map.of(
            RegularTaskCreationDTO.class,
            new RegularTaskCreationValidator(),
            RegularTaskUpdateDTO.class,
            new RegularTaskUpdateValidator(),
            EpicTaskCreationDTO.class,
            new EpicTaskCreationValidator(),
            EpicTaskUpdateDTO.class,
            new EpicTaskUpdateValidator(),
            SubTaskCreationDTO.class,
            new SubTaskCreationValidator(),
            SubTaskUpdateDTO.class,
            new SubTaskUpdateValidator());
  }

  private ValidatorFactory() {}

  public static <T> Validator<T> getValidator(Class<T> clazz) {
    Objects.requireNonNull(clazz, "Class can't be null");
    Validator<?> validator = VALIDATORS.get(clazz);
    if (validator == null) {
      throw new IllegalArgumentException("No validator for this DTO type: " + clazz);
    }
    return (Validator<T>) validator;
  }
}
