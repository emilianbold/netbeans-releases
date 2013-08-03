/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.common;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.glassfish.common.wizards.ServerWizardIterator;
import org.netbeans.modules.glassfish.spi.ServerUtilities;
import org.netbeans.modules.glassfish.spi.TreeParser;
import org.netbeans.modules.glassfish.spi.Utils;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author vkraemer
 */
public enum ServerDetails {
    
    /**
     * details for an instance of GlassFish Server 3.0/3.0.x
     */
    GLASSFISH_SERVER_3(NbBundle.getMessage(ServerDetails.class,"STR_3_SERVER_NAME", new Object[]{}), // NOI18N
        "deployer:gfv3ee6", // NOI18N
        new String[]{"lib/install/applications/__admingui/WEB-INF/lib/console-core-3.0-b74b"}, // NOI18N
        new String[0], // NOI18N
        300,
        "http://download.java.net/glassfish/v3/release/glassfish-v3.zip?nbretriever=fallback", // NOI18N
        "http://serverplugins.netbeans.org/glassfishv3/post701v3.txt" // NOI18N
    ),
    /**
     * details for an instance of GlassFish Server 3.0/3.0.x
     */
    GLASSFISH_SERVER_3_0_1(NbBundle.getMessage(ServerDetails.class,"STR_301_SERVER_NAME", new Object[]{}), // NOI18N
        "deployer:gfv3ee6", // NOI18N
        new String[]{"lib/install/applications/__admingui/WEB-INF/lib/console-core-3.0.1"}, // NOI18N
        new String[0], // NOI18N
        301,
        "http://download.java.net/glassfish/3.0.1/release/glassfish-3.0.1-ml.zip?nbretriever=fallback", // NOI18N
        "http://serverplugins.netbeans.org/glassfishv3/post701v3-0-1.txt" // NOI18N
    ),
    /**
     * details for an instance of GlassFish Server 3.1
     */
    GLASSFISH_SERVER_3_1(NbBundle.getMessage(ServerDetails.class, "STR_31_SERVER_NAME", new Object[]{}), // NOI18N
        "deployer:gfv3ee6wc", // NOI18N
        new String[]{"lib/install/applications/__admingui/WEB-INF/lib/console-core-3.1"}, // NOI18N
        new String[0],
        310,
        "http://download.java.net/glassfish/3.1/release/glassfish-3.1-ml.zip?nbretriever=fallback", // NOI18N
        "http://serverplugins.netbeans.org/glassfishv3/post701v3-1.txt" // NOI18N
    ),
    /**
     * details for an instance of GlassFish Server 3.1.1
     */
    GLASSFISH_SERVER_3_1_1(NbBundle.getMessage(ServerDetails.class, "STR_311_SERVER_NAME", new Object[]{}), // NOI18N
        "deployer:gfv3ee6wc", // NOI18N
        new String[]{"lib/install/applications/__admingui/WEB-INF/lib/console-core-3.1.1",}, // NOI18N
        new String[0],
        311,
        "http://download.java.net/glassfish/3.1.1/release/glassfish-3.1.1-ml.zip?nbretriever=fallback", // NOI18N
        "http://serverplugins.netbeans.org/glassfishv3/post701v3-1-1.txt" // NOI18N
            ),
    /**
     * details for an instance of GlassFish Server 3.1.2
     */
    GLASSFISH_SERVER_3_1_2(NbBundle.getMessage(ServerDetails.class, "STR_312_SERVER_NAME", new Object[]{}), // NOI18N
        "deployer:gfv3ee6wc", // NOI18N
        new String[]{"lib/install/applications/__admingui/WEB-INF/lib/console-core-3.1.2"}, // NOI18N
        new String[0],
        312,
        "http://download.java.net/glassfish/3.1.2/release/glassfish-3.1.2-ml.zip?nbretriever=fallback", // NOI18N
        "http://serverplugins.netbeans.org/glassfishv3/post71v3-1-2.txt" // NOI18N
    ),

    /**
     * details for an instance of GlassFish Server 3.1.2.2
     */
    GLASSFISH_SERVER_3_1_2_2(NbBundle.getMessage(ServerDetails.class, "STR_3122_SERVER_NAME", new Object[]{}), // NOI18N
        "deployer:gfv3ee6wc", // NOI18N
        new String[]{"lib/install/applications/__admingui/WEB-INF/lib/console-core-3.1.2.[0-9]{1,2}"}, // NOI18N
        new String[0],
        312,
        "http://download.java.net/glassfish/3.1.2.2/release/glassfish-3.1.2.2-ml.zip?nbretriever=fallback", // NOI18N
        "http://serverplugins.netbeans.org/glassfishv3/post71v3-1-2.txt" // NOI18N
    ),

