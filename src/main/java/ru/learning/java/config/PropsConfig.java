package ru.learning.java.config;

public class PropsConfig {
  public static AppConfig getProps() {
    return AppConfig.getInstance();
  }
}