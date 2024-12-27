package task.validation;

import task.exception.ValidationException;

public interface Validator<T> {
  void validate(T dto) throws ValidationException;
}
