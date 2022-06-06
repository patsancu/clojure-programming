run:
	docker run -it --rm myclojure bash
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
