package ru.learning.java.clients.api.data_driven;

import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ru.learning.java.clients.api.base.BaseApiTest;
import ru.learning.java.models.immutable.PostRequest;
import ru.learning.java.utils.TestDataLoader;
import ru.learning.java.validation.ModelValidator;

import java.util.HashMap;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Data-Driven Testing")
@DisplayName("Data-Driven — JSON источник для posts")
class PostJsonDataDrivenTest extends BaseApiTest {

  static Stream<PostRequest> posts() {
    return TestDataLoader
      .loadJsonList("/test-data/posts.json", PostRequest.class)
      .stream();
  }

  @ParameterizedTest(name = "[{index}] POST /posts {0}")
  @MethodSource("posts")
  @DisplayName("Создание постов по данным из JSON")
  void createPostFromJson(PostRequest request) throws Exception {
    ModelValidator.requireValid(request);
    
    Response response = apiClient.sendPost(
      BASE_URL + "/posts", 201,
      objectMapper.writeValueAsString(request),
      new HashMap<>(), new HashMap<>(), new HashMap<>()
    ).extract().response();

    assertThat(response.jsonPath().getInt("id")).isPositive();
  }
}
