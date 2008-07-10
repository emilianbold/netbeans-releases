package test.pkg.not.in.junit;

import junit.framework.TestCase;

public class NbModuleSuiteT extends TestCase {

    public NbModuleSuiteT(String t) {
        super(t);
    }

    public void testOne() {
        System.setProperty("t.one", "OK");
    }

    public void testFullhack() {
        System.setProperty("t.hack", System.getProperty("netbeans.full.hack"));
    }
}
