package ru.learning.java.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Модель Post
 */
public record Post(
  @JsonProperty("userId")
  Long userId,
  Long id,
  String title,
  String body
) {
}