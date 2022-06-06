FROM clojure:openjdk-19-tools-deps-alpine

RUN wget https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein && \
    chmod +x lein && \
    mv lein /usr/local/bin/ && \
    lein

RUN apk add -U rlwrap
#ENTRYPOINT clojure
