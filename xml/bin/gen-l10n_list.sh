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
echo -n > xml/l10n.list

for module in catalog compat core css tax text-edit tools tree-edit; do
    find xml/${module} -name "Bundle.properties" | sort > xml/${module}/l10n.list
    find xml/${module} -name "*.html" | grep -v "_ja.html" | grep -v "package.html" | sort >> xml/${module}/l10n.list

    cat xml/${module}/l10n.list >> xml/l10n.list
done
