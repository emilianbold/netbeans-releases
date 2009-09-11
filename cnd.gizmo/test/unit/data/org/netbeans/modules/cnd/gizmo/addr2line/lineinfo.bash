#!/bin/sh

executable=$1
if [ ! -x $executable ] ; then
    echo "Usage: $0 executable symbol+offset"
    exit 0
fi
shift

case `uname -p` in
    sparc) PC='$npc';;
    *)     PC='$pc';;
esac

dbx -q $executable 2> /dev/null <<%
>/dev/null stop in _start
>/dev/null run
>/dev/null assign $PC=$*
>/dev/null stepi
where
list +1
%
