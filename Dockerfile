FROM azul/zulu-openjdk:8

EXPOSE 80 443 8080

COPY application.properties /home/application.properties
COPY target/fred.moe.jar /home/fred.moe.jar
COPY public /home/public
COPY Caddyfile /home/Caddyfile
COPY README.md /home/README.md
COPY run.sh /home/run.sh

RUN apt install curl -y && apt clean && curl https://getcaddy.com | bash

CMD sh ./home/run.sh