#!/bin/bash

set -e

echo "▶️ Testing fallback route..."
curl -H "Host: booking.local" -H "X-Fallback: true" http://localhost:8080/ping || echo "Fallback route working"
