FROM openjdk:17
COPY target/edgex-device-wrapper.jar edgex-device-wrapper.jar
ENTRYPOINT ["java","-jar","/edgex-device-wrapper.jar"]

