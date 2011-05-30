CGLAGS="-g"
LDGLAGS="-g"
CC=gcc

BINARY="simple-bin"
OBJFILES=simple.o tolower.o toupper.o
DIR=SSS/XXX
FILE=SSS/xxx

all: ${BINARY}
	${CC} ${LDGLAGS} ${OBJFILES} -o ${BINARY}
	mkdir -p ${DIR}
	mkdir -p ${DIR}/subdir
	echo xxx > ${FILE}
	echo xxx > ${DIR}/subdir/f1
	echo xxx > ${DIR}/subdir/f2
	echo xxx > ${DIR}/subdir/f3

clean:
	rm -rf ${BINARY}
	rm -rf *.o
	rm -rf ${DIR}
	rm -rf ${FILE}

${BINARY}: ${OBJFILES}
	
%.o : %.c
	${CC} -c ${CFLAGS} -o $@ $<

