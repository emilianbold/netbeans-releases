Readme for visualweb/samples moduleas
=====================================

1. Manifest file samples/bundled/manifest.mf must contain

   OpenIDE-Module-Install: org/netbeans/modules/visualweb/samples/bundled/BundledModuleInstaller.class

   to install any complibs into the user.dir.
   
   Modify build.xml to create release.dir and copy any complibs from samples/complibs into $release.dir}/samples/complibs
   
   Then add similar code to 
   
   samples\bundled\src\org\netbeans\modules\visualweb\samples\bundled\BundledModuleInstaller class:
   
    public void restored() {
        Preferences preferences = NbPreferences.forModule( BundledModuleInstaller.class );
        try {
            if ( preferences.get(SAMPLES_BUNDLED_COMPLIBS, null) == null ) {
                File samplesComplibsDir = InstalledFileLocator.getDefault().locate("samples/complibs", null, false ); // NOI18N
                ComplibService complibService = (ComplibService) Lookup.getDefault().lookup( ComplibService.class );
                for ( File complibFile : samplesComplibsDir.listFiles() ) {
                    complibService.installComplibFile( complibFile, false );
                }
                preferences.put(SAMPLES_BUNDLED_COMPLIBS, INSTALLED);
            }
        } catch (ComplibException ce) {
            Logger.getLogger("org.netbeans.modules.visualweb.samples.bundled").log(Level.WARNING, ce.getMessage());
        }
    }
   
2. To add a web app to the NBM, you must do the following:

    a. Check in the cleaned web app project to the samples/webapps directory. Make sure you do not include any
       .class, .java~, .bak, etc. files or nbproject/private directory.
       
    b. Check in a description HTML file for the IDE GUI layer file. (For an example see the webSamplesCategory.html
       file in the module's src/org/netberans/modules/visualweb/samples/bundled/descriptions directory.
       
    c. Modify the src/org/netbeans/modules/visualweb/samples/[bundled|postrel]/layer.xml to include the sample web app
       in the IDE GUI.
       
    <folder name="Templates">
        <folder name="Project">
            <folder name="Samples">
                <folder name="Web">
                    <folder name="VisualJsf">
                        <attr name="position" intvalue="500" />
                        <attr name="SystemFileSystem.localizingBundle" stringvalue="org.netbeans.modules.visualweb.samples.bundled.Bundle"/>
                        <attr name="instantiatingWizardURL" urlvalue="nbresloc:/org/netbeans/modules/visualweb/samples/bundled/descriptions/webSamplesCategory.html"/>
                        <file name="ExampleOne" url="ExampleOne.zip">
                           <attr name="template" boolvalue="true"/>
                           <attr name="templateCategory" stringvalue="web-types"/>
                           <attr name="position" intvalue="100"/>
                           <attr name="instantiatingWizardURL" urlvalue="nbresloc:/org/netbeans/modules/visualweb/samples/bundled/descriptions/ExampleOne.html"/>
                           <attr name="SystemFileSystem.localizingBundle" stringvalue="org.netbeans.modules.visualweb.samples.bundled.Bundle"/>
                           <attr name="SystemFileSystem.icon" urlvalue="nbresloc:/org/netbeans/modules/visualweb/samples/bundled/ui/resources/webProjectIcon.gif"/>
                           <attr name="instantiatingIterator" methodvalue="org.netbeans.modules.visualweb.samples.bundled.wizard.SamplesWebWizardIterator.createIterator"/>
                        </file>
                    </folder>
                </folder>
            </folder>
        </folder>
    </folder> 
       
    d. Add the following tasks to the samples/bundled/build.xml file in the gather-samples target:
    
        <mkdir dir="${webapps.desc.dest}" /> <!-- Optional if the module you're adding to has no web apps in it already. -->
        <copy file="${webapps.desc}/ExampleOne.html" todir="${webapps.desc.dest}" />
        <zip basedir="${webapps.repos}/ExampleOne" excludes="CVS,private,.cvsignore" destfile="${webapps.dest}/ExampleOne.zip" />  
    
