/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.modules.coherence.server.util.Version;
import org.openide.filesystems.FileUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Holds basic Coherence and Coherence plugin properties.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class CoherenceProperties {

    /** Directory inside Coherence platform where are libraries placed. */
    public static final String PLATFORM_LIB_DIR = "lib"; //NOI18N

    /** Directory inside Coherence platform where are binaries placed. */
    public static final String PLATFORM_BIN_DIR = "bin"; //NOI18N

    /** Directory inside Coherence platform where is documentation placed. */
    public static final String PLATFORM_DOC_DIR = "doc"; //NOI18N

    /** File name of base Coherence JAR file. */
    public static final String COHERENCE_JAR_NAME = "coherence.jar"; //NOI18N

    /** Contains all Coherence server command line properties. */
    public static final List<CoherenceServerProperty> SERVER_PROPERTIES = new ArrayList<CoherenceServerProperty>();

    private static final Logger LOGGER = Logger.getLogger(CoherenceProperties.class.getName());

    static {
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.cacheconfig", "Cache configuration descriptor filename", String.class)); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.cluster", "Cluster name", String.class)); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.clusteraddress", "Cluster (multicast) IP address", String.class)); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.clusterport", "Cluster (multicast) IP port", Long.class)); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.distributed.backup", "Data backup storage location", String.class)); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.distributed.backupcount", "Number of data backups", Integer.class)); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.distributed.localstorage", "Local partition management enabled", Boolean.class)); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.distributed.threads", "Thread pool size", Integer.class)); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.distributed.transfer", "Partition transfer threshold", Long.class)); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.edition", "Product edition", String.class)); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.invocation.threads", "Invocation service thread pool size", Integer.class)); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.localhost", "Unicast IP address", String.class)); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.localport", "Unicast IP port", Long.class)); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.localport.adjust", "Unicast IP port auto assignment", Boolean.class, "true")); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.log", "Logging destination", String.class)); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.log.level", "Logging level", String.class)); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.log.limit", "Log output character limit", Long.class)); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.machine", "Machine name", String.class)); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.management", "JMX management mode", String.class)); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.management.readonly", "JMX management read-only flag", Boolean.class, "false")); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.management.remote", "Remote JMX management enabled flag", Boolean.class, "false")); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.member", "Member name", String.class)); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.mode", "Operational mode", String.class)); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.override", "Deployment configuration override filename", String.class)); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.priority", "Priority", Integer.class)); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.process", "Process name", String.class)); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.proxy.threads", "Coherence*Extend service thread pool size", Integer.class)); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.rack", "Rack name", String.class)); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.role", "Role name", String.class)); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.security", "Cache access security enabled flag", Boolean.class, "false")); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.security.keystore", "Security access controller keystore file name", String.class)); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.security.password", "Keystore or cluster encryption password", String.class)); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.security.permissions", "Security access controller permissions file name", String.class)); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.shutdownhook", "Shutdown listener action", String.class)); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.site", "Site name", String.class)); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.tcmp.enabled", "TCMP enabled flag", Boolean.class, "true")); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.tcpring", "TCP Ring enabled flag", Boolean.class, "false")); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.ttl", "Multicast packet time to live (TTL)", Long.class)); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.wka", "Well known IP address", String.class)); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.coherence.wka.port", "Well known IP port", Long.class)); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.pof.enabled", "Enable POF Serialization", Boolean.class, "false")); //NOI18N
        SERVER_PROPERTIES.add(new CoherenceServerProperty("tangosol.pof.config", "Configuration file containing POF Serialization class information", String.class)); //NOI18N
    }

    /**
     * Gets version of Coherence server for given server root directory.
     *
     * @param serverRoot root folder of the Coherence server
     * @return {@link Version} of the Coherence server if found, {@code null} otherwise
     */
    public static Version getServerVersion(File serverRoot) {
        String version = null;
        File productXml = new File(serverRoot, "product.xml"); //NOI18N
        if (productXml.exists()) {
            // parse the version number from product.xml file
            version = obtainVersionFromProductFile(productXml);
            LOGGER.log(Level.FINE, "Coherence version={0} obtained from product.xml file", version);
        }

        if (version == null) {
            // get Coherence version from its jar
            version = obtainVersionFromCoherenceJar(serverRoot);
            LOGGER.log(Level.FINE, "Coherence version={0} obtained from coherence.jar file", version);
        }

        if (version != null) {
            return Version.fromDottedNotationWithFallback(version);
        }
        return null;
    }

    /**
     * Gets coherence.jar file inside given server root.
     *
     * @param serverRoot root directory of Coherence server
     * @return coherence.jar file
     */
    public static File getCoherenceJar(File serverRoot) {
        return new File(serverRoot.getAbsolutePath() + File.separator
                + PLATFORM_LIB_DIR + File.separator + COHERENCE_JAR_NAME);
    }

    /**
     * Gets 'doc' directory inside given server root.
     *
     * @param serverRoot root directory of Coherence server
     * @return doc directory if exists, {@code null} otherwise
     */
    public static File getCoherenceJavadocDir(File serverRoot) {
        File docDir = new File(serverRoot.getAbsolutePath() + File.separator
                + PLATFORM_DOC_DIR + File.separator + "api"); //NOI18I
        return (docDir.exists()) ? docDir : null;
    }

    private static String obtainVersionFromProductFile(File productFile) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            VersionHandler handler = new VersionHandler();
            saxParser.parse(productFile, handler);
            return handler.getVersion();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        } catch (ParserConfigurationException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        } catch (SAXException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        LOGGER.log(Level.FINE, "No version tag found in product.xml file.");
        return null;
    }

    private static String obtainVersionFromCoherenceJar(File serverRoot) {
        JarInputStream jis = null;
        try {
            jis = new JarInputStream(FileUtil.toFileObject(getCoherenceJar(serverRoot)).getInputStream());
            Manifest manifest = jis.getManifest();
            if (manifest != null) {
                return manifest.getMainAttributes().getValue("Implementation-Version"); //NOI18N
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        } finally {
            try {
                jis.close();
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }
        return null;
    }

    private static final class VersionHandler extends DefaultHandler {

        private String version;

        public String getVersion() {
            return version;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.equals("version")) { //NOI18N
                version = attributes.getValue("value"); //NOI18N
            }
        }

    }

}
