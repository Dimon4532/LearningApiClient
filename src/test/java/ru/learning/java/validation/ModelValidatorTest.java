package ru.learning.java.validation;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.learning.java.models.CreateUserRequest;
import ru.learning.java.models.User;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Feature("Bean Validation (JSR-380)")
@DisplayName("ModelValidator — Jakarta Bean Validation")
class ModelValidatorTest {

  // ── CreateUserRequest ─────────────────────────────────────────────────────

  @Test
  @Story("Валидный объект")
  @DisplayName("Валидный CreateUserRequest проходит проверку")
  void testValidCreateUserRequest() {
    CreateUserRequest request = CreateUserRequest.builder()
                                                 .name("John Doe")
                                                 .email("john@example.com")
                                                 .username("john_doe")
                                                 .build();

    Set<ConstraintViolation<CreateUserRequest>> violations = ModelValidator.validate(request);
    assertThat(violations).isEmpty();
  }

  @Test
  @Story("Невалидный email")
  @DisplayName("Невалидный email отлавливается @Email")
  void testInvalidEmailDetected() {
    CreateUserRequest request = CreateUserRequest.builder()
                                                 .name("John")
                                                 .email("not-an-email")
                                                 .username("john_doe")
                                                 .build();

    Set<String> messages = ModelValidator.validateAndDescribe(request);

    assertThat(messages).anyMatch(m -> m.startsWith("email:"));
  }

  @Test
  @Story("Пустые обязательные поля")
  @DisplayName("Пустое имя и username отлавливаются @NotBlank")
  void testBlankFieldsDetected() {
    CreateUserRequest request = CreateUserRequest.builder()
                                                 .name("")
                                                 .email("john@example.com")
                                                 .username("")
                                                 .build();

    Set<String> messages = ModelValidator.validateAndDescribe(request);

    assertThat(messages).anyMatch(m -> m.startsWith("name:"));
    assertThat(messages).anyMatch(m -> m.startsWith("username:"));
  }

  @Test
  @Story("Pattern")
  @DisplayName("Username с пробелом не соответствует @Pattern")
  void testUsernamePatternViolation() {
    CreateUserRequest request = CreateUserRequest.builder()
                                                 .name("John")
                                                 .email("john@example.com")
                                                 .username("john doe with spaces")
                                                 .build();

    Set<String> messages = ModelValidator.validateAndDescribe(request);
    assertThat(messages).anyMatch(m -> m.contains("username"));
  }

  @Test
  @Story("requireValid")
  @DisplayName("requireValid бросает исключение для невалидной модели")
  void testRequireValidThrows() {
    CreateUserRequest invalid = CreateUserRequest.builder()
                                                 .name("")
                                                 .email("bad")
                                                 .username("")
                                                 .build();

    assertThatThrownBy(() -> ModelValidator.requireValid(invalid))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Validation failed");
  }

  @Test
  @Story("requireValid")
  @DisplayName("requireValid возвращает объект, если он валиден")
  void testRequireValidReturnsObject() {
    CreateUserRequest valid = CreateUserRequest.builder()
                                               .name("John")
                                               .email("john@example.com")
                                               .username("john")
                                               .build();

    CreateUserRequest result = ModelValidator.requireValid(valid);
    assertThat(result).isSameAs(valid);
  }

  // ── User (record) ─────────────────────────────────────────────────────────

  @Test
  @Story("Каскадная валидация")
  @DisplayName("Каскадная валидация: @Valid пробивается до Address и Company")
  void testCascadeValidation() {
    User user = User.builder()
                    .id(1L)
                    .name("John")
                    .email("john@example.com")
                    .username("john")
                    .address(User.Address.builder()
                                         .street("")              // ← нарушение @NotBlank
                                         .city("New York")
                                         .zipcode("10001")
                                         .build())
                    .company(User.Company.builder()
                                         .name("")                // ← нарушение @NotBlank
                                         .build())
                    .build();

    Set<String> messages = ModelValidator.validateAndDescribe(user);

    assertThat(messages).anyMatch(m -> m.startsWith("address.street:"));
    assertThat(messages).anyMatch(m -> m.startsWith("company.name:"));
  }

  @Test
  @Story("Положительный id")
  @DisplayName("@Positive отлавливает отрицательный id")
  void testNegativeIdDetected() {
    User user = User.builder()
                    .id(-1L)
                    .name("John")
                    .email("john@example.com")
                    .username("john")
                    .build();

    Set<String> messages = ModelValidator.validateAndDescribe(user);
    assertThat(messages).anyMatch(m -> m.startsWith("id:"));
  }
}