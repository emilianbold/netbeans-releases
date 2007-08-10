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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
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

    private static final Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.ant"); // NOI18N
    
    /** Name of the property to which the JPDA address will be set.
     * Target VM should use this address and connect to it
     */
    private String                  addressProperty;
    /** Default transport is socket*/
    private String                  transport = "dt_socket"; // NOI18N
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
    
    static void verifyPaths(Project project, Path path) {
        if (path == null) return ;
        String[] paths = path.list();
        for (int i = 0; i < paths.length; i++) {
            String pathName = project.replaceProperties(paths[i]);
            File file = FileUtil.normalizeFile 
                (project.resolveFile (pathName));
            if (!file.exists()) {
                project.log("Non-existing path \""+pathName+"\" provided.", Project.MSG_WARN);
                //throw new BuildException("Non-existing path \""+paths[i]+"\" provided.");
            }
        }
    }

    
    // main methods ............................................................

    public void execute () throws BuildException {
        verifyPaths(getProject(), classpath);
        //verifyPaths(getProject(), bootclasspath); Do not check the paths on bootclasspath (see issue #70930).
        verifyPaths(getProject(), sourcepath);
        try {
            logger.fine("JPDAStart.execute()"); // NOI18N
            debug ("Execute started"); // NOI18N
            if (name == null)
                throw new BuildException ("name attribute must specify name of this debugging session", getLocation ());
            if (addressProperty == null)
                throw new BuildException ("addressproperty attribute must specify name of property to which address will be set", getLocation ());
            if (transport == null)
                transport = "dt_socket"; // NOI18N
            debug ("Entering synch lock"); // NOI18N
            lock = new Object [2];
            synchronized (lock) {
                debug ("Entered synch lock"); // NOI18N
                RequestProcessor.getDefault ().post (this);
                try {
                    debug ("Entering wait"); // NOI18N
                    lock.wait ();
                    debug ("Wait finished"); // NOI18N
                    if (lock [1] != null) {
                        if (lock[1] instanceof DebuggerStartException) {
                            //getProject().log(((DebuggerStartException) lock[1]).getLocalizedMessage(), Project.MSG_ERR);
                            throw new BuildException(((DebuggerStartException) lock[1]).getLocalizedMessage());
                        } else {
                            throw new BuildException ((Throwable) lock [1]);
                        }
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
        logger.fine("JPDAStart.run()"); // NOI18N
        debug ("Entering synch lock"); // NOI18N
        synchronized (lock) {
            debug("Entered synch lock"); // NOI18N
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
                final Map args = lc.defaultArguments ();
                String address;
                try {
                    address = lc.startListening (args);
                } catch (java.io.IOException ioex) {
                    getProject().log("Listening failed with arguments: "+args);
                    throw ioex;
                } catch (com.sun.jdi.connect.IllegalConnectorArgumentsException iaex) {
                    getProject().log("Listening failed with arguments: "+args);
                    throw iaex;
                }
                int port = -1;
                try {
                    port = Integer.parseInt (address.substring (address.indexOf (':') + 1));
                    getProject ().setNewProperty (getAddressProperty (), "localhost:" + port); // NOI18N
                    Connector.IntegerArgument portArg = (Connector.IntegerArgument) args.get("port"); // NOI18N
                    portArg.setValue (port);
                } catch (Exception e) {
                    // this address format is not known, use default
                    getProject ().setNewProperty (getAddressProperty (), address);
                }

                debug ("Creating source path"); // NOI18N
                ClassPath sourcePath = createSourcePath (
                    getProject (),
                    classpath, 
                    sourcepath
                );
                ClassPath jdkSourcePath = createJDKSourcePath (
                    getProject (),
                    bootclasspath
                );
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Create sourcepath:"); // NOI18N
                    logger.fine("    classpath : " + classpath); // NOI18N
                    logger.fine("    sourcepath : " + sourcepath); // NOI18N
                    logger.fine("    bootclasspath : " + bootclasspath); // NOI18N
                    logger.fine("    >> sourcePath : " + sourcePath); // NOI18N
                    logger.fine("    >> jdkSourcePath : " + jdkSourcePath); // NOI18N
                }
                
                if (stopClassName != null && stopClassName.length() > 0) {
                    logger.fine(
                            "create method breakpoint, class name = " + // NOI18N
                            stopClassName
                        );
                    MethodBreakpoint b = createBreakpoint (stopClassName);
                    DebuggerManager.getDebuggerManager ().addDebuggerListener (
                        DebuggerManager.PROP_DEBUGGER_ENGINES,
                        new Listener (b)
                    );
                }                
                
                debug ("Debugger started"); // NOI18N
                logger.fine("start listening on port " + port); // NOI18N
                
                final Map properties = new HashMap ();
                // uncomment to implement smart stepping with step-outs 
                // rather than step-ins (for J2ME)
                // props.put("SS_ACTION_STEPOUT", Boolean.TRUE);
                properties.put ("sourcepath", sourcePath); // NOI18N
                properties.put ("name", getName ()); // NOI18N
                properties.put ("jdksources", jdkSourcePath); // NOI18N
                final ListeningConnector flc = lc;
                // Let it start asynchronously so that the script can go on and start the debuggee
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        try {
                            JPDADebugger.startListening (
                                flc,
                                args,
                                new Object[] { properties }
                            );
                        } catch (DebuggerStartException dsex) {
                            // Was not able to start up
                        }
                    }
                });
                getProject().addBuildListener(new BuildListener() {
                    
                    public void messageLogged(BuildEvent event) {}
                    public void taskStarted(BuildEvent event) { }
                    public void taskFinished(BuildEvent event) {}
                    public void targetStarted(BuildEvent event) {}
                    public void targetFinished(BuildEvent event) {}
                    public void buildStarted(BuildEvent event) {}
                    public void buildFinished(BuildEvent event) {
                        try {
                            flc.stopListening(args);
                        } catch (java.io.IOException ioex) {
                        } catch (com.sun.jdi.connect.IllegalConnectorArgumentsException iaex) {
                        }
                    }
                    
                });
            } catch (java.io.IOException ioex) {
                lock[1] = ioex;
            } catch (com.sun.jdi.connect.IllegalConnectorArgumentsException icaex) {
                lock[1] = icaex;
            } finally {
                debug ("Notifying"); // NOI18N
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

    private final static void debug (String msg) {
        if (!logger.isLoggable(Level.FINER)) return;
        logger.finer (
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
        if (bootclasspath == null) {
            // if current platform is default one, bootclasspath is set to null
            JavaPlatform jp = JavaPlatform.getDefault();
            if (jp != null) {
                return jp.getSourceFolders ();
            } else {
                return ClassPathSupport.createClassPath(java.util.Collections.EMPTY_LIST);
            }
        } else {
            return convertToSourcePath (project, bootclasspath);
        }
    }
    
    private static ClassPath convertToClassPath (Project project, Path path) {
        String[] paths = path == null ? new String [0] : path.list ();
        List l = new ArrayList ();
        int i, k = paths.length;
        for (i = 0; i < k; i++) {
            String pathName = project.replaceProperties(paths[i]);
            File f = FileUtil.normalizeFile (project.resolveFile (pathName));
            if (!isValid (f, project)) continue;
            URL url = fileToURL (f, project);
            if (url == null) continue;
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
            String pathName = project.replaceProperties(paths[i]);
            File file = FileUtil.normalizeFile 
                (project.resolveFile (pathName));
            if (!isValid (file, project)) continue;
            URL url = fileToURL (file, project);
            if (url == null) continue;
            logger.fine("convertToSourcePath - class: " + url); // NOI18N
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
                    logger.fine("convertToSourcePath - source : " + fos [j]); // NOI18N
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
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
                logger.fine("Have illegal url! "+ex.getLocalizedMessage()); // NOI18N
            }
        }
        return ClassPathSupport.createClassPath (l);
    }


    private static URL fileToURL (File file, Project project) {
        try {
            FileObject fileObject = FileUtil.toFileObject (file);
            if (fileObject == null) return null;
            if (FileUtil.isArchiveFile (fileObject)) {
                fileObject = FileUtil.getArchiveRoot (fileObject);
                if (fileObject == null) {
                    project.log("Bad archive "+file.getAbsolutePath(), Project.MSG_WARN);
                    /*
                    ErrorManager.getDefault().notify(ErrorManager.getDefault().annotate(
                            new NullPointerException("Bad archive "+file.toString()),
                            NbBundle.getMessage(JPDAStart.class, "MSG_WrongArchive", file.getAbsolutePath())));
                     */
                    return null;
                }
            }
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
