package ru.learning.java.clients.api.data_driven;

import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import ru.learning.java.clients.api.base.BaseApiTest;
import ru.learning.java.models.immutable.ImmutablePostRequest;
import ru.learning.java.models.immutable.PostRequest;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Data-Driven Testing")
@DisplayName("Data-Driven — CSV источник для posts")
class PostCsvDataDrivenTest extends BaseApiTest {

  @ParameterizedTest(name = "[{index}] POST /posts userId={0}, title={1}")
  @CsvFileSource(resources = "/test-data/posts.csv", numLinesToSkip = 1)
  @DisplayName("Создание постов по данным из CSV")
  void createPostFromCsv(long userId, String title, String body) throws Exception {
    PostRequest request = ImmutablePostRequest.builder()
                                              .userId(userId)
                                              .title(title)
                                              .body(body)
                                              .build();

    Response response = apiClient.sendPost(
      BASE_URL + "/posts", 201,
      objectMapper.writeValueAsString(request),
      new HashMap<>(), new HashMap<>(), new HashMap<>()
    ).extract().response();

    assertThat(response.jsonPath().getInt("id")).isPositive();
  }
}
