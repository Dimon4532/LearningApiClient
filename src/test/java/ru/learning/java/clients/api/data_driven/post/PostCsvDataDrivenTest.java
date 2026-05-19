package ru.learning.java.clients.api.data_driven.post;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import ru.learning.java.models.immutable.ImmutablePostRequest;

@Feature("Data-Driven Testing")
@DisplayName("Data-Driven — CSV источник для posts")
class PostCsvDataDrivenTest extends AbstractPostDataDrivenTest {

  @ParameterizedTest(name = "[{index}] POST /posts userId={0} title=\"{1}\"")
  @CsvFileSource(resources = "/test-data/posts.csv", numLinesToSkip = 1)
  @DisplayName("Создание постов по данным из CSV")
  void createPostFromCsv(long userId, String title, String body) throws Exception {
    createPostAndAssertCreated(
      ImmutablePostRequest.builder().userId(userId).title(title).body(body).build()
    );
  }
}
