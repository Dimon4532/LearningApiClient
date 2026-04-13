package ru.learning.java.allure;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import ru.learning.java.clients.api.base.SpecificationTest;
import ru.learning.java.models.Comment;
import ru.learning.java.models.CreateUserRequest;
import ru.learning.java.models.Post;
import ru.learning.java.models.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Тесты с интеграцией Allure через TestSpecification
 * Демонстрация использования кастомной спецификации с автоматическим логированием в Allure
 */
@Epic("Allure Integration Demo")
@Feature("TestSpecification Usage")
@DisplayName("Тесты с Allure отчетами через TestSpecification")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AllureIntegrationTest extends SpecificationTest {

  private static final String BASE_URL = "https://jsonplaceholder.typicode.com";

  @Test
  @Order(1)
  @Story("Allure отчеты")
  @DisplayName("1. GET запрос с автоматическим Allure логированием")
  @Description("Все детали запроса и ответа автоматически попадают в Allure отчет")
  @Severity(SeverityLevel.CRITICAL)
  void testGetWithAllureLogging() {
    Response response = given()
      .when()
      .get(BASE_URL + "/users/1")
      .then()
      .statusCode(200)
      .body("id", equalTo(1))
      .extract().response();

    User user = response.as(User.class);

    Allure.step("Проверка данных пользователя", () -> {
      assertThat(user.id()).isEqualTo(1);
      assertThat(user.name()).isNotEmpty();
      assertThat(user.email()).contains("@");
    });
  }

  @Test
  @Order(2)
  @Story("Allure отчеты")
  @DisplayName("2. POST запрос создания пользователя с Allure")
  @Description("Создание пользователя с логированием в Allure")
  @Severity(SeverityLevel.CRITICAL)
  void testCreateUserWithAllure() {
    CreateUserRequest newUser = CreateUserRequest.builder()
      .name("Allure Test User")
      .email("allure@test.com")
      .username("allureuser")
      .build();

    Allure.step("Создание запроса на создание пользователя", () -> Allure.addAttachment("User Data", "application/json", newUser.toString()));

    Response response = given()
      .body(newUser)
      .when()
      .post(BASE_URL + "/users")
      .then()
      .statusCode(201)
      .extract().response();

    Allure.step("Валидация ответа", () -> {
      assertThat(response.jsonPath().getString("name")).isEqualTo("Allure Test User");
      assertThat(response.jsonPath().getString("email")).isEqualTo("allure@test.com");
      assertThat(response.jsonPath().getInt("id")).isGreaterThan(0);
    });
  }

  @Test
  @Order(3)
  @Story("Allure отчеты")
  @DisplayName("3. Получение комментариев с детальным логированием")
  @Description("Работа с Comment моделью через TestSpecification")
  @Severity(SeverityLevel.NORMAL)
  void testGetCommentsWithAllure() {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("postId", "1");

    Allure.step("Получение комментариев для поста ID=1", () -> {
      Allure.parameter("postId", "1");
    });

    Response response = given()
      .queryParams(queryParams)
      .when()
      .get(BASE_URL + "/comments")
      .then()
      .statusCode(200)
      .body("$", not(empty()))
      .extract().response();

    List<Comment> comments = response.jsonPath().getList("$", Comment.class);

    Allure.step("Проверка полученных комментариев", () -> {
      Allure.parameter("Comments count", comments.size());
      assertThat(comments).isNotEmpty();
      assertThat(comments).allMatch(c -> c.postId() == 1);

      Comment firstComment = comments.getFirst();
      Allure.addAttachment("First Comment", String.format(
        "ID: %d, Name: %s, Email: %s",
        firstComment.id(),
        firstComment.name(),
        firstComment.email()
      ));
    });
  }

  @Test
  @Order(4)
  @Story("Allure отчеты")
  @DisplayName("4. Создание поста с подробными шагами")
  @Description("Пошаговое создание и валидация поста")
  @Severity(SeverityLevel.CRITICAL)
  void testCreatePostWithSteps() {
    Post newPost = Allure.step("Подготовка данных для поста", () -> new Post(1L, null, "Allure Test Post", "Testing with Allure reporting"));

    Response response = Allure.step("Отправка POST запроса", () -> given()
      .body(newPost)
      .when()
      .post(BASE_URL + "/posts")
      .then()
      .statusCode(201)
      .extract().response());

    Allure.step("Валидация созданного поста", () -> {
      assertThat(response.jsonPath().getString("title")).isEqualTo("Allure Test Post");
      assertThat(response.jsonPath().getString("body")).isEqualTo("Testing with Allure reporting");
      assertThat(response.jsonPath().getLong("userId")).isEqualTo(1L);

      Long postId = response.jsonPath().getLong("id");
      Allure.parameter("Created Post ID", postId);
      assertThat(postId).isGreaterThan(0);
    });
  }

  @Test
  @Order(5)
  @Story("Allure отчеты")
  @DisplayName("5. Комплексный сценарий с множественными шагами")
  @Description("Создание пользователя, поста и получение комментариев")
  @Severity(SeverityLevel.BLOCKER)
  void testComplexScenarioWithAllure() {
    // Шаг 1: Создание пользователя
    CreateUserRequest user = Allure.step("Шаг 1: Создание пользователя", () -> {
      CreateUserRequest newUser = CreateUserRequest.builder()
        .name("Complex Scenario User")
        .email("complex@scenario.com")
        .username("complexuser")
        .build();

      Response response = given()
        .body(newUser)
        .when()
        .post(BASE_URL + "/users")
        .then()
        .statusCode(201)
        .extract().response();

      Allure.parameter("User ID", response.jsonPath().getInt("id"));
      return newUser;
    });

    // Шаг 2: Создание поста от имени пользователя
    Long postId = Allure.step("Шаг 2: Создание поста", () -> {
      Post post = new Post(1L, null, "User's First Post", "Content created in complex scenario");

      Response response = given()
        .body(post)
        .when()
        .post(BASE_URL + "/posts")
        .then()
        .statusCode(201)
        .extract().response();

      Long id = response.jsonPath().getLong("id");
      Allure.parameter("Post ID", id);
      return id;
    });

    // Шаг 3: Получение комментариев
    Allure.step("Шаг 3: Получение комментариев к посту", () -> {
      Map<String, String> queryParams = new HashMap<>();
      queryParams.put("postId", "1");

      Response response = given()
        .queryParams(queryParams)
        .when()
        .get(BASE_URL + "/comments")
        .then()
        .statusCode(200)
        .extract().response();

      List<Comment> comments = response.jsonPath().getList("$", Comment.class);
      Allure.parameter("Comments found", comments.size());

      assertThat(comments).isNotEmpty();
    });

    Allure.step("Шаг 4: Финальная проверка", () -> {
      assertThat(user.getName()).isEqualTo("Complex Scenario User");
      assertThat(postId).isNotNull();
    });
  }

  @Test
  @Order(6)
  @Story("Allure отчеты")
  @DisplayName("6. Тест с attachment в Allure")
  @Description("Демонстрация добавления вложений в отчет")
  @Severity(SeverityLevel.NORMAL)
  void testWithAttachments() {
    CreateUserRequest user = CreateUserRequest.builder()
      .name("Attachment Demo User")
      .email("attachment@demo.com")
      .username("attachdemo")
      .build();

    // Добавляем JSON как attachment
    Allure.addAttachment(
      "Request Body JSON",
      "application/json",
      String.format("""
        {
          "name": "%s",
          "email": "%s",
          "username": "%s"
        }
        """, user.getName(), user.getEmail(), user.getUsername())
    );

    Response response = given()
      .body(user)
      .when()
      .post(BASE_URL + "/users")
      .then()
      .statusCode(201)
      .extract().response();

    // Добавляем response как attachment
    Allure.addAttachment(
      "Response Body",
      "application/json",
      response.getBody().asString()
    );

    assertThat(response.jsonPath().getString("name")).isEqualTo("Attachment Demo User");
  }

  @Test
  @Order(7)
  @Story("Негативные тесты")
  @DisplayName("7. Негативный тест с Allure отчетом")
  @Description("Проверка обработки ошибок с логированием в Allure")
  @Severity(SeverityLevel.NORMAL)
  void testNegativeScenarioWithAllure() {
    Allure.step("Попытка получить несуществующий ресурс", () -> {
      Allure.parameter("User ID", "999999");

      Response response = given()
        .when()
        .get(BASE_URL + "/users/999999")
        .then()
        .extract().response();

      Allure.step("Проверка статус кода 404", () -> {
        assertThat(response.statusCode()).isEqualTo(404);
        Allure.addAttachment("Response Status", String.valueOf(response.statusCode()));
      });
    });
  }
}