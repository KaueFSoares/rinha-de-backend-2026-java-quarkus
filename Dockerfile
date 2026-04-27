FROM ghcr.io/graalvm/native-image-community:21 AS builder

WORKDIR /build
ENV LANG=C.UTF-8

COPY mvnw .
COPY .mvn .mvn

COPY pom.xml .

RUN ./mvnw -B -q -DskipTests dependency:go-offline

COPY . .

RUN ./mvnw package -Dnative -DskipTests


FROM quay.io/quarkus/ubi9-quarkus-micro-image:2.0

WORKDIR /work

RUN chown 1001 /work \
    && chmod "g+rwX" /work \
    && chown 1001:root /work

COPY --from=builder --chown=1001:root --chmod=0755 /build/target/*-runner /work/application

EXPOSE 8080
USER 1001

ENTRYPOINT ["./application", "-Dquarkus.http.host=0.0.0.0"]
