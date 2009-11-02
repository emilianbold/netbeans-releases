package org.netbeans.test.installer;

import java.util.logging.Logger;

/**
 *
 * @author Mikhail Vaysman
 */
public class Installer {

    @org.junit.Test
    public void testInstaller() {
        TestData data = new TestData(Logger.getLogger("global"));

        Utils.phaseOne(data, "all");

        // Pages
          // Apache
        Utils.stepChooseComponet( "Apache Tomcat", data );
        // Welcome
        Utils.stepWelcome();
        // Agreement
        Utils.stepLicense();
        // Location
        Utils.stepSetDir(
            data,
            "Install the NetBeans IDE",
            data.GetNetBeansInstallPath( )
          );
        // GF
        Utils.stepSetDir(
            data,
            "Install GlassFish",
            data.GetApplicationServerInstallPath( )
          );
        if( data.m_bPreludePresents )
        {
          Utils.stepSetDir(
              data,
              "Install GlassFish prelude",
              data.GetApplicationServerPreludeInstallPath( )
            );
        }
        // Apache
        Utils.stepSetDir(
            data,
            "Install Apache Tomcat",
            data.GetTomcatInstallPath( )
          );
        // Summary
        Utils.stepInstall(data);
        //Installation
        //finish
        Utils.stepFinish();

        //Utils.phaseTwo(data);
        //Utils.phaseThree(data);
        Utils.phaseFour(data);

        //Utils.RunCommitTests( data );

        Utils.phaseFive( data );

        //Utils.phaseFourWOUninstall(data);
      //TODO Dir removed test
      //TODO Clean up work dir
    }
}
