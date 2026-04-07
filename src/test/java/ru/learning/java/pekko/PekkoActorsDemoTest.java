package ru.learning.java.pekko;

import io.qameta.allure.*;
import org.apache.pekko.actor.typed.ActorSystem;
import org.apache.pekko.actor.typed.javadsl.AskPattern;
import org.junit.jupiter.api.*;
import ru.learning.java.actors.HttpRequestActor;
import ru.learning.java.actors.HttpSupervisor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Демонстрация работы с акторной моделью Pekko
 */
@Epic("Pekko Actors Demo")
@Feature("Actor Model Examples")
@DisplayName("Примеры использования акторной модели")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PekkoActorsDemoTest {

  private static ActorSystem<HttpSupervisor.Command> actorSystem;
  private static final String BASE_URL = "https://jsonplaceholder.typicode.com";

  @BeforeAll
  static void setUp() {
    actorSystem = ActorSystem.create(HttpSupervisor.create(), "http-actor-system");
  }

  @AfterAll
  static void tearDown() {
    actorSystem.terminate();
  }

  @Test
  @Order(1)
  @Story("Акторы")
  @DisplayName("1. Выполнение GET запроса через актор")
  @Description("Демонстрация асинхронного выполнения GET запроса")
  void testGetRequestWithActor() throws ExecutionException, InterruptedException {
    CompletionStage<HttpRequestActor.Response> result = AskPattern.ask(
      actorSystem,
      replyTo -> new HttpSupervisor.StartRequest("req-1", BASE_URL + "/users/1", replyTo),
      Duration.ofSeconds(10),
      actorSystem.scheduler()
    );

    HttpRequestActor.Response response = result.toCompletableFuture().get();

    assertThat(response.success()).isTrue();
    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).contains("\"id\": 1");
  }

  @Test
  @Order(2)
  @Story("Акторы")
  @DisplayName("2. Выполнение POST запроса через актор")
  @Description("Демонстрация асинхронного выполнения POST запроса")
  void testPostRequestWithActor() throws ExecutionException, InterruptedException {
    String requestBody = """
      {
          "userId": 1,
          "title": "Actor Test Post",
          "body": "Post created via Pekko Actor"
      }
      """;

    CompletionStage<HttpRequestActor.Response> result = AskPattern.ask(
      actorSystem,
      replyTo -> new HttpSupervisor.StartPostRequest(
        "req-2", BASE_URL + "/posts", requestBody, replyTo
      ),
      Duration.ofSeconds(10),
      actorSystem.scheduler()
    );

    HttpRequestActor.Response response = result.toCompletableFuture().get();

    assertThat(response.success()).isTrue();
    assertThat(response.statusCode()).isIn(200, 201);
    assertThat(response.body()).contains("Actor Test Post");
  }

  @Test
  @Order(3)
  @Story("Акторы")
  @DisplayName("3. Параллельное выполнение нескольких запросов")
  @Description("Демонстрация параллельной работы нескольких акторов")
  void testMultipleParallelRequests() throws ExecutionException, InterruptedException {
    // Запускаем 5 параллельных запросов
    List<CompletionStage<HttpRequestActor.Response>> requests = new ArrayList<>();

    for (int i = 1; i <= 5; i++) {
      final int userId = i;
      requests.add(AskPattern.ask(
        actorSystem,
        replyTo -> new HttpSupervisor.StartRequest(
          "parallel-req-" + userId,
          BASE_URL + "/users/" + userId,
          replyTo
        ),
        Duration.ofSeconds(10),
        actorSystem.scheduler()
      ));
    }

    // Ждем завершения всех запросов
    for (CompletionStage<HttpRequestActor.Response> request : requests) {
      HttpRequestActor.Response response = request.toCompletableFuture().get();
      assertThat(response.success()).isTrue();
      assertThat(response.statusCode()).isEqualTo(200);
    }
  }

  @Test
  @Order(4)
  @Story("Акторы")
  @DisplayName("4. Обработка ошибки в акторе")
  @Description("Проверка обработки несуществующего ресурса")
  void testErrorHandlingInActor() throws ExecutionException, InterruptedException {
    CompletionStage<HttpRequestActor.Response> result = AskPattern.ask(
      actorSystem,
      replyTo -> new HttpSupervisor.StartRequest(
        "error-req",
        BASE_URL + "/users/999999",
        replyTo
      ),
      Duration.ofSeconds(10),
      actorSystem.scheduler()
    );

    HttpRequestActor.Response response = result.toCompletableFuture().get();

    // JSONPlaceholder возвращает 404, но это не error в смысле исключения
    assertThat(response.statusCode()).isEqualTo(404);
    assertThat(response.success()).isFalse();
  }
}