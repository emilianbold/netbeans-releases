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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.jpda.AttachingDICookie;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.util.RequestProcessor;

/**
 * Ant task to attach the NetBeans JPDA debugger to a remote process.
 * @see "#18708"
 * @author Jesse Glick
 */
public class JPDAConnect extends Task {
    
    private String host = "localhost";
    /**
     * Host to connect to.
     * By default, localhost.
     */
    public void setHost(String h) {
        host = h;
    }
    
    private int port = 8888;
    
    /** Explicit sourcepath of the debugged process. */
    private Path sourcepath = null;
    
    /** Explicit classpath of the debugged process. */
    private Path classpath = null;
    
    /** Explicit bootclasspath of the debugged process. */
    private Path bootclasspath = null;
    
    /**
     * JPDA server port to connect to.
     * By default, 8888.
     */
    public void setPort (int p) {
        port = p;
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
    
    public void execute () throws BuildException {
        final Object[] exc = new Object [1];
        final ClassPath sourcePath = JPDAStart.createSourcePath (
            getProject (),
            classpath, 
            sourcepath, 
            bootclasspath
        );
            
        synchronized (exc) {
            RequestProcessor.getDefault ().post (new Runnable () {
                public void run() {
                    //System.err.println("TG: " + Thread.currentThread().getThreadGroup());
                    // VirtualMachineManagerImpl can be initialized here, so needs
                    // to be inside RP thread.
                    AttachingDICookie info = AttachingDICookie.create 
                        (host, port);
                    synchronized (exc) {
                        DebuggerInfo di = DebuggerInfo.create (
                            AttachingDICookie.ID, 
                            new Object [] {
                                info, 
                                sourcePath
                            }
                        );
                    }
                }
            });
            try {
                exc.wait ();
            } catch (InterruptedException e) {
                throw new BuildException (e);
            }
        }
        log ("Attached JPDA debugger to " + host + ":" + port);
    }
}
