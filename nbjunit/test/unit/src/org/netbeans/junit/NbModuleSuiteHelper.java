package org.netbeans.junit;

import junit.framework.TestCase;

public class NbModuleSuiteHelper extends TestCase {
    static {
        System.err.println("here");
    }

    public NbModuleSuiteHelper(String t) {
        super(t);
    }

    public void testOne() {
        System.setProperty("t.one", "OK");
    }
}
