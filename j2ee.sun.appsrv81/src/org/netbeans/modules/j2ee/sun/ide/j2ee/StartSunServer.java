// <editor-fold defaultstate="collapsed" desc=" License Header ">
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
// </editor-fold>

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import org.netbeans.modules.derby.spi.support.DerbySupport;
import org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerServerSettings;
import org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerSupport;
import org.netbeans.modules.j2ee.sun.api.ServerLocationManager;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.AttachingDICookie;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.j2ee.sun.ide.dm.SunDeploymentManager;
import org.netbeans.modules.j2ee.sun.ide.j2ee.ui.MasterPasswordInputDialog;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.sun.api.Asenv;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.sun.api.SunServerStateInterface;
import org.netbeans.modules.j2ee.sun.ide.j2ee.ui.Util;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;
import org.openide.windows.InputOutput;

/**
 * Life Cycle management for an instance
 * @author Ludo
 */
public class StartSunServer extends StartServer implements ProgressObject, SunServerStateInterface,
        Runnable {
    private ProgressEventSupport pes;
    private CommandType ct = null;
    private int cmd = CMD_NONE;
    private DeploymentManager dm;
    private boolean debug = false;
    private ServerDebugInfo debugInfo = null;
    private boolean shouldStopDeploymentManagerSilently =false;
    private static final int CMD_NONE = 0;

    /**
     * Start the server
     */
    private static final int CMD_START = 1;
    
    /**
     * Stop the server
     */
    private static final int CMD_STOP = 2;
    
    /**
     * restart the server
     */
    private static final int CMD_RESTART = 3;
    
    /** For how long should we keep trying to get response from the server. */
    //private static final long TIMEOUT_DELAY = 300000;   // 5 minutes
    //longer for profiler mode...
    //private static final long PROFILER_TIMEOUT_DELAY = 600000; //10 minutes
    private static Map debugInfoMap = Collections.synchronizedMap((Map)new HashMap(2,1));
    private String httpPort =null; //null for not known yet...
    /** Normal mode */
    private  int currentMode     = MODE_RUN;
    /** Normal mode */
    private static final int MODE_RUN     = 0;
    /** Debug mode */
    private static final int MODE_DEBUG   = 1;
    /** Profile mode */
    private static final int MODE_PROFILE = 2;
    
    private final DeploymentManagerProperties dmProps;
    private String domain;
    private String domainDir;
    
    private StartSunServer(DeploymentManager deploymentManager) {
        this.dm = deploymentManager;
        this.dmProps = new DeploymentManagerProperties(deploymentManager);
        
        pes = new ProgressEventSupport(this);
        
        java.util.logging.Logger.getLogger("javax.enterprise.system.tools.admin.client").setLevel(java.util.logging.Level.OFF);
        java.util.logging.Logger.getLogger("javax.enterprise.system.tools.avk.tools.verifier").setLevel(java.util.logging.Level.OFF);
        java.util.logging.Logger.getLogger("javax.enterprise.system.tools.avk.appverification").setLevel(java.util.logging.Level.OFF);
        java.util.logging.Logger.getLogger("javax.enterprise.system.tools.avk.appverification.tools").setLevel(java.util.logging.Level.OFF);
        java.util.logging.Logger.getLogger("javax.enterprise.system.tools.avk.appverification.xml").setLevel(java.util.logging.Level.OFF);
        
    }
    
    static private Map<DeploymentManager, StartServer> dm2StartServer = new HashMap<DeploymentManager, StartServer>();
    
    static public StartServer get(DeploymentManager dm) {
        StartServer retVal  = null;
        synchronized (dm2StartServer) {
            retVal = dm2StartServer.get(dm);
            if (null == retVal) {
                retVal = new StartSunServer(dm);
                dm2StartServer.put(dm,retVal);
            }
        }
        return retVal;
    }
    
    public DeploymentManager getDeploymentManager() {
        return dm;
    }
    
    public boolean supportsStartDeploymentManager() {
        boolean ret = true;
        File domainDirectory = null;
        String domain = dmProps.getDomainName();
        String domainDir = dmProps.getLocation();
        if (null == domain || domain.trim().length() < 1 ||
                null == domainDir || domainDir.trim().length() < 1) {
            // done checking
            ret = false;
        } else {
            domainDirectory = new File(domainDir,domain);
            ret =((SunDeploymentManagerInterface)dm).isLocal();
            ret &= domainDirectory.canWrite();
        }
        return ret;
    }
    
    public boolean supportsStartDebugging(Target target) {
        return supportsStartDeploymentManager();
    }
    
    /**
     * Can be the specified target server started in profile mode? If the
     * target is also an admin server can be the admin server started in
     * profile mode?
     *
     * @param  target the target server in question, null implies the case where
     *         target is also an admin server.
     *
     * @return true if the target server can be started in profile mode, false
     *         otherwise. The default return value is false.
     *
     */
    public boolean supportsStartProfiling(Target target) {
        return supportsStartDeploymentManager();
    }
    
    public boolean isProfiling(Target target) {
        return isRunning();
    }
    public ProgressObject startProfiling(Target target, ProfilerServerSettings settings) {
        
        pes.fireHandleProgressEvent(null, new Status(
                ActionType.EXECUTE,
                CommandType.START,
                "",  // NOI18N
                StateType.RUNNING));
        currentMode=MODE_PROFILE;
        return startTarget(target, MODE_PROFILE, settings);// profile settings
    }
    
    
    /** Optional method.
     *
     * Stops the admin server. The DeploymentManager object will be disconnected.
     * The call should terminate immediately and not wait for the server to stop.
     * <div class="nonnormative">
     * This will be used at IDE shutdown so that the server shutdown does not block the IDE.
     * </div>
     */
    public void stopDeploymentManagerSilently() {
        shouldStopDeploymentManagerSilently =true;
        stopDeploymentManager();
    }
    
    /** See {@link stopDeploymentManagerSilently}
     * @rreturn true for our server:  stopDeploymentManagerSilently is implemented
     *
     */
    public boolean canStopDeploymentManagerSilently() {
        return true;
    }
    
    public ProgressObject startDeploymentManager() {
        ct = CommandType.START;
        pes.clearProgressListener();
        
        if (cmd == CMD_NONE) {
            cmd = CMD_START;
        }
        addProgressListener(new ProgressListener() {
            public void handleProgressEvent(ProgressEvent pe) {
                if (pe.getDeploymentStatus().isCompleted()) {
                    getDebugInfo();
                }
            }
        });
        // fail, if the server is waiting for a profiler...
        if (ProfilerSupport.getState() == ProfilerSupport.STATE_BLOCKING) {
                pes.fireHandleProgressEvent(null,new Status(ActionType.EXECUTE,
                        ct, NbBundle.getMessage(StartSunServer.class, "LBL_ErrorProfiledServer"), StateType.FAILED));  //NOI18N
                cmd = CMD_NONE;
                pes.clearProgressListener();
                return this; //we failed to start the server.
        }
        resetProfiler();
        pes.fireHandleProgressEvent(null, new Status(ActionType.EXECUTE,
                ct, "",
                StateType.RUNNING));
        debug=false;
        RequestProcessor.getDefault().post(this, 0, Thread.NORM_PRIORITY);
        
        return this;
    }
    
    public ProgressObject stopDeploymentManager() {
        SunDeploymentManager sunDm = (SunDeploymentManager)this.dm;
        ct = CommandType.STOP;
        pes.clearProgressListener();
        cmd = CMD_STOP;
        
        //always try to remove this profiler, otherwise it's possible sometimes
        // that the launcer that stop the server cannot work
        resetProfiler();

        boolean running = false;
        // this test was part of a deadlock
        // If the user starts to Profile an application on a server for which they 
        // do not have the correct admin password; they will probably try to 
        // use the instance's Stop action... that is would trigger a deadlock, 
        // involving  the AWT thread...  Using getTargets() directly to test
        // whether the server is running... GF and SJSAS always have at least
        // one target...
        try {
            Target [] targs = sunDm.getTargets();        
            running = (targs == null) ? false : targs.length > 0;
        } catch (IllegalStateException ise) {
            running = false;
        }         
        if(currentMode==MODE_PROFILE && !running && !portInUse()){
            currentMode =MODE_RUN;
            // the profiler stopped the server already!!!
            pes.fireHandleProgressEvent(null,  new Status(ActionType.EXECUTE, ct,
                    "", StateType.COMPLETED));                                  //NOI18N
            cmd = CMD_NONE;
            //pes.clearProgressListener();
        } else {
            // just in case the profiler changes how it behaves...
            if(currentMode==MODE_PROFILE) {
                currentMode =MODE_RUN;
            }
            pes.fireHandleProgressEvent(null, new Status(ActionType.EXECUTE,
                    ct, "", StateType.RUNNING));                                // NOI18N
            RequestProcessor.getDefault().post(this, 0, Thread.NORM_PRIORITY);
        }
        return this;
    }
    
    /** view the log file
     *
     */    
    public void viewLogFile(){
        getLogViewerWindow();
    }
    
    /**
     * open the log viewer and return its InputOutput window
     * @return the InputOutput window that holds the log data
     */
    private InputOutput getLogViewerWindow(){
        SunDeploymentManagerInterface sunDm = (SunDeploymentManagerInterface)this.dm;
        return org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.actions.ViewLogAction.viewLog(sunDm,false,false);
    }
    
    public synchronized void run() {
        int errorCode=-1;
        
        SunDeploymentManagerInterface sunDm = (SunDeploymentManagerInterface)dm;
        
        if (null == domain) {
            domain = dmProps.getDomainName();
        }
        if (null == domainDir) {
            domainDir = dmProps.getLocation();
        }
        
        String installRoot;
        File irf = ((SunDeploymentManagerInterface)dm).getPlatformRoot();
        if (null != irf && irf.exists()) {
            installRoot = irf.getAbsolutePath();
        } else {
            installRoot = null;
        }
        if (cmd == CMD_STOP || cmd == CMD_RESTART) {
            if (null == installRoot) {
                pes.fireHandleProgressEvent(null,new Status(ActionType.EXECUTE,
                        ct, NbBundle.getMessage(StartSunServer.class, "LBL_ErrorStoppingServer"), StateType.FAILED));
                
                cmd = CMD_NONE;
                pes.clearProgressListener();
                return; //we failed to stop the server.
            }
            try{
                if (!debug){
                    // in non debug mode. Now double check is the debug options are og for Windows
                    // see bug 4989322. Next time we'll stat in debug mode, we'll be in sh_mem mode...
                    if (!ServerLocationManager.isGlassFish(sunDm.getPlatformRoot())){
                        sunDm.fixJVMDebugOptions();
                    }
                }
                //also make sure the correct http port is known by the plugin (useful for web services regsitration
            } catch(Exception ex){
                Util.showInformation( ex.getLocalizedMessage());
            }
            
            
            try{
                if (cmd == CMD_STOP) {  // don't mess with this during a restart, since CMD_START will do the right thing.
                    HttpMonitorSupport.synchronizeMonitor((SunDeploymentManagerInterface) dm, 
                            false);
                }
            } catch (Exception eee){
                Logger.getLogger(StartSunServer.class.getName()).log(Level.FINE,"",eee);
            }

            String asadminCmd = installRoot + File.separator + "bin" +  File.separator + "asadmin";          //NOI18N            
            if ("\\".equals(File.separator)) {
                asadminCmd = asadminCmd + ".bat"; //NOI18N
            }
            String arr[] = { asadminCmd, "stop-domain",
                "--domaindir", domainDir, domain //NOI18N
            };
            
            errorCode = exec(arr, CMD_STOP, null);
            if (errorCode != 0) {
                pes.fireHandleProgressEvent(null,new Status(ActionType.EXECUTE,
                        ct, NbBundle.getMessage(StartSunServer.class, "LBL_ErrorStoppingServer"), StateType.FAILED));
                
                cmd = CMD_NONE;
                pes.clearProgressListener();
                return; //we failed to stop the server.
                
            }else {
                debugInfoMap.remove(sunDm.getHost()+sunDm.getPort());
                LogViewerSupport.removeLogViewerSupport(dmProps.getUrl());
            }
        }
        
        
        
        if (cmd == CMD_START || cmd == CMD_RESTART) {
            if (null == installRoot) {
                pes.fireHandleProgressEvent(null,new Status(ActionType.EXECUTE,
                        ct, NbBundle.getMessage(StartSunServer.class, "LBL_ErrorStartingServer"), StateType.FAILED));//NOI18N
                cmd = CMD_NONE;
                pes.clearProgressListener();
                resetProfiler();
                return; //we failed to start the server.            }
            }
            //verify is http monitoring is necessary on not for this run
            try{
                HttpMonitorSupport.synchronizeMonitorWithFlag((SunDeploymentManagerInterface) dm);
            } catch (Exception eee){
                Logger.getLogger(StartSunServer.class.getName()).log(Level.FINE,"",eee);
            }
            
            try{
                HttpProxyUpdater hpu = new HttpProxyUpdater(sunDm.getManagement(), false);
                if(dmProps.isSyncHttpProxyOn()){
                    hpu.addHttpProxySettings();
                }
            }catch(Exception ex){
            }
            
            //for glassfishserver, need a real start-domain command for possible JBI addon startup as well.
            String asadminCmd = installRoot + File.separator + "bin" +  File.separator + "asadmin";          //NOI18N
            
            if ("\\".equals(File.separator)) {
                asadminCmd = asadminCmd + ".bat"; //NOI18N
            }
            String debugString = "false";//NOI18N
            if (debug){
                debugString = "true";//NOI18N
            }
            String mpw = readMasterPasswordFile();
            if (mpw==null){
                pes.fireHandleProgressEvent(null,new Status(ActionType.EXECUTE,
                        ct, NbBundle.getMessage(StartSunServer.class, "LBL_ErrorStartingServer"), StateType.FAILED));//NOI18N
                cmd = CMD_NONE;
                pes.clearProgressListener();
                resetProfiler();
                return; //we failed to start the server.
                
            }
            File passWordFile =  Utils.createTempPasswordFile(sunDm.getPassword(), mpw);//NOI18N
            if (passWordFile==null){
                pes.fireHandleProgressEvent(null,new Status(ActionType.EXECUTE,
                        ct, NbBundle.getMessage(StartSunServer.class, "LBL_ErrorStartingServer"), StateType.FAILED));//NOI18N
                cmd = CMD_NONE;
                pes.clearProgressListener();
                resetProfiler();
                return; //we failed to start the server.            }
            }
            String arrd[] = { asadminCmd, "start-domain",  "--debug="+debugString ,
            "--user" , sunDm.getUserName(),//NOI18N
            "--passwordfile",  passWordFile.getAbsolutePath() , //NOI18N
            "--domaindir", domainDir, domain //NOI18N
            };
            
            //Starts JavaDB if it is not running:
            if (ServerLocationManager.isJavaDBPresent(sunDm.getPlatformRoot())){
                DerbySupport.ensureStarted();
            }
            
            InputOutput io = getLogViewerWindow();
            errorCode = exec(arrd, CMD_START, io);
            
            if (errorCode != 0) {
                
                if (((SunDeploymentManager)sunDm).isMaybeRunningButWrongUserName()==false){
                    
                    pes.fireHandleProgressEvent(null,new Status(ActionType.EXECUTE,
                            ct, NbBundle.getMessage(StartSunServer.class, "LBL_ErrorStartingServer"), StateType.FAILED));//NOI18N
                } else{//eror dialog already showned to the user
                    pes.fireHandleProgressEvent(null,new Status(ActionType.EXECUTE,
                            ct, NbBundle.getMessage(SunDeploymentManager.class,"ERR_AUTH_DIALOG_TITLE"), StateType.FAILED));//NOI18N
                    
                }
                cmd = CMD_NONE;
                pes.clearProgressListener();
                resetProfiler();
                return; //we failed to start the server.
            }
        }
        
        
        
        if(currentMode==MODE_PROFILE){
            pes.fireHandleProgressEvent(null,  new Status(ActionType.EXECUTE, ct, "", StateType.COMPLETED));//NOI18N
            cmd = CMD_NONE;
            pes.clearProgressListener();
            
            return;
        }
        
        boolean running = false;
        try {
            running = sunDm.isRunning(true);
        } catch (RuntimeException re) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, re);
        }
        Status successful = new Status(ActionType.EXECUTE, ct, "", StateType.COMPLETED);
        if (cmd != CMD_STOP && !running) {
            viewLogFile();
            // wait a little bit more to make sure we are not started. Sometimes, the server is not fully initialized
            for (int l=0;l<5;l++){
                try {
                    Thread.sleep(3000);
                    if(((SunDeploymentManagerInterface)dm).isRunning(true)){// GOOD, we are really ready
                        pes.fireHandleProgressEvent(null, successful);
                        cmd = CMD_NONE;
                        pes.clearProgressListener();
                        return;
                    }
                    
                } catch (Exception e) {
                }
            }
            // we tried, but we failed!!!
            pes.fireHandleProgressEvent(null,new Status(ActionType.EXECUTE,
                    ct, NbBundle.getMessage(StartSunServer.class, "LBL_ErrorStartingServer"), StateType.FAILED));
            cmd = CMD_NONE;
            pes.clearProgressListener();
            return; //we failed to start the server.
        }
        
        
        pes.fireHandleProgressEvent(null, successful);
        cmd = CMD_NONE;
        pes.clearProgressListener();
    }
    
    
    /**
     * Execute a command and redirect its stdout to the InputOutput object if it isn't
     * null.
     * @param arr arguments to {@link java.lang.lang.Runtime#exec}
     * @param type {@link #CMD_START} or {@link #CMD_STOP}
     * @param io destination for output from process, if there is an error
     * @return -1 if there is an error
     */
    private int exec(String[] arr, int type /*can be CMD_START or CMD_STOP*/, InputOutput io) {
        
        int exitValue = -1;
        
        try {
            final Process process = Runtime.getRuntime().exec(arr);
                                    
            ByteArrayOutputStream eos = new ByteArrayOutputStream();
            
            // start stream flusher to push output to parent streams and log if they exist
            StreamFlusher sfErr=new StreamFlusher(process.getErrorStream(), eos);
            sfErr.start();
            
            // need to keep client around for start
            // this should only be invoked for start-domain command
            ByteArrayOutputStream oos = new ByteArrayOutputStream();
            
            // set flusher on stdout also
            StreamFlusher sfOut=new StreamFlusher(process.getInputStream(), oos);
            sfOut.start();
            
            if (shouldStopDeploymentManagerSilently){
                //no need to wait at all, we are closing the ide...
                
                shouldStopDeploymentManagerSilently =false;
                return 0;
            }
            pes.fireHandleProgressEvent(null,  new Status(ActionType.EXECUTE,  ct, "" ,StateType.RUNNING));
            try {
                if(currentMode==MODE_PROFILE){
                    // asadmin start-domain doesn't return when the profiler
                    // options are used...
                    //
                    try {
                        Thread.sleep(3000);                        
                    } catch (Exception e) {
                        Logger.getLogger(StartSunServer.class.getName()).log(Level.FINE,"",e);
                    }
                    
                    int startupLimit = dmProps.getStartupTimeout();
                    startupLimit *= 1000;
                    startupLimit -= 3000;
                    if (hasCommandSucceeded(startupLimit)){
                        return 0;
                    } else {
                        if (null != io)
                            io.getOut().println(oos.toString());
                        return -1;
                    }
                } else {
                    
                    // startup timeout support
                    new Thread(new Runnable() {

                        public void run() {
                            try {
                                java.lang.Thread.sleep(dmProps.getStartupTimeout() *
                                        1000);
                            } catch (InterruptedException ex) {
                                // do something here?
                            }
                            try {
                                //process.
                                int foo = process.exitValue();
                            } catch (IllegalThreadStateException itse) {
                                process.destroy();
                            }
                        }
                        
                    
                        
                    });

                    // use the return value to determine if we want to make 
                    // sure the command has been successful...  
                    //
                    exitValue = process.waitFor();
                    
                    if (exitValue == 0) {
//                        if (hasCommandSucceeded()){
                            return 0;
//                        } else {
//                            return -1;
//                        }
                    } else {
                        if (null != io)
                            io.getOut().println(oos.toString());
                    }
                }
            } catch (InterruptedException ie) {
                // TODO --
            }
            
        } catch (IOException e) {
        }
        
        return exitValue;
    }
    
    private static final String MASTER_PASSWORD_ALIAS="master-password";//NOI18N
    private char[] getMasterPasswordPassword() {
        return MASTER_PASSWORD_ALIAS.toCharArray();
    }
    
    /* can return null if no mpw is known or entered by user
     **/
    private String readMasterPasswordFile() {
        String mpw= "changeit";//NOI18N
        
        String domain = dmProps.getDomainName();
        String domainDir = dmProps.getLocation();
        final File pwdFile = new File(domainDir + File.separator + domain  +File.separator+"master-password");
        if (pwdFile.exists()) {
            try {                
                SunDeploymentManagerInterface sdm = (SunDeploymentManagerInterface)dm;
                ClassLoader loader = ServerLocationManager.getNetBeansAndServerClassLoader(sdm.getPlatformRoot());
                Class pluginRootFactoryClass =loader.loadClass("com.sun.enterprise.security.store.PasswordAdapter");//NOI18N
                java.lang.reflect.Constructor constructor =pluginRootFactoryClass.getConstructor(new Class[] {String.class, getMasterPasswordPassword().getClass()});
                Object PasswordAdapter =constructor.newInstance(new Object[] {pwdFile.getAbsolutePath(),getMasterPasswordPassword() });
                Class PasswordAdapterClazz = PasswordAdapter.getClass();
                java.lang.reflect.Method method =PasswordAdapterClazz.getMethod("getPasswordForAlias", new Class[]{  MASTER_PASSWORD_ALIAS.getClass()});//NOI18N
                mpw = (String)method.invoke(PasswordAdapter, new Object[] {MASTER_PASSWORD_ALIAS });                
                
                return mpw;
            } catch (Exception ex) {
                //    ex.printStackTrace();
                return mpw;
            }
        } else {
            MasterPasswordInputDialog d=new MasterPasswordInputDialog();
            if (DialogDisplayer.getDefault().notify(d) ==NotifyDescriptor.OK_OPTION){
                mpw = d.getInputText();
                //now validate the password:
                try {
                    
                    File pwdFile2 = new File(domainDir + File.separator + domain  +File.separator+"config/domain-passwords");
                    SunDeploymentManagerInterface sdm = (SunDeploymentManagerInterface)dm;
                    ClassLoader loader = ServerLocationManager.getNetBeansAndServerClassLoader(sdm.getPlatformRoot());
                    Class pluginRootFactoryClass =loader.loadClass("com.sun.enterprise.security.store.PasswordAdapter");//NOI18N
                    java.lang.reflect.Constructor constructor =pluginRootFactoryClass.getConstructor(new Class[] {String.class, getMasterPasswordPassword().getClass()});
                    //this would throw an ioexception of the password is not the good one
                    constructor.newInstance(new Object[] {pwdFile2.getAbsolutePath(),mpw.toCharArray() });
                    
                    return mpw;
                    
                } catch (Exception ex) {
                   return null;
                }
            } else{
                return null;
                
            }
        }
    }
    
    
    
    /* return the status of an instance.
     * It is optimized to return the previous status if called more than twice within
     * a 5 seconds intervall
     * This boosts IDE reactivity
     */
    public boolean isRunning() {
        try {
            SunDeploymentManagerInterface sunDm = (SunDeploymentManagerInterface)dm;
            boolean runningState =sunDm.isRunning();
            if ( (runningState)&&(httpPort==null))  {
                
                httpPort = sunDm.getNonAdminPortNumber();
                if (httpPort!=null){
                    // this is safe.. the DM is running, so we aren't deleting it...
                    DeploymentManagerProperties dmProps = new DeploymentManagerProperties(dm);
                    dmProps.setHttpPortNumber(httpPort);
                }
            }
            return runningState;
        } catch(RuntimeException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return false;
        }
    }
    
    /**
     * Try to get response from the server, whether the START/STOP command has
     * succeeded.
     *
     * @return <code>true</code> if START/STOP command completion was verified,
     *         <code>false</code> if time-out ran out.
     */
    private boolean hasCommandSucceeded(int timeLeft) {
        SunDeploymentManagerInterface sunDm = (SunDeploymentManagerInterface)this.dm;
//        long to =TIMEOUT_DELAY;
//        if(currentMode==MODE_PROFILE){
//            to = PROFILER_TIMEOUT_DELAY;
//        }
        
        long timeout = System.currentTimeMillis();
        if (timeLeft > 0) {
            timeout += timeLeft;
        }
        try {
            
            while (true) {
                boolean isRunning = sunDm.isRunning(true);
                if (ct == CommandType.START) {
                    if (isRunning) {
                        return true;
                    }
                    if (((SunDeploymentManager)sunDm).isMaybeRunningButWrongUserName()){
                        return false;
                    }
                    if (currentMode == MODE_PROFILE) {
                        int state = ProfilerSupport.getState();
                        if (state == ProfilerSupport.STATE_BLOCKING ||
                                state == ProfilerSupport.STATE_RUNNING  ||
                                state == ProfilerSupport.STATE_PROFILING) {
                            
                            return true;
                        } else if (state == ProfilerSupport.STATE_INACTIVE) {
                            return false;
                        }
                    }
                }
                if (ct == CommandType.STOP && !isRunning) {
                    return true;
                }
                
                // if time-out ran out, suppose command failed
                if (System.currentTimeMillis() > timeout) {
                    return false;
                }
                try {
                    if (timeLeft > 1000) {
                        Thread.sleep(1000); // take a nap before next retry
                    } else {
                        Thread.sleep(timeLeft);
                    }
                } catch(InterruptedException ie) {}
            }
        } catch (RuntimeException e) {
            return false;
        }
    }
    
    /**
     * Returns true if target server needs a restart for last configuration changes to
     * take effect.  Implementation should override when communication about this
     * server state is needed.
     *
     * @param target target server; null implies the case where target is also admin server.
     * @return 
     */
    public boolean needsRestart(Target target) {
        SunDeploymentManagerInterface sunDm = (SunDeploymentManagerInterface)this.dm;
        return sunDm.isRestartNeeded();        
    }
    
    /**
     * Returns true if this target is in debug mode.
     * 
     * @param target target server
     * @return 
     */
    public boolean isDebuggable(Target target) {
        try {
            SunDeploymentManagerInterface sunDm = (SunDeploymentManagerInterface)this.dm;
            sunDm.refreshDeploymentManager();
            if (!sunDm.isRunning()){ //not running? Then not debuggable
                return false;
            }
            if ( sunDm.isLocal()) {
                
                return  (null!=debugInfoMap.get(sunDm.getHost()+sunDm.getPort()));//we need a debuginfo there if in debug
            }else {
                debugInfoMap.put(sunDm.getHost()+sunDm.getPort(),getDebugInfo());
                return true;
            }
        } catch(RuntimeException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return false;
        }
    }
    
    public boolean isDebugged() {
        SunDeploymentManagerInterface sunDm = (SunDeploymentManagerInterface)this.dm;
        if (!sunDm.isRunning()){ //not running? Then not debugged
            return false;
        }
        return  (null!=debugInfoMap.get(sunDm.getHost()+sunDm.getPort()));//we need a debuginfo there if in debug
    }

    /*
     * 
     * mode can be run, debug or profile
     **/    
    public ProgressObject startTarget(Target Target, int mode, ProfilerServerSettings settings) {
        //in theory, target should not be null, but it is always null there!!!
        // System.out.println("in startTarget, debug="+debug);
        //System.out.println("\n\n\nin startTarget, Target="+Target+"\nsettings="+settings);
        this.debug = mode==MODE_DEBUG;
        pes.clearProgressListener();
        SunDeploymentManagerInterface sunDm = (SunDeploymentManagerInterface)this.dm;
        if (debug){
            debugInfoMap.put(sunDm.getHost()+sunDm.getPort(),getDebugInfo());            
        }else{
            debugInfoMap.remove(sunDm.getHost()+sunDm.getPort());            
        }

        // fail, if the server is waiting for a profiler...
        if (ProfilerSupport.getState() == ProfilerSupport.STATE_BLOCKING) {
                pes.fireHandleProgressEvent(null,new Status(ActionType.EXECUTE,
                        ct, NbBundle.getMessage(StartSunServer.class, "LBL_ErrorProfiledServer"), StateType.FAILED));  //NOI18N
                cmd = CMD_NONE;
                pes.clearProgressListener();
                return this; //we failed to start the server.
        }
        if (settings!=null){
            if (!applySettingsToDomain(settings)) {
                // we need to fail here, now.
                Asenv asenvContent = new Asenv(sunDm.getPlatformRoot());
                String currentJdkRoot = asenvContent.get(Asenv.AS_JAVA);
                pes.fireHandleProgressEvent(null,new Status(ActionType.EXECUTE,
                        ct, NbBundle.getMessage(StartSunServer.class, "LBL_ErrorStartingProfiledServer",currentJdkRoot ), StateType.FAILED));  //NOI18N
                cmd = CMD_NONE;
                pes.clearProgressListener();
                Logger.getLogger(StartSunServer.class.getName()).log(Level.SEVERE,"Applying profiler changes");
                return this; //we failed to start the server.
            }
        } else{
            resetProfiler();
        }
        cmd = CMD_START;
        
        if (isRunning()) {
            cmd = CMD_RESTART;
        }
        
        
        ct = CommandType.START;
        pes.clearProgressListener();
        addProgressListener(new ProgressListener() {
            public void handleProgressEvent(ProgressEvent pe) {
                if (pe.getDeploymentStatus().isCompleted()) {
                    getDebugInfo();
                }
            }
        });
        pes.fireHandleProgressEvent(null, new Status(ActionType.EXECUTE,ct, "",StateType.RUNNING));
        RequestProcessor.getDefault().post(this, 0, Thread.NORM_PRIORITY);
        return this;
    }
        
    private String oldJdkRoot = null;
    
    private boolean applySettingsToDomain(ProfilerServerSettings settings) {
        boolean retVal;
        SunDeploymentManagerInterface sunDm = (SunDeploymentManagerInterface)this.dm;
        Asenv asenvContent = new Asenv(sunDm.getPlatformRoot());
        String currentJdkRoot = asenvContent.get(Asenv.AS_JAVA);
        File oldValue = new File(currentJdkRoot);
        if (!Utilities.isWindows()) {
            currentJdkRoot = "\""+currentJdkRoot+"\"";
        }
        String newJdkRoot = currentJdkRoot;
        Iterator<FileObject>  iter = settings.getJavaPlatform().getInstallFolders().iterator();
        String jdkPath;
        File newValue = null;
        if (iter.hasNext()) {
            FileObject fo = iter.next();
            newValue = FileUtil.toFile(fo);
            jdkPath = newValue.getAbsolutePath();            
            if (Utilities.isWindows()) {
                newJdkRoot = jdkPath;
            } else {
                newJdkRoot = "\""+jdkPath+"\"";
            }
        }
        // make sure you don't rewrite the asenv file if there is no change.
        boolean needToRewrite = false;
        try {
            if (newValue != null && oldValue.exists() && 
                    !newValue.getCanonicalPath().equals(oldValue.getCanonicalPath())) {
                    needToRewrite = true;
            }
        } catch (IOException ioe) {
            Logger.getLogger(StartSunServer.class.getName()).log(Level.FINER, null, ioe);
            needToRewrite = true;
        }
                
        if (needToRewrite) {
            retVal = ConfigureProfiler.modifyAsEnvScriptFile(dm, newJdkRoot);            
        } else {
            retVal = true;
        }
        if (retVal) {
            ConfigureProfiler.instrumentProfilerInDomain(dm , null,settings.getJvmArgs());
            if (needToRewrite) {
                oldJdkRoot = currentJdkRoot;
            } else {
                oldJdkRoot = null;
            }
        }
        return retVal;
    }
    
    
    private void resetProfiler() {
        ConfigureProfiler.removeProfilerFromDomain(dm);
        if (oldJdkRoot != null) {
            if (!ConfigureProfiler.modifyAsEnvScriptFile(dm, oldJdkRoot)) {
                Logger.getLogger(StartSunServer.class.getName()).warning("Environment rewrite failed");  // NOI18N
            } else {
                oldJdkRoot = null;
            }
            
        }
    }
    
    private boolean portInUse() {
        SunDeploymentManagerInterface sunDm = (SunDeploymentManagerInterface)dm;
        boolean retVal = true;
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(sunDm.getPort());
            retVal = false;
        } catch (IOException ioe) {
            // is there a better way to test this???
        } finally {
            if (null != ss) {
                try {
                    ss.close();
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                            ioe);
                }
            }
        }
        return retVal;
    }
    
    public ProgressObject stopTarget(Target target) {
        pes.clearProgressListener();
        boolean running = false;
        try {
            running = ((SunDeploymentManagerInterface)dm).isRunning(true);
        } catch (RuntimeException re) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, re);
        }
        if (!running) {
            pes.fireHandleProgressEvent(null, new Status(ActionType.EXECUTE,
                    CommandType.STOP, "",
                    StateType.COMPLETED));
            return this;
        }
        return stopDeploymentManager();
    }
    
    /*
     * impl of the j2eeserver interface. Sometimes, we are called with null, and we should not...
     *
     **/
    public ServerDebugInfo  getDebugInfo(Target target) {
        return getDebugInfo();
    }
    
    private ServerDebugInfo  getDebugInfo() {        
        try{
            SunDeploymentManagerInterface sunDm = (SunDeploymentManagerInterface)this.dm;
            if (!sunDm.isRunning()){
                return null;
            }
            if (isSuspended((SunDeploymentManagerInterface)this.dm)){
                return (ServerDebugInfo)debugInfoMap.get(sunDm.getHost()+sunDm.getPort());
            }
            
            String addr= sunDm.getDebugAddressValue();
            
            if (sunDm.isDebugSharedMemory()){//string constructor: sh_mem
                debugInfo =  new ServerDebugInfo(sunDm.getHost(), addr);
                //System.out.println("Ludo getDebugInfo shmem to "+sunDm.getHost()+sunDm.getPort());
                if (debug){
                    debugInfoMap.put(sunDm.getHost()+sunDm.getPort(),debugInfo);
                }
                
            } else{//dt_socket, with a port number (integer)
                int port = Integer.parseInt(addr);
                debugInfo =  new ServerDebugInfo(sunDm.getHost(), port);
                //System.out.println("Ludo getDebugInfo dtsocket to "+sunDm.getHost()+sunDm.getPort());
                if (debug){
                    debugInfoMap.put(sunDm.getHost()+sunDm.getPort(),debugInfo);
                }
                
                
            }
        }catch(Exception ex){
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            debugInfo =null;
        }
        return debugInfo;
    }
    
    /**
     * Returns true if this server is started in debug mode AND debugger is attached to it
     * AND threads are suspended (e.g. debugger stopped on breakpoint)
     */
    public static boolean isSuspended(SunDeploymentManagerInterface sunDm) {
        boolean retVal = false;
        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
        
        for (int i=0; ! retVal && i < sessions.length; i++) {
            Session s = sessions[i];
            if (s != null) {
                Object o = s.lookupFirst(null,AttachingDICookie.class);
                if (o != null) {
                    Object d = s.lookupFirst(null,JPDADebugger.class);
                    if (d != null) {
                        JPDADebugger jpda = (JPDADebugger)d;
                        if (jpda.getState() == JPDADebugger.STATE_STOPPED) { // the session is suspended.
                            AttachingDICookie attCookie = (AttachingDICookie)o;
                            String shmName = attCookie.getSharedMemoryName();
                            if (shmName!=null) {
                                if (shmName.startsWith(sunDm.getHost())) {
                                    retVal = true;
                                }
                            } else {//test the machine name and port number
                                int attachedPort = attCookie.getPortNumber();
                                ServerDebugInfo dbi = (ServerDebugInfo)debugInfoMap.get(sunDm.getHost()+sunDm.getPort());
                                if (null != dbi) {
                                    if (sameMachine(attCookie.getHostName(), sunDm.getHost()) &&
                                            dbi.getPort() == attachedPort) {
                                        retVal = true;
                                    }
                                }
                                
                            }
                        }
                    }
                }
            }
        }
        return retVal;
    }
    
    public DeploymentStatus getDeploymentStatus() {
        return pes.getDeploymentStatus();
    }
    
    public void addProgressListener(ProgressListener pl) {
        pes.addProgressListener(pl);
    }
    
    public void removeProgressListener(ProgressListener pl) {
        pes.removeProgressListener(pl);
    }
    
    
    public void cancel() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("");
    }
    
    public ClientConfiguration getClientConfiguration(TargetModuleID id) {
        throw new UnsupportedOperationException("");
    }
    
    public TargetModuleID[] getResultTargetModuleIDs() {
        throw new UnsupportedOperationException("");
    }
    
    public boolean isCancelSupported() {
        return false;
    }
    
    public boolean isStopSupported() {
        return false;
    }
    
    public void stop() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("");
    }
    
    
    
    
    public boolean isAlsoTargetServer(Target target) {
        return true;
    }
    
    public boolean needsStartForConfigure() {
        return false;
    }
    
    public ProgressObject startDebugging(Target target) {
        //System.out.println("in ProgressObject startDebugging");
        currentMode = MODE_DEBUG;
        return startTarget(target, MODE_DEBUG, null);//debug and no profile settings
    }
    
    public boolean needsStartForAdminConfig() {
        return true;
    }
    
    public boolean needsStartForTargetList() {
        return true;
    }
    
    private static  final  String LOCALHOST="localhost";//NOI18N
    private static  final  String LOCALADDRESS="127.0.0.1";//NOI18N
    
    /* return true if the 2 host names represent the same machine
     * deal with localhost, domain name and ips liek 127.0.0.1
     */
    public static  boolean sameMachine(String host1, String host2){
        try {
            if (host1.equals(host2)){
                return true;
            }
            if (host1.equals(LOCALHOST)){
                if (host2.equals(LOCALADDRESS)){
                    return true;
                }
                String localCanonicalHostName = java.net.InetAddress.getLocalHost().getCanonicalHostName();
                String h2 = java.net.InetAddress.getByName(host2).getCanonicalHostName();
                if (localCanonicalHostName.equals(h2)){
                    return true;
                }
            }
            if (host1.equals(LOCALADDRESS)){
                if (host2.equals(LOCALHOST)){
                    return true;
                }
                return true;
            }
            if (host2.equals(LOCALHOST)){
                if (host1.equals(LOCALADDRESS)){
                    return true;
                }
                String localCanonicalHostName = java.net.InetAddress.getLocalHost().getCanonicalHostName();
                String h1 = java.net.InetAddress.getByName(host1).getCanonicalHostName();
                if (localCanonicalHostName.equals(h1)){
                    return true;
                }
            }
            if (host2.equals(LOCALADDRESS)){
                if (host1.equals(LOCALHOST)){
                    return true;
                }
                String localCanonicalHostName = java.net.InetAddress.getLocalHost().getCanonicalHostName();
                String h1 = java.net.InetAddress.getByName(host1).getCanonicalHostName();
                if (localCanonicalHostName.equals(h1)){
                    return true;
                }
            }
            String h1 = java.net.InetAddress.getByName(host1).getCanonicalHostName();
            String h2 = java.net.InetAddress.getByName(host2).getCanonicalHostName();
            if (h1.equals(h2)){
                return true;
            }
        } catch (java.net.UnknownHostException ex) {
            //ex.printStackTrace();
        }
        return false;
    }
    
    /**
     * A class that attaches to the output streams of the executed process and sends the data
     * to the calling processes output streams
     */
    protected class StreamFlusher extends Thread {
        
        private final InputStream _input;
        private final OutputStream _output;
        
        public StreamFlusher(InputStream input, OutputStream output) {
            this._input=input;
            this._output=output;
        }
        
        public void run() {
            
            // check for null stream
            if (_input == null){
                return;
            }
            
            // transfer bytes from input to output stream
            try {
                int byteCnt=0;
                byte[] buffer=new byte[4096];
                while ((byteCnt=_input.read(buffer)) != -1) {
                    if (_output != null && byteCnt > 0) {
                        _output.write(buffer, 0, byteCnt);
                        _output.flush();
                    }
                    yield();
                }
            } catch (IOException e) {
                // just log this as an finest exception, because it really should matter
                //getLogger().log(Level.FINEST,"Exception thrown while reading/writing verbose error stream", e);
            }
        }
    }
}

