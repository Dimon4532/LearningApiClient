package ru.learning.java.clients.api.contract.openapi;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.restassured.OpenApiValidationFilter;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.learning.java.clients.api.base.BaseApiTest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@Feature("Contract Testing — OpenAPI (provider-driven schema)")
@DisplayName("OpenAPI — валидация Comments API против спецификации")
@Tag("external")
class CommentOpenApiSchemaTest extends BaseApiTest {

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
  @Story("GET /comments/{id} соответствует OpenAPI")
  @DisplayName("GET /comments/1 — соответствует схеме Comment")
  void getCommentByIdMatchesContract() {
    Response response = given()
      .filter(filter)
      .baseUri(BASE_URL)
      .when()
      .get("/comments/1")
      .then()
      .statusCode(200)
      .extract().response();

    assertThat(response.jsonPath().getInt("id")).isEqualTo(1);
    assertThat(response.jsonPath().getString("email")).contains("@");
  }

  @Test
  @Story("GET /comments?postId=1 соответствует OpenAPI")
  @DisplayName("GET /comments?postId=1 — массив элементов схемы Comment")
  void listCommentsForPostMatchesContract() {
    Response response = given()
      .filter(filter)
      .baseUri(BASE_URL)
      .queryParam("postId", 1)
      .when()
      .get("/comments")
      .then()
      .statusCode(200)
      .extract().response();

    assertThat(response.jsonPath().getList("$")).isNotEmpty();
    assertThat(response.jsonPath().getInt("[0].postId")).isEqualTo(1);
  }

  @Test
  @Story("POST /posts/{postId}/comments соответствует OpenAPI")
  @DisplayName("POST /posts/1/comments — запрос и ответ соответствуют схемам")
  void createCommentMatchesContract() {
    String body = """
      {"name":"Jane Doe","email":"jane@doe.com","body":"Looks great!"}
      """;

    Response response = given()
      .filter(filter)
      .baseUri(BASE_URL)
      .contentType("application/json")
      .body(body)
      .when()
      .post("/posts/1/comments")
      .then()
      .statusCode(201)
      .extract().response();

    assertThat(response.jsonPath().getInt("id")).isPositive();
    assertThat(response.jsonPath().getString("email")).isEqualTo("jane@doe.com");
  }
}
