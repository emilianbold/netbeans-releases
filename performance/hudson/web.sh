#!/bin/bash -x
if test ! -e /space/hudsonserver/master 
then
if [ -n "$web_enabled" ]
then

cd "$performance"/web
rm -rf "$WORKSPACE"/web
ant clean -Dnetbeans.dest.dir="$netbeans_dest"  
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureWebSetupTest* -Dnetbeans.dest.dir="$netbeans_dest" -Drepeat=1 -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dnetbeans.keyring.no.master=true -Dorg.netbeans.editor.linewrap=true
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureWebMenusTest* -Dnetbeans.dest.dir="$netbeans_dest" -Drepeat=1 -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dnetbeans.keyring.no.master=true -Dorg.netbeans.editor.linewrap=true
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureWebMenusTest* -Dnetbeans.dest.dir="$netbeans_dest" -Drepeat=1 -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dnetbeans.keyring.no.master=true -Dorg.netbeans.editor.linewrap=true
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureWebMenusTest* -Dnetbeans.dest.dir="$netbeans_dest" -Drepeat=7 -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dnetbeans.keyring.no.master=true -Dorg.netbeans.editor.linewrap=true
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureWebActionsTest* -Dnetbeans.dest.dir="$netbeans_dest" -Drepeat=1 -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dnetbeans.keyring.no.master=true -Dorg.netbeans.editor.linewrap=true
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureWebActionsTest* -Dnetbeans.dest.dir="$netbeans_dest" -Drepeat=1 -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dnetbeans.keyring.no.master=true -Dorg.netbeans.editor.linewrap=true
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureWebActionsTest* -Dnetbeans.dest.dir="$netbeans_dest" -Drepeat=7 -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dnetbeans.keyring.no.master=true -Dorg.netbeans.editor.linewrap=true

touch "$performance"/web/build/test/qa-functional/work/userdir0
touch "$performance"/web/build/test/qa-functional/work/tmpdir
rm -rf "$performance"/web/build/test/qa-functional/work/userdir0
rm -rf "$performance"/web/build/test/qa-functional/work/tmpdir
cp -R build/test/qa-functional/work/ "$WORKSPACE"/web
cp -R build/test/qa-functional/results "$WORKSPACE"/web

fi
fi
