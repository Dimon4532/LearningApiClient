package ru.learning.java.clients.api;

import io.qameta.allure.Description;
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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Feature("API Key Authentication")
@DisplayName("ApiKeyClientTest — аутентификация через API Keys")
public class ApiKeyClientTest extends BaseApiTest {
  private static final String FAKE_API_KEY = "test-api-key-12345";

  // ── API Key через заголовок ───────────────────────────────────────────────

  @Test
  @Order(1)
  @Story("API Key в заголовке")
  @DisplayName("1. GET с API Key в заголовке X-API-Key")
  @Description("Проверяем, что X-API-Key передаётся в заголовке и виден в ответе")
  void testGetWithApiKeyHeader() {
    Response response = apiKeyClient
      .sendGetWithApiKeyHeader(
        HTTPBIN_URL + "/get", 200,
        "X-API-Key", FAKE_API_KEY,
        new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    // HTTPBin возвращает все заголовки в поле headers
    String receivedKey = response.jsonPath().getString("headers.X-Api-Key");
    assertThat(receivedKey).isEqualTo(FAKE_API_KEY);
  }

  @Test
  @Order(2)
  @Story("API Key в заголовке")
  @DisplayName("2. GET с API Key в заголовке Authorization (кастомный формат)")
  @Description("Некоторые API принимают ключ как 'ApiKey <token>' в Authorization")
  void testGetWithApiKeyInAuthorizationHeader() {
    String apiKeyHeaderValue = "ApiKey " + FAKE_API_KEY;

    Response response = apiKeyClient
      .sendGetWithApiKeyHeader(
        HTTPBIN_URL + "/get", 200,
        "Authorization", apiKeyHeaderValue,
        new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    String receivedAuth = response.jsonPath().getString("headers.Authorization");
    assertThat(receivedAuth).isEqualTo(apiKeyHeaderValue);
  }

  @Test
  @Order(3)
  @Story("API Key в заголовке")
  @DisplayName("3. POST с API Key в заголовке X-API-Key")
  @Description("POST запрос с телом и API Key в заголовке")
  void testPostWithApiKeyHeader() {
    String body = """
      {"title": "test post", "body": "content", "userId": 1}
      """;

    Response response = apiKeyClient
      .sendPostWithApiKeyHeader(
        HTTPBIN_URL + "/post", 200,
        "X-API-Key", FAKE_API_KEY,
        body,
        new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    // HTTPBin отражает заголовки и тело запроса
    assertThat(response.jsonPath().getString("headers.X-Api-Key")).isEqualTo(FAKE_API_KEY);
    assertThat(response.jsonPath().getString("data")).contains("test post");
  }

  @Test
  @Order(4)
  @Story("API Key в заголовке")
  @DisplayName("4. Проверка, что без API Key возвращается 401")
  @Description("Негативный тест: запрос без ключа к защищённому ресурсу")
  void testGetWithoutApiKeyReturns401() {
    // httpbin /basic-auth/{user}/{passwd} требует Basic Auth — аналог защищённого ресурса
    // Используем специальный эндпоинт, который вернёт 401 без авторизации
    apiKeyClient
      .sendGetWithApiKeyHeader(
        HTTPBIN_URL + "/basic-auth/user/pass", 401,
        "X-API-Key", "",  // пустой ключ
        new HashMap<>(), new HashMap<>(), new HashMap<>()
      );
  }

  // ── API Key через query-параметр ──────────────────────────────────────────

  @Test
  @Order(5)
  @Story("API Key в query-параметре")
  @DisplayName("5. GET с API Key в query-параметре 'api_key'")
  @Description("Проверяем, что api_key передаётся как query-параметр")
  void testGetWithApiKeyQueryParam() {
    Response response = apiKeyClient
      .sendGetWithApiKeyQuery(
        HTTPBIN_URL + "/get", 200,
        "api_key", FAKE_API_KEY,
        new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    // HTTPBin возвращает query-параметры в поле args
    String receivedKey = response.jsonPath().getString("args.api_key");
    assertThat(receivedKey).isEqualTo(FAKE_API_KEY);
  }

  @Test
  @Order(6)
  @Story("API Key в query-параметре")
  @DisplayName("6. GET с API Key в query-параметре 'key' + дополнительные params")
  @Description("API Key не должен вытеснять другие query-параметры")
  void testGetWithApiKeyQueryParamAlongsideOtherParams() {
    var extraParams = new HashMap<String, String>();
    extraParams.put("userId", "1");
    extraParams.put("_limit", "5");

    Response response = apiKeyClient
      .sendGetWithApiKeyQuery(
        HTTPBIN_URL + "/get", 200,
        "key", FAKE_API_KEY,
        new HashMap<>(), new HashMap<>(), extraParams
      )
      .extract().response();

    // Проверяем, что все параметры дошли
    assertThat(response.jsonPath().getString("args.key")).isEqualTo(FAKE_API_KEY);
    assertThat(response.jsonPath().getString("args.userId")).isEqualTo("1");
    assertThat(response.jsonPath().getString("args._limit")).isEqualTo("5");
  }

  @Test
  @Order(7)
  @Story("API Key в query-параметре")
  @DisplayName("7. GET с API Key в query-параметре 'apikey' (альтернативное имя)")
  @Description("Разные API используют разные имена параметра: apikey, api_key, key, token")
  void testGetWithApiKeyDifferentParamNames() {
    for (String paramName : new String[]{"apikey", "api_key", "key", "token"}) {
      Response response = apiKeyClient
        .sendGetWithApiKeyQuery(
          HTTPBIN_URL + "/get", 200,
          paramName, FAKE_API_KEY,
          new HashMap<>(), new HashMap<>(), new HashMap<>()
        )
        .extract().response();

      assertThat(response.jsonPath().getString("args." + paramName))
        .as("Проверка параметра: " + paramName)
        .isEqualTo(FAKE_API_KEY);
    }
  }

  // ── API Key как Basic Auth (Stripe-style) ─────────────────────────────────

  @Test
  @Order(8)
  @Story("API Key как Basic Auth")
  @DisplayName("8. GET с API Key как Basic Auth username (Stripe-style)")
  @Description("Stripe и ряд других API принимают API Key как username в Basic Auth с пустым паролем")
  void testGetWithApiKeyAsBasicAuth() {
    // HTTPBin /basic-auth/{user}/{passwd} проверяет Basic Auth
    // Используем /get и смотрим на Authorization заголовок
    Response response = apiKeyClient
      .sendGetWithApiKeyAsBasicAuth(
        HTTPBIN_URL + "/get", 200,
        FAKE_API_KEY,
        new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    // REST Assured кодирует в Base64: Authorization: Basic base64(key:)
    String authHeader = response.jsonPath().getString("headers.Authorization");
    assertThat(authHeader).startsWith("Basic ");
    assertThat(authHeader).isNotBlank();
  }

  @Test
  @Order(9)
  @Story("API Key как Basic Auth")
  @DisplayName("9. Stripe-style: заголовок Authorization содержит Base64(apiKey:)")
  @Description("Проверяем, что Base64-encoded значение в заголовке соответствует паттерну Stripe")
  void testApiKeyAsBasicAuthFullCycle() {
    Response response = apiKeyClient
      .sendGetWithApiKeyAsBasicAuth(
        HTTPBIN_URL + "/get", 200,
        FAKE_API_KEY,
        new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    String authHeader = response.jsonPath().getString("headers.Authorization");

    // Декодируем Base64 и проверяем формат "apiKey:" (без пароля — Stripe-style)
    String decoded = new String(
      java.util.Base64.getDecoder().decode(authHeader.replace("Basic ", ""))
    );
    assertThat(decoded).isEqualTo(FAKE_API_KEY + ":");
  }

  // ── Комплексный сценарий ──────────────────────────────────────────────────

  @Test
  @Order(10)
  @Story("Комплексный сценарий")
  @DisplayName("10. Сравнение способов передачи API Key")
  @Description("Оба способа (header и query) доставляют ключ — демонстрация эквивалентности")
  void testHeaderVsQueryParamEquivalence() {
    Response viaHeader = apiKeyClient
      .sendGetWithApiKeyHeader(
        HTTPBIN_URL + "/get", 200,
        "X-API-Key", FAKE_API_KEY,
        new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    Response viaQuery = apiKeyClient
      .sendGetWithApiKeyQuery(
        HTTPBIN_URL + "/get", 200,
        "api_key", FAKE_API_KEY,
        new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    // Оба запроса вернули 200 и ключ дошёл до сервера в ожидаемом месте
    assertThat(viaHeader.jsonPath().getString("headers.X-Api-Key")).isEqualTo(FAKE_API_KEY);
    assertThat(viaQuery.jsonPath().getString("args.api_key")).isEqualTo(FAKE_API_KEY);
    assertThat(viaHeader.statusCode()).isEqualTo(viaQuery.statusCode());
  }
}