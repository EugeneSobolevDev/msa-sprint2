#!/bin/bash
set -e

# Имя Helm release
RELEASE_NAME=booking-service
CHART_PATH=./helm/booking-service
IMAGE_NAME=booking-service
IMAGE_TAG=local
VALUES_FILE=./helm/booking-service/values-staging.yaml
DOCKER_CONTEXT=./booking-service

echo "▶️ 1. Удаляем старый Helm release, если есть"
helm uninstall $RELEASE_NAME || true

echo "▶️ 2. Удаляем старые pod'ы booking-service, если остались"
kubectl delete pod -l app=booking-service --ignore-not-found
kubectl delete pod dns-test --ignore-not-found

echo "▶️ 3. Собираем локальный Docker образ"
docker build -t $IMAGE_NAME:$IMAGE_TAG $DOCKER_CONTEXT

echo "▶️ 4. Загружаем образ в Minikube"
minikube image load $IMAGE_NAME:$IMAGE_TAG

echo "▶️ 5. Деплой через Helm"
helm upgrade --install $RELEASE_NAME $CHART_PATH \
  -f $VALUES_FILE \
  --set bookingService.image=$IMAGE_NAME \
  --set bookingService.imagePullPolicy=Never \
  --set bookingService.env.ENABLE_FEATURE_X=true

echo "▶️ 6. Проверяем pod'ы"
kubectl get pods

echo "▶️ 7. Проверяем сервисы"
kubectl get svc