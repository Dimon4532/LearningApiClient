package ru.learning.java.clients.api.contract.pact;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
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

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Contract Testing — Pact (consumer-driven)")
@DisplayName("Pact — consumer pact для JsonPlaceholder Comments API")
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "JsonPlaceholderCommentsApi", port = "8890",
  pactVersion = PactSpecVersion.V3)
class CommentConsumerPactTest extends BaseApiTest {

  private static final String CONSUMER = "LearningApiClient";
  private static final String EMAIL_REGEX = "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}";

  @Pact(consumer = CONSUMER)
  public RequestResponsePact getComment1(PactDslWithProvider builder) {
    return builder
      .given("comment with id=1 exists")
      .uponReceiving("GET /comments/1")
      .path("/comments/1")
      .method("GET")
      .willRespondWith()
      .status(200)
      .headers(Map.of("Content-Type", "application/json; charset=utf-8"))
      .body(commentBody(1, 1, "id labore ex et quam laborum", "Eliseo@gardner.biz"))
      .toPact();
  }

  @Pact(consumer = CONSUMER)
  public RequestResponsePact listCommentsForPost(PactDslWithProvider builder) {
    return builder
      .given("comments for post with id=1 exist")
      .uponReceiving("GET /comments?postId=1")
      .path("/comments")
      .method("GET")
      .query("postId=1")
      .willRespondWith()
      .status(200)
      .headers(Map.of("Content-Type", "application/json; charset=utf-8"))
      .body(PactDslJsonArray.arrayMinLike(1)
        .integerType("postId", 1)
        .integerType("id", 1)
        .stringType("name", "id labore ex et quam laborum")
        .stringMatcher("email", EMAIL_REGEX, "Eliseo@gardner.biz")
        .stringType("body", "laudantium enim quasi est quidem magnam")
        .closeObject())
      .toPact();
  }

  @Pact(consumer = CONSUMER)
  public RequestResponsePact createComment(PactDslWithProvider builder) {
    return builder
      .given("post with id=1 exists")
      .uponReceiving("POST /posts/1/comments")
      .path("/posts/1/comments")
      .method("POST")
      .headers(Map.of("Content-Type", "application/json"))
      .body(new PactDslJsonBody()
        .stringType("name", "Jane Doe")
        .stringMatcher("email", EMAIL_REGEX, "jane@doe.com")
        .stringType("body", "Looks great!"))
      .willRespondWith()
      .status(201)
      .headers(Map.of("Content-Type", "application/json; charset=utf-8"))
      .body(new PactDslJsonBody()
        .stringMatcher("postId", "\\d+", "1")
        .integerType("id", 501)
        .stringType("name", "Jane Doe")
        .stringMatcher("email", EMAIL_REGEX, "jane@doe.com")
        .stringType("body", "Looks great!"))
      .toPact();
  }

  @Test
  @Story("GET /comments/1")
  @DisplayName("Consumer соответствует контракту GET /comments/1")
  @PactTestFor(pactMethod = "getComment1")
  void shouldGetComment1(MockServer mockServer) {
    Response response = apiClient.sendGet(
      mockServer.getUrl() + "/comments/1",
      Map.of(), Map.of(), Map.of(), Map.of()
    ).extract().response();

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.jsonPath().getInt("id")).isEqualTo(1);
    assertThat(response.jsonPath().getString("email")).contains("@");
  }

  @Test
  @Story("GET /comments?postId=1")
  @DisplayName("Consumer соответствует контракту GET /comments?postId=1")
  @PactTestFor(pactMethod = "listCommentsForPost")
  void shouldListCommentsForPost(MockServer mockServer) {
    Response response = apiClient.sendGet(
      mockServer.getUrl() + "/comments",
      Map.of(), Map.of(), Map.of("postId", "1"), Map.of()
    ).extract().response();

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.jsonPath().getList("$")).isNotEmpty();
    assertThat(response.jsonPath().getInt("[0].postId")).isEqualTo(1);
    assertThat(response.jsonPath().getString("[0].email")).contains("@");
  }

  @Test
  @Story("POST /posts/1/comments")
  @DisplayName("Consumer соответствует контракту POST /posts/1/comments")
  @PactTestFor(pactMethod = "createComment")
  void shouldCreateComment(MockServer mockServer) throws Exception {
    String body = """
      {"name":"Jane Doe","email":"jane@doe.com","body":"Looks great!"}
      """;

    Response response = apiClient.sendPost(
      mockServer.getUrl() + "/posts/1/comments", 201, body,
      Map.of("Content-Type", "application/json"),
      new HashMap<>(), new HashMap<>()
    ).extract().response();

    assertThat(response.statusCode()).isEqualTo(201);
    assertThat(response.jsonPath().getInt("id")).isPositive();
    assertThat(response.jsonPath().getString("postId")).isEqualTo("1");
    assertThat(response.jsonPath().getString("email")).isEqualTo("jane@doe.com");
  }

  private PactDslJsonBody commentBody(int postId, int id, String name, String email) {
    return new PactDslJsonBody()
      .integerType("postId", postId)
      .integerType("id", id)
      .stringType("name", name)
      .stringMatcher("email", EMAIL_REGEX, email)
      .stringType("body", "laudantium enim quasi est quidem magnam");
  }
}
