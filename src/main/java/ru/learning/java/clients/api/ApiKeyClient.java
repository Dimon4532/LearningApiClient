package ru.learning.java.clients.api;

import io.restassured.response.ValidatableResponse;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * API клиент для аутентификации через API Keys
 * Поддерживает передачу ключа через заголовок, query-параметр или Basic Auth
 */
public class ApiKeyClient extends ApiClient {

  // ── Через заголовок  ───────────────────

  /**
   * [GET] с API Key в заголовке
   *
   * @param url         адрес сервиса
   * @param statusCode  ожидаемый статус код
   * @param headerName  имя заголовка (например, "X-API-Key", "Authorization")
   * @param apiKey      значение ключа
   * @param headers     дополнительные заголовки
   * @param pathParams  параметры пути
   * @param queryParams параметры запроса
   */
  public ValidatableResponse sendGetWithApiKeyHeader(
    String url,
    int statusCode,
    String headerName,
    String apiKey,
    Map<String, String> headers,
    Map<String, String> pathParams,
    Map<String, String> queryParams
  ) {
    installSpecification(requestSpecification(), responseSpecification());
    return given()
      .header(headerName, apiKey)
      .headers(headers)
      .pathParams(pathParams)
      .queryParams(queryParams)
      .when()
      .get(url)
      .then()
      .assertThat().statusCode(statusCode)
      .log().ifError();
  }

  /**
   * [POST] с API Key в заголовке
   */
  public ValidatableResponse sendPostWithApiKeyHeader(
    String url,
    int statusCode,
    String headerName,
    String apiKey,
    Object body,
    Map<String, String> headers,
    Map<String, String> pathParams,
    Map<String, String> queryParams
  ) {
    installSpecification(requestSpecification(), responseSpecification());
    return given()
      .header(headerName, apiKey)
      .headers(headers)
      .pathParams(pathParams)
      .queryParams(queryParams)
      .body(body)
      .when()
      .post(url)
      .then()
      .assertThat().statusCode(statusCode)
      .log().ifError();
  }

  // ── Через query-параметр ─────────────────────────────────────────────────

  /**
   * [GET] с API Key в query-параметре
   *
   * @param paramName имя параметра (например, "api_key", "apikey", "key")
   * @param apiKey    значение ключа
   */
  public ValidatableResponse sendGetWithApiKeyQuery(
    String url,
    int statusCode,
    String paramName,
    String apiKey,
    Map<String, String> headers,
    Map<String, String> pathParams,
    Map<String, String> queryParams
  ) {
    Map<String, String> allQueryParams = new HashMap<>(queryParams);
    allQueryParams.put(paramName, apiKey); // добавляем ключ к остальным query params

    installSpecification(requestSpecification(), responseSpecification());
    return given()
      .headers(headers)
      .pathParams(pathParams)
      .queryParams(allQueryParams)
      .when()
      .get(url)
      .then()
      .assertThat().statusCode(statusCode)
      .log().ifError();
  }

  // ── Через Basic Auth (key как username, пустой password) ─────────────────

  /**
   * [GET] с API Key как Basic Auth username (Stripe-style)
   * Некоторые API (например, Stripe) принимают API key как Basic Auth username
   */
  public ValidatableResponse sendGetWithApiKeyAsBasicAuth(
    String url,
    int statusCode,
    String apiKey,
    Map<String, String> headers,
    Map<String, String> pathParams,
    Map<String, String> queryParams
  ) {
    installSpecification(requestSpecification(), responseSpecification());
    return given()
      .auth()
      .preemptive()
      .basic(apiKey, "")
      .headers(headers)
      .pathParams(pathParams)
      .queryParams(queryParams)
      .when()
      .get(url)
      .then()
      .assertThat().statusCode(statusCode)
      .log().ifError();
  }
}