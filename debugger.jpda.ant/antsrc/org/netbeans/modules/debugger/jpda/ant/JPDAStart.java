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
import org.apache.tools.ant.types.Path;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.jpda.ListeningDICookie;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.connect.ListeningConnector;
import com.sun.jdi.connect.Transport;
import com.sun.jdi.connect.Connector;

/**
 * Ant task to start the NetBeans JPDA debugger in listening mode.
 * @author Jesse Glick, David Konecny
 */
public class JPDAStart extends Task {

    private static final boolean verbose = System.getProperty ("netbeans.debugger.debug") != null;

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

        debug("Execute started");

        if (name == null) {
            throw new BuildException("name attribute must specify name of this debugging session", getLocation());
        }

        if (addressProperty == null) {
            throw new BuildException("addressproperty attribute must specify name of property to which address will be set", getLocation());
        }

        if (transport == null) {
            transport = "dt_socket";
        }

        debug("Entering synch lock");

        final Object [] lock = new Object[2];
        synchronized (lock)
        {
            debug("Entered synch lock");
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    debug("Entering synch lock");
                    synchronized (lock) {
                        debug("Entered synch lock");
                        try {

                            ListeningConnector lc = null;
                            for (Iterator i = Bootstrap.virtualMachineManager().listeningConnectors().iterator (); i.hasNext(); ) {
                                lc = (ListeningConnector) i.next();
                                Transport t = lc.transport();
                                if (t != null && t.name().equals(transport)) break;
                            }
                            if (lc == null) throw new BuildException("No trasports named " + transport + " found!");

                            // TODO: revisit later when http://developer.java.sun.com/developer/bugParade/bugs/4932074.html gets integrated into JDK
                            // This code parses the address string "HOST:PORT" to extract PORT and then point debugee to localhost:PORT
                            // This is NOT a clean solution to the problem but it SHOULD work in 99% cases
                            Map args = lc.defaultArguments();
                            String address = lc.startListening(args);
                            try
                            {
                                int port = Integer.parseInt(address.substring(address.indexOf(':') + 1));
                                getProject().setNewProperty(getAddressProperty(), "localhost:" + port);
                                Connector.IntegerArgument portArg = (Connector.IntegerArgument) args.get("port");
                                portArg.setValue(port);
                            }
                            catch (Exception e)
                            {
                                // this address format is not known, use default
                                getProject().setNewProperty(getAddressProperty(), address);
                            }

                            debug("Creating source path");
                            ClassPath sessionSourcePath = (classpath == null) ? null : createSourceClassPath(classpath);
                            if (sourcepath != null) {
                                sessionSourcePath = appendPath(sessionSourcePath, sourcepath);
                            }
//                            ClassPath bootcp = (bootclasspath == null) ? null : createSourceClassPath(bootclasspath);

                            debug("Creating cookie");
                            ListeningDICookie ldic = ListeningDICookie.create(lc, args);
                            final DebuggerInfo di = DebuggerInfo.create(ListeningDICookie.ID, new Object [] { ldic, sessionSourcePath });

                            DebuggerManager.getDebuggerManager().startDebugging(di);
                            debug("Debugger started");
                        }
                        catch (Throwable e) {
                            lock[1] = e;
                        }
                        finally  {
                            debug("Notifying");
                            lock.notify();
                        }
                    }
                }
            });
            try {
                debug("Entering wait");
                lock.wait();
                debug("Wait finished");
                if (lock[1] != null) {
                    throw new BuildException((Throwable)lock[1]);
                }
            } catch (InterruptedException e) {
                throw new BuildException(e);
            }
        }

    }

    private void debug(String msg) {
        if (!verbose) return;
        System.out.println(new Date() + " [" + Thread.currentThread().getName() + "] - " + msg);
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
                u = new File(paths[i]).toURI().toURL();
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
                u = new File(paths[i]).toURI().toURL();
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
