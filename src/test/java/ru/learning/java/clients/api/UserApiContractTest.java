package ru.learning.java.clients.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.learning.java.clients.api.base.BaseApiTest;
import ru.learning.java.models.CreateUserRequest;
import ru.learning.java.models.User;
import ru.learning.java.validation.ModelValidator;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Контрактные тесты: проверяем, что внешний API возвращает данные,
 * соответствующие нашим Bean Validation правилам.
 */
@Feature("Bean Validation (JSR-380)")
@DisplayName("Контрактные тесты — валидация ответа API через JSR-380")
public class UserApiContractTest extends BaseApiTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @Story("Контракт ответа API")
  @DisplayName("GET /users/1 — ответ соответствует Bean Validation модели User")
  void testGetSingleUserMatchesContract() throws Exception {
    Response response = apiClient
      .sendGet(BASE_URL + "/users/1",
        new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>())
      .extract().response();

    User user = objectMapper.readValue(response.asString(), User.class);

    // Все ограничения @NotNull/@NotBlank/@Email/@Positive должны соблюдаться
    assertThat(ModelValidator.validateAndDescribe(user))
      .as("API должен возвращать корректный объект User")
      .isEmpty();
  }

  @Test
  @Story("Контракт ответа API")
  @DisplayName("GET /users — каждый элемент списка валиден")
  void testGetUsersListAllItemsValid() throws Exception {
    Response response = apiClient
      .sendGet(BASE_URL + "/users",
        new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>())
      .extract().response();

    List<User> users = List.of(objectMapper.readValue(response.asString(), User[].class));

    assertThat(users).isNotEmpty();
    users.forEach(user ->
      assertThat(ModelValidator.validateAndDescribe(user))
        .as("Пользователь id=%d должен быть валиден", user.id())
        .isEmpty()
    );
  }

  @Test
  @DisplayName("POST /users — отправляем только валидную модель")
  void testCreateUserWithValidationGuard() throws JsonProcessingException {
    CreateUserRequest request = CreateUserRequest.builder()
                                                 .name("John Doe")
                                                 .email("john@example.com")
                                                 .username("john_doe")
                                                 .build();

    // Падаем ДО HTTP-запроса, если модель невалидна
    ModelValidator.requireValid(request);

    Response response = apiClient.sendPost(
      BASE_URL + "/users", 201,
      objectMapper.writeValueAsString(request),
      new HashMap<>(), new HashMap<>(), new HashMap<>()
    ).extract().response();

    assertThat(response.jsonPath().getInt("id")).isPositive();
  }
}