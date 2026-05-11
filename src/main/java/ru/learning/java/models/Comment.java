package ru.learning.java.models;

import lombok.Builder;

/**
 * Модель комментария
 */
@Builder
public record Comment(
  Long postId,
  Long id,
  String name,
  String email,
  String body
) {
}