/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.j2ee.sun.ide.dm;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.Authenticator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.ResourceBundle;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
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
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
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
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.j2ee.sun.share.plan.Util;



import org.netbeans.modules.j2ee.sun.ide.j2ee.Utils;

import org.netbeans.modules.j2ee.sun.api.ServerInterface;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.sun.api.ResourceConfiguratorInterface;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.sun.api.CmpMappingProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

import org.openide.ErrorManager;
import org.netbeans.modules.j2ee.sun.api.ServerLocationManager;
import org.netbeans.modules.j2ee.sun.ide.ShortCircuitProgressObject;
import org.netbeans.modules.glassfish.eecommon.api.DomainEditor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
/**
 *
 * , vkraemer
 */
public class SunDeploymentManager implements Constants, DeploymentManager, SunDeploymentManagerInterface {
    
    private PropertyChangeSupport propertySupport;
    private DeploymentManager innerDM;
    private DeploymentFactory df;
    private String host, userName, password;
    private String uri;
    private boolean isConnected;
    /* map that give a password for a given uri
     **/
    private static Map passwordForURI = Collections.synchronizedMap((Map)new HashMap(2,1));
    
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
    private boolean goodUserNamePassword =false;
    private boolean maybeRunningButWrongUserName =false;
    private long timeStampCheckingRunning =0;
    private File platformRoot  =null;
    private String domainDir = null;
    private boolean driversdeployed=false;
    
    /* cache for local value. Sometimes, the islocal() call can be very long for IP that changed
     * usually when dhcp is used. So we calculate the value in a thread at construct time
     * init value is false, but updated later within a thread.
     **/
    private boolean isLocal = false;
    /* used by a netbeans extension to associatate a StartServer with this DM
     */
    static final ResourceBundle bundle = ResourceBundle.getBundle("org.netbeans.modules.j2ee.sun.ide.dm.Bundle");// NOI18N
    
    // This code allows the extra classes missing from the 8.x appserv-cmp.jar to be
    // loaded from the copy in the appsrvbridge module.  If this code were not here, 
    // netbeans rules of one jar per package would prevent those classes from being
    // found.
    static {
        String packageWorkaroundPropName = "org.netbeans.core.startup.specialResource"; // NOI18N
        String existingSysProp = System.getProperty(packageWorkaroundPropName);
        String extraPackageName = "com.sun.jdo.api.persistence.mapping.ejb"; // NOI18N
        if (existingSysProp != null) {
            existingSysProp += ',';
        }
        existingSysProp += extraPackageName;
        System.setProperty(packageWorkaroundPropName, existingSysProp);
    }

    /* this is interesting: JMXREMOTE APIs create a factory and load it from the getContext classloader
     * which is a NetBeans class loader that does not know about our App Server Dynamic jars class loader.
     * So we need to hack on the fly the context classloader with the plugin classloader for any jxm calls
     * done in the app server jsr 88 impl!!!
     * this ClassLoader origClassLoader is used to stored the original CL so that NB is happy after a jsr88 call.
     **/
    
    
    public SunDeploymentManager( DeploymentFactory df, String uri,String userName, String password, File platformRootDir)  throws DeploymentManagerCreationException{
        propertySupport = new PropertyChangeSupport(this);
        this.df= df;
        this.uri =uri;
        this.platformRoot =  platformRootDir;
        secure = uri.endsWith(SECURESTRINGDETECTION);
        String uriNonSecure =uri;
        if (secure){
            uriNonSecure = uri.substring(0,  uri.length()- SECURESTRINGDETECTION.length());
        }
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
                this.password = ""; // DeploymentManagerProperties.getPassword(props);
            } else {
                this.password = password;
            }
            if (!"".equals(this.password)){
                passwordForURI.put(uri+platformRoot,this.password);
            } else {
                this.password = (String) passwordForURI.get(uri+platformRoot);
                if (this.password==null) {
                    this.password="";
                }
                
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
                if (df!=null) {
                    innerDM = df.getDeploymentManager(uri,userName,getPassword());
                }
            } else{
                if (df!=null) {
                    innerDM = df.getDisconnectedDeploymentManager(uri);
                }
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
        if (null != props) {
            this.userName = props.getProperty(InstanceProperties.USERNAME_ATTR);
        }
        return userName;
    }
    
    public String getPassword() {
        InstanceProperties props = SunURIManager.getInstanceProperties(platformRoot, host, adminPortNumber);
        if (null != props) {
            this.password = DeploymentManagerProperties.getPassword(props);
        }
        if ("".equals(password)) { // NOI18N
            //it means we did not stored the password. Get it from the static in memory cache if available
            password = (String) passwordForURI.get(uri+platformRoot);
            if (this.password==null) {
                this.password=""; // NOI18N
            }
            
        }
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
    synchronized public boolean isRestartNeeded(){
        if (secureStatusHasBeenChecked==false){
            return false;
        }
        
        if (goodUserNamePassword==false){
            return false;
        }
        if (isRunning()==false){
            return false;
        }
        if (driversdeployed==true){
            setRestartForDriverDeployment(false);
            return true;
        }
        
        boolean retVal;
        try{
            ServerInfo si = new ServerInfo(getMBeanServerConnection());
            retVal = si.isRestartRequired();
        } catch (java.rmi.RemoteException e){
            retVal = false;//silent assumption: server mgiht not even be running!
        }
        return retVal;
    }
    
    
    public void fixJVMDebugOptions() throws java.rmi.RemoteException{
        JvmOptions jvmInfo = new JvmOptions(getMBeanServerConnection());
        //String addr=
        jvmInfo.getAddressValue();
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
            if (null == lastAddress){
                throw re;
            }
            
        }
        return retVal;
    }
    
