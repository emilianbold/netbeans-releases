/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import org.netbeans.junit.NbTestCase;

/** Test for increments of spec versions.
 *
 * @author Jaroslav Tulach
 */
public class IncrementSpecificationVersionsTest extends NbTestCase {
    
    public IncrementSpecificationVersionsTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public void testIncrement() {
        String res;
        
        assertIncrement("1.0", false, true, "1.1");
        assertIncrement("1.0", true, true, "1.0.1");
        assertIncrement("1.0", false, false, null);
        assertIncrement("1.0", true, false, null);
        assertIncrement("1.0.0", false, false, "1.1.0");
        assertIncrement("1.0.0", true, false, "1.0.1");
    }
    
    public void testIncrementLevel4() {
        String res;
        
        assertIncrement("1.2.3.4.5.6.7", 4, true, "1.2.3.4.6");
        assertIncrement("1.0", 4, true, "1.0.0.0.1");
        assertIncrement("1.2.3.4.5", 4, true, "1.2.3.4.6");
    }
    
    private static void assertIncrement(String old, boolean branch, boolean manifest, String res) {
        String r = IncrementSpecificationVersions.increment(old, branch ? 2 : 1, manifest);
        assertEquals("Old: " + old + " branch: " + branch + " manifest: " + manifest, res, r);
    }
    private static void assertIncrement(String old, int stickyLevel, boolean manifest, String res) {
        String r = IncrementSpecificationVersions.increment(old, stickyLevel, manifest);
        assertEquals("Old: " + old + " stickyLevel: " + stickyLevel + " manifest: " + manifest, res, r);
    }
}
