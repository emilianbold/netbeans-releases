/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.xtest.plugin.ide;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.TopSecurityManager;
import org.netbeans.xtest.util.JNIKill;
import org.netbeans.xtest.util.PNGEncoder;

/**
 * Main part of XTest starter. Must not use anything outside lib/*.jar and lib/ext/*.jar.
 * @author Jan Chalupa, Jesse Glick
 */
public class Main extends Object {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    
    // ide running flag file
    private static File ideRunning = null;

    // netbeans pid system property 
    private static final String IDE_PID_SYSTEM_PROPERTY = "netbeans.pid";
    
    // flag whether IDE was interrupted 
    private static boolean ideInterrupted = false;

    
    private static void captureIDEScreen(final String fileName) {
        Thread captureThread = new Thread(new Runnable() {
            public void run() {
                try {
                    System.out.println("capturing screenshot:"+fileName);
                    String userdirName = System.getProperty("netbeans.user");
                    // capture the screenshot
                    PNGEncoder.captureScreenToIdeUserdir(userdirName, fileName);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Exception thrown when capturing IDE screenshot", e);
                }
            }
        });
        captureThread.start();
        try {
            captureThread.join(60000L);  // wait 1 minute at the most
        } catch (InterruptedException iex) {
            LOGGER.log(Level.WARNING, "Exception thrown when capturing IDE screenshot", iex);
        } finally {
        }
        if(captureThread.isAlive()) {
            captureThread.interrupt();
        }
    }

    
    private static void prepareModuleLoaderClassPath() {
        String moduleLoaderName = System.getProperty("xtest.ide.use.classloader","");
        if (!moduleLoaderName.equals("")) {
            if (!moduleLoaderName.equals("openide")) { // openide is handler in the executor build script	
                System.out.println("Using module "+moduleLoaderName+" classloader to load tests");
                String testbagClassPath = System.getProperty("tbag.classpath");
                if (testbagClassPath != null) {
                    // set appropriate property
                    String patchesProperty = "netbeans.patches."+moduleLoaderName;
                    System.out.println("Setting system property "+patchesProperty+" to "+testbagClassPath);
                    System.setProperty(patchesProperty, testbagClassPath);
                } else {
                    System.out.println("TestBag classpath (tbag.classpath) property not defined - there is nothing to load");
                }
            } else {
                System.out.println("Using openide classlaoder to load the tests");
            }
        } else {
            System.out.println("Using system classloader to load the tests");
        }
    }

    
    /** Starts the IDE.
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        System.out.println("!!!!! testlist is "+System.getProperty("testlist"));

        // use the console logging
        System.setProperty ("netbeans.logger.console", "true");

        final String blacklist = System.getProperty("xtest.ide.blacklist");
        if (blacklist != null) {
            try {
                Logger classLogger = Logger.getLogger("org.netbeans.ProxyClassLoader");
                classLogger.addHandler(BlacklistedClassesHandler.getBlacklistedClassesHandler(blacklist));
                classLogger.setLevel(Level.ALL);
                classLogger.setUseParentHandlers(false);
                System.out.println("BlacklistedClassesHandler handler added");
            } catch (Exception ex) {
                System.out.println("Can't initialize BlacklistedClassesHandler due to the following exception:");
                ex.printStackTrace();
            }
        }

        // create the IDE flag file
        String workdir = System.getProperty("xtest.workdir");
        if (workdir!=null) {
            // create flag file indicating running tests
            ideRunning = new File(workdir,"ide.flag");
            File idePID = new File(workdir,"ide.pid");
            PrintWriter writer = null;
            try {
                ideRunning.createNewFile();
                idePID.createNewFile();
                writer = new PrintWriter(new FileOutputStream(idePID));
                // get my pid
                JNIKill kill = new JNIKill();
                if (kill.startDumpThread()) System.out.println("IDE dump thread succesfully started.");
                long myPID = kill.getMyPID();
                // store it as a system property
                System.setProperty(IDE_PID_SYSTEM_PROPERTY, Long.toString(myPID));
                // write it out to a file 
                System.out.println("IDE is running under PID:"+myPID);
                writer.println(myPID);
            } catch (IOException ioe) {
                System.out.println("cannot create flag file:"+ideRunning);
                ideRunning = null;
            } catch (Throwable e) {
                System.out.println("There was a problem: "+e);
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        } else {
            System.out.println("cannot get xtest.workdir property - it has to be set to a valid working directory");
        }

        // prepare module's classpath - for tests which are loaded by modules' classlaoders
        prepareModuleLoaderClassPath();

        // do the expected stuff
        try {
            org.netbeans.core.startup.Main.main(args);
        }
        catch (Exception e) {
            e.printStackTrace();
            exit();
        }

        // some threads may be still active, wait for the event queue to become quiet
        Thread initThread = new Thread(new Runnable() {
            public void run() {
                try {
                    new QueueEmpty().waitEventQueueEmpty(2000);
                }
                catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Exception while waiting for empty event queue." , ex);
                }
            }
        });
        // workaround for JDK bug 4924516 (see below)
        Toolkit.getDefaultToolkit().addAWTEventListener(distributingHierarchyListener,
                HierarchyEvent.HIERARCHY_EVENT_MASK);
        // start the init thread
        initThread.start();
        try {
            initThread.join(60000L);  // wait 1 minute at the most
        }
        catch (InterruptedException iex) {
            // ignore it
        } finally {
            // workaround for JDK bug 4924516 (see below)
            Toolkit.getDefaultToolkit().removeAWTEventListener(distributingHierarchyListener);
        }
        if (initThread.isAlive()) {
            // time-out expired, event queue still busy -> interrupt the init thread
            LOGGER.info(new Date().toString() + ": EventQueue still busy, starting anyway.");
            initThread.interrupt();
        }
        // ok. The IDE should be up and fully initialized
        // let's run the test now
        try {
            // just aTest
            doTestPart();
        }
        catch (RuntimeException ex) {
            ex.printStackTrace();
        }
        catch (Error err) {
            err.printStackTrace();
        }
    }
    
    
    private static final String TEST_EXIT = "test.exit";
    private static final String TEST_TIMEOUT = "xtest.timeout";
    private static final String TEST_REUSE_IDE = "test.reuse.ide";
    private static final long DEFAULT_TIMEOUT = 2400000;
    
    // properties used for new project infrastructure
    private static final String IDE_CREATE_PROJECT = "xtest.ide.create.project";
    private static final String IDE_OPEN_PROJECT = "xtest.ide.open.project";
    private static final String IDE_OPEN_PROJECTS = "xtest.ide.open.projects";
    private static final String XTEST_USERDIR  = "xtest.userdir";

    private static MainWithProjectsInterface projectsHandle;

    /** Gets projects handle only when needed. It doesn't fail when used for
     * platform (http://www.netbeans.org/issues/show_bug.cgi?id=47928)
     */
    private static MainWithProjectsInterface getProjectsHandle() {
        if(projectsHandle == null) {
            try {
                projectsHandle = (MainWithProjectsInterface)new WithProjectsClassLoader().loadClass("org.netbeans.xtest.plugin.ide.MainWithProjects").newInstance();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
                return null;
            }
        }
        return projectsHandle;
    }

