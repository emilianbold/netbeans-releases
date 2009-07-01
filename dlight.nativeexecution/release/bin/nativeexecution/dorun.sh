#!/bin/sh 
PATH=/bin:/usr/bin:${PATH}
PROG=`basename '$0'`
USAGE="usage: ${PROG} -p pidfile -e envfile [-w work dir] [-x prompt] ..."
PROMPT=NO

fail() {
  echo $@ >&2
  exit 1
}

doExit() {
  test -f "${PIDFILE}" && rm "${PIDFILE}"
  test -f "${PIDFILE}.sh" && rm "${PIDFILE}.sh"
  test -f "${ENVFILE}" && rm "${ENVFILE}"
  echo ${STATUS} > "${PIDFILE}.res"
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

if [ -r "${ENVFILE}" ]; then
  echo "echo \$\$>\"${PIDFILE}\" && . \"${ENVFILE}\" && cd \"${WDIR}\" && exec $@" > "${PIDFILE}.sh"
else
  echo "echo \$\$>\"${PIDFILE}\" && cd \"${WDIR}\" && exec $@" > "${PIDFILE}.sh"
fi

/bin/sh "${PIDFILE}.sh"
STATUS=$?

echo ${STATUS} > "${PIDFILE}.res"

if [ "${PROMPT}" != "NO" ]; then
  /bin/echo "${PROMPT}"
  read X
fi

