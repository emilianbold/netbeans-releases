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

        //select apache
        Utils.stepChooseComponet("Apache Tomcat");

        Utils.phaseTwo(data);
        Utils.phaseThree(data);
        Utils.phaseFourWOUninstall(data);
    }
}
