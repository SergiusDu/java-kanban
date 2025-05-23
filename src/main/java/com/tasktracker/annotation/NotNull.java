package com.tasktracker.annotation;

import java.lang.annotation.*;

/**
 * Annotation to indicate that the annotated element must not be null. Can be applied to fields,
 * parameters, methods, local variables, record components and type uses.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({
  ElementType.FIELD,
  ElementType.PARAMETER,
  ElementType.METHOD,
  ElementType.LOCAL_VARIABLE,
  ElementType.RECORD_COMPONENT,
  ElementType.TYPE_USE
})
public @interface NotNull {
  /**
   * Message to be used as the error message when the constraint is violated.
   *
   * @return the error message string
   */
  String message() default "Value can't be null";
}
