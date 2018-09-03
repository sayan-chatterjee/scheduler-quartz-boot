FROM openjdk:8-jre-alpine
MAINTAINER Sayan Chatterjee <sayan.chatterjeea@ril.com>
ADD target/hrss-scheduler-service.jar hrss-scheduler-service.jar
ENTRYPOINT ["java", "-jar", "/hrss-scheduler-service.jar", "-Xmx128m -Xms32m"]
EXPOSE 9996