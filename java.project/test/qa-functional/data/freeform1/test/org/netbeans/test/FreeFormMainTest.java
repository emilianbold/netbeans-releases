/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * FreeFormMainTest.java
 * JUnit based test
 *
 * Created on June 14, 2004, 3:58 PM
 */

package org.netbeans.test;
import junit.framework.*;


import junit.framework.*;

/**
 *
 * @author mkubec
 */
public class FreeFormMainTest extends TestCase {
    
    public FreeFormMainTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(FreeFormMainTest.class);
        return suite;
    }

    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    /**
     * Test of getString method, of class org.netbeans.test.FreeFormMain.
     */
    public void testGetString() {
        System.out.println("testGetString");
        assertEquals("Ahoj", FreeFormMain.getString());
    }

}
