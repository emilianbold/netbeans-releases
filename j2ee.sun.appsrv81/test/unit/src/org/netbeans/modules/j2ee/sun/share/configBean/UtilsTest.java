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
 * UtilsTest.java
 * JUnit based test
 *
 * Created on March 1, 2004, 12:33 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import java.net.URLClassLoader;
import junit.framework.*;
import org.netbeans.modules.schema2beans.BaseBean;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

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

    public UtilsTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(UtilsTest.class);
        return suite;
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
