package ru.learning.java.clients.api;

import io.restassured.response.ValidatableResponse;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static io.restassured.RestAssured.given;

/**
 * API клиент для Digest Authentication (RFC 7616).
 * <p>
 * В отличие от Basic Auth, Digest не передаёт пароль открытым текстом —
 * вместо этого клиент и сервер обмениваются MD5-хэшами с использованием
 * случайного nonce от сервера.
 * <p>
 * REST Assured автоматически выполняет двухшаговый flow:
 * первый запрос → 401 + WWW-Authenticate, второй → расчёт response и повтор.
 */
public class DigestApiClient extends ApiClient {

  /**
   * GET с Digest Authentication
   *
   * @param url            адрес сервиса
   * @param expectedStatus ожидаемый статус
   * @param username       имя пользователя
   * @param password       пароль
   * @param headers        дополнительные заголовки
   * @param pathParams     параметры пути
   * @param queryParams    параметры запроса
   */
  public ValidatableResponse sendGetWithDigest(
    String url, int expectedStatus,
    String username, String password,
    Map<String, String> headers,
    Map<String, String> pathParams,
    Map<String, String> queryParams
  ) {
    Objects.requireNonNull(url, "url must not be null");
    Objects.requireNonNull(username, "username must not be null");
    Objects.requireNonNull(password, "password must not be null");

    return given()
      .auth().digest(username, password)
      .headers(defaultIfNull(headers))
      .pathParams(defaultIfNull(pathParams))
      .queryParams(defaultIfNull(queryParams))
      .when()
      .get(url)
      .then()
      .assertThat()
      .statusCode(expectedStatus)
      .log().ifValidationFails();
  }

  /**
   * POST с Digest Authentication
   */
  public ValidatableResponse sendPostWithDigest(
    String url, int expectedStatus,
    String username, String password, String body,
    Map<String, String> headers,
    Map<String, String> pathParams,
    Map<String, String> queryParams
  ) {
    Objects.requireNonNull(url, "url must not be null");
    Objects.requireNonNull(username, "username must not be null");
    Objects.requireNonNull(password, "password must not be null");

    return given()
      .auth().digest(username, password)
      .contentType("application/json")
      .headers(defaultIfNull(headers))
      .pathParams(defaultIfNull(pathParams))
      .queryParams(defaultIfNull(queryParams))
      .body(body == null ? "" : body)
      .when()
      .post(url)
      .then()
      .assertThat()
      .statusCode(expectedStatus)
      .log().ifValidationFails();
  }

  private Map<String, String> defaultIfNull(Map<String, String> map) {
    return map == null ? Collections.emptyMap() : map;
  }
}