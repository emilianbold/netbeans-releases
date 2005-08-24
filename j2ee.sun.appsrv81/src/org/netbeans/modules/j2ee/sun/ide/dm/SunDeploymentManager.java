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
package org.netbeans.modules.j2ee.sun.ide.dm;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Locale;
import java.io.IOException;
import java.net.Socket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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

import org.netbeans.modules.j2ee.sun.appsrvapi.PortDetector;
import org.openide.ErrorManager;


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


    public SunDeploymentManager( DeploymentFactory df, String uri,String userName, String password)  throws DeploymentManagerCreationException{
        this.df= df;
        this.uri =uri;
        secure = uri.endsWith(SECURESTRINGDETECTION);
        String uriNonSecure =uri;
        if (secure)
            uriNonSecure = uri.substring(0,  uri.length()- SECURESTRINGDETECTION.length());
        
        host = getHostFromURI(uriNonSecure);
        adminPortNumber = getPortFromURI(uriNonSecure);
        try {
            InstanceProperties props = InstanceProperties.getInstanceProperties("deployer:Sun:AppServer::"+host+":"+adminPortNumber); //NOI18N
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
    
    public DeploymentManager getInnerDeploymentManager()  {
        return innerDM;
    }
    
    private void resetInnerDeploymentManager() throws DeploymentManagerCreationException{
        try {
            if (isConnected){
                innerDM = df.getDeploymentManager(uri,userName,password);
            } else{
                innerDM = df.getDisconnectedDeploymentManager(uri);
                
            }
            if(innerDM == null) {
                throw new DeploymentManagerCreationException("invalid URI");
            }
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
                
                 org.netbeans.modules.j2ee.sun.ide.ExtendedClassLoader loader =  org.netbeans.modules.j2ee.sun.ide.Installer.getClassLoader();
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
        return userName;
    }
    
    public String getPassword() {
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
   public String getDebugAddressValue() throws java.rmi.RemoteException{
       JvmOptions jvmInfo = new JvmOptions(getMBeanServerConnection());
        return jvmInfo.getAddressValue();
       
   }
   public boolean isDebugSharedMemory() throws java.rmi.RemoteException{
        JvmOptions jvmInfo = new JvmOptions(getMBeanServerConnection());
        return  jvmInfo.isSharedMemory();
      
       
   }    
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
             Thread.currentThread().setContextClassLoader(org.netbeans.modules.j2ee.sun.ide.Installer.getPluginLoader());
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
            Thread.currentThread().setContextClassLoader(org.netbeans.modules.j2ee.sun.ide.Installer.getPluginLoader());
      //  getStartServerInterface().viewLogFile();
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
        Thread.currentThread().setContextClassLoader(org.netbeans.modules.j2ee.sun.ide.Installer.getPluginLoader());
        
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
        Thread.currentThread().setContextClassLoader(org.netbeans.modules.j2ee.sun.ide.Installer.getPluginLoader());
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
        Target[] retVal = null;
        // VBK Hack for getting the configuration editing to in the origClassLoader
        // J2EE 1.4 RI beta 1 deploytool to appear. It required this call to
        // return at least 1 target in the return value.
        if (null == innerDM) {
            retVal = new Target[1];
            retVal[0] = new FakeTarget();
            return retVal;
        }
        else {
            ClassLoader origClassLoader=Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(org.netbeans.modules.j2ee.sun.ide.Installer.getPluginLoader());
                retVal = innerDM.getTargets();
            }
            catch (IllegalStateException ise) {
                return new Target[0];
                //	throw ise;
            }
            finally{
                Thread.currentThread().setContextClassLoader(origClassLoader);
                
            }
            return retVal;
        }
        
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
        
        ClassLoader origClassLoader=Thread.currentThread().getContextClassLoader();
        try {
           Thread.currentThread().setContextClassLoader(org.netbeans.modules.j2ee.sun.ide.Installer.getPluginLoader());
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
        ClassLoader origClassLoader=Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(org.netbeans.modules.j2ee.sun.ide.Installer.getPluginLoader());
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
        Thread.currentThread().setContextClassLoader(org.netbeans.modules.j2ee.sun.ide.Installer.getPluginLoader());
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
        Thread.currentThread().setContextClassLoader(org.netbeans.modules.j2ee.sun.ide.Installer.getPluginLoader());
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
        Thread.currentThread().setContextClassLoader(org.netbeans.modules.j2ee.sun.ide.Installer.getPluginLoader());
        
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
        long current=System.currentTimeMillis();
        if (current-timeStampCheckingRunning<7000){
            timeStampCheckingRunning = current;
            return runningState;
        }
        timeStampCheckingRunning = current;
        runningState = false; // simpleConnect(getHost(),getPort());
        
        try {

           Target[] t= getTargets();
            if (t != null) {
                if (t.length==0)
                    runningState = false;
                else
                    
                    runningState = true;
            }            
        } catch (Throwable /*IllegalStateException*/ e) {
            runningState  =false;
        }
        if ((runningState)&&(nonAdminPortNumber == null)){
            try{
                nonAdminPortNumber =  (String)getManagement().getAttribute(new javax.management.ObjectName("com.sun.appserv:type=http-listener,id=http-listener-1,config=server-config,category=config") ,"port");
            } catch (Throwable /*IllegalStateException*/ ee) {
            ee.printStackTrace();
           }            
        }
        return runningState;
    }
    
    
    /* quickly test is 'something' is running a the jsotname:port.
     * if true, further trst need to be done to see if it's *PE or not
     */
    private  boolean simpleConnect(String host, int port) {
        try {
            if (secure==true)
                return false;
            InetSocketAddress isa = new InetSocketAddress(InetAddress.getByName(host), port);
            Socket socket = new Socket();
            socket.connect(isa, 500);
            socket.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /* may return null
     * or returns a netbeans specific class that implements the StartServer interface from j2eeserver
     * need to keep the mapping between a DM and a StartServer object
     */
    
    public SunServerStateInterface getStartServerInterface() {
        return startServerInterface;
    }
    
    public void ThrowExceptionIfSuspended(){
        
        /* this is called before any remote call, so it's a good place to do this extra check about being secure of not For EE version
         ** and accordingly set the environment correctly */
        if (secureStatusHasBeenChecked == false) {
            try{
                if (PortDetector.isSecurePort(getHost(),getPort())){
                    secure =true;
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
                secureStatusHasBeenChecked = false;
                //System.out.println("could be IOException, ConnectException, SocketTimeoutException");
                //e.printStackTrace();
            }
        }
        SunServerStateInterface ssrv = getStartServerInterface();
        if(ssrv!=null){
            if (ssrv.isSuspended()){
                //System.out.println("CANNOT DO A remote operation  WHILE STOPPED IN A BREAK POINT IN DEBUG MODE...");
                throw new RuntimeException(bundle.getString("MSG_ServerInDebug")) ;
            }
            
        }
    }
    
     /* used by a netbeans extension to associatate a StartServer with this DM
      */
    public void setStartServerInterface(SunServerStateInterface o) {
        startServerInterface = o;
    }
    private ServerInterface mmm=null;
    
    public ServerInterface getManagement() {
        if(mmm==null){
            
        try{
            Class[] argClass = new Class[1];
            argClass[0] = javax.enterprise.deploy.spi.DeploymentManager.class;
            Object[] argObject = new Object[1];
            argObject[0] = this;
            
            org.netbeans.modules.j2ee.sun.ide.ExtendedClassLoader loader = org.netbeans.modules.j2ee.sun.ide.Installer.getPluginLoader();
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
