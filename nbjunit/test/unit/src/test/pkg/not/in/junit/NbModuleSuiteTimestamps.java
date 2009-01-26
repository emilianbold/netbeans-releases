package test.pkg.not.in.junit;

import java.io.File;
import junit.framework.TestCase;

public class NbModuleSuiteTimestamps extends TestCase {

    public NbModuleSuiteTimestamps(String t) {
        super(t);
    }

    public void testCheckUserDirStamps() {
        long current = Long.getLong("stamps", -1);
        File userDir = new File(System.getProperty("netbeans.user"));
        assertTrue("User dir exists: " + userDir, userDir.exists());
        File[] newest = new File[1];
        long now = stamps(userDir, current, newest);
        if (current >= 0) {
            assertEquals("Old and new value is the same, newest for " + newest[0], current, now);
        }
        System.setProperty("stamps", String.valueOf(now));
    }

    private static long stamps(File f, long current, File[] newest) {
        if (f.isDirectory()) {
            for (File subFile : f.listFiles()) {
                current = stamps(subFile, current, newest);
            }
        } else {
            if (f.getName().endsWith("xml")) {
                long s = f.lastModified();
                if (s > current) {
                    current = s;
                    newest[0] = f;
                }
            }
        }
        return current;
    }
}
