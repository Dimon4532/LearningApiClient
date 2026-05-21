package ru.learning.java.clients.api.contract.openapi;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.restassured.OpenApiValidationFilter;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.learning.java.clients.api.base.BaseApiTest;

import static io.restassured.RestAssured.given;

/**
 * Контрактные тесты для Posts API против OpenAPI-спецификации.
 *
 * <p>Параллель с {@code PostConsumerPactTest}, но контракт хранится не в коде,
 * а в YAML-файле {@code contracts/openapi/jsonplaceholder.yaml}.
 */
@Feature("Contract Testing — OpenAPI (provider-driven schema)")
@DisplayName("OpenAPI — валидация Posts API против спецификации")
@Tag("external")
class PostOpenApiSchemaTest extends BaseApiTest {

  private static final String SPEC = "contracts/openapi/jsonplaceholder.yaml";

  private static OpenApiValidationFilter filter;

  @BeforeAll
  static void initValidator() {
    OpenApiInteractionValidator openApiInteractionValidator = OpenApiInteractionValidator
      .createForSpecificationUrl(SPEC)
      .build();
    filter = new OpenApiValidationFilter(openApiInteractionValidator);
  }

  @Test
  @Story("GET /posts/{id} соответствует OpenAPI")
  @DisplayName("GET /posts/1 — структура соответствует схеме Post")
  void getPostByIdMatchesContract() {
    given()
      .filter(filter)
      .baseUri(BASE_URL)
      .when()
      .get("/posts/1")
      .then()
      .statusCode(200);
  }

  @Test
  @Story("POST /posts соответствует OpenAPI")
  @DisplayName("POST /posts — запрос И ответ соответствуют схемам PostInput/Post")
  void createPostMatchesContract() {
    String body = """
      {"userId":1,"title":"My new post","body":"Hello from OpenAPI!"}
      """;

    given()
      .filter(filter)
      .baseUri(BASE_URL)
      .contentType("application/json")
      .body(body)
      .when()
      .post("/posts")
      .then()
      .statusCode(201);
  }
}