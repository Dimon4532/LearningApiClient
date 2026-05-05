package ru.learning.java.suites;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import ru.learning.java.clients.api.ApiClientTest;
import ru.learning.java.clients.api.ApiKeyClientTest;
import ru.learning.java.clients.api.AuthApiClientTest;
import ru.learning.java.clients.api.DigestApiClientTest;
import ru.learning.java.clients.api.FormApiClientTest;
import ru.learning.java.clients.api.JwtApiClientTest;
import ru.learning.java.clients.api.MultipartApiClientTest;
import ru.learning.java.clients.api.OAuthApiClientTest;
import ru.learning.java.clients.api.SoapApiClientTest;
import ru.learning.java.clients.api.UserApiContractTest;
import ru.learning.java.clients.api.XmlApiTest;


@Suite
@SuiteDisplayName("API Clients Full Test Suite")
@SelectClasses({
  ApiClientTest.class,
  ApiKeyClientTest.class,
  AuthApiClientTest.class,
  DigestApiClientTest.class,
  FormApiClientTest.class,
  JwtApiClientTest.class,
  MultipartApiClientTest.class,
  OAuthApiClientTest.class,
  SoapApiClientTest.class,
  UserApiContractTest.class,
  XmlApiTest.class
})
public class ApiTestSuite {
}