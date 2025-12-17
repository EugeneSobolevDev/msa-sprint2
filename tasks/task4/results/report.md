# Task 4: Автоматизация развёртывания и тестирования

## 1. Docker образ

- Собран локальный Docker образ `booking-service:local`

## 2. Helm chart

- Helm chart размещён в `helm/booking-service`
- Deployment с контейнером `booking-service`, liveness и readiness probes на `/ping`
- Service ClusterIP с портом 80 → targetPort 8080
- Для локальной разработки InitContainer отключён
- Два values-файла:
    - `values-staging.yaml` для локальной Minikube разработки
    - `values-prod.yaml` для продакшена с InitContainer и gRPC health probe

## 3. CI/CD (.gitlab-ci.yml)

- Стадии: `build`, `test`, `deploy`, `tag`
- `build` → docker build
- `test` → docker run + проверка `/ping`
- `deploy` → minikube image load + helm upgrade
- `tag` → создание git-тега с timestamp

## 4. Service Discovery через DNS

- Проверка через actuator `http://booking-service:8080/actuator/health/readiness`

## 5. Фича-флаг

- `ENABLE_FEATURE_X` при добавлении в переменные окружения log.info("XFeatureService is initialized");

## 6. Создан скрипт для деплоя сервиса 
- deploy-local.sh

