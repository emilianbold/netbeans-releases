package org.netbeans.test.installer;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import junit.framework.TestCase;

/**
 *
 * @author Mikhail Vaysman
 */
public class Uninstaller {

    @org.junit.Test
    public void testUninstaller() {
        TestData data = new TestData(Logger.getLogger("global"));

        try {
            String wd = System.getenv("WORKSPACE");
            TestCase.assertNotNull(wd);
            data.setWorkDir(new File(wd));
        } catch (IOException ex) {
            TestCase.fail("Can not get WorkDir");
        }


        System.setProperty("nbi.dont.use.system.exit", "true");
        System.setProperty("nbi.utils.log.to.console", "false");
        System.setProperty("servicetag.allow.register", "false");
        System.setProperty("show.uninstallation.survey", "false");
        System.setProperty("user.home", data.getWorkDirCanonicalPath());
        
        Utils.phaseFive(data);
    }
}
