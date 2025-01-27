# Base Alpine Linux based image with OpenJDK JRE only
#FROM openjdk:8-jre-alpine

# copy distribution ouput folder; includes mainly application jar file and libraries
#COPY target/DaqConfigLoader-0.0.1.jar /modbus-daq/
#COPY target/distribution/DaqConfigLoader-0.0.1/lib /modbus-daq/lib

# start DAQ from jar-file with externally mounted (!) configuration file (needs a mounted volume on "/daq-config.yaml")
#RUN apk update && apk add bash
#RUN apk --update add  procps  &&\
#    rm -rf /var/cache/apk/*
#RUN chmod +x modbus-daq/distribution/tar/bin/daqprocess.sh
#CMD ["modbus-daq/distribution/tar/bin/daqprocess.sh", "start", "P_HOST99"]
#CMD["/usr/bin/java", "-jar", "/modbus-daq/DaqConfigLoader-0.0.1.jar"]

FROM maven:3.6.3-openjdk-14-slim AS build
RUN mkdir -p /workspace
WORKDIR /workspace
COPY pom.xml /workspace
RUN mvn dependency:go-offline
COPY src /workspace/src
RUN mvn package

FROM openjdk:14-slim
COPY --from=build /workspace/target/*DAQAquila-1.0.0.jar app.jar
COPY --from=build /workspace/target/lib lib
ENTRYPOINT ["java","-jar","app.jar"]