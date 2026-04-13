package ru.learning.java.clients.api;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import ru.learning.java.clients.api.base.BaseApiTest;
import ru.learning.java.models.OAuthTokenResponse;

import java.time.Instant;
import java.util.HashMap;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.unauthorized;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Feature("OAuth 2.0 Authentication")
@DisplayName("Тесты OAuthApiClientTest - аутентификация через OAuth 2.0")
public class OAuthApiClientTest extends BaseApiTest {
  private static WireMockServer wireMock;
  private static String TOKEN_URL;
  private static String MOCK_BASE_URL;

  // Токен, который будет возвращать наш мок-сервер
  private static final String MOCK_ACCESS_TOKEN = "mock-access-token-abc123";
  private static final String MOCK_REFRESH_TOKEN = "mock-refresh-token-xyz789";
  private static final String CLIENT_ID = "test-client";
  private static final String CLIENT_SECRET = "test-secret";

  @BeforeAll
  static void startWireMock() {
    wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
    wireMock.start();

    MOCK_BASE_URL = "http://localhost:" + wireMock.port();
    TOKEN_URL = MOCK_BASE_URL + "/oauth/token";

    // ── Стаб: Client Credentials grant ──────────────────────────────────
    wireMock.stubFor(post(urlEqualTo("/oauth/token"))
      .withRequestBody(containing("grant_type=client_credentials"))
      .willReturn(okJson("""
        {
            "access_token":  "%s",
            "token_type":    "Bearer",
            "expires_in":    3600,
            "scope":         "read"
        }
        """.formatted(MOCK_ACCESS_TOKEN))));

    // ── Стаб: Resource Owner Password grant ─────────────────────────────
    wireMock.stubFor(post(urlEqualTo("/oauth/token"))
      .withRequestBody(containing("grant_type=password"))
      .willReturn(okJson("""
        {
            "access_token":  "%s",
            "refresh_token": "%s",
            "token_type":    "Bearer",
            "expires_in":    3600,
            "scope":         "read write"
        }
        """.formatted(MOCK_ACCESS_TOKEN, MOCK_REFRESH_TOKEN))));

    // ── Стаб: Refresh Token grant ────────────────────────────────────────
    wireMock.stubFor(post(urlEqualTo("/oauth/token"))
      .withRequestBody(containing("grant_type=refresh_token"))
      .willReturn(okJson("""
        {
            "access_token": "refreshed-token-new456",
            "token_type":   "Bearer",
            "expires_in":   3600
        }
        """)));

    // ── Стаб: защищённый ресурс (принимает Bearer токен) ────────────────
    wireMock.stubFor(get(urlEqualTo("/api/resource"))
      .withHeader("Authorization", equalTo("Bearer " + MOCK_ACCESS_TOKEN))
      .willReturn(okJson("""
        {"id": 1, "data": "protected content"}
        """)));

    // ── Стаб: защищённый ресурс без токена → 401 ────────────────────────
    wireMock.stubFor(get(urlEqualTo("/api/resource"))
      .withHeader("Authorization", absent())
      .willReturn(unauthorized()));

    // ── Стаб: невалидные credentials → 401 ──────────────────────────────
    wireMock.stubFor(post(urlEqualTo("/oauth/token"))
      .withRequestBody(containing("client_secret=wrong-secret"))
      .willReturn(unauthorized().withBody("""
        {"error": "invalid_client"}
        """)));
  }

  @AfterAll
  static void stopWireMock() {
    wireMock.stop();
  }

  // ── Client Credentials ───────────────────────────────────────────────────

  @Test
  @Order(1)
  @Story("Client Credentials")
  @DisplayName("1. Получение токена через Client Credentials grant")
  @Description("POST на /oauth/token с grant_type=client_credentials")
  void testFetchTokenClientCredentials() {
    String token = oAuthClient.fetchTokenClientCredentials(
      TOKEN_URL, CLIENT_ID, CLIENT_SECRET, "read"
    );
    assertThat(token).isEqualTo(MOCK_ACCESS_TOKEN);
  }

