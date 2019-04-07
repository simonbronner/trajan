FROM amazoncorretto:8
MAINTAINER "Simon Bronner" <sbronner@temenos.com>

RUN useradd --create-home -s /bin/bash trajan

ENV HOME /home/trajan

ADD --chown=trajan:trajan target/uberjar/trajan-0.1.0-SNAPSHOT-standalone.jar /home/trajan/trajan.jar

EXPOSE 8001

ENTRYPOINT ["java","-jar","/home/trajan/trajan.jar","--port","8001"]
CMD ["--server","--database","datomic:free://localhost:4334/trajan"]
