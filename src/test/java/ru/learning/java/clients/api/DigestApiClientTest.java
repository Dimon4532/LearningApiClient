package ru.learning.java.clients.api;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.learning.java.clients.api.base.BaseApiTest;
import ru.learning.java.models.Post;

import java.util.HashMap;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Feature("Digest Authentication")
@DisplayName("DigestApiClientTest — аутентификация через HTTP Digest")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DigestApiClientTest extends BaseApiTest {

  // Реальный публичный сервис с поддержкой Digest
  private static final String HTTPBIN_BASE = "https://httpbin.org";
  private static final String HTTPBINGO_BASE = "https://httpbingo.org";
  private static final String DIGEST_USER = "user";
  private static final String DIGEST_PASS = "passwd";

  // WireMock для негативных и контролируемых сценариев
  private static WireMockServer wireMock;
  private static String MOCK_URL;

  @BeforeAll
  static void setUp() {
    wireMock = new WireMockServer(wireMockConfig()
      .dynamicPort()
      .bindAddress("127.0.0.1"));
    wireMock.start();
    MOCK_URL = "http://127.0.0.1:" + wireMock.port();


    // Стаб: первый запрос без Authorization → 401 + challenge
    wireMock.stubFor(get(urlEqualTo("/digest-protected"))
      .atPriority(2)
      .withHeader("Authorization", notMatching("Digest.*"))
      .willReturn(aResponse()
        .withStatus(401)
        .withHeader(
          "WWW-Authenticate",
          "Digest realm=\"test@wiremock\", " +
            "qop=\"auth\", " +
            "nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", " +
            "opaque=\"5ccc069c403ebaf9f0171e9517f40e41\""
        )));

    // Стаб: второй запрос с заголовком Digest → 200
    wireMock.stubFor(get(urlEqualTo("/digest-protected"))
      .atPriority(1)
      .withHeader("Authorization", containing("Digest"))
      .willReturn(aResponse()
        .withStatus(200)
        .withHeader("Content-Type", "application/json")
        .withBody("{\"authenticated\": true, \"user\": \"" + DIGEST_USER + "\"}")));

    // Стаб: POST без Digest → 401 + challenge
    wireMock.stubFor(post(urlEqualTo("/digest-protected/data"))
      .atPriority(2)
      .withHeader("Authorization", notMatching("Digest.*"))
      .willReturn(aResponse()
        .withStatus(401)
        .withHeader(
          "WWW-Authenticate",
          "Digest realm=\"test@wiremock\", " +
            "qop=\"auth\", " +
            "nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c094\", " +
            "opaque=\"5ccc069c403ebaf9f0171e9517f40e42\""
        )));

    // Стаб: POST с Digest → 201 + JSON
    wireMock.stubFor(post(urlEqualTo("/digest-protected/data"))
      .atPriority(1)
      .withHeader("Authorization", containing("Digest"))
      .willReturn(aResponse()
        .withStatus(201)
        .withHeader("Content-Type", "application/json")
        .withBody("{\"id\": 42, \"status\": \"created\"}")));

    // Стаб: PUT без Digest → 401 + challenge
    wireMock.stubFor(put(urlPathMatching("/digest-protected/data/\\d+"))
      .atPriority(2)
      .withHeader("Authorization", notMatching("Digest.*"))
      .willReturn(aResponse()
        .withStatus(401)
        .withHeader("WWW-Authenticate",
          "Digest realm=\"test@wiremock\", qop=\"auth\", " +
            "nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c095\", " +
            "opaque=\"5ccc069c403ebaf9f0171e9517f40e43\"")));

    // Стаб: PUT с Digest → 200 + JSON
    wireMock.stubFor(put(urlPathMatching("/digest-protected/data/\\d+"))
      .atPriority(1)
      .withHeader("Authorization", containing("Digest"))
      .willReturn(aResponse()
        .withStatus(200)
        .withHeader("Content-Type", "application/json")
        .withBody("{\"status\": \"updated\"}")));
  }

  @AfterAll
  static void tearDown() {
    if (wireMock != null && wireMock.isRunning()) {
      wireMock.stop();
    }
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


  // ── POST с Digest ─────────────────────────────────────────────────────────

  @Test
  @Order(5)
  @Story("Digest Auth")
  @DisplayName("5. WireMock: POST с Digest-заголовком возвращает 201")
  @Severity(SeverityLevel.BLOCKER)
  void testPostWithDigestOnWireMock() {
    String body = "{\"name\": \"test\", \"value\": 100}";

    Response response = digestApiClient
      .sendPostWithDigest(
        MOCK_URL + "/digest-protected/data",
        201, DIGEST_USER, DIGEST_PASS, body,
        new HashMap<>(), new HashMap<>(), new HashMap<>()
      ).extract().response();

    assertThat(response.jsonPath().getInt("id")).isEqualTo(42);
    assertThat(response.jsonPath().getString("status")).isEqualTo("created");
  }

  @Test
  @Order(6)
  @Story("Digest Auth")
  @DisplayName("6. WireMock: POST с пустым телом проходит аутентификацию")
  @Severity(SeverityLevel.NORMAL)
  void testPostWithDigestEmptyBody() {
    String body = "";

    Response response = digestApiClient.sendPostWithDigest(
      MOCK_URL + "/digest-protected/data",
      201, DIGEST_USER, DIGEST_PASS, body,
      new HashMap<>(), new HashMap<>(), new HashMap<>()
    ).extract().response();

    assertThat(response.statusCode()).isEqualTo(201);
    assertThat(response.jsonPath().getInt("id")).isEqualTo(42);
    assertThat(response.jsonPath().getString("status")).isEqualTo("created");
  }

  @Test
  @Order(7)
  @Story("Digest Auth")
  @DisplayName("7. httpbingo.org: POST с Digest и JSON-телом")
  @Severity(SeverityLevel.CRITICAL)
  void testHttpBingoPostWithDigestJsonBody() {
    String body = "{\"message\": \"hello digest\"}";

    Response response = digestApiClient
      .sendPostWithDigest(
        HTTPBINGO_BASE + "/digest-auth/auth/" + DIGEST_USER + "/" + DIGEST_PASS,
        200, DIGEST_USER, DIGEST_PASS, body,
        new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    assertThat(response.jsonPath().getBoolean("authenticated")).isTrue();
    assertThat(response.jsonPath().getString("user")).isEqualTo(DIGEST_USER);
  }

  @Test
  @Order(8)
  @Story("Digest Auth")
  @DisplayName("8. POST с сериализацией Java-объекта через Jackson")
  @Severity(SeverityLevel.NORMAL)
  void testPostDigestWithJacksonObject() throws JsonProcessingException {
    Post newPost = new Post(1L, null, "Digest Jackson Post", "Created from Java object");
    String body = objectMapper.writeValueAsString(newPost);

    Response response = digestApiClient
      .sendPostWithDigest(
        MOCK_URL + "/digest-protected/data",
        201, DIGEST_USER, DIGEST_PASS, body,
        new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    assertThat(response.jsonPath().getInt("id")).isEqualTo(42);
    assertThat(response.jsonPath().getString("status")).isEqualTo("created");

    wireMock.verify(postRequestedFor(urlEqualTo("/digest-protected/data"))
      .withHeader("Authorization", containing("Digest"))
      .withRequestBody(equalToJson(body)));
  }

  @ParameterizedTest(name = "PUT /data/{0} → 200")
  @ValueSource(ints = {1, 42, 100, 999})
  @Order(9)
  @Story("Digest Auth")
  @DisplayName("9. WireMock: PUT с Digest для разных id")
  @Severity(SeverityLevel.NORMAL)
  void testPutWithDigestForVariousIds(int id) {
    String body = "{\"name\": \"item-" + id + "\"}";

    Response response = digestApiClient
      .sendPutWithDigest(
        MOCK_URL + "/digest-protected/data/" + id,
        200, DIGEST_USER, DIGEST_PASS, body,
        new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    assertThat(response.jsonPath().getString("status")).isEqualTo("updated");
  }
}