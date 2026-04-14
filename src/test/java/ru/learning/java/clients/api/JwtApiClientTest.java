package ru.learning.java.clients.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import ru.learning.java.clients.api.base.BaseApiTest;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Feature("JWT Authentication")
@DisplayName("JwtApiClientTest — аутентификация через JWT")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JwtApiClientTest extends BaseApiTest {

  private static WireMockServer wireMock;

  // Реальный JWT, подписанный HMAC256 — для примера
  // Header: {"alg":"HS256","typ":"JWT"}
  // Payload: {"sub":"user1","name":"Test User","iat":..., "exp": now+3600}
  private static final String VALID_JWT =
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9" +
      ".eyJzdWIiOiJ1c2VyMSIsIm5hbWUiOiJUZXN0IFVzZXIiLCJpYXQiOjE3MDAwMDAwMDAsImV4cCI6OTk5OTk5OTk5OX0.signature";

  private static final String EXPIRED_JWT =
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9" +
      ".eyJzdWIiOiJ1c2VyMSIsIm5hbWUiOiJUZXN0IFVzZXIiLCJpYXQiOjE0MDAwMDAwMDAsImV4cCI6MTQwMDAwMDAwMX0.signature";

  private static String MOCK_URL;

  @BeforeAll
  static void setUp() {
    wireMock = new WireMockServer(wireMockConfig().dynamicPort());
    wireMock.start();
    MOCK_URL = "http://localhost:" + wireMock.port();

    jwtClient = new JwtApiClient();

    // Стаб: POST /auth/login → выдаёт JWT
    wireMock.stubFor(post(urlEqualTo("/auth/login"))
      .withRequestBody(containing("testuser"))
      .willReturn(aResponse()
        .withStatus(200)
        .withHeader("Content-Type", "application/json")
        .withBody("{\"token\":\"" + VALID_JWT + "\",\"token_type\": \"Bearer\",\"expires_in\": 3600}")));

    // Стаб: неверные credentials → 401
    wireMock.stubFor(post(urlEqualTo("/auth/login"))
      .withRequestBody(containing("wronguser"))
      .willReturn(aResponse()
        .withStatus(401)
        .withBody("{\"error\": \"Invalid credentials\"}")));

    // Стаб: GET /api/protected с валидным токеном → 200
    wireMock.stubFor(get(urlEqualTo("/api/protected"))
      .atPriority(1)
      .withHeader("Authorization", containing("Bearer " + VALID_JWT))
      .willReturn(aResponse()
        .withStatus(200)
        .withHeader("Content-Type", "application/json")
        .withBody("""
          {"data": "secret content", "user": "user1"}
          """)));

    // Стаб: GET /api/protected без токена → 401
    wireMock.stubFor(get(urlEqualTo("/api/protected"))
      .atPriority(2)
      .withHeader("Authorization", equalTo("Bearer " + EXPIRED_JWT))
      .willReturn(aResponse()
        .withStatus(401)
        .withBody("{\"error\": \"Token expired\"}")));

    // Стаб: GET /api/protected без заголовка → 401
    wireMock.stubFor(get(urlEqualTo("/api/protected"))
      .atPriority(10)
      .willReturn(aResponse()
        .withStatus(401)
        .withBody("{\"error\": \"Unauthorized\"}")));

    // Стаб: POST /api/data с истёкшим токеном → 401
    wireMock.stubFor(post(urlEqualTo("/api/data"))
      .atPriority(2)
      .withHeader("Authorization", equalTo("Bearer " + EXPIRED_JWT))
      .willReturn(aResponse()
        .withStatus(401)
        .withBody("{\"error\": \"Token expired\"}")));

    // Стаб: POST /api/data с валидным токеном → 201
    wireMock.stubFor(post(urlEqualTo("/api/data"))
      .atPriority(1)
      .withHeader("Authorization", equalTo("Bearer " + VALID_JWT))
      .willReturn(aResponse()
        .withStatus(201)
        .withHeader("Content-Type", "application/json")
        .withBody("{\"id\": 42, \"status\": \"created\"}")));
  }

  @AfterAll
  static void tearDown() {
    wireMock.stop();
  }

  // ── Получение токена ──────────────────────────────────────────────────────

  @Test
  @Order(1)
  @Story("Получение JWT токена")
  @DisplayName("1. Получение JWT токена через логин/пароль")
  @Description("POST /auth/login возвращает JWT токен")
  @Severity(SeverityLevel.BLOCKER)
  void testFetchJwtToken() {
    String token = jwtClient.fetchJwtToken(MOCK_URL + "/auth/login", "testuser", "password");
    assertThat(token).isNotBlank();
    assertThat(token.split("\\.")).hasSize(3); // header.payload.signature
  }

  @Test
  @Order(2)
  @Story("Получение JWT токена")
  @DisplayName("2. Структура JWT: три части разделённые точкой")
  @Description("JWT состоит из header.payload.signature в Base64")
  @Severity(SeverityLevel.NORMAL)
  void testJwtStructure() {
    String token = jwtClient.fetchJwtToken(MOCK_URL + "/auth/login", "testuser", "password");
    String[] parts = token.split("\\.");

    assertThat(parts).hasSize(3);
    assertThat(parts[0]).isNotBlank(); // header
    assertThat(parts[1]).isNotBlank(); // payload
    assertThat(parts[2]).isNotBlank(); // signature
  }

  // ── Парсинг и валидация claims ────────────────────────────────────────────

  @Test
  @Order(3)
  @Story("Парсинг JWT claims")
  @DisplayName("3. Парсинг subject (sub) из JWT")
  @Description("Библиотека auth0/java-jwt позволяет декодировать claims без верификации подписи")
  @Severity(SeverityLevel.CRITICAL)
  void testParseJwtSubject() {
    String token = jwtClient.fetchJwtToken(MOCK_URL + "/auth/login", "testuser", "password");

    DecodedJWT decoded = JWT.decode(token);
    assertThat(decoded.getSubject()).isEqualTo("user1");
  }

  @Test
  @Order(4)
  @Story("Парсинг JWT claims")
  @DisplayName("4. Парсинг кастомного claim 'name' из JWT")
  @Severity(SeverityLevel.NORMAL)
  void testParseJwtCustomClaim() {
    String token = jwtClient.fetchJwtToken(MOCK_URL + "/auth/login", "testuser", "password");

    DecodedJWT decoded = JWT.decode(token);
    String name = decoded.getClaim("name").asString();
    assertThat(name).isEqualTo("Test User");
  }

  @Test
  @Order(5)
  @Story("Парсинг JWT claims")
  @DisplayName("5. Парсинг алгоритма подписи из заголовка JWT")
  @Severity(SeverityLevel.NORMAL)
  void testParseJwtAlgorithm() {
    String token = jwtClient.fetchJwtToken(MOCK_URL + "/auth/login", "testuser", "password");

    DecodedJWT decoded = JWT.decode(token);
    assertThat(decoded.getAlgorithm()).isEqualTo("HS256");
  }

  // ── Проверка expiration ───────────────────────────────────────────────────

  @Test
  @Order(6)
  @Story("Проверка expiration")
  @DisplayName("6. Токен имеет поле exp (время истечения)")
  @Description("Валидный токен должен содержать claim 'exp'")
  @Severity(SeverityLevel.CRITICAL)
  void testJwtHasExpirationClaim() {
    String token = jwtClient.fetchJwtToken(MOCK_URL + "/auth/login", "testuser", "password");

    DecodedJWT decoded = JWT.decode(token);
    assertThat(decoded.getExpiresAt()).isNotNull();
  }

  @Test
  @Order(7)
  @Story("Проверка expiration")
  @DisplayName("7. Валидный токен ещё не истёк")
  @Description("exp должен быть в будущем относительно текущего времени")
  @Severity(SeverityLevel.BLOCKER)
  void testJwtNotExpired() {
    String token = jwtClient.fetchJwtToken(MOCK_URL + "/auth/login", "testuser", "password");

    DecodedJWT decoded = JWT.decode(token);
    Date expiresAt = decoded.getExpiresAt();

    assertThat(expiresAt).isNotNull();
    assertThat(expiresAt.toInstant()).isAfter(Instant.now());
  }

  @Test
  @Order(8)
  @Story("Проверка expiration")
  @DisplayName("8. Истёкший токен: exp в прошлом")
  @Description("Демонстрация обнаружения истёкшего токена без обращения к серверу")
  @Severity(SeverityLevel.CRITICAL)
  void testExpiredJwtDetectedLocally() {
    // Декодируем заранее подготовленный истёкший токен
    DecodedJWT decoded = JWT.decode(EXPIRED_JWT);
    Date expiresAt = decoded.getExpiresAt();

    assertThat(expiresAt).isNotNull();
    assertThat(expiresAt.toInstant()).isBefore(Instant.now()); // exp в прошлом
  }

  // ── Использование токена ──────────────────────────────────────────────────

  @Test
  @Order(9)
  @Story("Использование JWT токена")
  @DisplayName("9. GET защищённого ресурса с валидным JWT")
  @Severity(SeverityLevel.BLOCKER)
  void testGetProtectedResourceWithValidJwt() {
    String token = jwtClient.fetchJwtToken(MOCK_URL + "/auth/login", "testuser", "password");
    log.info("Fetched JWT token: {}", token);

    Response response = jwtClient
      .sendGetWithJwt(
        MOCK_URL + "/api/protected", 200, token,
        new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    assertThat(response.jsonPath().getString("data")).isEqualTo("secret content");
    assertThat(response.jsonPath().getString("user")).isEqualTo("user1");
  }

  // ── Негативные тесты ──────────────────────────────────────────────────────

  @Test
  @Order(10)
  @Story("Негативные тесты")
  @DisplayName("10. Истёкший токен → 401 от сервера")
  @Description("Сервер отклоняет запрос с истёкшим JWT")
  @Severity(SeverityLevel.CRITICAL)
  void testExpiredJwtRejectedByServer() {
    jwtClient.sendGetWithJwt(
      MOCK_URL + "/api/protected", 401, EXPIRED_JWT,
      new HashMap<>(), new HashMap<>(), new HashMap<>()
    );
  }

  @Test
  @Order(11)
  @Story("Негативные тесты")
  @DisplayName("11. Неверные credentials → 401 при получении токена")
  @Severity(SeverityLevel.NORMAL)
  void testInvalidCredentials() {
    var response = given()
      .contentType("application/json")
      .body("{\"username\": \"wronguser\", \"password\": \"wrong\"}")
      .when()
      .post(MOCK_URL + "/auth/login")
      .then()
      .statusCode(401)
      .extract().response();

    assertThat(response.jsonPath().getString("error")).isEqualTo("Invalid credentials");
  }

  @Test
  @Order(12)
  @Story("Использование JWT токена")
  @DisplayName("12. POST защищённого ресурса с валидным JWT → 201")
  @Severity(SeverityLevel.BLOCKER)
  void testPostProtectedResourceWithValidJwt() {
    String token = jwtClient.fetchJwtToken(MOCK_URL + "/auth/login", "testuser", "password");
    log.info("Fetched JWT token: {}", token);
    String body = "{\"name\": \"test item\"}";

    Response response = jwtClient
      .sendPostWithJwt(
        MOCK_URL + "/api/data", 201, token, body,
        new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    assertThat(response.jsonPath().getInt("id")).isEqualTo(42);
    assertThat(response.jsonPath().getString("status")).isEqualTo("created");
  }

  @Test
  @Order(13)
  @Story("Негативные тесты")
  @DisplayName("13. POST с истёкшим JWT → 401")
  @Severity(SeverityLevel.CRITICAL)
  void testPostWithExpiredJwtRejected() {
    jwtClient.sendPostWithJwt(
      MOCK_URL + "/api/data", 401, EXPIRED_JWT, "{}",
      new HashMap<>(), new HashMap<>(), new HashMap<>()
    );
  }
}