    private static void doTestPart() {
        final MainWithExecInterface handle;
        try {
            handle = (MainWithExecInterface)new WithExecClassLoader().loadClass("org.netbeans.xtest.plugin.ide.MainWithExec").newInstance();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            return;
        }

        long testTimeout;

        try {
            // default timeout is 30 minutes
            testTimeout = Long.parseLong(System.getProperty(TEST_TIMEOUT, "2400000"));
        }
        catch (NumberFormatException ex) {
            testTimeout = DEFAULT_TIMEOUT;
        }
        if (testTimeout <= 0) {
            testTimeout = DEFAULT_TIMEOUT;
        }

        Thread testThread = new Thread( new Runnable() {
            public void run() {
                try {

                    // setup the repository - should not be needed for tests loaded by system classloader

                    if (System.getProperty(TEST_REUSE_IDE, "false").equals("false")) {

                        // create an empty Java project
                        if (System.getProperty(IDE_CREATE_PROJECT,"false").equals("true")) {
                            getProjectsHandle().createProject(System.getProperty(XTEST_USERDIR));
                        }
                        // open project at specified location
                        if (!System.getProperty(IDE_OPEN_PROJECT,"").equals("")) {
                            getProjectsHandle().openProject(System.getProperty(IDE_OPEN_PROJECT));
                        }
                        // open multiple projects at specified location
                        if (!System.getProperty(IDE_OPEN_PROJECTS,"").equals("")) {
                            String pathToProjects = System.getProperty(IDE_OPEN_PROJECTS);
                            File dir = new File(pathToProjects);
                            File[] projects = dir.listFiles();
                            for(int k=0;k<projects.length;k++) {
                                if(projects[k].isDirectory()) {
                                    getProjectsHandle().openProject(projects[k].getAbsolutePath());
                                }
                            }
                        }
                    }
                    handle.run();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    LOGGER.log(Level.WARNING, e.getMessage(), e);
                }
            }
        });

        // start the test thread
        testThread.start();
        try {
            testThread.join(testTimeout);
        }
        catch (InterruptedException iex) {
            LOGGER.log(Level.WARNING, iex.getMessage(), iex);
        }
        if (testThread.isAlive()) {
            // time-out expired, test not finished -> interrupt
            LOGGER.info(new Date().toString() + ": time-out expired - interrupting! ***");
            // thread dump
            new JNIKill().dumpMe();
            // save the screen capture
            captureIDEScreen("screenshot-timeout.png");
            ideInterrupted = true;
            testThread.interrupt();
        }

        // we're leaving IDE 
        // delete the flag file (if ide was not interrupted)
        if (ideRunning!=null) {
            if (!ideInterrupted) {
                if (ideRunning.delete()==false) {
                    System.out.println("Cannot delete the flag file "+ideRunning);
                }
            }
        }

        // close IDE
        if (System.getProperty(TEST_EXIT, "false").equals("true")) {
            Thread exitThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        // kill all running tasks
                        handle.killPendingTasks();
                        // discard changes in all modified files
                        handle.discardChanges();
                    } catch (Exception e) {
                        System.out.println("Exception when killing tasks or discarding changes.");
                        e.printStackTrace();
                    }
                    // exit IDE
                    handle.exit ();
                }
            });
            // try to exit nicely first
            LOGGER.info(new Date().toString() + ": soft exit attempt.");
            exitThread.start();
            try {
                // wait 60 seconds for the IDE to exit
                exitThread.join(60000);
            }
            catch (InterruptedException iex) {
                LOGGER.log(Level.WARNING, iex.getMessage(), iex);
            }
            if (exitThread.isAlive()) {
                // IDE refuses to shutdown, terminate unconditionally
                LOGGER.info(new Date().toString() + ": hard exit attempt!!!.");
                // screen shot first
                captureIDEScreen("screenshot-hardexit.png");
                exitThread.interrupt();
                exit();
            }
        }
    }

    private static void exit() {
        try {
            Class param[] = new Class[1];
            param[0] = int.class;
            Class c = TopSecurityManager.class;
           Method m = c.getMethod("exit",param);
            Integer intparam[] = {new Integer(1)};
            LOGGER.info(new Date().toString() + ": using TopSecurityManager.exit(1) to exit IDE.");
            // exit
           m.invoke(null,(Object[])intparam);
        }
        catch (Exception e) {
            LOGGER.info(new Date().toString() + ": using System.exit(1) to exit IDE.");
            // exit
            System.exit(1);
        }
    }

    private static class QueueEmpty implements AWTEventListener {

        private long eventDelayTime = 100; // 100 millis
        private long lastEventTime;

        /** Creates a new QueueEmpty instance */
        public QueueEmpty() {
        }

        /** method called every time when AWT Event is dispatched
         * @param event event dispatched from AWT Event Queue
         */
        public void eventDispatched(AWTEvent awtEvent) {
            lastEventTime = System.currentTimeMillis();
        }

        /** constructor with user defined value
         * @param eventdelaytime maximum delay between two events of one redraw action
         */
        public synchronized void waitEventQueueEmpty(long eventDelayTime) throws InterruptedException {
            this.eventDelayTime = eventDelayTime;
            waitEventQueueEmpty();
        }

        /** Waits until the AWTEventQueue is empty for a specified interval
         */
        public synchronized void waitEventQueueEmpty() throws InterruptedException {
            // store current time as the start time
            long startTime = System.currentTimeMillis();
            // register itself as an AWTEventListener
            Toolkit.getDefaultToolkit().addAWTEventListener(this,
                    AWTEvent.ACTION_EVENT_MASK |
                    AWTEvent.ADJUSTMENT_EVENT_MASK |
                    AWTEvent.COMPONENT_EVENT_MASK |
                    AWTEvent.CONTAINER_EVENT_MASK |
                    AWTEvent.FOCUS_EVENT_MASK |
                    AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK |
                    AWTEvent.HIERARCHY_EVENT_MASK |
                    AWTEvent.INPUT_METHOD_EVENT_MASK |
                    AWTEvent.INVOCATION_EVENT_MASK |
                    AWTEvent.ITEM_EVENT_MASK |
                    AWTEvent.KEY_EVENT_MASK |
                    AWTEvent.MOUSE_EVENT_MASK |
                    AWTEvent.MOUSE_MOTION_EVENT_MASK |
                    AWTEvent.PAINT_EVENT_MASK |
                    AWTEvent.TEXT_EVENT_MASK |
                    AWTEvent.WINDOW_EVENT_MASK);

            // set last event time to the current time
            lastEventTime=System.currentTimeMillis();
            // get the thread to be put asleep
            Thread t = Thread.currentThread();
            // get current AWT Event Queue
            EventQueue queue=Toolkit.getDefaultToolkit().getSystemEventQueue();

            try {

                // while AWT Event Queue is not empty and timedelay from last event is shorter then eventdelaytime

                //wait till the queue is empty
                while ( queue.peekEvent() != null ) Thread.sleep(100);
                //test it - post my own task and wait for it
                synchronized(this){
                    Runnable r = new Runnable() {
                        public void run() {
                            synchronized(QueueEmpty.this){QueueEmpty.this.notifyAll();}
                            }
                    };
                    EventQueue.invokeLater(r);
                    wait();
                }
                //now 2 sec continuously should be silence
                while (System.currentTimeMillis() - lastEventTime < eventDelayTime) {
                    //sleep for the rest of eventDelay time
                    long sleepTime = eventDelayTime + lastEventTime - System.currentTimeMillis();
                    // it may happen that sleepTime < 0 (TODO investigate why)
                    if(sleepTime > 0) {
                        Thread.sleep(sleepTime);
                    }
                }

            //if (queue.peekEvent()==null) System.out.println("The AWT event queue seems to be empty.");
            //else System.out.println("Ops, in the AWT event queue still seems to be some tasks!");

            }
            catch (InterruptedException ex) {
                throw ex;
            }
            finally {
                //removing from listeners
                Toolkit.getDefaultToolkit().removeAWTEventListener(this);
            }
        }
        
        
    }

    private static class WithExecClassLoader extends URLClassLoader {
        public WithExecClassLoader() {
            super(new URL[] {Main.class.getProtectionDomain().getCodeSource().getLocation()},
                    Thread.currentThread().getContextClassLoader());
        }
        protected Class loadClass(String n, boolean r) throws ClassNotFoundException {
            if (n.startsWith("org.netbeans.xtest.plugin.ide.MainWithExec")) { // NOI18N
                // Do not proxy to parent!
                Class c = findLoadedClass(n);
                if (c != null) return c;
                c = findClass(n);
                if (r) resolveClass(c);
                return c;
            } else {
                return super.loadClass(n, r);
            }
        }
    }

    public static interface MainWithExecInterface {
        void run() throws Exception;
        void killPendingTasks();
        void discardChanges();
        void exit();
    }


    /** ClassLoader to load projects API calls. */
    private static class WithProjectsClassLoader extends URLClassLoader {
        public WithProjectsClassLoader() {
            super(new URL[] {Main.class.getProtectionDomain().getCodeSource().getLocation()},
                    Thread.currentThread().getContextClassLoader());
        }
        protected Class loadClass(String n, boolean r) throws ClassNotFoundException {
            if (n.startsWith("org.netbeans.xtest.plugin.ide.MainWithProjects")) { // NOI18N
                // Do not proxy to parent!
                Class c = findLoadedClass(n);
                if (c != null) return c;
                c = findClass(n);
                if (r) resolveClass(c);
                return c;
            } else {
                return super.loadClass(n, r);
            }
        }
    }

    public static interface MainWithProjectsInterface {
        void openProject(String projectPath);
        void createProject(String projectDir);
    }
    
    /* Workaround for JDK bug http://developer.java.sun.com/developer/bugParade/bugs/4924516.html.
     * Also see issue http://www.netbeans.org/issues/show_bug.cgi?id=42414.
     * ------------------------------------------------------------------------------------------
     * It is fixed in JDK1.5.0, so tt can be removed when JDK1.4.2 become unsupported. The following
     * listener is added to Toolkit at runBare() method and removed when it finishes. 
     * It distributes HierarchyEvent to all listening components and its subcomponents.
     */
    private static final DistributingHierarchyListener 
                distributingHierarchyListener = new DistributingHierarchyListener();

    private static class DistributingHierarchyListener implements AWTEventListener {

        public DistributingHierarchyListener() {
        }

        public void eventDispatched(AWTEvent aWTEvent) {
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

