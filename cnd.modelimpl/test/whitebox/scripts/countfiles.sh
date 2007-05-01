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
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
# Microsystems, Inc. All Rights Reserved.
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
