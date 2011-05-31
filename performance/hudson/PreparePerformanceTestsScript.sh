#!bash -x

#if test ! -e /space/hudsonserver/master 
#then
env
jps | grep JUnitTestRunner | cut -d' ' -f1 | xargs kill -9
cd "$WORKSPACE"/../../../../../
reposdir=`pwd`
reposdir=`cygpath -m $reposdir`
netbeansdir="$reposdir"/netbeans
netbeansdir=`cygpath -m $netbeansdir`
filename=`ls "$reposdir"/zip`
echo "filename=$filename"
pnum=`gawk -v s=${filename} 'BEGIN {print substr(s,24,12)}'`
echo "pnum=$pnum"
echo -n ${pnum}>$reposdir/build.number

cp -f $reposdir/ergonomics/performance/hudson/netbeans.conf $netbeansdir/etc

cd $reposdir/ergonomics
pwd
hg pull -u
touch performance/tmp.xml
rm `echo \`hg status|grep ?|gawk -F " " '{print "./" $2}'\``
ant clean
rm -rf nbbuild/nbproject/private
rm -rf performance/build
rm -rf performance/*/build
rm -f $reposdir/netbeans/ergonomics/config/Modules/*
cd ../ergonomics/
ant bootstrap
cd ../ergonomics/performance
ant -Dnetbeans.dest.dir=$netbeansdir
cd j2se
ant -Dnetbeans.dest.dir=$netbeansdir
cd ../j2ee
ant -Dnetbeans.dest.dir=$netbeansdir
cd ../enterprise
ant -Dnetbeans.dest.dir=$netbeansdir
cd ../languages
ant -Dnetbeans.dest.dir=$netbeansdir
cd ../mobility
ant -Dnetbeans.dest.dir=$netbeansdir
cd ../web
ant -Dnetbeans.dest.dir=$netbeansdir
rm -f $reposdir/netbeans/ide/Modules/dict/*
#fi