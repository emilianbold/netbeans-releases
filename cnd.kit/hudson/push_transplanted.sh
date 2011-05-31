#!/bin/sh

#
# @See transplant.sh
#

## Functions 

rollback() {
   printf "Rollback any unpushed changes ... "

   for r in `hg out | sed 's/^changeset.*:\([0-9a-f]*\)$/\1/p;d'`; do
      echo hg strip $r
      hg --config extensions.mq= strip -n -f $r || return 1
   done

   echo OK
   return 0
}

fail() {
  echo FAIL.
  STATUS=1
  exit 1
}

fail_rollback() {
  rollback
  fail
}

## BEGIN

if [ "${REVFILE}" = "" ]; then
   fail REVFILE variable is not defined
fi

STATUS=0
trap "rm -f ${REVFILE}.tmp; exit \${STATUS}" 1 2 15 EXIT

echo Pushing changes...
echo "----------------------------------------------------------"
echo hg push 
hg push || fail_rollback

LAST_REV=`cat ${REVFILE}.tmp`
NEW_REV=`expr ${LAST_REV} + 1`
echo ${NEW_REV} > ${REVFILE}
rm ${REVFILE}.tmp

