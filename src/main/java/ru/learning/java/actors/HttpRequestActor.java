package ru.learning.java.actors;

import lombok.extern.slf4j.Slf4j;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import ru.learning.java.clients.api.ApiClient;

import java.util.HashMap;

/**
 * Актор для выполнения HTTP запросов
 */
@Slf4j
public class HttpRequestActor extends AbstractBehavior<HttpRequestActor.Command> {

  private final ApiClient apiClient;

  public interface Command {
  }

  public record ExecuteGetRequest(String url, ActorRef<Response> replyTo) implements Command {
  }

  public record ExecutePostRequest(String url, String body, ActorRef<Response> replyTo) implements Command {
  }

  public record Response(int statusCode, String body, boolean success) implements Command {
  }

  public static Behavior<Command> create() {
    return Behaviors.setup(HttpRequestActor::new);
  }

  public HttpRequestActor(ActorContext<Command> context) {
    super(context);
    this.apiClient = new ApiClient();
    log.info("HttpRequestActor создан: {}", context.getSelf().path());
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
      .onMessage(ExecuteGetRequest.class, this::onExecuteGetRequest)
      .onMessage(ExecutePostRequest.class, this::onExecutePostRequest)
      .build();
  }

  /**
   * Обработка GET запроса
   */
  private Behavior<Command> onExecuteGetRequest(ExecuteGetRequest request) {
    log.info("Получена команда ExecuteGetRequest для URL: {}", request.url());

    try {
      io.restassured.response.Response response = apiClient.sendGet(
        request.url(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>()
      ).extract().response();

      int statusCode = response.getStatusCode();
      String body = response.getBody().asString();
      boolean success = statusCode >= 200 && statusCode < 300;

      log.info("GET запрос выполнен. Статус: {}, Success: {}", statusCode, success);

      // Отправляем ответ обратно
      request.replyTo().tell(new Response(statusCode, body, success));

    } catch (Exception e) {
      log.error("Ошибка при выполнении GET запроса: {}", e.getMessage(), e);
      request.replyTo().tell(new Response(500, e.getMessage(), false));
    }

    return this;
  }

  /**
   * Обработка POST запроса
   */
  private Behavior<Command> onExecutePostRequest(ExecutePostRequest request) {
    log.info("Получена команда ExecutePostRequest для URL: {}", request.url());

    try {
      io.restassured.response.Response response = apiClient.sendPost(
        request.url(),
        201,
        request.body(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>()
      ).extract().response();

      int statusCode = response.getStatusCode();
      String body = response.getBody().asString();
      boolean success = statusCode >= 200 && statusCode < 300;

      log.info("POST запрос выполнен. Статус: {}, Success: {}", statusCode, success);

      request.replyTo().tell(new Response(statusCode, body, success));

    } catch (Exception e) {
      log.error("Ошибка при выполнении POST запроса: {}", e.getMessage(), e);
      request.replyTo().tell(new Response(500, e.getMessage(), false));
    }

    return this;
  }
}