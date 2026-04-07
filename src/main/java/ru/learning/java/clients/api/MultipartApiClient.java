package ru.learning.java.clients.api;

import io.restassured.response.ValidatableResponse;

import java.io.File;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class MultipartApiClient extends ApiClient {

  /**
   * [POST] загрузка файла через multipart/form-data
   *
   * @param url        адрес сервиса
   * @param statusCode ожидаемый статус код
   * @param file       файл для загрузки
   * @param fieldName  имя поля формы
   * @param headers    заголовки
   */
  public ValidatableResponse uploadFile(String url,
                                        int statusCode,
                                        File file,
                                        String fieldName,
                                        Map<String, String> headers) {
    installSpecification(requestSpecification(), responseSpecification());
    return given()
      .headers(headers)
      .multiPart(fieldName, file)
      .when()
      .post(url)
      .then()
      .assertThat().statusCode(statusCode)
      .log().all();
  }

  /**
   * [POST] загрузка файла + дополнительные поля формы
   */
  public ValidatableResponse uploadFileWithFormData(String url,
                                                    int statusCode,
                                                    File file,
                                                    String fieldName,
                                                    Map<String, String> formFields,
                                                    Map<String, String> headers) {
    installSpecification(requestSpecification(), responseSpecification());
    var request = given().headers(headers).multiPart(fieldName, file);
    formFields.forEach(request::multiPart);
    return request.when().post(url)
      .then().assertThat().statusCode(statusCode).log().all();
  }
}