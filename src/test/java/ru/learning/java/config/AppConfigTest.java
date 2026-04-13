package ru.learning.java.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Getter;

/**
 * Класс для работы с конфигурацией приложения
 */
@Getter
public class AppConfigTest {
  private static AppConfigTest instance;
  private final Config config;

  private final String baseUrl;
  private final String httpBinUrl;
  private final String soapUrl;
  private final int timeout;

  private AppConfigTest() {
    this.config = ConfigFactory.load();
    this.baseUrl = config.getString("api.url.base");
    this.httpBinUrl = config.getString("api.url.httpbin");
    this.soapUrl = config.getString("api.url.soap");
    this.timeout = config.getInt("api.timeout");
  }

  public static AppConfigTest getInstance() {
    if (instance == null) {
      instance = new AppConfigTest();
    }
    return instance;
  }
}