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
/*
 * PropertiesUtilTest.java
 * JUnit based test
 *
 * Created on March 17, 2004, 4:14 PM
 */

package org.netbeans.modules.j2ee.sun.share;

import java.util.Properties;
import junit.framework.*;

/**
 *
 * @author vkraemer
 */
public class PropertiesUtilTest extends TestCase {
    
    public PropertiesUtilTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(PropertiesUtilTest.class);
        return suite;
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public void  testSetArrayPropertyValue() {
        Properties p = new Properties();
        PropertiesUtil.setArrayPropertyValue(p, "a.", null);
        assertNull(p.getProperty("a.0"));
        PropertiesUtil.setArrayPropertyValue(p, "a.", new String[] { "a", null, "b", "c"} );
        assertNull(p.getProperty("a.3"));
        assertEquals(p.getProperty("a.0"),"a");
        assertEquals(p.getProperty("a.1"),"b");
        assertEquals(p.getProperty("a.2"),"c");
    }
        
        
        
    
    /** Test of getArrayPropertyValue method, of class org.netbeans.modules.j2ee.sun.share.PropertiesUtil. */
    public void testGetArrayPropertyValue() {
        Properties p = new Properties();
        
        String[] one = PropertiesUtil.getArrayPropertyValue(p,  "a.");
        
        assertNotNull(one);
        assertTrue(one.length == 0);
        
        p.setProperty("a.1", "bad");
        
        one = PropertiesUtil.getArrayPropertyValue(p,  "a.");
        
        assertNotNull(one);
        assertTrue(one.length == 0);
        
        p.setProperty("a.0", "good");
        
        one = PropertiesUtil.getArrayPropertyValue(p,  "a.");
        
        assertNotNull(one);
        assertTrue(one.length == 2);
        
        p.setProperty("a.0", "good");
    }
    
    // Add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    
    
}