    boolean lastIsSharedMem = false;
    public boolean isDebugSharedMemory() throws java.rmi.RemoteException{
        if (!isLocal()) {
            return false;
        }
        boolean retVal = lastIsSharedMem;
        JvmOptions jvmInfo = null;
        try {
            jvmInfo = new JvmOptions(getMBeanServerConnection());
            retVal = jvmInfo.isSharedMemory();
            lastIsSharedMem = retVal;
        } catch (java.rmi.RemoteException re) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    re);
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
//        InputStream innerPlan = null;
        
        try {
            File tmpFile = File.createTempFile("dplan","tmp");
            jar = new JarOutputStream(new FileOutputStream(tmpFile));
            Util.convert(plan,jar);
            jar.close();
            jar = null;
            tmpFile.deleteOnExit();
            return tmpFile;
        } catch (java.io.IOException ioe) {
            IllegalStateException ise =
                    new IllegalStateException("file handling issues");
            ise.initCause(ioe);
            throw ise;
        } finally {
            if (jar != null){
                try {
                    jar.close();
                } catch (Throwable t) {
                    jsr88Logger.severe("bad one");
                }
            }
        }
    }
    
    
//    // TODO remove once https://glassfish.dev.java.net/issues/show_bug.cgi?id=601601 is not an issue...
//    // after 8.1, 8.2, and 9.0 PE are no loger supported.
//    //
//    private static ProgressObject tempDirFailureObject =
//            new ShortCircuitProgressObject(CommandType.DISTRIBUTE,
//            NbBundle.getMessage(SunDeploymentManager.class,"ERR_BAD_TEMP_DIR"),
//            StateType.FAILED,new TargetModuleID[0]);
//
//    private boolean badTempDir() {
//        if (!Utilities.isWindows()) {
//            return false;
//        } else {
//            // TODO perform the real test here!
//            return false;
//        }
//    }
//    // end remove after https://glassfish.dev.java.net/issues/show_bug.cgi?id=601 resolved
    
    
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
            ProgressObject retVal;
//            if (badTempDir()) {
//                retVal = tempDirFailureObject;
//            } else {
            Thread.currentThread().setContextClassLoader(ServerLocationManager.getServerOnlyClassLoader(getPlatformRoot()));
            retVal = innerDM.distribute(target, archive, innerPlan);
            retVal.addProgressListener(new FileDeleter(f));
