package ru.learning.java.clients.api;

import io.restassured.response.ValidatableResponse;
import ru.learning.java.clients.api.base.Specification;

import java.util.Map;
import static io.restassured.RestAssured.given;

public class SoapApiClient extends Specification {

  /**
   * Отправка SOAP-запроса (POST с text/xml)
   */
  public ValidatableResponse sendSoapRequest(String url,
                                             String soapEnvelope,
                                             String soapAction,
                                             Map<String, String> headers) {
    installSpecification(requestSpecification(), responseSpecification());
    return given()
      .contentType("text/xml; charset=utf-8")
      .header("SOAPAction", soapAction)
      .headers(headers)
      .body(soapEnvelope)
      .when()
      .post(url)
      .then()
      .log().all();
  }
}