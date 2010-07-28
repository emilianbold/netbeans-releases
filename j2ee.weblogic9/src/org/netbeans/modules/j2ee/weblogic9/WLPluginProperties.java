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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.j2ee.weblogic9;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Plugin Properties Singleton class
 * @author Ivan Sidorkin
 */
public class WLPluginProperties {
    
    public enum Vendor {
        ORACLE("Oracle"),
        SUN("Sun");
        
        Vendor(String name ){
            this.name = name;
        }
        
        public String toString() {
            return name;
        }
        
        private final String name; 
    }

    private static final Logger LOGGER = Logger.getLogger(WLPluginProperties.class.getName());

    private static final String CONFIG_XML = "config/config.xml"; //NOI18N

    private static final String DOMAIN_LIB_DIR = "lib"; //NOI18N

    // additional properties that are stored in the InstancePropeties object
    public static final String SERVER_ROOT_ATTR = "serverRoot";        // NOI18N
    public static final String DOMAIN_ROOT_ATTR = "domainRoot";        // NOI18N
    //public static final String IS_LOCAL_ATTR = "isLocal";              // NOI18N
    public static final String HOST_ATTR = "host";                     // NOI18N
    public static final String PORT_ATTR = "port";                     // NOI18N
    public static final String DEBUGGER_PORT_ATTR = "debuggerPort";    // NOI18N
    public static final String ADMIN_SERVER_NAME= "adminName";      // NOI18N
    public static final String DOMAIN_NAME = "domainName";          // NOI18N
    
    public static final String VENDOR   = "vendor";                 // NOI18N
    public static final String JAVA_OPTS="java_opts";               // NOI18N
    
    public static final String BEA_JAVA_HOME="bea_java_home";           // NOI18N
    public static final String SUN_JAVA_HOME="sun_java_home";           // NOI18N
    
    private static final Pattern WIN_BEA_JAVA_HOME_PATTERN = 
        Pattern.compile("\\s*set BEA_JAVA_HOME\\s*=(.*)");
    
    private static final Pattern WIN_SUN_JAVA_HOME_PATTERN = 
        Pattern.compile("\\s*set SUN_JAVA_HOME\\s*=(.*)");
    
    private static final Pattern SHELL_BEA_JAVA_HOME_PATTERN = 
        Pattern.compile("\\s*(export)?\\s*BEA_JAVA_HOME\\s*=(.*)");
    
    private static final Pattern SHELL_SUN_JAVA_HOME_PATTERN = 
        Pattern.compile("\\s*(export)?\\s*SUN_JAVA_HOME\\s*=(.*)");
    
    private static final Pattern LISTEN_ADDRESS_PATTERN = 
        Pattern.compile("(?:[a-z]+\\:)?listen-address");            // NOI18N

    private static final Pattern LISTEN_PORT_PATTERN = 
        Pattern.compile("(?:[a-z]+\\:)?listen-port");               // NOI18N

    private static final Pattern NAME_PATTERN = 
        Pattern.compile("(?:[a-z]+\\:)?name");                      // NOI18N
    
    private static final  Pattern SERVER_PATTERN = 
        Pattern.compile("(?:[a-z]+\\:)?server");                    // NOI18N
    
    // TODO read from domain-registry.xml instead?
    private static final String DOMAIN_LIST = "common/nodemanager/nodemanager.domains"; // NOI18N

    private static WLPluginProperties pluginProperties = null;
    private String installLocation;


    public static synchronized WLPluginProperties getInstance(){
        if (pluginProperties == null) {
            pluginProperties = new WLPluginProperties();
        }
        return pluginProperties;
    }

    @CheckForNull
    public static File getDomainLibDirectory(WLDeploymentManager manager) {
        String domain = (String) manager.getInstanceProperties().getProperty(WLPluginProperties.DOMAIN_ROOT_ATTR);
        if (domain != null) {
            File domainLib = new File(new File(domain), DOMAIN_LIB_DIR);
            if (domainLib.exists() && domainLib.isDirectory()) {
                return domainLib;
            }
        }
        return null;
    }

    @CheckForNull
    public static File getServerLibDirectory(WLDeploymentManager manager) {
        String server = (String) manager.getInstanceProperties().getProperty(WLPluginProperties.SERVER_ROOT_ATTR);
        if (server != null) {
            File serverLib = new File(new File(server), DOMAIN_LIB_DIR);
            if (serverLib.exists() && serverLib.isDirectory()) {
                return serverLib;
            }
        }
        return null;
    }
    
