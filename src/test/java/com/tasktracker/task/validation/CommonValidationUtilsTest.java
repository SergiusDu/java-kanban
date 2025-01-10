package com.tasktracker.task.validation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive JUnit5 tests for {@link CommonValidationUtils}. Ensures that title, description,
 * and epicId validations are correctly enforced.
 */
class CommonValidationUtilsTest {

  /** Tests the validateTitle method with valid, invalid, and edge case titles. */
  @Test
  @DisplayName("validateTitle: Should pass validation for valid titles")
  void validateTitle_ValidTitles() {
    List<String> errors = new ArrayList<>();

    String validTitle1 = "ValidTaskTitle"; // > 10 characters
    String validTitle2 = "AnotherValidTitle"; // > 10 characters
    String validTitle3 = "ExactTenChr"; // 10 characters

    // Assuming the requirement is strictly > 10, "ExactTenChr" has 10 and should fail
    CommonValidationUtils.validateTitle(validTitle1, errors);
    CommonValidationUtils.validateTitle(validTitle2, errors);
    CommonValidationUtils.validateTitle(validTitle3, errors); // 10 characters

    assertTrue(errors.isEmpty(), "Valid titles should not produce any validation errors");
  }

  @Test
  @DisplayName("validateTitle: Should fail validation for null titles")
  void validateTitle_NullTitle() {
    List<String> errors = new ArrayList<>();

    NullPointerException exception =
        assertThrows(
            NullPointerException.class,
            () -> CommonValidationUtils.validateTitle(null, errors),
            "validateTitle should throw NullPointerException when title is null");

    assertEquals(
        "Title can't be null.", exception.getMessage(), "Exception message should match expected");
  }

  @Test
  @DisplayName("validateTitle: Should fail validation for titles shorter than minimum length")
  void validateTitle_ShortTitles() {
    List<String> errors = new ArrayList<>();

    String shortTitle1 = "Short"; // < 10 characters
    String shortTitle2 = "TenChars!"; // Exactly 9 characters
    String shortTitle3 = "NineChrs!"; // 9 characters

    CommonValidationUtils.validateTitle(shortTitle1, errors);
    CommonValidationUtils.validateTitle(shortTitle2, errors);
    CommonValidationUtils.validateTitle(shortTitle3, errors);

    assertEquals(3, errors.size(), "Each short title should add one validation error");

    assertTrue(
        errors.get(0).contains("Title length should be at least"),
        "First error message should relate to title length");
    assertTrue(
        errors.get(1).contains("Title length should be at least"),
        "Second error message should relate to title length");
    assertTrue(
        errors.get(2).contains("Title length should be at least"),
        "Third error message should relate to title length");
  }

  @Test
  @DisplayName("validateTitle: Should pass validation for titles exactly at minimum length")
  void validateTitle_ExactMinimumLength() {
    List<String> errors = new ArrayList<>();

    String exactLengthTitle = "ExactTenCh"; // 10 characters

    CommonValidationUtils.validateTitle(exactLengthTitle, errors);
    assertTrue(errors.isEmpty(), "Title with exactly 10 characters should pass validation");
  }

  @Test
  @DisplayName("validateDescription: Should pass validation for valid descriptions")
  void validateDescription_ValidDescriptions() {
    List<String> errors = new ArrayList<>();

    String validDesc1 = "ValidTaskDescr"; // > 10 characters
    String validDesc2 = "AnotherValidDesc"; // > 10 characters

    CommonValidationUtils.validateDescription(validDesc1, errors);
    CommonValidationUtils.validateDescription(validDesc2, errors);

    assertTrue(errors.isEmpty(), "Valid descriptions should not produce any validation errors");
  }

  @Test
  @DisplayName("validateDescription: Should fail validation for null descriptions")
  void validateDescription_NullDescription() {
    List<String> errors = new ArrayList<>();

    NullPointerException exception =
        assertThrows(
            NullPointerException.class,
            () -> CommonValidationUtils.validateDescription(null, errors),
            "validateDescription should throw NullPointerException when description is null");

    assertEquals(
        "Description can't be null.",
        exception.getMessage(),
        "Exception message should match expected");
  }

  @Test
  @DisplayName(
      "validateDescription: Should fail validation for descriptions shorter than minimum length")
  void validateDescription_ShortDescriptions() {
    List<String> errors = new ArrayList<>();

    String shortDesc1 = "Short"; // < 10 characters
    String shortDesc2 = "TenChars!"; // Exactly 9 characters
    String shortDesc3 = "NineChrs!"; // 9 characters

    CommonValidationUtils.validateDescription(shortDesc1, errors);
    CommonValidationUtils.validateDescription(shortDesc2, errors);
    CommonValidationUtils.validateDescription(shortDesc3, errors);

    assertEquals(3, errors.size(), "Each short description should add one validation error");

    assertTrue(
        errors.get(0).contains("Description length should be at least"),
        "First error message should relate to description length");
    assertTrue(
        errors.get(1).contains("Description length should be at least"),
        "Second error message should relate to description length");
    assertTrue(
        errors.get(2).contains("Description length should be at least"),
        "Third error message should relate to description length");
  }

  @Test
  @DisplayName(
      "validateDescription: Should pass validation for descriptions exactly at minimum length")
  void validateDescription_ExactMinimumLength() {
    List<String> errors = new ArrayList<>();

    String exactLengthDesc = "ExactTenD1"; // 10 characters

    CommonValidationUtils.validateDescription(exactLengthDesc, errors);

    // Assuming >=10 is valid
    assertTrue(errors.isEmpty(), "Description with exactly 10 characters should pass validation");
  }

  @Test
  @DisplayName("validateEpicId: Should pass validation for non-negative epic IDs")
  void validateEpicId_ValidIds() {
    List<String> errors = new ArrayList<>();

    int validId1 = 0;
    int validId2 = 10;
    int validId3 = Integer.MAX_VALUE;

    CommonValidationUtils.validateEpicId(validId1, errors);
    CommonValidationUtils.validateEpicId(validId2, errors);
    CommonValidationUtils.validateEpicId(validId3, errors);

    assertTrue(errors.isEmpty(), "Non-negative epic IDs should not produce any validation errors");
  }

  @Test
  @DisplayName("validateEpicId: Should fail validation for negative epic IDs")
  void validateEpicId_InvalidIds() {
    List<String> errors = new ArrayList<>();

    int invalidId1 = -1;
    int invalidId2 = -100;
    int invalidId3 = Integer.MIN_VALUE;

    CommonValidationUtils.validateEpicId(invalidId1, errors);
    CommonValidationUtils.validateEpicId(invalidId2, errors);
    CommonValidationUtils.validateEpicId(invalidId3, errors);

    assertEquals(3, errors.size(), "Each negative epic ID should add one validation error");

    assertTrue(
        errors.get(0).contains("Epic com.tasktracker.task ID should be positive."),
        "First error message should relate to epic ID positivity");
    assertTrue(
        errors.get(1).contains("Epic com.tasktracker.task ID should be positive."),
        "Second error message should relate to epic ID positivity");
    assertTrue(
        errors.get(2).contains("Epic com.tasktracker.task ID should be positive."),
        "Third error message should relate to epic ID positivity");
  }

  @Test
  @DisplayName("validateEpicId: Should pass validation for epic ID zero")
  void validateEpicId_EpicIdZero() {
    List<String> errors = new ArrayList<>();

    int epicId = 0;

    CommonValidationUtils.validateEpicId(epicId, errors);

    assertTrue(errors.isEmpty(), "Epic ID zero should pass validation");
  }
}
