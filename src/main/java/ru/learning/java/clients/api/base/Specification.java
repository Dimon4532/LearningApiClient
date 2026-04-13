
package ru.learning.java.clients.api.base;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static ru.learning.java.config.PropsConfig.getProps;

public abstract class Specification {

  private static final String KEYSTORE = getProps().apiKeystore();
  private static final String TRUSTSTORE = getProps().apiTruststore();
  private static final String KEYSTORE_PSW = getProps().apiKeystorePassword();
  private static final String TRUSTSTORE_PSW = getProps().apiTruststorePassword();
  private static final String BASE_URL = getProps().getBaseUrl();

  /**
   * Базовая спецификация запроса с настройками по умолчанию
   */
  public RequestSpecification requestSpecification() {
    RequestSpecBuilder builder = new RequestSpecBuilder()
      .setBaseUri(BASE_URL)
      .setContentType(ContentType.JSON)
      .setAccept(ContentType.JSON)
      .log(LogDetail.ALL);

    if (KEYSTORE != null && !KEYSTORE.isEmpty()) {
      builder.setKeyStore(KEYSTORE, KEYSTORE_PSW);
    }
    if (TRUSTSTORE != null && !TRUSTSTORE.isEmpty()) {
      builder.setTrustStore(TRUSTSTORE, TRUSTSTORE_PSW);
    }

    return builder.build();
  }

  /**
   * Базовая спецификация ответа
   */
  public ResponseSpecification responseSpecification() {
    return new ResponseSpecBuilder()
      .log(LogDetail.ALL)
      .build();
  }
}