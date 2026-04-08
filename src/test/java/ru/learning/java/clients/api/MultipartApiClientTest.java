package ru.learning.java.clients.api;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import ru.learning.java.clients.api.base.BaseApiTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@Epic("REST Assured Demo")
@Feature("MultipartApiClient")
@DisplayName("Тесты MultipartApiClient")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MultipartApiClientTest extends BaseApiTest {

  @Test
  @Order(1)
  @Story("Multipart")
  @DisplayName("1. Загрузка текстового файла")
  void testUploadTextFile() throws IOException {
    File tempFile = File.createTempFile("test-upload", ".txt");
    tempFile.deleteOnExit();
    Files.writeString(tempFile.toPath(), "Hello from REST Assured!");

    Response response = multipartApiClient
      .uploadFile(HTTPBIN_URL + "/post", 200, tempFile, "file", new HashMap<>())
      .extract().response();

    assertThat(response.jsonPath().getString("files.file")).isEqualTo("Hello from REST Assured!");
  }

  @Test
  @Order(2)
  @Story("Multipart")
  @DisplayName("2. Загрузка файла из classpath")
  void testUploadFileFromClasspath() {
    File file = new File(Objects.requireNonNull(
      getClass().getClassLoader().getResource("test-data/sample.txt")).getFile());

    Response response = multipartApiClient
      .uploadFile(HTTPBIN_URL + "/post", 200, file, "file", new HashMap<>())
      .extract().response();

    assertThat(response.jsonPath().getString("files.file")).isNotEmpty();
  }

  @Test
  @Order(3)
  @Story("Multipart")
  @DisplayName("3. Файл + поля формы")
  void testUploadFileWithMetadata() throws IOException {
    File tempFile = File.createTempFile("doc", ".txt");
    tempFile.deleteOnExit();
    Files.writeString(tempFile.toPath(), "Document content");

    Map<String, String> formFields = Map.of(
      "author", "John Doe",
      "description", "Test document"
    );

    Response response = multipartApiClient
      .uploadFileWithFormData(HTTPBIN_URL + "/post", 200, tempFile, "file", formFields, new HashMap<>())
      .extract().response();

    assertThat(response.jsonPath().getString("form.author")).isEqualTo("John Doe");
    assertThat(response.jsonPath().getString("form.description")).isEqualTo("Test document");
  }

  @Test
  @Order(4)
  @Story("Multipart")
  @DisplayName("4. Inline multipart без клиента")
  void testMultipartInline() throws IOException {
    File tempFile = File.createTempFile("inline", ".json");
    tempFile.deleteOnExit();
    Files.writeString(tempFile.toPath(), "{\"key\":\"value\"}");

    Response response = given()
      .contentType(ContentType.MULTIPART)
      .multiPart("file", tempFile, "application/json")
      .multiPart("category", "test-category")
      .post(HTTPBIN_URL + "/post")
      .then().statusCode(200)
      .extract().response();

    assertThat(response.jsonPath().getString("form.category")).isEqualTo("test-category");
  }
}