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
 */

package org.netbeans.modules.debugger.jpda.ant;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;

import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.connect.ListeningConnector;
import com.sun.jdi.connect.Transport;
import com.sun.jdi.connect.Connector;
import java.beans.PropertyChangeEvent;
import org.netbeans.api.debugger.DebuggerEngine;


import org.netbeans.api.debugger.DebuggerManagerAdapter;

import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.java.platform.JavaPlatform;

/**
 * Ant task to start the NetBeans JPDA debugger in listening mode.
 *
 * @author Jesse Glick, David Konecny
 */
public class JPDAStart extends Task implements Runnable {

    private static final boolean    verbose = System.getProperty ("netbeans.debugger.debug") != null;
    private static final boolean    startVerbose = System.getProperty ("netbeans.debugger.start") != null;

    /** Name of the property to which the JPDA address will be set.
     * Target VM should use this address and connect to it
     */
    private String                  addressProperty;
    /** Default transport is socket*/
    private String                  transport = "dt_socket";
    /** Name which will represent this debugging session in debugger UI.
     * If known in advance it should be name of the app which will be debugged.
     */
    private String                  name;
    /** Explicit sourcepath of the debugged process. */
    private Path                    sourcepath = null;
    /** Explicit classpath of the debugged process. */
    private Path                    classpath = null;
    /** Explicit bootclasspath of the debugged process. */
    private Path                    bootclasspath = null;
    private Object []               lock = null; 
    /** The class debugger should stop in, or null. */
    private String                  stopClassName = null;
    private MethodBreakpoint        breakpoint;

    {
        DebuggerManager.getDebuggerManager ().addDebuggerListener (
            DebuggerManager.PROP_DEBUGGER_ENGINES,
            new Listener ()
        );
    }

    // properties ..............................................................
    
    public void setAddressProperty (String propertyName) {
        this.addressProperty = propertyName;
    }
    
    private String getAddressProperty () {
        return addressProperty;
    }
    
    public void setTransport (String transport) {
        this.transport = transport;
    }
    
    private String getTransport () {
        return transport;
    }
    
    public void setName (String name) {
        this.name = name;
    }
    
    private String getName () {
        return name;
    }
    
    public void setStopClassName (String stopClassName) {
        this.stopClassName = stopClassName;
    }
    
    private String getStopClassName () {
        return stopClassName;
    }
    
    public void addClasspath (Path path) {
        if (classpath != null)
            throw new BuildException ("Only one classpath subelement is supported");
        classpath = path;
    }
    
    public void addBootclasspath (Path path) {
        if (bootclasspath != null)
            throw new BuildException ("Only one bootclasspath subelement is supported");
        bootclasspath = path;
    }
    
    public void addSourcepath (Path path) {
        if (sourcepath != null)
            throw new BuildException ("Only one sourcepath subelement is supported");
        sourcepath = path;
    }

    
    // main methods ............................................................

