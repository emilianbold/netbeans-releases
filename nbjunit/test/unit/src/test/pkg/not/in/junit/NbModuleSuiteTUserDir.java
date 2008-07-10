package test.pkg.not.in.junit;

import java.io.File;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;

public class NbModuleSuiteTUserDir extends NbTestCase {

    public NbModuleSuiteTUserDir(String t) {
        super(t);
    }

    public void testUserDir() throws IOException {
        File testfile = new File(System.getProperty("netbeans.user"), "testfile");
        if (testfile.exists()) {
            System.setProperty("t.userdir", "Exists");
        } else {
            System.setProperty("t.userdir", "Doesn't exist");
            testfile.createNewFile();
        }
        
    }
}
