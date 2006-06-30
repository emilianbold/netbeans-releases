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
package org.netbeans.modules.j2ee.sun.ide.dm;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Locale;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.lang.reflect.Method;
import java.util.ResourceBundle;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.shared.DConfigBeanVersionType;
import javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.sun.api.SunURIManager;
import org.netbeans.modules.j2ee.sun.appsrvapi.PortDetector;
import org.netbeans.modules.j2ee.sun.ide.editors.AdminAuthenticator;
import org.netbeans.modules.j2ee.sun.ide.j2ee.DeploymentManagerProperties;
import org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.actions.ViewLogAction;

import org.netbeans.modules.j2ee.sun.share.configbean.SunONEDeploymentConfiguration;

import org.netbeans.modules.j2ee.sun.ide.j2ee.mbmapping.JvmOptions;
import org.netbeans.modules.j2ee.sun.ide.j2ee.mbmapping.ServerInfo;

import java.util.jar.JarOutputStream;
import java.io.FileOutputStream;
import org.netbeans.modules.j2ee.sun.share.plan.Util;



import org.netbeans.modules.j2ee.sun.ide.j2ee.Utils;

import org.netbeans.modules.j2ee.sun.api.ServerInterface;
import org.netbeans.modules.j2ee.sun.api.SunServerStateInterface;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.sun.api.ResourceConfiguratorInterface;

import org.openide.ErrorManager;
import org.netbeans.modules.j2ee.sun.api.ServerLocationManager;
/**
 *
 * @author  ludo, vkraemer
 */
public class SunDeploymentManager implements Constants, DeploymentManager, SunDeploymentManagerInterface {
    private DeploymentManager innerDM;
    private DeploymentFactory df;
    private String host, userName, password;
    private String uri;
    private boolean isConnected;
    /* port is the admin port number, ususally 4848*/
    
    int adminPortNumber;
    /* httpport is the non admin port number, usually 8080*/
    String nonAdminPortNumber = null; //null, is unknown at this point, will be calculated when is running
    
    private static String SECURESTRINGDETECTION =":https";
    /*
     *flag to see of the secure (SSL) testing on the running server has been done or not
     * this is to avoid too many checks that would cause perf issues
     **/
    private transient boolean secureStatusHasBeenChecked = false;
    private boolean runningState=false;
    private boolean secure =false;
    private long timeStampCheckingRunning =0;
    private File platformRoot  =null;
    //are we java ee 5 or only 8.x? Needed for testing the secure mode.
    private boolean isGlassFish = false;
    
    /* cache for local value. Sometimes, the islocal() call can be very long for IP that changed
     * usually when dhcp is used. So we calculate the value in a thread at construct time
     * init value is false, but updated later within a thread.
     **/
    private boolean isLocal = false;
    /* used by a netbeans extension to associatate a StartServer with this DM
     */
    private SunServerStateInterface startServerInterface =null;
    static final ResourceBundle bundle = ResourceBundle.getBundle("org.netbeans.modules.j2ee.sun.ide.dm.Bundle");// NOI18N
    
    /* this is interesting: JMXREMOTE APIs create a factory and load it from the getContext classloader
     * which is a NetBeans class loader that does not know about our App Server Dynamic jars class loader.
     * So we need to hack on the fly the context classloader with the plugin classloader for any jxm calls
     * done in the app server jsr 88 impl!!!
     * this ClassLoader origClassLoader is used to stored the original CL so that NB is happy after a jsr88 call.
     **/


