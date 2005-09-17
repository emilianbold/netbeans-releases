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
import java.rmi.RemoteException;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.net.Authenticator;
import org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerServerSettings;
import org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerSupport;
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






import org.openide.util.RequestProcessor;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

import org.netbeans.modules.j2ee.deployment.plugins.api.StartServer;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;


import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.jpda.*;


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
        Authenticator.setDefault(new AdminAuthenticator());   
        pes = new ProgressEventSupport(this);

        java.util.logging.Logger.getLogger("javax.enterprise.system.tools.admin.client").setLevel(java.util.logging.Level.OFF);

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
            ConfigureProfiler.removeProfilerInDOmain(new DeploymentManagerProperties(dm));
        }

        
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
    public synchronized void run () {
        DeploymentManagerProperties dmProps = new DeploymentManagerProperties(dm);
        String domain = null;
        String domainDir = null;
        StateType state = StateType.COMPLETED;
        int errorCode=-1;
        
        String installRoot = PluginProperties.getDefault().getInstallRoot().getAbsolutePath();

       
        domain = dmProps.getDomainName();
        domainDir = dmProps.getLocation();
        //System.out.println("domain="+domain);
        if (domain==null){
            domain="domain1";
            dmProps.setDomainName(domain);
        }
        if (null == domainDir) {
            domainDir = PluginProperties.getDefault().getInstallRoot()+
                    File.separator+"domains";
            dmProps.setLocation(domainDir);
        }
        String asadminCmd = installRoot + File.separator +
            "bin" +             
            File.separator +
            "asadmin";          //NOI18N

        if (File.separator.equals("\\")) { 
            asadminCmd = asadminCmd + ".bat"; //NOI18N
        }

        if (state != StateType.FAILED) {        
            if (cmd == CMD_STOP || cmd == CMD_RESTART) {
                asadminCmd = domainDir + File.separator + domain  +File.separator + "bin" +   File.separator + "stopserv";
                if (File.separator.equals("\\")) {
                    asadminCmd = asadminCmd + ".bat"; //NOI18N
                }
                String arr[] = { asadminCmd, " "};
                
                errorCode = exec(arr);
                if (errorCode != 0) {                    
                    Util.showInformation(
                    NbBundle.getMessage(StartSunServer.class, "LBL_ErrorStoppingServer"),
                    NbBundle.getMessage(StartSunServer.class, "LBL_ErrorTitle"));
                    
                    state = StateType.FAILED;
                } 
            } 
        } 
        
        if (state != StateType.FAILED) {
            if (cmd == CMD_START || cmd == CMD_RESTART) {
                
                //verify is http monitoring is necessary on not for this run
                try{
                    HttpMonitorSupport.synchronizeMonitorWithFlag((SunDeploymentManagerInterface) dm);
                } catch (Exception eee){
                }

                asadminCmd = domainDir + File.separator + domain + File.separator + "bin" +   File.separator + "startserv";
                if (File.separator.equals("\\")) {
                    asadminCmd = asadminCmd + ".bat"; //NOI18N
                }
                String arr[] = new String[2];
                arr[0]= asadminCmd;
                if (debug) {
                    arr[1]= "debug";

                } else{
                    arr[1]= "";
                }


                errorCode = exec(arr);
                viewLogFile();
                if (errorCode != 0) {
                    System.out.println("errorCode"+errorCode);
                    Util.showInformation(
                    NbBundle.getMessage(StartSunServer.class, "LBL_ErrorStartingServer"),
                    NbBundle.getMessage(StartSunServer.class, "LBL_ErrorTitle"));
                    ErrorManager.getDefault().log(ErrorManager.ERROR, NbBundle.getMessage(StartSunServer.class, "LBL_ErrorStartingServer"));
                    
                    state = StateType.FAILED;
                }
            }
        }
        
        if (state != StateType.FAILED) {
            if(current_mode==MODE_PROFILE){
                pes.fireHandleProgressEvent(null,  new Status(ActionType.EXECUTE, ct, "", state));                                
                cmd = CMD_NONE;
                pes.clearProgressListener();

                return;
            }
            
            if (cmd != CMD_STOP && !((SunDeploymentManagerInterface)dm).isRunning(true)) {
                viewLogFile();
                 Util.showInformation(
                NbBundle.getMessage(StartSunServer.class, "LBL_ErrorStartingServer"),
                NbBundle.getMessage(StartSunServer.class, "LBL_ErrorTitle"));
                ErrorManager.getDefault().log(ErrorManager.ERROR, NbBundle.getMessage(StartSunServer.class, "LBL_ErrorStartingServer"));
                state = StateType.FAILED;
           }
            else
            if (cmd != CMD_STOP){// we started sucessfully 
                try{
                    if (debug==false){
                        // in non debug mode. Now double check is the debug options are og for Windows
                        // see bug 4989322. Next time we'll stat in debug mode, we'll be in sh_mem mode...
                        SunDeploymentManagerInterface sunDm = (SunDeploymentManagerInterface)dm;
                        sunDm.fixJVMDebugOptions();
                    }
                    //also make sure the correct http port is known by the plugin (useful for web services regsitration
                    
                }
                catch(Exception ex){
                    Util.showInformation( ex.getLocalizedMessage());
                }
                
            }
        }  
        
        if (state != StateType.FAILED) {
        pes.fireHandleProgressEvent (null,
                                     new Status(ActionType.EXECUTE,
                                     ct, "", state));
        }
        else{
        pes.fireHandleProgressEvent (null,
                                     new Status(ActionType.EXECUTE,
                                     ct, "Failure to start. See the server.log file.", state));
            
        }
        cmd = CMD_NONE;
        pes.clearProgressListener();
        return;
    }
    

    private int exec(String[] arr) {

        int exitValue = -1;        
        try {
            Process process = Runtime.getRuntime().exec(arr);
            String cmdName="";
            for (int j=0;j<arr.length;j++){
                cmdName= cmdName+arr[j]+" ";
            }
//            System.out.println("exec cmdName="+cmdName);
            // See is there is input that needs to be sent to the process
            sendInputToProcessInput(System.in, process);
            
            
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
          /*      pes.fireHandleProgressEvent(null,
                    new Status(ActionType.EXECUTE,
                    ct, cmdName,StateType.RUNNING));  */
                shouldStopDeploymentManagerSilently =false;
                return 0;
            }
            if(current_mode==MODE_PROFILE){
                try {
                        Thread.sleep(3000);
                        pes.fireHandleProgressEvent(null,
                    new Status(ActionType.EXECUTE,
                    ct, "running in profiler mode" ,StateType.RUNNING));
                } catch (Exception e) {
                } 
                if (hasCommandSucceeded())
                return 0;
                else return -1;
            }
            else
            // wait for max 150 seconds
            for (int i = 0; i < 150; i++) {                
                try {
                    exitValue = process.exitValue();
                } catch (IllegalThreadStateException ite) {
                } 

                if (exitValue >= 0) {//0 is not error, positive number is bad...See CLI return codes
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }
                    return exitValue;
                }
                if (i==0){
                    pes.fireHandleProgressEvent(null,
                    new Status(ActionType.EXECUTE,
                    ct, cmdName,StateType.RUNNING));
                }
//                else{//emit the message only cmdName once
//                    pes.fireHandleProgressEvent(null,
//                    new Status(ActionType.EXECUTE,
//                    ct, "",StateType.RUNNING));                    
//                }


                try {
                    if ((cmd == CMD_STOP)||(i>3))//faster to stop than to start!
                        Thread.sleep(1000);
                    else
                        Thread.sleep(6000);
                } catch (Exception e) {
                } 
            } 
        } catch (IOException e) {
        }
        
        return exitValue;
    }
            


        

        

    private void sendInputToProcessInput(InputStream in, Process subProcess) {
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
            out.println("");
            out.println("");
            out.println("changeit");
            out.flush();
        } catch (Exception e) {
         //   getLogger().log(Level.INFO,"WRITE TO INPUT ERROR", e);
        } finally {
            try {
                if (out != null) out.close();
            } catch (Throwable t) {}
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
            long timeout = System.currentTimeMillis() + TIMEOUT_DELAY;
            while (true) {
                boolean isRunning = isRunning();
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
                          // return false;
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

        return  (null!=debugInfoMap.get(sunDm.getHost()+sunDm.getPort()));//we need a debuginfo there if in debug

    }        
    
    public boolean isDebugged() {
        SunDeploymentManagerInterface sunDm = (SunDeploymentManagerInterface)this.dm;
        return  (null!=debugInfoMap.get(sunDm.getHost()+sunDm.getPort()));//we need a debuginfo there if in debug
    }
/*
 *
 * mode can be run, debug or profile
 **/
    
    public ProgressObject startTarget(Target Target, int mode, ProfilerServerSettings settings) {
        //in theory, target should not be null, but it is always null there!!!
       // System.out.println("in startTarget, debug="+debug);
       // System.out.println("\n\n\nin startTarget, Target="+Target);
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
}     
        cmd = CMD_START;
        
        if (isRunning()) {
            cmd = CMD_RESTART;
        }
        if (settings!=null){
            //Need to verify all the settings are applied....
        }
        
        return startDeploymentManager( );
    }
    

    public ProgressObject stopTarget(Target target) {
 //       System.out.println("           in stopTarget");
        pes.clearProgressListener();
        if (!isRunning()) {
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
                        if (attCookie.getHostName().equalsIgnoreCase(sunDm.getHost())) {
                            return true;
                        }
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

