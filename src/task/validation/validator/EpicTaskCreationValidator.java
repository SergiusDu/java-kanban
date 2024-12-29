package task.validation.validator;

import java.util.ArrayList;
import java.util.List;
import task.dto.EpicTaskCreationDTO;
import task.exception.ValidationException;
import task.validation.CommonValidationUtils;
import task.validation.Validator;

public class EpicTaskCreationValidator implements Validator<EpicTaskCreationDTO> {
  @Override
  public void validate(EpicTaskCreationDTO dto) throws ValidationException {
    List<String> errors = new ArrayList<>();
    CommonValidationUtils.validateTitle(dto.title(), errors);
    CommonValidationUtils.validateDescription(dto.description(), errors);

    if (!errors.isEmpty()) {
      throw new ValidationException(errors);
    }
  }
}
