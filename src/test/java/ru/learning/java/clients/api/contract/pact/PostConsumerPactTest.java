package ru.learning.java.clients.api.contract.pact;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.learning.java.clients.api.base.BaseApiTest;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Feature("Contract Testing — Pact (consumer-driven)")
@DisplayName("Pact — consumer pact для JsonPlaceholder Posts API")
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "JsonPlaceholderPostsApi", port = "8889",
  pactVersion = PactSpecVersion.V3)
class PostConsumerPactTest extends BaseApiTest {

  private static final String CONSUMER = "LearningApiClient";

  @Pact(consumer = CONSUMER)
  public RequestResponsePact getPost1(PactDslWithProvider builder) {
    return builder
      .given("post with id=1 exists")
      .uponReceiving("GET /posts/1")
      .path("/posts/1")
      .method("GET")
      .willRespondWith()
      .status(200)
      .headers(Map.of("Content-Type", "application/json; charset=utf-8"))
      .body(new PactDslJsonBody()
        .integerType("userId", 1)
        .integerType("id", 1)
        .stringMatcher("title", ".{3,}", "Sample post title")
        .stringType("body", "Sample body"))
      .toPact();
  }

  @Pact(consumer = CONSUMER)
  public RequestResponsePact createPost(PactDslWithProvider builder) {
    return builder
      .given("user with id=1 exists")
      .uponReceiving("POST /posts")
      .path("/posts")
      .method("POST")
      .headers(Map.of("Content-Type", "application/json"))
      .body(new PactDslJsonBody()
        .integerType("userId", 1)
        .stringType("title", "My new post")
        .stringType("body", "Hello from Pact!"))
      .willRespondWith()
      .status(201)
      .headers(Map.of("Content-Type", "application/json; charset=utf-8"))
      .body(new PactDslJsonBody()
        .integerType("id", 101)
        .integerType("userId", 1)
        .stringType("title", "My new post")
        .stringType("body", "Hello from Pact!"))
      .toPact();
  }

  @Test
  @Story("GET /posts/1")
  @DisplayName("Consumer соответствует контракту GET /posts/1")
  @PactTestFor(pactMethod = "getPost1")
  void shouldGetPost1(MockServer mockServer) {
    Response response = apiClient.sendGet(
      mockServer.getUrl() + "/posts/1",
      Map.of(), Map.of(), Map.of(), Map.of()
    ).extract().response();

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.jsonPath().getInt("id")).isEqualTo(1);
    assertThat(response.jsonPath().getString("title")).isNotBlank();
  }

  @Test
  @Story("POST /posts")
  @DisplayName("Consumer соответствует контракту POST /posts")
  @PactTestFor(pactMethod = "createPost")
  void shouldCreatePost(MockServer mockServer) throws Exception {
    String body = """
      {"userId":1,"title":"My new post","body":"Hello from Pact!"}
      """;

    Response response = apiClient.sendPost(
      mockServer.getUrl() + "/posts", 201, body,
      Map.of("Content-Type", "application/json"),
      new HashMap<>(), new HashMap<>()
    ).extract().response();

    assertThat(response.jsonPath().getInt("id")).isPositive();
    assertThat(response.jsonPath().getInt("userId")).isEqualTo(1);
  }
}