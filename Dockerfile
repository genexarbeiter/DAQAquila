# Base Alpine Linux based image with OpenJDK JRE only
FROM openjdk:8-jre-alpine

# copy distribution ouput folder; includes mainly application jar file and libraries
COPY target/ /modbus-daq/

# start DAQ from jar-file with externally mounted (!) configuration file (needs a mounted volume on "/daq-config.yaml")
RUN apk update && apk add bash
RUN apk --update add  procps  &&\
    rm -rf /var/cache/apk/*
RUN chmod +x modbus-daq/distribution/tar/bin/daqprocess.sh
CMD ["modbus-daq/distribution/tar/bin/daqprocess.sh", "start", "P_HOST99"]