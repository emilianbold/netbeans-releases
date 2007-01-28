#!/bin/sh

## resolve links - $0 may be a link
 
cd `dirname $0`/../..
RAVE_BASE=`pwd`

RAVE_ROOT=`dirname "$0"`/../../..

#
# resolve symlinks
#

while [ -h "$PRG" ]
do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '^.*-> \(.*\)$' 2>/dev/null`
    if expr "$link" : '^/' 2> /dev/null >/dev/null
    then
	PRG="$link"
    else
	PRG="`dirname "$PRG"`/$link"
    fi
done

EJBS_DIR=${RAVE_BASE}/samples/ejb/applications
# make it fully qualified
EJBS_DIR=`(cd "${EJBS_DIR}" && pwd)`


if [ -z "$RAVE_J2EE_HOME" ] ; then
    PE_DIR=${RAVE_ROOT}/SunAppServer8
else
    PE_DIR=$RAVE_J2EE_HOME
fi


# make it fully qualified
PE_HOME=`(cd "${PE_DIR}" && pwd)`

RAVE_DOMAIN=creator
USER=admin
PASSWORD=adminadmin
HOST=localhost

if [ $# -ne 2 ]; then
    ADMIN_PORT=24848
    DB_PORT=29092
else
    ADMIN_PORT=$1
    DB_PORT=$2
fi

#
# Create JDBC connection pool and JDBC resource to Travel schema
#

CONNECTION_POOL_ID=TravelDBPool
JDBC_RESOURCE_ID=jdbc/Travel
TRAVEL_DB_NAME="jdbc\:pointbase\:server\:\/\/localhost\:${DB_PORT}\/sample"

${PE_HOME}/bin/asadmin create-jdbc-connection-pool --user ${USER} --password ${PASSWORD} --port ${ADMIN_PORT} --datasourceclassname com.pointbase.xa.xaDataSource --steadypoolsize 1 --maxpoolsize 8 --poolresize 1 --restype javax.sql.XADataSource --property User=travel:Password=travel:DatabaseName=${TRAVEL_DB_NAME} ${CONNECTION_POOL_ID}
${PE_HOME}/bin/asadmin create-jdbc-resource --user admin --password adminadmin --port ${ADMIN_PORT} --connectionpoolid ${CONNECTION_POOL_ID} --description "Travel jdbc resource" ${JDBC_RESOURCE_ID}

#
# Create JDBC connection pool and JDBC resource to Jump Start Cycles (JSC) schema
#

JSC_CONNECTION_POOL_ID=JSCDBPool
JSC_JDBC_RESOURCE_ID=jdbc/JSC
JSC_DB_NAME="jdbc\:pointbase\:server\:\/\/localhost\:${DB_PORT}\/sample"

${PE_HOME}/bin/asadmin create-jdbc-connection-pool --user ${USER} --password ${PASSWORD} --port ${ADMIN_PORT} --datasourceclassname com.pointbase.xa.xaDataSource --steadypoolsize 1 --maxpoolsize 8 --poolresize 1 --restype javax.sql.XADataSource --property User=jsc:Password=jsc:DatabaseName=${JSC_DB_NAME} ${JSC_CONNECTION_POOL_ID}
${PE_HOME}/bin/asadmin create-jdbc-resource --user admin --password adminadmin --port ${ADMIN_PORT} --connectionpoolid ${JSC_CONNECTION_POOL_ID} --description "Jump Start Cycles jdbc resource" ${JSC_JDBC_RESOURCE_ID}

#
# Deploy ejb applications to PE
#

EJB_FILES=`find ${EJBS_DIR} -type f -name "*.ear" -print`

for FILE in ${EJB_FILES} ; do
     ${PE_HOME}/bin/asadmin deploy --user ${USER} --password ${PASSWORD} --port ${ADMIN_PORT} ${FILE}
done

