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
# Extends classpath with JAXP, Xerces and Xalan.
#
# Run it from this (xml/bin) directory: '. init-xalan.sh'.
#

XMLBINDIR=`pwd -P`
XML_ROOT=$XMLBINDIR/..
NBROOT=${XML_ROOT}/..

XML_APIS=${NBROOT}/core/external/xml-apis*.jar
XERCES=${NBROOT}/core/external/xerces*.jar
XALAN=${NBROOT}/libs/external/xalan-2.3.1.jar

##<debug>
#ls -la ${XML_APIS}
#ls -la ${XERCES}
#ls -la ${XALAN}
##</debug>

export CLASSPATH=${CLASSPATH}:${XML_APIS}:${XERCES}:${XALAN}
