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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.beans.PropertyChangeEvent;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.connect.ListeningConnector;
import com.sun.jdi.connect.Transport;
import com.sun.jdi.connect.Connector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.netbeans.api.debugger.jpda.DebuggerStartException;
import org.netbeans.api.java.classpath.GlobalPathRegistry;

import org.openide.ErrorManager;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.RequestProcessor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
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
                Iterator i = Bootstrap.virtualMachineManager ().
                    listeningConnectors ().iterator ();
                for (; i.hasNext ();) {
                    lc = (ListeningConnector) i.next ();
                    Transport t = lc.transport ();
                    if (t != null && t.name ().equals (transport)) break;
                }
                if (lc == null) 
                    throw new BuildException
                        ("No trasports named " + transport + " found!");

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
                    sourcepath
                );
                ClassPath jdkSourcePath = createJDKSourcePath (
                    getProject (),
                    bootclasspath
                );
                if (startVerbose) {
                    System.out.println("\nS Crete sourcepath: ***************");
                    System.out.println ("    classpath : " + classpath);
                    System.out.println ("    sourcepath : " + sourcepath);
                    System.out.println ("    bootclasspath : " + bootclasspath);
                    System.out.println ("    >> sourcePath : " + sourcePath);
                    System.out.println ("    >> jdkSourcePath : " + jdkSourcePath);
                }
                
                if (stopClassName != null && stopClassName.length() > 0) {
                    if (startVerbose)
                        System.out.println (
                            "\nS create method breakpoint, class name = " + 
                            stopClassName
                        );
                    MethodBreakpoint b = createBreakpoint (stopClassName);
                    DebuggerManager.getDebuggerManager ().addDebuggerListener (
                        DebuggerManager.PROP_DEBUGGER_ENGINES,
                        new Listener (b)
                    );
                }                
                
                debug ("Debugger started");
                if (startVerbose)
                    System.out.println("\nS start listening on port " + port);
                
                Map properties = new HashMap ();
                // uncomment to implement smart stepping with step-outs 
                // rather than step-ins (for J2ME)
                // props.put("SS_ACTION_STEPOUT", Boolean.TRUE);
                properties.put ("sourcepath", sourcePath);
                properties.put ("name", getName ());
                properties.put ("jdksources", jdkSourcePath);
                JPDADebugger.startListening (
                    lc, 
                    args, 
                    new Object[] {properties}
                );
            } catch (java.io.IOException ioex) {
                lock[1] = ioex;
            } catch (DebuggerStartException dsex) {
                lock[1] = dsex;
            } catch (com.sun.jdi.connect.IllegalConnectorArgumentsException icaex) {
                lock[1] = icaex;
            } finally {
                debug ("Notifying");
                lock.notify ();
            }
        }
    } // run ()

    
    // support methods .........................................................
    
    private MethodBreakpoint createBreakpoint (String stopClassName) {
        MethodBreakpoint breakpoint = MethodBreakpoint.create (
            stopClassName,
            "*"
        );
        breakpoint.setHidden (true);
        DebuggerManager.getDebuggerManager ().addBreakpoint (breakpoint);
        return breakpoint;
    }

    private void debug (String msg) {
        if (!verbose) return;
        System.out.println (
            new Date() + " [" + Thread.currentThread().getName() + 
            "] - " + msg
        );
    }

    static ClassPath createSourcePath (
        Project project, 
        Path classpath,
        Path sourcepath
    ) {
        ClassPath cp = convertToSourcePath (project, classpath);
        ClassPath sp = convertToClassPath (project, sourcepath);
        
        ClassPath sourcePath = ClassPathSupport.createProxyClassPath (
            new ClassPath[] {cp, sp}
        );
        return sourcePath;
    }

    static ClassPath createJDKSourcePath (
        Project project, 
        Path bootclasspath
    ) {
        if (bootclasspath == null)
            return JavaPlatform.getDefault ().getSourceFolders ();
            // if current platform is default one, bootclasspath is set to null
        else
            return convertToSourcePath (project, bootclasspath);
    }
    
    private static ClassPath convertToClassPath (Project project, Path path) {
        String[] paths = path == null ? new String [0] : path.list ();
        List l = new ArrayList ();
        int i, k = paths.length;
        for (i = 0; i < k; i++) {
            File f = FileUtil.normalizeFile (project.resolveFile (paths [i]));
            if (!isValid (f, project)) continue;
            URL url = fileToURL (f);
            if (f == null) continue;
            l.add (url);
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
    private static ClassPath convertToSourcePath (Project project, Path path) {
        String[] paths = path == null ? new String [0] : path.list ();
        List l = new ArrayList ();
        Set exist = new HashSet ();
        int i, k = paths.length;
        for (i = 0; i < k; i++) {
            File file = FileUtil.normalizeFile 
                (project.resolveFile (paths [i]));
            if (!isValid (file, project)) continue;
            URL url = fileToURL (file);
            if (url == null) continue;
            if (startVerbose)
                System.out.println ("class: " + url);
            try {
                FileObject fos[] = SourceForBinaryQuery.findSourceRoots 
                    (url).getRoots();
                int j, jj = fos.length;
                /* ?? (#60640)
                if (jj == 0) { // no sourcepath defined
                    // Take all registered source roots
                    Set allSourceRoots = GlobalPathRegistry.getDefault().getSourceRoots();
                    fos = (FileObject[]) allSourceRoots.toArray(new FileObject[0]);
                    jj = fos.length;
                }
                 */
                for (j = 0; j < jj; j++) {
                    if (startVerbose)
                        System.out.println("source : " + fos [j]);
                    if (FileUtil.isArchiveFile (fos [j]))
                        fos [j] = FileUtil.getArchiveRoot (fos [j]);
                    try {
                        url = fos [j].getURL ();
                    } catch (FileStateInvalidException ex) {
                        ErrorManager.getDefault ().notify 
                            (ErrorManager.EXCEPTION, ex);
                        continue;
                    }
                    if (url == null) continue;
                    if (!exist.contains (url)) {
                        l.add (ClassPathSupport.createResource (url));
                        exist.add (url);
                    }
                } // for
            } catch (IllegalArgumentException ex) {
                if (startVerbose)
                    System.out.println("illegal url!");
            }
        }
        return ClassPathSupport.createClassPath (l);
    }


    private static URL fileToURL (File file) {
        try {
            FileObject fileObject = FileUtil.toFileObject (file);
            if (fileObject == null) return null;
            if (FileUtil.isArchiveFile (fileObject))
                fileObject = FileUtil.getArchiveRoot (fileObject);
            return fileObject.getURL ();
        } catch (FileStateInvalidException e) {
            ErrorManager.getDefault ().notify (ErrorManager.EXCEPTION, e);
            return null;
        }
    }

    private static boolean isValid (File f, Project project) {
        if (f.getPath ().indexOf ("${") != -1 && !f.exists ()) { // NOI18N
            project.log (
                "Classpath item " + f + " will be ignored.",  // NOI18N
                Project.MSG_VERBOSE
            );
            return false;
        }
        return true;
    }

    
    // innerclasses ............................................................
    
    private static class Listener extends DebuggerManagerAdapter {
        
        private MethodBreakpoint    breakpoint;
        private Set                 debuggers = new HashSet ();
        
        
        Listener (MethodBreakpoint breakpoint) {
            this.breakpoint = breakpoint;
        }
        
        public void propertyChange (PropertyChangeEvent e) {
            if (e.getPropertyName () == JPDADebugger.PROP_STATE) {
                int state = ((Integer) e.getNewValue ()).intValue ();
                if ( (state == JPDADebugger.STATE_DISCONNECTED) ||
                     (state == JPDADebugger.STATE_STOPPED)
                ) {
                    RequestProcessor.getDefault ().post (new Runnable () {
                        public void run () {
                            if (breakpoint != null) {
                                DebuggerManager.getDebuggerManager ().
                                    removeBreakpoint (breakpoint);
                                breakpoint = null;
                            }
                        }
                    });
                    dispose ();
                }
            }
            return;
        }
        
        private void dispose () {
            DebuggerManager.getDebuggerManager ().removeDebuggerListener (
                DebuggerManager.PROP_DEBUGGER_ENGINES,
                this
            );
            Iterator it = debuggers.iterator ();
            while (it.hasNext ()) {
                JPDADebugger d = (JPDADebugger) it.next ();
                d.removePropertyChangeListener (
                    JPDADebugger.PROP_STATE,
                    this
                );
            }
        }
        
        public void engineAdded (DebuggerEngine engine) {
            JPDADebugger debugger = (JPDADebugger) engine.lookupFirst 
                (null, JPDADebugger.class);
            if (debugger == null) return;
            debugger.addPropertyChangeListener (
                JPDADebugger.PROP_STATE,
                this
            );
            debuggers.add (debugger);
        }
        
        public void engineRemoved (DebuggerEngine engine) {
            JPDADebugger debugger = (JPDADebugger) engine.lookupFirst 
                (null, JPDADebugger.class);
            if (debugger == null) return;
            debugger.removePropertyChangeListener (
                JPDADebugger.PROP_STATE,
                this
            );
            debuggers.remove (debugger);
        }
    }
}
