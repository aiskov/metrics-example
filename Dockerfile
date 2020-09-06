FROM openjdk:14.0.1-slim

RUN apt-get update && apt-get -y install curl && rm -r /var/lib/apt/lists/*

ENV JAVA_OPTS=""
ENV RUN_OPTS=""

EXPOSE 8080

COPY ./build/libs/app.jar /app.jar

ENTRYPOINT [ "sh", "-c", "java ${JAVA_OPTS} -jar /app.jar ${RUN_OPTS}" ]
