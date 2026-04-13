package ru.learning.java.clients.api.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import ru.learning.java.clients.api.ApiClient;
import ru.learning.java.clients.api.ApiKeyClient;
import ru.learning.java.clients.api.AuthApiClient;
import ru.learning.java.clients.api.FormApiClient;
import ru.learning.java.clients.api.JwtApiClient;
import ru.learning.java.clients.api.MultipartApiClient;
import ru.learning.java.clients.api.OAuthApiClient;
import ru.learning.java.clients.api.SoapApiClient;

import static ru.learning.java.config.PropsConfigTest.getProps;

public abstract class BaseApiTest {

  protected static ApiClient apiClient;
  protected static ApiKeyClient apiKeyClient;
  protected static AuthApiClient authApiClient;
  protected static FormApiClient formApiClient;
  protected static JwtApiClient jwtClient;
  protected static MultipartApiClient multipartApiClient;
  protected static OAuthApiClient oAuthClient;
  protected static SoapApiClient soapApiClient;

  protected static ObjectMapper objectMapper;

  protected static final String BASE_URL = getProps().getBaseUrl();
  protected static final String HTTPBIN_URL = getProps().getHttpBinUrl();
  protected static final String SOAP_URL = getProps().getSoapUrl();

  @BeforeAll
  static void setUpBase() {
    apiClient = new ApiClient();
    apiKeyClient = new ApiKeyClient();
    authApiClient = new AuthApiClient();
    formApiClient = new FormApiClient();
    jwtClient = new JwtApiClient();
    multipartApiClient = new MultipartApiClient();
    oAuthClient = new OAuthApiClient();
    soapApiClient = new SoapApiClient();

    objectMapper = new ObjectMapper();
  }
}