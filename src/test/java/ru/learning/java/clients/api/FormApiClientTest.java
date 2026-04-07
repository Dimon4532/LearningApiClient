package ru.learning.java.clients.api;

import io.qameta.allure.Description;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Epic("REST Assured Demo")
@Feature("FormApiClient")
@DisplayName("Тесты FormApiClient")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FormApiClientTest extends BaseApiTest {

  @Test
  @Order(1)
  @Story("Form Parameters")
  @DisplayName("1. POST с form parameters (без cookies)")
  @Description("Отправка формы в формате application/x-www-form-urlencoded")
  void testPostWithFormParams() {
    Map<String, String> formParams = new HashMap<>();
    formParams.put("username", "testuser");
    formParams.put("password", "testpass");
    formParams.put("email", "test@example.com");

    Response response = formApiClient
      .sendPostWithFormParams(HTTPBIN_URL + "/post", new HashMap<>(), formParams)
      .extract().response();

    assertThat(response.jsonPath().getString("form.username")).isEqualTo("testuser");
    assertThat(response.jsonPath().getString("form.password")).isEqualTo("testpass");
    assertThat(response.jsonPath().getString("form.email")).isEqualTo("test@example.com");
  }

  @Test
  @Order(2)
  @Story("Form Parameters")
  @DisplayName("2. POST с form parameters и cookies")
  @Description("Отправка формы с cookies")
  void testPostWithFormParamsAndCookies() {
    Map<String, String> formParams = new HashMap<>();
    formParams.put("action", "login");
    formParams.put("remember", "true");

    Map<String, String> cookies = new HashMap<>();
    cookies.put("session_id", "abc123");
    cookies.put("user_token", "xyz789");

    Response response = formApiClient
      .sendPostWithFormParams(HTTPBIN_URL + "/post", new HashMap<>(), cookies, formParams)
      .extract().response();

    assertThat(response.jsonPath().getString("form.action")).isEqualTo("login");
    assertThat(response.jsonPath().getString("form.remember")).isEqualTo("true");

    String cookieHeader = response.jsonPath().getString("headers.Cookie");
    if (cookieHeader != null) {
      assertThat(cookieHeader).contains("session_id");
    }
  }

  @Test
  @Order(3)
  @Story("Form Parameters")
  @DisplayName("3. GET с form parameters")
  @Description("GET запрос с form parameters в query string")
  void testGetWithFormParams() {
    Map<String, String> formParams = new HashMap<>();
    formParams.put("search", "rest-assured");
    formParams.put("category", "testing");

    Map<String, String> cookies = new HashMap<>();
    cookies.put("preferences", "dark_mode");

    Response response = formApiClient
      .sendGetWithFormParams(HTTPBIN_URL + "/get", new HashMap<>(), cookies, formParams)
      .extract().response();

    assertThat(response.jsonPath().getString("args.search")).isEqualTo("rest-assured");
    assertThat(response.jsonPath().getString("args.category")).isEqualTo("testing");
  }

  @Test
  @Order(4)
  @Story("Form Parameters")
  @DisplayName("4. PUT с form parameters")
  @Description("PUT запрос с form-encoded данными")
  void testPutWithFormParams() {
    Map<String, String> formParams = new HashMap<>();
    formParams.put("name", "Updated Name");
    formParams.put("status", "active");

    Map<String, String> cookies = new HashMap<>();
    cookies.put("auth_token", "token123");

    Response response = formApiClient
      .sendPutWithFormParams(HTTPBIN_URL + "/put", new HashMap<>(), cookies, formParams)
      .extract().response();

    assertThat(response.jsonPath().getString("form.name")).isEqualTo("Updated Name");
    assertThat(response.jsonPath().getString("form.status")).isEqualTo("active");
  }

  @Test
  @Order(5)
  @Story("Form Parameters")
  @DisplayName("5. PUT с form params — обновление данных сотрудника")
  void testPutFormParamsEmployee() {
    Map<String, String> formParams = new HashMap<>();
    formParams.put("role", "senior");
    formParams.put("department", "QA");

    formApiClient.sendPutWithFormParams(HTTPBIN_URL + "/put", new HashMap<>(), new HashMap<>(), formParams)
                 .assertThat()
                 .statusCode(200)
                 .body("form.role", equalTo("senior"))
                 .body("form.department", equalTo("QA"));
  }

  @Test
  @Order(6)
  @Story("Form Parameters")
  @DisplayName("6. PUT с form params и session cookie")
  void testPutFormParamsWithSession() {
    Map<String, String> formParams = new HashMap<>();
    formParams.put("role", "senior");
    formParams.put("department", "QA");

    Map<String, String> cookies = new HashMap<>();
    cookies.put("session_id", "sess-abc-999");

    Response response = formApiClient
      .sendPutWithFormParams(HTTPBIN_URL + "/put", new HashMap<>(), cookies, formParams)
      .extract().response();

    assertThat(response.jsonPath().getString("form.role")).isEqualTo("senior");
    assertThat(response.jsonPath().getString("form.department")).isEqualTo("QA");
    assertThat(response.jsonPath().getString("headers.Cookie")).contains("session_id");
  }

  @Test
  @Order(7)
  @Story("Form Parameters")
  @DisplayName("7. PATCH с form parameters")
  @Description("PATCH запрос с form-encoded данными")
  void testPatchWithFormParams() {
    Map<String, String> formParams = new HashMap<>();
    formParams.put("field_to_update", "new_value");

    Map<String, String> cookies = new HashMap<>();
    cookies.put("session", "active_session");

    Response response = formApiClient
      .sendPatchWithFormParams(HTTPBIN_URL + "/patch", new HashMap<>(), cookies, formParams)
      .extract().response();

    assertThat(response.jsonPath().getString("form.field_to_update")).isEqualTo("new_value");
  }
}