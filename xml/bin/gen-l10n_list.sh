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
# It is used for generating xml/*/l10n.list files.
#
# Run it from xml/.. directory.
#

# reset old list
L10N_LIST="xml/l10n.list"
echo -n > $L10N_LIST

for module in catalog compat core css tax text-edit tools tree-edit; do
    MODULE_L10N_LIST="xml/${module}/l10n.list"
    MODULE_L10N_LIST_TEMP="${MODULE_L10N_LIST}.temp"

    rm -f ${MODULE_L10N_LIST_TEMP}

    ## src
    for sub in / /compat/ /lib/ /lib/compat/; do
        MODULE_SRC=xml/${module}${sub}src
        if [ -e $MODULE_SRC ]; then
            find $MODULE_SRC -name "Bundle.properties" >> ${MODULE_L10N_LIST_TEMP}
            find $MODULE_SRC -name "*.html" | grep -v "_ja.html" | grep -v "package.html" >> ${MODULE_L10N_LIST_TEMP}
            find $MODULE_SRC -name "*.template" >> ${MODULE_L10N_LIST_TEMP}
        fi
    done
    
    ## javahelp
    MODULE_JAVAHELP=xml/${module}/javahelp
    if [ -e $MODULE_JAVAHELP ]; then
        for ext in html gif xml hs jhm; do
            find $MODULE_JAVAHELP -name "*.${ext}" | grep -v "_ja.${ext}" >> ${MODULE_L10N_LIST_TEMP}
        done
    fi

    ## sort
    cat ${MODULE_L10N_LIST_TEMP} | sort > ${MODULE_L10N_LIST}
    rm ${MODULE_L10N_LIST_TEMP}

#    find xml/${module} -name "Bundle.properties" | sort > ${MODULE_L10N_LIST}
#    find xml/${module} -name "*.html" | grep -v "_ja.html" | grep -v "package.html" | sort >> ${MODULE_L10N_LIST}

    cat ${MODULE_L10N_LIST} >> $L10N_LIST
done
