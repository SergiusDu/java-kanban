package com.tasktracker.unit;

import com.tasktracker.util.TypeSafeCaster;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TypeSafeCasterTest {

  @Test
  void castSafely_success() {
    Object strObj = "Hello";
    String result = TypeSafeCaster.castSafely(strObj, String.class);
    Assertions.assertEquals("Hello", result);
  }

  @Test
  void castSafely_classCastException() {
    Object intObj = 123;
    Assertions.assertThrows(
        ClassCastException.class, () -> TypeSafeCaster.castSafely(intObj, String.class));
  }

  @Test
  void castSafelyOptional_success() {
    Optional<?> optional = Optional.of("Hello");
    Optional<String> result = TypeSafeCaster.castSafely(optional, String.class);
    Assertions.assertTrue(result.isPresent());
    Assertions.assertEquals("Hello", result.get());
  }

  @Test
  void castSafelyOptional_empty() {
    Optional<?> emptyOptional = Optional.empty();
    Optional<String> result = TypeSafeCaster.castSafely(emptyOptional, String.class);
    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  void castSafelyOrThrow_success() {
    Optional<?> optional = Optional.of(42);
    Integer result = TypeSafeCaster.castSafelyOrThrow(optional, Integer.class);
    Assertions.assertEquals(42, result);
  }

  @Test
  void castSafelyOrThrow_throws() {
    Optional<?> optional = Optional.of("NotInteger");
    Assertions.assertThrows(
        ClassCastException.class, () -> TypeSafeCaster.castSafelyOrThrow(optional, Integer.class));
  }
}
