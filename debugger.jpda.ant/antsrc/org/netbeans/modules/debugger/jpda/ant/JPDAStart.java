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
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.debugger.AbstractDebugger;
import org.netbeans.modules.debugger.Register;
import org.netbeans.modules.debugger.jpda.ListeningDebuggerInfo;
import org.netbeans.modules.debugger.jpda.RemoteDebuggerInfo;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.ErrorManager;
import org.openide.debugger.DebuggerException;
import org.openide.debugger.DebuggerInfo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;

/**
 * Ant task to start the NetBeans JPDA debugger in listening mode.
 * @author Jesse Glick, David Konecny
 */
public class JPDAStart extends Task {

    /** Name of the property to which the JPDA address will be set. 
     * Target VM should use this address and connect to it
     */
    private String addressProperty;

    /** Default transport is socket*/
    private String transport = "dt_socket";
        
    /** Name which will represent this debugging session in debugger UI.
     * If known in advance it should be name of the app which will be debugged.
     */
    private String name;
    
    /** Explicit sourcepath of the debugged process. */
    private Path sourcepath = null;
    
    /** Explicit classpath of the debugged process. */
    private Path classpath = null;
    
    /** Explicit bootclasspath of the debugged process. */
    private Path bootclasspath = null;
    
    public void setAddressProperty(String propertyName) {
        this.addressProperty = propertyName;
    }
    
    private String getAddressProperty() {
        return addressProperty;
    }
    
    public void setTransport(String transport) {
        this.transport = transport;
    }
    
    private String getTransport() {
        return transport;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    private String getName() {
        return name;
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
    
    public void execute() throws BuildException {
        
        if (name == null) {
            throw new BuildException("name attribute must specify name of this debugging session", getLocation());
        }
        
        if (addressProperty == null) {
            throw new BuildException("addressproperty attribute must specify name of property to which address will be set", getLocation());
        }
        
        final DebuggerException[] exc = new DebuggerException[1];
        final AbstractDebugger d = Register.getCoreDebugger();
        assert d != null;
        
        // Starting debugger second time thrown:
        //----------------------------------------------------------------------
        //org.openide.debugger.DebuggerException: Exception while starting debugger: 
        //Cannot connect to remote VM.
        //java.lang.IllegalThreadStateException
        //        at org.netbeans.modules.debugger.jpda.JPDADebugger.connect(JPDADebugger.java:1115)
        //        at org.netbeans.modules.debugger.jpda.JPDADebugger.connect(JPDADebugger.java:1043)
        //        ... 4 more
        //Caused by: java.lang.IllegalThreadStateException
        //        at java.lang.ThreadGroup.add(ThreadGroup.java:744)
        //        at java.lang.ThreadGroup.<init>(ThreadGroup.java:106)
        //        at com.sun.tools.jdi.VirtualMachineImpl.<init>(VirtualMachineImpl.java:145)
        //        at com.sun.tools.jdi.VirtualMachineManagerImpl.createVirtualMachine(VirtualMachineManagerImpl.java:203)
        //        ... 8 more
        //----------------------------------------------------------------------
        // Diagnosis: JDI starts its thread group as a child group of the one Ant
        // is running in - which is destroyed when the Ant script finishes.
        // We need to be running in a permanent thread group when JDI is started.
        // RequestProcessor threads are always in the master thread group.
        
        final List SYNCH = new ArrayList();
        final String trans = getTransport();
        final String nm = getName();
        ClassPath cp_ = (classpath == null) ? null : createSourceClassPath(classpath);
        if (sourcepath != null) {
            cp_ = appendPath(cp_, sourcepath);
        }
        final ClassPath cp = cp_;
        final ClassPath bootcp = (bootclasspath == null) ? null : createSourceClassPath(bootclasspath);
        
        synchronized (SYNCH) {
            try {

                // post task which setup debugger into listening mode
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        
                        // first setup listening address
                        // and notify Task
                        ListeningDebuggerInfo info;
                        synchronized (SYNCH) {
                            try {
                                info = new ListeningDebuggerInfo(nm, trans, cp, bootcp);
                                SYNCH.add(info);
                            } catch (Exception e) {
                                SYNCH.add(new BuildException("Could not start debugger.", e, getLocation()));
                                return;
                            } finally {
                                // notify Task that address is ready
                                SYNCH.notify();
                            }
                        }
                        
                        // now start listening
                        try {
                            d.startDebugger(info);
                        } catch (DebuggerException e) {
                            ErrorManager.getDefault().log(ErrorManager.ERROR, "Could not start debugger. "+e.toString());
                            return;
                        }
                    }
                });
                
                try {
                    // wait till the debugger initialized port to which
                    // target VM can connect
                    SYNCH.wait();
                } catch (InterruptedException ie) {
                    throw new BuildException(ie);
                }
                
                assert SYNCH.size() == 1;
                if (SYNCH.get(0) instanceof BuildException) {
                    throw (BuildException)SYNCH.get(0);
                }
                assert SYNCH.get(0) instanceof ListeningDebuggerInfo;
                ListeningDebuggerInfo info = (ListeningDebuggerInfo)SYNCH.get(0);
                log("Set "+getAddressProperty()+"="+info.getAddress(), Project.MSG_VERBOSE);
                getProject().setNewProperty(getAddressProperty(), info.getAddress());
                log("Target VM can now connect to address "+info.getAddress()+" using "+info.getTransport()+" transport.");
            } catch (Exception e) {
                throw new BuildException("Could not start debugger.", e, getLocation());
            }
        }
        
    }

    /**
     * This method uses SourceForBinaryQuery to find sources for each
     * path item and returns them as ClassPath instance. All path items for which
     * the sources were not found are omitted.
     *
     */
    public static ClassPath createSourceClassPath(Path path) {
        String[] paths = path.list();
        List l = new ArrayList();
        List exist = new ArrayList();
        for (int i=0; i<paths.length; i++) {
            URL u = null;
            try {
                File f = FileUtil.normalizeFile(new File(paths[i]));
                if (paths[i].toLowerCase().endsWith(".jar")) {
                    u = new URL("jar:" + f.toURI() + "!/");
                } else {
                    u = f.toURI().toURL();
                }
            } catch (MalformedURLException e) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
                continue;
            }
            FileObject fos[] = SourceForBinaryQuery.findSourceRoot(u);
            if (fos.length > 0) {
                try {
                    u = FileUtil.toFile(fos[0]).toURI().toURL();
                } catch (MalformedURLException e) {
                    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
                    continue;
                }
                if (!exist.contains(u)) {
                    l.add(ClassPathSupport.createResource(u));
                    exist.add(u);
                }
            }
        }
        return ClassPathSupport.createClassPath(l);
    }

    /**
     * Appends to classpath all items from the given path.
     *
     * @param cp classpath; can be null
     */
    public static ClassPath appendPath(ClassPath cp, Path path) {
        String[] paths = path.list();
        List l = new ArrayList();
        List exist = new ArrayList();
        for (int i=0; i<paths.length; i++) {
            URL u = null;
            try {
                u = FileUtil.normalizeFile(new File(paths[i])).toURI().toURL();
            } catch (MalformedURLException e) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
                continue;
            }
            l.add(ClassPathSupport.createResource(u));
        }
        if (cp != null) {
            return ClassPathSupport.createProxyClassPath(new ClassPath[]{cp, ClassPathSupport.createClassPath(l)});
        } else {
            return ClassPathSupport.createClassPath(l);
        }
    }
    
}
