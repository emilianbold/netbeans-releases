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
