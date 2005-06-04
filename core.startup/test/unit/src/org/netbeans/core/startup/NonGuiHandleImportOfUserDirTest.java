/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.core.startup;

import java.io.File;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import junit.framework.*;
import org.netbeans.core.startup.CLIOptions;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.*;

/** Tests the behaviour of the import user dir "api".
 */
public class NonGuiHandleImportOfUserDirTest extends NbTestCase {
    private static NonGuiHandleImportOfUserDirTest instance;
    
    private File user;
    private boolean updaterInvoked;
    private Throwable toThrow;
    
    
    public static void main(java.lang.String[] args) throws Throwable {
        if (instance != null) {
            // ok this is invoked from the test by the core-launcher
            instance.nowDoTheInstall ();
            return;
        } else {
            // initial start
            junit.textui.TestRunner.run(new junit.framework.TestSuite (NonGuiHandleImportOfUserDirTest.class));
        }
    }
    public NonGuiHandleImportOfUserDirTest (String name) {
        super(name);
    }

    protected void setUp () throws Exception {
        clearWorkDir ();
        CLIOptions.clearForTests ();
        
        File home = new File (getWorkDir (), "home");
        user = new File (getWorkDir (), "user");
        
        assertTrue ("Home dir created", home.mkdirs ());
        assertTrue ("User dir created", user.mkdirs ());
        
        System.setProperty ("netbeans.home", home.toString ());
        System.setProperty ("netbeans.user", user.toString ());
        
        System.setProperty ("netbeans.importclass", NonGuiHandleImportOfUserDirTest.class.getName ());
        
        instance = this;
    }
    
    protected void tearDown () throws Exception {
        instance = null;
    }
    
    
    private void nowDoTheInstall () throws Throwable {
        assertTrue ("Called from AWT thread", javax.swing.SwingUtilities.isEventDispatchThread ());
        if (toThrow != null) {
            Throwable t = toThrow;
            toThrow = null;
            throw t;
        }
        
        updaterInvoked = true;
    }
    
    public void testIfTheUserDirIsEmptyTheUpdaterIsInvoked () {
        assertTrue ("Ok, returns without problems", Main.handleImportOfUserDir ());
        assertTrue ("the main method invoked", updaterInvoked);
        
        toThrow = new RuntimeException ();

        assertTrue ("The install is not called anymore 1", Main.handleImportOfUserDir ());
        assertTrue ("The install is not called anymore 2", Main.handleImportOfUserDir ());
        assertTrue ("The install is not called anymore 3", Main.handleImportOfUserDir ());
        assertTrue ("The install is not called anymore 4", Main.handleImportOfUserDir ());
    }

    public void testIfInvokedAndThrowsExceptionTheExecutionStops () {
        toThrow = new RuntimeException ();
        
        assertFalse ("Says no as exception was thrown", Main.handleImportOfUserDir ());
        assertNull ("Justs to be sure the exception was cleared", toThrow);
    }
    
    public void testIfThrowsUserCancelExThenUpdateIsFinished () {
        toThrow = new org.openide.util.UserCancelException ();
        
        assertTrue ("Says yes as user canceled the import", Main.handleImportOfUserDir ());
        assertNull ("Justs to be sure the exception was cleared", toThrow);
        
        assertTrue ("The install is not called anymore 1", Main.handleImportOfUserDir ());
    }
}
