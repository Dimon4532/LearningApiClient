package ru.learning.java.suites;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import ru.learning.java.clients.api.ApiClientTest;
import ru.learning.java.clients.api.AuthApiClientTest;
import ru.learning.java.clients.api.FormApiClientTest;
import ru.learning.java.clients.api.MultipartApiClientTest;
import ru.learning.java.clients.api.SoapApiClientTest;
import ru.learning.java.clients.api.XmlApiTest;

@Suite
@SuiteDisplayName("API Clients Full Test Suite")
@SelectClasses({
  ApiClientTest.class,
  AuthApiClientTest.class,
  FormApiClientTest.class,
  MultipartApiClientTest.class,
  XmlApiTest.class,
  SoapApiClientTest.class
})
public class ApiTestSuite {
}