    /**
     * details for an instance of GlassFish Server 4.0.0
     */
    GLASSFISH_SERVER_4_0(NbBundle.getMessage(ServerDetails.class, "STR_40_SERVER_NAME", new Object[]{}), // NOI18N
        "deployer:gfv3ee6wc", // NOI18N
        new String[]{"lib/install/applications/__admingui/WEB-INF/lib/console-core-4.0"}, // NOI18N
        new String[0],
        400,
        "http://download.java.net/glassfish/4.0/release/glassfish-4.0-ml.zip?nbretriever=fallback", // NOI18N
        "http://serverplugins.netbeans.org/glassfishv3/post71v4-0.txt" // NOI18N
    ),

    /**
     * details for an instance of GlassFish Server 4.0.1
     */
    GLASSFISH_SERVER_4_0_1(NbBundle.getMessage(ServerDetails.class, "STR_401_SERVER_NAME", new Object[]{}), // NOI18N
        "deployer:gfv3ee6wc", // NOI18N
        new String[]{"lib/install/applications/__admingui/WEB-INF/lib/console-core-4.0.1"}, // NOI18N
        new String[0],
        401,
        "http://download.java.net/glassfish/4.0.1/release/glassfish-4.0.1-ml.zip?nbretriever=fallback", // NOI18N
        "http://serverplugins.netbeans.org/glassfishv3/post71v4-0-1.txt" // NOI18N
    );

    /**
     * Creates an iterator for a wizard to instantiate server objects.
     * <p/>
     * @return Server wizard iterator initialized with supported GlassFish
     * server versions.
     */
    public static WizardDescriptor.InstantiatingIterator
            getInstantiatingIterator() {
        return new ServerWizardIterator(new ServerDetails[]{
                    GLASSFISH_SERVER_4_0_1,
                    GLASSFISH_SERVER_4_0,
                    GLASSFISH_SERVER_3_1_2_2,
                    GLASSFISH_SERVER_3_1_2,
                    GLASSFISH_SERVER_3_1_1,
                    GLASSFISH_SERVER_3_1,
                    GLASSFISH_SERVER_3_0_1,
                    GLASSFISH_SERVER_3,},
                new ServerDetails[]{
                    GLASSFISH_SERVER_4_0,
                    GLASSFISH_SERVER_3_1_2_2});
    }

    /**
     * Determine the version of the GlassFish Server installed in a directory
     * @param glassfishDir the directory that holds a GlassFish installation
     * @return -1 if the directory is not a GlassFish server install
     */
    public static int getVersionFromInstallDirectory(File glassfishDir)  {
// This commented out code shall be activated in next release to replace current
// trials and errors, it's faster.
//        GlassFishVersion version
//                = ServerUtils.getServerVersion(glassfishDir.getAbsolutePath());
        ServerDetails sd = null;
//        if (version != null) {
//            switch(version) {
//                case GF_3:       return GLASSFISH_SERVER_3.getVersion();
//                case GF_3_0_1:   return GLASSFISH_SERVER_3_0_1.getVersion();
//                case GF_3_1:     return GLASSFISH_SERVER_3_1.getVersion();
//                case GF_3_1_1:   return GLASSFISH_SERVER_3_1_1.getVersion();
//                case GF_3_1_2:   return GLASSFISH_SERVER_3_1_2.getVersion();
//                case GF_3_1_2_2:
//                case GF_3_1_2_3:
//                case GF_3_1_2_4:
//                case GF_3_1_2_5: return GLASSFISH_SERVER_3_1_2_2.getVersion();
//                case GF_4:       return GLASSFISH_SERVER_4_0.getVersion();
//                case GF_4_0_1:   return GLASSFISH_SERVER_4_0_1.getVersion();
//                default:         return -1;
//            }
//        }
        if (GLASSFISH_SERVER_4_0_1.isInstalledInDirectory(glassfishDir)) {
            sd = GLASSFISH_SERVER_4_0_1;
        } else if (GLASSFISH_SERVER_4_0.isInstalledInDirectory(glassfishDir)) {
            sd = GLASSFISH_SERVER_4_0;
        } else if (GLASSFISH_SERVER_3_1_2_2.isInstalledInDirectory(glassfishDir)) {
            sd = GLASSFISH_SERVER_3_1_2_2;
        } else if (GLASSFISH_SERVER_3_1_2.isInstalledInDirectory(glassfishDir)) {
            sd = GLASSFISH_SERVER_3_1_2;
        } else if (GLASSFISH_SERVER_3_1_1.isInstalledInDirectory(glassfishDir)){
            sd = GLASSFISH_SERVER_3_1_1;
        } else if (GLASSFISH_SERVER_3_1.isInstalledInDirectory(glassfishDir)) {
            sd = GLASSFISH_SERVER_3_1;
        } else if (GLASSFISH_SERVER_3.isInstalledInDirectory(glassfishDir)){
            sd = GLASSFISH_SERVER_3;
        }
        return  null==sd?-1:sd.getVersion();
    }

