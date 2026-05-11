package ru.learning.java.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.List;

public final class TestDataLoader {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private TestDataLoader() {}

  public static <T> List<T> loadJsonList(String classpathResource, Class<T> type) {
    try (InputStream is = TestDataLoader.class.getResourceAsStream(classpathResource)) {
      if (is == null) {
        throw new IllegalStateException("Resource not found: " + classpathResource);
      }
      return MAPPER.readValue(
        is,
        MAPPER.getTypeFactory().constructCollectionType(List.class, type)
      );
    } catch (Exception e) {
      throw new RuntimeException("Failed to load: " + classpathResource, e);
    }
  }
}