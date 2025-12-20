## 🛠️ Подготовка окружения

Сборка суперграфа
```bash
cd .\gateway\
docker-compose down
docker-compose up -d booking-subgraph hotel-subgraph promocode-subgraph
rover supergraph compose --config .\supergraph-local.yaml --elv2-license accept > supergraph.graphql
Заменяем в supergraph.graphql localhost на имена серверов
enum join__Graph {
  BOOKING_SUBGRAPH @join__graph(name: "booking-subgraph", url: "http://booking-subgraph:4001")
  HOTEL_SUBGRAPH @join__graph(name: "hotel-subgraph", url: "http://hotel-subgraph:4002")
  PROMOCODE_SUBGRAPH @join__graph(name: "promocode-subgraph", url: "http://promocode-subgraph:4003")
}
Get-Content supergraph.graphql -Encoding Unicode | Out-File -Encoding UTF8 -FilePath supergraph.graphql
Переименовываем supergraph.graphql в supergraph.graphql
cd..
docker compose up -d --build

```
---

Ознакомьтесь с предложенной структурой.
В дальнейшем поднять сервис можно будет с помощью:
```bash
docker compose up -d --build
```
---

## 🚀 Проверка корректности

Выполните следующий GraphQL-запрос через GraphQL Playground на https://studio.apollographql.com/sandbox/explorer:
```
graphql
	query {
		bookingsByUser(userId: "user1") {
			id
			hotel {
				name
				city
			}
			discountPercent
		}
	}
	
query {
  bookingsByUser(userId: "test-user-2") {
    id
    hotelId
    userId
    promoCode
    hotel {
      id
      name
      city
      stars
    }
    discountPercent
    discountInfo {
      originalDiscount
      finalDiscount
      description
    }
  }
}
```
Можно запрашивать больше данных.
Перед этим добавьте заголовок userid: user1, иначе данные не вернутся из-за ACL.

---

📌 Подсказки
- Все заголовки передаются из Gateway в подграфы автоматически.
- Для реализации ACL проверяйте req.headers['userid'] в резолверах.
- Если пользователь не авторизован, не возвращайте бронирование.
- При использовании реальных модулей не забудьте использовать одну и ту же сеть в docker!
