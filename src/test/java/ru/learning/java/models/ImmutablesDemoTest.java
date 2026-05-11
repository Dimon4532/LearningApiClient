package ru.learning.java.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.learning.java.models.immutable.Address;
import ru.learning.java.models.immutable.ImmutableAddress;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DisplayName("Immutables — демонстрация builder + проверки инвариантов")
public class ImmutablesDemoTest {
  @Test
  @DisplayName("Builder создаёт корректный объект и обеспечивает иммутабельность")
  void immutableBuilderWorks() {
    Address addr = ImmutableAddress.builder()
                                   .street("Lenina 1")
                                   .city("Moscow")
                                   .zipcode("101000")
                                   .build();

    assertThat(addr.country()).isEqualTo("RU"); // @Value.Default
    // withXxx создаёт копию с изменённым полем — оригинал не меняется
    Address copy = ImmutableAddress.copyOf(addr).withCity("Kazan");
    assertThat(copy.city()).isEqualTo("Kazan");
    assertThat(addr.city()).isEqualTo("Moscow");
  }

  @Test
  @DisplayName("@Value.Check падает на невалидных данных")
  void immutableCheckGuardsInvariants() {
    assertThatThrownBy(() ->
      ImmutableAddress.builder().street(" ").city("X").zipcode("1").build()
    ).isInstanceOf(IllegalStateException.class)
     .hasMessageContaining("street");
  }
}
