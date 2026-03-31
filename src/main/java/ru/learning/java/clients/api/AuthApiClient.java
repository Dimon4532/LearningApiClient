package ru.learning.java.clients.api;

import io.restassured.response.ValidatableResponse;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * API клиент для запросов с авторизацией
 * Расширяет базовый ApiClient, добавляя методы с Basic Authentication
 */
public class AuthApiClient extends ApiClient {

  /**
   * [GET] с авторизацией
   *
   * @param url         адрес сервиса
   * @param login       login
   * @param password    password
   * @param statusCode  ожидаемый статус код
   * @param headers     заголовки
   * @param pathParams  параметры пути запроса
   * @param queryParams параметры запроса
   */
  public ValidatableResponse sendGetWithAuth(String url,
                                             int statusCode,
                                             String login,
                                             String password,
                                             Map<String, String> headers,
                                             Map<String, String> pathParams,
                                             Map<String, String> queryParams) {
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
   * [POST] с авторизацией
   *
   * @param url         адрес сервиса
   * @param statusCode  ожидаемый статус код
   * @param login       login
   * @param password    password
   * @param body        тело запроса
   * @param headers     заголовки
   * @param pathParams  параметры пути запроса
   * @param queryParams параметры запроса
   */
  public ValidatableResponse sendPostWithAuth(String url,
                                              int statusCode,
                                              String login,
                                              String password,
                                              Object body,
                                              Map<String, String> headers,
                                              Map<String, String> pathParams,
                                              Map<String, String> queryParams) {
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
   * [PUT] с авторизацией
   *
   * @param url         адрес сервиса
   * @param statusCode  ожидаемый статус код
   * @param login       login
   * @param password    password
   * @param body        тело запроса
   * @param headers     заголовки
   * @param pathParams  параметры пути запроса
   * @param queryParams параметры запроса
   */
  public ValidatableResponse sendPutWithAuth(String url,
                                             int statusCode,
                                             String login,
                                             String password,
                                             Object body,
                                             Map<String, String> headers,
                                             Map<String, String> pathParams,
                                             Map<String, String> queryParams) {
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
   * [PATCH] с авторизацией
   *
   * @param url         адрес сервиса
   * @param statusCode  ожидаемый статус код
   * @param login       login
   * @param password    password
   * @param body        тело запроса
   * @param headers     заголовки
   * @param pathParams  параметры пути запроса
   * @param queryParams параметры запроса
   */
  public ValidatableResponse sendPatchWithAuth(String url,
                                               int statusCode,
                                               String login,
                                               String password,
                                               Object body,
                                               Map<String, String> headers,
                                               Map<String, String> pathParams,
                                               Map<String, String> queryParams) {
    installSpecification(requestSpecification(), responseSpecification());
    return given()
      .auth()
      .basic(login, password)
      .headers(headers)
      .pathParams(pathParams)
      .queryParams(queryParams)
      .body(body)
      .when()
      .patch(url)
      .then()
      .assertThat().statusCode(statusCode)
      .log().ifError();
  }

  /**
   * [DELETE] с авторизацией
   *
   * @param url         адрес сервиса
   * @param statusCode  ожидаемый статус код
   * @param login       login
   * @param password    password
   * @param headers     заголовки
   * @param pathParams  параметры пути запроса
   * @param queryParams параметры запроса
   */
  public ValidatableResponse sendDeleteWithAuth(String url,
                                                int statusCode,
                                                String login,
                                                String password,
                                                Map<String, String> headers,
                                                Map<String, String> pathParams,
                                                Map<String, String> queryParams) {
    installSpecification(requestSpecification(), responseSpecification());
    return given()
      .auth()
      .basic(login, password)
      .headers(headers)
      .pathParams(pathParams)
      .queryParams(queryParams)
      .when()
      .delete(url)
      .then()
      .assertThat().statusCode(statusCode)
      .log().ifError();
  }

  // ==================== BEARER TOKEN МЕТОДЫ ====================

  /**
   * [POST] для получения токена
   * Обычно используется для OAuth2 или других систем аутентификации
   *
   * @param url         адрес сервиса
   * @param statusCode  ожидаемый статус код
   * @param body        тело запроса (обычно credentials)
   * @param headers     заголовки
   * @param pathParams  параметры пути запроса
   * @param queryParams параметры запроса
   */
  public ValidatableResponse sendPostForToken(String url,
                                              int statusCode,
                                              Object body,
                                              Map<String, String> headers,
                                              Map<String, String> pathParams,
                                              Map<String, String> queryParams) {
    installSpecification(requestSpecification(), responseSpecification());
    return given()
      .headers(headers)
      .pathParams(pathParams)
      .queryParams(queryParams)
      .body(body)
      .when()
      .post(url)
      .then()
      .assertThat().statusCode(statusCode)
      .log().all();
  }

  /**
   * [GET] с Bearer Token
   *
   * @param url         адрес сервиса
   * @param statusCode  ожидаемый статус код
   * @param token       Bearer token
   * @param headers     заголовки
   * @param pathParams  параметры пути запроса
   * @param queryParams параметры запроса
   */
  public ValidatableResponse sendGetWithBearerToken(String url,
                                                    int statusCode,
                                                    String token,
                                                    Map<String, String> headers,
                                                    Map<String, String> pathParams,
                                                    Map<String, String> queryParams) {
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
   * [POST] с Bearer Token
   *
   * @param url         адрес сервиса
   * @param statusCode  ожидаемый статус код
   * @param token       Bearer token
   * @param body        тело запроса
   * @param headers     заголовки
   * @param pathParams  параметры пути запроса
   * @param queryParams параметры запроса
   */
  public ValidatableResponse sendPostWithBearerToken(String url,
                                                     int statusCode,
                                                     String token,
                                                     Object body,
                                                     Map<String, String> headers,
                                                     Map<String, String> pathParams,
                                                     Map<String, String> queryParams) {
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
   * [PUT] с Bearer Token
   *
   * @param url         адрес сервиса
   * @param statusCode  ожидаемый статус код
   * @param token       Bearer token
   * @param body        тело запроса
   * @param headers     заголовки
   * @param pathParams  параметры пути запроса
   * @param queryParams параметры запроса
   */
  public ValidatableResponse sendPutWithBearerToken(String url,
                                                    int statusCode,
                                                    String token,
                                                    Object body,
                                                    Map<String, String> headers,
                                                    Map<String, String> pathParams,
                                                    Map<String, String> queryParams) {
    installSpecification(requestSpecification(), responseSpecification());
    return given()
      .auth()
      .oauth2(token)
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
   * [PATCH] с Bearer Token
   *
   * @param url         адрес сервиса
   * @param statusCode  ожидаемый статус код
   * @param token       Bearer token
   * @param body        тело запроса
   * @param headers     заголовки
   * @param pathParams  параметры пути запроса
   * @param queryParams параметры запроса
   */
  public ValidatableResponse sendPatchWithBearerToken(String url,
                                                      int statusCode,
                                                      String token,
                                                      Object body,
                                                      Map<String, String> headers,
                                                      Map<String, String> pathParams,
                                                      Map<String, String> queryParams) {
    installSpecification(requestSpecification(), responseSpecification());
    return given()
      .auth()
      .oauth2(token)
      .headers(headers)
      .pathParams(pathParams)
      .queryParams(queryParams)
      .body(body)
      .when()
      .patch(url)
      .then()
      .assertThat().statusCode(statusCode)
      .log().ifError();
  }

  /**
   * [DELETE] с Bearer Token
   *
   * @param url         адрес сервиса
   * @param statusCode  ожидаемый статус код
   * @param token       Bearer token
   * @param headers     заголовки
   * @param pathParams  параметры пути запроса
   * @param queryParams параметры запроса
   */
  public ValidatableResponse sendDeleteWithBearerToken(String url,
                                                       int statusCode,
                                                       String token,
                                                       Map<String, String> headers,
                                                       Map<String, String> pathParams,
                                                       Map<String, String> queryParams) {
    installSpecification(requestSpecification(), responseSpecification());
    return given()
      .auth()
      .oauth2(token)
      .headers(headers)
      .pathParams(pathParams)
      .queryParams(queryParams)
      .when()
      .delete(url)
      .then()
      .assertThat().statusCode(statusCode)
      .log().ifError();
  }
}