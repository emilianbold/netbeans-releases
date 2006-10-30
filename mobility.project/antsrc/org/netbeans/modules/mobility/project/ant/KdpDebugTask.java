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

/*
 * Created on Apr 27, 2004
 */
package org.netbeans.modules.mobility.project.ant;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Location;
import com.sun.jdi.Method;

/**
 * @author kh148139
 *
 * Ant task to try connecting the JPDADebugger to the KDP
 * for a period of time. This behaviour is required because the KDP takes some time to start up
 * and emulators mostly support debugging in listening (server) mode.
 *
 * <p>Attributes:<ol>
 * <li>delay - Optional. Amount of time in ms for which the task delay before trying to connect the debugger to the KDP for the first time. Default value: 3000ms
 * <li>timeout - Optional. Amount of time in ms for which the task will keep trying to connect the debugger to the KDP. Default value: 20000ms
 * <li>period - Optional. Amount of time in ms to wait between connection attempts. Default value: 1000ms
 * </ol></p>
 */
public class KdpDebugTask extends Task {
    
    private static final boolean    startVerbose = System.getProperty("netbeans.debugger.start") != null; //NOI18N
    
    private long startTime;
    private long delay = 5000;
    private long timeout = 45000;
    private long period = 2000;
    private String host = "localhost"; //NOI18N
    
    private String address;
    /**
     * Explicit sourcepath of the debugged process.
     */
    private Path sourcepath = null;
    /**
     * Explicit classpath of the debugged process.
     */
    private Path classpath = null;
    /**
     * Explicit bootclasspath of the debugged process.
     */
    private Path bootclasspath = null;
    /**
     * Name which will represent this debugging session in debugger UI.
     * If known in advance it should be name of the app which will be debugged.
     */
    private String name;
    
    /**
     * Default transport is socket
     */
    private String transport = "dt_socket"; //NOI18N
    
    /**
     * @param periodMS The periodMS to set.
     */
    public void setPeriod(long periodMS) {
        this.period = periodMS;
    }
    
    /**
     * @param timeoutMS The timeoutMS to set.
     */
    public void setTimeout(long timeoutMS) {
        this.timeout = timeoutMS;
    }
    
    /**
     * @param delay The delay to set.
     */
    public void setDelay(long delay) {
        this.delay = delay;
    }
    
