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

package org.netbeans.jellytools;

/*
 * JellyTestCase.java
 *
 * Created on June 26, 2002, 4:08 PM
 */

import java.awt.Component;
import java.awt.Window;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import javax.swing.JDialog;

import junit.framework.*;
import org.netbeans.junit.*;

import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.util.PNGEncoder;
import org.netbeans.jemmy.util.Dumper;

/** JUnit test case with implemented Jemmy/Jelly2 support stuff
 *
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 1.0
 */
public class JellyTestCase extends NbTestCase {
    
    /** screen capture feature in case of failure is enabled by default
     */
    public boolean captureScreen = Boolean.valueOf(System.getProperty("jemmy.screen.capture", "true")).booleanValue();
    
    /** screen XML dump feature in case of failure is disabled by default
     */
    public boolean dumpScreen = Boolean.getBoolean("jemmy.screen.xmldump");
    
    /** closing all modal dialogs after each test case is disabled by default
     */
    public boolean closeAllModal = Boolean.valueOf(System.getProperty("jelly.close.modal", "true")).booleanValue();
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public JellyTestCase(String testName) {
        super(testName);
    }
    
    /** Overriden method from JUnit framework execution to perform conditional
     * screen shot and conversion from TimeoutExpiredException to AssertionFailedError. <br>
     * Redirects output and waits a second before test execution.
     * @throws Throwable Throwable
     */
    public void runBare() throws Throwable {
        PrintStream jemmyLog = getLog("jemmy.log");
        JemmyProperties.setCurrentOutput(new TestOut(System.in, jemmyLog, jemmyLog));
        new EventTool().waitNoEvent(1000);
        try {
            super.runBare();
        } catch (ThreadDeath td) {
            // ThreadDead must be re-throwed immediately
            throw td;
        } catch (Throwable th) {
            // suite is notified about test failure so it can do some debug actions
            try {
                failNotify(th);
            } catch (Exception e3) {}
            // screen capture is performed when test fails and in dependency on system property
            if (captureScreen) {
                try {
                    PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+File.separator+"screen.png");
                } catch (Exception e1) {}
            }
            // XML dump is performed when test fails and in dependency on system property
            if (dumpScreen) {
                try {
                    Dumper.dumpAll(getWorkDir().getAbsolutePath()+File.separator+"screen.xml");
                } catch (Exception e2) {}
            }
            // closes all modal dialogs in dependency on systems property
            if (closeAllModal) try {
                closeAllModal();
            } catch (Exception e) {}
            if (th instanceof JemmyException) {
                // all instancies of JemmyException are re-throwed as AssertionError (test failed)
                throw new AssertionFailedErrorException(th.getMessage(), th);
            } else {
                throw th;
            }
        }
    }
    
    /** Method called in case of fail or error just after screen shot and XML dumps. <br>
     * Override this method when you need to be notified about test failures or errors 
     * but avoid any exception to be throwed from this method.<br>
     * super.failNotify() does not need to be called because it is empty.
     * @param reason Throwable reason of current fail */
    protected void failNotify(Throwable reason) {
    }
    
    /** Closes all opened modal dialogs. Non-modal stay opened. */
    public static void closeAllModal() {
        JDialog dialog;
        ComponentChooser chooser = new ComponentChooser() {
            public boolean checkComponent(Component comp) {
                return(comp instanceof JDialog &&
                       comp.isShowing() &&
                       ((JDialog)comp).isModal());
            }
            public String getDescription() {
                return("Modal dialog");
            }
        };
        while((dialog = (JDialog)DialogWaiter.getDialog(chooser)) != null) {
            closeDialogs(findBottomDialog(dialog, chooser), chooser);
        }
    }

    private static JDialog findBottomDialog(JDialog dialog, ComponentChooser chooser) {
        Window owner = dialog.getOwner();
        if(chooser.checkComponent(owner)) {
            return(findBottomDialog((JDialog)owner, chooser));
        }
        return(dialog);
    }
    
    private static void closeDialogs(JDialog dialog, ComponentChooser chooser) {
        Window[] ownees = dialog.getOwnedWindows();
        for(int i = 0; i < ownees.length; i++) {
            if(chooser.checkComponent(ownees[i])) {
                closeDialogs((JDialog)ownees[i], chooser);
            }
        }
        new JDialogOperator(dialog).close();
    }
    
    /** Finishes test with status Fail
     * @param t Throwable reason of test failure
     */    
    public void fail(Throwable t) {
        t.printStackTrace(getLog());
        throw new AssertionFailedErrorException(t);
    }
    
}
