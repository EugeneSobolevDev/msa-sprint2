FROM debian:bullseye-slim

WORKDIR /app

# Install dependencies
RUN apt-get update && apt-get install -y \
    curl \
    postgresql-client \
    bash \
    dos2unix \
    && rm -rf /var/lib/apt/lists/*

COPY wait-for-it.sh regress.sh init-fixtures.sql ./
RUN dos2unix wait-for-it.sh regress.sh && \
    chmod +x wait-for-it.sh regress.sh

ENTRYPOINT ["./regress.sh"]