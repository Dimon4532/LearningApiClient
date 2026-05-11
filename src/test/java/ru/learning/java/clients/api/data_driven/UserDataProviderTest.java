package ru.learning.java.clients.api.data_driven;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.learning.java.models.CreateUserRequest;
import ru.learning.java.utils.TestDataLoader;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * TestNG @DataProvider.
 * Источник данных — JSON-файл, который читается один раз и поставляется
 * в тестовый метод по строкам.
 */
public class UserDataProviderTest {

  private static final String BASE_URL = "https://jsonplaceholder.typicode.com";
  private final ObjectMapper objectMapper = new ObjectMapper();

  @DataProvider(name = "validUsers")
  public static Object[][] validUsers() {
    List<CreateUserRequest> users =
      TestDataLoader.loadJsonList("/test-data/users.json", CreateUserRequest.class);

    return users.stream()
                .map(u -> new Object[]{u})
                .toArray(Object[][]::new);
  }

  @Test(dataProvider = "validUsers")
  public void createUserFromDataProvider(CreateUserRequest request) throws Exception {
    Response response = given()
      .contentType("application/json")
      .body(objectMapper.writeValueAsString(request))
      .when()
      .post(BASE_URL + "/users")
      .then()
      .statusCode(201)
      .extract().response();

    assertThat(response.jsonPath().getInt("id")).isPositive();
  }

  // В качестве альтернативы inline-данные не из файла
  @DataProvider(name = "inlineUsers")
  public Object[][] inlineUsers() {
    return new Object[][]{
      {"John", "john@example.com", "john_doe"},
      {"Jane", "jane@example.com", "jane_roe"}
    };
  }

  @Test(dataProvider = "inlineUsers")
  public void createUserInline(String name, String email, String username) {
    CreateUserRequest request = CreateUserRequest.builder()
                                                 .name(name).email(email).username(username).build();

    assertThat(request.getEmail()).contains("@");
  }
}