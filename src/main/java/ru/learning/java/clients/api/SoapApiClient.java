package ru.learning.java.clients.api;

import io.restassured.response.ValidatableResponse;
import ru.learning.java.clients.api.base.Specification;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class SoapApiClient extends Specification {

  /**
   * Отправка SOAP-запроса (POST с text/xml)
   *
   * @param url          адрес сервиса
   * @param headers      заголовки запроса
   * @param soapAction   действие SOAP
   * @param soapEnvelope тело SOAP запроса
   */
  public ValidatableResponse sendSoapRequest(
    String url, String soapEnvelope,
    String soapAction, Map<String, String> headers
  ) {
    return given()
      .spec(requestSpecification())
      .contentType("text/xml; charset=utf-8")
      .header("SOAPAction", soapAction)
      .headers(headers)
      .body(soapEnvelope)
      .when()
      .post(url)
      .then()
      .spec(responseSpecification())
      .log().all();
  }
}