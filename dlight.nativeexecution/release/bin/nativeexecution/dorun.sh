#!/bin/sh

if [ "$__DL_PATH" != "" ]; then
  PATH=$__DL_PATH
  export PATH
fi

PROG=`basename $0`
USAGE="usage: ${PROG} -p pidfile [-x prompt] ..."
PROMPT=NO

fail() {
  echo $@ >&2
  exit 1
}

doExit() {
  test -f ${PIDFILE} && rm ${PIDFILE}
  exit ${STATUS}
}

[ $# -lt 1 ] && fail $USAGE

while getopts p:x: opt; do
  case $opt in
    p) PIDFILE=$OPTARG
       ;;
    x) PROMPT=$OPTARG
       ;;
  esac
done

shift `expr $OPTIND - 1`


trap "doExit" 1 2 15 EXIT

STATUS=-1
/bin/sh -c "echo \$\$>${PIDFILE} && exec $@"
STATUS=$?
echo ${STATUS} > ${PIDFILE}.res


if [ "${PROMPT}" != "NO" ]; then
  /bin/echo "${PROMPT}"
  read X
fi

