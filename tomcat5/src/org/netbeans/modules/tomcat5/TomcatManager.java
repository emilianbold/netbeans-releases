/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tomcat5;

import java.io.File;
import java.io.InputStream;
import java.util.Locale;
import org.openide.filesystems.*;

import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.DConfigBeanVersionType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.openide.ErrorManager;
import org.openide.modules.InstalledFileLocator;

import org.netbeans.modules.j2ee.deployment.plugins.api.*;

import org.w3c.dom.Document;
import org.xml.sax.*;

import java.io.*;

import org.openide.xml.XMLUtil;

import org.netbeans.modules.tomcat5.config.*;
import org.netbeans.modules.tomcat5.ide.StartTomcat;
import org.netbeans.modules.tomcat5.util.TomcatInstallUtil;

import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.jpda.*;

import org.netbeans.modules.tomcat5.util.*;


/** DeploymentManager that can deploy to 
 * Tomcat 5 using manager application.
 *
 * @author  Radim Kubacki
 */
public class TomcatManager implements DeploymentManager {

    /** Enum value for get*Modules methods. */
    static final int ENUM_AVAILABLE = 0;
    
    /** Enum value for get*Modules methods. */
    static final int ENUM_RUNNING = 1;
    
    /** Enum value for get*Modules methods. */
    static final int ENUM_NONRUNNING = 2;
    
    public static final int TOMCAT_50 = 0;
    public static final int TOMCAT_55 = 1;
    
    /** server.xml check timestamp */
    public static final String TIMESTAMP = "timestamp";

    /** admin port property */
    public static final String ADMIN_PORT = "admin_port";

    /** debugger port property */
    public static final String DEBUG_PORT = "debugger_port";
    
    /** http server port property */
    public static final String SERVER_PORT = InstanceProperties.HTTP_PORT_NUMBER;

    public static final String HOST = "host";
    
    /** http server port property */
    public static final String CLASSIC = "classic";

    /** http server port property */
    public static final String DEBUG_TYPE = "debug_type";

    /** shared memory property */
    public static final String SHARED_MEMORY = "shared_memory";
    
    /** is it bundled tomcat property */
    public static final String IS_IT_BUNDLED_TOMCAT = "is_it_bundled_tomcat";

    /** default value for property classic */
    public static final Boolean DEFAULT_CLASSIC = Boolean.FALSE;

    /** default value for property debugger port */
    public static final Integer DEFAULT_DEBUG_PORT = new Integer(11555);
    
    /** default value for property server port */
    public static final Integer DEFAULT_SERVER_PORT = new Integer(8080);

    /** default value for property admin port */
    public static final Integer DEFAULT_ADMIN_PORT = new Integer(8005);

    /** default value for property debugging type*/
    public static final String DEFAULT_DEBUG_TYPE_UNIX = "SEL_debuggingType_socket";  // NOI18N  

    /** default value for property debugging type*/
    public static final String DEFAULT_DEBUG_TYPE_WINDOWS = "SEL_debuggingType_shared";  // NOI18N 

    /** default value for property shared memory*/
    public static final String DEFAULT_SHARED_MEMORY = "tomcat_shared_memory_id";  // NOI18N

    /** path to server xml */
    public static final String SERVERXML_PATH = File.separator + "conf" + File.separator + "server.xml";  // NOI18N
    
    /** path to default web.xml */
    public static final String WEBXML_PATH = File.separator + "conf" + File.separator + "web.xml";  // NOI18N
    
    /** some bundled tomcat settings are stored here */
    public static final String BUNDLED_TOMCAT_SETTING = "J2EE/BundledTomcat/Setting"; // NOI18N
    
    /** Tomcat specific Instance property - if this property exists and is set 
       to <code>true</code> web module's context log will be opened on run. */
    private static final String OPEN_CONTEXT_LOG_ON_RUN = "openContextLogOnRun";    

    /** Tomcat specific Instance property - if this property exists and is set 
       to <code>true</code> Tomcat will be started wiht the "-security" option */
    private static final String SECURITY_STARTUP_OPTION = "securityStartupOption";

    /** Tomcat specific Instance property - if this property exists and is set 
       to <code>true</code> Tomcat will be stoped wiht the "-force" option */
    private static final String FORCE_STOP_OPTION = "forceStopOption";
    
    /** Tomcat specific Instance property - running state check timeout in millis */
    public static final String RUNNING_CHECK_TIMEOUT = "runningCheckTimeout";
    
    /* Default running state check timeout in millis */
    public static final int DEFAULT_RUNNING_CHECK_TIMEOUT = 1000;

    /** Manager state. */
    private boolean connected;
    
    /** uri of this DeploymentManager. */
    private String uri;
    
    /** Username used for connecting. */
    private String username;
    /** Password used for connecting. */
    private String password;
    
    /** CATALINA_HOME of disconnected TomcatManager. */
    private String catalinaHome;
    /** CATALINA_BASE of disconnected TomcatManager. */
    private String catalinaBase;
    
    private FileObject catalinaBaseDir;
    
    private StartTomcat sTomcat;
    
    private Server root = null;
    
    /** System process of the started Tomcat */
    private Process process;
    
    /** Easier access to some server.xml settings. */
    private TomcatManagerConfig tomcatManagerConfig;
    
    /** LogManager manages all context and shared context logs for this TomcatManager. */
    private LogManager logManager = new LogManager(this);
    
    private TomcatPlatformImpl tomcatPlatform;
    
    /** Is it bundled Tomcat? */
    private Boolean isItBundledTomcat = null;
    
    private int tomcatVersion;

