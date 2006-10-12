#!/bin/sh

# 
# The contents of this file are subject to the terms of the Common Development
# and Distribution License (the License). You may not use this file except in
# compliance with the License.
#
# You can obtain a copy of the License at http://www.netbeans.org/cddl.html
# or http://www.netbeans.org/cddl.txt.
#
# When distributing Covered Code, include this CDDL Header Notice in each file
# and include the License file at http://www.netbeans.org/cddl.txt.
# If applicable, add the following below the CDDL Header, with the fields
# enclosed by brackets [] replaced by your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
#
# The Original Software is NetBeans. The Initial Developer of the Original
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
