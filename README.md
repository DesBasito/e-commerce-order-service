# Order Service

## 📋 Описание

Order Service - это центральный микросервис для управления заказами в e-commerce системе. Он координирует процесс создания заказа, взаимодействуя с другими сервисами для проверки клиентов, резервирования товаров, обработки платежей и отправки уведомлений.

## 🎯 Основные функции

- Создание новых заказов
- Валидация данных заказа
- Взаимодействие с Customer Service для проверки клиентов
- Взаимодействие с Product Service для резервирования товаров
- Интеграция с Payment Service для обработки платежей
- Отправка уведомлений через Kafka
- Управление Order Lines (детали заказа)
- Получение списка заказов и деталей

## ⚙️ Технический стек

- **Java 17**
- **Spring Boot 3.5.3**
- **Spring Data JPA**
- **Spring Cloud OpenFeign** (для межсервисного взаимодействия)
- **Spring Cloud Config Client**
- **Spring Cloud Netflix Eureka Client**
- **Spring Kafka** (для асинхронных уведомлений)
- **PostgreSQL**
- **Lombok**
- **Maven**

## 🗄️ Модель данных

### Order Entity
```java
@Entity
public class Order {
    @Id
    @GeneratedValue
    private Integer id;
    
    @Column(unique = true, nullable = false)
    private String reference;
    
    private BigDecimal totalAmount;
    
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    
    private String customerId;
    
    @OneToMany(mappedBy = "order")
    private List<OrderLine> orderLines;
    
    @CreatedDate
    private LocalDateTime createdDate;
    
    @LastModifiedDate
    private LocalDateTime lastModifiedDate;
}
```

### OrderLine Entity
```java
@Entity
@Table(name = "customer_line")
public class OrderLine {
    @Id
    @GeneratedValue
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
    
    private Integer productId;
    private double quantity;
}
```

### PaymentMethod Enum
```java
public enum PaymentMethod {
    PAYPAL,
    CREDIT_CARD,
    VISA,
    MASTER_CARD,
    BITCOIN
}
```

## 🚀 Запуск сервиса

