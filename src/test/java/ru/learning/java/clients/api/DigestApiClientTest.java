package ru.learning.java.clients.api;

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

import java.util.HashMap;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Feature("Digest Authentication")
@DisplayName("DigestApiClientTest — аутентификация через HTTP Digest")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DigestApiClientTest extends BaseApiTest {

  // Реальный публичный сервис с поддержкой Digest
  private static final String HTTPBIN_BASE = "https://httpbin.org";
  private static final String DIGEST_USER  = "user";
  private static final String DIGEST_PASS  = "passwd";

  // WireMock для негативных и контролируемых сценариев
  private static WireMockServer wireMock;
  private static String MOCK_URL;

  @BeforeAll
  static void setUp() {
    digestApiClient = new DigestApiClient();

    wireMock = new WireMockServer(wireMockConfig().dynamicPort());
    wireMock.start();
    MOCK_URL = "http://localhost:" + wireMock.port();

    // Стаб: первый запрос без Authorization → 401 + challenge
    wireMock.stubFor(get(urlEqualTo("/digest-protected"))
      .atPriority(2)
      .withHeader("Authorization", notMatching("Digest.*"))
      .willReturn(aResponse()
        .withStatus(401)
        .withHeader("WWW-Authenticate",
          "Digest realm=\"test@wiremock\", " +
            "qop=\"auth\", " +
            "nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", " +
            "opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"")));

    // Стаб: второй запрос с заголовком Digest → 200
    wireMock.stubFor(get(urlEqualTo("/digest-protected"))
      .atPriority(1)
      .withHeader("Authorization", containing("Digest"))
      .willReturn(aResponse()
        .withStatus(200)
        .withHeader("Content-Type", "application/json")
        .withBody("{\"authenticated\": true, \"user\": \"" + DIGEST_USER + "\"}")));
  }

  @AfterAll
  static void tearDown() {
    wireMock.stop();
  }

  // ── Реальный сервис httpbin.org ───────────────────────────────────────────

  @Test
  @Order(1)
  @Story("Digest Auth")
  @DisplayName("1. Успешный Digest на httpbin.org")
  @Description("Проверяем полный flow Digest Auth: 401 challenge → response с MD5-хэшем")
  @Severity(SeverityLevel.BLOCKER)
  void testHttpBinDigestAuthSuccess() {
    Response response = digestApiClient
      .sendGetWithDigest(
        HTTPBIN_BASE + "/digest-auth/auth/" + DIGEST_USER + "/" + DIGEST_PASS,
        200, DIGEST_USER, DIGEST_PASS,
        new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    assertThat(response.jsonPath().getBoolean("authenticated")).isTrue();
    assertThat(response.jsonPath().getString("user")).isEqualTo(DIGEST_USER);
  }

  @Test
  @Order(2)
  @Story("Digest Auth")
  @DisplayName("2. Неверный пароль → 401 от httpbin")
  @Severity(SeverityLevel.CRITICAL)
  void testHttpBinDigestAuthWrongPassword() {
    digestApiClient.sendGetWithDigest(
      HTTPBIN_BASE + "/digest-auth/auth/" + DIGEST_USER + "/" + DIGEST_PASS,
      401, DIGEST_USER, "wrong-password",
      new HashMap<>(), new HashMap<>(), new HashMap<>()
    );
  }

  // ── WireMock — изолированные тесты ────────────────────────────────────────

  @Test
  @Order(3)
  @Story("Digest Auth")
  @DisplayName("3. WireMock: повторный запрос с Digest-заголовком возвращает 200")
  @Description("Демонстрирует двухшаговый flow: первый 401, второй 200")
  @Severity(SeverityLevel.NORMAL)
  void testDigestFlowOnWireMock() {
    Response response = digestApiClient
      .sendGetWithDigest(
        MOCK_URL + "/digest-protected",
        200, DIGEST_USER, DIGEST_PASS,
        new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    assertThat(response.jsonPath().getBoolean("authenticated")).isTrue();
  }

  @Test
  @Order(4)
  @Story("Digest Auth")
  @DisplayName("4. WireMock: первый запрос содержит challenge")
  @Description("Проверяем, что сервер отвечает 401 + WWW-Authenticate")
  @Severity(SeverityLevel.NORMAL)
  void testDigestChallengeReceived() {
    // Делаем «голый» запрос без Digest, чтобы увидеть challenge
    Response response = io.restassured.RestAssured
      .given()
      .when()
      .get(MOCK_URL + "/digest-protected")
      .then()
      .extract().response();

    assertThat(response.statusCode()).isEqualTo(401);

    String wwwAuth = response.getHeader("WWW-Authenticate");
    assertThat(wwwAuth).startsWith("Digest");
    assertThat(wwwAuth).contains("realm=");
    assertThat(wwwAuth).contains("nonce=");
    assertThat(wwwAuth).contains("qop=");
  }
}