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
# It is used for generating xml/*/jar.list files.
#
# Run it from xml/.. directory.
# It is necessary to call "ant netbeans" before -- list files
# are generated from content of netbeans directories.
#

# reset old list
JAR_LIST="xml/jar.list"
JAR_LIST_TEMP="${JAR_LIST}.temp"
echo -n > $JAR_LIST_TEMP

for module in api catalog core css tax text-edit tools tree-edit xsl schema; do
    MODULE_HOME="xml/${module}"
    MODULE_JAR_LIST="jar.list"
    MODULE_JAR_LIST_TEMP="${MODULE_JAR_LIST}.temp"

    cd xml/${module}
    rm -f ${MODULE_JAR_LIST_TEMP}

    ## netbeans
    find netbeans -type f -name "*.jar" | grep -v "_ja\." >> ${MODULE_JAR_LIST_TEMP}

    ## sort
    cat ${MODULE_JAR_LIST_TEMP} | sort > ${MODULE_JAR_LIST}
    rm ${MODULE_JAR_LIST_TEMP}
    cd ../..

    cat ${MODULE_HOME}/${MODULE_JAR_LIST} >> $JAR_LIST_TEMP
done

cat ${JAR_LIST_TEMP} | sort > ${JAR_LIST}
rm ${JAR_LIST_TEMP}
