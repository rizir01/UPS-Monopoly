CC=gcc

all:	clean server

server: server.c
	${CC} -o server server.c -lpthread

clean: 
	rm -f server

