#!/bin/bash

if test ! -e /space/hudsonserver/master 
then

#project_root=$WORKSPACE/../../../../ergonomics
#netbeans_dest=$WORKSPACE/../../../../netbeans
#performance=$project_root/performance

# ergonomics root
cd $project_root
rm -rf nbbuild/nbproject/private

# performance project
cd $performance

rm -rf build/test/unit/results
rm -rf build/test/qa-functional/results

ant clean -Dnetbeans.dest.dir=$netbeans_dest
ant -Dnetbeans.dest.dir=$netbeans_dest

ant test-unit -Dsuite.dir=test -Dtest.includes=**/fod/* -Dnetbeans.dest.dir=$netbeans_dest
ant test-unit -Dsuite.dir=test -Dtest.includes=**/fod/* -Dnetbeans.dest.dir=$netbeans_dest
ant test-unit -Dsuite.dir=test -Dtest.includes=**/fod/* -Dnetbeans.dest.dir=$netbeans_dest

buildnum=`cat $WORKSPACE/../../../../build.number`
str1="<property name=\"perftestrun.buildnumber\" value=\"$buildnum\"/>"
str2="<property name=\"env.BUILD_NUMBER\" value=\"`echo $BUILD_NUMBER`\" />"
str3="<property name=\"env.JOB_NAME\" value=\"`echo $JOB_NAME`\" />"
export str="$str1 $str2 $str3"

gawk -v str="$str" '{print} NR == 4 {printf (str);}'  $performance/build/test/unit/results/TEST-org.netbeans.performance.fod.EnablementSpeedBase.xml > tmp.xml && mv tmp.xml $performance/build/test/unit/results/TEST-org.netbeans.performance.fod.EnablementSpeedBase.xml
sed -i "s/\(<property name=\"buildnumber\" value=\"\).*\(\"\)/\1$buildnum\2/g" $performance/build/test/unit/results/TEST-org.netbeans.performance.fod.EnablementSpeedBase.xml

gawk -v str="$str" '{print} NR == 4 {printf (str);}'  $performance/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableJavaTest.xml > tmp.xml && mv tmp.xml $performance/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableJavaTest.xml 
sed -i "s/\(<property name=\"buildnumber\" value=\"\).*\(\"\)/\1$buildnum\2/g" $performance/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableJavaTest.xml

gawk -v str="$str" '{print} NR == 4 {printf (str);}'  $performance/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableCNDTest.xml > tmp.xml && mv tmp.xml $performance/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableCNDTest.xml 
sed -i "s/\(<property name=\"buildnumber\" value=\"\).*\(\"\)/\1$buildnum\2/g" $performance/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableCNDTest.xml 

gawk -v str="$str" '{print} NR == 4 {printf (str);}'  $performance/build/test/unit/results/TEST-org.netbeans.performance.fod.EnablePHPTest.xml > tmp.xml && mv tmp.xml $performance/build/test/unit/results/TEST-org.netbeans.performance.fod.EnablePHPTest.xml 
sed -i "s/\(<property name=\"buildnumber\" value=\"\).*\(\"\)/\1$buildnum\2/g" $performance/build/test/unit/results/TEST-org.netbeans.performance.fod.EnablePHPTest.xml 

gawk -v str="$str" '{print} NR == 4 {printf (str);}'  $performance/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableRubyTest.xml > tmp.xml && mv tmp.xml $performance/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableRubyTest.xml 
sed -i "s/\(<property name=\"buildnumber\" value=\"\).*\(\"\)/\1$buildnum\2/g" $performance/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableRubyTest.xml 

gawk -v str="$str" '{print} NR == 4 {printf (str);}'  $performance/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableEnterpriseTest.xml > tmp.xml && mv tmp.xml $performance/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableEnterpriseTest.xml 
sed -i "s/\(<property name=\"buildnumber\" value=\"\).*\(\"\)/\1$buildnum\2/g" $performance/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableEnterpriseTest.xml 

cp -R build/test/unit/work/ $WORKSPACE/fod
cp -R build/test/unit/results/ $WORKSPACE/fod
rm -rf $WORKSPACE/fod/userdir0
rm -rf $WORKSPACE/fod/tmpdir

fi