### Предварительные условия
- Config Server (http://localhost:8888)
- Discovery Service (http://localhost:8761)
- Customer Service (http://localhost:8090)
- Product Service (http://localhost:8050)
- Payment Service
- PostgreSQL (localhost:5555)
- Kafka (для уведомлений)
- Java 17+
- Maven 3.6+

### Локальный запуск

```bash
# Клонирование репозитория
git clone <repository-url>
cd services/order

# Сборка проекта
./mvnw clean install

# Запуск сервиса
./mvnw spring-boot:run
```

## 🔧 Конфигурация

### application.yml
```yaml
spring:
  config:
    import: optional:configserver:http://localhost:8888
  application:
    name: order-service
```

### order-service.yml (в Config Server)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5555/order
    username: qwe
    password: qwe
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    database: postgresql
    database-platform: org.hibernate.dialect.PostgreSQLDialect

application:
  config:
    customer-url: http://localhost:8090/api/v1/customer
    product-url: http://localhost:8050/api/v1/products
    payment-url: http://localhost:8080/api/v1/payments

server:
  port: 8070
```

## 🌐 API Endpoints

### Создание заказа
```http
POST /api/v1/orders
Content-Type: application/json

{
  "reference": "ORD-2024-001",
  "amount": 2899.97,
  "paymentMethod": "CREDIT_CARD",
  "customerId": "507f1f77bcf86cd799439011",
  "products": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 2,
      "quantity": 1
    }
  ]
}
```

**Ответ:**
```json
15
```

### Получение всех заказов
```http
GET /api/v1/orders
```

**Ответ:**
```json
[
  {
    "id": 15,
    "reference": "ORD-2024-001",
    "amount": 2899.97,
    "paymentMethod": "CREDIT_CARD",
    "customerId": "507f1f77bcf86cd799439011"
  }
]
```

### Получение заказа по ID
```http
GET /api/v1/orders/{order-id}
```

**Ответ:**
```json
{
  "id": 15,
  "reference": "ORD-2024-001", 
  "amount": 2899.97,
  "paymentMethod": "CREDIT_CARD",
  "customerId": "507f1f77bcf86cd799439011"
}
``` 

### Получение Order Lines по ID заказа
```http
GET /api/v1/order-lines/order/{order-id}
```

**Ответ:**
```json
[
  {
    "id": 25,
    "quantity": 2.0
  },
  {
    "id": 26,
    "quantity": 1.0
  }
]
```

## 🏗️ Архитектура

### Структура пакетов
```
kg.manurov.ecommerce/
├── controller/
│   ├── OrderController.java
│   └── OrderLineController.java
├── dto/
│   ├── order/
│   │   ├── OrderRequest.java
│   │   └── OrderResponse.java
│   └── orderLine/
│       ├── OrderLineRequest.java
│       └── OrderLineResponse.java
├── feign/
│   ├── customer/
│   │   ├── CustomerClient.java
│   │   └── CustomerResponse.java
│   ├── payment/
│   │   ├── PaymentClient.java
│   │   └── PaymentRequest.java
│   └── product/
│       ├── ProductClient.java
│       ├── PurchaseRequest.java
│       └── PurchaseResponse.java
├── kafka/
│   ├── OrderConfirmation.java
│   └── OrderProducer.java
├── mapper/
│   ├── OrderMapper.java
│   └── OrderLineMapper.java
├── order/
│   ├── Order.java
│   ├── OrderLine.java
│   └── PaymentMethod.java
├── repositories/
│   ├── OrderRepository.java
│   └── OrderLineRepository.java
├── services/
│   ├── OrderService.java
│   └── OrderLineService.java
└── OrderApplication.java
```

## 🔄 Процесс создания заказа

### Пошаговый алгоритм:

1. **Валидация запроса** - проверка корректности данных заказа
2. **Проверка клиента** - вызов Customer Service для проверки существования клиента
3. **Резервирование товаров** - вызов Product Service для покупки товаров
4. **Создание заказа** - сохранение заказа в базе данных
5. **Создание Order Lines** - сохранение деталей заказа
6. **Обработка платежа** - вызов Payment Service
7. **Отправка уведомления** - отправка события в Kafka

### Код OrderService.createOrder():
```java
@Transactional
public Integer createOrder(OrderRequest request) {
    // 1. Проверка клиента
    CustomerResponse customer = customerClient.findCustomerById(request.customerId())
            .orElseThrow(() -> new BusinessException("Cannot create order:: No customer exists with the provided ID"));

    // 2. Резервирование товаров
    List<PurchaseResponse> purchasedProducts = productClient.purchaseProducts(request.products());

    // 3. Создание заказа
    Order order = repository.save(mapper.toOrder(request));

    // 4. Создание Order Lines
    for (PurchaseRequest purchaseRequest : request.products()) {
        orderLineService.saveOrderLine(
                new OrderLineRequest(null, order.getId(), purchaseRequest.productId(), purchaseRequest.quantity())
        );
    }

    // 5. Обработка платежа
    PaymentRequest paymentRequest = new PaymentRequest(
            request.amount(), request.paymentMethod(), order.getId(), order.getReference(), customer
    );
    paymentClient.requestOrderPayment(paymentRequest);

    // 6. Отправка уведомления
    orderProducer.sendOrderConfirmation(
            new OrderConfirmation(request.reference(), request.amount(), request.paymentMethod(), customer, purchasedProducts)
    );

    return order.getId();
}
```

## 🔗 Интеграция с внешними сервисами

### Customer Service (через Feign Client)
```java
@FeignClient(name = "customer-service", url = "${application.config.customer-url}")
public interface CustomerClient {
    @GetMapping("/{customer-id}")
    Optional<CustomerResponse> findCustomerById(@PathVariable("customer-id") String customerId);
}
```

### Product Service (через RestTemplate)
```java
@Service
public class ProductClient {
    
    @Value("${application.config.product-url}")
    private String productUrl;
    
    public List<PurchaseResponse> purchaseProducts(List<PurchaseRequest> requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(CONTENT_TYPE, APPLICATION_JSON_VALUE);
        
        HttpEntity<List<PurchaseRequest>> requestEntity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<List<PurchaseResponse>> responseEntity = restTemplate.exchange(
                productUrl + "/purchase", POST, requestEntity, 
                new ParameterizedTypeReference<>() {}
        );
        
        return responseEntity.getBody();
    }
}
```

### Payment Service (через Feign Client)
```java
@FeignClient(name = "product-service", url = "${application.config.payment-url}")
public interface PaymentClient {
    @PostMapping
    Integer requestOrderPayment(@RequestBody PaymentRequest request);
}
```

## 📨 Kafka Integration

### OrderProducer
```java
@Service
public class OrderProducer {
    
    private final KafkaTemplate<String, OrderConfirmation> kafkaTemplate;
    
    public void sendOrderConfirmation(OrderConfirmation orderConfirmation) {
        Message<OrderConfirmation> message = MessageBuilder
                .withPayload(orderConfirmation)
                .setHeader(TOPIC, "order-topic")
                .build();
        
        kafkaTemplate.send(message);
    }
}
```

### OrderConfirmation Event
```java
public record OrderConfirmation(
    String orderReference,
    BigDecimal totalAmount,
    PaymentMethod paymentMethod,
    CustomerResponse customer,
    List<PurchaseResponse> products
) {}
```

### Kafka Topic Configuration
```java
@Configuration
public class KafkaOrderTopicConfig {
    
    @Bean
    public NewTopic orderTopic() {
        return TopicBuilder
                .name("order-topic")
                .build();
    }
}
```

## 🔍 Валидация данных

### OrderRequest
- `amount` - должно быть положительным числом
- `paymentMethod` - обязательное поле
- `customerId` - обязательное поле, не может быть пустым
- `products` - обязательный список с минимум одним товаром

### PurchaseRequest
- `productId` - обязательное поле
- `quantity` - должно быть положительным числом

### Примеры ошибок валидации
```json
{
  "title": "Ошибка валидации",
  "errors": {
    "amount": ["Order amount should be positive"],
    "customerId": ["Customer should be present"],
    "products": ["You should at least purchase one product"]
  }
}
```

## 🔧 Обработка ошибок

### Типы ошибок

1. **BusinessException (404)**
   ```json
   "Cannot create order:: No customer exists with the provided ID"
   ```

2. **EntityNotFoundException (404)**
   ```json
   "No order found with the provided ID: 15"
   ```

3. **Product Purchase Exception (400)**
   ```json
   "An error occurred while processing the products purchase: 400 BAD_REQUEST"
   ```

4. **ValidationException (400)**
   ```json
   {
     "title": "Ошибка валидации",
     "errors": {
       "amount": ["Order amount should be positive"]
     }
   }
   ```

## 📊 Мониторинг

### Health Check
```http
GET /actuator/health
```

### Kafka Health Check
Spring Boot автоматически проверяет подключение к Kafka:
```json
{
  "status": "UP",
  "components": {
    "kafka": {
      "status": "UP"
    }
  }
}
```

## 🐛 Устранение неполадок

### Частые проблемы

1. **Customer Service недоступен**
    - Убедитесь, что Customer Service запущен на порту 8090
    - Проверьте конфигурацию customer-url в Config Server

2. **Product Service недоступен**
    - Убедитесь, что Product Service запущен на порту 8050
    - Проверьте конфигурацию product-url в Config Server

3. **Kafka недоступен**
    - Убедитесь, что Kafka запущен
    - Проверьте конфигурацию Kafka в application.yml

4. **PostgreSQL недоступен**
    - Убедитесь, что PostgreSQL запущен на порту 5555
    - Проверьте существование базы данных 'order'

5. **Feign Client ошибки**
    - Проверьте регистрацию сервисов в Eureka
    - Убедитесь в корректности URL в конфигурации

## 📁 Структура проекта

```
order/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── kg/manurov/ecommerce/
│   │   │       ├── configs/
│   │   │       │   ├── KafkaOrderTopicConfig.java
│   │   │       │   └── RestTemplate.java
│   │   │       ├── controller/
│   │   │       │   ├── OrderController.java
│   │   │       │   └── OrderLineController.java
│   │   │       ├── dto/
│   │   │       │   ├── order/
│   │   │       │   └── orderLine/
│   │   │       ├── errors/
│   │   │       │   └── BusinessException.java
│   │   │       ├── feign/
│   │   │       │   ├── customer/
│   │   │       │   ├── payment/
│   │   │       │   └── product/
│   │   │       ├── handler/
│   │   │       │   ├── ErrorResponseBody.java
│   │   │       │   └── GlobalExceptionHandler.java
│   │   │       ├── kafka/
│   │   │       │   ├── OrderConfirmation.java
│   │   │       │   └── OrderProducer.java
│   │   │       ├── mapper/
│   │   │       │   ├── OrderMapper.java
│   │   │       │   └── OrderLineMapper.java
│   │   │       ├── order/
│   │   │       │   ├── Order.java
│   │   │       │   ├── OrderLine.java
│   │   │       │   └── PaymentMethod.java
│   │   │       ├── repositories/
│   │   │       │   ├── OrderRepository.java
│   │   │       │   └── OrderLineRepository.java
│   │   │       ├── services/
│   │   │       │   ├── ErrorService.java
│   │   │       │   ├── OrderService.java
│   │   │       │   └── OrderLineService.java
│   │   │       └── OrderApplication.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
├── target/
├── pom.xml
└── README.md
```