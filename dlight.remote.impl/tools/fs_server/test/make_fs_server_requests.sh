#!/bin/bash
if [ $# -lt 2 ]; then
  progname=`basename $0`
  echo "Usage:"
  echo "    ${progname} <directory> <output>"
  exit 1
fi

R=$1
F=$2

if [ ! -d $R ]; then
  echo $R is not a directory
  exit 2
fi

R=`(cd $R; pwd)`

echo "" > $F
i=0
for D in `find $R -type d`; do i=`expr $i + 1`; echo "l $i ${#D} $D" >> $F; done
