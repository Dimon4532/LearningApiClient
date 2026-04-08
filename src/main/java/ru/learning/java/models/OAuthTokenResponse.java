package ru.learning.java.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuthTokenResponse {

  @JsonProperty("access_token")
  private String accessToken;

  @JsonProperty("refresh_token")
  private String refreshToken;

  @JsonProperty("token_type")
  private String tokenType;// "Bearer"

  @JsonProperty("expires_in")
  private Long expiresIn;

  @JsonProperty("scope")
  private String scope;

  // Удобный метод для проверки истечения в тестах
  public boolean isExpired(Instant issuedAt) {
    return Instant.now().isAfter(issuedAt.plusSeconds(expiresIn));
  }
}
