if test ! -e /space/hudsonserver/master 
then

if [ -n $languages_enabled ]
then

cd $performance/languages
rm -rf $WORKSPACE/languages

ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/ScriptingMeasuringSetupTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/ScriptingMeasureMenusTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/ScriptingMeasureMenusTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/ScriptingMeasureMenusTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=7
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/ScriptingMeasureDialogsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/ScriptingMeasureDialogsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/ScriptingMeasureDialogsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=7
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/ScriptingMeasureActionsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/ScriptingMeasureActionsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/ScriptingMeasureActionsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=7
cp -R build/test/qa-functional/work/ $WORKSPACE/languages
cp -R build/test/qa-functional/results/ $WORKSPACE/languages
rm -rf $WORKSPACE/languages/userdir0
rm -rf $WORKSPACE/languages/tmpdir

fi
fi
