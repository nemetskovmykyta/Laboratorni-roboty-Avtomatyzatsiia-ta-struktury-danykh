# Smart Library — Лабораторна робота №2

Веб-застосунок для автоматизації процесів бібліотеки, розроблений на базі **Java**, **Spring Boot**, **Spring Data JPA** та **Maven**.

---

## 📋 Основні можливості

- **Управління книгами**: створення та перегляд книг, автоматичне відстеження статусу (`AVAILABLE` / `LOANED`).
- **Реєстрація читачів**: збереження даних читачів із валідацією та гарантією унікальності `email`.
- **Видача книг**:
  - Стандартний термін користування: **14 днів**.
  - Перевірка доступності книги (не видається, якщо вже на руках).
  - Контроль ліміту активних позик відповідно до рівня надійності:
    - **Звичайний читач** — не більше **2** активних позик.
    - **Надійний читач** — не більше **5** активних позик.
- **Визначення рівня надійності читача**:
  Читач визнається **надійним**, якщо одночасно виконані 3 умови відносно моменту перевірки:
  1. За останні **30 днів** читач повернув щонайменше **3 книги**.
  2. Усі повернуті протягом цього періоду книги були повернені **не пізніше встановленого терміну** (`dueDate`).
  3. На момент перевірки читач **не має жодної простроченої активної позики**.
- **Повернення книг**:
  - Перевірка існування позики та того, що книга ще не повернута.
  - Фіксація фактичної дати повернення.
  - Зміна статусу книги назад на `AVAILABLE`.
  - Повне збереження історії позики в базі даних.
- **Перегляд активних позик** для конкретного читача.

---

## 🛠 Технологічний стек

- **Java**: 21+
- **Spring Boot**: 3.3.5
- **Spring Data JPA**: доступ та робота з реляційною БД
- **База даних**: H2 (in-memory) з увімкненою веб-консоллю
- **Збірка**: Apache Maven (включено Maven Wrapper `mvnw` / `mvnw.cmd`)
- **Документація API**: OpenAPI 3 / Swagger UI (`springdoc-openapi`)
- **Тестування**: JUnit 5, MockMvc, AssertJ, Spring Boot Test

---

## 📂 Структура проєкту

```
src/
├── main/
│   ├── java/com/smartlibrary/
│   │   ├── SmartLibraryApplication.java       # Точка входу в застосунок
│   │   ├── config/
│   │   │   ├── ClockConfig.java               # Бін системного годинника для гнучкого тестування дат
│   │   │   └── OpenApiConfig.java             # Налаштування Swagger / OpenAPI
│   │   ├── controller/
│   │   │   ├── BookController.java            # REST контролер книг
│   │   │   ├── ReaderController.java          # REST контролер читачів та перевірки надійності
│   │   │   └── LoanController.java            # REST контролер видачі та повернення книг
│   │   ├── dto/
│   │   │   ├── request/                       # DTO запитів з валідацією (jakarta.validation)
│   │   │   └── response/                      # DTO відповідей
│   │   ├── entity/
│   │   │   ├── Book.java                      # JPA сутність книги
│   │   │   ├── BookStatus.java                # Enum: AVAILABLE, LOANED
│   │   │   ├── Reader.java                    # JPA сутність читача
│   │   │   ├── Loan.java                      # JPA сутність позики (книга, читач, дати)
│   │   │   └── ReaderReliabilityStatus.java   # Enum: REGULAR (ліміт 2), RELIABLE (ліміт 5)
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java    # Централізована обробка помилок (@RestControllerAdvice)
│   │   │   ├── BookNotAvailableException.java
│   │   │   ├── EmailAlreadyExistsException.java
│   │   │   ├── LoanLimitExceededException.java
│   │   │   ├── LoanAlreadyReturnedException.java
│   │   │   └── ResourceNotFoundException.java
│   │   ├── repository/
│   │   │   ├── BookRepository.java
│   │   │   ├── ReaderRepository.java
│   │   │   └── LoanRepository.java
│   │   └── service/
│   │       ├── BookService.java
│   │       ├── ReaderService.java
│   │       ├── ReliabilityService.java        # Бізнес-логіка перевірки умов надійності
│   │       └── LoanService.java               # Логіка видачі/повернення з транзакційністю
│   └── resources/
│       └── application.properties             # Конфігурація застосунку та БД H2
└── test/
    ├── java/com/smartlibrary/
    │   ├── SmartLibraryApplicationTests.java
    │   ├── SmartLibraryIntegrationTest.java   # End-to-end інтеграційні тести API
    │   └── service/
    │       ├── BookServiceTest.java
    │       ├── ReaderServiceTest.java
    │       ├── LoanServiceTest.java           # Тести лімітів позик (2 vs 5) та повернень
    │       └── ReliabilityServiceTest.java    # Тести всіх 3 критеріїв надійності
    └── resources/
        └── application-test.properties
```

