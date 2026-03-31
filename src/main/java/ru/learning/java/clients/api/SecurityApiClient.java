package ru.learning.java.clients.api;

import io.restassured.response.ValidatableResponse;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Клиент для security-related API: basic auth, bearer token, form-urlencoded и cookie-based flows.
 */
public class SecurityApiClient extends ApiClient {
  /**
   * [GET] с Basic авторизацией.
   */
  public ValidatableResponse sendGetWithLoginAndPassword(String url, int statusCode, String login, String password,
                                                         Map<String, String> headers, Map<String, String> pathParams,
                                                         Map<String, String> queryParams
  ) {
    installSpecification(requestSpecification(), responseSpecification());
    return given()
      .auth()
      .basic(login, password)
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
   * [POST] с Basic авторизацией.
   */
  public ValidatableResponse sendPostWithLoginAndPassword(String url, int statusCode,
                                                          String login, String password, String body,
                                                          Map<String, String> headers, Map<String, String> pathParams,
                                                          Map<String, String> queryParams
  ) {
    installSpecification(requestSpecification(), responseSpecification());
    return given()
      .auth()
      .basic(login, password)
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

  /**
   * [PUT] с Basic авторизацией.
   */
  public ValidatableResponse sendPutWithLoginAndPassword(String url, int statusCode,
                                                         String login, String password, String body,
                                                         Map<String, String> headers, Map<String, String> pathParams,
                                                         Map<String, String> queryParams
  ) {
    installSpecification(requestSpecification(), responseSpecification());
    return given()
      .auth()
      .basic(login, password)
      .headers(headers)
      .pathParams(pathParams)
      .queryParams(queryParams)
      .body(body)
      .when()
      .put(url)
      .then()
      .assertThat().statusCode(statusCode)
      .log().ifError();
  }

  /**
   * [GET] с Bearer token.
   */
  public ValidatableResponse sendGetWithBearerToken(String url, int statusCode, String token,
                                                    Map<String, String> headers, Map<String, String> pathParams,
                                                    Map<String, String> queryParams
  ) {
    installSpecification(requestSpecification(), responseSpecification());
    return given()
      .auth()
      .oauth2(token)
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
   * [POST] с Bearer token.
   */
  public ValidatableResponse sendPostWithBearerToken(String url, int statusCode, String token, String body,
                                                     Map<String, String> headers, Map<String, String> pathParams,
                                                     Map<String, String> queryParams
  ) {
    installSpecification(requestSpecification(), responseSpecification());
    return given()
      .auth()
      .oauth2(token)
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

  /**
   * [POST] с Bearer token для сценариев обновления токена или защищенных операций.
   */
  public ValidatableResponse sendPostForToken(String url,
                                              Map<String, String> headers, Map<String, String> formParams
  ) {
    installSpecification(requestSpecification(), responseSpecification());
    return given()
      .headers(headers)
      .formParams(formParams)
      .when()
      .post(url)
      .then()
      .log().all();
  }

  /**
   * [GET] c типом URL_ENCODED и cookies.
   */
  public ValidatableResponse sendGetWithFormParamsAndCookies(String url,
                                                             Map<String, String> headers, Map<String, String> cookies,
                                                             Map<String, String> formParams
  ) {
    installSpecification(requestSpecification(), responseSpecification());
    return given()
      .cookies(cookies)
      .headers(headers)
      .formParams(formParams)
      .when()
      .get(url)
      .then();
  }

  /**
   * [POST] c типом URL_ENCODED.
   */
  public ValidatableResponse sendPostWithFormParams(String url,
                                                    Map<String, String> headers, Map<String, String> formParams
  ) {
    installSpecification(requestSpecification(), responseSpecification());
    return given()
      .headers(headers)
      .formParams(formParams)
      .when()
      .post(url)
      .then();
  }

  /**
   * [POST] c типом URL_ENCODED и cookies.
   */
  public ValidatableResponse sendPostWithFormParamsAndCookies(String url,
                                                              Map<String, String> headers, Map<String, String> cookies,
                                                              Map<String, String> formParams
  ) {
    installSpecification(requestSpecification(), responseSpecification());
    return given()
      .cookies(cookies)
      .headers(headers)
      .formParams(formParams)
      .when()
      .post(url)
      .then();
  }

  /**
   * [POST] для URL_ENCODED security flows с cookies и отключенным redirect.
   */
  public ValidatableResponse sendPostUrlEncodedClient(String url,
                                                      Map<String, String> headers, Map<String, String> formParams,
                                                      Map<String, String> cookies
  ) {
    installSpecification(requestSpecification(), responseSpecification());
    return given()
      .redirects().follow(false)
      .cookies(cookies)
      .headers(headers)
      .formParams(formParams)
      .when()
      .post(url)
      .then()
      .log().all();
  }
}
