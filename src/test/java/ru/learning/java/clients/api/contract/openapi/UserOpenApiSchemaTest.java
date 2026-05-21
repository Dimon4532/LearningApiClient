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

/**
 * Контрактные тесты по OpenAPI-спецификации (provider-driven contract testing).
 *
 * <p>Параллель с {@code UserConsumerPactTest}:
 * <ul>
 *   <li>Pact описывает <b>ожидания consumer'а</b> в коде, генерирует JSON-pact.</li>
 *   <li>OpenAPI описывает <b>контракт provider'а</b> в YAML, валидирует реальные ответы.</li>
 * </ul>
 * Здесь мы ходим к настоящему API и проверяем, что и запрос, и ответ
 * соответствуют schemes в {@code contracts/openapi/jsonplaceholder.yaml}.
 */
@Feature("Contract Testing — OpenAPI (provider-driven schema)")
@DisplayName("OpenAPI — валидация ответов JsonPlaceholder Users API против спецификации")
@Tag("external") // на случай нестабильности jsonplaceholder; см. совет про external-тесты
public class UserOpenApiSchemaTest extends BaseApiTest {

  private static final String SPEC = "contracts/openapi/jsonplaceholder.yaml";

  private static OpenApiValidationFilter openApiFilter;

  @BeforeAll
  static void initOpenApiValidator() {
    OpenApiInteractionValidator validator = OpenApiInteractionValidator
      .createForSpecificationUrl(SPEC)
      .build();
    openApiFilter = new OpenApiValidationFilter(validator);
  }

  @Test
  @Story("GET /users/{id} соответствует OpenAPI")
  @DisplayName("GET /users/1 — структура ответа соответствует схеме User")
  public void getUserByIdMatchesContract() {
    Response response = given()
      .filter(openApiFilter)               // ← вся магия здесь
      .baseUri(BASE_URL)
      .when()
      .get("/users/1")
      .then()
      .statusCode(200)
      .extract().response();

    // Содержательные проверки делаем ПОСЛЕ контрактной — как и в Pact
    assertThat(response.jsonPath().getInt("id")).isEqualTo(1);
    assertThat(response.jsonPath().getString("email")).contains("@");
  }

  @Test
  @Story("GET /users соответствует OpenAPI")
  @DisplayName("GET /users — каждый элемент массива соответствует схеме User")
  public void listUsersMatchesContract() {
    given()
      .filter(openApiFilter)
      .baseUri(BASE_URL)
      .when()
      .get("/users")
      .then()
      .statusCode(200);
  }
}