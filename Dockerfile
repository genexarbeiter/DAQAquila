# Base Alpine Linux based image with OpenJDK JRE only
FROM openjdk:8-jre-alpine

# copy distribution ouput folder; includes mainly application jar file and libraries
COPY target/distribution/ /modbus-daq

#TEST YAML
#COPY target/classes/de/demo_config.yaml /modbus-daq

# copy logback files
#COPY src/main/resources/zip/logback*.xml /modbus-daq/

# start DAQ from jar-file with externally mounted (!) configuration file (needs a mounted volume on "/daq-config.yaml")
CMD ["sh", "/modbus-daq/tar/bin/daqprocess.sh start P_HOST99"]