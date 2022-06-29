image = clojure:temurin-18-lein-focal


WORKDIR=$(shell pwd)
DST_DIR="/home/code/programming-clojure-book"

run:
	docker run --name clojure-repl -it -v $(WORKDIR):$(DST_DIR) -v this-m2:/root/.m2 --rm $(image) bash -c "cd $(DST_DIR); lein repl"
rebuild:
	docker build -t myclojure .

down:
	docker-compose down

up:
	docker-compose up

up-detached:
	docker-compose up -d

restart-detached: down up-detached

restart: down up