    /** Creates an instance of connected TomcatManager
     * @param conn <CODE>true</CODE> to create connected manager
     * @param uri URI for DeploymentManager
     * @param uname username
     * @param passwd password
     */
    public TomcatManager (boolean conn, String uri, String uname, String passwd, int aTomcatVersion) {
        if (TomcatFactory.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
            TomcatFactory.getEM ().log ("Creating connected TomcatManager uri="+uri+", uname="+uname); //NOI18N
        }
        this.connected = conn;
        sTomcat = null;
        tomcatVersion = aTomcatVersion;
        // parse home and base attrs
        final String home = "home=";
        final String base = ":base=";
        final String uriString = "http://";  // NOI18N
        int uriOffset = uri.indexOf (uriString);
        int homeOffset = uri.indexOf (home) + home.length ();
        int baseOffset = uri.indexOf (base, homeOffset);
        if (homeOffset >= home.length ()) {
            int homeEnd = baseOffset > 0 ? baseOffset : (uriOffset > 0 ? uriOffset - 1 : uri.length ());
            int baseEnd = uriOffset > 0 ? uriOffset - 1 : uri.length ();
            catalinaHome= uri.substring (homeOffset, homeEnd);
            if (baseOffset > 0) {
                catalinaBase = uri.substring (baseOffset + base.length (), baseEnd);
            }
            // Bundled Tomcat home and base dirs can be specified as attributes
            // specified in BUNDLED_TOMCAT_SETTING file. Tomcat manager URL can 
            // then look like "tomcat:home=$bundled_home:base=$bundled_base" and
            // therefore remains valid even if Tomcat version changes. (issue# 40659)
            if (catalinaHome.length() > 0 && catalinaHome.charAt(0) == '$') {
                FileSystem fs = Repository.getDefault().getDefaultFileSystem();
                FileObject fo = fs.findResource(BUNDLED_TOMCAT_SETTING);
                if (fo != null) {
                    catalinaHome = fo.getAttribute(catalinaHome.substring(1)).toString();
                    if (catalinaBase != null && catalinaBase.length() > 0 
                        && catalinaBase.charAt(0) == '$') {
                        catalinaBase = fo.getAttribute(catalinaBase.substring(1)).toString();
                    }
                }
            }
        }
        
        //parse the old format for backward compatibility
        if (uriOffset > 0) {
            String theUri = uri.substring (uriOffset + uriString.length ());
            int portIndex = theUri.indexOf (':');
            String host = theUri.substring (0, portIndex - 1);
            setHost (host);
            //System.out.println("host:"+host);
            int portEnd = theUri.indexOf ('/');
            portEnd = portEnd > 0 ? portEnd : theUri.length ();
            String port = theUri.substring (portIndex, portEnd - 1);
            //System.out.println("port:"+port);
            try {
                setServerPort (Integer.valueOf (port));
            } catch (NumberFormatException nef) {
                org.openide.ErrorManager.getDefault ().log (nef.getLocalizedMessage ());
            }
        }
        this.uri = uri;
        username = uname;
        password = passwd;
        tomcatManagerConfig = new TomcatManagerConfig(getCatalinaDir() + SERVERXML_PATH);
    }

    public InstanceProperties getInstanceProperties() {
        return InstanceProperties.getInstanceProperties(getUri());
    }
    
    /** Creates an instance of disconnected TomcatManager * /
    public TomcatManager (String catHome, String catBase) {
        if (TomcatFactory.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
            TomcatFactory.getEM ().log ("Creating discconnected TomcatManager home="+catHome+", base="+catBase);
        }
        this.connected = false;
        this.catalinaHome = catHome;
        this.catalinaBase = catBase;
    }
     */

    /**
     * Returns true if the server is running.
     *
     * @param checkResponse should be checked whether is the server responding - is really up?
     * @return <code>true</code> if the server is running.
     */
    public boolean isRunning(boolean checkResponse) {
        return isRunning(getRunningCheckTimeout(), checkResponse);
    }
    
    /**
     * Returns true if the server is running.
     * 
     * @param timeout for how long should we keep trying to detect the running state.
     * @param checkResponse should be checked whether is the server responding - is really up?
     * @return <code>true</code> if the server is running.
     */
    public boolean isRunning(int timeout, boolean checkResponse) {
        Process proc = getTomcatProcess();
        if (proc != null) {
            try {
                // process is stopped
                proc.exitValue();
                return false;
            } catch (IllegalThreadStateException e) {
                // process is running
                if (!checkResponse) {
                    return true;
                }
            }
        }
        if (checkResponse) {
            return URLWait.waitForStartup (this, timeout); // is tomcat responding?
        } else {
            return false; // cannot resolve the state
        }
    }
    
    /** Returns identifier of TomcatManager. This is not a real URI!
     * @return URI including home and base specification
     */
    public String getUri () {
        if (isTomcat55()) {
            return TomcatFactory55.tomcatUriPrefix + uri;
        } else {
            return TomcatFactory.tomcatUriPrefix + uri;            
        }
    }
    
    /** Returns URI of TomcatManager (manager application).
     * @return URI without home and base specification
     */
    public String getPlainUri () {
        return "http://" + getHost () + ":" + getServerPort () + "/manager/"; //NOI18N
    }
    
    /** Returns URI of TomcatManager.
     * @return URI without home and base specification
     */
    public String getServerUri () {
        return "http://" + getHost () + ":" + getServerPort (); //NOI18N
    }
    
    /** Returns catalinaHome.
     * @return catalinaHome or <CODE>null</CODE> when not specified.
     */
    public String getCatalinaHome () {
        return catalinaHome;
    }
    
    /** Returns catalinaBase.
     * @return catalinaBase or <CODE>null</CODE> when not specified.
     */
    public String getCatalinaBase () {
        return catalinaBase;
    }
    
    /** Returns catalinaHome directory.
     * @return catalinaHome or <CODE>null</CODE> when not specified.
     */
    public File getCatalinaHomeDir () {
        if (catalinaHome == null) {
            return null;
        }
        File homeDir = new File (catalinaHome);
        if (!homeDir.isAbsolute ()) {
            InstalledFileLocator ifl = InstalledFileLocator.getDefault ();
            homeDir = ifl.locate (catalinaHome, null, false);
        }
        return homeDir;
    }
    
    /** Returns catalinaBase directory.
     * @return catalinaBase or <CODE>null</CODE> when not specified.
     */
    public File getCatalinaBaseDir () {
        if (catalinaBase == null) {
            return null;
        }
        File baseDir = new File (catalinaBase);
        if (!baseDir.isAbsolute ()) {
            InstalledFileLocator ifl = InstalledFileLocator.getDefault ();
            baseDir = ifl.locate (catalinaBase, null, false);
            if (baseDir == null) {
                baseDir = new File(System.getProperty("netbeans.user")+System.getProperty("file.separator")+catalinaBase);   // NOI18N
            }
        }
        return baseDir;
    }
    
    /**
     * Returns catalina directory.
     * @return catalinaBase directory, if it does not exist return catalinaHome directory,
     * <CODE>null</CODE> otherwise.
     */
    public File getCatalinaDir() {
        File catalinaDir = getCatalinaBaseDir();
        if (catalinaDir == null) catalinaDir = getCatalinaHomeDir();
        return catalinaDir;        
    }

