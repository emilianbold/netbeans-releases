#!/bin/sh

#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
# Contributor(s):
#
# The Original Software is NetBeans. The Initial Developer of the Original
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
# Microsystems, Inc. All Rights Reserved.
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

PWD=`pwd`
cd `dirname $0`
BINDIR=`pwd`
cd $PWD
MYNAME=`basename $0`

AWK=nawk
PERL=perl
TMPFILE="/tmp/$MYNAME.$$"

trap "rm -rf $TMPFILE; exit" 1 2 15

BASEDIR=`dirname $BINDIR`
unset DATADIR

usage() {
cat << EOF


Usage: $MYNAME [-d initdata-dir] -p project [-l]
       $MYNAME -h

Options:

       -d :     Directory where information from substituted compillers collected. 
                Default: $BASEDIR/initdata
   
       -p :     Name of project (name of directory with actual data)
  
       -l :     Out filenames instead of files count
 
       -h :     Display this message and exit


EOF

   exit $1
}


COUNTCMD="wc -l"

while getopts hlp:d: choice
do
   case $choice in
      d) DATADIR=$OPTARG;;
      p) PROJECT=$OPTARG;;
      l) COUNTCMD="cat";;
      h) usage 0;;
      *) usage 1;;
   esac
done

if [ "_$DATADIR" = "_" ]; then
   DATADIR=$BASEDIR/initdata
fi

if [ ! -d $DATADIR ]; then
   echo
   echo "Wrong initdata-dir specified!"
   usage 1
fi

if [ "_$PROJECT" = "_" ]; then
   echo
   echo "ERROR: No project specified."
   echo 
   echo "Available projects in $DATADIR are:"
   echo
   ls $DATADIR/*/_project | ${AWK} -F/ "{ print $(NF-1) }" 2>/dev/null
   usage 1
fi

FLIST=`find ${DATADIR}/${PROJECT} -type f -name $PROJECT.* -prune ! \( -name *.inc -o -name *.mac -o -name *.fno \)`

cat > $TMPFILE << EOF 
for (@F) {
   if ("\$_" =~ /\.C$|\.cpp$|\.cc$|\.c$/) {
      \$fname = \$_; chomp(\$fname);
      print @F[1],"/" if ( \$fname !~ /^\//);
      print \$fname,"\n"
   }
}
EOF

if [ ! -z "$FLIST" ]; then 
   cat $FLIST | ${PERL} -n -a $TMPFILE | sort | uniq | ${COUNTCMD}
else 
   printf "" | ${COUNTCMD}
fi

rm -rf $TMPFILE
