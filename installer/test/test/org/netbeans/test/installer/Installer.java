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
        Utils.stepChooseComponet("Apache Tomcat");
        // Welcome
        Utils.stepWelcome();
        // Agreement
        Utils.stepLicense();
        // Location
        Utils.stepSetDir(data, "Install the NetBeans IDE", Utils.NB_DIR_NAME );
        // GF
        Utils.stepSetDir(data, "Install GlassFish", Utils.GF2_DIR_NAME);
        // Apache
        Utils.stepSetDir(data, "Install Apache Tomcat", Utils.TOMCAT_DIR_NAME);
        // Summary
        Utils.stepInstall(data);
        //Installation
        //finish
        Utils.stepFinish();


        //Utils.phaseTwo(data);
        //Utils.phaseThree(data);
        Utils.phaseFour(data);

        Utils.phaseFive( data );
        //Utils.phaseFourWOUninstall(data);
      //TODO Dir removed test
      //TODO Clean up work dir
    }
}
