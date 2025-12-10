Новый микросервис booking-service использует БД старого сервиса, миграция данных не требуется.
При создании бронирования оно оправляется в Kafka топик booking-created,
которое получает сервис booking-history-service.
сервис booking-history-service сохраняет бронирование в БД статистики booking-history-db.

План миграции на новый booking-service.
1. В монолите включить gRPC прокси, передав env (уже сделано в docker-compose.yml):
      BOOKING_SERVICE_EXTERNAL_HOST: booking-service
      BOOKING_SERVICE_EXTERNAL_PORT: 9090

2. Обновить архитектуру стенда, согласно нового docker-compose.yml

3. Фронту необходимо обеспечить передачу пустого userId для запросов listBookings, где необходимо получить все имеющиеся бронирования.
        было: "api/bookings"
        исправить на: "api/bookings?userId="