    /**
     * Host to connect to.
     * By default, localhost.
     */
    public void setHost(String h) {
        host = h;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    
    public void addClasspath(Path path) {
        if (classpath != null)
            throw new BuildException(NbBundle.getMessage(KdpDebugTask.class, "ERR_ANT_only_one_classpath_subelement"));//NOI18N
        classpath = path;
    }
    
    public void addBootclasspath(Path path) {
        if (bootclasspath != null)
            throw new BuildException(NbBundle.getMessage(KdpDebugTask.class, "ERR_ANT_only_one_bootclasspath_subelement"));//NOI18N
        bootclasspath = path;
    }
    
    public void addSourcepath(Path path) {
        if (sourcepath != null)
            throw new BuildException(NbBundle.getMessage(KdpDebugTask.class, "ERR_ANT_only_one_sourcepath_subelement"));//NOI18N
        sourcepath = path;
    }
    
    public void setTransport(String transport) {
        this.transport = transport;
    }
    
    
    public void setName(String name) {
        this.name = name;
    }
    
    private String getName() {
        return name;
    }
    
    public void execute() throws BuildException {
        //hack for Siemens copy operation takes too much time so wait longer
        String[] paths = classpath.list();
        long length = 0;
        for (int i = 0; i < paths.length; i++){
            File file = new File(paths[i]);
            if (!file.exists() || file.isDirectory())
                continue;
            length += (file.length()/1000L);
        }
        if (length > 50){ //more than 50K
            long newTimeoutAdd = (length - 50) * 1000;
            log (NbBundle.getMessage(KdpDebugTask.class, "ERR_ANT_Debugger_Add_time_out", Long.toString(newTimeoutAdd/1000)));            
            timeout = newTimeoutAdd + timeout;
        }
        //end
        ArrayList<String> stopMidletClasses = null;
        if (this.getProject().getProperty("debug.step.into") != null) { //NOI18N
            stopMidletClasses = new ArrayList<String>();
            String manifestMidlets = this.getProject().getProperty("manifest.midlets"); //NOI18N
            BufferedReader reader = new BufferedReader(new StringReader(manifestMidlets));
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    String[] lineData = line.split(","); // NOI18N
                    if (lineData.length != 3) {
                        if (startVerbose)
                            System.out.println("line \"" + line + "\" doesn't contain valid MIDlet data."); //NOI18N
                    } else {
                        if (startVerbose)
                            System.out.println("Adding STOP MIDlet class: " + lineData[2].trim()); //NOI18N
                        stopMidletClasses.add(lineData[2].trim());
                    }
                }
            } catch (IOException e) {
                throw new BuildException(NbBundle.getMessage(KdpDebugTask.class, "ERR_ANT_only_one_sourcepath_subelement"));//NOI18N
            }
        }
        boolean debuggerConnected = false;
        try {
            Thread.sleep(this.delay);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        this.startTime = System.currentTimeMillis();
        int attemptCount = 0;
        do {
            try {
                attemptCount++;
                this.runDebugger(stopMidletClasses, attemptCount);
                debuggerConnected = true;
            } catch (BuildException e) {
                try {
                    Thread.sleep(this.period += 1000);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }
        while (!debuggerConnected && (System.currentTimeMillis() < this.startTime + this.timeout));
        if (!debuggerConnected) {
            int attemptTime = (int) ((System.currentTimeMillis() - this.startTime) / 1000);
            log(NbBundle.getMessage(KdpDebugTask.class, "ERR_ANT_Debugger_timed_out", Integer.toString(attemptCount), Integer.toString(attemptTime)));
            throw new BuildException(NbBundle.getMessage(KdpDebugTask.class, "ERR_ANT_Debugger_timed_out", //NOI18N
                    Integer.toString(attemptCount), Integer.toString(attemptTime))); //NOI18N
        }
    }
    
//    private static boolean isExplicitCLInitPresent(ClassElement midletClass) {
//        if (midletClass == null) {
//            return false;
//        }
//        InitializerElement[] initializers = midletClass.getInitializers();
//        if (initializers == null)
//            return false;
//        for (int i = 0; i < initializers.length; i++) {
//            InitializerElement ie = initializers[i];
//            if (ie.isStatic())
//                return true;
//        }
//        return false;
//    }
//    
//    private static boolean isExplicitInitPresent(ClassElement midletClass) {
//        if (midletClass == null) {
//            return false;
//        }
//        InitializerElement[] initializers = midletClass.getInitializers();
//        if (initializers == null)
//            return false;
//        for (int i = 0; i < initializers.length; i++) {
//            InitializerElement ie = initializers[i];
//            if (!ie.isStatic())
//                return true;
//        }
//        return false;
//    }
//    
//    private static boolean isExplicitNoArgConstructorPresent(ClassElement midletClass) {
//        if (midletClass == null) {
//            return false;
//        }
//        ConstructorElement ce = midletClass.getConstructor(new Type[] {Type.VOID});
//        if (ce == null)
//            return true;
//        return false;
//    }
//    
//    private ClassElement getMIDletClassElement(String midletClassName, Project project, Path path) {
//        ClassElement midletClass = null;
//        midletClass = ClassElement.forName(midletClassName, FileUtil.toFileObject(new File(project.getBaseDir().getAbsolutePath() + "/src")));
//        return midletClass;
//    }
//    
//    private static boolean isStartAppPresent(ClassElement midletClass) {
//        if (midletClass == null) {
//            return false;
//        }
//        MethodElement[] methods = midletClass.getMethods();
//        
//        if (methods == null)
//            return false;
//        for (int i = 0; i < methods.length; i++) {
//            MethodElement me = methods[i];
//            if (me.getName().getName().equals("startApp")) //NOI18N
//                return true;
//        }
//        return false;
//    }
    
    
    public void runDebugger(final ArrayList<String> stopMidletClasses, final int attemptCount) throws BuildException {
        
        if (name == null)
            throw new BuildException(NbBundle.getMessage(KdpDebugTask.class, "ERR_ANT_Session_name_missing"), getLocation()); //NOI18N
        if (address == null)
            throw new BuildException(NbBundle.getMessage(KdpDebugTask.class, "ERR_ANT_Address_missing"), getLocation()); //NOI18N
        if (transport == null)
            transport = "dt_socket"; //NOI18N
        
        final Object[] lock = new Object[1];
        
        ClassPath sourcePath = createSourcePath(getProject(), classpath, sourcepath);
        ClassPath jdkSourcePath = createJDKSourcePath(getProject(), bootclasspath);
        if (startVerbose) {
            System.out.println("\nS Crete sourcepath: ***************"); //NOI18N
            System.out.println("    classpath : " + classpath); //NOI18N
            System.out.println("    sourcepath : " + sourcepath); //NOI18N
            System.out.println("    bootclasspath : " + bootclasspath); //NOI18N
            System.out.println("    >> sourcePath : " + sourcePath); //NOI18N
            System.out.println("    >> jdkSourcePath : " + jdkSourcePath); //NOI18N
        }
        final Map<String,Object> properties = new HashMap<String,Object>();
        properties.put("sourcepath", sourcePath); //NOI18N
        properties.put("name", getName()); //NOI18N
        properties.put("jdksources", jdkSourcePath); //NOI18N
        //J2ME specific - disables STEP-INTO on smart-stepping, instead if no source is found does a STEP_OUT
        properties.put("SS_ACTION_STEPOUT", Boolean.TRUE); //NOI18N
        properties.put("J2ME_DEBUGGER", Boolean.TRUE); //NOI18N
        
        synchronized (lock) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    synchronized (lock) {
                        final ArrayList<ClassLoadUnloadBreakpoint> classBreakpoints = new ArrayList<ClassLoadUnloadBreakpoint>();
                        try {
                            if (stopMidletClasses != null && stopMidletClasses.size() > 0) {
                                for (int i = 0; i < stopMidletClasses.size(); i++) {
                                    if (startVerbose)
                                        System.out.println("\tSETTING A CLASS PREPARE BREAKPOINT FOR CLASS : " + stopMidletClasses.get(i)); //NOI18N
                                    final ClassLoadUnloadBreakpoint classBreakpoint = ClassLoadUnloadBreakpoint.create(stopMidletClasses.get(i), false, ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED);
                                    classBreakpoint.setSuspend(JPDABreakpoint.SUSPEND_ALL);
                                    classBreakpoint.setHidden(true);
                                    classBreakpoint.enable();
                                    classBreakpoints.add(classBreakpoint);
                                    classBreakpoint.addJPDABreakpointListener(new JPDABreakpointListener() {
                                        public void breakpointReached(JPDABreakpointEvent event) {
                                            if (startVerbose)
                                                System.out.println("\tCLASS PREPARED BREAKPOINT reached in class - " + event.getReferenceType().name()); //NOI18N
                                            
                                            //remove all ClassLoadUnloadBreakpoints
                                            for (ClassLoadUnloadBreakpoint element : classBreakpoints) {
                                                DebuggerManager.getDebuggerManager().removeBreakpoint(element);
                                                if (startVerbose)
                                                    System.out.println("\t--REMOVED classBreakpoint : " + element.toString()); //NOI18N
                                            }
                                            
                                            ClassType classType = (ClassType) event.getReferenceType();
//                                            ClassElement midletClassElement = getMIDletClassElement(event.getReferenceType().name(), getProject(), classpath);
                                            
                                            Method entryMethod = null;
                                            
                                            
                                            //set hidden breakpoint in the startApp()
//                                            if (isStartAppPresent(midletClassElement)) {
//                                                entryMethod = classType.concreteMethodByName("startApp", "()V"); // NOI18N
//                                                if (startVerbose)
//                                                    System.out.println("entryMethod after found startApp : " + entryMethod); //NOI18N
//                                            }
//                                            
//                                            //the no-arg constructor will ALWAYS exist (either explicit or implicit) if the MIDlet is extended correctly
//                                            if (isExplicitNoArgConstructorPresent(midletClassElement)) {
//                                                entryMethod = classType.concreteMethodByName("<init>", "()V"); // NOI18N
//                                                if (startVerbose)
//                                                    System.out.println("entryMethod after <explicitNoArgConstructorPresent> : " + entryMethod); //NOI18N
//                                            }
//                                            
//                                            //try to find a initializer
//                                            if (isExplicitInitPresent(midletClassElement)) {
//                                                entryMethod = classType.concreteMethodByName("<init>", "()V"); // NOI18N
//                                                if (startVerbose)
//                                                    System.out.println("entryMethod after <explicitInitPresent> : " + entryMethod); //NOI18N
//                                            }
//                                            
//                                            //try to find a static initializer
//                                            if (isExplicitCLInitPresent(midletClassElement)) {
//                                                entryMethod = classType.concreteMethodByName("<clinit>", "()V"); // NOI18N
//                                                if (startVerbose)
//                                                    System.out.println("entryMethod after <explicitCLInitPresent> : " + entryMethod); //NOI18N
//                                            }
                                            
                                            //if unable to find any of the above
                                            if (entryMethod == null)
                                                entryMethod = classType.concreteMethodByName("<init>", "()V"); // NOI18N
                                            
                                            if (startVerbose)
                                                System.out.println("Setting entryMethod to : " + entryMethod.name()); //NOI18N
                                            try {
                                                List<Location> lineLocations = entryMethod.allLineLocations();
                                                Location location = lineLocations.get(0);
                                                final LineBreakpoint lineBreakpoint = LineBreakpoint.create(entryMethod.declaringType().name(), location.lineNumber());
                                                if (startVerbose)
                                                    System.out.println("\tSetting a line breakpoint in " + classType.name() + " on line " + location.lineNumber()); //NOI18N
                                                lineBreakpoint.setHidden(true);
                                                lineBreakpoint.enable();
                                                lineBreakpoint.addJPDABreakpointListener(new JPDABreakpointListener() {
                                                    public void breakpointReached(JPDABreakpointEvent event) {
                                                        if (startVerbose) {
                                                            System.out.println("EVENT: " + event); //NOI18N
                                                            System.out.println("\t\tLINE BREAKPOINT reached " + lineBreakpoint.toString() + "\n\t\t-REMOVING IT"); //NOI18N
                                                        }
                                                        DebuggerManager.getDebuggerManager().removeBreakpoint(lineBreakpoint);
                                                    }
                                                });
                                                DebuggerManager.getDebuggerManager().addBreakpoint(lineBreakpoint);
                                            } catch (AbsentInformationException e) {
                                                e.printStackTrace();
                                            }
                                            event.resume();
                                        }
                                        
                                    });
                                    DebuggerManager.getDebuggerManager().addBreakpoint(classBreakpoint);
                                }
                            }
                            log(NbBundle.getMessage(KdpDebugTask.class, "LBL_ANT_DebuggerConnecting", Integer.toString(attemptCount)));
                            
                            if (transport.equals("dt_socket")) { //NOI18N
                                try {
                                    JPDADebugger.attach(host, Integer.parseInt(address), new Object[]{properties});
                                } catch (NumberFormatException e) {
                                    throw new BuildException(NbBundle.getMessage(KdpDebugTask.class, "ERR_ANT_Address_missing"), getLocation()); //NOI18N
                                }
                            } else {
                                JPDADebugger.attach(address, new Object[]{properties});
                            }
                        } catch (Throwable e) {
                            lock[0] = e;
                        } finally {
                            //remove all existing ClassLoadUnloadBreakpoints if any
                            for ( ClassLoadUnloadBreakpoint element : classBreakpoints ) {
                                DebuggerManager.getDebuggerManager().removeBreakpoint(element);
                                if (startVerbose)
                                    System.out.println("\t-->>>>REMOVED classBreakpoint : " + element.toString()); //NOI18N
                            }
                            lock.notify();
                            
                        }
                    }
                }
            });
            try {
                lock.wait();
            } catch (InterruptedException e) {
                throw new BuildException(e);
            }
            if (lock[0] != null) {
                throw new BuildException((Throwable) lock[0]);
            }
        }
        if (host == null)
            log(NbBundle.getMessage(KdpDebugTask.class, "LBL_ANT_Debugger_attached", address)); //NOI18N
        else
            log(NbBundle.getMessage(KdpDebugTask.class, "LBL_ANT_Debugger_attached_with_host", host, address)); //NOI18N
    }
    
    static ClassPath createSourcePath(
            Project project,
            Path classpath,
            Path sourcepath
            ) {
        ClassPath cp = convertToSourcePath(project, classpath);
        ClassPath sp = convertToClassPath(project, sourcepath);
        
        ClassPath sourcePath = ClassPathSupport.createProxyClassPath(
                new ClassPath[] {cp, sp}
        );
        return sourcePath;
    }
    
    static ClassPath createJDKSourcePath(
            Project project,
            Path bootclasspath
            ) {
        if (bootclasspath == null)
            return JavaPlatform.getDefault().getSourceFolders();
        // if current platform is default one, bootclasspath is set to null
        return convertToSourcePath(project, bootclasspath);
    }
    
    private static ClassPath convertToClassPath(Project project, Path path) {
        String[] paths = path == null ? new String [0] : path.list();
        List<URL> l = new ArrayList<URL>();
        int i, k = paths.length;
        for (i = 0; i < k; i++) {
            File f = FileUtil.normalizeFile(project.resolveFile(paths [i]));
            if (!isValid(f, project)) continue;
            URL url = fileToURL(f);
            if (f == null) continue;
            l.add(url);
        }
        URL[] urls = l.toArray(new URL [l.size()]);
        return ClassPathSupport.createClassPath(urls);
    }
    
    /**
     * This method uses SourceForBinaryQuery to find sources for each
     * path item and returns them as ClassPath instance. All path items for which
     * the sources were not found are omitted.
     *
     */
    private static ClassPath convertToSourcePath(Project project, Path path) {
        String[] paths = path == null ? new String [0] : path.list();
        List<PathResourceImplementation> l = new ArrayList<PathResourceImplementation>();
        Set<URL> exist = new HashSet<URL>();
        int i, k = paths.length;
        for (i = 0; i < k; i++) {
            File file = FileUtil.normalizeFile
                    (project.resolveFile(paths [i]));
            if (!isValid(file, project)) continue;
            URL url = fileToURL(file);
            if (url == null) continue;
            try {
                FileObject fos[] = SourceForBinaryQuery.findSourceRoots
                        (url).getRoots();
                int j, jj = fos.length;
                for (j = 0; j < jj; j++) {
                    if (FileUtil.isArchiveFile(fos [j]))
                        fos [j] = FileUtil.getArchiveRoot(fos [j]);
                    try {
                        url = fos [j].getURL();
                    } catch (FileStateInvalidException ex) {
                        ErrorManager.getDefault().notify
                                (ErrorManager.EXCEPTION, ex);
                        continue;
                    }
                    if (url == null) continue;
                    if (!exist.contains(url)) {
                        l.add(ClassPathSupport.createResource(url));
                        exist.add(url);
                    }
                } // for
            } catch (IllegalArgumentException ex) {
            }
        }
        return ClassPathSupport.createClassPath(l);
    }
    
    
    private static URL fileToURL(File file) {
        try {
            FileObject fileObject = FileUtil.toFileObject(file);
            if (fileObject == null) return null;
            if (FileUtil.isArchiveFile(fileObject))
                fileObject = FileUtil.getArchiveRoot(fileObject);
            return fileObject.getURL();
        } catch (FileStateInvalidException e) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
            return null;
        }
    }
    
    private static boolean isValid(File f, Project project) {
        if (f.getPath().indexOf("${") != -1 && !f.exists()) { // NOI18N
            project.log(
                    "Classpath item " + f + " will be ignored.",  // NOI18N
                    Project.MSG_VERBOSE
                    );
            return false;
        }
        return true;
    }
    
}
