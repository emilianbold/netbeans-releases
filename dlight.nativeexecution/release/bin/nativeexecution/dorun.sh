#!/bin/sh

PROG=`basename $0`
USAGE="usage: ${PROG} -p pidfile [-x prompt] ..."
PROMPT=NO

fail() {
  echo $@ >&2
  exit 1
}

doExit() {
  test -f ${PIDFILE} && rm ${PIDFILE}
  test -f ${ENVFILE} && rm ${ENVFILE}
  echo ${STATUS} > ${PIDFILE}.res
  exit ${STATUS}
}

[ $# -lt 1 ] && fail $USAGE

while getopts w:e:p:x: opt; do
  case $opt in
    p) PIDFILE=$OPTARG
       ;;
    x) PROMPT=$OPTARG
       ;;
    e) ENVFILE=$OPTARG
       ;;
    w) WDIR=$OPTARG
       ;;
  esac
done

shift `expr $OPTIND - 1`

trap "doExit" 1 2 15 EXIT

if [ "${WDIR}" = "" ]; then
  WDIR=.
fi

STATUS=-1

test -r ${ENVFILE} && ENVSETUP=" && . ${ENVFILE}"

/bin/sh -c "echo \$\$>${PIDFILE} ${ENVSETUP} && cd ${WDIR} && exec $@"
STATUS=$?
echo ${STATUS} > ${PIDFILE}.res

if [ "${PROMPT}" != "NO" ]; then
  /bin/echo "${PROMPT}"
  read X
fi