    /**
     * Return path to catalina work directory, which is used to store generated 
     * sources and classes from JSPs.
     *
     * @return path to catalina work directory.
     */
    public String getCatalinaWork() {
        String engineName = tomcatManagerConfig.getEngineElement().getAttributeValue("name"); //NOI18N
        String hostName = tomcatManagerConfig.getHostElement().getAttributeValue("name"); //NOI18N
        StringBuffer catWork = new StringBuffer(getCatalinaDir().getAbsolutePath());
        catWork.append("/work/").append(engineName).append("/").append(hostName); //NOI18N
        return catWork.toString(); 
    }
    
    public FileObject getCatalinaBaseFileObject() {
        if (catalinaBaseDir!=null) return catalinaBaseDir;
        File baseDir = getCatalinaBaseDir();
        if (baseDir==null) baseDir = getCatalinaHomeDir();
        String[] files = baseDir.list();
        if (files == null || files.length == 0) {
            createBaseDir(baseDir,getCatalinaHomeDir());
        }
        if (baseDir==null) return null;
        catalinaBaseDir = FileUtil.toFileObject(baseDir);
        if (catalinaBaseDir==null) {
            // try to refresh parent FileObject
            File parentDir = baseDir.getParentFile();
            if (parentDir != null) {
                FileObject parentFileObject = FileUtil.toFileObject(parentDir);
                if (parentFileObject != null) {
                    parentFileObject.refresh();
                    catalinaBaseDir = FileUtil.toFileObject(baseDir);
                }
            }
        }
        return catalinaBaseDir;
    }
    
    public StartTomcat getStartTomcat(){
        return sTomcat;
    }
    
    public void setStartTomcat (StartTomcat st){
        sTomcat = st;
    }
    
