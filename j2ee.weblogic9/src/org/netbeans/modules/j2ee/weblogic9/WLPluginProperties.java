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
package org.netbeans.modules.j2ee.weblogic9;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Plugin Properties Singleton class
 * @author Ivan Sidorkin
 */
public class WLPluginProperties {
    
    private static final boolean verboseRegistration =
            System.getProperty("netbeans.weblogic.registration") != null;
    
    private static final String CONFIG_XML = "config/config.xml";
    
    // additional properties that are stored in the InstancePropeties object
    public static final String SERVER_ROOT_ATTR = "serverRoot";        // NOI18N
    public static final String DOMAIN_ROOT_ATTR = "domainRoot";        // NOI18N
    public static final String IS_LOCAL_ATTR = "isLocal";              // NOI18N
    public static final String HOST_ATTR = "host";                     // NOI18N
    public static final String PORT_ATTR = "port";                     // NOI18N
    public static final String DEBUGGER_PORT_ATTR = "debuggerPort";    // NOI18N
    
    private static WLPluginProperties pluginProperties = null;
    private String installLocation;
    
    
    public static WLPluginProperties getInstance(){
        if(pluginProperties==null){
            pluginProperties = new WLPluginProperties();
        }
        return pluginProperties;
    }
    
    
    
    /** Creates a new instance of */
    private WLPluginProperties() {
        java.io.InputStream inStream = null;
        try {
            try {
                propertiesFile = getPropertiesFile();
                if (null != propertiesFile)
                    inStream = propertiesFile.getInputStream();
            } catch (java.io.FileNotFoundException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            } catch (java.io.IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            } finally {
                loadPluginProperties(inStream);
                if (null != inStream)
                    inStream.close();
            }
        } catch (java.io.IOException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        }
        
    }
    
    void loadPluginProperties(java.io.InputStream inStream) {
        Properties inProps = new Properties();
        if (null != inStream)
            try {
                inProps.load(inStream);
            } catch (java.io.IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
        String loc = inProps.getProperty(INSTALL_ROOT_KEY);
        if (loc!=null){// try to get the default value
            setInstallLocation(loc);
        }
    }
    
    private static final String INSTALL_ROOT_KEY = "installRoot"; // NOI18N
    
    
    private  FileObject propertiesFile = null;
    
    private FileObject getPropertiesFile() throws java.io.IOException {
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        FileObject dir = fs.findResource("J2EE");
        FileObject retVal = null;
        if (null != dir) {
            retVal = dir.getFileObject("weblogic","properties"); // NOI18N
            if (null == retVal) {
                retVal = dir.createData("weblogic","properties"); //NOI18N
            }
        }
        return retVal;
    }
    
    
    public void saveProperties(){
        Properties outProp = new Properties();
        String installRoot = getInstallLocation();
        if (installRoot != null)
            outProp.setProperty(INSTALL_ROOT_KEY, installRoot);
        
        FileLock l = null;
        java.io.OutputStream outStream = null;
        try {
            if (null != propertiesFile) {
                try {
                    l = propertiesFile.lock();
                    outStream = propertiesFile.getOutputStream(l);
                    if (null != outStream)
                        outProp.store(outStream, "");
                } catch (java.io.IOException e) {
                    Logger.getLogger("global").log(Level.INFO, null, e);
                } finally {
                    if (null != outStream)
                        outStream.close();
                    if (null != l)
                        l.releaseLock();
                }
            }
        } catch (java.io.IOException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        }
    }
    
    //temporary fix of #65456 
    //TODO domains file should be detected automatically upon nodemanager.properties content;
    //same problem also in ServerPropertiesPanel.getRegisteredDomains()
    public static final String DOMAIN_LIST = "common/nodemanager/nodemanager.domains"; // NOI18N
    
    public static boolean domainListExists(File candidate) {
        if (null == candidate ||
                !candidate.exists() ||
                !candidate.canRead() ||
                !candidate.isDirectory()  ||
                !new File(candidate.getPath() + File.separator + DOMAIN_LIST).exists()) {
            return false;
        }
        return true;
    }
    
    private static Collection fileColl = new java.util.ArrayList();
    
    static {
        fileColl.add("common");        // NOI18N
        fileColl.add("javelin");       // NOI18N
        fileColl.add("uninstall");     // NOI18N
        fileColl.add("common/bin");    // NOI18N
        fileColl.add("server/lib/weblogic.jar"); // NOI18N
    }
    
    public static boolean isGoodServerLocation(File candidate){
        if (null == candidate ||
                !candidate.exists() ||
                !candidate.canRead() ||
                !candidate.isDirectory()  ||
                !hasRequiredChildren(candidate, fileColl)) {
            return false;
        }
        return true;
    }

    /**
     * Checks whether the server root contains weblogic.jar of version 9 or 10.
     */
    public static boolean isSupportedVersion(File serverRoot) {
        File weblogicJar = new File(serverRoot, "server/lib/weblogic.jar"); // NOI18N
        if (!weblogicJar.exists()) {
            return false;
        }
        try {
            JarInputStream jarInputStream = new JarInputStream(new BufferedInputStream(new FileInputStream(weblogicJar)));
            Manifest manifest = jarInputStream.getManifest();
            String implementationVersion = manifest.getMainAttributes().getValue("Implementation-Version"); // NOI18N
            if (implementationVersion != null) { // NOI18N
                implementationVersion = implementationVersion.trim();
                return implementationVersion.startsWith("9.") || implementationVersion.startsWith("10."); // NOI18N
            }
        } catch (IOException e) {
            Logger.getLogger(WLPluginProperties.class.getName()).log(Level.FINE, null, e);
        }
        return false;
    }
    
    public static String getWeblogicDomainVersion(String domainRoot) {
        // Domain config file
        File config = new File(domainRoot, CONFIG_XML);
        
        // Check if the file exists
        if (!config.exists())
            return null;
        
        try {
            InputSource source = new InputSource(new FileInputStream(config));
            Document d = XMLUtil.parse(source, false, false, null, null);
            
            // Retrieve domain version
            if (d.getElementsByTagName("domain-version").getLength() > 0) {
                return d.getElementsByTagName("domain-version").item(0).getTextContent();
            }
            
        } catch(FileNotFoundException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        } catch(IOException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        } catch(SAXException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        }
        
        return null;
    }
    
    private static boolean hasRequiredChildren(File candidate, Collection requiredChildren) {
        if (null == candidate)
            return false;
        String[] children = candidate.list();
        if (null == children)
            return false;
        if (null == requiredChildren)
            return true;
        Iterator iter = requiredChildren.iterator();
        while (iter.hasNext()){
            String next = (String)iter.next();
            File test = new File(candidate.getPath()+File.separator+next);
            if (!test.exists())
                return false;
        }
        return true;
    }
    
    public void setInstallLocation(String installLocation){
        if ( installLocation.endsWith("/") || installLocation.endsWith("\\") ){
            installLocation = installLocation.substring(0, installLocation.length() - 1 );
        }
        
        this.installLocation = installLocation;
//        WLDeploymentFactory.resetWLClassLoader(installLocation);
    }
    
    public String getInstallLocation(){
        return this.installLocation;
    }
}
