package ru.learning.java.clients.api;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * API клиент для OAuth 2.0 аутентификации
 * Поддерживает: Client Credentials, Resource Owner Password, Refresh Token
 */
public class OAuthApiClient extends AuthApiClient {

  // ── Client Credentials Grant ──────────────────────────────────────────────

  /**
   * Получение токена через Client Credentials grant
   *
   * @param tokenUrl     URL эндпоинта токена (например, /oauth/token)
   * @param clientId     идентификатор клиента
   * @param clientSecret секрет клиента
   * @param scope        запрашиваемые scopes (через пробел), может быть null
   * @return access_token из ответа
   */
  public String fetchTokenClientCredentials(
    String tokenUrl,
    String clientId,
    String clientSecret,
    String scope
  ) {
    RequestSpecification spec = given()
      .contentType("application/x-www-form-urlencoded")
      .formParam("grant_type", "client_credentials")
      .formParam("client_id", clientId)
      .formParam("client_secret", clientSecret);

    if (scope != null) {
      spec.formParam("scope", scope);
    }

    return spec
      .when()
      .post(tokenUrl)
      .then()
      .assertThat().statusCode(200)
      .log().all()
      .extract()
      .jsonPath()
      .getString("access_token");
  }

  // ── Resource Owner Password Grant ────────────────────────────────────────

  /**
   * Получение токена через Resource Owner Password grant
   *
   * @param tokenUrl     URL эндпоинта токена (например, /oauth/token)
   * @param clientId     идентификатор клиента
   * @param clientSecret секрет клиента
   * @param username     логин пользователя
   * @param password     пароль пользователя
   * @param scope        область видимости (scope) для токена
   */
  public String fetchTokenPassword(
    String tokenUrl,
    String clientId,
    String clientSecret,
    String username,
    String password,
    String scope
  ) {
    RequestSpecification spec = given()
      .contentType("application/x-www-form-urlencoded")
      .formParam("grant_type", "password")
      .formParam("client_id", clientId)
      .formParam("client_secret", clientSecret)
      .formParam("username", username)
      .formParam("password", password);

    if (scope != null) {
      spec.formParam("scope", scope);
    }

    return spec
      .when()
      .post(tokenUrl)
      .then()
      .assertThat().statusCode(200)
      .log().all()
      .extract()
      .jsonPath()
      .getString("access_token");
  }

  // ── Refresh Token ─────────────────────────────────────────────────────────

  /**
   * Обновление токена через Refresh Token grant
   *
   * @param tokenUrl     URL эндпоинта токена
   * @param clientId     идентификатор клиента
   * @param clientSecret секрет клиента
   * @param refreshToken обновляемый токен
   */
  public String refreshToken(
    String tokenUrl,
    String clientId,
    String clientSecret,
    String refreshToken
  ) {
    return given()
      .contentType("application/x-www-form-urlencoded")
      .formParam("grant_type", "refresh_token")
      .formParam("client_id", clientId)
      .formParam("client_secret", clientSecret)
      .formParam("refresh_token", refreshToken)
      .when()
      .post(tokenUrl)
      .then()
      .assertThat().statusCode(200)
      .log().all()
      .extract()
      .jsonPath()
      .getString("access_token");
  }

  // ── Полный OAuth flow в одном методе ─────────────────────────────────────

  /**
   * GET запрос с автоматическим получением OAuth токена (Client Credentials)
   * Удобен когда не нужно хранить токен между запросами
   */
  public ValidatableResponse sendGetWithOAuth(
    String url,
    int statusCode,
    String tokenUrl,
    String clientId,
    String clientSecret,
    String scope,
    Map<String, String> headers,
    Map<String, String> pathParams,
    Map<String, String> queryParams
  ) {
    String token = fetchTokenClientCredentials(tokenUrl, clientId, clientSecret, scope);
    return sendGetWithBearerToken(url, statusCode, token, headers, pathParams, queryParams);
  }
}