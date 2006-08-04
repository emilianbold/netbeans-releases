#!/bin/sh
NAME=cmdLine.log
if [ $# = 3 ]; then
	SUFFIX=`echo $2 | awk '{print substr($1,7,3)}'`
	NAME=$NAME$SUFFIX;
fi

rm -f ./$NAME
echo $0 >./$NAME;
for PARAM in $*; do
echo $PARAM >>./$NAME;
done
