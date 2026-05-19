package ru.learning.java.clients.api.data_driven.post;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.response.Response;
import ru.learning.java.clients.api.base.BaseApiTest;
import ru.learning.java.models.immutable.PostRequest;

import java.util.HashMap;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public abstract class AbstractPostDataDrivenTest extends BaseApiTest {

  protected void createPostAndAssertCreated(PostRequest request) throws JsonProcessingException {
    Response response = apiClient.sendPost(
      BASE_URL + "/posts", 201,
      objectMapper.writeValueAsString(request),
      new HashMap<>(), new HashMap<>(), new HashMap<>()
    ).extract().response();

    assertThat(response.jsonPath().getInt("id")).isPositive();
    assertThat(response.jsonPath().getString("title")).isEqualTo(request.title());
  }
}
