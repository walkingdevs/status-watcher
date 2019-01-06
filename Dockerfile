FROM iron/java:1.8

VOLUME /status-watcher-dir

ENTRYPOINT ["java", "-jar", "app.jar"]

RUN wget https://dl.bintray.com/walkingdevs/mvn/walkingdevs/status-watcher/0.2/status-watcher-0.2-shaded.jar -O /app.jar