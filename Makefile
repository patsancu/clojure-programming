image = clojure:temurin-18-lein-focal


WORKDIR_LINUX=$(shell pwd)
# This workdir is relative to the VM that is actually running docker
WORKDIR="/home/patrick/maketv-code/learning/clojure-programming/"
DST_DIR="/home/code/programming-clojure-book"

run:
	docker run --name clojure-repl -it -v $(WORKDIR):$(DST_DIR) -v this-m2:/root/.m2 --rm $(image) bash -c "cd $(DST_DIR); lein repl"
another:
	docker exec -ti clojure-repl bash -c "cd $(DST_DIR); lein repl"
run-linux:
	docker run --name clojure-repl -it -v $(WORKDIR_LINUX):$(DST_DIR) -v this-m2:/root/.m2 --rm $(image) bash -c "cd $(DST_DIR); lein repl"
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
