package com.tasktracker.task.validation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CommonValidationUtilsTest {

  private static final String VALID_LONG_STRING = "ThisIsCertainlyLongEnough";
  private static final String VALID_MIN_LENGTH_STRING = "ExactTenCh"; // Exactly 10 characters
  private static final String INVALID_SHORT_STRING = "Short";
  private static final String NINE_CHAR_STRING = "NineChars"; // Exactly 9 characters

  @Test
  @DisplayName("validateTitle: Should pass for valid titles (>= min length)")
  void validateTitle_ValidTitles() {
    List<String> errors = new ArrayList<>();
    CommonValidationUtils.validateTitle(VALID_LONG_STRING, errors);
    CommonValidationUtils.validateTitle(VALID_MIN_LENGTH_STRING, errors);
    assertTrue(errors.isEmpty(), "Valid titles should not produce errors. Errors: " + errors);
  }

  @Test
  @DisplayName("validateTitle: Should throw NullPointerException for null title")
  void validateTitle_NullTitle_ThrowsNullPointerException() {
    List<String> errors = new ArrayList<>();
    NullPointerException exception =
        assertThrows(
            NullPointerException.class, () -> CommonValidationUtils.validateTitle(null, errors));
    assertEquals("Title can't be null.", exception.getMessage());
  }

  @Test
  @DisplayName("validateTitle: Should fail for titles shorter than minimum length")
  void validateTitle_ShortTitles_AddsError() {
    List<String> errors = new ArrayList<>();
    CommonValidationUtils.validateTitle(INVALID_SHORT_STRING, errors);
    assertEquals(
        1, errors.size(), "Short title should add one validation error. Errors: " + errors);
    assertTrue(
        errors
            .get(0)
            .contains("Title length should be at least " + CommonValidationUtils.MIN_TITLE_LENGTH),
        "Error message for short title is incorrect. Got: " + errors.get(0));

    errors.clear();
    CommonValidationUtils.validateTitle(NINE_CHAR_STRING, errors);
    assertEquals(
        1,
        errors.size(),
        "Nine-character title should add one validation error. Errors: " + errors);
    assertTrue(
        errors
            .get(0)
            .contains("Title length should be at least " + CommonValidationUtils.MIN_TITLE_LENGTH),
        "Error message for nine-character title is incorrect. Got: " + errors.get(0));
  }

  @Test
  @DisplayName("validateDescription: Should pass for valid descriptions (>= min length)")
  void validateDescription_ValidDescriptions() {
    List<String> errors = new ArrayList<>();
    CommonValidationUtils.validateDescription(VALID_LONG_STRING, errors);
    CommonValidationUtils.validateDescription(VALID_MIN_LENGTH_STRING, errors);
    assertTrue(errors.isEmpty(), "Valid descriptions should not produce errors. Errors: " + errors);
  }

  @Test
  @DisplayName("validateDescription: Should throw NullPointerException for null description")
  void validateDescription_NullDescription_ThrowsNullPointerException() {
    List<String> errors = new ArrayList<>();
    NullPointerException exception =
        assertThrows(
            NullPointerException.class,
            () -> CommonValidationUtils.validateDescription(null, errors));
    assertEquals("Description can't be null.", exception.getMessage());
  }

  @Test
  @DisplayName("validateDescription: Should fail for descriptions shorter than minimum length")
  void validateDescription_ShortDescriptions_AddsError() {
    List<String> errors = new ArrayList<>();
    CommonValidationUtils.validateDescription(INVALID_SHORT_STRING, errors);
    assertEquals(
        1, errors.size(), "Short description should add one validation error. Errors: " + errors);
    assertTrue(
        errors
            .get(0)
            .contains(
                "Description length should be at least "
                    + CommonValidationUtils.MIN_DESCRIPTION_LENGTH),
        "Error message for short description is incorrect. Got: " + errors.get(0));

    errors.clear();
    CommonValidationUtils.validateDescription(NINE_CHAR_STRING, errors);
    assertEquals(
        1,
        errors.size(),
        "Nine-character description should add one validation error. Errors: " + errors);
    assertTrue(
        errors
            .get(0)
            .contains(
                "Description length should be at least "
                    + CommonValidationUtils.MIN_DESCRIPTION_LENGTH),
        "Error message for nine-character description is incorrect. Got: " + errors.get(0));
  }

  @Test
  @DisplayName("validateUuid: Should pass for a valid UUID")
  void validateUuid_ValidUuid_NoError() {
    List<String> errors = new ArrayList<>();
    UUID validUuid = UUID.randomUUID();
    CommonValidationUtils.validateUuid(validUuid, errors);
    assertTrue(errors.isEmpty(), "Valid UUID should not produce errors. Errors: " + errors);
  }

  @Test
  @DisplayName("validateUuid: Should throw NullPointerException for null UUID")
  void validateUuid_NullUuid_ThrowsNullPointerException() {
    List<String> errors = new ArrayList<>();
    NullPointerException exception =
        assertThrows(
            NullPointerException.class, () -> CommonValidationUtils.validateUuid(null, errors));
    assertEquals("Epic task id cannot be null", exception.getMessage());
  }

  @Test
  @DisplayName(
      "validateUuid: String length check for UUID (usually redundant for valid UUID objects)")
  void validateUuid_LengthCheck() {
    List<String> errors = new ArrayList<>();
    UUID aValidUuid = UUID.randomUUID();
    CommonValidationUtils.validateUuid(aValidUuid, errors);
    assertTrue(
        errors.stream().noneMatch(error -> error.contains("Invalid epic task UUID format")),
        "A standard UUID should pass the length check without format errors. Errors: " + errors);
    assertTrue(errors.isEmpty(), "A standard UUID should not produce any validation errors.");
  }
}
