package ru.learning.java;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import ru.learning.java.clients.api.ApiClient;
import ru.learning.java.clients.api.AuthApiClient;
import ru.learning.java.clients.api.FormApiClient;
import ru.learning.java.models.Post;
import ru.learning.java.models.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Демонстрационные тесты для обучения работе с REST Assured
 */
@Epic("REST Assured Demo")
@Feature("API Testing Examples")
@DisplayName("Примеры тестирования API с REST Assured")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestAssuredDemoTest {

  private static ApiClient apiClient;
  private static AuthApiClient authApiClient;
  private static FormApiClient formApiClient;

  private static final String BASE_URL = "https://jsonplaceholder.typicode.com";
  private static final String HTTPBIN_URL = "https://httpbin.org";

  @BeforeAll
  static void setUp() {
    apiClient = new ApiClient();
    authApiClient = new AuthApiClient();
    formApiClient = new FormApiClient();
  }

  // ==================== БАЗОВЫЕ API CLIENT ТЕСТЫ ====================

  @Test
  @Order(1)
  @Story("GET запросы")
  @DisplayName("1. Простой GET запрос - получение списка пользователей")
  @Description("Демонстрация базового GET запроса и валидации статус кода")
  void testSimpleGetRequest() {
    Response response = apiClient.sendGet(
      BASE_URL + "/users",
      new HashMap<>(),
      new HashMap<>(),
      new HashMap<>(),
      new HashMap<>()
    ).extract().response();

    // Валидация статус кода
    assertThat(response.statusCode()).isEqualTo(200);

    // Валидация что вернулся массив
    assertThat(response.jsonPath().getList("$")).isNotEmpty();
  }

  @Test
  @Order(2)
  @Story("GET запросы")
  @DisplayName("2. GET запрос с path параметром")
  @Description("Получение конкретного пользователя по ID")
  void testGetWithPathParameter() {
    Map<String, String> pathParams = new HashMap<>();
    pathParams.put("userId", "1");

    apiClient.sendGet(
        BASE_URL + "/users/{userId}",
        new HashMap<>(),
        pathParams,
        new HashMap<>(),
        new HashMap<>()
      )
      .assertThat()
      .statusCode(200)
      .body("id", equalTo(1))
      .body("name", notNullValue())
      .body("email", containsString("@"));
  }

  @Test
  @Order(3)
  @Story("GET запросы")
  @DisplayName("3. GET запрос с query параметрами")
  @Description("Фильтрация постов по userId")
  void testGetWithQueryParameters() {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("userId", "1");

    Response response = apiClient.sendGet(
      BASE_URL + "/posts",
      new HashMap<>(),
      new HashMap<>(),
      queryParams,
      new HashMap<>()
    ).extract().response();

    // Десериализация в объекты
    List<Post> posts = response.jsonPath().getList("$", Post.class);

    assertThat(posts).isNotEmpty();
    assertThat(posts).allMatch(post -> post.userId() == 1);
  }

  @Test
  @Order(4)
  @Story("GET запросы")
  @DisplayName("4. Валидация JSON структуры с Hamcrest матчерами")
  @Description("Проверка полей пользователя различными способами")
  void testJsonValidationWithHamcrest() {
    apiClient.sendGet(
        BASE_URL + "/users/1",
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>()
      )
      .assertThat()
      .statusCode(200)
      .body("id", instanceOf(Integer.class))
      .body("name", not(emptyString()))
      .body("email", matchesPattern("^[A-Za-z0-9+_.-]+@(.+)$"))
      .body("address.city", notNullValue())
      .body("company.name", notNullValue());
  }

  @Test
  @Order(5)
  @Story("GET запросы")
  @DisplayName("5. Десериализация JSON в Java объект")
  @Description("Преобразование ответа в типизированный объект")
  void testJsonToObjectDeserialization() {
    Response response = apiClient.sendGet(
      BASE_URL + "/users/1",
      new HashMap<>(),
      new HashMap<>(),
      new HashMap<>(),
      new HashMap<>()
    ).extract().response();

    User user = response.as(User.class);

    assertThat(user).isNotNull();
    assertThat(user.id()).isEqualTo(1);
    assertThat(user.name()).isNotEmpty();
    assertThat(user.email()).contains("@");
  }

  @Test
  @Order(6)
  @Story("POST запросы")
  @DisplayName("6. POST запрос с JSON телом")
  @Description("Создание нового поста")
  void testPostRequestWithJsonBody() {
    String requestBody = """
            {
                "userId": 1,
                "title": "Test Post Title",
                "body": "Test Post Body"
            }
            """;

    apiClient.sendPost(
        BASE_URL + "/posts",
        201,
        requestBody,
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>()
      )
      .assertThat()
      .body("id", notNullValue())
      .body("title", equalTo("Test Post Title"))
      .body("body", equalTo("Test Post Body"))
      .body("userId", equalTo(1));
  }

  @Test
  @Order(7)
  @Story("POST запросы")
  @DisplayName("7. POST запрос с объектом")
  @Description("Создание поста из Java объекта")
  void testPostRequestWithObject() {
    Post newPost = new Post(1L, null, "My Test Post", "Content of my test post");

    Response response = apiClient.sendPost(
      BASE_URL + "/posts",
      201,
      newPost,
      new HashMap<>(),
      new HashMap<>(),
      new HashMap<>()
    ).extract().response();

    assertThat(response.jsonPath().getInt("id")).isGreaterThan(0);
  }

  @Test
  @Order(8)
  @Story("PUT запросы")
  @DisplayName("8. PUT запрос - обновление ресурса")
  @Description("Полное обновление поста")
  void testPutRequest() {
    String updatedBody = """
            {
                "id": 1,
                "userId": 1,
                "title": "Updated Title",
                "body": "Updated Body"
            }
            """;

    Map<String, String> pathParams = new HashMap<>();
    pathParams.put("postId", "1");

    apiClient.sendPut(
        BASE_URL + "/posts/{postId}",
        200,
        updatedBody,
        new HashMap<>(),
        pathParams,
        new HashMap<>()
      )
      .assertThat()
      .body("id", equalTo(1))
      .body("title", equalTo("Updated Title"))
      .body("body", equalTo("Updated Body"));
  }

  @Test
  @Order(9)
  @Story("PATCH запросы")
  @DisplayName("9. PATCH запрос - частичное обновление")
  @Description("Обновление только заголовка поста")
  void testPatchRequest() {
    String patchBody = """
            {
                "title": "Partially Updated Title"
            }
            """;

    Map<String, String> pathParams = new HashMap<>();
    pathParams.put("postId", "1");

    apiClient.sendPatch(
        BASE_URL + "/posts/{postId}",
        200,
        patchBody,
        new HashMap<>(),
        pathParams,
        new HashMap<>()
      )
      .assertThat()
      .body("title", equalTo("Partially Updated Title"))
      .body("id", equalTo(1));
  }

  @Test
  @Order(10)
  @Story("DELETE запросы")
  @DisplayName("10. DELETE запрос")
  @Description("Удаление поста")
  void testDeleteRequest() {
    Map<String, String> pathParams = new HashMap<>();
    pathParams.put("postId", "1");

    apiClient.sendDelete(
      BASE_URL + "/posts/{postId}",
      200,
      new HashMap<>(),
      pathParams,
      new HashMap<>()
    );

    // JSONPlaceholder возвращает 200 и пустой объект
  }

  @Test
  @Order(11)
  @Story("Негативные тесты")
  @DisplayName("11. Негативный тест - несуществующий ресурс")
  @Description("Проверка обработки 404 ошибки")
  void testNotFoundError() {
    Response response = apiClient.sendGet(
      BASE_URL + "/users/999999",
      new HashMap<>(),
      new HashMap<>(),
      new HashMap<>(),
      new HashMap<>()
    ).extract().response();

    assertThat(response.statusCode()).isEqualTo(404);
  }

  @Test
  @Order(12)
  @Story("Валидация ответов")
  @DisplayName("12. Валидация времени ответа")
  @Description("Проверка что API отвечает быстро")
  void testResponseTime() {
    apiClient.sendGet(
        BASE_URL + "/users",
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>()
      )
      .assertThat()
      .time(lessThan(2000L));
  }

  @Test
  @Order(13)
  @Story("Валидация заголовков")
  @DisplayName("13. Проверка заголовков ответа")
  @Description("Валидация Content-Type и других заголовков")
  void testResponseHeaders() {
    Response response = apiClient.sendGet(
      BASE_URL + "/users/1",
      new HashMap<>(),
      new HashMap<>(),
      new HashMap<>(),
      new HashMap<>()
    ).extract().response();

    assertThat(response.getContentType()).contains("application/json");
    assertThat(response.getHeader("Content-Type")).isNotNull();
  }

  // ==================== AUTH API CLIENT ТЕСТЫ ====================

  @Test
  @Order(14)
  @Story("Авторизация")
  @DisplayName("14. GET запрос с Basic Auth - успешная авторизация")
  @Description("Демонстрация GET запроса с корректными credentials")
  void testGetWithBasicAuthSuccess() {
    Response response = authApiClient.sendGetWithAuth(
      HTTPBIN_URL + "/basic-auth/user/passwd",
      200,
      "user",
      "passwd",
      new HashMap<>(),
      new HashMap<>(),
      new HashMap<>()
    ).extract().response();

    assertThat(response.jsonPath().getBoolean("authenticated")).isTrue();
    assertThat(response.jsonPath().getString("user")).isEqualTo("user");
  }

  @Test
  @Order(15)
  @Story("Авторизация")
  @DisplayName("15. GET запрос с Basic Auth - неверные credentials")
  @Description("Проверка обработки 401 Unauthorized")
  void testGetWithBasicAuthFailure() {
    Response response = authApiClient.sendGet(
      HTTPBIN_URL + "/basic-auth/user/passwd",
      new HashMap<>(),
      new HashMap<>(),
      new HashMap<>(),
      new HashMap<>()
    ).extract().response();

    // Без авторизации должен вернуться 401
    assertThat(response.statusCode()).isEqualTo(401);
  }

  @Test
  @Order(16)
  @Story("Авторизация")
  @DisplayName("16. POST запрос с Basic Auth")
  @Description("Демонстрация POST запроса с авторизацией")
  void testPostWithBasicAuth() {
    String requestBody = """
            {
                "title": "Secured Post",
                "content": "This post requires authentication"
            }
            """;

    Response response = authApiClient.sendPostWithAuth(
      HTTPBIN_URL + "/post",
      200,
      "testuser",
      "testpass",
      requestBody,
      new HashMap<>(),
      new HashMap<>(),
      new HashMap<>()
    ).extract().response();

    // httpbin возвращает отправленные данные в поле json
    assertThat(response.jsonPath().getString("json.title")).isEqualTo("Secured Post");
    assertThat(response.jsonPath().getString("headers.Authorization")).contains("Basic");
  }

  @Test
  @Order(17)
  @Story("Авторизация")
  @DisplayName("17. PUT запрос с Basic Auth")
  @Description("Демонстрация PUT запроса с авторизацией")
  void testPutWithBasicAuth() {
    String updateBody = """
            {
                "status": "updated",
                "message": "Resource updated with auth"
            }
            """;

    Response response = authApiClient.sendPutWithAuth(
      HTTPBIN_URL + "/put",
      200,
      "admin",
      "admin123",
      updateBody,
      new HashMap<>(),
      new HashMap<>(),
      new HashMap<>()
    ).extract().response();

    assertThat(response.jsonPath().getString("json.status")).isEqualTo("updated");
    assertThat(response.jsonPath().getString("headers.Authorization")).isNotNull();
  }

  @Test
  @Order(18)
  @Story("Авторизация")
  @DisplayName("18. PATCH запрос с Basic Auth")
  @Description("Демонстрация PATCH запроса с авторизацией")
  void testPatchWithBasicAuth() {
    String patchBody = """
            {
                "field": "patched_value"
            }
            """;

    Response response = authApiClient.sendPatchWithAuth(
      HTTPBIN_URL + "/patch",
      200,
      "patcher",
      "patch123",
      patchBody,
      new HashMap<>(),
      new HashMap<>(),
      new HashMap<>()
    ).extract().response();

    assertThat(response.jsonPath().getString("json.field")).isEqualTo("patched_value");
  }

  @Test
  @Order(19)
  @Story("Авторизация")
  @DisplayName("19. DELETE запрос с Basic Auth")
  @Description("Демонстрация DELETE запроса с авторизацией")
  void testDeleteWithBasicAuth() {
    Response response = authApiClient.sendDeleteWithAuth(
      HTTPBIN_URL + "/delete",
      200,
      "deleter",
      "delete123",
      new HashMap<>(),
      new HashMap<>(),
      new HashMap<>()
    ).extract().response();

    assertThat(response.jsonPath().getString("headers.Authorization")).contains("Basic");
  }

  // ==================== FORM API CLIENT ТЕСТЫ ====================

  @Test
  @Order(20)
  @Story("Form Parameters")
  @DisplayName("20. POST запрос с form parameters (без cookies)")
  @Description("Отправка формы в формате application/x-www-form-urlencoded")
  void testPostWithFormParams() {
    Map<String, String> formParams = new HashMap<>();
    formParams.put("username", "testuser");
    formParams.put("password", "testpass");
    formParams.put("email", "test@example.com");

    Response response = formApiClient.sendPostWithFormParams(
      HTTPBIN_URL + "/post",
      new HashMap<>(),
      formParams
    ).extract().response();

    // httpbin возвращает form данные в поле form
    assertThat(response.jsonPath().getString("form.username")).isEqualTo("testuser");
    assertThat(response.jsonPath().getString("form.password")).isEqualTo("testpass");
    assertThat(response.jsonPath().getString("form.email")).isEqualTo("test@example.com");
  }

  @Test
  @Order(21)
  @Story("Form Parameters")
  @DisplayName("21. POST запрос с form parameters и cookies")
  @Description("Отправка формы с cookies")
  void testPostWithFormParamsAndCookies() {
    Map<String, String> formParams = new HashMap<>();
    formParams.put("action", "login");
    formParams.put("remember", "true");

    Map<String, String> cookies = new HashMap<>();
    cookies.put("session_id", "abc123");
    cookies.put("user_token", "xyz789");

    Response response = formApiClient.sendPostWithFormParams(
      HTTPBIN_URL + "/post",
      new HashMap<>(),
      cookies,
      formParams
    ).extract().response();

    assertThat(response.jsonPath().getString("form.action")).isEqualTo("login");
    assertThat(response.jsonPath().getString("form.remember")).isEqualTo("true");
    // Проверяем что cookies были отправлены
    assertThat(response.jsonPath().getString("headers.Cookie")).contains("session_id=abc123");
  }

  @Test
  @Order(22)
  @Story("Form Parameters")
  @DisplayName("22. GET запрос с form parameters")
  @Description("GET запрос с form parameters в query string")
  void testGetWithFormParams() {
    Map<String, String> formParams = new HashMap<>();
    formParams.put("search", "rest-assured");
    formParams.put("category", "testing");

    Map<String, String> cookies = new HashMap<>();
    cookies.put("preferences", "dark_mode");

    Response response = formApiClient.sendGetWithFormParams(
      HTTPBIN_URL + "/get",
      new HashMap<>(),
      cookies,
      formParams
    ).extract().response();

    // При GET form params преобразуются в query params
    assertThat(response.jsonPath().getString("args.search")).isEqualTo("rest-assured");
    assertThat(response.jsonPath().getString("args.category")).isEqualTo("testing");
  }

  @Test
  @Order(23)
  @Story("Form Parameters")
  @DisplayName("23. PUT запрос с form parameters")
  @Description("PUT запрос с form-encoded данными")
  void testPutWithFormParams() {
    Map<String, String> formParams = new HashMap<>();
    formParams.put("name", "Updated Name");
    formParams.put("status", "active");

    Map<String, String> cookies = new HashMap<>();
    cookies.put("auth_token", "token123");

    Response response = formApiClient.sendPutWithFormParams(
      HTTPBIN_URL + "/put",
      new HashMap<>(),
      cookies,
      formParams
    ).extract().response();

    assertThat(response.jsonPath().getString("form.name")).isEqualTo("Updated Name");
    assertThat(response.jsonPath().getString("form.status")).isEqualTo("active");
  }

  @Test
  @Order(24)
  @Story("Form Parameters")
  @DisplayName("24. PATCH запрос с form parameters")
  @Description("PATCH запрос с form-encoded данными")
  void testPatchWithFormParams() {
    Map<String, String> formParams = new HashMap<>();
    formParams.put("field_to_update", "new_value");

    Map<String, String> cookies = new HashMap<>();
    cookies.put("session", "active_session");

    Response response = formApiClient.sendPatchWithFormParams(
      HTTPBIN_URL + "/patch",
      new HashMap<>(),
      cookies,
      formParams
    ).extract().response();

    assertThat(response.jsonPath().getString("form.field_to_update")).isEqualTo("new_value");
  }

  @Test
  @Order(25)
  @Story("Комплексные сценарии")
  @DisplayName("25. Комбинированный тест - Auth + Form")
  @Description("Использование AuthApiClient с базовыми методами")
  void testAuthClientWithBasicMethods() {
    // AuthApiClient наследует методы от ApiClient
    Response response = authApiClient.sendGet(
      BASE_URL + "/users/1",
      new HashMap<>(),
      new HashMap<>(),
      new HashMap<>(),
      new HashMap<>()
    ).extract().response();

    User user = response.as(User.class);
    assertThat(user.id()).isEqualTo(1);
  }

  @Test
  @Order(26)
  @Story("Комплексные сценарии")
  @DisplayName("26. Комбинированный тест - Form + Basic")
  @Description("Использование FormApiClient с базовыми методами")
  void testFormClientWithBasicMethods() {
    // FormApiClient наследует методы от ApiClient
    Post newPost = new Post(1L, null, "Form Client Test", "Testing inheritance");

    Response response = formApiClient.sendPost(
      BASE_URL + "/posts",
      201,
      newPost,
      new HashMap<>(),
      new HashMap<>(),
      new HashMap<>()
    ).extract().response();

    assertThat(response.jsonPath().getString("title")).isEqualTo("Form Client Test");
  }
}