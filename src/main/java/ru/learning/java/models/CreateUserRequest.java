package ru.learning.java.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Модель для создания пользователя.
 * Содержит ограничения Bean Validation для проверки данных
 * перед отправкой на сервер.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

  @NotBlank(message = "name must not be blank")
  @Size(min = 2, max = 100, message = "name length must be between {min} and {max}")
  private String name;

  @NotBlank(message = "email must not be blank")
  @Email(message = "email must be a valid email address")
  private String email;

  @NotBlank(message = "username must not be blank")
  @Pattern(regexp = "^[a-zA-Z0-9._-]{3,30}$",
    message = "username must contain only letters, digits, '.', '_', '-' (3..30 chars)")
  private String username;
}