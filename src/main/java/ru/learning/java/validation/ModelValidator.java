package ru.learning.java.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Утилита-обёртка над Jakarta Bean Validation.
 * <p>
 * ValidatorFactory — тяжёлый объект, поэтому создаём его один раз
 * и переиспользуем для всех вызовов.
 */
public final class ModelValidator {

  private static final ValidatorFactory FACTORY =
    Validation.buildDefaultValidatorFactory();

  private static final Validator VALIDATOR = FACTORY.getValidator();

  private ModelValidator() {
  }

  /**
   * Валидирует объект и возвращает множество нарушений.
   * Пустое множество означает, что объект валиден.
   */
  public static <T> Set<ConstraintViolation<T>> validate(T target) {
    return VALIDATOR.validate(target);
  }

  /**
   * Возвращает читаемые сообщения нарушений в формате "field: message".
   */
  public static <T> Set<String> validateAndDescribe(T target) {
    return VALIDATOR.validate(target).stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.toSet());
  }

  /**
   * Бросает {@link IllegalArgumentException}, если объект не валиден.
   * Удобно использовать перед отправкой запроса на сервер.
   */
  public static <T> T requireValid(T target) {
    Set<ConstraintViolation<T>> violations = VALIDATOR.validate(target);
    if (!violations.isEmpty()) {
      String details = violations.stream()
                                 .map(v -> v.getPropertyPath() + " " + v.getMessage())
                                 .collect(Collectors.joining("; "));
      throw new IllegalArgumentException("Validation failed: " + details);
    }
    return target;
  }
}