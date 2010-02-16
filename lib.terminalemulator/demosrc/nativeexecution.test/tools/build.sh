#!/bin/sh

PROG=`basename "$0"`
USAGE="usage: ${PROG} [-h host] [options to gmake]"

while getopts h: opt; do
  case $opt in
    h) RHOST=$OPTARG
       ;;
  esac
done

shift `expr $OPTIND - 1`

if [ "$RHOST" = "" ]; then
   PREFIX=""
else 
   PREFIX="ssh $RHOST"
fi

LHOST=`uname -n`
PWD=`pwd`

$PREFIX sh -s << EOF
uname -a
MAKE=\`which gmake || which make\`
cd /net/$LHOST/$PWD
\$MAKE $@
EOF

