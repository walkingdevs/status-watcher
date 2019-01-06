FROM iron/java:1.8

VOLUME /status-watcher-dir

ENTRYPOINT ["java", "-jar", "app.jar"]

ADD target/status-watcher.jar /app.jar