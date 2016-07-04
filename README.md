## Building Project

To build the project JDK 8 should be installed.

Use the following command to cleanly rebuild the project:

    ./gradlew clean build

## Running Demo Project

Please use the following command to run the project:

    ./gradlew bootRun

Client application will be accessibe in browser

    http://localhost:8082

By default WebServer will be started on port 8082. Generator will generate 1 operation in 10 seconds.
Operations for the latest hour will be pregenerated on startup.

To change these settings you may use system gradle properties:

    ./gradlew bootRun \
              -Dserver.port=<tcp port> \
              -Dorientdb.generator.rate=<number of seconds in which 1 operation is generated> \
              -Dorientdb.generator.pregen=<should generator pregenerate data for the latest hour or should start with clean db>


## Examples:

Run application on port 8081 and generate 1 operation per second:

    ./gradlew bootRun -Dserver.port=8081 -Dorientdb.generator.rate=1

Run application on port 8080 and generate 1 operation per second and start from clean db:

    ./gradlew bootRun -Dserver.port=8080 -Dorientdb.generator.rate=1 -Dorientdb.generator.pregen=false