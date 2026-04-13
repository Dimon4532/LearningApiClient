package ru.learning.java.clients.api;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static io.restassured.RestAssured.given;

public class JwtApiClient extends AuthApiClient {

  /**
   * Получение JWT токена через логин/пароль
   *
   * @param url      адрес сервиса
   * @param username логин
   * @param password пароль
   * @return JWT токен
   */
  public String fetchJwtToken(String url, String username, String password) {
    String body = """
      {"username": "%s", "password": "%s"}
      """.formatted(username, password);

    return given()
      .contentType("application/json")
      .body(body)
      .when()
      .post(url)
      .then()
      .statusCode(200)
      .extract()
      .jsonPath()
      .getString("token");
  }

  /**
   * GET запрос с JWT Bearer токеном
   *
   * @param url            адрес сервиса
   * @param expectedStatus ожидаемый статус код
   * @param token          JWT токен
   * @param headers        HTTP заголовки
   * @param pathParams     параметры пути
   * @param queryParams    параметры запроса
   */
  public ValidatableResponse sendGetWithJwt(
    String url, int expectedStatus, String token,
    Map<String, String> headers,
    Map<String, String> pathParams,
    Map<String, String> queryParams
  ) {
    Objects.requireNonNull(url, "url must not be null");
    Objects.requireNonNull(token, "token must not be null");

    RequestSpecification spec = given()
      .header("Authorization", "Bearer " + token)
      .headers(defaultIfNull(headers))
      .pathParams(defaultIfNull(pathParams))
      .queryParams(defaultIfNull(queryParams));

    return spec
      .when()
      .get(url)
      .then()
      .assertThat()
      .statusCode(expectedStatus)
      .log().ifValidationFails();
  }

  private Map<String, String> defaultIfNull(Map<String, String> map) {
    return map == null ? Collections.emptyMap() : map;
  }
}