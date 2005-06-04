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
import org.netbeans.TopSecurityManager;
import org.netbeans.xtest.util.JNIKill;
import org.netbeans.xtest.util.PNGEncoder;
import org.openide.ErrorManager;

/**
 * Main part of XTest starter. Must not use anything outside lib/*.jar and lib/ext/*.jar.
 * @author Jan Chalupa, Jesse Glick
 */
public class Main extends Object {
    
    public static ErrorManager errMan;
    
    // ide running flag file
    private static File ideRunning = null;

    // netbeans pid system property 
    private static final String IDE_PID_SYSTEM_PROPERTY = "netbeans.pid";
    
    // flag whether IDE was interrupted 
    private static boolean ideInterrupted = false;
    
    
    private static void captureIDEScreen(String fileName) {
        try {
            System.out.println("capturing screenshot:"+fileName);
            String userdirName = System.getProperty("netbeans.user");            
            // capture the screenshot
            PNGEncoder.captureScreenToIdeUserdir(userdirName,fileName);
  
        } catch (Exception e) {
            if (errMan != null) {
                errMan.log(ErrorManager.USER, "Exception thrown when capturing IDE screenshot");
                errMan.notify(ErrorManager.USER,e);
            } else {
                System.err.println("Exception thrown when capturing IDE screenshot and ErrorManager not found!");
                e.printStackTrace(System.err);
            }
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

        // get the static ErrorManager instance
        errMan = ErrorManager.getDefault();
        // and initialize it (if XTestErrorManage is used)
        //errMan.log(ErrorManager.EXCEPTION,"Starting tests");
            
        // some threads may be still active, wait for the event queue to become quiet
        Thread initThread = new Thread(new Runnable() {
            public void run() {
                try {
                    new QueueEmpty().waitEventQueueEmpty(2000);
                }
                catch (Exception ex) {
                    errMan.notify(ErrorManager.EXCEPTION, ex);
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
            errMan.notify(ErrorManager.EXCEPTION, iex);
        } finally {
            // workaround for JDK bug 4924516 (see below)
            Toolkit.getDefaultToolkit().removeAWTEventListener(distributingHierarchyListener);
        }
        if (initThread.isAlive()) {
            // time-out expired, event queue still busy -> interrupt the init thread
            errMan.log(ErrorManager.USER, new Date().toString() + ": EventQueue still busy, starting anyway.");
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
    //private static final String TEST_REDIRECT = "test.output.redirect";
    private static final String TEST_REUSE_IDE = "test.reuse.ide";
    private static final long DEFAULT_TIMEOUT = 2400000;
    
    // properties used for new project infrastructure
    private static final String IDE_CREATE_PROJECT = "xtest.ide.create.project";
    private static final String IDE_OPEN_PROJECT = "xtest.ide.open.project";
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
                errMan.notify(e);
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
            errMan.notify(e);
            return;
        }
        
        /*
        if (System.getProperty(TEST_REDIRECT) != null && System.getProperty(TEST_REDIRECT).equals("true")) {
            handle.setRedirect();
        }
         */

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
                    }
                    
                    handle.run();
                }
                catch (Exception ex) {
                    errMan.notify(ErrorManager.EXCEPTION, ex);
                }
            }
        });
        
        errMan.log(ErrorManager.USER, new Date().toString() + ": just starting.");
        // start the test thread
        testThread.start();
        try {
            testThread.join(testTimeout);
        }
        catch (InterruptedException iex) {
            errMan.notify(ErrorManager.EXCEPTION, iex);
        }
        if (testThread.isAlive()) {
            // time-out expired, test not finished -> interrupt
            errMan.log(ErrorManager.USER, new Date().toString() + ": time-out expired - interrupting! ***");
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
                    // terminate all running processes
                    try {
                        handle.terminateProcesses();
                    } catch (Exception e) {
                        System.out.println("Exception when terminating processes started from IDE");
                        e.printStackTrace();
                    }
                    // exit IDE
                    handle.exit ();
                }
            });
            // try to exit nicely first
            errMan.log(ErrorManager.USER, new Date().toString() + ": soft exit attempt.");
            exitThread.start();
            try {
                // wait 60 seconds for the IDE to exit
                exitThread.join(60000);
            }
            catch (InterruptedException iex) {
                errMan.notify(ErrorManager.EXCEPTION, iex);
            }
            if (exitThread.isAlive()) {
                // IDE refuses to shutdown, terminate unconditionally
                errMan.log(ErrorManager.USER, new Date().toString() + ": hard exit attempt!!!.");
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
           errMan.log(ErrorManager.USER, new Date().toString() + ": using TopSecurityManager.exit(1) to exit IDE.");
           // exit
           m.invoke(null,(Object[])intparam);
        }
        catch (Exception e) {
           errMan.log(ErrorManager.USER, new Date().toString() + ": using System.exit(1) to exit IDE.");
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
                while ( queue.peekEvent() != null ) Thread.currentThread().sleep(100);
                //test it - post my own task and wait for it
                synchronized(this){
                    Runnable r = new Runnable() {
                        public void run() {
                            synchronized(QueueEmpty.this){QueueEmpty.this.notifyAll();}
                        }
                    };
                    queue.invokeLater(r);
                    wait();
                }
                //now 2 sec continuously should be silence
                while (System.currentTimeMillis() - lastEventTime < eventDelayTime) {
                    //sleep for the rest of eventDelay time
                    long sleepTime = eventDelayTime + lastEventTime - System.currentTimeMillis();
                    // it may happen that sleepTime < 0 (TODO investigate why)
                    if(sleepTime > 0) {
                        t.sleep(sleepTime);
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
        int terminateProcesses();
        void exit();
        //void setRedirect();
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

