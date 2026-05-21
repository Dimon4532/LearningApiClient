package ru.learning.java.clients.api.contract.openapi;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.learning.java.clients.api.base.BaseApiTest;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

/**
 * Лёгкая альтернатива OpenAPI: одна схема — один эндпоинт.
 * Полезно, когда полной OpenAPI-спецификации нет, но контракт нужен.
 */
@Feature("Contract Testing — JSON Schema")
@DisplayName("JSON Schema — ответ GET /users/1 валиден по локальной схеме")
@Tag("external")
class UserJsonSchemaTest extends BaseApiTest {

  @Test
  @Story("JSON Schema валидация одного ресурса")
  @DisplayName("GET /users/1 — соответствует contracts/json-schema/user-schema.json")
  void getUserByIdMatchesJsonSchema() {
    given()
      .baseUri(BASE_URL)
      .when()
      .get("/users/1")
      .then()
      .statusCode(200)
      .body(matchesJsonSchemaInClasspath("contracts/json-schema/user-schema.json"));
  }
}