---

## 🚀 Запуск проєкту

### За допомогою Maven Wrapper

У кореневій директорії виконайте:

**Windows (PowerShell або CMD):**
```powershell
.\mvnw.cmd spring-boot:run
```

**Linux / macOS:**
```bash
./mvnw spring-boot:run
```

Застосунок запуститься на порту `8080`.

---

## 📖 Документація API та H2 Console

Після запуску застосунку доступні:
- **Swagger UI (інтерактивна документація API):**  
  [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON специфікація:**  
  [http://localhost:8080/api-docs](http://localhost:8080/api-docs)
- **H2 Web Console (перегляд таблиць БД):**  
  [http://localhost:8080/h2-console](http://localhost:8080/h2-console)  
  - JDBC URL: `jdbc:h2:mem:smartlibrarydb`  
  - User: `sa`  
  - Password: *(порожній)*

---

## 📡 REST API Ендпоінти

### 1. Читачі (`/api/readers`)

| Метод | URL | Опис |
|---|---|---|
| `POST` | `/api/readers` | Створення читача (перевіряє унікальність `email`) |
| `GET` | `/api/readers` | Отримання списку всіх читачів |
| `GET` | `/api/readers/{readerId}` | Отримання даних читача за ID |
| `GET` | `/api/readers/{readerId}/reliability` | **Перевірка надійності читача** (розрахунок за 30 днів) |
| `GET` | `/api/readers/{readerId}/loans/active` | **Отримання активних позик читача** |

### 2. Книги (`/api/books`)

| Метод | URL | Опис |
|---|---|---|
| `POST` | `/api/books` | **Створення книги** (початковий статус `AVAILABLE`) |
| `GET` | `/api/books` | **Отримання списку всіх книг** бібліотеки |
| `GET` | `/api/books/{bookId}` | Отримання книги за ID |

### 3. Позики (`/api/loans`)

| Метод | URL | Опис |
|---|---|---|
| `POST` | `/api/loans` | **Видача книги читачу** (термін 14 днів, перевірка доступності та ліміту) |
| `POST` | `/api/loans/{loanId}/return` | **Повернення книги** (фіксація дати повернення, книга стає `AVAILABLE`) |
| `GET` | `/api/loans/{loanId}` | Отримання інформації про позику за ID |
| `GET` | `/api/loans` | Список усіх позик |

---

## 🧪 Приклади запитів (cURL)

### Створення читача
```bash
curl -X POST http://localhost:8080/api/readers \
  -H "Content-Type: application/json" \
  -d '{"name": "Іван Петренко", "email": "ivan.petrenko@example.com", "phone": "+380501112233"}'
```

### Створення книги
```bash
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"title": "Алгоритми та структури даних", "author": "Автор", "isbn": "978-0000000001"}'
```

### Видача книги
```bash
curl -X POST http://localhost:8080/api/loans \
  -H "Content-Type: application/json" \
  -d '{"readerId": 1, "bookId": 1}'
```

### Перевірка надійності читача
```bash
curl http://localhost:8080/api/readers/1/reliability
```
**Приклад відповіді:**
```json
{
  "readerId": 1,
  "readerName": "Іван Петренко",
  "readerEmail": "ivan.petrenko@example.com",
  "status": "REGULAR",
  "reliable": false,
  "maxAllowedLoans": 2,
  "currentActiveLoansCount": 1,
  "returnedInLast30DaysCount": 0,
  "allReturnsOnTimeInLast30Days": true,
  "hasOverdueActiveLoans": false,
  "evaluationDate": "2026-11-20",
  "explanation": "Читач має рівень 'ЗВИЧАЙНИЙ' (максимум 2 активних позик). Не виконано умови надійності: повернуто лише 0 книг за останні 30 днів (вимагається щонайменше 3)."
}
```

### Перегляд активних позик читача
```bash
curl http://localhost:8080/api/readers/1/loans/active
```

### Повернення книги
```bash
curl -X POST http://localhost:8080/api/loans/1/return
```

---

## 🧪 Запуск тестів

Для виконання всіх 22 автоматичних тестів:
```powershell
.\mvnw.cmd test
```
Тести покривають:
- Реєстрацію читачів та перевірку дублікатів email.
- Валідацію вхідних даних (DTO).
- Доступність книг та зміну статусів `AVAILABLE` / `LOANED`.
- Ліміти на кількість активних книг (2 для звичайних, 5 для надійних).
- Повернення книг та заборону повторного повернення.
- Усі три умови розрахунку надійності читача.
- Повний наскрізний життєвий цикл роботи через REST API.
