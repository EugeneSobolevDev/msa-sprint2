#!/bin/bash
set -e

IMAGE_NAME="booking-service:local"
DEPLOYMENTS=("booking-service-v1" "booking-service-v2")

echo "=== Остановка и удаление всех подов booking-service в Minikube ==="
kubectl delete pod -l app=booking-service --wait || true

echo "=== Удаление старого образа ${IMAGE_NAME} из Minikube ==="
minikube ssh -- "docker rmi -f ${IMAGE_NAME} || true"

echo "=== Сборка нового образа на хосте ==="
docker build -t ${IMAGE_NAME} .

echo "=== Загрузка нового образа в Minikube ==="
minikube image load ${IMAGE_NAME}

echo "=== Перезапуск деплойментов ==="
for dep in "${DEPLOYMENTS[@]}"; do
    echo "Перезапуск $dep"
    kubectl rollout restart deployment "$dep"
done

echo "=== Скрипт завершен ==="
kubectl get pods -l app=booking-service
