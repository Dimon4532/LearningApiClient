package ru.learning.java.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Getter;

/**
 * Класс для работы с конфигурацией приложения
 */
@Getter
public class AppConfig {
  private static AppConfig instance;
  private final Config config;

  private final String baseUrl;
  private final int timeout;
  private final String keystore;
  private final String truststore;
  private final String keystorePassword;
  private final String truststorePassword;

  private AppConfig() {
    this.config = ConfigFactory.load();
    this.baseUrl = config.getString("api.baseUrl");
    this.timeout = config.getInt("api.timeout");
    this.keystore = config.getString("api.keystore");
    this.truststore = config.getString("api.truststore");
    this.keystorePassword = config.getString("api.keystorePassword");
    this.truststorePassword = config.getString("api.truststorePassword");
  }

  public static AppConfig getInstance() {
    if (instance == null) {
      instance = new AppConfig();
    }
    return instance;
  }

  public String apiKeystore() {
    return keystore.isEmpty() ? null : keystore;
  }

  public String apiTruststore() {
    return truststore.isEmpty() ? null : truststore;
  }

  public String apiKeystorePassword() {
    return keystorePassword;
  }

  public String apiTruststorePassword() {
    return truststorePassword;
  }
}