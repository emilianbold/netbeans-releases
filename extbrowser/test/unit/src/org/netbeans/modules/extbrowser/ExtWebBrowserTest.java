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
 * ExtWebBrowserTest.java
 * NetBeans JUnit based test
 *
 * Created on November 2, 2001, 10:42 AM
 */

package org.netbeans.modules.extbrowser;

import junit.framework.*;
import org.netbeans.junit.*;
import java.beans.*;
import org.openide.ErrorManager;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.NbBundle;
import org.openide.awt.HtmlBrowser;

/**
 *
 * @author rk109395
 */
public class ExtWebBrowserTest extends NbTestCase {

    public ExtWebBrowserTest (java.lang.String testName) {
        super(testName);
    }        
        
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /** Test of getDescription method, of class org.netbeans.modules.extbrowser.ExtWebBrowser. */
/*    public void testGetName () {
        if (testObject.getName () == null)
            fail ("ExtWebBrowser.getName () returns <null>.");
    }
 */
    
    /** Test of getBrowserExecutable method, of class org.netbeans.modules.extbrowser.ExtWebBrowser. */
    public void testGetBrowserExecutable () {
        if (testObject.getBrowserExecutable () == null)
            fail ("ExtWebBrowser.getBrowserExecutable () returns <null>.");
    }
    
    /** Test of setBrowserExecutable method, of class org.netbeans.modules.extbrowser.ExtWebBrowser. */
    public void testSetBrowserExecutable () {
        testObject.setBrowserExecutable (new NbProcessDescriptor ("netscape", ""));
    }
    
    /** Test of isStartWhenNotRunning method, of class org.netbeans.modules.extbrowser.ExtWebBrowser. */
    /*public void testIsStartWhenNotRunning () {
        testObject.isStartWhenNotRunning ();
    } */
    
    /** Test of setStartWhenNotRunning method, of class org.netbeans.modules.extbrowser.ExtWebBrowser. */
    /*public void testSetStartWhenNotRunning () {
        testObject.setStartWhenNotRunning (true);
    } */
    
    /** Test of defaultBrowserExecutable method, of class org.netbeans.modules.extbrowser.ExtWebBrowser. */
    public void testDefaultBrowserExecutable () {
        if (testObject.defaultBrowserExecutable () == null)
            fail ("ExtWebBrowser.defaultBrowserExecutable () failed.");
    }
    
    /** Test of createHtmlBrowserImpl method, of class org.netbeans.modules.extbrowser.ExtWebBrowser. */
    public void testCreateHtmlBrowserImpl () {
        testObject.createHtmlBrowserImpl ();
    }
    
    /** Test of addPropertyChangeListener method, of class org.netbeans.modules.extbrowser.ExtWebBrowser. */
    public void testAddPropertyChangeListener () {
        testObject.addPropertyChangeListener (new PropertyChangeListener () {
            public void propertyChange (PropertyChangeEvent evt) {}
        });
    }
    
    /** Test of removePropertyChangeListener method, of class org.netbeans.modules.extbrowser.ExtWebBrowser. */
    public void testRemovePropertyChangeListener () {
        testObject.removePropertyChangeListener (new PropertyChangeListener () {
            public void propertyChange (PropertyChangeEvent evt) {}
        });
    }
    
    /** Test of getDDEServer method, of class org.netbeans.modules.extbrowser.ExtWebBrowser. */
    public void testGetDDEServer () {
        testObject.getDDEServer ();
    }
    
    /** Test of setDDEServer method, of class org.netbeans.modules.extbrowser.ExtWebBrowser. */
    public void testSetDDEServer () {
        testObject.setDDEServer ("NETSCAPE");
    }
    
    public static Test suite () {
        TestSuite suite = new NbTestSuite (ExtWebBrowserTest.class);
        
        return suite;
    }
    
    // Add test methods here, they have to start with 'test' name.
    // for example: 
    // public void testHello() {}
    protected ExtWebBrowser testObject;
    
    protected void setUp () {
        testObject = new ExtWebBrowser ();
    }
    
}
