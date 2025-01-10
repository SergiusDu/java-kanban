package com.tasktracker.task.validation;

import static org.junit.jupiter.api.Assertions.*;

import com.tasktracker.task.dto.*;
import com.tasktracker.task.validation.validator.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ValidatorFactoryTest {

  @Test
  @DisplayName("getValidator should return RegularTaskCreationValidator for RegularTaskCreationDTO")
  void getValidator_ReturnsRegularTaskCreationValidator() {
    Validator<RegularTaskCreationDTO> validator =
        ValidatorFactory.getValidator(RegularTaskCreationDTO.class);
    assertNotNull(validator);
    assertInstanceOf(RegularTaskCreationValidator.class, validator);
  }

  @Test
  @DisplayName("getValidator should return RegularTaskUpdateValidator for RegularTaskUpdateDTO")
  void getValidator_ReturnsRegularTaskUpdateValidator() {
    Validator<RegularTaskUpdateDTO> validator =
        ValidatorFactory.getValidator(RegularTaskUpdateDTO.class);
    assertNotNull(validator);
    assertInstanceOf(RegularTaskUpdateValidator.class, validator);
  }

  @Test
  @DisplayName("getValidator should return EpicTaskCreationValidator for EpicTaskCreationDTO")
  void getValidator_ReturnsEpicTaskCreationValidator() {
    Validator<EpicTaskCreationDTO> validator =
        ValidatorFactory.getValidator(EpicTaskCreationDTO.class);
    assertNotNull(validator);
    assertInstanceOf(EpicTaskCreationValidator.class, validator);
  }

  @Test
  @DisplayName("getValidator should return EpicTaskUpdateValidator for EpicTaskUpdateDTO")
  void getValidator_ReturnsEpicTaskUpdateValidator() {
    Validator<EpicTaskUpdateDTO> validator = ValidatorFactory.getValidator(EpicTaskUpdateDTO.class);
    assertNotNull(validator);
    assertInstanceOf(EpicTaskUpdateValidator.class, validator);
  }

  @Test
  @DisplayName("getValidator should return SubTaskCreationValidator for SubTaskCreationDTO")
  void getValidator_ReturnsSubTaskCreationValidator() {
    Validator<SubTaskCreationDTO> validator =
        ValidatorFactory.getValidator(SubTaskCreationDTO.class);
    assertNotNull(validator);
    assertInstanceOf(SubTaskCreationValidator.class, validator);
  }

  @Test
  @DisplayName("getValidator should return SubTaskUpdateValidator for SubTaskUpdateDTO")
  void getValidator_ReturnsSubTaskUpdateValidator() {
    Validator<SubTaskUpdateDTO> validator = ValidatorFactory.getValidator(SubTaskUpdateDTO.class);
    assertNotNull(validator);
    assertInstanceOf(SubTaskUpdateValidator.class, validator);
  }

  @Test
  @DisplayName("getValidator should throw IllegalArgumentException for unknown DTO type")
  void getValidator_ThrowsExceptionForUnknownDTO() {
    class UnknownDTO {}
    assertThrows(
        IllegalArgumentException.class,
        () -> ValidatorFactory.getValidator(UnknownDTO.class),
        "Should throw IllegalArgumentException when no validator is found for the DTO type");
  }
}
