package ru.learning.java.clients.api;

import io.restassured.response.ValidatableResponse;
import ru.learning.java.clients.api.base.Specification;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Базовый API клиент для стандартных HTTP запросов
 * Содержит основные методы для работы с REST API
 */
public class ApiClient extends Specification {

  /**
   * [GET]
   *
   * @param url         адрес сервиса
   * @param headers     заголовки
   * @param pathParams  параметры пути запроса
   * @param queryParams параметры запроса
   * @param cookies     cookies
   */
  public ValidatableResponse sendGet(String url,
                                     Map<String, String> headers,
                                     Map<String, String> pathParams,
                                     Map<String, String> queryParams,
                                     Map<String, String> cookies) {
    installSpecification(requestSpecification(), responseSpecification());
    return given()
      .redirects().follow(false)
      .headers(headers)
      .log().cookies()
      .log().headers()
      .cookies(cookies)
      .pathParams(pathParams)
      .queryParams(queryParams)
      .when()
      .get(url)
      .then()
      .log().all();
  }

  /**
   * [POST]
   *
   * @param url         адрес сервиса
   * @param statusCode  ожидаемый статус код
   * @param body        тело запроса
   * @param headers     заголовки
   * @param pathParams  параметры пути запроса
   * @param queryParams параметры запроса
   */
  public ValidatableResponse sendPost(String url,
                                      int statusCode,
                                      Object body,
                                      Map<String, String> headers,
                                      Map<String, String> pathParams,
                                      Map<String, String> queryParams) {
    installSpecification(requestSpecification(), responseSpecification());
    return given()
      .redirects().follow(false)
      .headers(headers)
      .log().cookies()
      .log().headers()
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
   * [PUT]
   *
   * @param url         адрес сервиса
   * @param statusCode  ожидаемый статус код
   * @param body        тело запроса
   * @param headers     заголовки
   * @param pathParams  параметры пути запроса
   * @param queryParams параметры запроса
   */
  public ValidatableResponse sendPut(String url,
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
      .put(url)
      .then()
      .assertThat().statusCode(statusCode)
      .log().ifError();
  }

  /**
   * [PATCH]
   *
   * @param url         адрес сервиса
   * @param statusCode  ожидаемый статус код
   * @param body        тело запроса
   * @param headers     заголовки
   * @param pathParams  параметры пути запроса
   * @param queryParams параметры запроса
   */
  public ValidatableResponse sendPatch(String url,
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
      .patch(url)
      .then()
      .assertThat().statusCode(statusCode)
      .log().ifError();
  }

  /**
   * [DELETE]
   *
   * @param url         адрес сервиса
   * @param statusCode  ожидаемый статус код
   * @param headers     заголовки
   * @param pathParams  параметры пути запроса
   * @param queryParams параметры запроса
   */
  public void sendDelete(String url,
                         int statusCode,
                         Map<String, String> headers,
                         Map<String, String> pathParams,
                         Map<String, String> queryParams) {
    installSpecification(requestSpecification(), responseSpecification());
    given()
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