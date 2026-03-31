# 🚀 REST Assured API Testing - Учебный проект

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![REST Assured](https://img.shields.io/badge/REST%20Assured-5.5.7-green.svg)](https://rest-assured.io/)
[![Apache Pekko](https://img.shields.io/badge/Apache%20Pekko-1.1.3-blue.svg)](https://pekko.apache.org/)
[![JUnit 5](https://img.shields.io/badge/JUnit-5.11.4-red.svg)](https://junit.org/junit5/)
[![Allure](https://img.shields.io/badge/Allure-2.32.0-yellow.svg)](https://docs.qameta.io/allure/)

Комплексный учебный проект для изучения тестирования REST API с использованием современного стека технологий Java.

## 📋 Содержание

- [О проекте](#-о-проекте)
- [Технологии](#-технологии)
- [Быстрый старт](#-быстрый-старт)
- [Примеры использования](#-примеры-использования)
- [API Клиенты](#-api-клиенты)
- [Тестовые сценарии](#-тестовые-сценарии)
- [Allure отчеты](#-allure-отчеты)
- [Акторная модель](#-акторная-модель)
- [Дальнейшее развитие](#-дальнейшее-развитие)
- [Полезные ресурсы](#-полезные-ресурсы)

## 🎯 О проекте

Этот проект создан для обучения и демонстрации лучших практик тестирования REST API в Java. Он включает:

- ✅ **39+ готовых тестовых сценариев** для различных HTTP методов и типов аутентификации
- ✅ **Гибкую архитектуру** с паттерном Page Object (API Client)
- ✅ **Интеграцию с Allure** для красивых отчетов
- ✅ **Акторную модель** с Apache Pekko для асинхронных запросов
- ✅ **Поддержку различных типов аутентификации** (Basic Auth, Bearer Token)
- ✅ **Работу с различными форматами** (JSON, Form Data)

## 🛠 Технологии

| Технология       | Версия  | Назначение                           |
|------------------|---------|--------------------------------------|
| **Java**         | 21      | Основной язык программирования       |
| **REST Assured** | 5.5.7   | Библиотека для тестирования REST API |
| **JUnit 5**      | 5.11.4  | Фреймворк для написания тестов       |
| **AssertJ**      | 3.27.7  | Fluent assertions библиотека         |
| **Allure**       | 2.32.0  | Генерация отчетов                    |
| **Apache Pekko** | 1.1.3   | Акторная модель для асинхронности    |
| **Lombok**       | 1.18.40 | Уменьшение boilerplate кода          |
| **Jackson**      | 2.21.2  | JSON сериализация/десериализация     |
| **Logback**      | 1.5.32  | Логирование                          |
| **Maven**        | -       | Система сборки                       |

## 🚀 Быстрый старт

### Предварительные требования

- **Java 21** или выше
- **Maven 3.6+**
- **IDE** (IntelliJ IDEA рекомендуется)

### Установка

1. **Клонируйте репозиторий:**
   bash git clone <your-repo-url> cd test-project-3
2. **Соберите проект:**
   bash mvn clean install
3. **Запустите тесты:**
   bash mvn test
4. **Сгенерируйте Allure отчет:**
   bash mvn allure:report
5. **Откройте отчет:**
   bash mvn allure:serve

## 💡 Примеры использования

### Простой GET запрос

java 
@Test 
void testSimpleGetRequest() { 
Response response = apiClient.sendGet((), new HashMap<>(), new HashMap<>(), new HashMap<>) )">.extract().response();
assertThat(response.statusCode()).isEqualTo(200);
}

### POST запрос с JSON 
java 
@Test 
void testPostWithJson() throws JsonProcessingException { 
Post newPost = new Post(1L, null, "Title", "Content");
Response response = apiClient.sendPost(
BASE_URL + "/posts",
201,
objectMapper.writeValueAsString(newPost),
new HashMap<>(),
new HashMap<>(),
new HashMap<>()
).extract().response();

assertThat(response.jsonPath().getInt("id")).isGreaterThan(0);
}

## 🔧 API Клиенты

### ApiClient (базовый)
Содержит методы для стандартных HTTP операций:
- `sendGet()` - GET запросы
- `sendPost()` - POST запросы
- `sendPut()` - PUT запросы
- `sendPatch()` - PATCH запросы
- `sendDelete()` - DELETE запросы

### AuthApiClient (с аутентификацией)
Расширяет `ApiClient` и добавляет:
- **Basic Auth**: `sendGetWithAuth()`, `sendPostWithAuth()`, etc.
- **Bearer Token**: `sendGetWithBearerToken()`, `sendPostWithBearerToken()`, etc.
- `sendPostForToken()` - получение токена

### FormApiClient (для form-data)
Работа с `application/x-www-form-urlencoded`:
- `sendPostWithFormParams()` - POST с form параметрами
- `sendPutWithFormParams()` - PUT с form параметрами
- `sendPatchWithFormParams()` - PATCH с form параметрами

## 🧪 Тестовые сценарии

Проект включает **39 тестов**, сгруппированных по темам:

### 1-13: Базовые операции (ApiClient)
- ✅ GET запросы (простые, с параметрами, с path params)
- ✅ POST запросы (JSON, объекты)
- ✅ PUT/PATCH запросы
- ✅ DELETE запросы
- ✅ Валидация JSON структуры
- ✅ Десериализация в объекты
- ✅ Проверка заголовков и времени ответа

### 14-19: Basic Authentication (AuthApiClient)
- ✅ GET с Basic Auth (успех/провал)
- ✅ POST/PUT/PATCH/DELETE с Basic Auth

### 20-24: Form Parameters (FormApiClient)
- ✅ POST с form parameters
- ✅ Работа с cookies
- ✅ PUT/PATCH с form data

### 25-31: Комплексные сценарии
- ✅ Работа с комментариями
- ✅ Создание пользователей (Builder pattern)
- ✅ Интеграционные тесты

### 32-39: Bearer Token Authentication
- ✅ Получение токена
- ✅ GET/POST/PUT/PATCH/DELETE с Bearer Token
- ✅ Полный цикл аутентификации
- ✅ Негативные тесты

## 📊 Allure отчеты

Проект использует Allure для создания детальных отчетов

**Запуск отчета:**
   bash mvn allure:serve

## 🎭 Акторная модель

Демонстрация асинхронных запросов с Apache Pekko:

java 
@Test 
void testGetRequestWithActor() throws ExecutionException, InterruptedException {
CompletionStage<HttpRequestActor.Response> result = AskPattern.ask;
HttpRequestActor.Response response = result.toCompletableFuture().get();
assertThat(response.success()).isTrue();
}

## 🔄 Дальнейшее развитие

### 🎓 Уровень 1: Базовые улучшения

1. **Расширение покрытия API**
   - Добавить тесты для файловой загрузки (multipart/form-data)
   - Работа с XML (кроме JSON)
   - Тесты для SOAP сервисов

2. **Больше типов аутентификацией**
   - OAuth 2.0 (полный flow)
   - API Keys
   - JWT токены (с проверкой expiration)
   - Digest Authentication

3. **Улучшение моделей данных**
   - Добавить валидацию через Bean Validation (JSR-303)
   - Создать builders для всех моделей
   - Добавить примеры использования Immutables

4. **Data-Driven тестирование**
   - Параметризованные тесты с `@ParameterizedTest`
   - Чтение тестовых данных из CSV/JSON файлов
   - Использование TestNG DataProvider

### 🚀 Уровень 2: Продвинутые техники

5. **CI/CD Integration**
   - GitHub Actions / GitLab CI конфигурация
   - Docker контейнеры для тестов
   - Автоматическая публикация Allure отчетов

6. **Performance Testing**
   - Интеграция с Gatling
   - Load testing с JMeter
   - Метрики производительности в отчетах

7. **Contract Testing**
   - Spring Cloud Contract
   - Pact для consumer-driven contracts
   - OpenAPI/Swagger спецификации

8. **Mock серверы**
   - WireMock для мокирования API
   - MockServer интеграция
   - Тесты с локальным окружением

9. **Улучшение архитектуры**
   - Dependency Injection (Spring/Guice)
   - Retry механизмы (Resilience4j)
   - Circuit Breaker паттерн
   - Rate limiting

### 🎯 Уровень 3: Enterprise решения

10. **Database Testing**
   - Интеграция с TestContainers
   - Проверка данных в БД после API вызовов
   - Flyway/Liquibase для миграций

11. **Security Testing**
   - OWASP ZAP интеграция
   - Security headers валидация
   - SQL injection тесты
   - XSS проверки

12. **Monitoring & Observability**
   - Prometheus metrics
   - ELK Stack для логов
   - Distributed tracing (Jaeger/Zipkin)

13. **Advanced Reporting**
   - Кастомные Allure plugins
   - Интеграция с Jira/TestRail
   - Slack/Email уведомления
   - Тренды качества (historical reports)

14. **GraphQL Testing**
   - GraphQL запросы и мутации
   - Schema validation
   - Subscription тесты

15. **WebSocket Testing**
   - Real-time communication тесты
   - Socket.IO интеграция

### 💼 Практические задания

16. **Создать тесты для реального API**
   - GitHub API
   - Swagger Petstore
   - Rick and Morty API
   - Pokemon API

17. **Реализовать паттерны**
   - Builder для сложных запросов
   - Chain of Responsibility для middleware
   - Strategy для различных auth механизмов
   - Factory для создания клиентов

18. **Документация**
   - API документация с Swagger
   - JavaDoc для всех публичных методов
   - Confluence/Wiki страницы
   - Video tutorials

### 🎨 Дополнительные идеи

19. **Кросс-платформенное тестирование**
   - Сравнение REST vs GraphQL vs gRPC
   - Миграция с RestAssured на другие библиотеки
   - Polyglot тесты (Java + Python + JavaScript)

20. **AI/ML интеграция**
   - Автоматическая генерация тестов из Swagger
   - Анализ логов с помощью ML
   - Предсказание потенциальных багов

## 📚 Полезные ресурсы

### Официальная документация
- [REST Assured Guide](https://rest-assured.io/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Allure Documentation](https://docs.qameta.io/allure/)
- [Apache Pekko Documentation](https://pekko.apache.org/docs/pekko/current/)

### Обучающие материалы
- [REST API Testing with REST Assured](https://testautomationu.applitools.com/automating-your-api-tests-with-rest-assured/)
- [Java Testing with JUnit 5](https://www.baeldung.com/junit-5)
- [Allure Report Tutorial](https://www.youtube.com/watch?v=gUPzhDR2f1E)

### Практика
- [JSONPlaceholder](https://jsonplaceholder.typicode.com/) - Free fake API
- [HTTPBin](https://httpbin.org/) - HTTP testing service
- [ReqRes](https://reqres.in/) - Test API

## 📝 Лицензия

Этот проект распространяется под лицензией MIT. Используйте его свободно для обучения и практики!

## 👨‍💻 Автор

Создано для изучения современных практик API тестирования в Java.

---

**Happy Testing! 🚀**

*Не забудьте поставить ⭐ если проект был вам полезен!*