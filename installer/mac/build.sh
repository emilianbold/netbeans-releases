#!/bin/bash

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

set -x -e

if [ -z "$1" ] || [ -z "$2" ] ; then
    echo "usage: $0 zipdir basename"
    echo ""
    echo "zipdir is the dir which contains the zip distros, e.g. nbbuild/dist"
    echo "basename is the distro filename prefix, e.g. netbeans-hudson-trunk-2464"
    echo "zipdir should contain <basename>.zip, <basename>-java.zip, <basename>-ruby.zip,..."
    echo Requires GLASSFISH_LOCATION, TOMCAT_LOCATION, OPENESB_LOCATION, JBICORE_LOCATION to be set. 
    exit 1
fi

if [ -n "$3" ]; then
   INSTRUMENT_SH=$3
fi

zipdir=$1
basename=$2

progdir=`dirname $0`
cd $progdir
progdir=`pwd`

dmgname=$basename
# Remove build number from DMG name
dmgname=`echo "$dmgname" | sed "s/-[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]//g"`

instrument_build() {
   DIR=$1
   $INSTRUMENT_SH $DIR $progdir/../../emma/emma_filter $progdir/../../emma/emma.jar
#   mkdir $DIR/emma-lib
   chmod a+w $DIR/emma-lib
   #cp $progdir/../../emma/emma_filter $DIR/emma-lib/netbeans_coverage.ec
   cp $progdir/../../emma/emma.jar $DIR/emma-lib/
   sed -i -e "s/^netbeans_default_options=/netbeans_default_options=\"--cp:p $\{NETBEANS_HOME\}\/emma-lib\/emma.jar -J-Demma.coverage.file=\$\{NETBEANS_HOME\}\/emma-lib\/netbeans_coverage.ec -J-Dnetbeans.security.nocheck=true/" $DIR/etc/netbeans.conf
}


buildnum=""`find "$zipdir" -name '*[0-9].zip'`
buildnum="`expr $buildnum : '.*-\(.*\)\..*'`" 
installdir="NetBeans 6.1 RC1"

ant -f $progdir/build.xml distclean

dmg_postfix=php
license_file=pkg/license.txt
ant -f $progdir/build.xml clean
mkdir $progdir/build
unzip -d $progdir/build $zipdir/$basename-php.zip
ant -f $progdir/build.xml -Ddmgname=_$dmgname-$dmg_postfix-macosx.dmg -Dnb.dir=$progdir/build/netbeans -Dnetbeans.appname="$installdir" build-dmg -Dnetbeans_license_file="$progdir/$license_file"

