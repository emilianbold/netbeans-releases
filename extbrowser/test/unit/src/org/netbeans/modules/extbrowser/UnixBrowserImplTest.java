/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 */                

package org.netbeans.modules.extbrowser;
 
import junit.framework.*;
import org.netbeans.junit.*;
import java.awt.*;
import java.beans.*;
import java.io.*;
import java.net.*;
import java.util.ResourceBundle;
import javax.swing.*;
import org.openide.*;
import org.openide.awt.HtmlBrowser;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.options.SystemOption;
import org.openide.util.RequestProcessor;
         
/**
 *
 * @author rk109395
 */
public class UnixBrowserImplTest extends NbTestCase {

    public UnixBrowserImplTest (java.lang.String testName) {
        super(testName);
    }        
        
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /** Test of backward method, of class org.netbeans.modules.extbrowser.UnixBrowserImpl. */
    public void testBackward () {
        if (!org.openide.util.Utilities.isUnix ())
            return;
        testObject.backward ();
    }
    
    /** Test of forward method, of class org.netbeans.modules.extbrowser.UnixBrowserImpl. */
    public void testForward () {
        if (!org.openide.util.Utilities.isUnix ())
            return;
        testObject.forward ();
    }
    
    /** Test of isBackward method, of class org.netbeans.modules.extbrowser.UnixBrowserImpl. */
    public void testIsBackward () {
        if (!org.openide.util.Utilities.isUnix ())
            return;
        testObject.isBackward ();
    }
    
    /** Test of isForward method, of class org.netbeans.modules.extbrowser.UnixBrowserImpl. */
    public void testIsForward () {
        if (!org.openide.util.Utilities.isUnix ())
            return;
        testObject.isForward ();
    }
    
    /** Test of isHistory method, of class org.netbeans.modules.extbrowser.UnixBrowserImpl. */
    public void testIsHistory () {
        if (!org.openide.util.Utilities.isUnix ())
            return;
        if (testObject.isHistory ())
            fail ("NbDdeBrowserImpl.isHistory retunred true. It should return false.");
    }
    
    /** Test of reloadDocument method, of class org.netbeans.modules.extbrowser.UnixBrowserImpl. */
    public void testReloadDocument () {
        if (!org.openide.util.Utilities.isUnix ())
            return;
        testObject.reloadDocument ();
    }
    
    /** Test of setURL method, of class org.netbeans.modules.extbrowser.UnixBrowserImpl. */
    public void testSetURL () throws java.net.MalformedURLException {
        if (!org.openide.util.Utilities.isUnix ())
            return;
        testObject.setURL (new java.net.URL ("http://www.netbeans.org/"));
    }
    
    /** Test of showHistory method, of class org.netbeans.modules.extbrowser.UnixBrowserImpl. */
    public void testShowHistory () {
        if (!org.openide.util.Utilities.isUnix ())
            return;
        testObject.showHistory ();
    }
    
    /** Test of stopLoading method, of class org.netbeans.modules.extbrowser.UnixBrowserImpl. */
    public void testStopLoading () {
        if (!org.openide.util.Utilities.isUnix ())
            return;
        testObject.stopLoading ();
    }
    
    public static Test suite () {
        TestSuite suite = new NbTestSuite (UnixBrowserImplTest.class);
        
        return suite;
    }
    
    // Add test methods here, they have to start with 'test' name.
    // for example: 
    // public void testHello() {}
    protected HtmlBrowser.Impl testObject;
    
    protected void setUp () {
        if (org.openide.util.Utilities.isUnix ())
            testObject = new ExtWebBrowser ().createHtmlBrowserImpl ();
    }

}
