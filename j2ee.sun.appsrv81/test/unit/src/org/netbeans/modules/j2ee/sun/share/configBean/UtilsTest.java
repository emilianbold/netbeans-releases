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

package org.netbeans.modules.j2ee.sun.share.configbean;

import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import junit.framework.TestCase;

/**
 *
 * @author vkraemer
 */
public class UtilsTest extends TestCase {

    /** Test of makeCE method, of class org.netbeans.modules.j2ee.sun.share.configbean.Utils. */
    public void testMakeCE() {
        ConfigurationException ce = Utils.makeCE(null, null, null);
        assertEquals(ce.getMessage(),"ConfigurationException references unknown message key 'null' has params '{1}' '{2}' '{3}' '{4}' '{5}'");
        ce = Utils.makeCE("ERR_UnexpectedRuntimeException", null, null);
        assertEquals(ce.getMessage(),"Unexpected runtime exception");     
        ce = Utils.makeCE("unknownMessageKeyValue", null, null);
        assertEquals(ce.getMessage(),"ConfigurationException references unknown message key 'unknownMessageKeyValue' has params '{1}' '{2}' '{3}' '{4}' '{5}'");
        Object args[] = new Object[0];
        ce = Utils.makeCE("ERR_UnexpectedRuntimeException", args, null);
        assertEquals(ce.getMessage(),"Unexpected runtime exception");     
        ce = Utils.makeCE("unknownMessageKeyValue", args, null);
        assertEquals(ce.getMessage(),"ConfigurationException references unknown message key 'unknownMessageKeyValue' has params '{1}' '{2}' '{3}' '{4}' '{5}'");
        args = new Object[1];
        ce = Utils.makeCE("unknownMessageKeyValue", args, null);
        assertEquals(ce.getMessage(),"ConfigurationException references unknown message key 'unknownMessageKeyValue' has params 'null' '{2}' '{3}' '{4}' '{5}'");
        args[0] = "Foobar";
        ce = Utils.makeCE("unknownMessageKeyValue", args, null);
        assertEquals(ce.getMessage(),"ConfigurationException references unknown message key 'unknownMessageKeyValue' has params 'Foobar' '{2}' '{3}' '{4}' '{5}'");
    }

    public UtilsTest(String testName) {
        super(testName);
    }
    
    /** Test of getFQNKey method, of class org.netbeans.modules.j2ee.sun.share.configbean.Utils. 
    public void testGetFQNKey() {
        System.out.println("testGetFQNKey");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /** Test of getUriFromKey method, of class org.netbeans.modules.j2ee.sun.share.configbean.Utils. 
    public void testGetUriFromKey() {
        System.out.println("testGetUriFromKey");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /** Test of getFilenameFromKey method, of class org.netbeans.modules.j2ee.sun.share.configbean.Utils. 
    public void testGetFilenameFromKey() {
        System.out.println("testGetFilenameFromKey");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    */
}
