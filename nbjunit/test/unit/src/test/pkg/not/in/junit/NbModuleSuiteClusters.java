package test.pkg.not.in.junit;

import java.io.File;
import junit.framework.TestCase;

public class NbModuleSuiteClusters extends TestCase {

    public NbModuleSuiteClusters(String t) {
        super(t);
    }

    public void testSetClusters() {
        String dirs = System.getProperty("netbeans.dirs");
        assertNotNull("Dirs specified", dirs);

        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (String d : dirs.replace(File.separatorChar, '/').split(File.pathSeparator)) {
            String sd = d.replaceFirst(".*/", "");
            if (sd.matches("platform|harness|extra")) { // extra for libs.junit4
                continue;
            }
            sb.append(sep).append(sd);
            sep = ":";
        }
        System.setProperty("clusters", sb.toString());
    }
}
