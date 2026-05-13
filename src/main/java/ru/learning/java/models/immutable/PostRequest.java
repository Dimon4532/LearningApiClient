package ru.learning.java.models.immutable;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutablePostRequest.class)
@JsonDeserialize(as = ImmutablePostRequest.class)
public interface PostRequest {

  long userId();

  String title();

  String body();

  @Value.Default
  default List<String> tags() {
    return List.of();
  }

  @Value.Check
  default void check() {
    if (userId() <= 0) {
      throw new IllegalStateException("userId must be positive, was: " + userId());
    }
    if (title() == null || title().length() < 3) {
      throw new IllegalStateException("title must have at least 3 characters");
    }
    if (body() == null || body().isBlank()) {
      throw new IllegalStateException("body must not be blank");
    }
  }
}
