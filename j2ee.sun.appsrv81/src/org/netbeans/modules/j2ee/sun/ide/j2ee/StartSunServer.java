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


package org.netbeans.modules.j2ee.sun.ide.j2ee;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.net.Authenticator;
import org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerServerSettings;
import org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerSupport;
import org.netbeans.modules.j2ee.sun.api.ServerLocationManager;
import org.netbeans.modules.j2ee.sun.appsrvapi.PortDetector;
import org.netbeans.modules.j2ee.sun.ide.editors.AdminAuthenticator;

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
import org.netbeans.modules.j2ee.sun.ide.j2ee.ui.MasterPasswordInputDialog;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;

import org.netbeans.modules.j2ee.deployment.plugins.api.StartServer;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;

import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.sun.api.SunServerStateInterface;

import org.netbeans.modules.j2ee.sun.ide.j2ee.ui.Util;

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
    private static final int CMD_START = 1;
    private static final int CMD_STOP = 2;
    private static final int CMD_RESTART = 3;
    
     /** For how long should we keep trying to get response from the server. */
    private static final long TIMEOUT_DELAY = 580000;   
    private static Map debugInfoMap = Collections.synchronizedMap((Map)new HashMap(2,1));
    private String httpPort =null; //null for not known yet...
    /** Normal mode */
    private  int current_mode     = MODE_RUN;
    /** Normal mode */
    private static final int MODE_RUN     = 0;
    /** Debug mode */
    private static final int MODE_DEBUG   = 1;
    /** Profile mode */
    private static final int MODE_PROFILE = 2;

    public StartSunServer(DeploymentManager deploymentManager) {
        this.dm = deploymentManager;
        pes = new ProgressEventSupport(this);

        java.util.logging.Logger.getLogger("javax.enterprise.system.tools.admin.client").setLevel(java.util.logging.Level.OFF);
        java.util.logging.Logger.getLogger("javax.enterprise.system.tools.avk.tools.verifier").setLevel(java.util.logging.Level.OFF);
        java.util.logging.Logger.getLogger("javax.enterprise.system.tools.avk.appverification").setLevel(java.util.logging.Level.OFF);
        java.util.logging.Logger.getLogger("javax.enterprise.system.tools.avk.appverification.tools").setLevel(java.util.logging.Level.OFF);
        java.util.logging.Logger.getLogger("javax.enterprise.system.tools.avk.appverification.xml").setLevel(java.util.logging.Level.OFF);

    }
           

    
    public void setDeploymentManager(DeploymentManager deploymentManager) {
        this.dm = deploymentManager;
    }

    public DeploymentManager getDeploymentManager() {
        return dm;
    }
    
    public boolean supportsStartDeploymentManager() {
        DeploymentManagerProperties dmProps = new DeploymentManagerProperties(dm);
        String domain = null;
        String domainRootDir = null;
        domain = dmProps.getDomainName();
        domainRootDir = dmProps.getLocation();
        File domainDirectory = null;
        if (null == domain || domain.trim().length() < 1 || 
                null == domainRootDir || domainRootDir.trim().length() < 1)
            return false;
        else
            domainDirectory = new File(domainRootDir,domain);
        boolean ret =((SunDeploymentManagerInterface)dm).isLocal();
        ret &= domainDirectory.canWrite();
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
       current_mode=MODE_PROFILE;
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
        //
    }
    
    /** See {@link stopDeploymentManagerSilently}
     * @rreturn true for our server:  stopDeploymentManagerSilently is implemented

     */
    public boolean canStopDeploymentManagerSilently () {
       return true; 
    }

    public ProgressObject startDeploymentManager() {


        ct = CommandType.START;
        pes.clearProgressListener();

        
        if (cmd == CMD_NONE) {
            cmd = CMD_START;
        } 
        ConfigureProfiler.removeProfilerInDOmain(new DeploymentManagerProperties(dm));
        pes.fireHandleProgressEvent(null, new Status(ActionType.EXECUTE,
                                                     ct, "",
                                                     StateType.RUNNING));
        RequestProcessor.getDefault().post(this, 0, Thread.NORM_PRIORITY);
        

        return this;
    }

    public ProgressObject stopDeploymentManager() {

        SunDeploymentManagerInterface sunDm = (SunDeploymentManagerInterface)this.dm;
        debugInfoMap.remove(sunDm.getHost()+sunDm.getPort());
        ct = CommandType.STOP;
        pes.clearProgressListener();
        cmd = CMD_STOP;
        if(current_mode==MODE_PROFILE){
            current_mode =MODE_RUN;
            //System.out.println("resetting profiler mode");
        }

        //always try to remove this profiler, otherwise it's possible sometimes
        // that the launcer that stop the server cannot work
        ConfigureProfiler.removeProfilerInDOmain(new DeploymentManagerProperties(dm));
        
        pes.fireHandleProgressEvent(null, new Status(ActionType.EXECUTE,
                                                     ct, "",
                                                     StateType.RUNNING));
        RequestProcessor.getDefault().post(this, 0, Thread.NORM_PRIORITY);
        return this;
    }
    /* view the log file
     *
     */
    
    public void viewLogFile(){
        SunDeploymentManagerInterface sunDm = (SunDeploymentManagerInterface)this.dm;
        org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.actions.ViewLogAction.viewLog(sunDm);
        
    }
    public synchronized void run() {
	DeploymentManagerProperties dmProps = new DeploymentManagerProperties(dm);
	String domain = null;
	String domainDir = null;
	int errorCode=-1;
	
	File irf = ((SunDeploymentManagerInterface)dm).getPlatformRoot();
	if (null == irf || !irf.exists()) {
	    return;
	}
	String installRoot = irf.getAbsolutePath(); 
	
	domain = dmProps.getDomainName();
	domainDir = dmProps.getLocation();
	//System.out.println("domain="+domain);
	if (domain==null){
	    domain="domain1";
	    dmProps.setDomainName(domain);
	}
	if (null == domainDir) {
	    domainDir = installRoot+File.separator+"domains";
	    dmProps.setLocation(domainDir);
	}
	
	
	
	if (cmd == CMD_STOP || cmd == CMD_RESTART) {
	    String asadminCmd = domainDir + File.separator + domain  +File.separator + "bin" +   File.separator + "stopserv";
	    if (File.separator.equals("\\")) {
		asadminCmd = asadminCmd + ".bat"; //NOI18N
	    }
	    String arr[] = { asadminCmd, " "};
	    
	    errorCode = exec(arr, CMD_STOP);
	    if (errorCode != 0) {
		pes.fireHandleProgressEvent(null,new Status(ActionType.EXECUTE,
			ct, NbBundle.getMessage(StartSunServer.class, "LBL_ErrorStoppingServer"), StateType.FAILED));
		
		cmd = CMD_NONE;
		pes.clearProgressListener();
		return; //we failed to stop the server.
		
	    }
	}
	
	
	
	if (cmd == CMD_START || cmd == CMD_RESTART) {
            Authenticator.setDefault(new AdminAuthenticator());
	    
	    //verify is http monitoring is necessary on not for this run
	    try{
		HttpMonitorSupport.synchronizeMonitorWithFlag((SunDeploymentManagerInterface) dm);
	    } catch (Exception eee){
	    }
	    
	    String asadminCmd = domainDir + File.separator + domain + File.separator + "bin" +   File.separator + "startserv";
	    if (File.separator.equals("\\")) {//NOI18N
		asadminCmd = asadminCmd + ".bat"; //NOI18N
	    }
	    String arr[] = new String[3];
	    arr[0]= asadminCmd;
            String osType=System.getProperty("os.name");//NOI18N
            if (osType.startsWith("Mac OS"))//no native for mac
                arr[1]= "";//NOI18N
            else
                arr[1]= "native";//NOI18N
            
	    if (debug) {
		arr[2]= "debug";//NOI18N
		
	    } else{
		arr[2]= "";//NOI18N
	    }
	    
	    
	    errorCode = exec(arr, CMD_START);
	    viewLogFile();
	    if (errorCode != 0) {
		
		pes.fireHandleProgressEvent(null,new Status(ActionType.EXECUTE,
			ct, NbBundle.getMessage(StartSunServer.class, "LBL_ErrorStartingServer"), StateType.FAILED));//NOI18N
		cmd = CMD_NONE;
		pes.clearProgressListener();
		return; //we failed to start the server.
	    }
	}
	
	
	
	if(current_mode==MODE_PROFILE){
	    pes.fireHandleProgressEvent(null,  new Status(ActionType.EXECUTE, ct, "", StateType.COMPLETED));//NOI18N
	    cmd = CMD_NONE;
	    pes.clearProgressListener();
	    
	    return;
	}
	
	if (cmd != CMD_STOP && !((SunDeploymentManagerInterface)dm).isRunning(true)) {
	    viewLogFile();
            // wait a little bit more to make sure we are not started. Sometimes, the server is not fully initialized
            for (int l=0;l<5;l++){
                try {
                    Thread.sleep(3000);
                    if(((SunDeploymentManagerInterface)dm).isRunning(true)){// GOOD, we are really ready
                        pes.fireHandleProgressEvent(null, new Status(ActionType.EXECUTE, ct, "", StateType.COMPLETED));
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
	} else
	    if (cmd != CMD_STOP){// we started sucessfully
	    try{
		if (debug==false){
		    // in non debug mode. Now double check is the debug options are og for Windows
		    // see bug 4989322. Next time we'll stat in debug mode, we'll be in sh_mem mode...
		    SunDeploymentManagerInterface sunDm = (SunDeploymentManagerInterface)dm;
		    sunDm.fixJVMDebugOptions();
		}
		//also make sure the correct http port is known by the plugin (useful for web services regsitration
		
	    } catch(Exception ex){
		Util.showInformation( ex.getLocalizedMessage());
	    }
	    
	    }
                  
        pes.fireHandleProgressEvent (null, new Status(ActionType.EXECUTE, ct, "", StateType.COMPLETED));
        cmd = CMD_NONE;
        pes.clearProgressListener();
        return;
    }
    

    private int exec(String[] arr, int type /*can be CMD_START or CMD_STOP*/) {

        int exitValue = -1;
        String mpw=null;
        if (type==CMD_START){// we need a master password in order to start.
            mpw = readMasterPasswordFile();
            if (mpw==null){
                return -2;
            }
        }
        try {
            Process process = Runtime.getRuntime().exec(arr);
            String cmdName="";
            for (int j=0;j<arr.length;j++){
                cmdName= cmdName+arr[j]+" ";
            }
//            System.out.println("exec cmdName="+cmdName);
            // See is there is input that needs to be sent to the process
            if (type==CMD_START){
                sendInputToProcessInput(System.in, process,mpw);
                
            }
            
            
            // start stream flusher to push output to parent streams and log if they exist
            StreamFlusher sfErr=new StreamFlusher(process.getErrorStream(), /*System.err*/null);
            sfErr.start();
            
            
            // need to keep client around for start
            // this should only be invoked for start-domain command
            
            // set flusher on stdout also
            StreamFlusher sfOut=new StreamFlusher(process.getInputStream(), /*System.out*/null);
            sfOut.start();  

            if (shouldStopDeploymentManagerSilently==true){
                //no need to wait at all, we are closin the ide...

                shouldStopDeploymentManagerSilently =false;
                return 0;
            }
            pes.fireHandleProgressEvent(null,
                    new Status(ActionType.EXECUTE,
                    ct, "" ,StateType.RUNNING));
            if(current_mode==MODE_PROFILE){
                try {
                    Thread.sleep(3000);
                    
                } catch (Exception e) {
                }
            }
            if (hasCommandSucceeded())
                return 0;
            else return -1;
            
        } catch (IOException e) {
        }
        
        return exitValue;
    }
            


        

        

    private void sendInputToProcessInput(InputStream in, Process subProcess, String masterPassword) {
        // return if no input
        if (in == null || subProcess == null) return;
        
        PrintWriter out=null;
        BufferedReader br=null;
        try {
            // open the output stream on the process which excepts the input
            out = new PrintWriter(new BufferedWriter(
            new OutputStreamWriter(subProcess.getOutputStream())));
            
            // read in each line and resend it to sub process
//////////            br=new BufferedReader(new InputStreamReader(System.in));
//////////            String sxLine=null;
//////////            while ((sxLine=br.readLine()) != null) {
//////////                // get input lines from process if any
//////////                out.println(sxLine);
//////////                if (bDebug) System.out.println("Feeding in Line:" + sxLine);
//////////            }
            out.println(((SunDeploymentManagerInterface)dm).getUserName());
            out.println(((SunDeploymentManagerInterface)dm).getPassword());
            out.println(masterPassword);
            out.flush();
        } catch (Exception e) {
         //   getLogger().log(Level.INFO,"WRITE TO INPUT ERROR", e);
        } finally {
            try {
                if (out != null) out.close();
            } catch (Throwable t) {}
        }
    }
    private static final String MASTER_PASSWORD_ALIAS="master-password";//NOI18N
    private char[] getMasterPasswordPassword() {
        return MASTER_PASSWORD_ALIAS.toCharArray();
    }
    
    /* can return null if no mpw is known or entered by user
     **/
    protected String readMasterPasswordFile() {
        String mpw= "changeit";//NOI18N
        DeploymentManagerProperties dmProps = new DeploymentManagerProperties(dm);
        String domain ;
        String domainDir ;
        String installRoot = ((SunDeploymentManagerInterface)dm).getPlatformRoot().getAbsolutePath();
        
        domain = dmProps.getDomainName();
        domainDir = dmProps.getLocation();
        
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
              //  ex.printStackTrace();
              //  System.out.println("INVALID  master PASSWORD");
                return null;
            }                    
            } else{
                return null;
                
            }
        }
    }  
    
    private void asyncExec(final String[] arr) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    BufferedReader input = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(arr).getInputStream()));
                    String line = null;
                    while ((line = input.readLine()) != null) System.out.println(">>> " + line);
                    input.close();
                } catch (Exception ex) {
                    System.err.println("Error starting/stopping integrated SJSAS:\n" + ex);
                }
            }
        }).start();
    }    
  
    /* return the status of an instance.
     * It is optimized to return the previous status if called more than twice within
     * a 5 seconds intervall
     * This boosts IDE reactivity
     */
    public boolean isRunning() {
        SunDeploymentManagerInterface sunDm = (SunDeploymentManagerInterface)dm;
        boolean runningState =sunDm.isRunning();
        if ( (runningState)&&(httpPort==null))  {
            
            httpPort = sunDm.getNonAdminPortNumber();
            if (httpPort!=null){
                DeploymentManagerProperties dmProps = new DeploymentManagerProperties(dm);
                dmProps.setHttpPortNumber(httpPort);
            }
        }
        return runningState;
    }
        /**
         * Try to get response from the server, whether the START/STOP command has 
         * succeeded.
         *
         * @return <code>true</code> if START/STOP command completion was verified,
         *         <code>false</code> if time-out ran out.
         */
        private boolean hasCommandSucceeded() {
            SunDeploymentManagerInterface sunDm = (SunDeploymentManagerInterface)this.dm;        
            long timeout = System.currentTimeMillis() + TIMEOUT_DELAY;

            while (true) {
                boolean isRunning = sunDm.isRunning(true);
                if (ct == CommandType.START) {
                    if (isRunning) {
                        return true;
                    }
                    if (current_mode == MODE_PROFILE) {
                        int state = ProfilerSupport.getState();
                        if (state == ProfilerSupport.STATE_BLOCKING ||
                                state == ProfilerSupport.STATE_RUNNING  ||
                                state == ProfilerSupport.STATE_PROFILING) {
                            
                            return true;
                        } else if (state == ProfilerSupport.STATE_INACTIVE) {
                            System.out.println("---ProfilerSupport.STATE_INACTIVE");
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
                    Thread.sleep(1000); // take a nap before next retry
                } catch(InterruptedException ie) {}
            }
        }
       
    /**
     * Returns true if target server needs a restart for last configuration changes to 
     * take effect.  Implementation should override when communication about this 
     * server state is needed.
     *
     * @param target target server; null implies the case where target is also admin server.
     */
    public boolean needsRestart(Target target) {
        SunDeploymentManagerInterface sunDm = (SunDeploymentManagerInterface)this.dm;
        return sunDm.isRestartNeeded();
        
    }
    
    /**
     * Returns true if this target is in debug mode.
     */
    public boolean isDebuggable(Target target) {
        SunDeploymentManagerInterface sunDm = (SunDeploymentManagerInterface)this.dm;        
        if (sunDm.isRunning()==false){ //not running? Then not debuggable
            return false;
        }
        return  (null!=debugInfoMap.get(sunDm.getHost()+sunDm.getPort()));//we need a debuginfo there if in debug

    }        
    
    public boolean isDebugged() {
        SunDeploymentManagerInterface sunDm = (SunDeploymentManagerInterface)this.dm;
        if (sunDm.isRunning()==false){ //not running? Then not debugged
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
        if (debug==true){
            debugInfoMap.put(sunDm.getHost()+sunDm.getPort(),getDebugInfo());

        }else{
            debugInfoMap.remove(sunDm.getHost()+sunDm.getPort());
            
        }

        if (settings!=null){
            ConfigureProfiler.instrumentProfilerInDOmain(new  DeploymentManagerProperties(dm) , null,settings.getJvmArgs())  ;
        } else{
            //reset the profilere
            ConfigureProfiler.removeProfilerInDOmain(new DeploymentManagerProperties(dm));
            
        }
        cmd = CMD_START;
        
        if (isRunning()) {
            cmd = CMD_RESTART;
        }

        
        ct = CommandType.START;
        pes.clearProgressListener();
        pes.fireHandleProgressEvent(null, new Status(ActionType.EXECUTE,ct, "",StateType.RUNNING));
        RequestProcessor.getDefault().post(this, 0, Thread.NORM_PRIORITY);
        return this;
    }
    

    public ProgressObject stopTarget(Target target) {
       // System.out.println("           in stopTarget");
        pes.clearProgressListener();
        if (!(((SunDeploymentManagerInterface)dm).isRunning(true))) {
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
        //System.out.println("           in getDebugInfo debug="+debug);
        //System.out.println(""+target+"           in getDebugInfo ");
       // if (target==null){
            //Thread.dumpStack();
       //     return null;/// no target passed!!! necessary to prevent J2eeserver to call us when we are not started yet.
       // }
        return getDebugInfo();
    }
    
    private ServerDebugInfo  getDebugInfo() {

        SunDeploymentManagerInterface sunDm = (SunDeploymentManagerInterface)this.dm;
        if (sunDm.isRunning()==false){
            return null;
        }
        if (isSuspended((SunDeploymentManagerInterface)this.dm)){
            return (ServerDebugInfo)debugInfoMap.get(sunDm.getHost()+sunDm.getPort());
        }
        try{
            String addr= sunDm.getDebugAddressValue();

            if (sunDm.isDebugSharedMemory()){//string constructor: sh_mem
                debugInfo =  new ServerDebugInfo(sunDm.getHost(), addr);
                //System.out.println("Ludo getDebugInfo shmem to "+sunDm.getHost()+sunDm.getPort());
                if (debug==true){
                    debugInfoMap.put(sunDm.getHost()+sunDm.getPort(),debugInfo);
                }
                
            } else{//dt_socket, with a port number (integer)
                int port = Integer.parseInt(addr);
                debugInfo =  new ServerDebugInfo(sunDm.getHost(), port);
                //System.out.println("Ludo getDebugInfo dtsocket to "+sunDm.getHost()+sunDm.getPort());
                if (debug==true){
                    debugInfoMap.put(sunDm.getHost()+sunDm.getPort(),debugInfo);
                }
                
                
            }
        }catch(Exception ex){
            //System.out.println("Ludo getDebugInfo error hardcoded to 1044"+ex);
            /*if (File.separatorChar == '/') {//unix   
                int port =1044;
                return new ServerDebugInfo("localhost", port); //default value
            }
            else{
                String addr = sunDm.getHost()+sunDm.getPort();
                return new ServerDebugInfo(sunDm.getHost(), addr); //second default value on pc
            }*/
	    debugInfo =null;
        }
        return debugInfo;
    }
    
    

    
    /**
     * Returns true if this server is started in debug mode AND debugger is attached to it 
     * AND threads are suspended (e.g. debugger stopped on breakpoint)
     */
    public static boolean isSuspended(SunDeploymentManagerInterface sunDm) {
        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
       
        for (int i=0; i < sessions.length; i++) {
            Session s = sessions[i];
            if (s != null) {
                Object o = s.lookupFirst(null,AttachingDICookie.class);
                if (o != null) {
                    Object d = s.lookupFirst(null,JPDADebugger.class);
                    if (d != null) {
                        JPDADebugger jpda = (JPDADebugger)d;
                        if (jpda.getState() != JPDADebugger.STATE_STOPPED) { //We are not suspended.
                            return false;
                        }
                    }
                    AttachingDICookie attCookie = (AttachingDICookie)o;
                    String shmName = attCookie.getSharedMemoryName();
                    if (shmName!=null) {                       
                        if (shmName.startsWith(sunDm.getHost())) {
                            return true;
                        }                        
                    } else {//just test the machine name
			return sameMachine(attCookie.getHostName(), sunDm.getHost());

                    }
                }
            }
        }
        return false;
       

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
        return null;
    }
        
    public TargetModuleID[] getResultTargetModuleIDs() {
        return null;
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
 //       System.out.println("in isAlsoTargetServer");

        return true;
    }
    
    public boolean needsStartForConfigure() {
        return false;
    }
    
    public ProgressObject startDebugging(Target target) {
        //System.out.println("in ProgressObject startDebugging");
       current_mode = MODE_DEBUG;
       return startTarget(target, MODE_DEBUG, null);//debug and no profile settings


    }

    public boolean needsStartForAdminConfig() {
        return true;
    }    

    public boolean needsStartForTargetList() {
        return true;
    }
    /* return true if the 2 host names represent the same machine
     * deal with localhost, domain name and ips liek 127.0.0.1
     */
     public static  boolean sameMachine(String host1, String host2){
        try {
            if (host1.equals(host2))
                return true;
            if (host1.equals("localhost")){
                if (host2.equals("127.0.0.1"))
                    return true;
                String localCanonicalHostName = java.net.InetAddress.getLocalHost().getCanonicalHostName();
                String h2 = java.net.InetAddress.getByName(host2).getCanonicalHostName();
                if (localCanonicalHostName.equals(h2))
                    return true;
            }
            if (host1.equals("127.0.0.1")){
                if (host2.equals("localhost"))
                    return true;
                String localCanonicalHostName = java.net.InetAddress.getLocalHost().getCanonicalHostName();
                String h2 = java.net.InetAddress.getByName(host2).getCanonicalHostName();
                    return true;
            }
            if (host2.equals("localhost")){
                if (host1.equals("127.0.0.1"))
                    return true;
                String localCanonicalHostName = java.net.InetAddress.getLocalHost().getCanonicalHostName();
                String h1 = java.net.InetAddress.getByName(host1).getCanonicalHostName();
                if (localCanonicalHostName.equals(h1))
                    return true;
            }
            if (host2.equals("127.0.0.1")){
                if (host1.equals("localhost"))
                    return true;
                String localCanonicalHostName = java.net.InetAddress.getLocalHost().getCanonicalHostName();
                String h1 = java.net.InetAddress.getByName(host1).getCanonicalHostName();
                if (localCanonicalHostName.equals(h1))
                    return true;
            }
            String h1 = java.net.InetAddress.getByName(host1).getCanonicalHostName();
            String h2 = java.net.InetAddress.getByName(host2).getCanonicalHostName();
            if (h1.equals(h2))
                return true;
        } catch (java.net.UnknownHostException ex) {
            ex.printStackTrace();
        }
        return false;
    }     
    
/**
     * A class that attaches to the output streams of the executed process and sends the data
     * to the calling processes output streams
     */
    protected class StreamFlusher extends Thread {
        
        private InputStream _input=null;
        private OutputStream _output=null;

        
        public StreamFlusher(InputStream input, OutputStream output) {
            this._input=input;
            this._output=output;
        }
        
        public void run() {
            
            // check for null stream
            if (_input == null) return;
            
            PrintStream printStream=null;

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

