/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.junit;

import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestResult;

/** Regular test of the behaviour.
 *
 * @author jarda
 */
public class NbTestCaseTest extends NbTestCase {
    
    public NbTestCaseTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void testJustRunTestCase() {
        class Fail extends NbTestCase {
            public Fail() {
                super("testFail");
            }

            public void testFail() {
                throw new IllegalStateException();
            }
        }

        Fail f = new Fail();

        TestResult res = new TestResult();
        f.run(res);

        assertEquals("One error", 1, res.errorCount());


    }


    public void testLoggingUtil() throws Exception {
        CharSequence seq = Log.enable("", Level.WARNING);

        Logger log = Logger.getLogger(getName());
        log.log(Level.SEVERE, "Ahoj");
        log.log(Level.FINE, "Jardo");



        String s = seq.toString();
        if (s.indexOf("Ahoj") == -1) {
            fail("There should be Ahoj\n" + s);
        }
        assertEquals("Not logged for FINE: " + s, -1, s.indexOf("Jardo"));

        WeakReference<CharSequence> r = new WeakReference<CharSequence>(seq);
        seq = null;
        assertGC("Sequence can go away", r);

        int len = Logger.getLogger("").getHandlers().length;

        log.log(Level.WARNING, "Go away");

        assertEquals("One logger is gone", len - 1, Logger.getLogger("").getHandlers().length);
    }
    
}
