package ru.learning.java.actors;

import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import lombok.extern.slf4j.Slf4j;

/**
 * Супервизор для управления HTTP акторами
 * Демонстрирует паттерн супервизора в акторной модели
 */
@Slf4j
public class HttpSupervisor extends AbstractBehavior<HttpSupervisor.Command> {

  public interface Command {}

  public record StartRequest(String requestId, String url, ActorRef<HttpRequestActor.Response> replyTo) implements Command {}

  public record StartPostRequest(String requestId, String url, String body, ActorRef<HttpRequestActor.Response> replyTo) implements Command {}

  public static Behavior<Command> create() {
    return Behaviors.setup(HttpSupervisor::new);
  }

  public HttpSupervisor(ActorContext<Command> context) {
    super(context);
    log.info("HttpSupervisor создан");
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
      .onMessage(StartRequest.class, this::onStartRequest)
      .onMessage(StartPostRequest.class, this::onStartPostRequest)
      .build();
  }

  private Behavior<Command> onStartRequest(StartRequest command) {
    log.info("Создание актора для GET запроса: {}", command.requestId());

    // Создаем нового актора для каждого запроса с автоматическим именем
    ActorRef<HttpRequestActor.Command> httpActor =
      getContext().spawnAnonymous(HttpRequestActor.create());

    // Отправляем команду актору
    httpActor.tell(new HttpRequestActor.ExecuteGetRequest(command.url(), command.replyTo()));

    return this;
  }

  private Behavior<Command> onStartPostRequest(StartPostRequest command) {
    log.info("Создание актора для POST запроса: {}", command.requestId());

    // Создаем нового актора для каждого запроса с автоматическим именем
    ActorRef<HttpRequestActor.Command> httpActor =
      getContext().spawnAnonymous(HttpRequestActor.create());

    httpActor.tell(new HttpRequestActor.ExecutePostRequest(command.url(), command.body(), command.replyTo()));

    return this;
  }
}