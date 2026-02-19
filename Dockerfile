
FROM gradle:8.5-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build -x test --no-daemon


FROM eclipse-temurin:17-jre-alpine
EXPOSE 8080
RUN mkdir -p /app


COPY --from=build /home/gradle/src/build/libs/*.jar /app/ecommerce-api.jar


COPY --from=build /home/gradle/src/src/main/resources/db/migration /app/resources/db/migration

# Создаем симлинк
RUN mkdir -p /app/classes
RUN ln -s /app/resources /app/classes

WORKDIR /app
ENTRYPOINT ["java", "-cp", "/app/ecommerce-api.jar:/app/classes", "com.example.ApplicationKt"]