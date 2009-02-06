#!/bin/sh
PROG=`basename $0`
USAGE="usage: ${PROG} -p pidfile [-x prompt] ..."
PROMPT=NO

fail() {
  echo $@ >&2
  exit 1
}

rmPidFile() {
  test -f ${PIDFILE} && rm ${PIDFILE}
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

trap "rmPidFile; exit" 1 2 15 EXIT

cat << EOF | /bin/sh
echo \$\$>${PIDFILE} && exec $@
EOF

rmPidFile
if [ "${PROMPT}" != "NO" ]; then
  /bin/echo "${PROMPT}"
  read X
fi

