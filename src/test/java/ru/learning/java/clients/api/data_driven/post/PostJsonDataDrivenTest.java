package ru.learning.java.clients.api.data_driven.post;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ru.learning.java.models.immutable.ImmutablePostRequest;
import ru.learning.java.models.immutable.PostRequest;
import ru.learning.java.utils.TestDataLoader;

import java.util.stream.Stream;

@Feature("Data-Driven Testing")
@DisplayName("Data-Driven — JSON источник для posts")
class PostJsonDataDrivenTest extends AbstractPostDataDrivenTest {

  static Stream<PostRequest> posts() {
    return TestDataLoader
      .loadJsonList("/test-data/posts.json", ImmutablePostRequest.class)
      .stream()
      .map(p -> p);
  }

  @ParameterizedTest(name = "[{index}] POST /posts title=\"{0}\"")
  @MethodSource("posts")
  @DisplayName("Создание постов по данным из JSON")
  void createPostFromJson(PostRequest request) throws Exception {
    createPostAndAssertCreated(request);
  }
}
