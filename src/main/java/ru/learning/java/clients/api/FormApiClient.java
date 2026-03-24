package ru.learning.java.clients.api;

import io.restassured.response.ValidatableResponse;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * API клиент для запросов с application/x-www-form-urlencoded
 * Расширяет базовый ApiClient, добавляя методы для работы с form parameters
 */
public class FormApiClient extends ApiClient {

  /**
   * [GET] с form parameters и cookies
   *
   * @param url        адрес сервиса
   * @param headers    заголовки
   * @param cookies    cookies
   * @param formParams параметры формы
   */
  public ValidatableResponse sendGetWithFormParams(String url,
                                                   Map<String, String> headers,
                                                   Map<String, String> cookies,
                                                   Map<String, String> formParams) {
    installSpecification(requestSpecification(), responseSpecification());
    return given()
      .cookies(cookies)
      .headers(headers)
      .formParams(formParams)
      .when()
      .get(url)
      .then()
      .log().all();
  }

  /**
   * [POST] с form parameters (без cookies)
   *
   * @param url        адрес сервиса
   * @param headers    заголовки
   * @param formParams параметры формы
   */
  public ValidatableResponse sendPostWithFormParams(String url,
                                                    Map<String, String> headers,
                                                    Map<String, String> formParams) {
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
   * [POST] с form parameters и cookies
   *
   * @param url        адрес сервиса
   * @param headers    заголовки
   * @param cookies    cookies
   * @param formParams параметры формы
   */
  public ValidatableResponse sendPostWithFormParams(String url,
                                                    Map<String, String> headers,
                                                    Map<String, String> cookies,
                                                    Map<String, String> formParams) {
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

  /**
   * [PUT] с form parameters
   *
   * @param url        адрес сервиса
   * @param headers    заголовки
   * @param cookies    cookies
   * @param formParams параметры формы
   */
  public ValidatableResponse sendPutWithFormParams(String url,
                                                   Map<String, String> headers,
                                                   Map<String, String> cookies,
                                                   Map<String, String> formParams) {
    installSpecification(requestSpecification(), responseSpecification());
    return given()
      .cookies(cookies)
      .headers(headers)
      .formParams(formParams)
      .when()
      .put(url)
      .then()
      .log().all();
  }

  /**
   * [PATCH] с form parameters
   *
   * @param url        адрес сервиса
   * @param headers    заголовки
   * @param cookies    cookies
   * @param formParams параметры формы
   */
  public ValidatableResponse sendPatchWithFormParams(String url,
                                                     Map<String, String> headers,
                                                     Map<String, String> cookies,
                                                     Map<String, String> formParams) {
    installSpecification(requestSpecification(), responseSpecification());
    return given()
      .cookies(cookies)
      .headers(headers)
      .formParams(formParams)
      .when()
      .patch(url)
      .then()
      .log().all();
  }
}