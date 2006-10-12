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

#set -x

#
# It is used as automatical update of XML web pages.
#
# You must specify $CVS_ROOT, which should contain nb_all/xml,
#   nb_all/nbbuild with compiled nbantext.jar and plans folder.
# e.g.: CVS_ROOT=/tmp/cvs_netbeans_org
# If necessary, you can specify proxy host and port
#   HTTP_PROXYHOST, HTTP_PROXYPORT variables.
#
# You can run it anywhere, typically it is run by cron.
#


#
# Date stamp
#
DATE_STAMP=`date +%Y.%m.%d-%H:%M`

### DEBUG
set > $CVS_ROOT/"${DATE_STAMP}__0.set.txt"


#
# update libs
#
cd $CVS_ROOT/nb_all/libs
echo "#################" >> $CVS_ROOT/"${DATE_STAMP}__1.update.txt"
echo "# cvs update libs" >> $CVS_ROOT/"${DATE_STAMP}__1.update.txt"
cvs update -A -d -P 2>&1 >> $CVS_ROOT/"${DATE_STAMP}__1.update.txt"


#
# update xml
#
cd $CVS_ROOT/nb_all/xml
echo "################" >> $CVS_ROOT/"${DATE_STAMP}__1.update.txt"
echo "# cvs update xml" >> $CVS_ROOT/"${DATE_STAMP}__1.update.txt"
cvs update -A -d -P 2>&1 >> $CVS_ROOT/"${DATE_STAMP}__1.update.txt"


#
# add xalan and xerces on classpath
#
cd bin
. init-xalan.sh
cd ..


#
# update content
#
cd www
ant -Dhttp.proxyPort=${HTTP_PROXYPORT} -Dhttp.proxyHost=${HTTP_PROXYHOST} -logfile $CVS_ROOT/"${DATE_STAMP}__2.ant_all.txt" all 2>&1 >> $CVS_ROOT/"${DATE_STAMP}__2.ant_error.txt"


#
# commit changes
#
cvs commit -m "Automatic update -- ${DATE_STAMP}." 2>&1 > $CVS_ROOT/"${DATE_STAMP}__3.commit.txt"


#
# post update xml - to log status after commit
#
cvs update -A -d -P 2>&1 >> $CVS_ROOT/"${DATE_STAMP}__4.post-update.txt"
