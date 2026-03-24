package ru.learning.java.models;

import lombok.Builder;

/**
 * Модель пользователя (Java Record)
 */
public record User(
  Long id,
  String name,
  String email,
  String username
) {
  @Builder
  public User {}
}