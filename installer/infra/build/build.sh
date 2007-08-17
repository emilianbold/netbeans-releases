#!/bin/sh

# 
# The contents of this file are subject to the terms of the Common Development and
# Distribution License (the License). You may not use this file except in compliance
# with the License.
# 
# You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
# http://www.netbeans.org/cddl.txt.
# 
# When distributing Covered Code, include this CDDL Header Notice in each file and
# include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
# the following below the CDDL Header, with the fields enclosed by brackets []
# replaced by your own identifying information:
# 
#     "Portions Copyrighted [year] [name of copyright owner]"
# 
# The Original Software is NetBeans. The Initial Developer of the Original Software
# is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
# Rights Reserved.
# 

################################################################################
# build-private.sh should define the following properties
################################################################################
#BUILD_NUMBER=
#
#OUTPUT_DIR=
#
#ANT_OPTS=
#
#BINARY_CACHE_HOST=
#NB_BUILDS_HOST=
#GLASSFISH_BUILDS_HOST=
#OPENESB_BUILDS_HOST=
#SJSAM_BUILDS_HOST=
#
#NB_FILES_PREFIX=
#
#JDK_HOME=
#
#CVS_ROOT=
#
#GLASSFISH_HOME=
#GLASSFISH_ASADMIN=
#GLASSFISH_HTTP_PORT=
#GLASSFISH_ADMIN_PORT=
#GLASSFISH_HOST=
#GLASSFISH_USER=
#GLASSFISH_PASSWORD=
#
#REGISTRIES_HOME=
#
#USE_JARSIGNER=
#JARSIGNER_KEYSTORE=
#JARSIGNER_ALIAS=
#JARSIGNER_STOREPASS=
#
#REMOTE_HOST_WINDOWS=
#REMOTE_PORT_WINDOWS=
#REMOTE_USER_WINDOWS=
#
#REMOTE_HOST_LINUX=
#REMOTE_PORT_LINUX=
#REMOTE_USER_LINUX=
#
#REMOTE_HOST_SOLARIS_X86=
#REMOTE_PORT_SOLARIS_X86=
#REMOTE_USER_SOLARIS_X86=
#
#REMOTE_HOST_SOLARIS_SPARC=
#REMOTE_PORT_SOLARIS_SPARC=
#REMOTE_USER_SOLARIS_SPARC=
#
#REMOTE_HOST_MACOSX=
#REMOTE_PORT_MACOSX=
#REMOTE_USER_MACOSX=
#
#SJSAS_IMAGE_HOSTNAME_WINDOWS=
#SJSAS_IMAGE_HOSTNAME_LINUX=
#SJSAS_IMAGE_HOSTNAME_SOLARIS_X86=
#SJSAS_IMAGE_HOSTNAME_SOLARIS_SPARC=
#SJSAS_IMAGE_HOSTNAME_MACOSX=
#
#ADDITIONAL_PARAMETERS=
#
################################################################################

################################################################################
# get the path to the current directory and change to it
DIRNAME=`dirname $0`
cd ${DIRNAME}

################################################################################
# load the properties
. ./build-private.sh

################################################################################
# define the working directory location and create it
WORK_DIR=nbi_all
[ ! -d ${WORK_DIR} ] && mkdir ${WORK_DIR}

################################################################################
# define the temp file location
TEMP_FILE=${WORK_DIR}/temp.sh.tmp

################################################################################
# define the log file location and create the directory for logs
LOGS_DIR=${DIRNAME}/logs
LOG_FILE=logs/${BUILD_NUMBER}.log
[ ! -d ${LOGS_DIR} ] && mkdir -p ${LOGS_DIR}

################################################################################
# define the environment for running ant
export ANT_OPTS

