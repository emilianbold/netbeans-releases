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


import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.net.Authenticator;
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
    private static Map debugInfoMap = Collections.synchronizedMap((Map)new HashMap(2,1));
    private String httpPort =null; //null for not known yet...


    public StartSunServer(DeploymentManager deploymentManager) {
        this.dm = deploymentManager;
        Authenticator.setDefault(new AdminAuthenticator());   
        pes = new ProgressEventSupport(this);
        //map this startserver object to our dpeloyment manager.
        //used to get one from the other one...
        ((SunDeploymentManagerInterface)dm).setStartServerInterface(this);
        java.util.logging.Logger.getLogger("javax.enterprise.system.tools.admin.client").setLevel(java.util.logging.Level.OFF);

    }
           

    
    public void setDeploymentManager(DeploymentManager deploymentManager) {
        this.dm = deploymentManager;
         //map this startserver object to our dpeloyment manager.
        //used to get one from the other one...
       ((SunDeploymentManagerInterface)dm).setStartServerInterface(this);
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
       // System.out.println("in startDeploymentManager");
      //  Thread.dumpStack();
        
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
        //System.out.println("in stopDeploymentManager");
        SunDeploymentManagerInterface sunDm = (SunDeploymentManagerInterface)this.dm;
        debugInfoMap.remove(sunDm.getHost()+sunDm.getPort());
        ct = CommandType.STOP;
        pes.clearProgressListener();
        cmd = CMD_STOP;
        
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
        
        String installRoot = null;//dmProps.getLocation();

        if (installRoot == null) {
             installRoot = PluginProperties.getDefault().getInstallRoot().getAbsolutePath(); //System.getProperty("com.sun.aas.installRoot"); 
          /*  Util.showInformation(
            NbBundle.getMessage(StartSunServer.class, "MSG_WrongInstallDir"),
            NbBundle.getMessage(StartSunServer.class, "LBL_ErrorTitle"));
            state = StateType.FAILED;*/
        }
        else{
            File ff= new File(installRoot);
            if ((ff!=null)&&(!ff.exists())) {
                Util.showInformation(
                NbBundle.getMessage(StartSunServer.class, "MSG_WrongInstallDir"),
                NbBundle.getMessage(StartSunServer.class, "LBL_ErrorTitle"));
                state = StateType.FAILED;
            }  
        }
       
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
                String arr[] = { asadminCmd,
                             "stop-domain", 
                            /// "--domain", 
                        "--domaindir",
                        domainDir,
                             domain
                };
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

                String debugString = "";//"--debug=false";
                String arr[];
                if (debug) {
                   String arrd[] = { asadminCmd,
                            "start-domain",
                            "--debug=true",
                           "--domaindir",
                           domainDir,
                            domain
                    };
                    arr = arrd;
                }
                else{
                    String arrnd[] = { asadminCmd,
                            "start-domain",
                            "--domaindir",
                            domainDir,
                            domain
                    };
                    arr= arrnd;
                }
                

                
                // if (!isRunning())
                errorCode = exec(arr);
                viewLogFile();
                if (errorCode != 0) {
                    Util.showInformation(
                    NbBundle.getMessage(StartSunServer.class, "LBL_ErrorStartingServer"),
                    NbBundle.getMessage(StartSunServer.class, "LBL_ErrorTitle"));
                    ErrorManager.getDefault().log(ErrorManager.ERROR, NbBundle.getMessage(StartSunServer.class, "LBL_ErrorStartingServer"));
                    
                    state = StateType.FAILED;
                }
            }
        }
        
        if (state != StateType.FAILED) {
            if (cmd != CMD_STOP && !isRunning()) {
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
        
        pes.fireHandleProgressEvent (null,
                                     new Status(ActionType.EXECUTE,
                                     ct, "", state));
        cmd = CMD_NONE;
        pes.clearProgressListener();
        return;
    }
    

    private int exec(String[] arr) {
        int exitValue = -1;        
        try {
            Process subProcess = Runtime.getRuntime().exec(arr);
            String cmdName="";
            for (int j=0;j<arr.length;j++){
                cmdName= cmdName+arr[j]+" ";
            }
            if (shouldStopDeploymentManagerSilently==true){
                //no need to wait at all, we are closin the ide...
          /*      pes.fireHandleProgressEvent(null,
                    new Status(ActionType.EXECUTE,
                    ct, cmdName,StateType.RUNNING));  */
                shouldStopDeploymentManagerSilently =false;
                return 0;
            }
            // wait for max 150 seconds
            for (int i = 0; i < 150; i++) {                
                try {
                    exitValue = subProcess.exitValue();
                } catch (IllegalThreadStateException ite) {
                } 

                if (exitValue >= 0) {//0 is not error, positive number is bad...See CLI return codes
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }
                    return exitValue;
                }
                pes.fireHandleProgressEvent(null,
                    new Status(ActionType.EXECUTE,
                    ct, cmdName,StateType.RUNNING));                


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
            


    public boolean isAlsoTargetServer() {
        return true;
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

    public ProgressObject startTarget(Target Target, boolean debug) {
        //in theory, target should not be null, but it is always null there!!!
       // System.out.println("in startTarget, debug="+debug);
       // System.out.println("\n\n\nin startTarget, Target="+Target);
        this.debug = debug;
        pes.clearProgressListener();
        SunDeploymentManagerInterface sunDm = (SunDeploymentManagerInterface)this.dm;
        if (debug==true){
            debugInfoMap.put(sunDm.getHost()+sunDm.getPort(),getDebugInfo());

        }else{
            debugInfoMap.remove(sunDm.getHost()+sunDm.getPort());
            
        }
        
        cmd = CMD_START;
        
        if (isRunning()) {
            cmd = CMD_RESTART;
        }
        
        
        return startDeploymentManager();
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
    public boolean isSuspended() {
        ServerDebugInfo sdi = null;
        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();

        SunDeploymentManagerInterface sunDm = (SunDeploymentManagerInterface)this.dm;
        try {
            sdi = (ServerDebugInfo)debugInfoMap.get(sunDm.getHost()+sunDm.getPort());
        } catch (Exception e) {
            // don't care - just a try
        }

        if (sdi == null) {
       //     ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "DebuggerInfo cannot be found for: " + this.toString());
       //     System.out.println("cannot find ServerDebugInfo in isSuspendended ");
            return false;
        }

        for (int i=0; i < sessions.length; i++) {
            Session s = sessions[i];
            if (s != null) {
                Object o = s.lookupFirst(null,AttachingDICookie.class);
                if (o != null) {
                    AttachingDICookie attCookie = (AttachingDICookie)o;
                    if (sdi.getTransport().equals(ServerDebugInfo.TRANSPORT_SHMEM)) {
                        if (attCookie.getSharedMemoryName().equalsIgnoreCase(sdi.getShmemName())) {
                            Object d = s.lookupFirst(null,JPDADebugger.class);
                            if (d != null) {
                                JPDADebugger jpda = (JPDADebugger)d;
                                if (jpda.getState() == JPDADebugger.STATE_STOPPED) {
                                    return true;
                                }
                            }
                        }
                    } else {
                        if (attCookie.getHostName().equalsIgnoreCase(sdi.getHost())) {
                            if (attCookie.getPortNumber() == sdi.getPort()) {
                                Object d = s.lookupFirst(null,JPDADebugger.class);
                                if (d != null) {
                                    JPDADebugger jpda = (JPDADebugger)d;
                                    if (jpda.getState() == JPDADebugger.STATE_STOPPED) {
                                        return true;
                                    }
                                }
                            }
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


    
    public ProgressObject startServer(Target target) {
 //       System.out.println("in ProgressObject startServer");
        
        return startTarget(target, false);
        
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
       return startTarget(target, true);


    }

    public boolean needsStartForAdminConfig() {
        return true;
    }    

    public boolean needsStartForTargetList() {
        return true;
    }

}

