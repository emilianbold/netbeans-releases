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
 */

package org.netbeans.xtest.plugin.ide;

import java.util.*;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.AWTEvent;
import java.awt.event.MouseEvent;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem; // override java.io.FileSystem
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;

import org.netbeans.TopSecurityManager;

// native kill stuff
import org.netbeans.xtest.util.JNIKill;
// PNGEncoder
import org.netbeans.xtest.util.PNGEncoder;

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
                        
        // need to initialize org.openide.TopManager, not org.netbeans.core.Plain
          System.getProperties().put (
              // Note this is really the NbTopManager impl now:
            "org.openide.TopManager", // NOI18N
            "org.netbeans.core.Main" // NOI18N
          );
        
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
           org.netbeans.core.Main.main(args);
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
        // start the init thread
        initThread.start();
        try {
            initThread.join(60000L);  // wait 1 minute at the most
        }
        catch (InterruptedException iex) {
            errMan.notify(ErrorManager.EXCEPTION, iex);
            
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
    }
    
    
    static final String TEST_CLASS = "test.class";
    //private static final String TEST_CLASSPATH = "test.classpath";
    private static final String TEST_ARGS = "test.arguments";
    static final String TEST_EXECUTOR = "test.executor";
    private static final String TEST_EXIT = "test.exit";
    static final String TEST_FINISHED = "test.finished";
    private static final String TEST_TIMEOUT = "xtest.timeout";
    //private static final String TEST_REDIRECT = "test.output.redirect";
    private static final String TEST_REUSE_IDE = "test.reuse.ide";
    private static final long DEFAULT_TIMEOUT = 2400000;
    
    private static final String IDE_MOUNTS = "xtest.ide.mounts";
    
    private static void doTestPart() {
        
        
        // do nothing if no TEST_CLASS specified
        if (System.getProperty(TEST_CLASS) == null)
            return;                
        
        org.netbeans.core.NbTopManager.get();
        
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

        
        StringTokenizer st = new StringTokenizer(System.getProperty(TEST_ARGS, ""));
        final String[] params = new String[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens()) {
            params[i++] = st.nextToken();
        }
        
        
        // get the current time 
        final long startTime = System.currentTimeMillis();                 
        final long testTime = testTimeout;
        
        Thread testThread = new Thread( new Runnable() {
            public void run() {
                try {
                    
                    // setup the repository - should not be needed for tests loaded by system classloader
                    
                    if ((!System.getProperty(IDE_MOUNTS,"").equals("")) && 
                        System.getProperty(TEST_REUSE_IDE, "false").equals("false")) {
                            mountFileSystems();
                    }
                     
                    /* 
                    setNodeProperties();
                    */
                    
                    handle.run(params, startTime, testTime);
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
                    LifecycleManager.getDefault().exit();
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
           java.lang.reflect.Method m = c.getMethod("exit",param);
           Integer intparam[] = {new Integer(1)};
           errMan.log(ErrorManager.USER, new Date().toString() + ": using TopSecurityManager.exit(1) to exit IDE.");
           // exit
           m.invoke(null,intparam);
        }
        catch (Exception e) {
           errMan.log(ErrorManager.USER, new Date().toString() + ": using System.exit(1) to exit IDE.");
           // exit
           System.exit(1);
        }
    }
    
    
    private static void mountFileSystems() {
        Repository repo = Repository.getDefault();
        
        // unmount the currently mounted filesystems
        // (could be more sofisticated some day)
        /*
        Enumeration all = repo.getFileSystems();
        while (all.hasMoreElements()) {
            FileSystem fs = (FileSystem)all.nextElement();
            // preserve the hidden and default filesystems
            if (!fs.isHidden() && !fs.isDefault())
                repo.removeFileSystem(fs);
        } 
         */       
        // mount new filesystems as specified in IDE_MOUNTS
        StringTokenizer stok = new StringTokenizer(System.getProperty(IDE_MOUNTS), System.getProperty("path.separator"));
        while (stok.hasMoreElements()) {
            String pathelem = stok.nextToken();
            try {
                if (pathelem.endsWith(".jar") || pathelem.endsWith(".zip")) {
                    JarFileSystem jfs = new JarFileSystem();
                    jfs.setJarFile(new java.io.File(pathelem));
                    repo.addFileSystem(jfs);
                }
                else {
                    LocalFileSystem lfs = new LocalFileSystem();
                    lfs.setRootDirectory(new java.io.File(pathelem));
                    repo.addFileSystem(lfs);
                }
            }
            catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            }
        }
        // sleep for a while, so the filesystem is mounted for sure
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
        }
    }
    
    
    
    /*
    private static void setNodeProperties() {
        FileObject fo = Repository.getDefault().findResource(System.getProperty(TEST_CLASS) + ".java");
        if (fo != null) {
            try {
                DataObject obj = DataObject.find(fo);
                Node nod = obj.getNodeDelegate();
                Node.PropertySet[] psets = nod.getPropertySets();
                Node.Property[] props = null;
                
                // get the Execution property set
                for (int i = 0; i < psets.length; i++) {
                    if (psets[i].getName().equals("Execution")) {
                        props = psets[i].getProperties();
                        break;
                    }
                }
                // get the "params" property and try to set it
                if (props != null) {
                    for (int i = 0; i < props.length; i++) {
                        if (props[i].getName().equals("params")) {
                            if (System.getProperty(TEST_ARGS) != null) {
                                props[i].setValue(System.getProperty(TEST_ARGS));
                            }
                        }
                    }
                }
            }
            catch (java.lang.Exception ex) {
                // ok, not able to set the Arguments property
                // it's still worth trying to proceed with the test
                // the FileObject may just be read-only
            }
            
        }
        
    }
    */

    private static class QueueEmpty implements java.awt.event.AWTEventListener {
        
        private long eventDelayTime = 100; // 100 millis
        private long lastEventTime;
        
        /** Creates a new QueueEmpty instance */
        public QueueEmpty() {
        }
        
        /** method called every time when AWT Event is dispatched
         * @param event event dispatched from AWT Event Queue
         */
        public void eventDispatched(java.awt.AWTEvent awtEvent) {
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
                    t.sleep(eventDelayTime + lastEventTime - System.currentTimeMillis());
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
        void run(String[] params, long startTime, long testTime) throws Exception;
        int terminateProcesses();
        //void setRedirect();
    }

}

