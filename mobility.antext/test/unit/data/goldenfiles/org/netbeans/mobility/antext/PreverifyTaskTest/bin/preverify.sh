#!/bin/sh
echo $*
rm -f ./cmdLine.log
echo $0 >./cmdLine.log;
for PARAM in $*; do
echo $PARAM >>./cmdLine.log;
done
