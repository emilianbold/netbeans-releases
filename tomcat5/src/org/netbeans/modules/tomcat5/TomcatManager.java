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
import java.util.Enumeration;

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

import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;

import org.w3c.dom.Document;
import org.xml.sax.*;

import java.io.*;

import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.openide.xml.XMLUtil;

import org.netbeans.modules.tomcat5.config.*;
import org.netbeans.modules.tomcat5.ide.StartTomcat;
import org.netbeans.modules.tomcat5.util.TomcatInstallUtil;
import org.netbeans.modules.tomcat5.nodes.DebuggingTypeEditor;

import org.openide.util.NbBundle;

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
    
    /** server.xml check timestamp */
    public static final String TIMESTAMP = "timestamp";

    /** admin port property */
    public static final String ADMIN_PORT = "admin_port";

    /** debugger port property */
    public static final String DEBUG_PORT = "debugger_port";
    
    /** http server port property */
    public static final String SERVER_PORT = "server_port";

    /** http server port property */
    public static final String CLASSIC = "classic";

    /** http server port property */
    public static final String DEBUG_TYPE = "debug_type";

    /** http server port property */
    public static final String DEBUG_SHARED = "debug_shared";

    /** default value for property classic */
    public static final Boolean DEFAULT_CLASSIC = Boolean.FALSE;

    /** default value for property debugger port */
    public static final Integer DEFAULT_DEBUG_PORT = new Integer(11555);
    
    /** default value for property server port */
    public static final Integer DEFAULT_SERVER_PORT = new Integer(8080);

    /** default value for property admin port */
    public static final Integer DEFAULT_ADMIN_PORT = new Integer(8005);

    /** default value for property debugging type*/
    public static final String DEFAULT_DEBUG_TYPE = NbBundle.getMessage (DebuggingTypeEditor.class, "SEL_debuggingType_socket");

    /** path to server xml */
    public static final String SERVERXML_PATH = File.separator + "conf" + File.separator + "server.xml";  // NOI18N
    
    /** path to default web.xml */
    public static final String WEBXML_PATH = File.separator + "conf" + File.separator + "web.xml";  // NOI18N

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
    
    private FileSystem catalinaFS;
    
    private StartTomcat sTomcat;
    
    /** storage for HTTP connector port */
//    private Integer serverPort;
    
    /** storage debug port */
