package ru.learning.java.clients.api;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import ru.learning.java.clients.api.base.BaseApiTest;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasXPath;

@Epic("REST Assured Demo")
@Feature("SoapApiClient")
@DisplayName("Тесты SoapApiClient")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SoapApiClientTest extends BaseApiTest {

  @Test
  @Order(1)
  @Story("SOAP")
  @DisplayName("1. SOAP — конвертация числа в слова")
  void testSoapNumberToWords() {
    String envelope = """
      <?xml version="1.0" encoding="utf-8"?>
      <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
        <soap:Body>
          <NumberToWords xmlns="http://www.dataaccess.com/webservicesserver/">
            <ubiNum>42</ubiNum>
          </NumberToWords>
        </soap:Body>
      </soap:Envelope>
      """;

    Response response = soapApiClient
      .sendSoapRequest(
        SOAP_URL, envelope,
        "http://www.dataaccess.com/webservicesserver/NumberToWords",
        new HashMap<>()
      )
      .extract().response();

    String result = response.xmlPath()
                            .getString("Envelope.Body.NumberToWordsResponse.NumberToWordsResult");

    assertThat(result).containsIgnoringCase("forty");
  }

  @Test
  @Order(2)
  @Story("SOAP")
  @DisplayName("2. SOAP — валидация ответа через XPath")
  void testSoapResponseWithXPath() {
    String envelope = """
      <?xml version="1.0" encoding="utf-8"?>
      <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
        <soap:Body>
          <NumberToDollars xmlns="http://www.dataaccess.com/webservicesserver/">
            <dNum>100</dNum>
          </NumberToDollars>
        </soap:Body>
      </soap:Envelope>
      """;

    soapApiClient.sendSoapRequest(
                   SOAP_URL, envelope,
                   "http://www.dataaccess.com/webservicesserver/NumberToDollars",
                   new HashMap<>()
                 )
                 .assertThat()
                 .statusCode(200)
                 .body(hasXPath("//*[local-name()='NumberToDollarsResult']"));
  }
}