package ru.learning.java.clients.api;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import ru.learning.java.clients.api.base.BaseApiTest;
import ru.learning.java.models.UserXml;

import java.util.HashMap;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasXPath;

@Epic("REST Assured Demo")
@Feature("XML")
@DisplayName("Тесты работы с XML")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class XmlApiTest extends BaseApiTest {

  @Test
  @Order(1)
  @Story("XML")
  @DisplayName("1. Получение XML и извлечение значения тега")
  void testParseXmlResponse() {
    Response response = apiClient
      .sendGet(HTTPBIN_URL + "/xml", new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>())
      .extract().response();

    String title = response.xmlPath().getString("slideshow.@title");
    assertThat(title).isNotEmpty();

    int slideCount = response.xmlPath().getList("slideshow.slide").size();
    assertThat(slideCount).isGreaterThan(0);
  }

  @Test
  @Order(2)
  @Story("XML")
  @DisplayName("2. Валидация XML через Hamcrest hasXPath")
  void testXmlValidationWithHamcrest() {
    apiClient.sendGet(HTTPBIN_URL + "/xml", new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>())
             .assertThat()
             .statusCode(200)
             .contentType(ContentType.XML)
             .body(hasXPath("/slideshow/@title"))
             .body(hasXPath("//slide"));
  }

  @Test
  @Order(3)
  @Story("XML")
  @DisplayName("3. POST запрос с XML телом")
  void testPostWithXmlBody() {
    String xmlBody = """
      <?xml version="1.0" encoding="UTF-8"?>
      <user>
          <name>John Doe</name>
          <email>john@example.com</email>
      </user>
      """;

    Response response = given()
      .contentType(ContentType.XML)
      .body(xmlBody)
      .post(HTTPBIN_URL + "/post")
      .then().statusCode(200)
      .extract().response();

    assertThat(response.jsonPath().getString("data")).contains("<name>John Doe</name>");
  }

  @Test
  @Order(4)
  @Story("XML")
  @DisplayName("4. POST с JAXB объектом")
  void testPostWithJaxbObject() {
    UserXml user = new UserXml("Jane Smith", "jane@example.com");

    Response response = given()
      .contentType(ContentType.XML)
      .body(user)
      .post(HTTPBIN_URL + "/post")
      .then().statusCode(200)
      .extract().response();

    assertThat(response.jsonPath().getString("data")).contains("Jane Smith");
  }
}