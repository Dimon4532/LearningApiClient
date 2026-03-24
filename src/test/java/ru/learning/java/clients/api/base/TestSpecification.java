package ru.learning.java.clients.api.base;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

/**
 * Спецификация для тестов с поддержкой Allure
 */
public class TestSpecification extends Specification {

  @Override
  public RequestSpecification requestSpecification() {
    RequestSpecification baseSpec = super.requestSpecification();

    return new RequestSpecBuilder()
      .addRequestSpecification(baseSpec)
      .addFilter(new AllureRestAssured())
      .build();
  }
}