//    private Integer debugPort;

    private Server root = null;
    
    /** Creates an instance of connected TomcatManager
     * @param conn <CODE>true</CODE> to create connected manager
     * @param uri URI for DeploymentManager
     * @param uname username
     * @param passwd password
     */
    public TomcatManager (boolean conn, String uri, String uname, String passwd) {
        if (TomcatFactory.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
            TomcatFactory.getEM ().log ("Creating connected TomcatManager uri="+uri+", uname="+uname); //NOI18N
        }
        this.connected = conn;
        sTomcat = null;
        int uriOffset = uri.indexOf ("http:");  // NOI18N
        if (uriOffset > 0) {
            // parse home and base attrs
            final String home = "home=";
            final String base = ":base=";
            int homeOffset = uri.indexOf (home) + home.length ();
            int baseOffset = uri.indexOf (base, homeOffset);
            if (homeOffset >= home.length ()) {
                if (baseOffset > 0) {
                    catalinaHome = uri.substring (homeOffset, baseOffset);
                    catalinaBase = uri.substring (baseOffset + base.length (),uriOffset-1);
                }
                else {
                    catalinaHome = uri.substring (homeOffset,uriOffset-1);
                }
            }
        }
        this.uri = uri.substring (uriOffset);
        username = uname;
        password = passwd;
    }

    public InstanceProperties getInstanceProperties() {
        return InstanceProperties.getInstanceProperties(TomcatFactory.tomcatUriPrefix + getUri());
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
    
    /** Returns URI of TomcatManager.
     * @return URI including home and base specification
     */
    public String getUri () {
        return ((catalinaHome != null)? "home="+catalinaHome + ":": "") +// NOI18N
            ((catalinaBase != null)? "base="+catalinaBase + ":": "") +   // NOI18N
            uri;
    }
    
    /** Returns URI of TomcatManager.
     * @return URI without home and base specification
     */
    public String getPlainUri () {
        return uri;
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
    
    public FileSystem getCatalinaBaseFileSystem() {
        if (catalinaFS!=null) return catalinaFS;
        catalinaFS = findFileSystem(getCatalinaBaseDir());
        if (catalinaFS==null) {
            catalinaFS = new LocalFileSystem();
            try {
                ((LocalFileSystem)catalinaFS).setRootDirectory(getCatalinaBaseDir());
                catalinaFS.setHidden(true);
                Repository.getDefault().addFileSystem(catalinaFS);
            } catch (Exception ex) {
                org.openide.ErrorManager.getDefault ().notify (ErrorManager.EXCEPTION, ex);
            }
        }
        return catalinaFS;
    }   
    
    private FileSystem findFileSystem(java.io.File file) {
        String fileName = file.getAbsolutePath();
        java.util.Enumeration e = Repository.getDefault().getFileSystems();
        while (e.hasMoreElements()) {
            FileSystem fs = (FileSystem)e.nextElement();
            File fsFile = FileUtil.toFile(fs.getRoot());
            if (fsFile==null) continue;
            String fsFileName = fsFile.getAbsolutePath();
            if (fileName.equals(fsFileName)) return fs;
        }
        return null;
    }
    
    public StartTomcat getStartTomcat(){
        return sTomcat;
    }
    
    public void setStartTomcat (StartTomcat st){
        sTomcat = st;
    }
    
    /** Returns username.
     * @return username or <CODE>null</CODE> when not connected.
     */
    public String getUsername () {
        return username;
    }
    
    /** Returns password.
     * @return password or <CODE>null</CODE> when not connected.
     */
    public String getPassword () {
        return password;
    }
    
    public DeploymentConfiguration createConfiguration (DeployableObject deplObj) 
    throws InvalidModuleException {
        if (TomcatFactory.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
            TomcatFactory.getEM ().log ("TomcatManager.createConfiguration "+deplObj);
        }
        if (!ModuleType.WAR.equals (deplObj.getType ())) {
            throw new InvalidModuleException ("Only WAR modules are supported for TomcatManager"); // NOI18N
        }
        
        return new WebappConfiguration (deplObj);
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
    
    
    public Target[] getTargets () throws IllegalStateException {
        if (!isConnected ()) {
            throw new IllegalStateException ("TomcatManager.getTargets called on disconnected instance");   // NOI18N
        }
        
        // PENDING 
        return new TomcatTarget [] { 
            new TomcatTarget (uri, "Tomcat at "+uri)
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
        return DEFAULT_DEBUG_TYPE;
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
                return Integer.valueOf(o);
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
    
    private void updatePortsFromFile() {
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
                    if (ip != null) {
                        ip.setProperty(ADMIN_PORT, DEFAULT_ADMIN_PORT.toString());
                        ip.setProperty(SERVER_PORT, DEFAULT_SERVER_PORT.toString());
                    }
                    return;
                }
            }
            
            inputStream = new FileInputStream(f);
            
            Long t;
            if (f.exists()) {
                t = new Long(f.lastModified());
                if (isPortUpToDate(t)) {
                    return;
                }
            } else {
                return;
            }
            
            Document doc = XMLUtil.parse(new InputSource(inputStream), false, false, null,org.openide.xml.EntityCatalog.getDefault());
            Server server = Server.createGraph(doc);
            Service service = server.getService(0);
            Integer adminPort = new Integer(TomcatInstallUtil.getAdminPort(server));
            Integer serverPort = new Integer(TomcatInstallUtil.getPort(service));
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
            Integer adminPort = Integer.valueOf(ip.getProperty(ADMIN_PORT));
            return adminPort;
        }
        return null;
    }

    private Integer readPortFromFile() {
        updatePortsFromFile();
        InstanceProperties ip = getInstanceProperties();
        if (ip != null) {
            Integer serverPort = Integer.valueOf(ip.getProperty(SERVER_PORT));
            return serverPort;
        }
        return null;
    }

    /*private Integer readPortFromFile() {
        try {
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
                    return DEFAULT_SERVER_PORT;
                }
            }
            
            inputStream = new FileInputStream(f);
            
            Long t;
            if (f.exists()) {
                t = new Long(f.lastModified());
            } else {
                return null;
            }
            
            Document doc = XMLUtil.parse(new InputSource(inputStream), false, false, null,org.openide.xml.EntityCatalog.getDefault());
            Server server = Server.createGraph(doc);
            Service service = server.getService(0);

            Integer serverPort = new Integer(TomcatInstallUtil.getPort(service));
            inputStream.close();

            InstanceProperties ip = getInstanceProperties();
            if (ip != null) {
                ip.setProperty(TIMESTAMP, t.toString());
                ip.setProperty(SERVER_PORT, serverPort.toString());
                return serverPort;
            }

        } catch (Exception e) {
            if (TomcatFactory.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
                TomcatFactory.getEM ().log (e.toString());
            }
        }
        return null;
    }*/
    
    private boolean isPortUpToDate(Long timestamp) {
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

    public Server getRoot() {
        
        if (this.root != null) {
            return root;
        }
        
        try {
            FileInputStream inputStream;
            if (catalinaBase != null) {
                try {
                    inputStream = new FileInputStream(new File(catalinaBase + "/conf/server.xml"));
                } catch (FileNotFoundException fnfe) {
                    return null;
                }
            } else {
                try {
                    inputStream = new FileInputStream(new File(catalinaHome + "/conf/server.xml"));
                } catch (FileNotFoundException fnfe) {
                    return null;
                }
            }

            Document doc = XMLUtil.parse(new InputSource(inputStream), false, false, null,org.openide.xml.EntityCatalog.getDefault());
            root = Server.createGraph(doc);
            return root;
        } catch (Exception e) {
            if (TomcatFactory.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
                TomcatFactory.getEM ().log (e.toString());
            }
            return null;
        }
    }
    
}
