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

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Contract Testing — Pact (consumer-driven)")
@DisplayName("Pact — consumer pact для JsonPlaceholder Users API")
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "JsonPlaceholderUsersApi", port = "8888", pactVersion = PactSpecVersion.V3)
class UserConsumerPactTest extends BaseApiTest {

  /**
   * Декларативное описание контракта: «на GET /users/1 ожидаю 200 +
   * JSON с полями id (number), name, username, email (matches email regex)».
   */
  @Pact(consumer = "LearningApiClient")
  public RequestResponsePact getUser1(PactDslWithProvider builder) {
    return builder
      .given("user with id=1 exists")
      .uponReceiving("GET /users/1")
      .path("/users/1")
      .method("GET")
      .willRespondWith()
      .status(200)
      .headers(Map.of("Content-Type", "application/json; charset=utf-8"))
      .body(new PactDslJsonBody()
        .integerType("id", 1)
        .stringType("name", "Leanne Graham")
        .stringType("username", "Bret")
        .stringMatcher("email",
          "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}",
          "Sincere@april.biz"))
      .toPact();
  }

  @Test
  @Story("Consumer pact: GET /users/1")
  @DisplayName("Consumer соответствует контракту GET /users/1")
  @PactTestFor(pactMethod = "getUser1")
  void shouldGetUser1(MockServer mockServer) {
    Response response = apiClient.sendGet(
      mockServer.getUrl() + "/users/1",
      new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>()
    ).extract().response();

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.jsonPath().getInt("id")).isEqualTo(1);
    assertThat(response.jsonPath().getString("email")).contains("@");
  }
}