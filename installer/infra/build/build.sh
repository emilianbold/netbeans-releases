#!/bin/sh

# 
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
# 
# Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
# 
# The contents of this file are subject to the terms of either the GNU General Public
# License Version 2 only ("GPL") or the Common Development and Distribution
# License("CDDL") (collectively, the "License"). You may not use this file except in
# compliance with the License. You can obtain a copy of the License at
# http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
# License for the specific language governing permissions and limitations under the
# License.  When distributing the software, include this License Header Notice in
# each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
# designates this particular file as subject to the "Classpath" exception as provided
# by Sun in the GPL Version 2 section of the License file that accompanied this code.
# If applicable, add the following below the License Header, with the fields enclosed
# by brackets [] replaced by your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
# 
# Contributor(s):
# 
# The Original Software is NetBeans. The Initial Developer of the Original Software
# is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
# Rights Reserved.
# 
# If you wish your version of this file to be governed by only the CDDL or only the
# GPL Version 2, indicate your decision by adding "[Contributor] elects to include
# this software in this distribution under the [CDDL or GPL Version 2] license." If
# you do not indicate a single choice of license, a recipient has the option to
# distribute your version of this file under either the CDDL, the GPL Version 2 or
# to extend the choice of license to its licensees as provided above. However, if you
# add GPL Version 2 code and therefore, elected the GPL Version 2 license, then the
# option applies only if the new code is made subject to such option by the copyright
# holder.
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
#PORTALPACK_BUILDS_HOST=
#
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
source ../../../../build-private.sh

################################################################################
# define the temp file location
TEMP_FILE=${WORK_DIR}/temp.sh.tmp

################################################################################
# define the log file location and create the directory for logs
#LOGS_DIR=${DIRNAME}/logs
#LOG_FILE=logs/${BUILD_NUMBER}.log
#[ ! -d ${LOGS_DIR} ] && mkdir -p ${LOGS_DIR}

################################################################################
# define the environment for running ant
export ANT_OPTS

if [ -z "$BUILD_NETBEANS" ] ; then
    #build NetBeans bundles by default
    BUILD_NETBEANS=1
fi

if [ -z "$BUILD_NBJDK5" ] ; then
    #do not build NetBeans/JDK5 bundles by default
    BUILD_NBJDK5=0
fi

if [ -z "$BUILD_NBJDK6" ] ; then
    #do not build NetBeans/JDK6 bundles by default
    BUILD_NBJDK6=0
fi

if [ -z "$EN_BUILD" ] ; then
    EN_BUILD=1
fi

if [ -z "$ML_BUILD" ] ; then
    ML_BUILD=0
fi

if [ -z "$COMMUNITY_ML_BUILD" ] ; then
    COMMUNITY_ML_BUILD=0
fi

if [ -z "$BUILD_JTB" ] ; then
    #do not build NetBeans/JDK5 bundles by default
    BUILD_JTB=0
fi

if [ -z "$BUILD_MYSQL" ] ; then
    #do not build NetBeans/GlassFish/MySQL bundles by default
    BUILD_MYSQL=0
fi

if [ -z "$BUILD_JAVAFX" ] ; then
    #do not build NetBeans/JavaFX bundles by default
    BUILD_JAVAFX=0
fi


run() {
    ################################################################################
    # run the build
    ant build\
            \"-Dbuild.number=${BUILD_NUMBER}\" \
            \"-Doutput.dir=${OUTPUT_DIR}\" \
            \"-Dbinary.cache.host=${BINARY_CACHE_HOST}\" \
            \"-Dnb.builds.host=${NB_BUILDS_HOST}\" \
            \"-Dnb.files.prefix=${NB_FILES_PREFIX}\" \
            \"-Dnb.locales=${LOCALES}\" \
            \"-Dnb.build.type=${NB_BUILD_TYPE}\" \
            \"-Dgf.build.type=${GF_BUILD_TYPE}\" \
            \"-Dgf-mod.build.type=${GFMOD_BUILD_TYPE}\"\
            \"-Dcommunity.mlbuild=${COMMUNITY_ML_BUILD}\" \
            \"-Dglassfish.builds.host=${GLASSFISH_BUILDS_HOST}\" \
            \"-Dopenesb.builds.host=${OPENESB_BUILDS_HOST}\" \
            \"-Dsjsam.builds.host=${SJSAM_BUILDS_HOST}\" \
            \"-Dportalpack.builds.host=${PORTALPACK_BUILDS_HOST}\" \
    	    \"-Dwtk.builds.host=${WTK_BUILDS_HOST}\" \
            \"-Djavafx.builds.host=${JAVAFX_BUILDS_HOST}\" \
            \"-Djdk.home=${JDK_HOME}\" \
            \"-Dcvs.root=${CVS_ROOT}\" \
            \"-Dcvs.timestamp=${CVS_STAMP}\" \
            \"-Dcvs.branch=${CVS_BRANCH}\" \
            \"-Dbuild.jdk5=${BUILD_NBJDK5}\" \
            \"-Dbuild.jdk6=${BUILD_NBJDK6}\" \
            \"-Dbuild.jtb=${BUILD_JTB}\" \
            \"-Dbuild.mysql=${BUILD_MYSQL}\" \
            \"-Dbuild.netbeans.bundles=${BUILD_NETBEANS}\" \
            \"-Dbuild.javafx=${BUILD_JAVAFX}\" \
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
            \"-Dpack200.enabled=${USE_PACK200}\" \
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
            \"-Dnbi.cache.dir=${CACHE_DIR}\" \
            ${ADDITIONAL_PARAMETERS} \
            $*
            ERROR_CODE=$?

            if [ $ERROR_CODE != 0 ]; then
                 echo "ERROR: $ERROR_CODE - NBI installers build failed"
                 exit $ERROR_CODE;
            fi
}

setNetBeansBuildsHost() {
    if [ -n "$1" ] && [ 1 == $1 ] && [ -n "${NB_BUILDS_HOST_ML}" ] ; then
        NB_BUILDS_HOST=${NB_BUILDS_HOST_ML}
    else 
        NB_BUILDS_HOST=${NB_BUILDS_HOST_EN}
    fi
}

if [ 1 == "$EN_BUILD" ] ; then
        setNetBeansBuildsHost
        run $*
fi

if [ 1 == "$ML_BUILD" ] ; then
	setNetBeansBuildsHost $ML_BUILD
	NB_BUILD_TYPE=ml
        GF_BUILD_TYPE=ml
        GFMOD_BUILD_TYPE=ml
	OUTPUT_DIR=${OUTPUT_DIR}/${NB_BUILD_TYPE}
	run $*
fi


################################################################################
