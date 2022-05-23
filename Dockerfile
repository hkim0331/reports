FROM openjdk:8-alpine

COPY target/uberjar/reports.jar /reports/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/reports/app.jar"]
