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

package org.openide.modules;

import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.modules.SpecificationVersion;

/** Test parsing of specification versions.
 * @author Jesse Glick
 */
public class SpecificationVersionTest extends NbTestCase {
    
    public SpecificationVersionTest(String name) {
        super(name);
    }
    
    public void testParseAndCompare() throws Exception {
        SpecificationVersion v = new SpecificationVersion("1.2.3");
        assertEquals("1.2.3", v.toString());
        assertTrue(v.compareTo(new SpecificationVersion("1.2.3")) == 0);
        assertTrue(v.compareTo(new SpecificationVersion("2.4.6")) < 0);
        assertTrue(v.compareTo(new SpecificationVersion("1.2.4")) < 0);
        assertTrue(v.compareTo(new SpecificationVersion("1.2.0")) > 0);
        assertTrue(v.compareTo(new SpecificationVersion("1.2")) > 0);
        assertTrue(v.compareTo(new SpecificationVersion("1.3")) < 0);
        assertTrue(v.compareTo(new SpecificationVersion("1.2.3.0")) == 0);
        assertTrue(v.compareTo(new SpecificationVersion("1.2.2.99")) > 0);
        assertTrue(v.compareTo(new SpecificationVersion("1.3.0")) < 0);
        assertTrue(v.compareTo(new SpecificationVersion("1")) > 0);
        assertTrue(v.compareTo(new SpecificationVersion("2")) < 0);
        v = new SpecificationVersion("10.99.3");
        assertTrue(v.compareTo(new SpecificationVersion("10.9.4")) > 0);
        assertTrue(v.compareTo(new SpecificationVersion("10.100")) < 0);
    }
    
    public void testMisparse() throws Exception {
        misparse("");
        misparse("1.");
        misparse(".1");
        misparse("-1");
        misparse("0x13");
        misparse("2..4");
        misparse("2...4");
        misparse("13.8.");
        misparse("1.4.0beta");
        misparse("hello");
    }
    
    private void misparse(String s) throws Exception {
        try {
            new SpecificationVersion(s);
            assertTrue("Should have misparsed: " + s, false);
        } catch (NumberFormatException nfe) {
            // OK, expected.
        }
    }
    
}
