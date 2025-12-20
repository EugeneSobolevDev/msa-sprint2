#!/bin/bash

set -e

echo "▶️ Running in-cluster DNS test via Spring Boot Actuator..."

kubectl run dns-test --rm -it \
  --image=busybox \
  --restart=Never \
  -- wget -qO- http://booking-service/actuator/health/readiness \
  && echo "✅ Success" \
  || echo "❌ Failed"
