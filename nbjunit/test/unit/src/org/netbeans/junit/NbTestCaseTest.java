/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.junit;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestResult;
import junit.framework.AssertionFailedError;

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
    
    public void testNetBeansFullHackIsSet() throws Exception {
        assertEquals("true", System.getProperty("netbeans.full.hack"));
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
    
    public void testAssertGcPasses() {
        Object o = new Object();
        WeakReference<Object> wr = new WeakReference<Object>(o);
        
        o = null;
        assertGC("The object is really not referenced", wr);
    }

    static Object REF_O;

    public void testAssertGcFails() {
        REF_O = new Object();
        WeakReference<Object> wr = new WeakReference<Object>(REF_O);
        try {
            assertGC("The object is really not referenced", wr);
        } catch (AssertionFailedError afe) {
            assertTrue("Found the reference", afe.getMessage().indexOf("REF_O") >= 0);
            return;
        } finally {
            REF_O = null;
        }
        fail("The assertion should fail");
    }

    public void testAssertGcFailsForUntracableObject() {
        Object o = new Object();
        WeakReference<Object> wr = new WeakReference<Object>(o);
        
        try {
            assertGC("The object is really not referenced", wr);
        } catch (AssertionFailedError afe) {
            assertTrue("Found the reference", afe.getMessage().indexOf("Not found") >= 0);
            return;
        }
        fail("The assertion should fail");
    }
    
    private static String previous;
    
    public void testWorkDirLength() throws IOException {
        System.setProperty("nbjunit.too.long", "10");

        getLog().println("Ahoj");
        clearWorkDir();

        String s = getWorkDirPath();
        if (s.equals(previous)) {
            fail("Inside one testcase, one directory shall not be used multiple times: " + previous + " now: " + s);
        }
        previous = s;
            
        if (s.length() > 200) {
            fail("Too long (" + s.length() + "): " + s);
        }
        if (s.indexOf(".netbeans.") > 0) {
            fail("package names shall be abbreviated: " + s);
        }
        if (s.indexOf("testWorkDir") > 0) {
            fail("no testWorkDir: " + s);
        }
    }
    
    public void testWorkDirLengthsimilarname() throws IOException {
        testWorkDirLength();
    }
}