################################################################################
# run the build
ant build \
        \"-Dbuild.number=${BUILD_NUMBER}\" \
        \"-Doutput.dir=${OUTPUT_DIR}\" \
        \"-Dbinary.cache.host=${BINARY_CACHE_HOST}\" \
        \"-Dnb.builds.host=${NB_BUILDS_HOST}\" \
        \"-Dnb.files.prefix=${NB_FILES_PREFIX}\" \
        \"-Dglassfish.builds.host=${GLASSFISH_BUILDS_HOST}\" \
        \"-Dopenesb.builds.host=${OPENESB_BUILDS_HOST}\" \
        \"-Dsjsam.builds.host=${SJSAM_BUILDS_HOST}\" \
        \"-Dportalpack.builds.host=${PORTALPACK_BUILDS_HOST}\" \
        \"-Djdk.home=${JDK_HOME}\" \
        \"-Dcvs.root=${CVS_ROOT}\" \
        \"-Dglassfish.home=${GLASSFISH_HOME}\" \
        \"-Dglassfish.asadmin=${GLASSFISH_ASADMIN}\" \
        \"-Dglassfish.http.port=${GLASSFISH_HTTP_PORT}\" \
        \"-Dglassfish.admin.port=${GLASSFISH_ADMIN_PORT}\" \
        \"-Dglassfish.host=${GLASSFISH_HOST}\" \
        \"-Dglassfish.user=${GLASSFISH_USER}\" \
        \"-Dglassfish.password=${GLASSFISH_PASSWORD}\" \
        \"-Dregistries.home=${REGISTRIES_HOME}\" \
	\"-Djarsigner.enabled=${USE_JARSIGNER}\" \
        \"-Djarsigner.keystore=${JARSIGNER_KEYSTORE}\" \
        \"-Djarsigner.alias=${JARSIGNER_ALIAS}\" \
        \"-Djarsigner.storepass=${JARSIGNER_STOREPASS}\" \
        \"-Dremote.host.windows=${REMOTE_HOST_WINDOWS}\" \
        \"-Dremote.port.windows=${REMOTE_PORT_WINDOWS}\" \
        \"-Dremote.user.windows=${REMOTE_USER_WINDOWS}\" \
        \"-Dremote.host.linux=${REMOTE_HOST_LINUX}\" \
        \"-Dremote.port.linux=${REMOTE_PORT_LINUX}\" \
        \"-Dremote.user.linux=${REMOTE_USER_LINUX}\" \
        \"-Dremote.host.solaris-x86=${REMOTE_HOST_SOLARIS_X86}\" \
        \"-Dremote.port.solaris-x86=${REMOTE_PORT_SOLARIS_X86}\" \
        \"-Dremote.user.solaris-x86=${REMOTE_USER_SOLARIS_X86}\" \
        \"-Dremote.host.solaris-sparc=${REMOTE_HOST_SOLARIS_SPARC}\" \
        \"-Dremote.port.solaris-sparc=${REMOTE_PORT_SOLARIS_SPARC}\" \
        \"-Dremote.user.solaris-sparc=${REMOTE_USER_SOLARIS_SPARC}\" \
        \"-Dremote.host.macosx=${REMOTE_HOST_MACOSX}\" \
        \"-Dremote.port.macosx=${REMOTE_PORT_MACOSX}\" \
        \"-Dremote.user.macosx=${REMOTE_USER_MACOSX}\" \
        \"-Dsjsas.image.token.hostname.windows=${SJSAS_IMAGE_HOSTNAME_WINDOWS}\" \
        \"-Dsjsas.image.token.hostname.linux=${SJSAS_IMAGE_HOSTNAME_LINUX}\" \
        \"-Dsjsas.image.token.hostname.solaris-x86=${SJSAS_IMAGE_HOSTNAME_SOLARIS_X86}\" \
        \"-Dsjsas.image.token.hostname.solaris-sparc=${SJSAS_IMAGE_HOSTNAME_SOLARIS_SPARC}\" \
        \"-Dsjsas.image.token.hostname.macosx=${SJSAS_IMAGE_HOSTNAME_MACOSX}\" \
	\"-Dmakedmg.remote.port=${MAKEDMG_MACOSX_SYSTEM_PORT}\" \
	\"-Dmakedmg.remote.host=${MAKEDMG_MACOSX_SYSTEM_HOST}\" \
	\"-Dmakedmg.remote.user=${MAKEDMG_MACOSX_SYSTEM_USER}\" \
	\"-Dmakedmg.ssh.keyfile=${MAKEDMG_MACOSX_SYSTEM_SSH_KEYFILE}\" \
	\"-Dmakedmg.ssh.keypass=${MAKEDMG_MACOSX_SYSTEM_SSH_PASSPHRASE}\" \
        ${ADDITIONAL_PARAMETERS} \
        $*
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - NBI installers build failed"
    exit $ERROR_CODE;
fi


################################################################################
