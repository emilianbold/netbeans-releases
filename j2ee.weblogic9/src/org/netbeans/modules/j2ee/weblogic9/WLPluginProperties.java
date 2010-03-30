/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Plugin Properties Singleton class
 * @author Ivan Sidorkin
 */
public class WLPluginProperties {

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
    public static boolean isSupportedVersion(Version version) {
        return version != null && ("9".equals(version.getMajorNumber()) || "10".equals(version.getMajorNumber())); // NOI18N
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
                    return new Version(implementationVersion);
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

    /**
     * Class representing the WebLogic version.
     * <p>
     * <i>Immutable</i>
     *
     * @author Petr Hejl
     */
    public static final class Version implements Comparable<Version> {

        private String majorNumber = "0";

        private String minorNumber = "0";

        private String microNumber = "0";

        private String update = "";

        /**
         * Constructs the version from the spec version string.
         * Expected format is <code>MAJOR_NUMBER[.MINOR_NUMBER[.MICRO_NUMBER[.UPDATE]]]</code>.
         *
         * @param version spec version string with the following format:
         *             <code>MAJOR_NUMBER[.MINOR_NUMBER[.MICRO_NUMBER[.UPDATE]]]</code>
         */
        public Version(String version) {
            assert version != null : "Version can't be null"; // NOI18N

            String[] tokens = version.split("\\.");

            if (tokens.length >= 4) {
                update = tokens[3];
            }
            if (tokens.length >= 3) {
                microNumber = tokens[2];
            }
            if (tokens.length >= 2) {
                minorNumber = tokens[1];
            }
            majorNumber = tokens[0];
        }

        /**
         * Returns the major number.
         *
         * @return the major number. Never returns <code>null</code>.
         */
        public String getMajorNumber() {
            return majorNumber;
        }

        /**
         * Returns the minor number.
         *
         * @return the minor number. Never returns <code>null</code>.
         */
        public String getMinorNumber() {
            return minorNumber;
        }

        /**
         * Returns the micro number.
         *
         * @return the micro number. Never returns <code>null</code>.
         */
        public String getMicroNumber() {
            return microNumber;
        }

        /**
         * Returns the update.
         *
         * @return the update. Never returns <code>null</code>.
         */
        public String getUpdate() {
            return update;
        }

        /**
         * {@inheritDoc}<p>
         * Two versions are equal if and only if they have same major, minor,
         * micro number and update.
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Version other = (Version) obj;
            if (this.majorNumber != other.majorNumber
                    && (this.majorNumber == null || !this.majorNumber.equals(other.majorNumber))) {
                return false;
            }
            if (this.minorNumber != other.minorNumber
                    && (this.minorNumber == null || !this.minorNumber.equals(other.minorNumber))) {
                return false;
            }
            if (this.microNumber != other.microNumber
                    && (this.microNumber == null || !this.microNumber.equals(other.microNumber))) {
                return false;
            }
            if (this.update != other.update
                    && (this.update == null || !this.update.equals(other.update))) {
                return false;
            }
            return true;
        }

        /**
         * {@inheritDoc}<p>
         * The implementation consistent with {@link #equals(Object)}.
         */
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 17 * hash + (this.majorNumber != null ? this.majorNumber.hashCode() : 0);
            hash = 17 * hash + (this.minorNumber != null ? this.minorNumber.hashCode() : 0);
            hash = 17 * hash + (this.microNumber != null ? this.microNumber.hashCode() : 0);
            hash = 17 * hash + (this.update != null ? this.update.hashCode() : 0);
            return hash;
        }

        /**
         * {@inheritDoc}<p>
         * Compares the versions based on its major, minor, micro and update.
         * Major number is the most significant. Implementation is consistent
         * with {@link #equals(Object)}.
         */
        public int compareTo(Version o) {
            int comparison = compareToIgnoreUpdate(o);
            if (comparison != 0) {
                return comparison;
            }
            return update.compareTo(o.update);
        }

        /**
         * Compares the versions based on its major, minor, micro. Update field
         * is ignored. Major number is the most significant.
         *
         * @param o version to compare with
         */
        public int compareToIgnoreUpdate(Version o) {
            int comparison = majorNumber.compareTo(o.majorNumber);
            if (comparison != 0) {
                return comparison;
            }
            comparison = minorNumber.compareTo(o.minorNumber);
            if (comparison != 0) {
                return comparison;
            }
            return microNumber.compareTo(o.microNumber);
        }

    }
}
