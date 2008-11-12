package org.netbeans.test.installer;

import java.util.logging.Logger;
import junit.framework.TestCase;

/**
 *
 * @author Mikhail Vaysman
 */
public class InstallerTest extends TestCase {

    public InstallerTest() {
        super("Installer test");
    }

    @org.junit.Test
    public void testInstaller() {
        TestData data = new TestData(Logger.getLogger("global"));

        Utils.phaseOne(this, data, "all");

        //select apache
        Utils.stepChooseComponet("Apache Tomcat");

        Utils.phaseTwo(data);
        Utils.phaseThree(data);
        Utils.phaseFourWOUninstall(data);
    }
}
