package ru.learning.java.clients.api;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.learning.java.clients.api.base.BaseApiTest;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasXPath;

@Epic("REST Assured Demo")
@Feature("SoapApiClient")
@DisplayName("Тесты SoapApiClient")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
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

  @Test
  @Order(3)
  @Story("SOAP")
  @DisplayName("3. SOAP — конвертация нуля")
  void testSoapZeroToWords() {
    String envelope = """
    <?xml version="1.0" encoding="utf-8"?>
    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      <soap:Body>
        <NumberToWords xmlns="http://www.dataaccess.com/webservicesserver/">
          <ubiNum>0</ubiNum>
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

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(result).containsIgnoringCase("zero");
  }

  @Test
  @Order(4)
  @Story("SOAP")
  @DisplayName("4. SOAP — конвертация 1000")
  void testSoapThousandToWords() {
    String envelope = """
    <?xml version="1.0" encoding="utf-8"?>
    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      <soap:Body>
        <NumberToWords xmlns="http://www.dataaccess.com/webservicesserver/">
          <ubiNum>1000</ubiNum>
        </NumberToWords>
      </soap:Body>
    </soap:Envelope>
    """;

    soapApiClient
      .sendSoapRequest(
        SOAP_URL, envelope,
        "http://www.dataaccess.com/webservicesserver/NumberToWords",
        new HashMap<>()
      )
      .assertThat()
      .statusCode(200)
      .body(hasXPath("//*[local-name()='NumberToWordsResult']")); // XPath — второй способ валидации
  }

  // ── Утилита ───────────────────────────────────────────────────────────────

  private String buildNumberToWordsEnvelope(int number) {
    return """
    <?xml version="1.0" encoding="utf-8"?>
    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      <soap:Body>
        <NumberToWords xmlns="http://www.dataaccess.com/webservicesserver/">
          <ubiNum>%d</ubiNum>
        </NumberToWords>
      </soap:Body>
    </soap:Envelope>
    """.formatted(number);
  }

  // ── Параметризованный тест ────────────────────────────────────────────────

  @ParameterizedTest(name = "{0} → должно содержать ''{1}''")
  @CsvSource({
    "1,    one",
    "42,   forty",
    "100,  one hundred",
    "999,  nine hundred"
  })
  @Order(5)
  @Story("SOAP")
  @DisplayName("5. SOAP — параметризованная конвертация чисел в слова")
  void testNumberToWordsParameterized(int number, String expectedWord) {
    String envelope = buildNumberToWordsEnvelope(number);

    Response response = soapApiClient
      .sendSoapRequest(
        SOAP_URL, envelope,
        "http://www.dataaccess.com/webservicesserver/NumberToWords",
        new HashMap<>()
      )
      .extract().response();

    String result = response.xmlPath()
                            .getString("Envelope.Body.NumberToWordsResponse.NumberToWordsResult");

    assertThat(result)
      .as("Число %d должно содержать слово '%s'", number, expectedWord)
      .containsIgnoringCase(expectedWord);
  }

  // ── SOAP Fault ────────────────────────────────────────────────────────────

  @Test
  @Order(6)
  @Story("SOAP Fault")
  @DisplayName("6. SOAP — некорректный namespace возвращает Fault")
  void testSoapFaultOnWrongNamespace() {
    // Намеренно используем неверный namespace — сервер не найдёт метод
    String invalidEnvelope = """
    <?xml version="1.0" encoding="utf-8"?>
    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      <soap:Body>
        <NumberToWords xmlns="http://wrong.namespace.example.com/">
          <ubiNum>42</ubiNum>
        </NumberToWords>
      </soap:Body>
    </soap:Envelope>
    """;

    Response response = soapApiClient
      .sendSoapRequest(
        SOAP_URL, invalidEnvelope,
        "http://www.dataaccess.com/webservicesserver/NumberToWords",
        new HashMap<>()
      )
      .extract().response();

    // SOAP Fault может прийти как с 500, так и с 200 — зависит от сервера
    assertThat(response.statusCode()).isIn(200, 500);

    // Главное — в теле должен быть Fault
    String body = response.asString();
    assertThat(body).containsIgnoringCase("Fault");
  }

  @Test
  @Order(7)
  @Story("SOAP Fault")
  @DisplayName("7. SOAP Fault содержит faultstring")
  void testSoapFaultContainsFaultstring() {
    String invalidEnvelope = """
    <?xml version="1.0" encoding="utf-8"?>
    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      <soap:Body>
        <NonExistentMethod xmlns="http://www.dataaccess.com/webservicesserver/">
          <param>value</param>
        </NonExistentMethod>
      </soap:Body>
    </soap:Envelope>
    """;

    Response response = soapApiClient
      .sendSoapRequest(
        SOAP_URL, invalidEnvelope,
        "http://www.dataaccess.com/webservicesserver/NonExistentMethod",
        new HashMap<>()
      )
      .extract().response();

    assertThat(response.statusCode()).isIn(200, 500);

    // Извлекаем faultstring через xmlPath — оба варианта namespace
    String faultString = response.xmlPath().getString("**.find { it.name() == 'faultstring' }");

    // Если xmlPath не сработал — проверяем сырую строку
    if (faultString == null || faultString.isBlank()) {
      assertThat(response.asString()).containsIgnoringCase("fault");
    } else {
      assertThat(faultString).isNotBlank();
      log.info("SOAP Fault message: {}", faultString);
    }
  }
}