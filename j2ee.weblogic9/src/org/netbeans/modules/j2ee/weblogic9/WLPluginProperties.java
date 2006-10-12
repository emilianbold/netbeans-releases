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
package org.netbeans.modules.j2ee.weblogic9;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.ErrorManager;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Plugin Properties Singleton class
 * @author Ivan Sidorkin
 */
public class WLPluginProperties {
    
    private static final boolean verboseRegistration =
            System.getProperty("netbeans.weblogic.registration") != null;
    
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
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            } catch (java.io.IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            } finally {
                loadPluginProperties(inStream);
                if (null != inStream)
                    inStream.close();
            }
        } catch (java.io.IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        
    }
    
    void loadPluginProperties(java.io.InputStream inStream) {
        Properties inProps = new Properties();
        if (null != inStream)
            try {
                inProps.load(inStream);
            } catch (java.io.IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
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
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                } finally {
                    if (null != outStream)
                        outStream.close();
                    if (null != l)
                        l.releaseLock();
                }
            }
        } catch (java.io.IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
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
     * Checks whether license.bea file contains at least one occurence of version 9.0 or 9.1.
     *
     * The method is rather heurestic than exact way how to detect the version 
     * because we are not able to decide which version belongs to the directory specified by the user
     * in case of more than one license-group tag occurences (several WL servers in one BEA home).
     */
    public static boolean isSupportedVersion(File serverRoot) {
        List<File> registryFiles = findRegistryFiles(serverRoot);
        for (File registryFile : registryFiles) {
            if (testRegistryFile(serverRoot, registryFile)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * @return registry.xml from all BEA homes
     */
    private static List<File> findRegistryFiles(File serverRoot) {
        List<File> registryList = new LinkedList<File>();
        
        List<String> beaHomesList = findBeaHomes();
        for (String beaHome : beaHomesList) {
            File registryFile = new File(beaHome + File.separator + "registry.xml"); // NOI18N
            registryList.add(registryFile);
        }
        
        return registryList;
    }
    
    /**
     * @return true if the given registry contains server version 9.x on the given server root path
     */
    private static boolean testRegistryFile(File serverRoot, File registryFile) {
        try {
            InputSource input = new InputSource(new BufferedInputStream(new FileInputStream(registryFile)));
            Document doc = XMLUtil.parse(input, false, false, null, null);
            NodeList releaseNodes = doc.getElementsByTagName("release"); // NOI18N
            for (int i = 0; i < releaseNodes.getLength(); i++) {
                Node releaseNode = releaseNodes.item(i);
                NamedNodeMap releaseNodeAttributes = releaseNode.getAttributes();
                String level = releaseNodeAttributes.getNamedItem("level").getNodeValue(); // NOI18N
                String installDir = releaseNodeAttributes.getNamedItem("InstallDir").getNodeValue(); // NOI18N
                String installDirCanonical = new File(installDir).getCanonicalPath();
                if (level != null && level.startsWith("9.") && installDirCanonical.equals(serverRoot.getCanonicalPath())) {
                    return true;
                }
            }
        } catch (Exception ex) {
            if (verboseRegistration) {
                String msg = NbBundle.getMessage(WLPluginProperties.class, "ERR_READING_REGISTRY_FILE", registryFile.getPath());
                ErrorManager.getDefault().annotate(ex, msg);
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        
        return false;
        
    }
    
    private static List<String> findBeaHomes() {
        List<String> beaHomesList = new LinkedList<String>();
        String dir = "";
        if (Utilities.isUnix()) {
            dir = System.getProperty("user.home", ""); // NOI18N
        }
        else 
        if (Utilities.isWindows()) {
            String systemDrive = System.getenv("SystemDrive"); // NOI18N
            if (systemDrive == null) {
                systemDrive = "C:"; // NOI18N
            }
            dir = systemDrive;
        }
        File beaHomeList = new File(dir + File.separator + "bea" + File.separator + "beahomelist"); // NOI18N
        try {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(beaHomeList));
                String list = br.readLine();
                if (list != null) {
                    StringTokenizer st = new StringTokenizer(list, ";"); // NOI18N
                    while (st.hasMoreTokens()) {
                        beaHomesList.add(st.nextToken());
                    }
                }
            }
            finally {
                if (br != null) {
                    br.close();
                }
            }
        } catch (Exception ex) {
            String msg = NbBundle.getMessage(WLPluginProperties.class, "ERR_READING_BEAHOMELIST", beaHomeList.getPath());
            ErrorManager.getDefault().annotate(ex, msg);
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        
        return beaHomesList;
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
    
    public boolean isCurrentServerLocationValid(){
        if (getInstallLocation()!=null)
            return (isGoodServerLocation(new File(getInstallLocation())));
        else
            return false;
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
    
    private static final String J2SE_PLATFORM_VERSION_15 = "1.5"; // NOI18N
    private static final String J2SE_PLATFORM_VERSION_16 = "1.6"; // NOI18N
    
    public static boolean runningOnCorrectJdk() {
        SpecificationVersion defPlatVersion = JavaPlatformManager.getDefault().getDefaultPlatform().getSpecification().getVersion();
        // test just JDK 1.5 and 1.6 for now, because WL 9.x requires it. Future releases may come with another requirements.
        if (J2SE_PLATFORM_VERSION_15.equals(defPlatVersion.toString()) ||
            J2SE_PLATFORM_VERSION_16.equals(defPlatVersion.toString()))
            return true;
        return false;
    }
}
