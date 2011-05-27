#!/bin/bash -x

if test ! -e /space/hudsonserver/master 
then

# ergonomics root
cd "$project_root"
rm -rf nbbuild/nbproject/private
ant bootstrap

# performance project
cd "$performance"

rm -rf build/test/unit/results
rm -rf build/test/qa-functional/results

ant clean -Dnetbeans.dest.dir=$netbeans_dest
ant -Dnetbeans.dest.dir=$netbeans_dest

ant test-unit -Dsuite.dir=test -Dtest.includes=**/fod/* -Dnetbeans.dest.dir=$netbeans_dest -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dnetbeans.keyring.no.master=true -Dorg.netbeans.editor.linewrap=true
ant test-unit -Dsuite.dir=test -Dtest.includes=**/fod/* -Dnetbeans.dest.dir=$netbeans_dest -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dnetbeans.keyring.no.master=true -Dorg.netbeans.editor.linewrap=true
ant test-unit -Dsuite.dir=test -Dtest.includes=**/fod/* -Dnetbeans.dest.dir=$netbeans_dest -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dnetbeans.keyring.no.master=true -Dorg.netbeans.editor.linewrap=true

buildnum=`cat "$reposdir"/build.number`
str1="<property name=\"perftestrun.buildnumber\" value=\"$buildnum\"/>"
str2="<property name=\"env.BUILD_NUMBER\" value=\"`echo $BUILD_NUMBER`\" />"
str3="<property name=\"env.JOB_NAME\" value=\"`echo $JOB_NAME`\" />"
export str="$str1 $str2 $str3"

gawk -v str="$str" '{print} NR == 4 {printf (str);}'  "$performance"/build/test/unit/results/TEST-org.netbeans.performance.fod.EnablementSpeedBase.xml > tmp.xml && mv tmp.xml "$performance"/build/test/unit/results/TEST-org.netbeans.performance.fod.EnablementSpeedBase.xml
sed -i "s/\(<property name=\"buildnumber\" value=\"\).*\(\"\)/\1$buildnum\2/g" $performance/build/test/unit/results/TEST-org.netbeans.performance.fod.EnablementSpeedBase.xml

gawk -v str="$str" '{print} NR == 4 {printf (str);}'  "$performance"/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableJavaTest.xml > tmp.xml && mv tmp.xml "$performance"/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableJavaTest.xml 
sed -i "s/\(<property name=\"buildnumber\" value=\"\).*\(\"\)/\1$buildnum\2/g" $performance/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableJavaTest.xml

gawk -v str="$str" '{print} NR == 4 {printf (str);}'  "$performance"/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableCNDTest.xml > tmp.xml && mv tmp.xml "$performance"/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableCNDTest.xml 
sed -i "s/\(<property name=\"buildnumber\" value=\"\).*\(\"\)/\1$buildnum\2/g" $performance/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableCNDTest.xml 

gawk -v str="$str" '{print} NR == 4 {printf (str);}'  "$performance"/build/test/unit/results/TEST-org.netbeans.performance.fod.EnablePHPTest.xml > tmp.xml && mv tmp.xml "$performance"/build/test/unit/results/TEST-org.netbeans.performance.fod.EnablePHPTest.xml 
sed -i "s/\(<property name=\"buildnumber\" value=\"\).*\(\"\)/\1$buildnum\2/g" $performance/build/test/unit/results/TEST-org.netbeans.performance.fod.EnablePHPTest.xml 

gawk -v str="$str" '{print} NR == 4 {printf (str);}'  "$performance"/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableEnterpriseTest.xml > tmp.xml && mv tmp.xml "$performance"/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableEnterpriseTest.xml 
sed -i "s/\(<property name=\"buildnumber\" value=\"\).*\(\"\)/\1$buildnum\2/g" $performance/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableEnterpriseTest.xml 

cp -R build/test/unit/work/ "$WORKSPACE"/fod
cp -R build/test/unit/results/ "$WORKSPACE"/fod
rm -rf "$WORKSPACE"/fod/userdir0
rm -rf "$WORKSPACE"/fod/tmpdir

fi
