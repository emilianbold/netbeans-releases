/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.jellytools;

/*
 * JellyTestCase.java
 *
 * Created on June 26, 2002, 4:08 PM
 */

import java.awt.Component;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import java.io.File;
import java.io.PrintStream;
import javax.swing.JDialog;

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
    /** Wait 1000 ms before every test case */
    public boolean waitNoEvent = Boolean.valueOf(System.getProperty("jelly.wait.no.event", "true")).booleanValue();

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public JellyTestCase(String testName) {
        super(testName);
    }
    
    /** Inits environment before test case is executed. It can be overridden
     * in sub class but it is recommended to call super.initEnvironment() at
     * the beginning.
     * <br>
     * Default initialization: output messages from jemmy are redirected
     * to jemmy.log file in workdir; jemmy timeouts are loaded from 
     * org.netbeans.jellytools.timeouts and if system property jelly.timeouts_resource
     * or jelly.timeouts_file are set, timeouts are loaded from specified
     * resource/file;
     */
    protected void initEnvironment() {
        // redirect log messages from jemmy to jemmy.log file in workdir
        PrintStream jemmyLog = getLog("jemmy.log");
        JemmyProperties.setCurrentOutput(new TestOut(System.in, jemmyLog, jemmyLog));
        // load timeouts
        String timeoutsResource = System.getProperty("jelly.timeouts_resource");
        String timeoutsFile = System.getProperty("jelly.timeouts_file");
        try {
            JemmyProperties.getCurrentTimeouts().load(getClass().getClassLoader().
                         getResourceAsStream("org/netbeans/jellytools/timeouts"));
            if(timeoutsResource != null && !"".equals(timeoutsResource)) {
                JemmyProperties.getCurrentTimeouts().load(
                    getClass().getClassLoader().getResourceAsStream(timeoutsResource));
            } else if(timeoutsFile != null && !"".equals(timeoutsFile)) {
                JemmyProperties.getCurrentTimeouts().load(timeoutsFile);
            }
        } catch (Exception e) {
            throw new JemmyException("Initialization of timeouts failed.", e);
        }
    }
    
    /** Overriden method from JUnit framework execution to perform conditional
     * screen shot and conversion from TimeoutExpiredException to AssertionFailedError. <br>
     * Waits a second before test execution.
     * @throws Throwable Throwable
     */
    public void runBare() throws Throwable {
        initEnvironment();
        // workaround for JDK bug 4924516 (see below)
        Toolkit.getDefaultToolkit().addAWTEventListener(distributingHierarchyListener, 
                                                        HierarchyEvent.HIERARCHY_EVENT_MASK);
        // wait 
        if(waitNoEvent) {
            new EventTool().waitNoEvent(1000);
        }
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
        } finally {
            // workaround for JDK bug 4924516 (see below)
            Toolkit.getDefaultToolkit().removeAWTEventListener(distributingHierarchyListener);
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
    
    
    /*
     * methods for managing failures of group of dependent tests
     * Usage: each method involved in a group must start with 
     * startTest() call and when finished it must perform endTest().
     * To clear the test status (new group of tests does not depend on 
     * previous tests), method must call clearTestStatus() prior to startTest()
     * Example:
     * public void myTest() {
     *     startTest();
     *      // do my stuff
     *     endTest();
     * }
     */
    
    /** private variable for holding state whether test was finished
     */
    private static boolean testStatus = true;
    
    /** Checks whether previus test finished correctly and
     *  sets test status to 'not finished' state
     *
     */
    protected void startTest() {
        if (!testStatus) {
            fail("Depending on previous test, but it failed");
        }
        testStatus = false;
    }
    
    /** Sets the test status to 'finished' state (test passed)
     */
    protected void endTest() {
        testStatus = true;
    }
    
    /** Clears test status (used when test does not depend on previous test)
     */
    protected void clearTestStatus() {
        testStatus = true;
    }
    
    /* Workaround for JDK bug http://developer.java.sun.com/developer/bugParade/bugs/4924516.html.
     * Also see issue http://www.netbeans.org/issues/show_bug.cgi?id=32466.
     * ------------------------------------------------------------------------------------------
     * It can be removed when it is fixed (probably in JDK1.5.0). The following
     * listener is added to Toolkit at runBare() method and removed when it finishes. 
     * It distributes HierarchyEvent to all listening components and its subcomponents.
     */
    private static final DistributingHierarchyListener 
                distributingHierarchyListener = new DistributingHierarchyListener();
    
    private static class DistributingHierarchyListener implements AWTEventListener {
        
        public DistributingHierarchyListener() {
        }
        
        public void eventDispatched(java.awt.AWTEvent aWTEvent) {
            HierarchyEvent hevt = null;
            if (aWTEvent instanceof HierarchyEvent) {
                hevt = (HierarchyEvent) aWTEvent;
            }
            if (hevt != null && ((HierarchyEvent.SHOWING_CHANGED & hevt.getChangeFlags()) != 0)) {
                distributeShowingEvent(hevt.getComponent(), hevt);
            }
        }
        
        private static void distributeShowingEvent(Component c, HierarchyEvent hevt) {
            //HierarchyListener[] hierarchyListeners = c.getHierarchyListeners();
            // Need to use component.getListeners because it is not synchronized
            // and it not cause deadlock
            HierarchyListener[] hierarchyListeners = (HierarchyListener[])(c.getListeners(HierarchyListener.class));
            if (hierarchyListeners != null) {
                for (int i = 0; i < hierarchyListeners.length; i++) {
                    hierarchyListeners[i].hierarchyChanged(hevt);
                }
            }
            if (c instanceof Container) {
                Container cont = (Container) c;
                int n = cont.getComponentCount();
                for (int i = 0; i < n; i++) {
                    distributeShowingEvent(cont.getComponent(i), hevt);
                }
            }
        }
    }
}
