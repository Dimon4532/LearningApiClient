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

import static org.assertj.core.api.Assertions.assertThat;

@Epic("REST Assured Demo")
@Feature("AuthApiClient")
@DisplayName("Тесты AuthApiClient")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthApiClientTest extends BaseApiTest {

  @Test
  @Order(1)
  @Story("Basic Auth")
  @DisplayName("1. GET с Basic Auth - успешная авторизация")
  @Description("Демонстрация GET запроса с корректными credentials")
  void testGetWithBasicAuthSuccess() {
    Response response = authApiClient
      .sendGetWithAuth(
        HTTPBIN_URL + "/basic-auth/user/passwd", 200,
        "user", "passwd", new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    assertThat(response.jsonPath().getBoolean("authenticated")).isTrue();
    assertThat(response.jsonPath().getString("user")).isEqualTo("user");
  }

  @Test
  @Order(2)
  @Story("Basic Auth")
  @DisplayName("2. GET с Basic Auth - неверные credentials")
  @Description("Проверка обработки 401 Unauthorized")
  void testGetWithBasicAuthFailure() {
    Response response = authApiClient
      .sendGet(
        HTTPBIN_URL + "/basic-auth/user/passwd",
        new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    assertThat(response.statusCode()).isEqualTo(401);
  }

  @Test
  @Order(3)
  @Story("Basic Auth")
  @DisplayName("3. POST с Basic Auth")
  @Description("Демонстрация POST запроса с авторизацией")
  void testPostWithBasicAuth() {
    String requestBody = """
      {
          "title": "Secured Post",
          "content": "This post requires authentication"
      }
      """;

    Response response = authApiClient
      .sendPostWithAuth(
        HTTPBIN_URL + "/post", 200, "testuser", "testpass",
        requestBody, new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    assertThat(response.jsonPath().getString("json.title")).isEqualTo("Secured Post");
    assertThat(response.jsonPath().getString("json.content")).isEqualTo("This post requires authentication");
  }

  @Test
  @Order(4)
  @Story("Basic Auth")
  @DisplayName("4. PUT с Basic Auth")
  @Description("Демонстрация PUT запроса с авторизацией")
  void testPutWithBasicAuth() {
    String updateBody = """
      {
          "status": "updated",
          "message": "Resource updated with auth"
      }
      """;

    Response response = authApiClient
      .sendPutWithAuth(
        HTTPBIN_URL + "/put", 200, "admin", "admin123",
        updateBody, new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    assertThat(response.jsonPath().getString("json.status")).isEqualTo("updated");
    assertThat(response.jsonPath().getString("json.message")).isEqualTo("Resource updated with auth");
  }

  @Test
  @Order(5)
  @Story("Basic Auth")
  @DisplayName("5. PATCH с Basic Auth")
  @Description("Демонстрация PATCH запроса с авторизацией")
  void testPatchWithBasicAuth() {
    String patchBody = """
      {
          "field": "patched_value"
      }
      """;

    Response response = authApiClient
      .sendPatchWithAuth(
        HTTPBIN_URL + "/patch", 200, "patcher", "patch123",
        patchBody, new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    assertThat(response.jsonPath().getString("json.field")).isEqualTo("patched_value");
  }

  @Test
  @Order(6)
  @Story("Basic Auth")
  @DisplayName("6. DELETE с Basic Auth")
  @Description("Демонстрация DELETE запроса с авторизацией")
  void testDeleteWithBasicAuth() {
    Response response = authApiClient
      .sendDeleteWithAuth(
        HTTPBIN_URL + "/delete", 200, "deleter", "delete123",
        new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.jsonPath().getString("url")).isEqualTo("https://httpbin.org/delete");
  }

  @Test
  @Order(7)
  @Story("Bearer Token")
  @DisplayName("7. Получение Bearer токена")
  @Description("Демонстрация получения токена через POST запрос")
  void testGetBearerToken() {
    String requestBody = """
      {
          "username": "testuser",
          "password": "testpass"
      }
      """;

    Response response = authApiClient
      .sendPostForToken(
        HTTPBIN_URL + "/post", 200, requestBody,
        new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    assertThat(response.jsonPath().getString("json.username")).isEqualTo("testuser");
    assertThat(response.statusCode()).isEqualTo(200);
  }

  @Test
  @Order(8)
  @Story("Bearer Token")
  @DisplayName("8. GET с Bearer Token")
  @Description("Демонстрация GET запроса с Bearer токеном")
  void testGetWithBearerToken() {
    String token = "my-test-bearer-token-12345";

    Response response = authApiClient
      .sendGetWithBearerToken(
        HTTPBIN_URL + "/bearer", 200, token,
        new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    assertThat(response.jsonPath().getBoolean("authenticated")).isTrue();
    assertThat(response.jsonPath().getString("token")).isEqualTo(token);
  }

  @Test
  @Order(9)
  @Story("Bearer Token")
  @DisplayName("9. POST с Bearer Token")
  @Description("Отправка данных с Bearer токеном")
  void testPostWithBearerToken() {
    String token = "secure-bearer-token-xyz";
    String requestBody = """
      {
          "action": "create",
          "resource": "user-profile"
      }
      """;

    Response response = authApiClient
      .sendPostWithBearerToken(
        HTTPBIN_URL + "/post", 200, token, requestBody,
        new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    assertThat(response.jsonPath().getString("json.action")).isEqualTo("create");
    assertThat(response.jsonPath().getString("json.resource")).isEqualTo("user-profile");
    assertThat(response.jsonPath().getString("headers.Authorization")).contains("Bearer");
  }

  @Test
  @Order(10)
  @Story("Bearer Token")
  @DisplayName("10. PUT с Bearer Token")
  @Description("Обновление данных с Bearer токеном")
  void testPutWithBearerToken() {
    String token = "update-token-abc123";
    String updateBody = """
      {
          "status": "active",
          "updated_by": "token_user"
      }
      """;

    Response response = authApiClient
      .sendPutWithBearerToken(
        HTTPBIN_URL + "/put", 200, token, updateBody,
        new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    assertThat(response.jsonPath().getString("json.status")).isEqualTo("active");
    assertThat(response.jsonPath().getString("json.updated_by")).isEqualTo("token_user");
    assertThat(response.jsonPath().getString("headers.Authorization")).contains("Bearer");
  }

  @Test
  @Order(11)
  @Story("Bearer Token")
  @DisplayName("11. PATCH с Bearer Token")
  @Description("Частичное обновление с Bearer токеном")
  void testPatchWithBearerToken() {
    String token = "patch-token-def456";
    String patchBody = """
      {
          "email": "newemail@example.com"
      }
      """;

    Response response = authApiClient
      .sendPatchWithBearerToken(
        HTTPBIN_URL + "/patch", 200, token, patchBody,
        new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    assertThat(response.jsonPath().getString("json.email")).isEqualTo("newemail@example.com");
    assertThat(response.jsonPath().getString("headers.Authorization")).contains("Bearer");
  }

  @Test
  @Order(12)
  @Story("Bearer Token")
  @DisplayName("12. DELETE с Bearer Token")
  @Description("Удаление ресурса с Bearer токеном")
  void testDeleteWithBearerToken() {
    String token = "delete-token-ghi789";

    Response response = authApiClient
      .sendDeleteWithBearerToken(
        HTTPBIN_URL + "/delete", 200, token,
        new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.jsonPath().getString("headers.Authorization")).contains("Bearer");
    assertThat(response.jsonPath().getString("url")).isEqualTo("https://httpbin.org/delete");
  }

  @Test
  @Order(13)
  @Story("Bearer Token")
  @DisplayName("13. Комплексный сценарий - получение и использование токена")
  @Description("Полный цикл: получение токена и использование его для запросов")
  void testCompleteTokenFlow() {
    String loginBody = """
      {
          "username": "admin",
          "password": "admin123"
      }
      """;

    Response loginResponse = authApiClient
      .sendPostForToken(
        HTTPBIN_URL + "/post", 200, loginBody,
        new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    assertThat(loginResponse.statusCode()).isEqualTo(200);

    String token = "simulated-token-from-login";

    Response dataResponse = authApiClient
      .sendGetWithBearerToken(
        HTTPBIN_URL + "/bearer", 200, token,
        new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    assertThat(dataResponse.jsonPath().getBoolean("authenticated")).isTrue();
    assertThat(dataResponse.jsonPath().getString("token")).isEqualTo(token);
  }

  @Test
  @Order(14)
  @Story("Bearer Token")
  @DisplayName("14. Негативный тест - невалидный Bearer токен")
  @Description("Проверка обработки невалидного токена")
  void testInvalidBearerToken() {
    Response response = authApiClient
      .sendGet(
        HTTPBIN_URL + "/bearer",
        new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    assertThat(response.statusCode()).isEqualTo(401);
  }
}