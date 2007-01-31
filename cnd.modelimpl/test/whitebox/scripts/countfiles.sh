#!/bin/sh

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