    /**
     * Determine the version of the GlassFish Server that wrote the domain.xml file
     * 
     * @param domainXml the file to analyze
     * @return -1 if domainXml is null, unreadable or not a directory
     * @throws IllegalStateException if domainXml cannot be parsed
     */
    public static int getVersionFromDomainXml(File domainXml) throws IllegalStateException {
        if (null == domainXml || !domainXml.isFile() || !domainXml.canRead()) {
            return -1;
        }
        return hasDefaultConfig(domainXml) ? GLASSFISH_SERVER_3_1.getVersion() :
            GLASSFISH_SERVER_3.getVersion();
    }

    private static boolean hasDefaultConfig(File domainXml) throws IllegalStateException {
        DomainParser dp = new DomainParser();
        List<TreeParser.Path> paths = new ArrayList<TreeParser.Path>();
        paths.add(new TreeParser.Path("/domain/configs/config",dp)); // NOI18N
        TreeParser.readXml(domainXml, paths);
        return dp.hasDefaultConfig();
    }
    
    private String displayName;
    private String uriFragment;
    private String indirectUrl;
    private String directUrl;
    private String[] requiredFiles;
    private String[] excludedFiles;
    private int versionInt;
    

    ServerDetails(String displayName, String uriFragment, 
            String[] requiredFiles, String[] excludedFiles, int versionInt,
            String directUrl, String indirectUrl) {
            this.displayName = displayName;
            this.uriFragment = uriFragment;
            this.indirectUrl = indirectUrl;
            this.directUrl = directUrl;
            this.requiredFiles = requiredFiles;
            this.excludedFiles = excludedFiles;
            this.versionInt = versionInt;
    }
    
    @Override 
    public String toString() {
        return displayName;
    }

    public String getUriFragment() {
        return uriFragment;
    }

    public int getVersion() {
        return versionInt;
    }

    /**
     * Determine if the glassfishDir holds a valid install of this release of
     * GlassFish Server.
     * @param glassfishDir
     * @return true if the glassfishDir holds this particular server version.
     */
    public boolean isInstalledInDirectory(File glassfishDir) {
        File descriminatorFile;
        boolean badFile = false;
        if (glassfishDir != null && glassfishDir.canRead() && glassfishDir.isDirectory()) {
            for (String s : requiredFiles) {
                descriminatorFile = Utils.getFileFromPattern(s+ServerUtilities.GFV3_VERSION_MATCHER, glassfishDir);
                if (null == descriminatorFile) {
                    badFile = true;
                }
            }
            for (String s : excludedFiles) {
                descriminatorFile = Utils.getFileFromPattern(s+ServerUtilities.GFV3_JAR_MATCHER, glassfishDir);
                if (null != descriminatorFile) {
                    badFile = true;
                }
            }
        } else {
            badFile = true;
        }
        return !badFile;
    }

    static class DomainParser extends TreeParser.NodeReader {

        private boolean hasDefaultConfig = false;
        private boolean hasDefaultConfig() {
            return hasDefaultConfig;
        }

        @Override
        public void readAttributes(String qname, Attributes attributes) throws SAXException {
            String name = attributes.getValue("name"); // NOI18N
            if ("default-config".equals(name)) { // NOI18N
                hasDefaultConfig = true;
            }
        }

    }

    public String getDirectUrl() {
        return directUrl;
    }

    public String getIndirectUrl() {
        return indirectUrl;
    }

}
