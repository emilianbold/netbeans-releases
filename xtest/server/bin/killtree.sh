#!/bin/sh

#
# Script for killing process and all its subprocesses
#

if [ -z "$*" ] ; then
  echo "Incorrect parameters."
  echo "Usage: $0 pid1 [pid2]..."
  exit 1
fi

PID=$*
PIDLIST=
echo " " > /tmp/space.char

while
  if [ ! -z "${PID}" ] ; then
    CHILDS=
    for P in $PID ;
    do
      CHILD=`ps -ef | tail +2 | paste -d" " /tmp/space.char - | tr -s " " | cut -d " " -f 3,4 | grep ${P}$ | cut -d " " -f 1` 
      CHILDS="${CHILDS} ${CHILD}"
      kill -9 $P
    done
    PIDLIST="${PIDLIST} ${CHILDS}"
    PID=${CHILDS}
  else
    rm -f /tmp/space.char
    exit
  fi
do
  true;
done