//            }
            return retVal;
            
            
        } catch (IllegalStateException ise) {
            throw ise;
        } catch (java.io.IOException ioe) {
            IllegalStateException ise =
                    new IllegalStateException("file handling issues");
            ise.initCause(ioe);
            throw ise;
        } finally {
            Thread.currentThread().setContextClassLoader(origClassLoader);
            if (null != innerPlan){
                try {
                    innerPlan.close();
                } catch (Throwable t) {
                    jsr88Logger.severe("bad two");
                }
            }
        }
    }
    
    public ProgressObject distribute(Target[] target, ModuleType moduleType, InputStream inputStream, InputStream inputStream0) throws IllegalStateException {
        return distribute(target, inputStream, inputStream0);
    }
    
    public ProgressObject distribute(Target[] target, File archive, File plan)
    throws IllegalStateException {
        ThrowExceptionIfSuspended();

        archive = FileUtil.normalizeFile(archive);
        File[] resourceDirs = Utils.getResourceDirs(archive);
        if(resourceDirs != null){
            Utils.registerResources(resourceDirs, (ServerInterface)getManagement());
        }
        
        ClassLoader origClassLoader=Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(ServerLocationManager.getServerOnlyClassLoader(getPlatformRoot()));
            //  getStartServerInterface().viewLogFile();
            ViewLogAction.viewLog(this);
//            if (badTempDir()) {
//                return tempDirFailureObject;
//            }
            return innerDM.distribute(target, archive, null);
            // return distribute(target, a, p);
            //}
        } catch (/*java.io.FileNotFoundException*/ Exception fnfe) {
            IllegalStateException ise =
                    new IllegalStateException();
            ise.initCause(fnfe);
            throw ise;
        } finally{
            Thread.currentThread().setContextClassLoader(origClassLoader);
        }
        
    }

    private static final String MAGIC_HACK_CONST =  "org.jvnet.glassfish.comms.deployment.backend.SipArchiveDeployer";

    
    public TargetModuleID[] getAvailableModules(ModuleType modType, Target[] target)
    throws TargetException, IllegalStateException {
        try {
            ThrowExceptionIfSuspended();
        } catch (RuntimeException re) {
            return new TargetModuleID[0];
        }
        ClassLoader origClassLoader=Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ServerLocationManager.getServerOnlyClassLoader(getPlatformRoot()));
        Thread holder = Thread.currentThread();
        try {
            try {
                grabInnerDM(holder,false);
                TargetModuleID[] tm =  innerDM.getAvailableModules(modType, target);

                // This scary looking bit of introspection
                // forces the server jar side of the bridge
                // initialize to support the SipArchiveDeployer
                //
                if (ModuleType.WAR.equals(modType)) {
                    // watchout for SIP stuff here.
                    Class xmt = null;
                    try {
                            xmt = getExtendedClassLoader().loadClass("com.sun.enterprise.deployment.util.XModuleType");
                    } catch (ClassNotFoundException cnfe) {
                        Logger.getLogger(SunDeploymentManager.class.getName()).log(Level.INFO, null, cnfe);
                    }
                    if (null == xmt) {
                        return tm;
                    }
                    Method m = null;
                    try {
                        m = xmt.getMethod("getModuleType", String.class);
                    } catch (NoSuchMethodException nsme) {
                        Logger.getLogger(SunDeploymentManager.class.getName()).log(Level.INFO, null, nsme);
                     }
                    if (null == m) {
                        return tm;
                    }
                    Object mt = null;
                    try {
                        mt = m.invoke(null,MAGIC_HACK_CONST);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(SunDeploymentManager.class.getName()).log(Level.INFO, null, ex);
                    } catch (IllegalArgumentException ex) {
                        Logger.getLogger(SunDeploymentManager.class.getName()).log(Level.INFO, null, ex);
                    } catch (InvocationTargetException ex) {
                        Logger.getLogger(SunDeploymentManager.class.getName()).log(Level.INFO, null, ex);
                    }
                    if (null == mt) {
                        return tm;
                    }
                    Integer v = null;
                    try {
                        m = mt.getClass().getMethod("getValue", new Class[] { });
                        v = (Integer) m.invoke(mt, new Object[] {});
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(SunDeploymentManager.class.getName()).log(Level.INFO, null, ex);
                    } catch (IllegalArgumentException ex) {
                        Logger.getLogger(SunDeploymentManager.class.getName()).log(Level.INFO, null, ex);
                    } catch (InvocationTargetException ex) {
                        Logger.getLogger(SunDeploymentManager.class.getName()).log(Level.INFO, null, ex);
                    }  catch (NoSuchMethodException ex) {
                        Logger.getLogger(SunDeploymentManager.class.getName()).log(Level.INFO, null, ex);
                    } catch (SecurityException ex) {
                        Logger.getLogger(SunDeploymentManager.class.getName()).log(Level.INFO, null, ex);
                    }
                    if (null == v) {
                        return tm;
                    }
                    TargetModuleID[] stm =  innerDM.getAvailableModules(new MyMT(v.intValue()), target);
                    if (null != stm && stm.length > 0) {
                        List<TargetModuleID> l = new ArrayList<TargetModuleID>();
                        for (TargetModuleID e: tm) {
                            l.add(e);
                        }
                        for (TargetModuleID e: stm) {
                            l.add(e);
                        }
                        return l.toArray(new TargetModuleID[l.size()]);
                    } else {
                        return tm;
                    }
                } else {
                    return tm;
                }
            } finally {
                releaseInnerDM(holder);
            }
        } finally{
            Thread.currentThread().setContextClassLoader(origClassLoader);
        }
    }

    static class MyMT extends ModuleType {
        MyMT(int foo) {
            super(foo);
        }

        @Override
        protected ModuleType[] getEnumValueTable() {
            throw new UnsupportedOperationException();
        }

        @Override
        protected int getOffset() {
            throw new UnsupportedOperationException();
        }

        @Override
        protected String[] getStringTable() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return MAGIC_HACK_CONST;
        }

        @Override
        public int getValue() {
            return super.getValue();
        }
        @Override
        public String getModuleExtension() {
            return "";
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
        Thread holder = Thread.currentThread();
        try{
            try {
                grabInnerDM(holder,false);
                TargetModuleID[] ttt= innerDM.getRunningModules(mType, target);
                return ttt;
            } finally {
                releaseInnerDM(holder);
            }
        } finally{
            Thread.currentThread().setContextClassLoader(origClassLoader);
        }
        
    }
    
    public Locale[] getSupportedLocales() {
        //System.out.println("getSupportedLocales.");
        ThrowExceptionIfSuspended();
        return innerDM.getSupportedLocales();
    }
    
    synchronized public Target[] getTargets() throws IllegalStateException {
        try {
            ThrowExceptionIfSuspended();
        } catch (Exception ex) {
            return new Target[0];
        }
        Target[] retVal = null;
        Thread holder = Thread.currentThread();
        if (secureStatusHasBeenChecked==false){ //unknown status. no targets.
            retVal = null;
        } else {
            // VBK Hack for getting the configuration editing to in the origClassLoader
            // J2EE 1.4 RI beta 1 deploytool to appear. It required this call to
            // return at least 1 target in the return value.
            if (null == innerDM) {
                retVal = new Target[1];
                retVal[0] = new FakeTarget();
//            return retVal;
            } else {
                try {
                    grabInnerDM(holder,false);
                    if (null == domainDir) {
                        DeploymentManagerProperties dmProps = new DeploymentManagerProperties(this);
                        domainDir =  dmProps.getLocation();
                        if ("".equals(domainDir)) {
                            isLocal = false;
                        }
                        String domainName = dmProps.getDomainName();
                        domainDir += File.separator +
                                domainName;
                        domainDir = new File(domainDir).getCanonicalPath();
                    }
                    if (isLocal()){// if the server is local, make sure we are talking to the correct one
                        //we do that by testing the server location known by the IDE with the server location known by the
                        // server
                        
                        try{
                            Object configDir = getManagement().invoke(new javax.management.ObjectName("com.sun.appserv:type=domain,category=config"),"getConfigDir", null, null);
                            if (configDir==null){
                                mmm=null;
                                return null;
                            }
                            String dir = configDir.toString();
                            File domainLocationAsReturnedByTheServer = new File(dir).getParentFile();
                            
                            String l1 =  domainLocationAsReturnedByTheServer.getCanonicalPath();
                            
                            if (l1.equals(domainDir)==false){ //not the same location, so let's make sure we do not return an invalid target
                                
                                return null;
                            }
                        } catch (java.rmi.RemoteException  ee) {
                            return null;//cannot talk to the admin server->not running well
                        } catch (Throwable  ee) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                    ee);
                            return null;//cannot talk to the admin server->not running well
                        }
                    }
                    ClassLoader origClassLoader=Thread.currentThread().getContextClassLoader();
                    try {
                        Thread.currentThread().setContextClassLoader(ServerLocationManager.getServerOnlyClassLoader(getPlatformRoot()));
                        retVal = innerDM.getTargets();
                    } catch (IllegalStateException ise) {
                        retVal = new Target[0];
                        //	throw ise;
                    } finally{
                        Thread.currentThread().setContextClassLoader(origClassLoader);
                        
                    }
                } catch (IOException ioe) {
                    Logger.getLogger(SunDeploymentManager.class.getName()).log(Level.FINER, null, ioe);
                } finally {
                    releaseInnerDM(holder);
                }
            }
        }
        
        // help prevent running into GF issue 3693
        //
        // By returning a single target, we prevent the j2eeserver layer from 
        // deciding that the IDE needs to use archive deployment.  If the IDE 
        // uses JSR 88 API's to deploy to GF, the call to dm.start() will 
        // cause problems.
        // 
        // If the plugin needs to support multiple targets, this will need
        // to be revisited.
        //
        if (null != retVal && retVal.length > 1) {
            return new Target[] { retVal[0] };
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
        ProgressObject  retVal = null;
        Thread holder = Thread.currentThread();
        try {
            Thread.currentThread().setContextClassLoader(ServerLocationManager.getServerOnlyClassLoader(getPlatformRoot()));
            //        File f = getInternalPlanFile(plan);
            //        innerPlan = new FileInputStream(f);
            grabInnerDM(holder,false);
            retVal = innerDM.redeploy(targetModuleID, archive, innerPlan);
            if (null != retVal) {
                retVal.addProgressListener(new ReleaseInnerDMPL(holder));
                // the server might have finished before we could register the 
                // listener....
                if (!retVal.getDeploymentStatus().isRunning()) {
                    releaseInnerDM(holder);
                }
            }
            return retVal;
        } catch (IllegalStateException ise) {
            releaseInnerDM(holder);
            throw ise;
        } catch (Exception ioe) {
            IllegalStateException ise =
                    new IllegalStateException("file handling issues");
            ise.initCause(ioe);
            releaseInnerDM(holder);
            throw ise;
        } finally {
            if (null == retVal) {
                    releaseInnerDM(holder);
            }
            Thread.currentThread().setContextClassLoader(origClassLoader);
            if (null != innerPlan){
                try {
                    innerPlan.close();
                } catch (Throwable t) {
                    t.printStackTrace();
                    jsr88Logger.severe("bad two");
                }
            }
        }
    }
    
    public ProgressObject redeploy(TargetModuleID[] targetModuleID,
            File archive, File plan)
            throws UnsupportedOperationException, IllegalStateException {
        ThrowExceptionIfSuspended();
        
        archive = FileUtil.normalizeFile(archive);
        File[] resourceDirs = Utils.getResourceDirs(archive);
        if(resourceDirs != null){
            Utils.registerResources(resourceDirs, (ServerInterface)getManagement());
        }
        
        ViewLogAction.viewLog(this);
        
        ClassLoader origClassLoader=Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ServerLocationManager.getServerOnlyClassLoader(getPlatformRoot()));
        ProgressObject retVal = null;
        Thread holder = Thread.currentThread();
        try{
            grabInnerDM(holder,false);
            retVal = innerDM.redeploy(targetModuleID, archive, null);
            if (null != retVal) {
                retVal.addProgressListener(new ReleaseInnerDMPL(holder));
                // the server might have finished before we could register the 
                // listener....
                if (!retVal.getDeploymentStatus().isRunning()) {
                    releaseInnerDM(holder);
                }
            }
            return  retVal;
        } catch (IllegalStateException ise) {
            releaseInnerDM(holder);
            throw ise;
        } catch (Exception ioe) {
            IllegalStateException ise =
                    new IllegalStateException("file handling issues");
            ise.initCause(ioe);
            releaseInnerDM(holder);
            throw ise;
        } finally{
            if (null == retVal) {
                releaseInnerDM(holder);
            }
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
            // need to weed list of targetModules -- no app clients!
            // see https://glassfish.dev.java.net/issues/show_bug.cgi?id=641
            TargetModuleID[] weeded = weedOutAppClientTMID(targetModuleID);
            ProgressObject retVal = null;
            if (weeded.length < 1) {
                retVal = new ShortCircuitProgressObject(CommandType.START,
                        NbBundle.getMessage(SunDeploymentManager.class,"MESS_STARTED"),
                        StateType.COMPLETED,targetModuleID);
            } else {
                retVal =  new StartPOWorkAround(weeded,innerDM.start(weeded));
            }
            return retVal;
        } finally{
            Thread.currentThread().setContextClassLoader(origClassLoader);
        }
    }
    
    public ProgressObject stop(TargetModuleID[] targetModuleID)
    throws IllegalStateException {
        ThrowExceptionIfSuspended();
        ClassLoader origClassLoader=Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ServerLocationManager.getServerOnlyClassLoader(getPlatformRoot()));
        try{
            // need to weed list of targetModules -- no app clients!
            // see https://glassfish.dev.java.net/issues/show_bug.cgi?id=641
            TargetModuleID[] weeded = weedOutAppClientTMID(targetModuleID);
            ProgressObject retVal = null;
            if (weeded.length < 1) {
                retVal = new ShortCircuitProgressObject(CommandType.STOP,
                        NbBundle.getMessage(SunDeploymentManager.class,"MESS_STOPPED"),
                        StateType.COMPLETED,targetModuleID);
            } else {
                retVal =  innerDM.stop(weeded);
            }
            return retVal;
        } finally{
            Thread.currentThread().setContextClassLoader(origClassLoader);
        }
    }
    
    // TODO : remove this method when GFV2/SJSAS9.1 is the "legacy" version 
    // for the plugin.
    private TargetModuleID[] weedOutAppClientTMID(TargetModuleID[] tmids) {
        // the server team resolved 641 and also 3248 in v2... but since we
        // allow users to work with older server versions, we have to dance here.
        if (ServerLocationManager.getAppServerPlatformVersion(platformRoot) >
                ServerLocationManager.GF_V1) {
            return tmids;
        }
        ArrayList<TargetModuleID> retList = new ArrayList<TargetModuleID>();
        try{
            Class[] argClass = new Class[1];
            argClass[0] = TargetModuleID.class;
            Object[] argObject = new Object[1];
            
            ClassLoader loader =  getExtendedClassLoader();
            if(loader != null){
                Class cc = loader.loadClass("org.netbeans.modules.j2ee.sun.bridge.AppServerBridge");
                Method isCar = cc.getMethod("isCar", argClass);//NOI18N
                for (TargetModuleID tmid : tmids) {
                    argObject[0] = tmid;
                    boolean doNotAddToRetList = ((Boolean) isCar.invoke(null,argObject)).booleanValue();
                    if (!doNotAddToRetList) {
                        retList.add(tmid);
                    }
                }
            }
        }catch(Exception ex){
            //Suppressing exception while trying to obtain admin host port value
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ex);
        }
        return retList.toArray(new TargetModuleID[retList.size()]);
    }
    
    public ProgressObject undeploy(TargetModuleID[] targetModuleID)
    throws IllegalStateException {
        // System.out.println("undeploy.");
        ThrowExceptionIfSuspended();
        ClassLoader origClassLoader=Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ServerLocationManager.getServerOnlyClassLoader(getPlatformRoot()));
        
        ProgressObject retVal = null;
        Thread holder = Thread.currentThread();
        try{
            grabInnerDM(holder,false);
            retVal = innerDM.undeploy(targetModuleID);
            if (null != retVal) {
                retVal.addProgressListener(new ReleaseInnerDMPL(holder));
                // the server might have finished before we could register the 
                // listener....
                if (!retVal.getDeploymentStatus().isRunning()) {
                    releaseInnerDM(holder);
                }
            }
            return retVal;
        
        } catch (Exception ioe) {
            IllegalStateException ise =
                    new IllegalStateException("");
            ise.initCause(ioe);
            releaseInnerDM(holder);
            throw ise;
        } finally{
            if (null == retVal) {
                releaseInnerDM(holder);
            }
            Thread.currentThread().setContextClassLoader(origClassLoader);
            
        }
    }
    
    
    private void calculateIsLocal(){
        boolean isset = false;
        InstanceProperties ip = SunURIManager.getInstanceProperties(getPlatformRoot(),
                getHost(), getPort());
        if (ip!=null){ //Null is a possible returned value there...
            Object localDomainDir = ip.getProperty("LOCATION");
            if (null != localDomainDir) {
                isLocal = true;
                isset = true;
            }
            if ("".equals(localDomainDir)) {
                isLocal = false;
                isset = true;           // this may be redundant
            }
        }
        if (!isset) {
            if (getHost().equals("localhost")) {
                isLocal = true;
            } else {
                try {
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                String ia = InetAddress.getByName(getHost()).getHostAddress();
                                if(ia.equals("127.0.0.1")){//NOI18N
                                    isLocal = true;
                                } else {
                                    String localCanonName = InetAddress.getLocalHost().getCanonicalHostName();
                                    String currentCanonName = InetAddress.getByName(getHost()).getCanonicalHostName();
                                    
                                    isLocal =  (localCanonName.equals(currentCanonName) ) ? true : false;
                                }
                            } catch (Exception e) {
                                // e.printStackTrace();
                                jsr88Logger.severe(e.getMessage());
                            }
                        }
                    }.start();
                    
                } catch (Throwable t) {
                    // t.printStackTrace(); wll default to false.
                    return;
                }
            }
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
    public synchronized boolean isRunning() {
        return isRunning(false);
        
    }
     /* return the status of an instance.
      * when not forced, It is optimized to return the previous status if called more than twice within
      * a 4 seconds intervall
      * This boosts IDE reactivity
      */
    public boolean isRunning(boolean forced) {
        
        boolean retVal = false;
        
        if (isSuspended()) {
            retVal = true;
        } else {
            
//            if (secureStatusHasBeenChecked==false){
//
//            }
//         if (secureStatusHasBeenChecked&& (goodUserNamePassword==false)){
//            System.out.println("ISRUNNING TRUE BUT!!!!WRONG U PA: REFRESH THE NODe Please") ;
//            return true;
//            }
            
            long current=System.currentTimeMillis();
            //  System.out.println("in in running call"+ (current-timeStampCheckingRunning));
            if (forced==false && current-timeStampCheckingRunning<4000){
                //  System.out.println("Cached in in running call");
                //timeStampCheckingRunning = current;
                retVal = runningState;
                
            } else {
                boolean newrunningState = false;
                timeStampCheckingRunning = current;
                
                try {
                    ThrowExceptionIfSuspended();
                    
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
                runningState = newrunningState;
                if ((runningState)&&(nonAdminPortNumber == null)){
                    try{
                        //  System.out.println("inrunning get admin port number"+(System.currentTimeMillis()-current));
                        nonAdminPortNumber =  (String)getManagement().getAttribute(new javax.management.ObjectName("com.sun.appserv:type=http-listener,id=http-listener-1,config=server-config,category=config") ,"port");
                        //  sharedMemoryName = getDebugAddressValueReal();
                        // fix the null http port setting... in the ant properties file.
                        Runnable t = new Runnable() {
                            public void run() {
                                try {
                                    storeAntDeploymentProperties(getAntDeploymentPropertiesFile(),true);
                                } catch (IOException ioe) {
                                    // what can I do here
                                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ioe);
                                }
                                
                            }
                            
                        };
                        RequestProcessor.getDefault().post(t);
                    } catch (Throwable /*IllegalStateException*/ ee) {
                        ee.printStackTrace();
                    }
                }
                timeStampCheckingRunning = System.currentTimeMillis();
//        System.out.println("startinsruning"+(timeStampCheckingRunning-current));
                
                retVal = runningState;
            }
        }
        return retVal;
    }
    
    
    
    
    
    synchronized public void  ThrowExceptionIfSuspended(){
        
        /* this is called before any remote call, so it's a good place to do this extra check about being secure of not For EE version
         ** and accordingly set the environment correctly */
        
        if (secureStatusHasBeenChecked == false) {
            // long current=System.currentTimeMillis();
            mmm=null;
            try{
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
                
                //  now we know if we are secure or not, we can check is the username passowrd is good:
                testCredentials();
                
                
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
        if (secureStatusHasBeenChecked&& (goodUserNamePassword==false)){
            throw new RuntimeException(bundle.getString("MSG_WRONG_UserPassword")) ;
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
                    mmm.setDeploymentManager(this);
                }
            }catch(Exception ex){
                mmm = null;
                //Suppressing exception while trying to obtain admin host port value
                ex.printStackTrace();
            }
            
            
        }
        return mmm;
    }
    
    
    public void testCredentials() {
        
        Authenticator.setDefault(new AdminAuthenticator(this));
        
        goodUserNamePassword =false;
        maybeRunningButWrongUserName=false;
        try {
            Class[] argClass = new Class[1];
            argClass[0] = javax.enterprise.deploy.spi.DeploymentManager.class;
            Object[] argObject = new Object[1];
            argObject[0] = this;
            
            ClassLoader loader = getExtendedClassLoader();
            if(loader != null){
                Class cc = loader.loadClass("org.netbeans.modules.j2ee.sun.share.management.ServerMEJB");
                ServerInterface si;
                
                si = (ServerInterface) cc.newInstance();
                si.setDeploymentManager(this);
                si.checkCredentials();
                goodUserNamePassword =true;
            }
            
            
        } catch (ClassNotFoundException ex) {
            goodUserNamePassword =false;
        }  catch (InstantiationException ex) {
            goodUserNamePassword =false;
        } catch (IllegalAccessException ex) {
            goodUserNamePassword =false;
        } catch (IOException e){
            // If the server at the "other end" doesn't speak the admin protocol
            // we need to ignore it.  If the server on the other end speaks remotejmx
            // we will probably end up with some other error here.  I have no test
            // for that.
            //
            // most common problem: a v3 instance and a v2 instance are installed
            // on one machine and share the admin port... so only one can execute
            // at a time
            String mess = e.getMessage();
            if (null != mess) {
                if (mess.contains("remotejmx")) {
                    goodUserNamePassword = false;
                    return;
                }

                // if v3 is running on the admin port we see an invalid stream
                // header message in the exception.  The message includes a hex
                // value, so we check for that first. less likely to be localized...
                //
                if (mess.contains("3C21444F")) {
                    goodUserNamePassword = false;
                    return;
                }

                if (mess.contains("invalid stream header")) {
                    goodUserNamePassword = false;
                    return;
                }

                if(!mess.contains("500")){//not an internal error, so user/password error!!!
                    maybeRunningButWrongUserName =true ;
                }
            }
            
            String serverTitle = getInstanceDisplayName();
            DialogDescriptor desc = new DialogDescriptor(
                    NbBundle.getMessage(SunDeploymentManager.class,
                    "ERR_AUTH_DIALOG_MSG",new Object[] { ((serverTitle != null) ? serverTitle :           // NOI18N
                        NbBundle.getMessage(SunDeploymentManager.class, "WORD_SERVER")),    // NOI18N
                    stripSlashHtmlAndNewlines(e.getLocalizedMessage()) } ),
                    NbBundle.getMessage(SunDeploymentManager.class,"ERR_AUTH_DIALOG_TITLE"));   //NOI18N
            desc.setModal(false);
            desc.setMessageType(DialogDescriptor.ERROR_MESSAGE);
            desc.setOptions(new Object[] { DialogDescriptor.OK_OPTION });
            desc.setOptionsAlign(DialogDescriptor.BOTTOM_ALIGN);
            DialogDisplayer.getDefault().notify(desc);
        }
        
    }
    
    /** take the </html> off the end of a string.
     *  makes it easier to embed the strings in a string with 
     * markup safely
     */
    private String stripSlashHtmlAndNewlines(String input) {
        String retVal = null;
        int dex = input.toLowerCase(Locale.ENGLISH).lastIndexOf("</html>");
        if (dex > -1) {
            retVal = input.substring(0, dex);
        } else {
            retVal = input;
        }
        return retVal.replaceAll("\n", " ");
    }
    
    private String getInstanceDisplayName() {
        InstanceProperties ip = SunURIManager.getInstanceProperties(getPlatformRoot(), getHost(), getPort());
        return (ip != null) ? ip.getProperty(InstanceProperties.DISPLAY_NAME_ATTR) : null;
    }
    
    private ResourceConfiguratorInterface resourceConfigurator = null;
    
    public ResourceConfiguratorInterface getResourceConfigurator() {
        if(resourceConfigurator == null){
            org.netbeans.modules.j2ee.sun.api.restricted.ResourceConfigurator r = new org.netbeans.modules.j2ee.sun.api.restricted.ResourceConfigurator();
            r.setDeploymentManager(this);
            resourceConfigurator = r;
//            try{
//                Class[] argClass = new Class[1];
//                argClass[0] = javax.enterprise.deploy.spi.DeploymentManager.class;
//                Object[] argObject = new Object[1];
//                argObject[0] = this;
//
//                ClassLoader loader = getExtendedClassLoader();
//                if(loader != null){
//                    Class cc = loader.loadClass("org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ResourceConfigurator");
//                    resourceConfigurator = (ResourceConfiguratorInterface)cc.newInstance();
//                    java.lang.reflect.Method setDeploymentManager = cc.getMethod("setDeploymentManager", argClass);//NOI18N
//                    setDeploymentManager.invoke(resourceConfigurator, argObject);
//                }
//            }catch(Exception ex){
//                //Suppressing exception
//                //return will be a null value for resourceConfigurator
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
//                        ex);
//            }
        }
        return resourceConfigurator;
    }
    
    private javax.management.MBeanServerConnection getMBeanServerConnection() throws RemoteException, ServerException{
        ServerInterface serverMgmt = getManagement();
        return (javax.management.MBeanServerConnection)serverMgmt.getMBeanServerConnection();
    }
    
    public CmpMappingProvider getSunCmpMapper() {
        CmpMappingProvider sunCmpMapper = null;
        try {
            ClassLoader loader = getExtendedClassLoader();
            Class cc = loader.loadClass("org.netbeans.modules.j2ee.sun.bridge.cmp.CmpMappingProviderImpl");

            sunCmpMapper = (CmpMappingProvider) cc.newInstance();
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
        }
        return sunCmpMapper;
    }

    public boolean isMaybeRunningButWrongUserName() {
        return maybeRunningButWrongUserName;
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
        if (-1 != len1){
            //int uriLength = uri.length();
            try {
                retVal = Integer.parseInt(uri.substring(len1+1));
            } catch (NumberFormatException nfe) {
                jsr88Logger.warning(nfe.getMessage());
            }
        }
        return retVal;
    }
    
    
    
    synchronized public void  refreshDeploymentManager(){
        try{
            secureStatusHasBeenChecked=false;
            resetInnerDeploymentManager();
        }catch(Exception ex)   {
            return;//nothing much
        }
    }
    
    
    public File getPlatformRoot() {
        return  platformRoot;
    }
    
    private ClassLoader getExtendedClassLoader(){
        
        return ServerLocationManager.getNetBeansAndServerClassLoader(getPlatformRoot());
    }
    
    synchronized public void setUserName(String name) {
        mmm = null;
        secureStatusHasBeenChecked=false;
        String oldValue = userName;
        userName = name;
        propertySupport.firePropertyChange("name", oldValue, userName);
    }
    
    synchronized public void setPassword(String pw) {
        mmm= null;
        secureStatusHasBeenChecked=false;
        String oldValue = password;
        password = pw;
        passwordForURI.put(uri+platformRoot,password);
        
        refreshDeploymentManager();
        
        propertySupport.firePropertyChange("password", oldValue, password);
        
        
    }
    
    public File getAntDeploymentPropertiesFile() {
        return new File(System.getProperty("netbeans.user"), getInstanceID() + ".properties"); // NOI18N
    }
    
    public void storeAntDeploymentProperties(File file, boolean create) throws IOException {
        if (!create && !file.exists()) {
            return;
        }
        Properties antProps = new Properties();
        antProps.setProperty("sjsas.root", getPlatformRoot().getAbsolutePath()); // NOI18N
        antProps.setProperty("sjsas.url", getWebUrl());                // NOI18N
        String val = getUserName();
        if (null != val) {
            antProps.setProperty("sjsas.username", val);         // NOI18N
        }
        antProps.setProperty("sjsas.host",getHost());
        antProps.setProperty("sjsas.port",getPort()+"");
        // FIXME Get file object for parent folder and use FileObject.createData() here
        boolean ret = file.createNewFile();
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        FileLock lock = null;
        try {
            lock = fo.lock();
            OutputStream os = fo.getOutputStream(lock);
            try {
                antProps.store(os,"");
            } finally {
                if (null != os) {
                    os.close();
                }
            }
        } finally {
            if (null != lock) {
                lock.releaseLock();
            }
        }
    }
    
    private static String PROP_INSTANCE_ID = "PROP_INSTANCE_ID";
    
    // package protected for access from SunDeploymentFactory.
    String getInstanceID() {
        InstanceProperties ip = SunURIManager.getInstanceProperties(platformRoot,host,adminPortNumber);
        String name = null;
        if (null != ip) {
            name = ip.getProperty(PROP_INSTANCE_ID);
        }
        if (name == null) {
            boolean isGF = ServerLocationManager.isGlassFish(platformRoot);
            String prefix = isGF ? "glassfish" : "sjsas8"; // NOI18N
            String[] instanceURLs = Deployment.getDefault().getInstancesOfServer("J2EE"); // NOI18N
            int len = 0;
            if (null != instanceURLs) {
                len = instanceURLs.length;
            }
            for (int i = 0; name == null; i++) {
                if (i == 0) {
                    name = prefix;
                } else {
                    name = prefix + "_" + i; // NOI18N
                }
                
                for (int j = 0; j < len; j++) { // String url: instanceURLs) {
                    String url = instanceURLs[j];
                    String localUri = null;
                    if (null != ip) {
                        localUri = ip.getProperty(InstanceProperties.URL_ATTR);
                    }
                    if (!url.equals(localUri)) {
                        InstanceProperties iip = InstanceProperties.getInstanceProperties(url);
                        if (iip != null) {
                            String anotherName = iip.getProperty(PROP_INSTANCE_ID);
                            if (name.equals(anotherName)) {
                                name = null;
                                break;
                            }
                        }
                    }
                }
            }
            if (null != ip) {
                ip.setProperty(PROP_INSTANCE_ID, name);
            }
        }
        return name;
    }
    
    private String getWebUrl() {
        return "http://"+host+":"+nonAdminPortNumber;       // NOI18N
    }
    
    public HashMap getSunDatasourcesFromXml(){
        return getDomainEditor().getSunDatasourcesFromXml();
    }
    
    public HashMap getConnPoolsFromXml(){
        return getDomainEditor().getConnPoolsFromXml();
    }
    
    public HashMap getAdminObjectResourcesFromXml(){
        return getDomainEditor().getAdminObjectResourcesFromXml();
    }
    
    public void createSampleDataSourceinDomain(){
        getDomainEditor().createSampleDatasource();
    }

    private DomainEditor getDomainEditor(){
         DeploymentManagerProperties dmProps = new DeploymentManagerProperties(this);
         DomainEditor de = new DomainEditor(dmProps.getLocation(), dmProps.getDomainName(), true);
         return de;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }
    
    // VBK hack target objects to support configuration prototyping in
    // J2EE 1.4 RI beta 1 deploytool
    static class FakeTarget implements Target {
        
        public String getDescription() {
            return "fakeTargetDescr";      // NOI18N
        }
        public String getName() {
            return "fakeTargetName";       // NOI18N
        }
        
    }
    
    //private final AtomicBoolean locked = new AtomicBoolean(false);
    private final AtomicReference<Thread> owner = new AtomicReference<Thread>(null);

    public boolean grabInnerDM(Thread bar, boolean returnInsteadOfWait) {
        while (true) {
            //if (locked.compareAndSet(false,true)) {
            if (owner.compareAndSet(null, bar)) {
                // I just closed the lock
                break;
            } else {
                try {
                    if (returnInsteadOfWait) {
                        return false;
                    }
                    synchronized (owner) { //(innerDM) {
                        //innerDM.wait();
                        owner.wait(500);
                    }
                } catch (InterruptedException ie) {
                    // what do I do now?
                }
            }
        }
        return true;
    }
    
    public void releaseInnerDM(Thread foo) {
        if (owner.compareAndSet(foo, null)) {
            //locked.set(false);
            synchronized (owner) { // (innerDM) {
                //innerDM.notifyAll();
                owner.notifyAll();
            }
        }
    }
    
    private class ReleaseInnerDMPL implements ProgressListener {
        Thread locker;
        ReleaseInnerDMPL(Thread locker) {
            this.locker = locker;
        }
        
        public void handleProgressEvent(ProgressEvent progressEvent) {
            if (owner.compareAndSet(locker, null)) {
                DeploymentStatus dms = progressEvent.getDeploymentStatus();
                if (!dms.isRunning()) {
                    synchronized (owner) { //(innerDM) {
                        //innerDM.notifyAll();
                        owner.notifyAll();
                    }
                }
            }
        }
    }
    
    public void setRestartForDriverDeployment(boolean restart){
        this.driversdeployed = restart;
    }

    public int getAppserverVersion() {
        return ServerLocationManager.getAppServerPlatformVersion(getPlatformRoot());
    }
    
    /** put a file inside this progress listener to get rid of it after
     * it has been used
     */
