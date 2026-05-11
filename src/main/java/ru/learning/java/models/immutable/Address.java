package ru.learning.java.models.immutable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * Демонстрация Immutables: иммутабельный value-объект.
 * Кодогенерация создаст класс ImmutableAddress с builder, equals, hashCode, toString.
 */
@Value.Immutable
@Value.Style(jdkOnly = true)
@JsonSerialize(as = ImmutableAddress.class)
@JsonDeserialize(as = ImmutableAddress.class)
public interface Address {

  String street();

  String city();

  String zipcode();

  @Value.Default
  default String country() {
    return "RU";
  }

  @Value.Check
  default void check() {
    if (street().isBlank()) {
      throw new IllegalStateException("street must not be blank");
    }
  }
}