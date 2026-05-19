package ru.learning.java.clients.api.data_driven.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.learning.java.models.immutable.ImmutablePostRequest;
import ru.learning.java.models.immutable.PostRequest;
import ru.learning.java.utils.TestDataLoader;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PostDataProviderTest {

  private static final String BASE_URL = "https://jsonplaceholder.typicode.com";
  private final ObjectMapper objectMapper = new ObjectMapper();

  @DataProvider(name = "validPosts")
  public static Object[][] validPosts() {
    List<PostRequest> posts =
      TestDataLoader.loadJsonList("/test-data/posts.json", PostRequest.class);

    return posts.stream()
                .map(post -> new Object[]{post})
                .toArray(Object[][]::new);
  }

  @Test(dataProvider = "validPosts")
  public void createPostFromDataProvider(PostRequest request) throws Exception {
    Response response = given()
      .contentType("application/json")
      .body(objectMapper.writeValueAsString(request))
      .when()
      .post(BASE_URL + "/posts")
      .then()
      .statusCode(201)
      .extract().response();

    assertThat(response.jsonPath().getInt("id")).isPositive();
  }

  @DataProvider(name = "invalidInlinePosts")
  public Object[][] invalidInlinePosts() {
    return new Object[][]{
      {"zero user and empty text", 0L, "", ""},
      {"second invalid inline row", 0L, "", ""}
    };
  }

  @Test(dataProvider = "invalidInlinePosts")
  public void rejectInvalidPostBeforeHttpRequest(String caseName, long userId, String title, String body) {
    assertThatThrownBy(() -> ImmutablePostRequest.builder()
                                                 .userId(userId)
                                                 .title(title)
                                                 .body(body)
                                                 .build())
      .as(caseName)
      .isInstanceOf(IllegalStateException.class);
  }
}