    public void execute () throws BuildException {
        try {
            if (startVerbose)
                System.out.println("\nS JPDAStart ***************");
            debug ("Execute started");
            if (name == null)
                throw new BuildException ("name attribute must specify name of this debugging session", getLocation ());
            if (addressProperty == null)
                throw new BuildException ("addressproperty attribute must specify name of property to which address will be set", getLocation ());
            if (transport == null)
                transport = "dt_socket";
            debug ("Entering synch lock");
            lock = new Object [2];
            synchronized (lock) {
                debug ("Entered synch lock");
                RequestProcessor.getDefault ().post (this);
                try {
                    debug ("Entering wait");
                    lock.wait ();
                    debug ("Wait finished");
                    if (lock [1] != null) {
                        throw new BuildException ((Throwable) lock [1]);
                    }
                } catch (InterruptedException e) {
                    throw new BuildException (e);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace ();
            throw new BuildException (t);
        }
    }
    
    public void run () {
        if (startVerbose)
            System.out.println("\nS JPDAStart2 ***************");
        debug ("Entering synch lock");
        synchronized (lock) {
            debug("Entered synch lock");
            try {

                ListeningConnector lc = null;
                for (Iterator i = Bootstrap.virtualMachineManager ().listeningConnectors ().iterator (); i.hasNext(); ) {
                    lc = (ListeningConnector) i.next();
                    Transport t = lc.transport ();
                    if (t != null && t.name ().equals (transport)) break;
                }
                if (lc == null) throw new BuildException("No trasports named " + transport + " found!");

                // TODO: revisit later when http://developer.java.sun.com/developer/bugParade/bugs/4932074.html gets integrated into JDK
                // This code parses the address string "HOST:PORT" to extract PORT and then point debugee to localhost:PORT
                // This is NOT a clean solution to the problem but it SHOULD work in 99% cases
                Map args = lc.defaultArguments ();
                String address = lc.startListening (args);
                int port = -1;
                try {
                    port = Integer.parseInt (address.substring (address.indexOf (':') + 1));
                    getProject ().setNewProperty (getAddressProperty (), "localhost:" + port);
                    Connector.IntegerArgument portArg = (Connector.IntegerArgument) args.get("port");
                    portArg.setValue (port);
                } catch (Exception e) {
                    // this address format is not known, use default
                    getProject ().setNewProperty (getAddressProperty (), address);
                }

                debug ("Creating source path");
                ClassPath sourcePath = createSourcePath (
                    getProject (),
                    classpath, 
                    sourcepath, 
                    bootclasspath
                );
                
                if (stopClassName != null && stopClassName.length() > 0) {
                    if (startVerbose)
                        System.out.println("\nS create method breakpoint, class name = " + stopClassName);
                    createBreakpoint (stopClassName);
                }                
                
                debug ("Debugger started");
                if (startVerbose)
                    System.out.println("\nS start listening on port " + port);
                
                Properties props = new Properties();
                // uncomment to implement smart stepping with step-outs rather than step-ins (for J2ME)
                // props.put("SS_ACTION_STEPOUT", Boolean.TRUE);
                JPDADebugger.startListening (lc, args, new Object[] {sourcePath, getName (), props});
            } catch (Throwable e) {
                lock [1] = e;
            } finally {
                debug ("Notifying");
                lock.notify ();
            }
        }
    } // run ()

    
    // support methods .........................................................
    
    private void createBreakpoint (String stopClassName) {
        breakpoint = MethodBreakpoint.create (
            stopClassName,
            ""
        );
        breakpoint.setHidden (true);
        DebuggerManager.getDebuggerManager ().addBreakpoint (breakpoint);
    }
    
    private void removeBreakpoint () {
        if (breakpoint != null) {
            DebuggerManager.getDebuggerManager ().removeBreakpoint (breakpoint);
            breakpoint = null;
        }
    }

    private void debug (String msg) {
        if (!verbose) return;
        System.out.println (new Date() + " [" + Thread.currentThread().getName() + "] - " + msg);
    }

    static ClassPath createSourcePath (
        Project project, 
        Path classpath,
        Path sourcepath,
        Path bootclasspath
    ) {
        ClassPath sourcePath = convertToSourcePath 
           (project, classpath);
        if (sourcepath != null)
            sourcePath = ClassPathSupport.createProxyClassPath (
                new ClassPath[] {
                    sourcePath,
                    convertToClassPath (project, sourcepath)
                }
            );
//        sourcePath = ClassPathSupport.createProxyClassPath (
//            new ClassPath[] {
//                sourcePath,
//                (bootclasspath == null) ? 
//                    JavaPlatform.getDefault ().getSourceFolders () :
//                    convertToSourcePath (project, bootclasspath)
//            }
//        );
        if (startVerbose) {
            System.out.println("\nS Crete sourcepath: ***************");
            System.out.println ("    classpath : " + classpath);
            System.out.println ("    sourcepath : " + sourcepath);
            System.out.println ("    bootclasspath : " + bootclasspath);
            System.out.println ("    >> sourcePath : " + sourcePath);
        }
        return sourcePath;
    }
    
    static ClassPath convertToClassPath (Project project, Path path) {
        String[] paths = path.list ();
        List l = new ArrayList ();
        int i, k = paths.length;
        for (i = 0; i < k; i++) {
            URL u = null;
            try {
                File f = FileUtil.normalizeFile (project.resolveFile (paths [i]));
                if (!isValid (f, project)) {
                    continue;
                }
                String pathString = paths [i].toLowerCase ();
                if (pathString.endsWith (".jar")) {
                    u = new URL ("jar:" + f.toURI () + "!/");
                } else if (pathString.endsWith (".zip")) {
                    u = new URL ("jar:" + f.toURI () + "!/");
                } else {
                    u = f.toURI ().toURL ();
                }
            } catch (MalformedURLException e) {
                ErrorManager.getDefault ().notify (ErrorManager.EXCEPTION, e);
                continue;
            }
            l.add (u);
        }
        URL[] urls = (URL[]) l.toArray (new URL [l.size ()]);
        return ClassPathSupport.createClassPath (urls);
    }

    /**
     * This method uses SourceForBinaryQuery to find sources for each
     * path item and returns them as ClassPath instance. All path items for which
     * the sources were not found are omitted.
     *
     */
    static ClassPath convertToSourcePath (Project project, Path path) {
        String[] paths = path == null ? new String [0] : path.list ();
        List l = new ArrayList ();
        List exist = new ArrayList ();
        for (int i = 0; i < paths.length; i++) {
            URL u = null;
            try {
                File f = FileUtil.normalizeFile (project.resolveFile (paths [i]));
                if (!isValid (f, project)) {
                    continue;
                }
                String pathString = paths [i].toLowerCase ();
                if (pathString.endsWith (".jar")) {
                    u = new URL ("jar:" + f.toURI () + "!/");
                } else if (pathString.endsWith (".zip")) {
                    u = new URL ("jar:" + f.toURI () + "!/");
                } else {
                    u = f.toURI ().toURL ();
                }
            } catch (MalformedURLException e) {
                ErrorManager.getDefault ().notify (ErrorManager.EXCEPTION, e);
                continue;
            }
            FileObject fos[] = SourceForBinaryQuery.findSourceRoots (u).getRoots();
            if (startVerbose)
                System.out.println("class: " + u);
            if (fos.length > 0) {
                if (startVerbose)
                    System.out.println("source : " + fos [0]);
                try {
                    File file = FileUtil.toFile(fos [0]);
                    if (file == null) continue;
    //                    u = FileUtil.toFile (fos [0]).toURI ().toURL ();
                    u = file.toURI ().toURL ();
                } catch (MalformedURLException e) {
                    ErrorManager.getDefault ().notify (ErrorManager.EXCEPTION, e);
                    continue;
                }
                if (!exist.contains (u)) {
                    l.add (ClassPathSupport.createResource (u));
                    exist.add (u);
                }
            }
        }
        return ClassPathSupport.createClassPath (l);
    }

    /**
     * Appends to classpath all items from the given path.
     *
     * @param cp classpath; can be null
     */
//    public static ClassPath appendPath (Project project, ClassPath cp, Path path) {
//        String[] paths = path.list ();
//        List l = new ArrayList ();
//        for (int i = 0; i < paths.length; i++) {
//            URL u = null;
//            try {
//                File f = FileUtil.normalizeFile (project.resolveFile (paths [i]));
//                if (!isValid (f, project)) {
//                    continue;
//                }
//                u = f.toURI ().toURL ();
//            } catch (MalformedURLException e) {
//                ErrorManager.getDefault ().notify (ErrorManager.EXCEPTION, e);
//                continue;
//            }
//            l.add (ClassPathSupport.createResource (u));
//        }
//        if (cp != null) {
//            return ClassPathSupport.createProxyClassPath (
//                new ClassPath [] {cp, ClassPathSupport.createClassPath (l)}
//            );
//        } else {
//            return ClassPathSupport.createClassPath (l);
//        }
//    }

    private static boolean isValid (File f, Project project) {
        if (f.getPath ().indexOf ("${") != -1 && !f.exists ()) { // NOI18N
            project.log ("Classpath item " + f + " will be ignored.", Project.MSG_VERBOSE); // NOI18N
            return false;
        }
        return true;
    }
    
    private class Listener extends DebuggerManagerAdapter {
        public void propertyChange (PropertyChangeEvent e) {
            if (e.getPropertyName () == JPDADebugger.PROP_STATE) {
                int state = ((Integer) e.getNewValue ()).intValue ();
                if ( (state == JPDADebugger.STATE_DISCONNECTED) ||
                     (state == JPDADebugger.STATE_STOPPED)
                ) {
                    RequestProcessor.getDefault ().post (new Runnable () {
                        public void run () {
                            removeBreakpoint ();
                        }
                    });
                }
            }
            return;
        }
        
        public void engineAdded (DebuggerEngine engine) {
            JPDADebugger debugger = (JPDADebugger) engine.lookupFirst 
                (null, JPDADebugger.class);
            if (debugger == null) return;
            debugger.addPropertyChangeListener (
                JPDADebugger.PROP_STATE,
                this
            );
        }
        
        public void engineRemoved (DebuggerEngine engine) {
            JPDADebugger debugger = (JPDADebugger) engine.lookupFirst 
                (null, JPDADebugger.class);
            if (debugger == null) return;
            debugger.removePropertyChangeListener (
                JPDADebugger.PROP_STATE,
                this
            );
        }
    }
}
