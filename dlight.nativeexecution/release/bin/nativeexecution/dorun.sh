#!/bin/sh 
PATH=/bin:/usr/bin:${PATH}
PROG=`basename "$0"`
USAGE="usage: ${PROG} -p pidfile -e envfile [-s] [-w work dir] [-x prompt] ..."
PROMPT=NO
SUSPEND=

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

while getopts w:e:p:x:s opt; do
  case $opt in
    p) PIDFILE=$OPTARG
       ;;
    x) PROMPT=$OPTARG
       ;;
    e) ENVFILE=$OPTARG
       ;;
    w) WDIR=$OPTARG
       ;;
    s) SUSPEND=1
       ;;
  esac
done

shift `expr $OPTIND - 1`

trap "doExit" 1 2 15 EXIT

if [ "${WDIR}" = "" ]; then
  WDIR=.
fi

STATUS=-1

echo "echo \$\$>\"${PIDFILE}\" || exit \$?" > "${PIDFILE}.sh"
if [ -r "${ENVFILE}" ]; then
  echo ". \"${ENVFILE}\" || exit \$?" >> "${PIDFILE}.sh"
fi
echo "cd \"${WDIR}\" || exit \$?" >> "${PIDFILE}.sh"
if [ -n "$SUSPEND" ]; then
  echo "ITS_TIME_TO_START=" >> "${PIDFILE}.sh"
  echo "trap 'ITS_TIME_TO_START=1' CONT" >> "${PIDFILE}.sh"
  echo "while [ -z \"\$ITS_TIME_TO_START\" ]; do sleep 1; done" >> "${PIDFILE}.sh"
fi
echo "exec $@" >> "${PIDFILE}.sh"

/bin/sh "${PIDFILE}.sh"
STATUS=$?

echo ${STATUS} > "${PIDFILE}.res"

if [ "${PROMPT}" != "NO" ]; then
  /bin/echo "${PROMPT}"
  read X
fi

