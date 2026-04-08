package ru.learning.java.clients.api.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import ru.learning.java.clients.api.ApiClient;
import ru.learning.java.clients.api.ApiKeyClient;
import ru.learning.java.clients.api.AuthApiClient;
import ru.learning.java.clients.api.FormApiClient;
import ru.learning.java.clients.api.MultipartApiClient;
import ru.learning.java.clients.api.OAuthApiClient;
import ru.learning.java.clients.api.SoapApiClient;

public abstract class BaseApiTest {

  protected static ApiClient apiClient;
  protected static AuthApiClient authApiClient;
  protected static FormApiClient formApiClient;
  protected static MultipartApiClient multipartApiClient;
  protected static SoapApiClient soapApiClient;
  protected static OAuthApiClient oAuthClient;
  protected static ApiKeyClient apiKeyClient;
  protected static ObjectMapper objectMapper;

  protected static final String BASE_URL = "https://jsonplaceholder.typicode.com";
  protected static final String HTTPBIN_URL = "https://httpbin.org";
  protected static final String SOAP_URL =
    "https://www.dataaccess.com/webservicesserver/NumberConversion.wso";

  @BeforeAll
  static void setUpBase() {
    apiClient = new ApiClient();
    authApiClient = new AuthApiClient();
    formApiClient = new FormApiClient();
    multipartApiClient = new MultipartApiClient();
    soapApiClient = new SoapApiClient();
    objectMapper = new ObjectMapper();
    oAuthClient = new OAuthApiClient();
    apiKeyClient = new ApiKeyClient();
  }

  @AfterEach
  void resetSpecifications() {
    RestAssured.reset();
  }
}