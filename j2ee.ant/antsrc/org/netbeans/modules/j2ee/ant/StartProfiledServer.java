/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ant;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.CommandlineJava;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.j2ee.deployment.profiler.spi.Profiler;
import org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerSupport;
import org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerServerSettings;
import org.openide.filesystems.*;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;

/**
 * Ant task that starts the server in profile mode.
 *
 * @author sherold
 */
public class StartProfiledServer extends Task /*implements Deployment.Logger*/ {
    
    private static final String PLAT_PROP_ANT_NAME = "platform.ant.name"; //NOI18N
    
    private boolean         forceRestart;
    /** timeout on waiting for server startup, default = 3 min */
    private int             startupTimeout = 180000;
    /** java platform "platform.ant.name" property */
    private String          javaPlatform;
    private CommandlineJava jvmarg       = new CommandlineJava();
    private Environment     env          = new Environment();
    
    public void execute() throws BuildException {
      
        Profiler profiler = ServerRegistry.getProfiler();
        if (profiler == null) {
            throw new BuildException("No profiler found"); // NOI18N
        }
        JavaPlatform[] installedPlatforms = JavaPlatformManager.getDefault().getInstalledPlatforms();
        JavaPlatform platform = null;
        for (int i = 0; i < installedPlatforms.length; i++) {
            String platformName = (String)installedPlatforms[i].getProperties().get(PLAT_PROP_ANT_NAME);
            if (platformName != null && platformName.equals(javaPlatform)) {
                platform = installedPlatforms[i];
            }
        }
        if (platform == null) {
            throw new BuildException("Java platform " + javaPlatform + " does not exist"); // NOI18N
        }
        String[] envvar = env.getVariables();
        if (envvar == null) {
            envvar = new String[0];
        }
        ProfilerServerSettings settings = new ProfilerServerSettings(
                                                    platform,
                                                    jvmarg.getVmCommand().getArguments(), 
                                                    envvar);
        
        log("*****************************************");
        log("*** StartProfiledServer Task ************");
        log("*** forceRestart: " + forceRestart);
        log("*** " + settings);
        
        FileObject fo = FileUtil.toFileObject(getProject().getBaseDir());
        fo.refresh(); // without this the "build" directory is not found in filesystems
        J2eeModuleProvider jmp = (J2eeModuleProvider)FileOwnerQuery.getOwner(fo).getLookup().lookup(J2eeModuleProvider.class);
        ServerInstance si = ServerRegistry.getInstance().getServerInstance(jmp.getServerInstanceID());
        log(">>> Starting server...");
        if (!si.startProfile(settings, forceRestart)) {
            throw new BuildException("Starting server in profile mode failed"); // NOI18N
        }
        log(">>> Server ready, attaching profiler...");
        if (!profiler.attachProfiler(getProject().getProperties())) {
            throw new BuildException("Attaching the profiler to server failed"); // NOI18N
        }
        log(">>> Profiler attached, waiting for server startup (timeout after " + startupTimeout + " sec)...");
        
        // wait for the server to finish its startup
        long timeout = System.currentTimeMillis() + startupTimeout;
        while (true) {
            if (si.isRunning()) {
                log(">>> Server is up and running.");
                si.refresh(); // update the server status
                return;
            }
            // if time-out ran out, suppose command failed
            if (System.currentTimeMillis() > timeout) {
                throw new BuildException("Profiled server didn't start in time"); // NOI18N
            }
            try { 
                Thread.sleep(1000);  // take a nap before next retry
            } catch (Exception ex) {};
        }
    }
    
    public void setForceRestart(boolean forceRestart) {
        this.forceRestart = forceRestart;
    }
    
    public void setStartupTimeout(int timeout) {
      startupTimeout = timeout;
    }
    
    public void setJavaPlatform(String javaPlatform) {
        this.javaPlatform = javaPlatform;
    }
    
    public Commandline.Argument createJvmarg() {
        return jvmarg.createVmArgument();
    }
    
    public void addEnv(Environment.Variable var) {
        env.addVariable(var);
    }
}
