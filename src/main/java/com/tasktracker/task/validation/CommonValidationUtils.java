package com.tasktracker.task.validation;

import java.util.List;
import java.util.Objects;

public final class CommonValidationUtils {
  public static final int MIN_TITLE_LENGTH = 10;
  public static final int MIN_DESCRIPTION_LENGTH = 10;

  private CommonValidationUtils() {}

  public static void validateTitle(String title, List<String> errors) {
    Objects.requireNonNull(title, "Title can't be null.");
    if (title.length() < MIN_TITLE_LENGTH) {
      errors.add(
          "Title length should be at least "
              + MIN_TITLE_LENGTH
              + ". Your title: \""
              + title
              + "\".");
    }
  }

  public static void validateDescription(String description, List<String> errors) {
    Objects.requireNonNull(description, "Description can't be null.");
    if (description.length() < MIN_DESCRIPTION_LENGTH) {
      errors.add(
          "Description length should be at least "
              + MIN_DESCRIPTION_LENGTH
              + ". Your description: \""
              + description
              + "\".");
    }
  }

  public static void validateEpicId(int epicId, List<String> errors) {
    if (epicId < 0) {
      errors.add("Epic com.tasktracker.task ID should be positive.");
    }
  }
}
