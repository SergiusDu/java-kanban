package util;

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
}