    public SunDeploymentManager( DeploymentFactory df, String uri,String userName, String password, File platformRootDir)  throws DeploymentManagerCreationException{
        this.df= df;
        this.uri =uri;
	this.platformRoot =  platformRootDir;
        isGlassFish= ServerLocationManager.isGlassFish(platformRootDir);
        secure = uri.endsWith(SECURESTRINGDETECTION);
        String uriNonSecure =uri;
        if (secure)
            uriNonSecure = uri.substring(0,  uri.length()- SECURESTRINGDETECTION.length());
        
        host = getHostFromURI(uriNonSecure);
        adminPortNumber = getPortFromURI(uriNonSecure);
        try {
            InstanceProperties props = SunURIManager.getInstanceProperties(platformRoot, host, adminPortNumber); 

            if (userName == null && props != null) {
                this.userName = props.getProperty(InstanceProperties.USERNAME_ATTR);
            } else {
                this.userName = userName;
            }
            if (password == null && props != null) {
                this.password = props.getProperty(InstanceProperties.PASSWORD_ATTR);
            } else {
                this.password = password;
            }
            isConnected = this.userName != null;
        } catch (IllegalStateException ise) {
            // get before set throws an ISE.  Instance registration time
            // triggers this
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ise);
        }
        resetInnerDeploymentManager();        
        calculateIsLocal();
    }
    


    /* return the real dm created from the app server implementation of 88, not our wrapper (ie. not this class
     **
     */
    

    
    private void resetInnerDeploymentManager() throws DeploymentManagerCreationException{
        try {
            if (isConnected){
                innerDM = df.getDeploymentManager(uri,userName,password);
            } else{
                if (df!=null) innerDM = df.getDisconnectedDeploymentManager(uri);
            }
////////            if(innerDM == null) {
////////                throw new DeploymentManagerCreationException("invalid URI");
////////            }
        } catch (NoClassDefFoundError ee) {
            throw ee;
        } catch (Exception e) {
            e.printStackTrace();
            throw new DeploymentManagerCreationException("DeploymentManagerCreationException" + e.getMessage());
        }
        if (secure) {
          // different classloader!  AppServerBridge.setServerConnectionEnvironment(innerDM);
            try{
                Class[] argClass = new Class[1];
                argClass[0] = DeploymentManager.class;
                Object[] argObject = new Object[1];
                argObject[0] = innerDM;
                
                 ClassLoader loader =  getExtendedClassLoader();
                if(loader != null){
                    Class cc = loader.loadClass("org.netbeans.modules.j2ee.sun.bridge.AppServerBridge");
                    Method setServerConnectionEnvironment = cc.getMethod("setServerConnectionEnvironment", argClass);//NOI18N
                    setServerConnectionEnvironment.invoke(null, argObject);
                }
            }catch(Exception ex){
                //Suppressing exception while trying to obtain admin host port value
                ex.printStackTrace();
            }
        }

    }
    public String getUserName() {
        InstanceProperties props = SunURIManager.getInstanceProperties(platformRoot, host, adminPortNumber);
        this.userName = props.getProperty(InstanceProperties.USERNAME_ATTR);
        return userName;
    }
    
    public String getPassword() {
        InstanceProperties props = SunURIManager.getInstanceProperties(platformRoot, host, adminPortNumber);        
        this.password = props.getProperty(InstanceProperties.PASSWORD_ATTR);
        return password;
    }
    
    public String getHost() {
        return host;
    }
    
    public int getPort() {
        return adminPortNumber;
    }
    /*
     * return the real http port for the server. Usually, it is "8080", or null if the server is not running
     *
     **/
    public String getNonAdminPortNumber() {
        return nonAdminPortNumber;
    }
    
     /*
      *
      *return true is this  deploymment manager needs a restart, because of changes in admin configuration
      */
    public boolean isRestartNeeded(){
        try{
        ServerInfo si = new ServerInfo(getMBeanServerConnection());
        return si.isRestartRequired();
        }
        catch (java.rmi.RemoteException e){
            return false;//silent assumption: server mgiht not even be running!
        }
        
    }

    
    public void fixJVMDebugOptions() throws java.rmi.RemoteException{
        JvmOptions jvmInfo = new JvmOptions(getMBeanServerConnection());
        String addr= jvmInfo.getAddressValue();
        if (jvmInfo.isWindows()){
            if (jvmInfo.isSharedMemory()==false){//force shmem on windows system!!!
                jvmInfo.setDefaultTransportForDebug(getHost()+getPort());
            }
        }
    }
    String lastAddress = null;
    public String getDebugAddressValue() throws java.rmi.RemoteException{
        String retVal = lastAddress;
        JvmOptions jvmInfo = null;
        try {
            jvmInfo = new JvmOptions(getMBeanServerConnection());
            retVal = jvmInfo.getAddressValue();
            lastAddress = retVal;
        } catch (java.rmi.RemoteException re) {
            if (null == lastAddress)
                throw re;
        }
        return retVal;       
    }
    
    boolean lastIsSharedMem = false;
   public boolean isDebugSharedMemory() throws java.rmi.RemoteException{
       boolean retVal = lastIsSharedMem;
        JvmOptions jvmInfo = null;
        try {
            jvmInfo = new JvmOptions(getMBeanServerConnection());
            retVal = jvmInfo.isSharedMemory();
            lastIsSharedMem = retVal;
        } catch (java.rmi.RemoteException re) {
            // there is nothing that we can do here.
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,e);
        }
        return  retVal;
      
       
   }    
