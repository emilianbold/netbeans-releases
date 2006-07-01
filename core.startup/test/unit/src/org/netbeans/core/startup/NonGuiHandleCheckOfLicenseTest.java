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

package org.netbeans.core.startup;

import java.io.File;
import org.netbeans.junit.NbTestCase;

/** Tests the behavior of the license check "api".
 */
public class NonGuiHandleCheckOfLicenseTest extends NbTestCase {
    private static NonGuiHandleCheckOfLicenseTest instance;

    private File user;
    private File userVar;
    private boolean updaterInvoked;
    private Throwable toThrow;

    public static void showLicensePanel () throws Throwable {
        if (instance != null) {
            // ok this is invoked from the test by the core-launcher
            instance.nowDoTheInstall ();
            return;
        } else {
            // initial start
            junit.textui.TestRunner.run(new junit.framework.TestSuite (NonGuiHandleCheckOfLicenseTest.class));
        }
    }

    public NonGuiHandleCheckOfLicenseTest (String name) {
        super(name);
    }

    protected void setUp () throws Exception {
        clearWorkDir ();
        CLIOptions.clearForTests ();
        
        File home = new File (getWorkDir (), "nb/home");
        user = new File (getWorkDir (), "user");
        userVar = new File (user,"var");
        
        assertTrue ("Home dir created", home.mkdirs ());
        assertTrue ("User dir created", user.mkdirs ());
        
        System.setProperty ("netbeans.home", home.toString ());
        System.setProperty ("netbeans.user", user.toString ());
        
        System.setProperty ("netbeans.accept_license_class", NonGuiHandleCheckOfLicenseTest.class.getName ());
        
        File f = new File(userVar,"license_accepted");
        if (f.exists()) {
            f.delete();
        }
        
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
    
    /** Test if check is invoked when there is not file "var/license_accepted" */
    public void testIfTheUserDirIsEmptyTheLicenseCheckIsInvoked () {
        assertTrue ("Ok, returns without problems", Main.handleLicenseCheck ());
        assertTrue ("the main method invoked", updaterInvoked);
        
        toThrow = new RuntimeException ();
        
        //File "var/license_accepted" is created during first call in user dir
        //then license check is not invoked anymore
        assertTrue ("The check is not called anymore 1", Main.handleLicenseCheck ());
        assertTrue ("The check is not called anymore 2", Main.handleLicenseCheck ());
        assertTrue ("The check is not called anymore 3", Main.handleLicenseCheck ());
        assertTrue ("The check is not called anymore 4", Main.handleLicenseCheck ());
        
        File f = new File(userVar,"license_accepted");
        if (f.exists()) {
            f.delete();
        }
    }
    
    public void testIfInvokedAndThrowsExceptionTheExecutionStops () {
        toThrow = new RuntimeException();
        
        assertFalse ("Says no as exception was thrown", Main.handleLicenseCheck());
        assertNull ("Justs to be sure the exception was cleared", toThrow);
    }
    
    public void testIfThrowsUserCancelExThenLicenseCheckIsCalledAgain () {
        toThrow = new org.openide.util.UserCancelException();
        assertFalse("Says no as user did not accept the license", Main.handleLicenseCheck());
        assertNull("Justs to be sure the exception was cleared", toThrow);
        
        toThrow = new org.openide.util.UserCancelException();
        assertFalse("Says no as user did not accept the license", Main.handleLicenseCheck());
        assertNull("Justs to be sure the exception was cleared", toThrow);
    }
    
}
