package ru.learning.java.clients.api.contract.pact;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

@Provider("JsonPlaceholderCommentsApi")
@PactFolder("target/pacts")
@ExtendWith(PactVerificationInvocationContextProvider.class)
public class CommentProviderPactVerificationTest {

  private static HttpServer server;

  @BeforeAll
  static void startStub() throws IOException {
    server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext("/comments/1", exchange -> {
      if ("GET".equals(exchange.getRequestMethod())) {
        respond(exchange, 200, """
          {"postId":1,"id":1,"name":"id labore ex et quam laborum","email":"Eliseo@gardner.biz","body":"laudantium enim quasi est quidem magnam"}
          """);
      } else {
        respond(exchange, 405, "");
      }
    });
    server.createContext("/comments", exchange -> {
      if ("GET".equals(exchange.getRequestMethod()) && "postId=1".equals(exchange.getRequestURI().getQuery())) {
        respond(exchange, 200, """
          [{"postId":1,"id":1,"name":"id labore ex et quam laborum","email":"Eliseo@gardner.biz","body":"laudantium enim quasi est quidem magnam"}]
          """);
      } else {
        respond(exchange, 404, "");
      }
    });
    server.createContext("/posts/1/comments", exchange -> {
      if ("POST".equals(exchange.getRequestMethod())) {
        respond(exchange, 201, """
          {"postId":"1","id":501,"name":"Jane Doe","email":"jane@doe.com","body":"Looks great!"}
          """);
      } else {
        respond(exchange, 405, "");
      }
    });
    server.start();
  }

  @BeforeEach
  void setTarget(PactVerificationContext context) {
    context.setTarget(new HttpTestTarget("localhost", server.getAddress().getPort()));
  }

  @TestTemplate
  void verifyPact(PactVerificationContext context) {
    context.verifyInteraction();
  }

  @State("comment with id=1 exists")
  void commentExists() {
    // Данные уже встроены в стаб.
  }

  @State("comments for post with id=1 exist")
  void commentsForPostExist() {
    // Данные уже встроены в стаб.
  }

  @State("post with id=1 exists")
  void postExists() {
    // Данные уже встроены в стаб.
  }

  @AfterAll
  static void stopStub() {
    if (server != null) server.stop(0);
  }

  private static void respond(HttpExchange exchange, int statusCode, String json) throws IOException {
    byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
    exchange.sendResponseHeaders(statusCode, bytes.length);
    try (OutputStream os = exchange.getResponseBody()) {
      os.write(bytes);
    }
  }
}
