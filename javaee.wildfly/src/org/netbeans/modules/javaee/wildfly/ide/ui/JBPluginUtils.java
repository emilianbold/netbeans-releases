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
package org.netbeans.modules.javaee.wildfly.ide.ui;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.javaee.wildfly.ide.ui.JBPluginUtils.Version;
import org.openide.filesystems.JarFileSystem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Ivan Sidorkin
 */
public class JBPluginUtils {

    public static final String SERVER_4_XML = File.separator + "deploy" + File.separator + // NOI18N
            "jbossweb-tomcat55.sar" + File.separator + "server.xml"; // NOI18N

    public static final String SERVER_4_2_XML = File.separator + "deploy" + File.separator + // NOI18N
            "jboss-web.deployer" + File.separator + "server.xml"; // NOI18N

    public static final String SERVER_5_XML = File.separator + "deploy" + File.separator + // NOI18N
            "jbossweb.sar" + File.separator + "server.xml"; // NOI18N

    public static final Version JBOSS_5_0_0 = new Version("5.0.0"); // NOI18N

    public static final Version JBOSS_5_0_1 = new Version("5.0.1"); // NOI18N

    public static final Version JBOSS_6_0_0 = new Version("6.0.0"); // NOI18N

    public static final Version JBOSS_7_0_0 = new Version("7.0.0"); // NOI18N

    public static final Version WILDFLY_8_0_0 = new Version("8.0.0"); // NOI18N

    private static final Logger LOGGER = Logger.getLogger(JBPluginUtils.class.getName());

    public static final String LIB = "lib" + File.separator;

    public static final String MODULES_BASE_7 = "modules" + File.separator + "system"
            + File.separator + "layers" + File.separator + "base" + File.separator;

    public static final String CLIENT = "client" + File.separator;

    public static final String COMMON = "common" + File.separator;

    //--------------- checking for possible domain directory -------------
    private static List<String> domainRequirements7x;

    private static synchronized List<String> getDomainRequirements8x() {
        if (domainRequirements7x == null) {
            domainRequirements7x = new ArrayList<String>(11);
            Collections.addAll(domainRequirements7x,
                    "configuration"// NOI18N
            );
        }
        return domainRequirements7x;
    }

    //--------------- checking for possible server directory -------------
    private static List<String> serverRequirements7x;

    private static synchronized List<String> getServerRequirements8x() {
        if (serverRequirements7x == null) {
            serverRequirements7x = new ArrayList<String>(6);
            Collections.addAll(serverRequirements7x,
                    "bin", // NOI18N
                    "modules", // NOI18N
                    "jboss-modules.jar"); // NOI18N
        }
        return serverRequirements7x;
    }

    @NonNull
    public static String getModulesBase(String serverRoot) {
        return MODULES_BASE_7;
    }

    //------------  getting exists servers---------------------------
    /**
     * returns Hashmap key = server name value = server folder full path
     */
    public static Map getRegisteredDomains(String serverLocation) {
        Map result = new HashMap();
        File serverDirectory = new File(serverLocation);

        if (isGoodJBServerLocation(serverDirectory)) {
            Version version = getServerVersion(serverDirectory);
            String[] files = new String[]{"standalone", "domain"};
            File file = serverDirectory;

            if (files != null) {
                for (String file1 : files) {
                    String path = file.getAbsolutePath() + File.separator + file1;
                    if (isGoodJBInstanceLocation(serverDirectory, new File(path))) {
                        result.put(file1, path);
                    }
                }
            }
        }
        return result;
    }

    private static boolean isGoodJBInstanceLocation(File candidate, List<String> requirements) {
        return null != candidate && candidate.exists() && candidate.canRead() 
                && candidate.isDirectory()
                && hasRequiredChildren(candidate, requirements);
    }

    private static boolean isGoodJBInstanceLocation8x(File serverDir, File candidate) {
        return isGoodJBInstanceLocation(candidate, getDomainRequirements8x());
    }

    public static boolean isGoodJBInstanceLocation(File serverDir, File candidate) {
        Version version = getServerVersion(serverDir);
        if (version == null || !"8".equals(version.getMajorNumber())) { // NOI18N
            return JBPluginUtils.isGoodJBInstanceLocation8x(serverDir, candidate);
        }
        return ("8".equals(version.getMajorNumber()) && JBPluginUtils.isGoodJBInstanceLocation8x(serverDir, candidate)); // NOI18N
    }

    private static boolean isGoodJBServerLocation(File candidate, List<String> requirements) {
        if (null == candidate
                || !candidate.exists()
                || !candidate.canRead()
                || !candidate.isDirectory()
                || !hasRequiredChildren(candidate, requirements)) {
            return false;
        }
        return true;
    }

    private static boolean isGoodJBServerLocation8x(File candidate) {
        return isGoodJBServerLocation(candidate, getServerRequirements8x());
    }

