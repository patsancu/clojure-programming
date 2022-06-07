FROM clojure:temurin-18-lein-focal

RUN wget https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein && \
    chmod +x lein && \
    mv lein /usr/local/bin/ && \
    lein

RUN apk add -U rlwrap
#ENTRYPOINT clojure
