/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.debugger.jpda.ant;

import java.io.File;
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

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.jpda.DebuggerStartException;

import org.openide.ErrorManager;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.RequestProcessor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.ExceptionBreakpoint;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.source.BuildArtifactMapper;
import org.netbeans.api.java.source.BuildArtifactMapper.ArtifactsUpdated;
import org.openide.util.NbBundle;


/**
 * Ant task to start the NetBeans JPDA debugger in listening mode.
 *
 * @author Jesse Glick, David Konecny
 */
public class JPDAStart extends Task implements Runnable {

    private static final Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.ant"); // NOI18N

    private static final String SOCKET_TRANSPORT = "dt_socket"; // NOI18N
    private static final String SHMEM_TRANSPORT = "dt_shmem"; // NOI18N
    
    /** Name of the property to which the JPDA address will be set.
     * Target VM should use this address and connect to it
     */
    private String                  addressProperty;
    /** Default transport is socket*/
    private String                  transport = SOCKET_TRANSPORT;
    /** Name which will represent this debugging session in debugger UI.
     * If known in advance it should be name of the app which will be debugged.
     */
    private String                  name;
    /** Explicit sourcepath of the debugged process. */
    private Sourcepath              sourcepath = null;
    /** Explicit classpath of the debugged process. */
    private Path                    classpath = null;
    /** Explicit bootclasspath of the debugged process. */
    private Path                    bootclasspath = null;
    private Object []               lock = null; 
    /** The class debugger should stop in, or null. */
    private String                  stopClassName = null;
    private String                  listeningCP = null;

    
    // properties ..............................................................
    
    public void setAddressProperty (String propertyName) {
        this.addressProperty = propertyName;
    }
    
    private String getAddressProperty () {
        return addressProperty;
    }
    
