package ru.learning.java.models;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

/**
 * Модель пользователя (Java Record) с поддержкой Bean Validation.
 */
public record User(
  @NotNull @Positive Long id,
  @NotBlank String name,
  @NotBlank @Email String email,
  @NotBlank String username,
  @Valid Address address,
  @Pattern(regexp = "^[\\d\\s().+x-]+$", message = "phone has invalid format")
  String phone,
  String website,
  @Valid Company company
) {
  @Builder
  public User {
  }

  public record Address(
    @NotBlank String street,
    String suite,
    @NotBlank String city,
    @Pattern(regexp = "^[\\d-]+$", message = "zipcode must contain only digits and '-'")
    String zipcode,
    @Valid Geo geo
  ) {
    @Builder
    public Address {
    }

    public record Geo(String lat, String lng) {
      @Builder
      public Geo {
      }
    }
  }

  public record Company(
    @NotBlank String name,
    String catchPhrase,
    String bs
  ) {
    @Builder
    public Company {
    }
  }
}