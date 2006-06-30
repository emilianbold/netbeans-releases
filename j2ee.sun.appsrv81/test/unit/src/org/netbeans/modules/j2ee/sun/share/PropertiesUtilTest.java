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
