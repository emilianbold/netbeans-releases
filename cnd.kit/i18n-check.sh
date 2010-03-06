#!/bin/sh -x
WORKSPACE=$1
LOG=$2
MAILTO=$3

WORKSPACE=${WORKSPACE:-..};
LOG=${LOG:-${WORKSPACE}/i18n-check.log};


# check workspace  existence and access
if [ ! -d ${WORKSPACE} ]; then
	echo "Error: ${WORKSPACE} is not a directory"
	exit 4
fi
if [ ! -r ${WORKSPACE} ]; then
	echo "Error: can not read ${WORKSPACE}"
	exit 8
fi

#WORKSPACE=`(cd ${WORKSPACE}; pwd)`

#echo WORKSPACE=$WORKSPACE
#echo LOG=$LOG

cd ${WORKSPACE}

perl nbbuild/misc/i18ncheck.pl cnd* asm* dlight* lib.terminalemulator/src | grep -v "/test/" | grep -v "cnd.antlr/" | grep -v "generated/"  | tee ${LOG}
#perl nbbuild/misc/i18ncheck.pl cnd* asm* dlight* lib.terminalemulator/src terminal | grep -v "/test/" | grep -v "cnd.antlr/" | grep -v "generated/"  | tee ${LOG}
cnt=`cat ${LOG} | wc -l`
if [ ${cnt} -gt 0 ]; then
	echo "I18n check FAILED"
	if [ -n "${MAILTO}" ]; then
		mailx -s "I18n check FAILED" cnd-incremental-builds@sun.com < ${LOG}
	fi
else
	echo "I18n check SUCCEEDED - no warnings"
fi
