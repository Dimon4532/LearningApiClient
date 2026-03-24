package ru.learning.java.models;

import lombok.Builder;

/**
 * Модель пользователя (Java Record)
 */
public record User(
  Long id,
  String name,
  String email,
  String username,
  Address address,
  String phone,
  String website,
  Company company
) {
  @Builder
  public User {
  }

  public record Address(String street, String suite, String city, String zipcode, Geo geo) {
    @Builder
    public Address {
    }

    public record Geo(String lat, String lng) {
      @Builder
      public Geo {
      }
    }
  }

  public record Company(String name, String catchPhrase, String bs) {
    @Builder
    public Company {
    }
  }
}