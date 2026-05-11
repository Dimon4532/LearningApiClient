package ru.learning.java.clients.api.data_driven;

import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import ru.learning.java.clients.api.base.BaseApiTest;
import ru.learning.java.models.CreateUserRequest;
import ru.learning.java.validation.ModelValidator;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Data-Driven Testing")
@DisplayName("Data-Driven — CSV источник")
class UserCsvDataDrivenTest extends BaseApiTest {

  @ParameterizedTest(name = "[{index}] POST /users {0} <{1}>")
  @CsvFileSource(resources = "/test-data/users.csv", numLinesToSkip = 1)
  @DisplayName("Создание пользователей по данным из CSV")
  void createUserFromCsv(String name, String email, String username) throws Exception {
    CreateUserRequest request = CreateUserRequest.builder()
                                                 .name(name).email(email).username(username).build();

    ModelValidator.requireValid(request);

    Response response = apiClient.sendPost(
      BASE_URL + "/users", 201,
      objectMapper.writeValueAsString(request),
      new HashMap<>(), new HashMap<>(), new HashMap<>()
    ).extract().response();

    assertThat(response.jsonPath().getInt("id")).isPositive();
  }
}
