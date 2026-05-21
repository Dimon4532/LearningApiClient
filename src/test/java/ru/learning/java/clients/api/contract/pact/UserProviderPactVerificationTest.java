package ru.learning.java.clients.api.contract.pact;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
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

@Provider("JsonPlaceholderUsersApi")
@PactFolder("target/pacts")
@ExtendWith(PactVerificationInvocationContextProvider.class)
public class UserProviderPactVerificationTest {

  private static HttpServer server;

  @BeforeAll
  static void startStubProvider() throws IOException {
    server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext("/users/1", exchange -> {
      String json = """
                {"id":1,"name":"Leanne Graham","username":"Bret","email":"Sincere@april.biz"}
                """;
      byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
      exchange.sendResponseHeaders(200, bytes.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(bytes);
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

  @State("user with id=1 exists")
  void userExists() {
    //Оставим пока пустым.
  }

  @AfterAll
  static void stopStubProvider() {
    if (server != null) server.stop(0);
  }
}
