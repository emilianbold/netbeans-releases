#/bin/bash
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
#
# Oracle and Java are registered trademarks of Oracle and/or its affiliates.
# Other names may be trademarks of their respective owners.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common
# Development and Distribution License("CDDL") (collectively, the
# "License"). You may not use this file except in compliance with the
# License. You can obtain a copy of the License at
# http://www.netbeans.org/cddl-gplv2.html
# or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
# specific language governing permissions and limitations under the
# License.  When distributing the software, include this License Header
# Notice in each file and include the License file at
# nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
# particular file as subject to the "Classpath" exception as provided
# by Oracle in the GPL Version 2 section of the License file that
# accompanied this code. If applicable, add the following below the
# License Header, with the fields enclosed by brackets [] replaced by
# your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
#
# If you wish your version of this file to be governed by only the CDDL
# or only the GPL Version 2, indicate your decision by adding
# "[Contributor] elects to include this software in this distribution
# under the [CDDL or GPL Version 2] license." If you do not indicate a
# single choice of license, a recipient has the option to distribute
# your version of this file under either the CDDL, the GPL Version 2 or
# to extend the choice of license to its licensees as provided above.
# However, if you add GPL Version 2 code and therefore, elected the GPL
# Version 2 license, then the option applies only if the new code is
# made subject to such option by the copyright holder.
#
# Contributor(s):

# To build in debug mode export DEBUG=Y variable

# To get this script work, set variables:
#  DLIGHT   - if in DEBUG mode           - path to temporary dlight directory (where pty is stored)
#  DISCOVER - if building on Solaris     - path to discover binary tool
#  CC       - if building with DevStudio - path to CC binary

if [ "$1" = "--solaris" ]; then
	SOLARIS=1
fi

if [ -n "$DEBUG" ]; then
    DLIGHT="/path/to/dlight/tmp"
    export NOSTRIP=Y
fi

MAKE=`which gmake || which make`

if [ "x$SOLARIS" != "x" ]; then
	DISCOVER=/path/to/discover
	args='CC=/path/to/cc NOSTRIP=1 CFLAGS_EXTRA=-O3 64BITS=64 CF_COMMON=""'
else
	CC=gcc
	args='64BITS=64'
fi

sh build.sh clean

cd pty
rm -rf dist
cd ..

cd killall
rm -rf dist
cd ..

cd unbuffer
rm -rf dist
cd ..


sh build.sh $args

cd pty
${MAKE} $args
cd ..

cd killall
${MAKE} $args
cd ..

cd unbuffer
${MAKE} $args
cd ..

BUILD_ALL=buildall

mkdir -p $BUILD_ALL
cd $BUILD_ALL
rm -f *
cd ..

find "../release/bin/nativeexecution/" "unbuffer/dist/" "pty/dist" "killall/dist" -not -name "*.sh" -type f -exec cp {} $BUILD_ALL \;

if [ "x$SOLARIS" != "x" ]; then
	find "$BUILD_ALL" -type f -exec sh -c "$DISCOVER"' -v -w $0.%p.txt $0' {} \;
fi

if [ -n "$DEBUG" ]; then
#    sed -i '/copyFile(localFile, safeLocalFile);/c\ /* copyFile(localFile, safeLocalFile); */' ../src/org/netbeans/modules/nativeexecution/api/util/HelperUtility.java    
    PTY=`find "pty/dist" -name pty`
    find "${DLIGHT}" -name pty -exec cp $PTY {} \;
    find "${DLIGHT}" -name pty -exec file {} \;
fi

find $BUILD_ALL -type f | xargs file

