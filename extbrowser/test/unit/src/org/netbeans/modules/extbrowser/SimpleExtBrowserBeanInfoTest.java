/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * SimpleExtBrowserBeanInfoTest.java
 * NetBeans JUnit based test
 *
 * Created on November 2, 2001, 10:42 AM
 */                

package org.netbeans.modules.extbrowser;
 
import junit.framework.*;
import org.netbeans.junit.*;
import java.awt.Image;
import java.beans.*;
import org.openide.util.NbBundle;
         
/**
 *
 * @author rk109395
 */
public class SimpleExtBrowserBeanInfoTest extends NbTestCase {

    public SimpleExtBrowserBeanInfoTest (java.lang.String testName) {
        super(testName);
    }        
        
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /** Test of getBeanDescriptor method, of class org.netbeans.modules.extbrowser.SimpleExtBrowserBeanInfo. */
    public void testGetBeanDescriptor () {
        if (testObject.getBeanDescriptor () == null)
            fail ("SimpleExtBrowserBeanInfo.getBeanDescriptor () returned <null>.");
    }
    
    /** Test of getPropertyDescriptors method, of class org.netbeans.modules.extbrowser.SimpleExtBrowserBeanInfo. */
    public void testGetPropertyDescriptors () {
        if (testObject.getPropertyDescriptors () == null)
            fail ("SimpleExtBrowserBeanInfo.getPropertyDescriptors () returned <null>.");
    }
    
    /** Test of getIcon method, of class org.netbeans.modules.extbrowser.SimpleExtBrowserBeanInfo. */
    public void testGetIcon () {
        if (testObject.getIcon (BeanInfo.ICON_COLOR_32x32) == null)
            fail ("SimpleExtBrowserBeanInfo.getIcon (BeanInfo.ICON_COLOR_32x32) returned <null>.");
    }
    
    public static Test suite () {
        TestSuite suite = new NbTestSuite (SimpleExtBrowserBeanInfoTest.class);
        
        return suite;
    }
    
    // Add test methods here, they have to start with 'test' name.
    // for example: 
    // public void testHello() {}
    protected BeanInfo testObject;
    
    protected void setUp () {
        testObject = new SimpleExtBrowserBeanInfo ();
    }

}
