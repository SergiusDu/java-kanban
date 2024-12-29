package task.validation.validator;

import java.util.ArrayList;
import java.util.List;
import task.dto.SubTaskUpdateDTO;
import task.exception.ValidationException;
import task.validation.CommonValidationUtils;
import task.validation.Validator;

public class SubTaskUpdateValidator implements Validator<SubTaskUpdateDTO> {
  @Override
  public void validate(SubTaskUpdateDTO dto) throws ValidationException {
    List<String> errors = new ArrayList<>();
    CommonValidationUtils.validateTitle(dto.title(), errors);
    CommonValidationUtils.validateDescription(dto.description(), errors);
    CommonValidationUtils.validateEpicId(dto.epicId(), errors);

    if (!errors.isEmpty()) {
      throw new ValidationException(errors);
    }
  }
}
