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
 * NbDdeBrowserImplTest.java
 * NetBeans JUnit based test
 *
 * Created on November 2, 2001, 10:42 AM
 */                

package org.netbeans.modules.extbrowser;
 
import junit.framework.*;
import org.netbeans.junit.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.net.*;
import javax.swing.*;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import org.openide.NotifyDescriptor;
import org.openide.ErrorManager;
import org.openide.awt.HtmlBrowser;
import org.openide.execution.NbProcessDescriptor;
import org.openide.options.SystemOption;
import org.openide.util.SharedClassObject;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;
         
/**
 *
 * @author rk109395
 */
public class NbDdeBrowserImplTest extends NbTestCase {

    public NbDdeBrowserImplTest (java.lang.String testName) {
        super(testName);
    }        
        
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    private NbDdeBrowserImpl getDDEBrowserImpl() {
        return (NbDdeBrowserImpl)((DelegatingWebBrowserImpl)testObject).getImplementation();
    }
    
    /** Test of getBrowserPath method, of class org.netbeans.modules.extbrowser.NbDdeBrowserImpl. */
    public void testGetBrowserPath () throws NbBrowserException {
        if (!org.openide.util.Utilities.isWindows ())
            return;
        getDDEBrowserImpl().getBrowserPath ("IEXPLORE");
    }
    
    /** Test of getDefaultOpenCommand method, of class org.netbeans.modules.extbrowser.NbDdeBrowserImpl. */
    public void testGetDefaultOpenCommand () throws NbBrowserException {
        if (!org.openide.util.Utilities.isWindows ())
            return;
        getDDEBrowserImpl().getDefaultOpenCommand ();
    }
    
    /** Test of backward method, of class org.netbeans.modules.extbrowser.NbDdeBrowserImpl. */
    public void testBackward () {
        if (!org.openide.util.Utilities.isWindows ())
            return;
        
        testObject.backward ();
    }
    
    /** Test of forward method, of class org.netbeans.modules.extbrowser.NbDdeBrowserImpl. */
    public void testForward () {
        if (!org.openide.util.Utilities.isWindows ())
            return;
        testObject.forward ();
    }
    
    /** Test of isBackward method, of class org.netbeans.modules.extbrowser.NbDdeBrowserImpl. */
    public void testIsBackward () {
        if (!org.openide.util.Utilities.isWindows ())
            return;
        testObject.isBackward ();
    }
    
    /** Test of isForward method, of class org.netbeans.modules.extbrowser.NbDdeBrowserImpl. */
    public void testIsForward () {
        if (!org.openide.util.Utilities.isWindows ())
            return;
        testObject.isForward ();
    }
    
    /** Test of isHistory method, of class org.netbeans.modules.extbrowser.NbDdeBrowserImpl. */
    public void testIsHistory () {
        if (!org.openide.util.Utilities.isWindows ())
            return;
        if (testObject.isHistory ())
            fail ("NbDdeBrowserImpl.isHistory retunred true. It should return false.");
    }
    
    /** Test of reloadDocument method, of class org.netbeans.modules.extbrowser.NbDdeBrowserImpl. */
    public void testReloadDocument () {
        if (!org.openide.util.Utilities.isWindows ())
            return;
        testObject.reloadDocument ();
    }
    
    /** Test of setURL method, of class org.netbeans.modules.extbrowser.NbDdeBrowserImpl. */
    public void testSetURL () throws java.net.MalformedURLException {
        if (!org.openide.util.Utilities.isWindows ())
            return;
        testObject.setURL (new java.net.URL ("http://www.netbeans.org/"));
    }
    
    /** Test of showHistory method, of class org.netbeans.modules.extbrowser.NbDdeBrowserImpl. */
    public void testShowHistory () {
        if (!org.openide.util.Utilities.isWindows ())
            return;
        testObject.showHistory ();
    }
    
    /** Test of stopLoading method, of class org.netbeans.modules.extbrowser.NbDdeBrowserImpl. */
    public void testStopLoading () {
        if (!org.openide.util.Utilities.isWindows ())
            return;
        testObject.stopLoading ();
    }
    
    public static Test suite () {
        TestSuite suite = new NbTestSuite (NbDdeBrowserImplTest.class);
        
        return suite;
    }
    
    // Add test methods here, they have to start with 'test' name.
    // for example: 
    // public void testHello() {}
    protected ExtBrowserImpl testObject;
    
    protected void setUp () {
        if (org.openide.util.Utilities.isWindows ())
            testObject = (ExtBrowserImpl)new ExtWebBrowser ().createHtmlBrowserImpl ();
    }

}