  @Test
  @Order(2)
  @Story("Client Credentials")
  @DisplayName("2. Десериализация полного ответа токена в модель")
  @Description("Проверяем все поля OAuthTokenResponse")
  void testTokenResponseDeserialization() {
    Response response = oAuthClient
      .sendPostForToken(
        TOKEN_URL, 200,
        "grant_type=client_credentials&client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET,
        new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    OAuthTokenResponse tokenResponse = response.as(OAuthTokenResponse.class);

    assertThat(tokenResponse.getAccessToken()).isEqualTo(MOCK_ACCESS_TOKEN);
    assertThat(tokenResponse.getTokenType()).isEqualToIgnoringCase("Bearer");
    assertThat(tokenResponse.getExpiresIn()).isEqualTo(3600L);
    assertThat(tokenResponse.getScope()).isEqualTo("read");
  }

  @Test
  @Order(3)
  @Story("Client Credentials")
  @DisplayName("3. Токен ещё не истёк сразу после получения")
  void testTokenNotExpiredRightAfterIssue() {
    Response response = oAuthClient
      .sendPostForToken(
        TOKEN_URL, 200,
        "grant_type=client_credentials&client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET,
        new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    OAuthTokenResponse tokenResponse = response.as(OAuthTokenResponse.class);
    assertThat(tokenResponse.isExpired(Instant.now())).isFalse();
  }

  // ── Resource Owner Password ──────────────────────────────────────────────

  @Test
  @Order(4)
  @Story("Resource Owner Password")
  @DisplayName("4. Получение токена через Resource Owner Password grant")
  void testFetchTokenPassword() {
    String token = oAuthClient.fetchTokenPassword(
      TOKEN_URL, CLIENT_ID, CLIENT_SECRET, "user", "password", "read write"
    );
    assertThat(token).isEqualTo(MOCK_ACCESS_TOKEN);
  }

  @Test
  @Order(5)
  @Story("Resource Owner Password")
  @DisplayName("5. Ответ содержит refresh_token при Password grant")
  void testPasswordGrantReturnsRefreshToken() {
    Response response = oAuthClient
      .sendPostForToken(
        TOKEN_URL, 200,
        "grant_type=password&client_id=" + CLIENT_ID + "&username=user&password=pass",
        new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    assertThat(response.jsonPath().getString("refresh_token")).isEqualTo(MOCK_REFRESH_TOKEN);
  }

  // ── Refresh Token ────────────────────────────────────────────────────────

  @Test
  @Order(6)
  @Story("Refresh Token")
  @DisplayName("6. Обновление токена через Refresh Token grant")
  void testRefreshToken() {
    String newToken = oAuthClient.refreshToken(
      TOKEN_URL, CLIENT_ID, CLIENT_SECRET, MOCK_REFRESH_TOKEN
    );
    assertThat(newToken).isEqualTo("refreshed-token-new456");
  }

  // ── Использование токена ─────────────────────────────────────────────────

  @Test
  @Order(7)
  @Story("Использование токена")
  @DisplayName("7. GET защищённого ресурса с валидным токеном")
  void testGetProtectedResourceWithToken() {
    ValidatableResponse response = oAuthClient.sendGetWithBearerToken(
      MOCK_BASE_URL + "/api/resource", 200, MOCK_ACCESS_TOKEN,
      new HashMap<>(), new HashMap<>(), new HashMap<>()
    );
    response.body("id", org.hamcrest.Matchers.equalTo(1));
    response.body("data", org.hamcrest.Matchers.equalTo("protected content"));
  }

  @Test
  @Order(8)
  @Story("Использование токена")
  @DisplayName("8. Полный OAuth flow: получить токен → использовать для запроса")
  @Description("Client Credentials → access_token → защищённый GET")
  void testFullOAuthFlow() {
    // Шаг 1: получаем токен
    String token = oAuthClient.fetchTokenClientCredentials(
      TOKEN_URL, CLIENT_ID, CLIENT_SECRET, "read"
    );
    assertThat(token).isNotBlank();

    // Шаг 2: используем токен для запроса к ресурсу
    Response response = oAuthClient.sendGetWithBearerToken(
      MOCK_BASE_URL + "/api/resource", 200, token,
      new HashMap<>(), new HashMap<>(), new HashMap<>()
    ).extract().response();

    assertThat(response.jsonPath().getString("data")).isEqualTo("protected content");
  }

  @Test
  @Order(9)
  @Story("Использование токена")
  @DisplayName("9. Автоматический OAuth flow в одном методе sendGetWithOAuth")
  void testSendGetWithOAuth() {
    ValidatableResponse response = oAuthClient.sendGetWithOAuth(
      MOCK_BASE_URL + "/api/resource", 200,
      TOKEN_URL, CLIENT_ID, CLIENT_SECRET, "read",
      new HashMap<>(), new HashMap<>(), new HashMap<>()
    );
    response.body("data", org.hamcrest.Matchers.equalTo("protected content"));
  }

  // ── Негативные тесты ─────────────────────────────────────────────────────

  @Test
  @Order(10)
  @Story("Негативные тесты")
  @DisplayName("10. Запрос к защищённому ресурсу без токена → 401")
  void testGetProtectedResourceWithoutToken() {
    oAuthClient.sendGet(
      MOCK_BASE_URL + "/api/resource", new HashMap<>(),
      new HashMap<>(), new HashMap<>(), new HashMap<>()
    );
  }

  @Test
  @Order(11)
  @Story("Негативные тесты")
  @DisplayName("11. Получение токена с неверным client_secret → 401")
  void testFetchTokenWithWrongSecret() {
    oAuthClient.sendPostForToken(
      TOKEN_URL, 401,
      "grant_type=client_credentials&client_id=" + CLIENT_ID + "&client_secret=wrong-secret",
      new HashMap<>(), new HashMap<>(), new HashMap<>()
    );
  }
}