//   public String getDebugAddressValue() throws java.rmi.RemoteException{
//       JvmOptions jvmInfo = new JvmOptions(getMBeanServerConnection());
//        return jvmInfo.getAddressValue();
//       
//   }
//   public boolean isDebugSharedMemory() throws java.rmi.RemoteException{
//        JvmOptions jvmInfo = new JvmOptions(getMBeanServerConnection());
//        return  jvmInfo.isSharedMemory();
//      
//       
//   }    
    public DeploymentConfiguration createConfiguration(DeployableObject dObj)
    throws javax.enterprise.deploy.spi.exceptions.InvalidModuleException {
        return new SunONEDeploymentConfiguration(dObj/*, this*/);
        //    return innerDM.createConfiguration(dObj);
    }
    
    File getInternalPlanFile(InputStream plan) throws IllegalStateException {
        JarOutputStream jar = null;
        InputStream innerPlan = null;
        
        try {
            File tmpFile = File.createTempFile("dplan","tmp");
            jar = new JarOutputStream(new FileOutputStream(tmpFile));
            Util.convert(plan,jar);
            jar.close();
            jar = null;
            tmpFile.deleteOnExit();
            return tmpFile;
        }
        catch (java.io.IOException ioe) {
            IllegalStateException ise =
            new IllegalStateException("file handling issues");
            ise.initCause(ioe);
            throw ise;
        }
        finally {
            if (jar != null)
                try {
                    jar.close();
                }
                catch (Throwable t) {
                    jsr88Logger.severe("bad one");
                }
        }
    }
    
    public ProgressObject distribute(Target[] target,
    InputStream archive, InputStream plan) throws IllegalStateException {
        InputStream innerPlan = null;
        //System.out.println("distribute.");
        ThrowExceptionIfSuspended();
      //  getStartServerInterface().viewLogFile();
        ViewLogAction.viewLog(this);

        ClassLoader origClassLoader=Thread.currentThread().getContextClassLoader();
        try {
            // do a stream distribute
            //  1. convert the plan into a zip file based plan
            //
            final File f = getInternalPlanFile(plan);
            innerPlan = new FileInputStream(f);
            //
            //  2. call the inner DeploymentManager.distribute method
            //
             Thread.currentThread().setContextClassLoader(ServerLocationManager.getServerOnlyClassLoader(getPlatformRoot()));
            ProgressObject retVal = innerDM.distribute(target, archive, innerPlan);
            retVal.addProgressListener(new FileDeleter(f));
            return retVal;
        }
        catch (IllegalStateException ise) {
            throw ise;
        }
        catch (java.io.IOException ioe) {
            IllegalStateException ise =
            new IllegalStateException("file handling issues");
            ise.initCause(ioe);
            throw ise;
        }
        finally {
            Thread.currentThread().setContextClassLoader(origClassLoader);
            if (null != innerPlan)
                try {
                    innerPlan.close();
                }
                catch (Throwable t) {
                    jsr88Logger.severe("bad two");
                }
        }
    }
    
    public ProgressObject distribute(Target[] target, File archive, File plan)
    throws IllegalStateException {
        InputStream a, p;
  //      System.out.println("distribute.2"+plan);
        ThrowExceptionIfSuspended();
        File[] resourceDirs = Utils.getResourceDirs(archive);
        if(resourceDirs != null){
            Utils.registerResources(resourceDirs, (ServerInterface)getManagement());
        }

        ClassLoader origClassLoader=Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(ServerLocationManager.getServerOnlyClassLoader(getPlatformRoot()));
      //  getStartServerInterface().viewLogFile();
        ViewLogAction.viewLog(this);
            return innerDM.distribute(target, archive, null);
           // return distribute(target, a, p);
            //}
        }
        catch (/*java.io.FileNotFoundException*/ Exception fnfe) {
            IllegalStateException ise =
            new IllegalStateException();
            ise.initCause(fnfe);
            throw ise;
        }
        finally{
            Thread.currentThread().setContextClassLoader(origClassLoader);
        }

    }
    
    public TargetModuleID[] getAvailableModules(ModuleType modType, Target[] target)
    throws TargetException, IllegalStateException {
        
        ThrowExceptionIfSuspended();
        ClassLoader origClassLoader=Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ServerLocationManager.getServerOnlyClassLoader(getPlatformRoot()));
        
        try{
            TargetModuleID[] tm =  innerDM.getAvailableModules(modType, target);
/*     	System.out.println("in getAvailableModules "+modType);
         for(int i = 0; i < target.length; i++) {
                System.out.println("Target is "+i+" "+target[i]);
         }
        for(int j = 0; j < tm.length; j++) {
                System.out.println("TargetModuleID is "+j+" "+tm[j]);
         }
     */
            return tm;
        }
        finally{
            Thread.currentThread().setContextClassLoader(origClassLoader);
        }
    }
    
    public Locale getCurrentLocale() {//TODO change the classloader there also!!! Ludo
      //  System.out.println("getCurrentLocale.");
        ThrowExceptionIfSuspended();
        return innerDM.getCurrentLocale();
    }
    
    public DConfigBeanVersionType getDConfigBeanVersion() {//TODO change the classloader there also!!! Ludo
        //System.out.println("getDConfigBeanVersion.");
        ThrowExceptionIfSuspended();
        return innerDM.getDConfigBeanVersion();
    }
    
    public Locale getDefaultLocale() {
        //System.out.println("getDefaultLocale.");
        ThrowExceptionIfSuspended();
        return innerDM.getDefaultLocale();
    }
    
    public TargetModuleID[] getNonRunningModules(ModuleType mType, Target[] target)
    throws TargetException, IllegalStateException {
        //System.out.println("getNonRunningModules.");
        ThrowExceptionIfSuspended();
        return innerDM.getNonRunningModules(mType, target);
    }
    
    public TargetModuleID[] getRunningModules(ModuleType mType, Target[] target)
    throws TargetException, IllegalStateException {
        ClassLoader origClassLoader=Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ServerLocationManager.getServerOnlyClassLoader(getPlatformRoot()));
        try{
            TargetModuleID[] ttt= innerDM.getRunningModules(mType, target);
            return ttt;
        }
        finally{
            Thread.currentThread().setContextClassLoader(origClassLoader);
        }
        
    }
    
    public Locale[] getSupportedLocales() {
        //System.out.println("getSupportedLocales.");
        ThrowExceptionIfSuspended();
        return innerDM.getSupportedLocales();
    }
    
    public Target[] getTargets() throws IllegalStateException {
        ThrowExceptionIfSuspended();
        if (secureStatusHasBeenChecked==false) //unknown status. no targets.
            return null;
        Target[] retVal = null;
        // VBK Hack for getting the configuration editing to in the origClassLoader
        // J2EE 1.4 RI beta 1 deploytool to appear. It required this call to
        // return at least 1 target in the return value.
        if (null == innerDM) {
            retVal = new Target[1];
            retVal[0] = new FakeTarget();
            return retVal;
        }
        AdminAuthenticator.setPreferredSunDeploymentManagerInterface(this);
        if (isLocal()){// if the server is local, make sure we are talking to the correct one
            //we do that by testing the server location known by the IDE with the server location known by the
            // server
            try{
                Object configDir = getManagement().invoke(new javax.management.ObjectName("ias:type=domain,category=config"),"getConfigDir", null, null);
                if (configDir==null){
                    mmm=null;
                    return null;
                }
                String dir = configDir.toString();
                File domainLocationAsReturnedByTheServer = new File(dir).getParentFile().getParentFile();
                
                String l1 =  domainLocationAsReturnedByTheServer.getCanonicalPath();
                DeploymentManagerProperties dmProps = new DeploymentManagerProperties(this);
                String domainDir =  dmProps.getLocation();
                domainDir = new File(domainDir).getCanonicalPath();

                if (l1.equals(domainDir)==false){ //not the same location, so let's make sure we do not reutrn an invalid target
                    
                    return null;
                }
            } catch (Throwable  ee) {
                ee.printStackTrace();
            }
        }
        ClassLoader origClassLoader=Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(ServerLocationManager.getServerOnlyClassLoader(getPlatformRoot()));
            retVal = innerDM.getTargets();
        } catch (IllegalStateException ise) {
            return new Target[0];
            //	throw ise;
        } finally{
            Thread.currentThread().setContextClassLoader(origClassLoader);
            
        }
        return retVal;

        
    }
    
    public boolean isDConfigBeanVersionSupported(DConfigBeanVersionType vers) {
        //System.out.println("isDConfigBeanVersionSupported.");
        ThrowExceptionIfSuspended();
        return innerDM.isDConfigBeanVersionSupported(vers);
    }
    
    public boolean isLocaleSupported(Locale locale) {
        //System.out.println("isLocaleSupported.");
        ThrowExceptionIfSuspended();
        return innerDM.isLocaleSupported(locale);
    }
    
    public boolean isRedeploySupported() {
        //System.out.println("isRedeploySupported.");
        ThrowExceptionIfSuspended();
        return innerDM.isRedeploySupported();
    }
    
    public ProgressObject redeploy(TargetModuleID[] targetModuleID,
    InputStream archive, InputStream plan)
    throws UnsupportedOperationException, IllegalStateException {
        //System.out.println("redeploy.");
        ThrowExceptionIfSuspended();

        InputStream innerPlan = null;
   //     getStartServerInterface().viewLogFile();
        ViewLogAction.viewLog(this);
        
        ClassLoader origClassLoader=Thread.currentThread().getContextClassLoader();
        try {
           Thread.currentThread().setContextClassLoader(ServerLocationManager.getServerOnlyClassLoader(getPlatformRoot()));
    //        File f = getInternalPlanFile(plan);
    //        innerPlan = new FileInputStream(f);
            ProgressObject  retVal = innerDM.redeploy(targetModuleID, archive, innerPlan);
//            retVal.addProgressListener(new FileDeleter(f));
            return retVal;
        }
        catch (IllegalStateException ise) {
            ise.printStackTrace();
            throw ise;
        }
        catch (Exception ioe) {
            ioe.printStackTrace();
            IllegalStateException ise =
            new IllegalStateException("file handling issues");
            ise.initCause(ioe);
            throw ise;
        }
        finally {
            Thread.currentThread().setContextClassLoader(origClassLoader);
            if (null != innerPlan)
                try {
                    innerPlan.close();
                }
                catch (Throwable t) {
                    t.printStackTrace();
                    jsr88Logger.severe("bad two");
                }
        }
    }
    
    public ProgressObject redeploy(TargetModuleID[] targetModuleID,
    File archive, File plan)
    throws UnsupportedOperationException, IllegalStateException {
        ThrowExceptionIfSuspended();

        File[] resourceDirs = Utils.getResourceDirs(archive);
        if(resourceDirs != null){
            Utils.registerResources(resourceDirs, (ServerInterface)getManagement());
        }

        InputStream a, p=null;
        //System.out.println("redeploy.");
        try {
            a = new FileInputStream(archive);
//            p = new FileInputStream(plan);
         }
        catch (java.io.FileNotFoundException fnfe) {
            fnfe.printStackTrace();
            IllegalStateException ise =
            new IllegalStateException();
            ise.initCause(fnfe);
            throw ise;
        }
        ViewLogAction.viewLog(this);
        
        ClassLoader origClassLoader=Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ServerLocationManager.getServerOnlyClassLoader(getPlatformRoot()));
        try{
            return  innerDM.redeploy(targetModuleID, archive, null);
            //return redeploy(targetModuleID, a, p);
        }
        finally{
            Thread.currentThread().setContextClassLoader(origClassLoader);
            
        }
    }
    
    public void release() {
        //System.out.println("release.");
       /* try{
            innerDM.release();
        }catch (Exception  e){
            //npe there f called 2 times in a row from studio...
            //need to talk to Nam
        }*/
        
    }
    
    public void setDConfigBeanVersion(DConfigBeanVersionType versionType)
    throws DConfigBeanVersionUnsupportedException {
       // System.out.println("setDConfigBeanVersion.");
        ThrowExceptionIfSuspended();
        innerDM.setDConfigBeanVersion(versionType);
    }
    
    public void setLocale(Locale locale)
    throws UnsupportedOperationException {
       // System.out.println("setLocale.");
        ThrowExceptionIfSuspended();
        innerDM.setLocale(locale);
    }
    
    public ProgressObject start(TargetModuleID[] targetModuleID)
    throws IllegalStateException {
        ThrowExceptionIfSuspended();
        ClassLoader origClassLoader=Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ServerLocationManager.getServerOnlyClassLoader(getPlatformRoot()));
        try{
            return  innerDM.start(targetModuleID);
        }
        finally{
            Thread.currentThread().setContextClassLoader(origClassLoader);
            
        }
        
        
    }
    
    public ProgressObject stop(TargetModuleID[] targetModuleID)
    throws IllegalStateException {
        ThrowExceptionIfSuspended();
        ClassLoader origClassLoader=Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ServerLocationManager.getServerOnlyClassLoader(getPlatformRoot()));
        try{ 
            return  innerDM.stop(targetModuleID);
        }
        finally{
            Thread.currentThread().setContextClassLoader(origClassLoader);
            
        }
        
        
        
    }
    
    public ProgressObject undeploy(TargetModuleID[] targetModuleID)
    throws IllegalStateException {
       // System.out.println("undeploy.");
        ThrowExceptionIfSuspended();
        ClassLoader origClassLoader=Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ServerLocationManager.getServerOnlyClassLoader(getPlatformRoot()));
        
        try{
            return innerDM.undeploy(targetModuleID);
        }
        
        finally{
            Thread.currentThread().setContextClassLoader(origClassLoader);
            
        }
    }
    
    
    private void calculateIsLocal(){
        if (getHost().equals("localhost"))
        {
            isLocal =true;
            return;
        }
        try {
            new Thread() {
                public void run() {
                    try {
                        String ia = InetAddress.getByName(getHost()).getHostAddress();
                        if(ia.equals("127.0.0.1")){//NOI18N
                             isLocal = true;
                             return;
                        }
                        
                        String localCanonName = InetAddress.getLocalHost().getCanonicalHostName();
                        String currentCanonName = InetAddress.getByName(getHost()).getCanonicalHostName();
                        
                        isLocal =  (localCanonName.equals(currentCanonName) ) ? true : false;
                        return;
                    } catch (Exception e) {
                       // e.printStackTrace();
                    }
                }
            }.start();
            
        }
        catch (Throwable t) {
           // t.printStackTrace(); wll default to false.
        }
    }
    
    /* return true if the server instance is locale
     **/
    public boolean isLocal() {
        
        return isLocal; //the cached value
        
    }
        
      //  return LOCALHOST.equals(getHost());

    
     /* return the status of an instance.
      * It is optimized to return the previous status if called more than twice within
      * a 5 seconds intervall
      * This boosts IDE reactivity
      */
    public boolean isRunning() {
        return isRunning(false);
    
    }
     /* return the status of an instance.
      * when not forced, It is optimized to return the previous status if called more than twice within
      * a 4 seconds intervall
      * This boosts IDE reactivity
      */
    public boolean isRunning(boolean forced) {

        if (isSuspended())
        return true;
        long current=System.currentTimeMillis();
      //  System.out.println("in in running call"+ (current-timeStampCheckingRunning));
        if (forced==false)
            if (current-timeStampCheckingRunning<4000){
            //  System.out.println("Cached in in running call");
            //timeStampCheckingRunning = current;
            return runningState;
            }
        boolean newrunningState = false; 
        timeStampCheckingRunning = current;
        
        try {          
            
            Target[] t= getTargets();
            if (t != null) {
                if (t.length==0)
                    newrunningState = false;
                else
                    
                    newrunningState = true;
            }

           //System.out.println("isRunning" +runningState);
        } catch (Throwable /*IllegalStateException*/ e) {
            newrunningState  =false;
            //System.out.println(" bisRunning" +runningState);
       }
        if(newrunningState!=runningState){
            // state changed
            new DeploymentManagerProperties(this).refreshServerInstance();
        }
        runningState = newrunningState;
        if ((runningState)&&(nonAdminPortNumber == null)){
            try{
      //  System.out.println("inrunning get admin port number"+(System.currentTimeMillis()-current));
                nonAdminPortNumber =  (String)getManagement().getAttribute(new javax.management.ObjectName("com.sun.appserv:type=http-listener,id=http-listener-1,config=server-config,category=config") ,"port");
              //  sharedMemoryName = getDebugAddressValueReal();
            } catch (Throwable /*IllegalStateException*/ ee) {
            ee.printStackTrace();
           }            
        }
        timeStampCheckingRunning = System.currentTimeMillis();
//        System.out.println("startinsruning"+(timeStampCheckingRunning-current));

       return runningState;
    }
    

    

    
    public void  ThrowExceptionIfSuspended(){
        
        /* this is called before any remote call, so it's a good place to do this extra check about being secure of not For EE version
         ** and accordingly set the environment correctly */

        if (secureStatusHasBeenChecked == false) {
            long current=System.currentTimeMillis();
            mmm=null;
            try{
                if(isGlassFish)
                    secure=PortDetector.isSecurePortGlassFish(getHost(),getPort());
                else
                    secure=PortDetector.isSecurePort(getHost(),getPort());
                    
                if (secure==true){
                    if (!uri.endsWith(SECURESTRINGDETECTION)){
                        uri=uri+SECURESTRINGDETECTION;//make it secure and reset the inner one
                    }
                    resetInnerDeploymentManager();
                    //System.out.println("Setting as Secure!!!!!");
                    
                }
                //System.out.println("secure="+secure);
                secureStatusHasBeenChecked = true;// check done!!!
                 
            } catch(Exception e){
                //Cannot detect if it's secure of not yet..
                // could be IOException, ConnectException, SocketTimeoutException
              //  System.out.println("timeout "+( System.currentTimeMillis()-current));
              //  System.out.println("caanot check secure");
                secureStatusHasBeenChecked = false;
                //System.out.println("could be IOException, ConnectException, SocketTimeoutException");
                //e.printStackTrace();
            }
        }
            if (isSuspended()){

                //System.out.println("CANNOT DO A remote operation  WHILE STOPPED IN A BREAK POINT IN DEBUG MODE...");
                throw new RuntimeException(bundle.getString("MSG_ServerInDebug")) ;
            }

    }
    
    
    /**
     * Returns true if this server is started in debug mode AND debugger is attached to it 
     * AND threads are suspended (e.g. debugger stopped on breakpoint)
     */
    public boolean isSuspended() {
        return org.netbeans.modules.j2ee.sun.ide.j2ee.StartSunServer.isSuspended(this);
    }      
    
    
   


    private ServerInterface mmm=null;
    
    public ServerInterface getManagement() {
        if(mmm==null){
            
        try{
            Class[] argClass = new Class[1];
            argClass[0] = javax.enterprise.deploy.spi.DeploymentManager.class;
            Object[] argObject = new Object[1];
            argObject[0] = this;
            
            ClassLoader loader = getExtendedClassLoader();
            if(loader != null){
                Class cc = loader.loadClass("org.netbeans.modules.j2ee.sun.share.management.ServerMEJB");
                mmm = (ServerInterface)cc.newInstance();
                java.lang.reflect.Method setDeploymentManager = cc.getMethod("setDeploymentManager", argClass);//NOI18N
                setDeploymentManager.invoke(mmm, argObject);
            }
        }catch(Exception ex){
            //Suppressing exception while trying to obtain admin host port value
            ex.printStackTrace();
        }           
            
            
           /// mmm = new ServerMEJB(this);
        }
        return mmm;
    }
    
    private ResourceConfiguratorInterface resourceConfigurator = null;
    
    public ResourceConfiguratorInterface getResourceConfigurator() {
        if(resourceConfigurator == null){
            try{
                Class[] argClass = new Class[1];
                argClass[0] = javax.enterprise.deploy.spi.DeploymentManager.class;
                Object[] argObject = new Object[1];
                argObject[0] = this;
                
                ClassLoader loader = getExtendedClassLoader();
                if(loader != null){
                    Class cc = loader.loadClass("org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ResourceConfigurator");
                    resourceConfigurator = (ResourceConfiguratorInterface)cc.newInstance();
                    java.lang.reflect.Method setDeploymentManager = cc.getMethod("setDeploymentManager", argClass);//NOI18N
                    setDeploymentManager.invoke(resourceConfigurator, argObject);
                }
            }catch(Exception ex){
                //Suppressing exception
                //return will be a null value for resourceConfigurator
            }
        }
        return resourceConfigurator;
    }
    
    public javax.management.MBeanServerConnection getMBeanServerConnection() throws RemoteException, ServerException{
        ServerInterface serverMgmt = getManagement();
        return (javax.management.MBeanServerConnection)serverMgmt.getMBeanServerConnection();
    }
    



    public boolean isSecure() {
        return secure;
    }


    
    
    
    static String getHostFromURI(String uri) {
        String retVal = null;
        try {
            int len1 = uri.lastIndexOf(':');
            //int uriLength = uri.length();
            String partial = uri.substring(0,len1);
            int len2 = partial.lastIndexOf(':');
            
            retVal =  uri.substring(len2+1,len1);
        } catch (Throwable t) {
            jsr88Logger.warning("getHostFromURI:: invalid uri: "+ uri);
        }
        return retVal;
    }
    
    static int getPortFromURI(String uri) {
        int retVal = -1;
        int len1 = uri.lastIndexOf(':');
        if (-1 == len1)
            return retVal;
        //int uriLength = uri.length();
        try {
            retVal = Integer.parseInt(uri.substring(len1+1));
        } catch (NumberFormatException nfe) {
            jsr88Logger.warning(nfe.getMessage());
        }
        return retVal;
    }  
    
    
    
    public void  refreshDeploymentManager(){
        try{
            resetInnerDeploymentManager();
        }catch(Exception ex)   {
        }
    }
    
    
    public File getPlatformRoot() {
        return  platformRoot;
    }

    private ClassLoader getExtendedClassLoader(){
	
	return ServerLocationManager.getNetBeansAndServerClassLoader(getPlatformRoot());	
    }

    public void setUserName(String name) {
        mmm = null;
        userName = name;
    }

    public void setPassword(String pw) {
        mmm= null;
        password = pw;
        refreshDeploymentManager();
        
    }
    
    // VBK hack target objects to support configuration prototyping in
     // J2EE 1.4 RI beta 1 deploytool
     class FakeTarget implements Target {
         
         public String getDescription() {
             return "fakeTargetDescr";
         }
                  public String getName() {
             return "fakeTargetName";
         }
         
     }
     
     class FileDeleter implements ProgressListener {
         File f;
         public FileDeleter(File f) {
             this.f = f;
         }
         
         public void handleProgressEvent(ProgressEvent pe) {
             DeploymentStatus ds = pe.getDeploymentStatus();
             if (ds.isCompleted() || ds.isFailed()) {
                 boolean complete = ds.isCompleted();
                 f.delete();
             }
         }
     }    
    
}
