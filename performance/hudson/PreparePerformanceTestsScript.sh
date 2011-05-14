#!bash -x

#if test ! -e /space/hudsonserver/master 
#then
env
jps | grep JUnitTestRunner | cut -d' ' -f1 | xargs kill -9
cd "$WORKSPACE"/../../../../
jobdir=`pwd`
jobdir=`cygpath -m $jobdir`
cd ..
reposdir=`pwd`
reposdir=`cygpath -m $reposdir`
filename=`ls "$jobdir"/zip`
echo "filename=$filename"
pnum=`gawk -v s=${filename} 'BEGIN {print substr(s,24,12)}'`
echo "pnum=$pnum"
echo -n ${pnum}>$reposdir/build.number

cp -f $reposdir/netbeans.conf $jobdir/netbeans/etc

cd $reposdir/ergonomics
pwd
hg pull -u
touch performance/tmp.xml
rm `echo \`hg status|grep ?|gawk -F " " '{print "./" $2}'\``
ant clean
rm -rf nbbuild/nbproject/private
rm -rf performance/build
rm -rf performance/*/build
#rm -f $jobdir/netbeans/ergonomics/config/Modules/*
cd ../ergonomics/
ant bootstrap
cd ../ergonomics/performance
ant test-build -Dnetbeans.dest.dir=./../../netbeans
cd j2se
ant test-build -Dnetbeans.dest.dir=./../../../netbeans
cd ../j2ee
ant test-build -Dnetbeans.dest.dir=./../../../netbeans
cd ../enterprise
ant test-build -Dnetbeans.dest.dir=./../../../netbeans
cd ../languages
ant test-build -Dnetbeans.dest.dir=./../../../netbeans
cd ../mobility
ant test-build -Dnetbeans.dest.dir=./../../../netbeans
cd ../web
ant test-build -Dnetbeans.dest.dir=./../../../netbeans
#fi