// <editor-fold defaultstate="collapsed" desc=" FileDeleter code ">
    static class FileDeleter implements ProgressListener {
        File f;
        public FileDeleter(File f) {
            this.f = f;
        }
        
        public void handleProgressEvent(ProgressEvent pe) {
            DeploymentStatus ds = pe.getDeploymentStatus();
            if (ds.isCompleted() || ds.isFailed()) {
                // boolean complete = ds.isCompleted();
                f.delete();
            }
        }
    }
    //</editor-fold>

// <editor-fold defaultstate="collapsed" desc=" StartPOWorkAround code ">

    /** the PO returned by start has TMID objects that do not have the WebUrl property
     * initialized.  That is so helpful.
     */
    static class StartPOWorkAround implements ProgressObject {

        TargetModuleID[] modules;
        ProgressObject po;

        public StartPOWorkAround(TargetModuleID[] modules, ProgressObject po) {
            this.po = po;
            this.modules = modules;
        }

        public DeploymentStatus getDeploymentStatus() {
            DeploymentStatus retVal = po.getDeploymentStatus();
            // if the start has failed, we should make sure the result value
            // will be empty, too. just in case someone uses the value despite
            // having checked that the operation failed.
            if (retVal.isFailed()) {
                modules = new TargetModuleID[0];
            }
            return retVal;
        }

        public TargetModuleID[] getResultTargetModuleIDs() {
            return modules;
        }

        public ClientConfiguration getClientConfiguration(TargetModuleID arg0) {
            return po.getClientConfiguration(arg0);
        }

        public boolean isCancelSupported() {
            return po.isCancelSupported();
        }

        public void cancel() throws OperationUnsupportedException {
            po.cancel();
        }

        public boolean isStopSupported() {
            return po.isStopSupported();
        }

        public void stop() throws OperationUnsupportedException {
            po.stop();
        }

        public void addProgressListener(ProgressListener arg0) {
            po.addProgressListener(arg0);
        }

        public void removeProgressListener(ProgressListener arg0) {
            po.removeProgressListener(arg0);
        }
    }
    // </editor-fold>
}
