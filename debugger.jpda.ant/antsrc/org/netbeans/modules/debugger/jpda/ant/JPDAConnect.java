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
    public void setPort(int p) {
        port = p;
    }
    
    public void addClasspath(Path path) {
        if (classpath != null) {
            throw new BuildException("Only one classpath subelement is supported");
        }
        classpath = path;
    }
    
    public void addBootclasspath(Path path) {
        if (bootclasspath != null) {
            throw new BuildException("Only one bootclasspath subelement is supported");
        }
        bootclasspath = path;
    }
    
    public void addSourcepath(Path path) {
        if (sourcepath != null) {
            throw new BuildException("Only one sourcepath subelement is supported");
        }
        sourcepath = path;
    }
    
/*
    public void execute() throws BuildException {
        final DebuggerException[] exc = new DebuggerException[1];
        final AbstractDebugger d = Register.getCoreDebugger();
        assert d != null;
        ClassPath cp_ = (classpath == null) ? null : JPDAStart.createSourceClassPath(classpath);
        if (sourcepath != null) {
            cp_ = JPDAStart.appendPath(cp_, sourcepath);
        }
        final ClassPath cp = cp_;
        final ClassPath bootcp = (bootclasspath == null) ? null : JPDAStart.createSourceClassPath(bootclasspath);
        // XXX using RemoteDebuggerInfo is not quite right because it assumes that
        // the remote process *must* already be running. By comparison, o.n.m.d.j.Launcher
        // seems to use server=n and com.sun.jdi.VirtualMachine.accept, which is surely
        // safer. Current idiom is to use <parallel>/<sleep> in the Ant script. Better would be
        // for there to be a DebuggerInfo which is more similar to JPDADebuggerInfo than
        // RemoteDebuggerInfo in behavior re. accepting connections, yet does not actually
        // start the process - just waiting for a connection. Then <parallel> would not be
        // needed; you would simply run this task first and then the debugged process
        // (with server=n) would attach to the IDE. Unfortunately the debuggerjpda module
        // does not currently support this scenario since JPDADebugger.startDebugger is a
        // switch statement (!) and none of the choices are what is needed here.
        // Also, ideally the task would run first and produce a free socket port and
        // define this as a property in the Ant project so that you could use that when
        // running <java>; otherwise it is messy to debug more than one app at once.
        // For more, see #37097.
        // XXX just running startDebugger synch does not work. The first time it does.
        // But the second time this is thrown:
        //----------------------------------------------------------------------
        //org.openide.debugger.DebuggerException: Exception while starting debugger: 
        //Cannot connect to remote VM.
        //java.lang.IllegalThreadStateException
        //        at org.netbeans.modules.debugger.jpda.JPDADebugger.connect(JPDADebugger.java:1115)
        //        at org.netbeans.modules.debugger.jpda.JPDADebugger.connect(JPDADebugger.java:1043)
        //        at org.netbeans.modules.debugger.jpda.JPDADebugger.startDebugger(JPDADebugger.java:253)
        //        at org.netbeans.modules.debugger.multisession.EnterpriseDebugger.startDebugger(EnterpriseDebugger.java:208)
        //        at org.netbeans.modules.debugger.jpda.ant.JPDAConnect.execute(JPDAConnect.java:63)
        //        ... 4 more
        //Caused by: java.lang.IllegalThreadStateException
        //        at java.lang.ThreadGroup.add(ThreadGroup.java:744)
        //        at java.lang.ThreadGroup.<init>(ThreadGroup.java:106)
        //        at com.sun.tools.jdi.VirtualMachineImpl.<init>(VirtualMachineImpl.java:145)
        //        at com.sun.tools.jdi.VirtualMachineManagerImpl.createVirtualMachine(VirtualMachineManagerImpl.java:203)
        //        at com.sun.tools.jdi.VirtualMachineManagerImpl.createVirtualMachine(VirtualMachineManagerImpl.java:211)
        //        at com.sun.tools.jdi.SocketAttachingConnector.attach(SocketAttachingConnector.java:66)
        //        at org.netbeans.modules.debugger.jpda.JPDADebugger.connect(JPDADebugger.java:1106)
        //        ... 8 more
        //--- Nested Exception ---
        //org.openide.debugger.DebuggerException: Exception while starting debugger: 
        //Cannot connect to remote VM.
        //java.lang.IllegalThreadStateException
        //        at org.netbeans.modules.debugger.jpda.JPDADebugger.connect(JPDADebugger.java:1115)
        //        at org.netbeans.modules.debugger.jpda.JPDADebugger.connect(JPDADebugger.java:1043)
        //        at org.netbeans.modules.debugger.jpda.JPDADebugger.startDebugger(JPDADebugger.java:253)
        //        at org.netbeans.modules.debugger.multisession.EnterpriseDebugger.startDebugger(EnterpriseDebugger.java:208)
        //        at org.netbeans.modules.debugger.jpda.ant.JPDAConnect.execute(JPDAConnect.java:63)
        //        at org.apache.tools.ant.Task.perform(Task.java:341)
        //        at org.apache.tools.ant.taskdefs.Sequential.execute(Sequential.java:117)
        //        at org.apache.tools.ant.Task.perform(Task.java:341)
        //        at org.apache.tools.ant.taskdefs.Parallel$TaskThread.run(Parallel.java:177)
        //Caused by: java.lang.IllegalThreadStateException
        //        at java.lang.ThreadGroup.add(ThreadGroup.java:744)
        //        at java.lang.ThreadGroup.<init>(ThreadGroup.java:106)
        //        at com.sun.tools.jdi.VirtualMachineImpl.<init>(VirtualMachineImpl.java:145)
        //        at com.sun.tools.jdi.VirtualMachineManagerImpl.createVirtualMachine(VirtualMachineManagerImpl.java:203)
        //        at com.sun.tools.jdi.VirtualMachineManagerImpl.createVirtualMachine(VirtualMachineManagerImpl.java:211)
        //        at com.sun.tools.jdi.SocketAttachingConnector.attach(SocketAttachingConnector.java:66)
        //        at org.netbeans.modules.debugger.jpda.JPDADebugger.connect(JPDADebugger.java:1106)
        //        ... 8 more
        //----------------------------------------------------------------------
        // Diagnosis: JDI starts its thread group as a child group of the one Ant
        // is running in - which is destroyed when the Ant script finishes.
        // We need to be running in a permanent thread group when JDI is started.
        // RequestProcessor threads are always in the master thread group.
        synchronized (exc) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    //System.err.println("TG: " + Thread.currentThread().getThreadGroup());
                    // VirtualMachineManagerImpl can be initialized here, so needs
                    // to be inside RP thread.
                    DebuggerInfo info = new RemoteDebuggerInfo(host, port, cp, bootcp);
                    synchronized (exc) {
                        try {
                            d.startDebugger(info);
                        } catch (DebuggerException e) {
                            exc[0] = e;
                        } finally {
                            exc.notify();
                        }
                    }
                }
            });
            try {
                exc.wait();
            } catch (InterruptedException e) {
                throw new BuildException(e);
            }
        }
        if (exc[0] != null) {
            // XXX certain kinds of exceptions could be more nicely reported
            throw new BuildException("Could not attach JPDA debugger to " + host + ":" + port, exc[0], getLocation());
        } else {
            log("Attached JPDA debugger to " + host + ":" + port);
        }
    }
*/

}
