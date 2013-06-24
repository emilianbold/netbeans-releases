#!/bin/bash -x
if test ! -e /space/hudsonserver/master
then

if [ -n "$languages_enabled" ]
then

cd $performance/languages
rm -rf "$WORKSPACE"/languages

ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/ScriptingMeasuringSetupTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dorg.netbeans.editor.linewrap=true
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/ScriptingMeasureMenusTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dorg.netbeans.editor.linewrap=true
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/ScriptingMeasureMenusTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dorg.netbeans.editor.linewrap=true
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/ScriptingMeasureMenusTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=7 -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dorg.netbeans.editor.linewrap=true
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/ScriptingMeasureDialogsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dorg.netbeans.editor.linewrap=true
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/ScriptingMeasureDialogsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dorg.netbeans.editor.linewrap=true
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/ScriptingMeasureDialogsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=7 -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dorg.netbeans.editor.linewrap=true
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/ScriptingMeasureActionsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dorg.netbeans.editor.linewrap=true
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/ScriptingMeasureActionsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dorg.netbeans.editor.linewrap=true
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/ScriptingMeasureActionsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=7 -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dorg.netbeans.editor.linewrap=true

touch "$performance"/languages/build/test/qa-functional/work/userdir0
touch "$performance"/languages/build/test/qa-functional/work/tmpdir
rm -rf "$performance"/languages/build/test/qa-functional/work/userdir0
rm -rf "$performance"/languages/build/test/qa-functional/work/tmpdir
cp -R build/test/qa-functional/work/ "$WORKSPACE"/languages
cp -R build/test/qa-functional/results "$WORKSPACE"/languages
fi
fi
