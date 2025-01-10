package com.tasktracker.task.validation.validator;

import com.tasktracker.task.dto.RegularTaskUpdateDTO;
import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.validation.CommonValidationUtils;
import com.tasktracker.task.validation.Validator;
import java.util.ArrayList;
import java.util.List;

public class RegularTaskUpdateValidator implements Validator<RegularTaskUpdateDTO> {
  @Override
  public void validate(RegularTaskUpdateDTO dto) throws ValidationException {
    List<String> errors = new ArrayList<>();
    CommonValidationUtils.validateTitle(dto.title(), errors);
    CommonValidationUtils.validateDescription(dto.description(), errors);

    if (!errors.isEmpty()) {
      throw new ValidationException(errors);
    }
  }
}
