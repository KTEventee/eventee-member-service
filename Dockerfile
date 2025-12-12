
FROM amazoncorretto:17-alpine3.22 AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./


RUN ./gradlew dependencies --no-daemon || return 0
COPY src src
RUN ./gradlew bootJar --no-daemon



FROM amazoncorretto:17-alpine3.22-jdk
RUN addgroup -S app && adduser -S app -G app
WORKDIR /home/app
COPY --from=builder /app/build/libs/*.jar app.jar

RUN chown -R app:app /home/app
USER app


ENTRYPOINT ["java", "-jar", "app.jar"]
