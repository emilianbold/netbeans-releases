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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.DebuggerStartException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import java.beans.PropertyChangeEvent;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.RequestProcessor;

/**
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
    
    private static final ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.modules.mobility.project.ant.KdpDebugTask"); //NOI18N
    
    private long startTime;
    private long delay = 5000;
    private long timeout = 45000;
    private long period = 2000;
    private String host = "localhost"; //NOI18N
    private String address;
    
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
    
    public void setTransport(String transport) {
        this.transport = transport;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void execute() throws BuildException {
        
        Project project = getProject();
        if (name == null) name = project.getProperty("app.codename"); //NOI18N
        if (name == null) throw new BuildException(NbBundle.getMessage(KdpDebugTask.class, "ERR_ANT_Session_name_missing"), getLocation()); //NOI18N
        if (address == null) throw new BuildException(NbBundle.getMessage(KdpDebugTask.class, "ERR_ANT_Address_missing"), getLocation()); //NOI18N
        int intAddr = 0;
        if (transport.equals("dt_socket")) try {
            intAddr = Integer.parseInt(address);
        } catch (NumberFormatException nfe) {
            throw new BuildException(NbBundle.getMessage(KdpDebugTask.class, "ERR_ANT_Address_missing"), getLocation()); //NOI18N
        }
        
        //locate source root
        String src = project.getProperty("src.dir"); //NOI18N
        if (src == null)  throw new BuildException(NbBundle.getMessage(KdpDebugTask.class, "ERR_ANT_source_root_missing"), getLocation()); //NOI18N
        File srcFile = new File(project.getBaseDir(), src);
        if (!srcFile.isDirectory()) srcFile = new File(src);
        final FileObject srcRoot = FileUtil.toFileObject(srcFile);
        if (!srcFile.isDirectory() || srcRoot == null)  throw new BuildException(NbBundle.getMessage(KdpDebugTask.class, "ERR_ANT_source_root_missing"), getLocation()); //NOI18N
        
        
        //adjust sleep delay when too big jar to deploy
        String dist = project.getProperty("dist.dir"); //NOI18N
        String jar = project.getProperty("dist.jar"); //NOI18N
        if (dist != null && jar != null) {
            File jarFile = new File(project.getBaseDir(), dist + '/' + jar);
            if (jarFile.isFile() && jarFile.length() > 50000) {
                long newTimeoutAdd = jarFile.length() - 50000;
                log(NbBundle.getMessage(KdpDebugTask.class, "ERR_ANT_Debugger_Add_time_out", Long.toString(newTimeoutAdd/1000)));
                timeout = newTimeoutAdd + timeout;
            }
        }
        
        //get platform source path
        ClassPath  jdkSourcePath = JavaPlatformManager.getDefault().getDefaultPlatform().getSourceFolders();
        String platform = project.getProperty("platform.active"); //NOI18N
        if (platform != null) for (JavaPlatform p : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
            if ( platform.equals(p.getProperties().get("platform.ant.name"))) { //NOI18N
                jdkSourcePath = p.getSourceFolders();
            }
        }
        
        //get source path
        ClassPath sourcePath = ClassPath.getClassPath(srcRoot, ClassPath.SOURCE);
        ClassPath libPath = ClassPath.getClassPath(srcRoot, ClassPath.COMPILE);
        if (libPath != null) {
            Set exist = new HashSet();
            HashSet<FileObject> resources = new HashSet();
            for (FileObject root : libPath.getRoots()) try {
                URL url = root.getURL();
                for (FileObject fos : SourceForBinaryQuery.findSourceRoots(url).getRoots()) {
                    if (FileUtil.isArchiveFile (fos)) fos = FileUtil.getArchiveRoot(fos);
                    resources.add(fos);
                }
            } catch (IllegalArgumentException ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            } catch (FileStateInvalidException fsie) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, fsie);
            }
            if (!resources.isEmpty()) {
                if (sourcePath != null) resources.addAll(Arrays.asList(sourcePath.getRoots()));
                sourcePath = ClassPathSupport.createClassPath(resources.toArray(new FileObject[resources.size()]));
            }
        }
        
        final Map<String,Object> properties = new HashMap<String,Object>();
        properties.put("sourcepath", sourcePath); //NOI18N
        properties.put("name", name); //NOI18N
        properties.put("jdksources", jdkSourcePath); //NOI18N
        //J2ME specific - disables STEP-INTO on smart-stepping, instead if no source is found does a STEP_OUT
        properties.put("SS_ACTION_STEPOUT", Boolean.TRUE); //NOI18N
        properties.put("J2ME_DEBUGGER", Boolean.TRUE); //NOI18N
        
        
        //sleep for defined delay
        try {
            Thread.sleep(this.delay);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        
        //start debugger
        this.startTime = System.currentTimeMillis();
        int attemptCount = 0;
        boolean debuggerConnected = false;
        do {
            try {
                attemptCount++;
                log(NbBundle.getMessage(KdpDebugTask.class, "LBL_ANT_DebuggerConnecting", Integer.toString(attemptCount)));
                
                if (transport.equals("dt_socket")) JPDADebugger.attach(host, intAddr, new Object[]{properties}); //NOI18N
                else JPDADebugger.attach(address, new Object[]{properties});
                
                if (host == null) log(NbBundle.getMessage(KdpDebugTask.class, "LBL_ANT_Debugger_attached", address)); //NOI18N
                else log(NbBundle.getMessage(KdpDebugTask.class, "LBL_ANT_Debugger_attached_with_host", host, address)); //NOI18N
                
                debuggerConnected = true;
            } catch (DebuggerStartException e) {
                try {
                    Thread.sleep(this.period += 1000);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        } while (!debuggerConnected && (System.currentTimeMillis() < this.startTime + this.timeout));
        
        if (!debuggerConnected) {
            int attemptTime = (int) ((System.currentTimeMillis() - this.startTime) / 1000);
            log(NbBundle.getMessage(KdpDebugTask.class, "ERR_ANT_Debugger_timed_out", Integer.toString(attemptCount), Integer.toString(attemptTime)));
            throw new BuildException(NbBundle.getMessage(KdpDebugTask.class, "ERR_ANT_Debugger_timed_out", //NOI18N
                    Integer.toString(attemptCount), Integer.toString(attemptTime))); //NOI18N
        }
    }
    
    private static class BreakManager extends DebuggerManagerAdapter implements JPDABreakpointListener {
        
        private Set<MethodBreakpoint>  breakpoints;
        private Set debuggers = new HashSet();
        
        
        BreakManager(Set<MethodBreakpoint> breakpoints) {
            this.breakpoints = breakpoints;
            DebuggerManager dm = DebuggerManager.getDebuggerManager();
            dm.addDebuggerListener(DebuggerManager.PROP_DEBUGGER_ENGINES, this);
            for (MethodBreakpoint breakpoint : breakpoints) {
                breakpoint.addJPDABreakpointListener(this);
                dm.addBreakpoint(breakpoint);
            }
        }
        
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName() == JPDADebugger.PROP_STATE) {
                int state = ((Integer) e.getNewValue()).intValue();
                if ((state == JPDADebugger.STATE_DISCONNECTED) || (state == JPDADebugger.STATE_STOPPED)) breakpointReached(null);
            }
        }
        
        public void engineAdded(DebuggerEngine engine) {
            JPDADebugger debugger = (JPDADebugger) engine.lookupFirst
                    (null, JPDADebugger.class);
            if (debugger == null) return;
            debugger.addPropertyChangeListener(
                    JPDADebugger.PROP_STATE,
                    this
                    );
            debuggers.add(debugger);
        }
        
        public void engineRemoved(DebuggerEngine engine) {
            JPDADebugger debugger = (JPDADebugger) engine.lookupFirst
                    (null, JPDADebugger.class);
            if (debugger == null) return;
            debugger.removePropertyChangeListener(
                    JPDADebugger.PROP_STATE,
                    this
                    );
            debuggers.remove(debugger);
        }
        
        public void breakpointReached(JPDABreakpointEvent event) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    DebuggerManager dm = DebuggerManager.getDebuggerManager();
                    synchronized (breakpoints) {
                        for (MethodBreakpoint breakpoint : breakpoints) dm.removeBreakpoint(breakpoint);
                        breakpoints.clear();
                    }
                }
            });
            DebuggerManager.getDebuggerManager().removeDebuggerListener(DebuggerManager.PROP_DEBUGGER_ENGINES, this);
            Iterator it = debuggers.iterator();
            while (it.hasNext()) {
                JPDADebugger d = (JPDADebugger) it.next();
                d.removePropertyChangeListener(JPDADebugger.PROP_STATE, this);
            }
        }
    }
}
