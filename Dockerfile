FROM openjdk:17-jdk-slim
WORKDIR /app
# 指明从 build 文件夹里拿包，并重命名为 app.jar
COPY build/libs/ledger-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]