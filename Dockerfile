FROM azul/zulu-openjdk:8

EXPOSE 80 443 8080

COPY application.properties /application.properties
COPY target/fred.moe.jar /fred.moe.jar

ENTRYPOINT java -jar fred.moe.jar