    public static boolean isGoodJBServerLocation(File candidate) {
        Version version = getServerVersion(candidate);
        if (version == null || !"8".equals(version.getMajorNumber())) { // NOI18N
            return JBPluginUtils.isGoodJBServerLocation8x(candidate);
        }

        return ("8".equals(version.getMajorNumber()) && JBPluginUtils.isGoodJBServerLocation8x(candidate)); // NOI18N
    }

    /**
     * Checks whether the given candidate has all required childrens. Children
     * can be both files and directories. Method does not distinguish between
     * them.
     *
     * @return true if the candidate has all files/directories named in
     * requiredChildren, false otherwise
     */
    private static boolean hasRequiredChildren(File candidate, List<String> requiredChildren) {
        if (null == candidate || null == candidate.list()) {
            return false;
        }
        if (null == requiredChildren) {
            return true;
        }

        for (String next : requiredChildren) {
            File test = new File(candidate.getPath() + File.separator + next);
            if (!test.exists()) {
                return false;
            }
        }
        return true;
    }

    //--------------------------------------------------------------------
    /**
     *
     *
     */
    public static String getDeployDir(String domainDir) {
        return domainDir + File.separator + "deployments"; //NOI18N
    }

    public static String getHTTPConnectorPort(String domainDir) {
        String defaultPort = "8080"; // NOI18N

        /*
         * Following block is trying to solve different server versions.
         */
        File serverXmlFile = new File(domainDir + SERVER_4_XML);
        if (!serverXmlFile.exists()) {
            serverXmlFile = new File(domainDir + SERVER_4_2_XML);
            if (!serverXmlFile.exists()) {
                serverXmlFile = new File(domainDir + SERVER_5_XML);
                if (!serverXmlFile.exists()) {
                    return defaultPort;
                }
            }
        }

        InputStream inputStream = null;
        Document document = null;
        try {
            inputStream = new FileInputStream(serverXmlFile);
            try {
                document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
            } finally {
                inputStream.close();
            }

            // get the root element
            Element root = document.getDocumentElement();

            NodeList children = root.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeName().equals("Service")) {  // NOI18N
                    NodeList nl = child.getChildNodes();
                    for (int j = 0; j < nl.getLength(); j++) {
                        Node ch = nl.item(j);

                        if (ch.getNodeName().equals("Connector")) {  // NOI18N
                            String port = ch.getAttributes().getNamedItem("port").getNodeValue();
                            if (port.startsWith("$")) {
                                // FIXME check properties somehow
                                return defaultPort;
                            }
                            try {
                                Integer.parseInt(port);
                                return port;
                            } catch (NumberFormatException ex) {
                                return defaultPort;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.INFO, null, e);
            // it is ok
            // it optional functionality so we don't need to look at any exception
        }

        return defaultPort;
    }

    /**
     * Return true if the specified port is free, false otherwise.
     */
    public static boolean isPortFree(int port) {
        ServerSocket soc = null;
        try {
            soc = new ServerSocket(port);
        } catch (IOException ioe) {
            return false;
        } finally {
            if (soc != null) {
                try {
                    soc.close();
                } catch (IOException ex) {
                } // noop
            }
        }

        return true;
    }

    /**
     * Return the version of the server located at the given path. If the server
     * version can't be determined returns <code>null</code>.
     *
     * @param serverPath path to the server directory
     * @return specification version of the server
     */
    @CheckForNull
    public static Version getServerVersion(File serverPath) {
        assert serverPath != null : "Can't determine version with null server path"; // NOI18N

        Version version = null;
        File serverDir = new File(serverPath, getModulesBase(serverPath.getAbsolutePath()) + "org/jboss/as/server/main");
        File[] files = serverDir.listFiles(new JarFileFilter());
        if (files != null) {
            for (File jarFile : files) {
                version = getVersion(jarFile);
                if (version != null) {
                    break;
                }
            }
        }
        return version;
    }

    static class JarFileFilter implements FilenameFilter {

        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".jar");
        }
    }

    private static Version getVersion(File systemJarFile) {
        if (!systemJarFile.exists()) {
            return null;
        }

        try {
            JarFileSystem systemJar = new JarFileSystem();
            systemJar.setJarFile(systemJarFile);
            Attributes attributes = systemJar.getManifest().getMainAttributes();
            String version = attributes.getValue("Specification-Version"); // NOI18N
            if (version != null) {
                return new Version(version);
            }
            return null;
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return null;
        } catch (PropertyVetoException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return null;
        }
    }

    /**
     * Class representing the JBoss version.
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
         * Constructs the version from the spec version string. Expected format
         * is <code>MAJOR_NUMBER[.MINOR_NUMBER[.MICRO_NUMBER[.UPDATE]]]</code>.
         *
         * @param version spec version string with the following format:
         * <code>MAJOR_NUMBER[.MINOR_NUMBER[.MICRO_NUMBER[.UPDATE]]]</code>
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
         * {@inheritDoc}
         * <p>
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
         * {@inheritDoc}
         * <p>
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
         * {@inheritDoc}
         * <p>
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