    /**
     * Gets the list of registered domains according to the given server
     * installation root
     *
     * @param serverRoot the server's installation location
     *
     * @return an array if strings with the domains' paths
     */
    public static String[] getRegisteredDomainPaths(String serverRoot){
        // init the resulting vector
        List<String> result = new ArrayList<String>();

        // is the server root was not defined, return an empty array of domains
        if (serverRoot == null) {
            return new String[0];
        }

        // init the input stream for the file and the w3c document object
        File file = new File(serverRoot + File.separator
                + DOMAIN_LIST.replaceAll("/", Matcher.quoteReplacement(File.separator)));
        LineNumberReader lnr = null;

        // read the list file line by line fetching out the domain paths
        try {
            // create a new reader for the FileInputStream
            lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(file)));

            // read the lines
            String line;
            while ((line = lnr.readLine()) != null) {
                // skip the comments
                if (line.startsWith("#")) {  // NOI18N
                    continue;
                }

                // fetch the domain path
                String path = line.split("=")[1].replaceAll("\\\\\\\\", "/").replaceAll("\\\\:", ":"); // NOI18N

                // add the path to the resulting set
                result.add(path);
            }
        } catch (FileNotFoundException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);   // NOI18N
        } catch (IOException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);   // NOI18N
        } finally {
            try {
                // close the stream
                if (lnr != null) {
                    lnr.close();
                }
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);  // NOI18N
            }
        }

        // convert the vector to an array and return
        return (String[]) result.toArray(new String[result.size()]);
    }
    
    /**
     * Returns map of server domain configuration properties red from config.xml file.
     * Only properties required for the moment are returned.  
     * Method implementation should be extended for additional properties. 
     * return server configuration properties 
     */
    public static Properties getDomainProperties( String domainPath ) {
        Properties properties = new Properties();
        String configPath = domainPath + "/config/config.xml"; // NOI18N

        // init the input stream for the file and the w3c document object
        InputStream inputStream = null;
        Document document = null;

        try {
            // open the stream from the instances config file
            File config = new File(configPath);
            if ( !config.exists()){
                Logger.getLogger("global").log(Level.INFO, "Domain config file " +
                		"is not found. Probavly server configuration was " +
                		"changed externally"); // NOI18N
                return properties;
            }
            inputStream = new FileInputStream(config);

            // parse the document
            document = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(inputStream);

            // get the root element
            Element root = document.getDocumentElement();

            // get the child nodes
            NodeList children = root.getChildNodes();

            // for each child
            for (int j = 0; j < children.getLength(); j++) {
                Node child = children.item(j);
                if ("name".equals(child.getNodeName())) {
                    String domainName = child.getFirstChild().getNodeValue();
                    properties.put(DOMAIN_NAME, domainName);
                }
                // if the child's name equals 'server' get its children
                // and iterate over them
                else if (SERVER_PATTERN.matcher(child.getNodeName()).matches())
                {
                    NodeList nl = child.getChildNodes();

                    // declare the server's name/host/port
                    String name = ""; // NOI18N
                    String port = ""; // NOI18N
                    String host = ""; // NOI18N

                    // iterate over the children
                    for (int k = 0; k < nl.getLength(); k++) {
                        Node ch = nl.item(k);

                        // if the child's name equals 'name' fetch the
                        // instance's name
                        if (NAME_PATTERN.matcher(ch.getNodeName()).matches()) {
                            name = ch.getFirstChild().getNodeValue();
                        }

                        // if the child's name equals 'listen-port' fetch the
                        // instance's port
                        if (LISTEN_PORT_PATTERN.matcher(ch.getNodeName())
                                .matches())
                        {
                            port = ch.getFirstChild().getNodeValue();
                        }

                        // if the child's name equals 'listen-address' fetch the
                        // instance's host
                        if (LISTEN_ADDRESS_PATTERN.matcher(ch.getNodeName())
                                .matches())
                        {
                            if (ch.hasChildNodes()) {
                                host = ch.getFirstChild().getNodeValue();
                            }
                        }
                    }

                    if (port != null) {
                        port = port.trim();
                    }

                    // if all the parameters were fetched successfully add
                    // them to the result
                    if ((name != null) && (!name.equals(""))) { // NOI18N
                        // address and port have minOccurs=0 and are missing in
                        // 90 examples server
                        port = (port == null || port.equals("")) // NOI18N
                        ? Integer.toString(WLDeploymentFactory.DEFAULT_PORT)
                                : port;
                        host = (host == null || host.equals("")) ? "localhost" // NOI18N
                                : host;
                        properties.put(PORT_ATTR,
                                port);
                        properties.put(HOST_ATTR, host);
                        properties.put(ADMIN_SERVER_NAME, name);
                    }
                }
            }
        }
        catch (FileNotFoundException e) {
            Logger.getLogger("global").log(Level.INFO, null, e); // NOI18N
        }
        catch (IOException e) {
            Logger.getLogger("global").log(Level.INFO, null, e); // NOI18N
        }
        catch (ParserConfigurationException e) {
            Logger.getLogger("global").log(Level.INFO, null, e); // NOI18N
        }
        catch (SAXException e) {
            Logger.getLogger("global").log(Level.INFO, null, e); // NOI18N
        }
        finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            catch (IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e); // NOI18N
            }
        }
        return properties;
    }
    
    /**
     * Returns map of JDK configuration which is used for starting server
     */
    public static Properties getRuntimeProperties(String domainPath){
        Properties properties = new Properties();
        String beaJavaHome = null;
        String sunJavaHome = null; 
        try {
            if (Utilities.isWindows()) {
                String setDomainEnv = domainPath + "/bin/setDomainEnv.cmd"; // NOI18N
                File file = new File(setDomainEnv);
                if (!file.exists()) {
                    Logger.getLogger("global")
                            .log(Level.INFO,
                                    "Domain environment "
                                            + "setup setDomainEnv.cmd is not found. Probavly server configuration was "
                                            + "changed externally"); // NOI18N
                    return properties;
                }
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    Matcher bea = WIN_BEA_JAVA_HOME_PATTERN.matcher(line);
                    Matcher sun = WIN_SUN_JAVA_HOME_PATTERN.matcher(line);

                    if (bea.matches()) {
                        beaJavaHome = line.substring(bea.start(1), bea.end(1))
                                .trim();
                    }
                    else if (sun.matches()) {
                        sunJavaHome = line.substring(sun.start(1), sun.end(1))
                                .trim();
                    }
                }
            }
            else {
                String setDomainEnv = domainPath + "/bin/setDomainEnv.sh"; // NOI18N
                File file = new File(setDomainEnv);
                if (!file.exists()) {
                    Logger.getLogger("global")
                            .log(Level.INFO,
                                    "Domain environment "
                                            + "setup setDomainEnv.cmd is not found. Probavly server configuration was "
                                            + "changed externally"); // NOI18N
                    return properties;
                }
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    Matcher bea = SHELL_BEA_JAVA_HOME_PATTERN.matcher(line);
                    Matcher sun = SHELL_SUN_JAVA_HOME_PATTERN.matcher(line);
                    if (bea.matches()) {
                        beaJavaHome = line.substring(bea.start(2), bea.end(2))
                                .trim();
                    }
                    else if (sun.matches()) {
                        sunJavaHome = line.substring(sun.start(2), sun.end(2))
                                .trim();
                    }
                }
            }
        }
        catch (FileNotFoundException e) {
            Logger.getLogger("global").log(Level.INFO, null, e); // NOI18N
        }
        catch (IOException e) {
            Logger.getLogger("global").log(Level.INFO, null, e); // NOI18N
        }
        if ( beaJavaHome != null ){
            properties.put( BEA_JAVA_HOME , beaJavaHome );
        }
        if ( sunJavaHome != null ){
            properties.put( SUN_JAVA_HOME, sunJavaHome);
        }
        return properties;
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
        FileObject dir = FileUtil.getConfigFile("J2EE");
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

    private static Collection fileColl = new java.util.ArrayList();

    static {
        fileColl.add("common");        // NOI18N
        fileColl.add("common/bin");    // NOI18N
        fileColl.add("server/lib/weblogic.jar"); // NOI18N
        fileColl.add(".product.properties"); // NOI18N
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
    public static boolean isSupportedVersion(Version version) {
        return version != null && (Integer.valueOf(9).equals(version.getMajor())
                    || Integer.valueOf(10).equals(version.getMajor())
                    || Integer.valueOf(11).equals(version.getMajor()));
    }

    public static Version getVersion(File serverRoot) {
        File weblogicJar = new File(serverRoot, "server/lib/weblogic.jar"); // NOI18N
        if (!weblogicJar.exists()) {
            return null;
        }
        try {
            // JarInputStream cannot be used due to problem in weblogic.jar in Oracle Weblogic Server 10.3
            JarFile jar = new JarFile(weblogicJar);
            try {
                Manifest manifest = jar.getManifest();
                String implementationVersion = null;
                if (manifest != null) {
                    implementationVersion = manifest.getMainAttributes()
                            .getValue("Implementation-Version"); // NOI18N
                }
                if (implementationVersion != null) { // NOI18N
                    implementationVersion = implementationVersion.trim();
                    return Version.fromJsr277NotationWithFallback(implementationVersion);
                }
            } finally {
                try {
                    jar.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.FINEST, null, ex);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.FINE, null, e);
        }
        return null;
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