    public void setTransport (String transport) {
        logger.fine("Set transport: '"+transport+"'");
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

    public void setListeningcp(String listeningCP) {
        this.listeningCP = listeningCP;
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
    
    public void addSourcepath (Sourcepath path) {
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
                transport = SOCKET_TRANSPORT;
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
                        } else if (lock[1] instanceof ThreadDeath) {
                            throw (ThreadDeath) lock[1];
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
                    ListeningConnector llc = (ListeningConnector) i.next ();
                    Transport t = llc.transport ();
                    if (t != null && t.name ().equals (transport)) {
                        lc = llc;
                        break;
                    }
                }
                if (lc == null) 
                    throw new BuildException
                        ("No trasports named " + transport + " found!");

                logger.fine("Listening using transport "+transport);

                final Map args = lc.defaultArguments ();
                String address = null;
                try {
                    address = lc.startListening (args);
                } catch (java.io.IOException ioex) {
                    boolean passed = false;
                    // workaround for issue 148490
                    if (SHMEM_TRANSPORT.equals(transport)) {
                        Connector.StringArgument argName = (Connector.StringArgument) args.get("name"); // NOI18N
                        for (int x = 0; x < 5; x++) {
                            String tryAddress = "javadebug" + Math.round(Math.random() * 10000); // NOI18N
                            try {
                                argName.setValue (tryAddress);
                                address = lc.startListening (args);
                                passed = true;
                                break;
                            } catch (Exception e) {
                                // ignore
                            }
                        } // for
                    }
                    if (!passed) {
                        getProject().log("Listening failed with arguments: "+args);
                        throw ioex;
                    }
                } catch (com.sun.jdi.connect.IllegalConnectorArgumentsException iaex) {
                    getProject().log("Listening failed with arguments: "+args);
                    throw iaex;
                }
                /* A fix to bug http://developer.java.sun.com/developer/bugParade/bugs/4932074.html has been integrated into JDK 1.5
                // Uncomment if the fix is not complete in all cases
                // This code parses the address string "HOST:PORT" to extract PORT and then point debugee to localhost:PORT
                // This is NOT a clean solution to the problem but it SHOULD work in 99% cases
                if (SOCKET_TRANSPORT.equals(transport)) {
                    int port = -1;
                    try {
                        port = Integer.parseInt (address.substring (address.indexOf (':') + 1));
                        Connector.IntegerArgument portArg = (Connector.IntegerArgument) args.get("port"); // NOI18N
                        portArg.setValue (port);
                        address = "localhost:" + port; // NOI18N
                    } catch (Exception e) {
                        // this address format is not known, use default
                    }
                }*/
                if (SOCKET_TRANSPORT.equals(transport)) {
                    try {
                        int port = Integer.parseInt (address.substring (address.indexOf (':') + 1));
                        Connector.IntegerArgument portArg = (Connector.IntegerArgument) args.get("port"); // NOI18N
                        portArg.setValue (port);
                        // Since some users have badly configured host addresses,
                        // perform a check for the address and use "localhost"
                        // if the address can not be resolved: (see http://www.netbeans.org/issues/show_bug.cgi?id=154974)
                        String host = address.substring(0, address.indexOf (':'));
                        try {
                            InetAddress.getByName(host);
                        } catch (UnknownHostException uhex) {
                            address = "localhost:" + port; // NOI18N
                        } catch (SecurityException  se) {}
                    } catch (Exception e) {
                        // ignore
                    }
                }
                if (SHMEM_TRANSPORT.equals(transport)) {
                    try {
                        Connector.StringArgument name = (Connector.StringArgument) args.get("name"); // NOI18N
                        name.setValue (address);
                    } catch (Exception e) {
                        // ignore
                    }
                }
                getProject ().setNewProperty (getAddressProperty (), address);

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
                
                Breakpoint first = null;
                
                if (stopClassName != null && stopClassName.length() > 0) {
                    logger.fine(
                            "create method breakpoint, class name = " + // NOI18N
                            stopClassName
                        );
                    first = createBreakpoint (stopClassName);
                }                
                
                debug ("Debugger started"); // NOI18N
                logger.fine("start listening at " + address); // NOI18N
                
                final Map properties = new HashMap ();
                // uncomment to implement smart stepping with step-outs 
                // rather than step-ins (for J2ME)
                // props.put("SS_ACTION_STEPOUT", Boolean.TRUE);
                properties.put ("sourcepath", sourcePath); // NOI18N
                properties.put ("name", getName ()); // NOI18N
                properties.put ("jdksources", jdkSourcePath); // NOI18N
                properties.put ("listeningCP", listeningCP); // NOI18N
                String workDir = getProject().getProperty("work.dir");
                File baseDir;
                if (workDir != null) {
                    baseDir = new File(workDir);
                } else {
                    baseDir = getProject().getBaseDir();
                }
                properties.put ("baseDir", baseDir); // NOI18N

                logger.fine("JPDAStart: properties = "+properties);

                final ListeningConnector flc = lc;
                final WeakReference<Session> startedSessionRef[] = new WeakReference[] { new WeakReference<Session>(null) };
                
                Map<URL, ArtifactsUpdated> listeners = new HashMap<URL, ArtifactsUpdated>();
                List<Breakpoint> artificialBreakpoints = new LinkedList<Breakpoint>();
                if (listeningCP != null) {
                    ExceptionBreakpoint b = createCompilationErrorBreakpoint();
                    DebuggerManager.getDebuggerManager ().addBreakpoint (b);
                    artificialBreakpoints.add(b);
                }

                DebuggerManager.getDebuggerManager().addDebuggerListener(
                        DebuggerManager.PROP_DEBUGGER_ENGINES,
                        new Listener(first, artificialBreakpoints, listeners, startedSessionRef));

                // Let it start asynchronously so that the script can go on and start the debuggee
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        DebuggerManagerListener sessionListener = new DebuggerManagerAdapter() {
                            @Override
                            public void sessionAdded(Session session) {
                                synchronized (startedSessionRef) {
                                    // TODO: make that more deterministic.
                                    startedSessionRef[0] = new WeakReference(session);
                                }
                            }
                        };
                        try {
                            DebuggerManager.getDebuggerManager().addDebuggerListener(sessionListener);
                            JPDADebugger.startListening (
                                flc,
                                args,
                                new Object[] { properties }
                            );
                        } catch (DebuggerStartException dsex) {
                            // Was not able to start up
                        } finally {
                            DebuggerManager.getDebuggerManager().removeDebuggerListener(sessionListener);
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
                        Session s = startedSessionRef[0].get();
                        if (s != null) {
                            s.kill();
                        }
                    }
                    
                });
            } catch (java.io.IOException ioex) {
                lock[1] = ioex;
            } catch (com.sun.jdi.connect.IllegalConnectorArgumentsException icaex) {
                lock[1] = icaex;
            } catch (ThreadDeath td) {
                // Session was canceled - see issue #148483
                lock[1] = td;
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

    private ExceptionBreakpoint createCompilationErrorBreakpoint() {
        ExceptionBreakpoint b = ExceptionBreakpoint.create("java.lang.RuntimeException", ExceptionBreakpoint.TYPE_EXCEPTION_UNCATCHED);
        b.setHidden (true);
        b.addJPDABreakpointListener(new JPDABreakpointListener() {
            public void breakpointReached(JPDABreakpointEvent event) {
                boolean suspend = false;
                try {
                    if (event.getVariable() instanceof ObjectVariable) {
                        ObjectVariable ov = (ObjectVariable) event.getVariable();
                        JPDAClassType ct = ov.getClassType();
                        if (ct != null) {
                            suspend = "java.lang.RuntimeException".equals(ct.getName());
                            if (suspend) {
                                java.lang.reflect.Method invokeMethodMethod = ov.getClass().getMethod("invokeMethod", JPDAThread.class, String.class, String.class, Variable[].class);
                                invokeMethodMethod.setAccessible(true);
                                Variable message = (Variable) invokeMethodMethod.invoke(ov, event.getThread(), "getMessage", "()Ljava/lang/String;", new Variable[0]);
                                if (message != null) {
                                    suspend = message.getValue().startsWith("\"Uncompilable source code");
                                }
                                //suspend = suspend && ov.invokeMethod("getMessage", "()Ljava/lang/String;", new Variable[0]).getValue().startsWith("\"Uncompilable source code");
                            }
                        }
                    }
                } catch (IllegalAccessException iaex) {
                    logger.log(Level.FINE, null, iaex);
                } catch (java.lang.reflect.InvocationTargetException itex) {
                    logger.log(Level.FINE, null, itex);
                } catch (NoSuchMethodException ex) {
                    logger.log(Level.FINE, null, ex);
                //} catch (InvalidExpressionException ex) {
                //    logger.log(Level.FINE, null, ex);
                }

                if (!suspend) {
                    event.resume();
                }
            }
        });
        b.setPrintText(NbBundle.getBundle("org/netbeans/modules/debugger/jpda/ant/Bundle").getString("MSG_StoppedOnCompileError"));
        return b;
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
        Sourcepath sourcepath
    ) {
        if (sourcepath != null && sourcepath.isExclusive()) {
            return convertToClassPath (project, sourcepath);
        }
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
                SourceForBinaryQuery.Result srcRootsResult = SourceForBinaryQuery.findSourceRoots(url);
                FileObject fos[] = srcRootsResult.getRoots();
                int j, jj = fos.length;
                logger.fine("  source roots = "+java.util.Arrays.asList(fos)+"; jj = "+jj);
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
            if (fileObject == null) {
                project.log("Have no FileObject for "+file.getAbsolutePath(), Project.MSG_WARN);
                return null;
            }
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

    public static class Sourcepath extends Path {

        private boolean isExclusive;

        public Sourcepath(Project p) {
            super(p);
        }

        public void setExclusive(String exclusive) {
            isExclusive = "true".equalsIgnoreCase(exclusive);
        }

        boolean isExclusive() {
            return isExclusive;
        }
    }
    
    private static class Listener extends DebuggerManagerAdapter {
        
        private Set                 debuggers = new HashSet ();

        private Breakpoint first;
        private final List<Breakpoint> artificalBreakpoints;
        private final Map<URL, ArtifactsUpdated> listeners;
        private final WeakReference<Session> startedSessionRef[];
        
        private Listener(Breakpoint first,
                         List<Breakpoint> artificalBreakpoints,
                         Map<URL, ArtifactsUpdated> listeners,
                         WeakReference<Session> startedSessionRef[]) {
            this.first = first;
            this.artificalBreakpoints = artificalBreakpoints;
            this.listeners = listeners;
            this.startedSessionRef = startedSessionRef;
        }
        
        @Override
        public void propertyChange (final PropertyChangeEvent e) {
            if (JPDADebugger.PROP_STATE.equals(e.getPropertyName ())) {
                int state = ((Integer) e.getNewValue ()).intValue ();
                if (state == JPDADebugger.STATE_STOPPED) {
                    RequestProcessor.getDefault().post(new Runnable() {
                        public void run() {
                            if (first != null) {
                                DebuggerManager.getDebuggerManager().removeBreakpoint(first);
                                first = null;
                                ((JPDADebugger) e.getSource()).removePropertyChangeListener(Listener.this);
                            }
                        }
                    });
                }
            }
            return;
        }

        private void dispose() {
            DebuggerManager.getDebuggerManager ().removeDebuggerListener (
                DebuggerManager.PROP_DEBUGGER_ENGINES,
                this
            );
            RequestProcessor.getDefault ().post (new Runnable () {
                public void run () {
                    if (artificalBreakpoints != null) {
                        for (Breakpoint b : artificalBreakpoints) {
                            DebuggerManager.getDebuggerManager().removeBreakpoint(b);
                        }
                    }
                    if (first != null) {
                        DebuggerManager.getDebuggerManager().removeBreakpoint(first);
                    }
                    if (listeners != null) {
                        for (Entry<URL, ArtifactsUpdated> e : listeners.entrySet()) {
                            BuildArtifactMapper.removeArtifactsUpdatedListener(e.getKey(), e.getValue());
                        }
                    }
                }
            });
        }
        
        public void engineAdded (DebuggerEngine engine) {
            // Consider only engines from the started session.
            Session s;
            synchronized (startedSessionRef) {
                s = startedSessionRef[0].get();
            }
            if (s == null) {
                return ;
            }
            boolean haveEngine = false;
            for (String l : s.getSupportedLanguages()) {
                if (engine.equals(s.getEngineForLanguage(l))) {
                    haveEngine = true;
                    break;
                }
            }
            if (!haveEngine) {
                return ;
            }
            JPDADebugger debugger = engine.lookupFirst(null, JPDADebugger.class);
            if (debugger == null) return;
            debugger.addPropertyChangeListener (
                JPDADebugger.PROP_STATE,
                this
            );
            debuggers.add (debugger);
        }
        
        public void engineRemoved (DebuggerEngine engine) {
            JPDADebugger debugger = engine.lookupFirst(null, JPDADebugger.class);
            if (debugger == null) return;
            if (debuggers.remove (debugger) && debuggers.isEmpty()) {
                debugger.removePropertyChangeListener (
                    JPDADebugger.PROP_STATE,
                    this
                );
                dispose();
            }
        }
    }

}
