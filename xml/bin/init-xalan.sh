#!/bin/sh

# 
#                 Sun Public License Notice
# 
# The contents of this file are subject to the Sun Public License
# Version 1.0 (the "License"). You may not use this file except in
# compliance with the License. A copy of the License is available at
# http://www.sun.com/
# 
# The Original Code is NetBeans. The Initial Developer of the Original
# Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
# Microsystems, Inc. All Rights Reserved.
# 

#set -x

#
# Extends classpath with JAXP, Xerces and Xalan from NetBeans nbextra.
#
# Run it from this (xml/bin) directory: '. init-xalan.sh'.
#

XMLBINDIR=`pwd -P`
XML_ROOT=$XMLBINDIR/..
NBROOT=${XML_ROOT}/..
BINROOT=${NBROOT}/../nbextra
LIBS_ROOT=${NBROOT}/libs

XML_APIS=${BINROOT}/core/release/lib/ext/xml-apis.jar
XERCES=${BINROOT}/core/release/lib/ext/xerces.jar

grep libs.xalan.jar.path public.properties | awk -F= '{ print $2 }'
XALAN_JAR_PATH=`grep "libs.xalan.jar.path" ${LIBS_ROOT}/public.properties | awk -F= '{ print $2 }'`
XALAN=${BINROOT}/${XALAN_JAR_PATH}

##<debug>
#ls -la ${XML_APIS}
#ls -la ${XERCES}
#ls -la ${XALAN}
##</debug>

export CLASSPATH=${CLASSPATH}:${XML_APIS}:${XERCES}:${XALAN}
