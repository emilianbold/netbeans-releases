package test.pkg.not.in.junit;

import junit.framework.Test;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;

public class NbModuleSuiteMockServiceTest extends NbTestCase {

    public static Test suite() {
        NbModuleSuite.Configuration testConfig = NbModuleSuite.createConfiguration(NbModuleSuiteMockServiceTest.class);
        testConfig = testConfig.gui(false);
        return testConfig.suite();
    }

    public NbModuleSuiteMockServiceTest(String name) {
        super(name);
    }

    public void testMockService() {
        MockServices.setServices(DD.class);
        DD dd = Lookup.getDefault().lookup(DD.class);
        assertNotNull("DD found", dd);
        assertEquals("Same class", DD.class, dd.getClass());
    }

   public static class DD {


   }

}
