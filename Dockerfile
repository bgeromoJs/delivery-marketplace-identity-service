# syntax=docker/dockerfile:1

# ---- Build ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Camada de dependências separada: muda pouco, então o cache do Docker sobrevive
# a alterações de código.
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q package -DskipTests

# ---- Runtime ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app

COPY --from=build /workspace/target/*.jar app.jar

USER app
EXPOSE 8080

# Heap = 40% do limite de memória do container (mem_limit no docker-compose.yml).
# Os outros 60% ficam para metaspace, code cache, threads e buffers nativos.
# ExitOnOutOfMemoryError faz o processo sair em vez de ficar travado, e o
# restart: unless-stopped do Compose sobe o serviço de novo.
ENV JAVA_MAX_RAM_PERCENTAGE=40
ENTRYPOINT ["sh", "-c", "exec java -XX:MaxRAMPercentage=${JAVA_MAX_RAM_PERCENTAGE} -XX:+ExitOnOutOfMemoryError -jar /app/app.jar \"$@\"", "--"]
