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

package org.netbeans.modules.debugger.jpda.ant;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;

import org.openide.util.RequestProcessor;

import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.java.classpath.ClassPath;


/**
 * Ant task to attach the NetBeans JPDA debugger to a remote process.
 * @see "#18708"
 * @author Jesse Glick
 */
public class JPDAConnect extends Task implements BuildListener {

    private static final Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.ant"); // NOI18N
    
    private String host = "localhost"; // NOI18N

    private String address;
    
    /** Explicit sourcepath of the debugged process. */
    private Path sourcepath = null;
    
    /** Explicit classpath of the debugged process. */
    private Path classpath = null;
    
    /** Explicit bootclasspath of the debugged process. */
    private Path bootclasspath = null;
        
    /** Name which will represent this debugging session in debugger UI.
     * If known in advance it should be name of the app which will be debugged.
     */
    private String name;

    /** Default transport is socket*/
    private String transport = "dt_socket"; // NOI18N
    
    private JPDADebugger debugger;
    
    
    /**
     * Host to connect to.
     * By default, localhost.
     */
    public void setHost (String h) {
        host = h;
    }
    
    public void setAddress (String address) {
        this.address = address;
    }
    
    private String getAddress () {
        return address;
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
    
    public void execute () throws BuildException {
        logger.fine("JPDAConnect.execute ()"); // NOI18N
        
        getProject().addBuildListener(this);

        JPDAStart.verifyPaths(getProject(), classpath);
        //JPDAStart.verifyPaths(getProject(), bootclasspath); Do not check the paths on bootclasspath (see issue #70930).
        JPDAStart.verifyPaths(getProject(), sourcepath);
        
        if (name == null)
            throw new BuildException (
                "name attribute must specify name of this debugging session", 
                getLocation ()
            );
        if (address == null)
            throw new BuildException (
                "address attribute must specify port number or memory " +
                "allocation unit name of connection", 
                getLocation ()
            );
        if (transport == null)
            transport = "dt_socket"; // NOI18N

        final Object[] lock = new Object [1];

        ClassPath sourcePath = JPDAStart.createSourcePath (
            getProject (),
            classpath, 
            sourcepath
        );
        ClassPath jdkSourcePath = JPDAStart.createJDKSourcePath (
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
        final Map properties = new HashMap ();
        properties.put ("sourcepath", sourcePath); // NOI18N
        properties.put ("name", getName ()); // NOI18N
        properties.put ("jdksources", jdkSourcePath); // NOI18N
        

        synchronized(lock) {
            RequestProcessor.getDefault ().post (new Runnable () {
                public void run() {
                    synchronized(lock) {
                        try {
                            if (logger.isLoggable(Level.FINE)) {
                                logger.fine(
                                    "JPDAConnect.execute ().synchronized: "  // NOI18N
                                    + "host = " + host + " port = " + address + // NOI18N
                                    " transport = " + transport // NOI18N
                                );
                            }
                            // VirtualMachineManagerImpl can be initialized 
                            // here, so needs to be inside RP thread.
                            if (transport.equals ("dt_socket")) // NOI18N
                                try {
                                    debugger = JPDADebugger.attach (
                                        host, 
                                        Integer.parseInt (address), 
                                        new Object[] {properties}
                                    );
                                } catch (NumberFormatException e) {
                                    throw new BuildException (
                                        "address attribute must specify port " +
                                        "number for dt_socket connection", 
                                        getLocation ()
                                    );
                                }
                            else
                                debugger = JPDADebugger.attach (
                                    address, 
                                    new Object[] {properties}
                                );
                            logger.fine(
                                    "JPDAConnect.execute ().synchronized " + // NOI18N
                                    "end: success" // NOI18N
                                );
                        } catch (Throwable e) {
                            logger.fine(
                                    "JPDAConnect.execute().synchronized " + // NOI18N
                                    "end: exception " + e // NOI18N
                                );
                            lock[0] = e;
                        } finally {
                            lock.notify();
                        }
                    }
                }
            });
            try {
                lock.wait();
            } catch (InterruptedException e) {
                logger.fine("JPDAConnect.execute() " + "end: exception " + e); // NOI18N
                throw new BuildException(e);
            }
            if (lock[0] != null)  {
                logger.fine("JPDAConnect.execute() " + "end: exception " + lock[0]); // NOI18N
                throw new BuildException((Throwable) lock[0]);
            }

        }
        if (host == null)
            log ("Attached JPDA debugger to " + address);
        else
            log ("Attached JPDA debugger to " + host + ":" + address);
        logger.fine("JPDAConnect.execute () " + "end: success"); // NOI18N
    }

    public void buildStarted(BuildEvent buildEvent) {
    }

    public void buildFinished(BuildEvent buildEvent) {
        logger.fine("Build Finished."); // NOI18N
        // The build has finished - we need to terminate the debugger
        if (debugger != null) {
            // Find the associated session
            Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
            for (int i = 0; i < sessions.length; i++) {
                JPDADebugger d = (JPDADebugger) sessions[i].lookupFirst(null, JPDADebugger.class);
                if (d == debugger) {
                    sessions[i].kill();
                    break;
                }
            }
        }
    }

    public void targetStarted(BuildEvent buildEvent) {
    }

    public void targetFinished(BuildEvent buildEvent) {
    }

    public void taskStarted(BuildEvent buildEvent) {
    }

    public void taskFinished(BuildEvent buildEvent) {
    }

    public void messageLogged(BuildEvent buildEvent) {
    }
}
