package ru.learning.java.models;

/**
 * Модель комментария
 */
public record Comment(
  Long postId,
  Long id,
  String name,
  String email,
  String body
) {
}