    /**
     * Returns true if this server is started in debug mode AND debugger is attached to it.
     * Doesn't matter whether the thread are suspended or not.
     */
    public boolean isDebugged() {
        
        ServerDebugInfo sdi = null;

        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();

        try {
            sdi = getStartTomcat().getDebugInfo(null);
        } catch (Exception e) {
            // don't care - just a try
        }

        if (sdi == null) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "DebuggerInfo cannot be found for: " + this.toString());
        }

        for (int i=0; i < sessions.length; i++) {
            Session s = sessions[i];
            if (s != null) {
                Object o = s.lookupFirst(null, AttachingDICookie.class);
                if (o != null) {
                    AttachingDICookie attCookie = (AttachingDICookie)o;
                    if (sdi.getTransport().equals(ServerDebugInfo.TRANSPORT_SHMEM)) {
                        if (attCookie.getSharedMemoryName().equalsIgnoreCase(sdi.getShmemName())) {
                            return true;
                        }
                    } else {
                        if (attCookie.getHostName().equalsIgnoreCase(sdi.getHost())) {
                            if (attCookie.getPortNumber() == sdi.getPort()) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }
        
    /**
     * Returns true if this server is started in debug mode AND debugger is attached to it 
     * AND threads are suspended (e.g. debugger stopped on breakpoint)
     */
    public boolean isSuspended() {

        ServerDebugInfo sdi = null;
        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();

        try {
            sdi = getStartTomcat().getDebugInfo(null);
        } catch (Exception e) {
            // don't care - just a try
        }

        if (sdi == null) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "DebuggerInfo cannot be found for: " + this.toString());
        }

        for (int i=0; i < sessions.length; i++) {
            Session s = sessions[i];
            if (s != null) {
                Object o = s.lookupFirst(null, AttachingDICookie.class);
                if (o != null) {
                    AttachingDICookie attCookie = (AttachingDICookie)o;
                    if (sdi.getTransport().equals(ServerDebugInfo.TRANSPORT_SHMEM)) {
                        String shmem = attCookie.getSharedMemoryName();
                        if (shmem == null) continue;
                        if (shmem.equalsIgnoreCase(sdi.getShmemName())) {
                            Object d = s.lookupFirst(null, JPDADebugger.class);
                            if (d != null) {
                                JPDADebugger jpda = (JPDADebugger)d;
                                if (jpda.getState() == JPDADebugger.STATE_STOPPED) {
                                    return true;
                                }
                            }
                        }
                    } else {
                        String host = attCookie.getHostName();
                        if (host == null) continue;
                        if (host.equalsIgnoreCase(sdi.getHost())) {
                            if (attCookie.getPortNumber() == sdi.getPort()) {
                                Object d = s.lookupFirst(null, JPDADebugger.class);
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

    /** Returns username.
     * @return username or <CODE>null</CODE> when not connected.
     */
    public String getUsername () {
        InstanceProperties ip = getInstanceProperties();
        if (ip != null) {
            return ip.getProperty(InstanceProperties.USERNAME_ATTR);
        }
        return username;
    }
    
    public void setUsername (String username){
        InstanceProperties ip = getInstanceProperties();
        if (ip != null) {
            ip.setProperty(InstanceProperties.USERNAME_ATTR, username);
            this.username = username;
        }
    }
    
    /** Returns password.
     * @return password or <CODE>null</CODE> when not connected.
     */
    public String getPassword () {
        InstanceProperties ip = getInstanceProperties();
        if (ip != null) {
            return ip.getProperty(InstanceProperties.PASSWORD_ATTR);
        }
        return password;
    }
    
    public void setPassword (String password){
        InstanceProperties ip = getInstanceProperties();
        if (ip != null) {
            ip.setProperty(InstanceProperties.PASSWORD_ATTR, password);
            this.password = password;
        }
    }
    
    /** 
     * Return display name which represents this tomcat manager's server instance 
     * in IDE.
     *
     * @return display name which represents this tomcat manager's server instance 
     * in IDE, <code>null</code> if display name is not defined, which should not
     * occur.
     */
    public String getDisplayName() {
        InstanceProperties ip = getInstanceProperties();
        if (ip != null) {
            return ip.getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
        }
        return null;
    }
    
    public void setOpenContextLogOnRun(boolean val) {
        InstanceProperties ip = getInstanceProperties();
        if (ip != null) {
            ip.setProperty(OPEN_CONTEXT_LOG_ON_RUN, Boolean.valueOf(val).toString());
        }
    }
    
    public boolean getOpenContextLogOnRun() {
        InstanceProperties ip = getInstanceProperties();
        if (ip != null) {
            Object val = ip.getProperty(OPEN_CONTEXT_LOG_ON_RUN);
            if (val != null) return Boolean.valueOf(val.toString()).booleanValue();
        }
        return true;
    }

    /**
     * Set whether Tomcat should be started with the "-security" option.
     *
     * @param val <code>true</code> if Tomcat should be started with the "-security" 
     *         option, <code>false</code> otherwise.
     */
    public void setSecurityStartupOption(boolean val) {
        InstanceProperties ip = getInstanceProperties();
        if (ip != null) {
            ip.setProperty(SECURITY_STARTUP_OPTION, Boolean.valueOf(val).toString());
        }
    }

    /**
     * Should be Tomcat started with the "-security" option?
     *
     * @return <code>true</code> if Tomcat should be started with the "-security" 
     *         option, <code>false</code> otherwise.
     */
    public boolean getSecurityStartupOption() {
        InstanceProperties ip = getInstanceProperties();
        if (ip != null) {
            Object val = ip.getProperty(SECURITY_STARTUP_OPTION);
            if (val != null) return Boolean.valueOf(val.toString()).booleanValue();
        }
        return false;
    }

    /**
     * Set whether Tomcat should be stopped with the "-force" option.
     *
     * @param val <code>true</code> if Tomcat should be stopped with the "-force" 
     *         option, <code>false</code> otherwise.
     */
    public void setForceStopOption(boolean val) {
        InstanceProperties ip = getInstanceProperties();
        if (ip != null) {
            ip.setProperty(FORCE_STOP_OPTION, Boolean.valueOf(val).toString());
        }
    }
    
    /**
     * Should be Tomcat stopped with the "-force" option?
     *
     * @return <code>true</code> if Tomcat should be stopped with the "-force" 
     *         option, <code>false</code> otherwise.
     */
    public boolean getForceStopOption() {
        InstanceProperties ip = getInstanceProperties();
        if (ip != null) {
            Object val = ip.getProperty(FORCE_STOP_OPTION);
            if (val != null) return Boolean.valueOf(val.toString()).booleanValue();
        }
        return false;
    }
    
        
    /** 
     * Return running state check timeout. The default value is 1000ms.
     *
     * @return running state check timeout. The default value is 1000ms.
     */
    public int getRunningCheckTimeout() {
        InstanceProperties ip = getInstanceProperties();
        if (ip != null) {
            Object val = ip.getProperty(RUNNING_CHECK_TIMEOUT);
            if (val != null) {
                try {
                    return Integer.valueOf(val.toString()).intValue();
                } catch (NumberFormatException nfe) {
                    // ignore
                }
            }
        }
        return DEFAULT_RUNNING_CHECK_TIMEOUT;
    }
    
    /**
     * Is it bundled Tomcat?
     *
     * @return <code>true</code> if this TomcatManager represents bundled Tomcat, 
     *         <code>false</code> otherwise.
     */
    public boolean isItBundledTomcat() {
        if (isItBundledTomcat == null) {
            isItBundledTomcat = Boolean.FALSE;
            InstanceProperties ip = getInstanceProperties();
            if (ip != null) {
                Object val = ip.getProperty(IS_IT_BUNDLED_TOMCAT);
                if (val != null) {
                    isItBundledTomcat = Boolean.valueOf(val.toString());   
                }
            }
        }
        return isItBundledTomcat.booleanValue();
    }
    
    public boolean isTomcat55() {
        return tomcatVersion == TOMCAT_55;
    }
    
    public boolean isTomcat50() {
        return tomcatVersion == TOMCAT_50;
    }
    
    public int getTomcatVersion() {
        return tomcatVersion;
    }

// --- DeploymentManager interface implementation ----------------------
    
    public DeploymentConfiguration createConfiguration (DeployableObject deplObj) 
    throws InvalidModuleException {
        if (TomcatFactory.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
            TomcatFactory.getEM ().log ("TomcatManager.createConfiguration "+deplObj);
        }
        if (!ModuleType.WAR.equals (deplObj.getType ())) {
            throw new InvalidModuleException ("Only WAR modules are supported for TomcatManager"); // NOI18N
        }
        
        return new WebappConfiguration (deplObj, tomcatVersion);
    }
    
    public Locale getCurrentLocale () {
        return Locale.getDefault ();
    }
    
    public Locale getDefaultLocale () {
        return Locale.getDefault ();
    }
    
    public Locale[] getSupportedLocales () {
        return Locale.getAvailableLocales ();
    }
    
    public boolean isLocaleSupported (Locale locale) {
        if (locale == null) {
            return false;
        }
        
        Locale [] supLocales = getSupportedLocales ();
        for (int i =0; i<supLocales.length; i++) {
            if (locale.equals (supLocales[i])) {
                return true;
            }
        }
        return false;
    }
    
    public TargetModuleID[] getAvailableModules (ModuleType moduleType, Target[] targetList) 
    throws TargetException, IllegalStateException {
        return modules (ENUM_AVAILABLE, moduleType, targetList);
    }
    
    public TargetModuleID[] getNonRunningModules (ModuleType moduleType, Target[] targetList) 
    throws TargetException, IllegalStateException {
        return modules (ENUM_NONRUNNING, moduleType, targetList);
    }
    
    public TargetModuleID[] getRunningModules (ModuleType moduleType, Target[] targetList) 
    throws TargetException, IllegalStateException {
        return modules (ENUM_RUNNING, moduleType, targetList);
    }    
    
    public Target[] getTargets () throws IllegalStateException {
        if (!isConnected ()) {
            throw new IllegalStateException ("TomcatManager.getTargets called on disconnected instance");   // NOI18N
        }
        
        // PENDING 
        return new TomcatTarget [] { 
            new TomcatTarget (uri, "Tomcat at "+uri, getServerUri ())
        };
    }
    
    public DConfigBeanVersionType getDConfigBeanVersion () {
        // PENDING 
        return null;
    }
    
    public void setDConfigBeanVersion (DConfigBeanVersionType version) 
    throws DConfigBeanVersionUnsupportedException {
        if (!DConfigBeanVersionType.V1_3_1.equals (version)) {
            throw new DConfigBeanVersionUnsupportedException ("unsupported version");
        }
    }
    
    public boolean isDConfigBeanVersionSupported (DConfigBeanVersionType version) {
        return DConfigBeanVersionType.V1_3_1.equals (version);
    }
    
    public boolean isRedeploySupported () {
        // XXX what this really means
        return false;
    }
    
    public ProgressObject redeploy (TargetModuleID[] targetModuleID, InputStream inputStream, InputStream inputStream2) 
    throws UnsupportedOperationException, IllegalStateException {
        // PENDING
        throw new UnsupportedOperationException ("TomcatManager.redeploy not supported yet.");
    }
    
    public ProgressObject redeploy (TargetModuleID[] tmID, File file, File file2) 
    throws UnsupportedOperationException, IllegalStateException {
        // PENDING
        throw new UnsupportedOperationException ("TomcatManager.redeploy not supported yet.");
    }
    
    public void release () {
    }
    
    public void setLocale (Locale locale) throws UnsupportedOperationException {
    }
    
    public ProgressObject start (TargetModuleID[] tmID) throws IllegalStateException {
        if (!isConnected ()) {
            throw new IllegalStateException ("TomcatManager.start called on disconnected instance");   // NOI18N
        }
        if (tmID.length != 1 || !(tmID[0] instanceof TomcatModule)) {
            throw new IllegalStateException ("TomcatManager.start invalid TargetModuleID passed");   // NOI18N
        }
        
        TomcatManagerImpl impl = new TomcatManagerImpl (this);
        impl.start ((TomcatModule)tmID[0]);
        return impl;
    }
    
    public ProgressObject stop (TargetModuleID[] tmID) throws IllegalStateException {
        if (!isConnected ()) {
            throw new IllegalStateException ("TomcatManager.stop called on disconnected instance");   // NOI18N
        }
        if (tmID.length != 1 || !(tmID[0] instanceof TomcatModule)) {
            throw new IllegalStateException ("TomcatManager.stop invalid TargetModuleID passed");   // NOI18N
        }
        
        TomcatManagerImpl impl = new TomcatManagerImpl (this);
        impl.stop ((TomcatModule)tmID[0]);
        return impl;
    }
    
    public ProgressObject undeploy (TargetModuleID[] tmID) throws IllegalStateException {
        if (!isConnected ()) {
            throw new IllegalStateException ("TomcatManager.undeploy called on disconnected instance");   // NOI18N
        }

        ErrorManager.getDefault().log(ErrorManager.WARNING, "-----------------------------------------------------------------------------------");  // NOI18N
        ErrorManager.getDefault().log(ErrorManager.WARNING, "Please report this message at: http://www.netbeans.org/issues/show_bug.cgi?id=58255");  // NOI18N
        ErrorManager.getDefault().log(ErrorManager.WARNING, "TomcatManager.undeploy invalid TargetModuleID passed");  // NOI18N
        ErrorManager.getDefault().log(ErrorManager.WARNING, "Number of TargetModuleIDs passed: " + tmID.length); // NOI18N
        for (int i=0; i < tmID.length; i++) {
            String className = tmID[i].getClass().getName();
            String moduleID = tmID[i].getModuleID();
            String target = tmID[i].getTarget().getName();
            ErrorManager.getDefault().log(ErrorManager.WARNING, " class name: " + className); // NOI18N
            ErrorManager.getDefault().log(ErrorManager.WARNING, " moduleID:   " + moduleID);  // NOI18N
            ErrorManager.getDefault().log(ErrorManager.WARNING, " target:     " + target);    // NOI18N                
        }
        ErrorManager.getDefault().log(ErrorManager.WARNING, "-----------------------------------------------------------------------------------");  // NOI18N
            
        if (tmID.length != 1 || !(tmID[0] instanceof TomcatModule)) {
            throw new IllegalStateException ("TomcatManager.undeploy invalid TargetModuleID passed");   // NOI18N
        }
        
        TomcatManagerImpl impl = new TomcatManagerImpl (this);
        impl.remove ((TomcatModule)tmID[0]);
        return impl;
    }
    
    /** Deploys web module using deploy command
     * @param targets Array containg one web module
     * @param is Web application stream
     * @param deplPlan Server specific data
     * @throws IllegalStateException when TomcatManager is disconnected
     * @return Object that reports about deployment progress
     */    
    public ProgressObject distribute (Target[] targets, InputStream is, InputStream deplPlan) 
    throws IllegalStateException {
        if (!isConnected ()) {
            throw new IllegalStateException ("TomcatManager.distribute called on disconnected instance");   // NOI18N
        }
        
        if (TomcatFactory.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
            TomcatFactory.getEM ().log ("TomcatManager.distribute streams");
        }
        TomcatManagerImpl impl = new TomcatManagerImpl (this);
        impl.deploy (targets[0], is, deplPlan);
        return impl;
    }
    
    /** Deploys web module using install command
     * @param targets Array containg one web module
     * @param moduleArchive directory with web module or WAR file
     * @param deplPlan Server specific data
     * @throws IllegalStateException when TomcatManager is disconnected
     * @return Object that reports about deployment progress
     */    
    public ProgressObject distribute (Target[] targets, File moduleArchive, File deplPlan) 
    throws IllegalStateException {
        if (!isConnected ()) {
            throw new IllegalStateException ("TomcatManager.distribute called on disconnected instance");   // NOI18N
        }
        
        if (TomcatFactory.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
            TomcatFactory.getEM ().log ("TomcatManager.distribute archive="+moduleArchive.getPath ()+", plan="+deplPlan.getPath ()); // NOI18N
        }
        TomcatManagerImpl impl = new TomcatManagerImpl (this);
        impl.install (targets[0], moduleArchive, deplPlan);
        return impl;
    }
    
// --- End of DeploymentManager interface implementation ----------------------
        
    /** Utility method that retrieve the list of J2EE application modules 
     * distributed to the identified targets.
     * @param state     One of available, running, non-running constants.
     * @param moduleType    Predefined designator for a J2EE module type.
     * @param targetList    A list of deployment Target designators.
     */
    private TargetModuleID[] modules (int state, ModuleType moduleType, Target[] targetList)
    throws TargetException, IllegalStateException {
        if (!isConnected ()) {
            throw new IllegalStateException ("TomcatManager.modules called on disconnected instance");   // NOI18N
        }
        if (targetList.length != 1) {
            throw new TargetException ("TomcatManager.modules supports only one target");   // NOI18N
        }
        
        if (!ModuleType.WAR.equals (moduleType)) {
            return new TargetModuleID[0];
        }
        
        TomcatManagerImpl impl = new TomcatManagerImpl (this);
        return impl.list (targetList[0], state);
    }
    
    /** Connected / disconnected status.
     * @return <CODE>true</CODE> when connected.
     */
    public boolean isConnected () {
        return connected;
    }
    
    public String toString () {
        return "Tomcat manager ["+uri+", home "+catalinaHome+", base "+catalinaBase+(connected?"conneceted":"disconnected")+"]";    // NOI18N
    }

    /**
     * Getter for property debugPort.
     * @return Value of property debugPort.
     */
    public java.lang.Integer getDebugPort() {
        InstanceProperties ip = getInstanceProperties();
        if (ip != null) {
            String prop = ip.getProperty(DEBUG_PORT);
            if (prop != null) {
                return Integer.valueOf(prop);
            }
        }
        return DEFAULT_DEBUG_PORT;
    }
    
    /**
     * Getter for property debugPort.
     * @return Value of property debugPort.
     */
    public String getDebugType() {
        InstanceProperties ip = getInstanceProperties();
        if (ip != null) {
            String prop = ip.getProperty(DEBUG_TYPE);
            if (prop != null) {
                return prop;
            }
        }
        if (org.openide.util.Utilities.isWindows()) {
            return DEFAULT_DEBUG_TYPE_WINDOWS;
        }
        return DEFAULT_DEBUG_TYPE_UNIX;
    }

    public String getSharedMemory() {
        InstanceProperties ip = getInstanceProperties();
        if (ip != null) {
            String prop = ip.getProperty(SHARED_MEMORY);
            if (prop != null) {
                return prop;
            }
        }
        return DEFAULT_SHARED_MEMORY;
    }

    /**
     * Getter for property debugPort.
     * @return Value of property debugPort.
     */
    public Boolean getClassic() {
        InstanceProperties ip = getInstanceProperties();
        if (ip != null) {
            String prop = ip.getProperty(CLASSIC);
            if (prop != null) {
                return Boolean.valueOf(prop);
            }
        }
        return DEFAULT_CLASSIC;
    }

    public void setClassic(Boolean classic) {
        InstanceProperties ip = getInstanceProperties();
        if (ip != null) {
            ip.setProperty(CLASSIC, classic.toString());
        }
    }

    public void setDebugType(String str) {
        InstanceProperties ip = getInstanceProperties();
        if (ip != null) {
            ip.setProperty(DEBUG_TYPE, str);
        }
    }

    public void setSharedMemory(String str) {
        InstanceProperties ip = getInstanceProperties();
        if (ip != null) {
            ip.setProperty(SHARED_MEMORY, str);
        }
    }
    
    /**
     * Setter for property debugPort.
     * @param port New value of property debugPort.
     */
    public void setDebugPort(java.lang.Integer port) {
        InstanceProperties ip = getInstanceProperties();
        if (ip != null) {
            ip.setProperty(DEBUG_PORT, port.toString());
        }
    }    
    
    public Integer getServerPort() {
        
        boolean upToDate = false;
        InstanceProperties ip = getInstanceProperties();
        if (ip == null) {
            return null;   
        }
                
        String time;
        try {
            time = ip.getProperty(TIMESTAMP);
        } catch (IllegalStateException ise) {
            // TODO - Workaround - should be fixed on j2eeserver side
            return null;
        }
        if (time != null) {
            Long t = Long.valueOf(time);
            upToDate = isPortUpToDate(t);
        }
        if (upToDate) {
            String o;
            try {
                o = ip.getProperty(SERVER_PORT);
            } catch (IllegalStateException ise) {
                // TODO - Workaround - should be fixed on j2eeserver side
                return null;
            }
            if (o != null) {
                Integer i = null;
                try {
                    i = Integer.valueOf(o); 
                } catch (Exception e){
                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Cannot convert port number: " + o + " to Integer."); //NOI18N
                }
                return i;
            } 
        } else {
            if (TomcatFactory.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
                TomcatFactory.getEM ().log ("server port not uptodate, gonna read from file"); // NOI18N 
            }
            Integer p = readPortFromFile();
            return p;
        }
        return null;
    }
    
    public Integer getAdminPort() {
        boolean upToDate = false;
        InstanceProperties ip = getInstanceProperties();
        if (ip == null) {
            return null;
        }
        String time = ip.getProperty(TIMESTAMP);
        if (time != null) {
            Long t = Long.valueOf(time);
            upToDate = isPortUpToDate(t);
        }
        if (upToDate) {
            String o = ip.getProperty(ADMIN_PORT);
            if (o != null) {
                return Integer.valueOf(o);
            }
        } else {
            if (TomcatFactory.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
                TomcatFactory.getEM ().log ("admin port not uptodate, gonna read from file"); // NOI18N
            }
            Integer p = readAdminPortFromFile();
            return p;
        }
        return null;
    }
    
    private synchronized void updatePortsFromFile() {
        try {
            InstanceProperties ip = getInstanceProperties();
            FileInputStream inputStream;
            File f;
            if (catalinaBase != null) {
                f = new File(catalinaBase + SERVERXML_PATH);
            } else {
                f = new File(catalinaHome + SERVERXML_PATH);
            }
            if (!f.isAbsolute ()) {
                InstalledFileLocator ifl = InstalledFileLocator.getDefault ();
                f = ifl.locate (f.getPath(), null, false);
                if (f == null) {
                    return;
                }
            }
            
            inputStream = new FileInputStream(f);
            Long t = null;
            
            if (f.exists()) {
                t = new Long(f.lastModified());
            } else {
                return;
            }

            if (ip != null) {
                String stamp = ip.getProperty(TomcatManager.TIMESTAMP);
                if (stamp != null) {
                    if (isPortUpToDate(Long.valueOf(stamp))) {
                        return;
                    }
                }                
            }
                        
            Document doc = XMLUtil.parse(new InputSource(inputStream), false, false, null,org.openide.xml.EntityCatalog.getDefault());
            Server server = Server.createGraph(doc);
            Integer adminPort = new Integer(TomcatInstallUtil.getAdminPort(server));
            Integer serverPort = new Integer(TomcatInstallUtil.getPort(server));
            inputStream.close();
            
            if (ip != null) {
                ip.setProperty(TIMESTAMP, t.toString());
                ip.setProperty(ADMIN_PORT, adminPort.toString());
                ip.setProperty(SERVER_PORT, serverPort.toString());
            }
        } catch (Exception e) {
            if (TomcatFactory.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
                TomcatFactory.getEM ().log (e.getMessage());
            }
        }
    }
    
    private Integer readAdminPortFromFile() {
        updatePortsFromFile();
        InstanceProperties ip = getInstanceProperties();
        if (ip != null) {
            Integer adminPort = null;
            try {
                adminPort = Integer.valueOf(ip.getProperty(ADMIN_PORT));
            } catch (Exception e) {
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, e.toString());
            }
            return adminPort;
        }
        return null;
    }

    private Integer readPortFromFile() {
        updatePortsFromFile();
        InstanceProperties ip = getInstanceProperties();
        if (ip != null) {
            Integer serverPort = null;
            try {
                serverPort = Integer.valueOf(ip.getProperty(SERVER_PORT));
            } catch (Exception e) {
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, e.toString());
            }
            return serverPort;
        }
        return null;
    }
    
    private synchronized boolean isPortUpToDate(Long timestamp) {
        String serverXml;
        if (catalinaBase == null) {
            serverXml = catalinaHome + SERVERXML_PATH;
        } else {
            serverXml = catalinaBase + SERVERXML_PATH;
        }
        File serverXmlFile = new File(serverXml);
        if (serverXmlFile.exists()) {
            long l = serverXmlFile.lastModified();
            if (l <= timestamp.longValue()) {
                return true;
            }
        }
        return false;
    }
    
    public void setServerPort(Integer port) {
        InstanceProperties ip = getInstanceProperties();
        if (ip == null) {
            return;
        }
        ip.setProperty(SERVER_PORT, port.toString());
    }
    
    //PENDING: does not set in server.xml
    private void setHost (String host) {
        InstanceProperties ip = getInstanceProperties();
        if (ip == null) {
            return;
        }
        ip.setProperty(HOST, host);
    }
    
    public String getHost () {
        InstanceProperties ip = getInstanceProperties();
        if (ip == null) {
            return null;
        }
        return ip.getProperty(HOST);
    }
    
    public void setAdminPort(Integer port) {
        InstanceProperties ip = getInstanceProperties();
        if (ip == null) {
            return;
        }
        ip.setProperty(ADMIN_PORT, port.toString());
    }

    public Server getRoot() {
        // do we really need to cache this? shouldn't we at least return a
        // defensive copy, otherwise we may get easily out of sync with server.xml
        // if (this.root != null) {
        //    return root;
        // }        
        try {
            File f = new File(getCatalinaDir().getAbsolutePath() + SERVERXML_PATH);
            InputStream in = new BufferedInputStream(new FileInputStream(f));            
            Document doc = XMLUtil.parse(new InputSource(in), false, false, null,org.openide.xml.EntityCatalog.getDefault());
            root = Server.createGraph(doc);
            return root;
        } catch (Exception e) {
            if (TomcatFactory.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
                TomcatFactory.getEM ().log (e.toString());
            }
            return null;
        }
    }
    
    /** Initializes base dir for use with Tomcat 5.0.x. 
     *  @param baseDir directory for base dir.
     *  @param homeDir directory to copy config files from.
     *  @return File with absolute path for created dir or <CODE>null</CODE> when ther is an error.
     */    
    public File createBaseDir(File baseDir, File homeDir) {
        File targetFolder;
        if (!baseDir.isAbsolute ()) {
            baseDir = new File(System.getProperty("netbeans.user")+System.getProperty("file.separator")+baseDir);
            targetFolder = new File(System.getProperty("netbeans.user"));

        } else {
            targetFolder = baseDir.getParentFile ();
        }
        
        try {
            
            if (targetFolder == null) {
                TomcatFactory.getEM ().log (ErrorManager.INFORMATIONAL, "Cannot find parent folder for base dir "+baseDir.getPath ());
                return null;
            }
            File baseDirFO = new File (targetFolder, baseDir.getName ());
            baseDirFO.mkdir ();
                        
            // create directories
            String [] subdirs = new String [] { 
                "conf",   // NOI18N
                "conf/Catalina",   // NOI18N
                "conf/Catalina/localhost",   // NOI18N
                "logs",   // NOI18N
                "work",   // NOI18N
                "temp",   // NOI18N
                "webapps" // NOI18N
            };
            for (int i = 0; i<subdirs.length; i++) {
                File dest = new File (baseDirFO, subdirs [i]);
                dest.mkdirs ();
            }
            // copy config files
            final String ADMIN_XML = "conf/Catalina/localhost/admin.xml";
            String [] files = new String [] { 
                "conf/catalina.policy",   // NOI18N
                "conf/catalina.properties",   // NOI18N
                "conf/server.xml",   // NOI18N
                "conf/tomcat-users.xml",   // NOI18N
                "conf/web.xml",   // NOI18N
                ADMIN_XML,   // NOI18N For bundled tomcat 5.0.x 
                "conf/Catalina/localhost/manager.xml",   // NOI18N
                //"conf/Catalina/localhost/balancer.xml"   // NOI18N For bundled tomcat 5.0.x 
            };
            String [] patternFrom = new String [] { 
                null, 
                null, 
                null,
                "</tomcat-users>",   // NOI18N
                null,
                "docBase=\"../server/webapps/admin\"",    // NOI18N For bundled tomcat 5.0.x 
                "docBase=\"../server/webapps/manager\"",    // NOI18N
                //"docBase=\"balancer\""                    // NOI18N For bundled tomcat 5.0.x 
            };
            String passwd = null;
            if (isItBundledTomcat()) {
                passwd = TomcatInstallUtil.generatePassword(8);
                setPassword(passwd);
            }
            String [] patternTo = new String [] { 
                null, 
                null, 
                null,
                passwd != null ? "<user username=\"ide\" password=\"" + passwd + "\" roles=\"manager,admin\"/>\n</tomcat-users>" : null,   // NOI18N
                null, 
                "docBase=\"${catalina.home}/server/webapps/admin\"",   // NOI18N For bundled tomcat 5.0.x
                "docBase=\"${catalina.home}/server/webapps/manager\"",   // NOI18N 
                //"docBase=\""+new File (homeDir, "webapps/balancer").getAbsolutePath ()+"\""   // NOI18N For bundled tomcat 5.0.x
            };
            for (int i = 0; i<files.length; i++) {
                // get folder from, to, name and ext
                int slash = files[i].lastIndexOf ('/');
                String sfolder = files[i].substring (0, slash);
                File fromDir = new File (homeDir, sfolder); // NOI18N
                File toDir = new File (baseDir, sfolder); // NOI18N

                if (patternTo[i] == null) {
                    FileInputStream is = new FileInputStream (new File (fromDir, files[i].substring (slash+1)));
                    FileOutputStream os = new FileOutputStream (new File (toDir, files[i].substring (slash+1)));
                    try {
                        final byte[] BUFFER = new byte[4096];
                        int len;

                        for (;;) {
                            len = is.read (BUFFER);
                            if (len == -1) break;
                            os.write (BUFFER, 0, len);
                        }
                    } catch (java.io.IOException ioe) {
                        ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, ioe);
                    } finally {
                        try { if (os != null) os.close (); } catch (java.io.IOException ioe) { // ignore this
                        }
                        try { if (is != null) is.close (); } catch (java.io.IOException ioe) { // ignore this 
                        }
                    }
                }
                else {
                    // use patched version
                    if (!copyAndPatch (
                        new File (fromDir, files[i].substring (slash+1)), 
                        new File (toDir, files[i].substring (slash+1)), 
                        patternFrom[i],
                        patternTo[i]
                        )) {
                        if (!(ADMIN_XML.equals(files[i]) && !(new File (fromDir, files[i].substring (slash+1))).exists()) ){
                            ErrorManager.getDefault ().log (ErrorManager.INFORMATIONAL, "Cannot create config file "+files[i]);
                            return null;
                        }
                    }
                }
            }
            // deploy the ROOT context, if exists
            if (new File(homeDir, "webapps/ROOT").exists()) { // NOI18N
                writeToFile(new File(baseDir, "conf/Catalina/localhost/ROOT.xml"), // NOI18N
                    "<Context path=\"\" docBase=\"${catalina.home}/webapps/ROOT\"/>\n"); // NOI18N
            }
        } catch (java.io.IOException ioe) {
            ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, ioe);
            return null;
        }
        if (isItBundledTomcat()) {
            TomcatInstallUtil.patchBundledServerXml(new File(baseDir, "conf/server.xml")); // NOI18N
        }
        return baseDir;
    }
    
    /**
     * Create a file and fill it with the data.
     */
    private void writeToFile(File file, String data) throws IOException {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file));
            bw.write(data);
        } finally {
            if (bw != null) bw.close();
        }
    }
    
    /** Copies server.xml file and patches appBase="webapps" to
     * appBase="$CATALINA_HOME/webapps" during the copy.
     * @return success status.
     */
    private boolean copyAndPatch (File src, File dst, String from, String to) {
        java.io.Reader r = null;
        java.io.Writer out = null;
        if (!src.exists())
            return false;
        try {
            r = new BufferedReader (new InputStreamReader (new FileInputStream (src), "utf-8")); // NOI18N
            StringBuffer sb = new StringBuffer ();
            final char[] BUFFER = new char[4096];
            int len;

            for (;;) {
                len = r.read (BUFFER);
                if (len == -1) break;
                sb.append (BUFFER, 0, len);
            }
            int idx = sb.toString ().indexOf (from);
            if (idx >= 0) {
                sb.replace (idx, idx+from.length (), to);  // NOI18N
            }
            else {
                // Something unexpected
                TomcatFactory.getEM ().log(ErrorManager.INFORMATIONAL, "Pattern "+from+" not found in "+src.getPath ());
            }
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream (dst), "utf-8")); // NOI18N
            out.write (sb.toString ());
            
        } catch (java.io.IOException ioe) {
            ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, ioe);
            return false;
        } finally {
            try { if (out != null) out.close (); } catch (java.io.IOException ioe) { // ignore this
            }
            try { if (r != null) r.close (); } catch (java.io.IOException ioe) { // ignore this 
            }
        }
        return true;
    }
    
    /**
     * Open a context log for the specified module, if specified module does not
     * have its own logger defined, open shared context log instead.
     *
     * @param module module its context log should be opened
     */
    public void openLog(TargetModuleID module) {
        TomcatModule tomcatModule = null;
        if (module instanceof TomcatModule) {
            tomcatModule = (TomcatModule)module;
        } else {
            try {
                TargetModuleID[] tomMod = getRunningModules(ModuleType.WAR, new Target[]{module.getTarget()});
                for (int i = 0; i < tomMod.length; i++) {
                    if (module.getModuleID().equals(tomMod[i].getModuleID())) {
                        tomcatModule = (TomcatModule)tomMod[i];
                        break;
                    }
                }
            } catch (TargetException te) {
                ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, te);
            }
        }
        if (tomcatModule != null && logManager.hasContextLogger(tomcatModule)) {
            logManager.openContextLog(tomcatModule);
        } else {
            logManager.openSharedContextLog();
        }
    }
    
    /**
     * Return <code>TomcatManagerConfig</code> for easier access to some server.xml
     * settings.
     *
     * @return <code>TomcatManagerConfig</code> for easier access to some server.xml
     *         settings.
     */
    public TomcatManagerConfig tomcatManagerConfig() {
        return tomcatManagerConfig;
    }
    
    /**
     * Return <code>LogManager</code> which manages all context and shared context
     * logs for this <code>TomcatManager</code>.
     *
     * @return <code>LogManager</code> which manages all context and shared context
     *         logs for this <code>TomcatManager</code>.
     */
    public LogManager logManager() {
        return logManager;
    }
    
    /**
     * Set the <code>Process</code> of the started Tomcat.
     *
     * @param <code>Process</code> of the started Tomcat.
     */
    public synchronized void setTomcatProcess(Process p) {
        process = p;
    }

    /**
     * Return <code>Process</code> of the started Tomcat.
     *
     * @return <code>Process</code> of the started Tomcat, <code>null</code> if
     *         Tomcat wasn't started by IDE.
     */
    public synchronized Process getTomcatProcess() {
        return process;
    }
    
    public synchronized TomcatPlatformImpl getTomcatPlatform() {
        if (tomcatPlatform == null) {
            tomcatPlatform = new TomcatPlatformImpl(getCatalinaHomeDir(), getCatalinaBaseDir(), getDisplayName());
        }
        return tomcatPlatform;
    }
}
