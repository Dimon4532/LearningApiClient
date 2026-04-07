package ru.learning.java.clients.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import ru.learning.java.clients.api.base.BaseApiTest;
import ru.learning.java.models.Comment;
import ru.learning.java.models.CreateUserRequest;
import ru.learning.java.models.Post;
import ru.learning.java.models.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

@Epic("REST Assured Demo")
@Feature("ApiClient")
@DisplayName("Тесты ApiClient")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiClientTest extends BaseApiTest {

  @Test
  @Order(1)
  @Story("GET запросы")
  @DisplayName("1. Простой GET запрос - получение списка пользователей")
  @Description("Демонстрация базового GET запроса и валидации статус кода")
  void testSimpleGetRequest() {
    Response response = apiClient
      .sendGet(BASE_URL + "/users", new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>())
      .extract().response();

    assertThat(response.statusCode()).isEqualTo(200);
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

    apiClient.sendGet(BASE_URL + "/users/{userId}", new HashMap<>(), pathParams, new HashMap<>(), new HashMap<>())
             .assertThat().statusCode(200)
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

    Response response = apiClient
      .sendGet(BASE_URL + "/posts", new HashMap<>(), new HashMap<>(), queryParams, new HashMap<>())
      .extract().response();

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
    apiClient.sendGet(BASE_URL + "/users/1", new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>())
             .assertThat().statusCode(200)
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
    Response response = apiClient
      .sendGet(BASE_URL + "/users/1", new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>())
      .extract().response();

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

    apiClient.sendPost(BASE_URL + "/posts", 201, requestBody, new HashMap<>(), new HashMap<>(), new HashMap<>())
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
  void testPostRequestWithObject() throws JsonProcessingException {
    Post newPost = new Post(1L, null, "My Test Post", "Content of my test post");

    Response response = apiClient
      .sendPost(
        BASE_URL + "/posts", 201, objectMapper.writeValueAsString(newPost),
        new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

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

    apiClient.sendPut(BASE_URL + "/posts/{postId}", 200, updatedBody, new HashMap<>(), pathParams, new HashMap<>())
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

    apiClient.sendPatch(BASE_URL + "/posts/{postId}", 200, patchBody, new HashMap<>(), pathParams, new HashMap<>())
             .assertThat()
             .body("title", equalTo("Partially Updated Title"))
             .body("id", equalTo(1));
  }

  @Test
  @Order(10)
  @Story("PATCH запросы")
  @DisplayName("10. PATCH — частичное обновление через httpbin")
  void testPatchOnHttpbin() {
    String patchBody = """
      {
          "status": "reviewed"
      }
      """;

    apiClient.sendPatch(HTTPBIN_URL + "/patch", 200, patchBody, new HashMap<>(), new HashMap<>(), new HashMap<>())
             .assertThat()
             .body("json.status", equalTo("reviewed"));
  }

  @Test
  @Order(11)
  @Story("PATCH запросы")
  @DisplayName("11. PATCH — частичное обновление через Map и ObjectMapper")
  void testPatchOnHttpbinWithMap() throws JsonProcessingException {
    Map<String, String> patch = new HashMap<>();
    patch.put("status", "reviewed");

    apiClient.sendPatch(
               HTTPBIN_URL + "/patch", 200, objectMapper.writeValueAsString(patch),
               new HashMap<>(), new HashMap<>(), new HashMap<>()
             )
             .assertThat()
             .body("json.status", equalTo("reviewed"));
  }

  @Test
  @Order(12)
  @Story("DELETE запросы")
  @DisplayName("12. DELETE запрос")
  @Description("Удаление поста")
  void testDeleteRequest() {
    Map<String, String> pathParams = new HashMap<>();
    pathParams.put("postId", "1");

    apiClient.sendDelete(BASE_URL + "/posts/{postId}", 200, new HashMap<>(), pathParams, new HashMap<>());
  }

  @Test
  @Order(13)
  @Story("Негативные тесты")
  @DisplayName("13. Негативный тест - несуществующий ресурс")
  @Description("Проверка обработки 404 ошибки")
  void testNotFoundError() {
    Response response = apiClient
      .sendGet(BASE_URL + "/users/999999", new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>())
      .extract().response();

    assertThat(response.statusCode()).isEqualTo(404);
  }

  @Test
  @Order(14)
  @Story("Валидация ответов")
  @DisplayName("14. Валидация времени ответа")
  @Description("Проверка что API отвечает быстро")
  void testResponseTime() {
    apiClient.sendGet(BASE_URL + "/users", new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>())
             .assertThat()
             .time(lessThan(2000L));
  }

  @Test
  @Order(15)
  @Story("Валидация заголовков")
  @DisplayName("15. Проверка заголовков ответа")
  @Description("Валидация Content-Type и других заголовков")
  void testResponseHeaders() {
    Response response = apiClient
      .sendGet(BASE_URL + "/users/1", new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>())
      .extract().response();

    assertThat(response.getContentType()).contains("application/json");
    assertThat(response.getHeader("Content-Type")).isNotNull();
  }

  @Test
  @Order(16)
  @Story("Комплексные сценарии")
  @DisplayName("16. Комбинированный тест - AuthApiClient с базовыми методами")
  @Description("Использование AuthApiClient с базовыми методами")
  void testAuthClientWithBasicMethods() {
    Response response = authApiClient
      .sendGet(BASE_URL + "/users/1", new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>())
      .extract().response();

    User user = response.as(User.class);
    assertThat(user.id()).isEqualTo(1);
  }

  @Test
  @Order(17)
  @Story("Комплексные сценарии")
  @DisplayName("17. Комбинированный тест - FormApiClient с базовыми методами")
  @Description("Использование FormApiClient с базовыми методами")
  void testFormClientWithBasicMethods() throws JsonProcessingException {
    Post newPost = new Post(1L, null, "Form Client Test", "Testing inheritance");

    Response response = formApiClient
      .sendPost(
        BASE_URL + "/posts", 201, objectMapper.writeValueAsString(newPost),
        new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    assertThat(response.jsonPath().getString("title")).isEqualTo("Form Client Test");
  }

  @Test
  @Order(18)
  @Story("Работа с комментариями")
  @DisplayName("18. GET запрос - получение комментариев к посту")
  @Description("Демонстрация работы с моделью Comment")
  void testGetCommentsForPost() {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("postId", "1");

    Response response = apiClient
      .sendGet(BASE_URL + "/comments", new HashMap<>(), new HashMap<>(), queryParams, new HashMap<>())
      .extract().response();

    List<Comment> comments = response.jsonPath().getList("$", Comment.class);
    assertThat(comments).isNotEmpty();
    assertThat(comments).allMatch(comment -> comment.postId() == 1);
    assertThat(comments.getFirst().email()).contains("@");
    assertThat(comments.getFirst().body()).isNotEmpty();
  }

  @Test
  @Order(19)
  @Story("Работа с комментариями")
  @DisplayName("19. GET запрос - получение конкретного комментария")
  @Description("Проверка десериализации одного комментария")
  void testGetSingleComment() {
    Map<String, String> pathParams = new HashMap<>();
    pathParams.put("commentId", "1");

    Response response = apiClient
      .sendGet(BASE_URL + "/comments/{commentId}", new HashMap<>(), pathParams, new HashMap<>(), new HashMap<>())
      .extract().response();

    Comment comment = response.as(Comment.class);
    assertThat(comment).isNotNull();
    assertThat(comment.id()).isEqualTo(1);
    assertThat(comment.email()).isNotEmpty();
    assertThat(comment.name()).isNotEmpty();
  }

  @Test
  @Order(20)
  @Story("Создание пользователей")
  @DisplayName("20. POST запрос - создание пользователя с Builder")
  @Description("Демонстрация использования CreateUserRequest с Lombok Builder")
  void testCreateUserWithBuilder() throws JsonProcessingException {
    CreateUserRequest newUser = CreateUserRequest.builder()
                                                 .name("John Doe")
                                                 .email("john.doe@example.com")
                                                 .username("johndoe")
                                                 .build();

    Response response = apiClient
      .sendPost(
        BASE_URL + "/users", 201, objectMapper.writeValueAsString(newUser),
        new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    assertThat(response.jsonPath().getString("name")).isEqualTo("John Doe");
    assertThat(response.jsonPath().getString("email")).isEqualTo("john.doe@example.com");
    assertThat(response.jsonPath().getString("username")).isEqualTo("johndoe");
    assertThat(response.jsonPath().getInt("id")).isGreaterThan(0);
  }

  @Test
  @Order(21)
  @Story("Создание пользователей")
  @DisplayName("21. POST запрос - создание пользователя через конструктор")
  @Description("Альтернативный способ создания объекта CreateUserRequest")
  void testCreateUserWithConstructor() throws JsonProcessingException {
    CreateUserRequest newUser = new CreateUserRequest();
    newUser.setName("Jane Smith");
    newUser.setEmail("jane.smith@example.com");
    newUser.setUsername("janesmith");

    Response response = apiClient
      .sendPost(
        BASE_URL + "/users", 201, objectMapper.writeValueAsString(newUser),
        new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    assertThat(response.jsonPath().getString("name")).isEqualTo("Jane Smith");
    assertThat(response.jsonPath().getString("email")).isEqualTo("jane.smith@example.com");
  }

  @Test
  @Order(22)
  @Story("Комплексные сценарии")
  @DisplayName("22. Комплексный сценарий - создание поста и комментариев")
  @Description("Создание поста и получение его комментариев")
  void testComplexScenarioPostAndComments() throws JsonProcessingException {
    Post newPost = new Post(1L, null, "Integration Test Post", "Testing comments integration");

    Response postResponse = apiClient
      .sendPost(
        BASE_URL + "/posts", 201, objectMapper.writeValueAsString(newPost),
        new HashMap<>(), new HashMap<>(), new HashMap<>()
      )
      .extract().response();

    Long postId = postResponse.jsonPath().getLong("id");
    assertThat(postId).isNotNull();

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("postId", "1");

    Response commentsResponse = apiClient
      .sendGet(BASE_URL + "/comments", new HashMap<>(), new HashMap<>(), queryParams, new HashMap<>())
      .extract().response();

    List<Comment> comments = commentsResponse.jsonPath().getList("$", Comment.class);
    assertThat(comments).isNotEmpty();
    assertThat(comments).allMatch(c -> c.postId() == 1);
  }
}