FROM ghcr.io/apollographql/router:v2.10.0

COPY supergraph.graphql /app/supergraph.graphql
COPY router.yaml /app/router.yaml
EXPOSE 4000
CMD ["--supergraph", "/app/supergraph.graphql", "--log", "debug", "--config", "/app/router.yaml"]


