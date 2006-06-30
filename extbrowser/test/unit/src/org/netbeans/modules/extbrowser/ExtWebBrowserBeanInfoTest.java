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
 *
 * ExtWebBrowserBeanInfoTest.java
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
public class ExtWebBrowserBeanInfoTest extends NbTestCase {

    public ExtWebBrowserBeanInfoTest (java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /** Test of getBeanDescriptor method, of class org.netbeans.modules.extbrowser.ExtWebBrowserBeanInfo. */
    public void testGetBeanDescriptor () {
        if (testObject.getBeanDescriptor () == null)
            fail ("ExtWebBrowserBeanInfo.getBeanDescriptor () returned <null>.");
    }
    
    /** Test of getPropertyDescriptors method, of class org.netbeans.modules.extbrowser.ExtWebBrowserBeanInfo. */
    public void testGetPropertyDescriptors () {
        if (testObject.getPropertyDescriptors () == null)
            fail ("ExtWebBrowserBeanInfo.getPropertyDescriptors () returned <null>.");
    }
    
    /** Test of getIcon method, of class org.netbeans.modules.extbrowser.ExtWebBrowserBeanInfo. */
    public void testGetIcon () {
        if (testObject.getIcon (BeanInfo.ICON_COLOR_32x32) == null)
            fail ("ExtWebBrowserBeanInfo.getIcon (BeanInfo.ICON_COLOR_32x32) returned <null>.");
    }
    
    public static Test suite () {
        TestSuite suite = new NbTestSuite (ExtWebBrowserBeanInfoTest.class);
        
        return suite;
    }
    
    // Add test methods here, they have to start with 'test' name.
    // for example: 
    // public void testHello() {}
    protected BeanInfo testObject;
    
    protected void setUp () {
        testObject = new ExtWebBrowserBeanInfo ();
    }

}
