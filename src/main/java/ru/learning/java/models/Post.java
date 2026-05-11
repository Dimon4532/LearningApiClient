package ru.learning.java.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * Модель Post
 */
@Builder
public record Post(
  @JsonProperty("userId")
  Long userId,
  Long id,
  String title,
  String body
) {
}