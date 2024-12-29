package task.validation.validator;

import java.util.ArrayList;
import java.util.List;
import task.dto.EpicTaskUpdateDTO;
import task.exception.ValidationException;
import task.validation.CommonValidationUtils;
import task.validation.Validator;

public class EpicTaskUpdateValidator implements Validator<EpicTaskUpdateDTO> {
  @Override
  public void validate(EpicTaskUpdateDTO dto) throws ValidationException {
    List<String> errors = new ArrayList<>();
    CommonValidationUtils.validateTitle(dto.title(), errors);
    CommonValidationUtils.validateDescription(dto.description(), errors);

    if (!errors.isEmpty()) {
      throw new ValidationException(errors);
    }
  }
}
