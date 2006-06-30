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
