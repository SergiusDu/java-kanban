package util;

import java.util.Optional;

public class TypeSafeCaster {

  private TypeSafeCaster() {}

  /**
   * Casts an object to the specified class type safely.
   *
   * @param obj the object to cast
   * @param clazz the class type to cast to
   * @return the casted object of type T
   * @throws ClassCastException if the object cannot be casted to the specified type
   */
  public static <T> T castSafely(Object obj, Class<T> clazz) {
    if (clazz.isInstance(obj)) {
      return clazz.cast(obj);
    } else {
      throw new ClassCastException(
          "Expected object to be an instance of "
              + clazz.getSimpleName()
              + " but found "
              + (obj != null ? obj.getClass().getSimpleName() : "null"));
    }
  }

  /**
   * Safely casts the content of an Optional to the specified class type.
   *
   * @param optional the Optional containing the object to cast
   * @param clazz the class type to cast to
   * @return an Optional containing the casted object if present and of the correct type
   */
  public static <T> Optional<T> castSafely(Optional<?> optional, Class<T> clazz) {
    return optional.filter(clazz::isInstance).map(clazz::cast);
  }

  /**
   * Casts the content of an Optional to the specified class type or throws a ClassCastException.
   *
   * @param optional the Optional containing the object to cast
   * @param clazz the class type to cast to
   * @return the casted object of type T
   * @throws ClassCastException if the object cannot be casted to the specified type or is not
   *     present
   */
  public static <T> T castSafelyOrThrow(Optional<?> optional, Class<T> clazz) {
    return optional
        .map(obj -> castSafely(obj, clazz))
        .orElseThrow(
            () ->
                new ClassCastException(
                    "Expected an instance of "
                        + clazz.getSimpleName()
                        + " but found empty Optional."));
  }
}
