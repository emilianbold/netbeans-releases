/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.j2ee.weblogic9.j2ee;


import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule.Type;
import org.netbeans.modules.j2ee.deployment.plugins.spi.support.LookupProviderSupport;
import org.openide.modules.InstalledFileLocator;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;
import org.netbeans.modules.j2ee.weblogic9.WLBaseDeploymentManager;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * A sub-class of the J2eePlatformFactory that is set up to return the 
 * plugin-specific J2eePlatform.
 * 
 * @author Kirill Sorokin
 */
public class WLJ2eePlatformFactory extends J2eePlatformFactory {

    private static final Logger LOGGER = Logger.getLogger(WLJ2eePlatformFactory.class.getName());

    @Override
    public J2eePlatformImpl getJ2eePlatformImpl(DeploymentManager dm) {
        assert WLBaseDeploymentManager.class.isAssignableFrom(dm.getClass()) : this + " cannot create platform for unknown deployment manager:" + dm;
        return new J2eePlatformImplImpl((WLBaseDeploymentManager)dm);
    }
    
    private static class J2eePlatformImplImpl extends J2eePlatformImpl {
        
        private static final String J2EE_API_DOC    = "docs/javaee6-doc-api.zip";    // NOI18N
        private static final Set<Type> MODULE_TYPES = new HashSet<Type>();
        static {
            MODULE_TYPES.add(J2eeModule.Type.EAR);
            MODULE_TYPES.add(J2eeModule.Type.WAR);
            MODULE_TYPES.add(J2eeModule.Type.EJB);
        }

        private final Set<Profile> PROFILES = new HashSet<Profile>();
        
//        private String platformRoot = WLPluginProperties.getInstance().getInstallLocation();
        private String platformRoot;
        
        private LibraryImplementation[] libraries = null;
        
        /**
         * The platform icon's URL
         */
        private static final String ICON = "org/netbeans/modules/" +   // NOI18N
                "j2ee/weblogic9/resources/16x16.gif";                  // NOI18N
        
        /**
         * The server's deployment manager, to be exact the plugin's wrapper for
         * it
         */
        WLBaseDeploymentManager dm;
        
        public J2eePlatformImplImpl(WLBaseDeploymentManager dm) {
            this.dm = dm;
            
            // Allow J2EE 1.4 Projects
            PROFILES.add(Profile.J2EE_14);
            
            // Check for WebLogic Server 10x to allow Java EE 5 Projects
            String version = WLPluginProperties.getWeblogicDomainVersion(dm.getInstanceProperties().getProperty(WLPluginProperties.DOMAIN_ROOT_ATTR));
            
            if (version != null && version.contains("10")) { // NOI18N
                PROFILES.add(Profile.JAVA_EE_5);
            }
        }
        
        public boolean isToolSupported(String toolName) {
            if (J2eePlatform.TOOL_WSGEN.equals(toolName) || J2eePlatform.TOOL_WSIMPORT.equals(toolName)) {
                return true;
            }
            if (J2eePlatform.TOOL_JSR109.equals(toolName)) {
                return false; // to explicitelly emphasise that JSR 109 is not supported
            }
            if ("defaultPersistenceProviderJavaEE5".equals(toolName)) {
                return true;
            }
            return false;
        }
        
        public File[] getToolClasspathEntries(String toolName) {
            File[] cp = new File[0];
            if (J2eePlatform.TOOL_WSGEN.equals(toolName) || J2eePlatform.TOOL_WSIMPORT.equals(toolName)) {
                cp = new File[] { new File(getPlatformRoot(), "server/lib/weblogic.jar") }; // NOI18N
            }
            return cp;
        }

        @Override
        public Set<Profile> getSupportedProfiles() {
            return PROFILES;
        }

        @Override
        public Set<Type> getSupportedTypes() {
            return MODULE_TYPES;
        }

        public Set/*<String>*/ getSupportedJavaPlatformVersions() {
            Set versions = new HashSet();
            versions.add("1.4"); // NOI18N
            versions.add("1.5"); // NOI18N
            return versions;
        }
        
        public JavaPlatform getJavaPlatform() {
            return null;
        }
        
        public java.io.File[] getPlatformRoots() {
            return new File[]{new File(getPlatformRoot())};
        }
        
        public LibraryImplementation[] getLibraries() {
            if (libraries == null) {
                initLibraries();
            }
            return libraries;
        }
        
        private void initLibraries() {
            
            // create a new library
            LibraryImplementation library = new J2eeLibraryTypeProvider().
                    createLibrary();
            
            // set its name
            library.setName(NbBundle.getMessage(WLJ2eePlatformFactory.class, 
                    "LIBRARY_NAME"));                                  // NOI18N
            
            // add the required jars to the library
            try {
                List list = new ArrayList();
                list.add(fileToUrl(new File(getPlatformRoot(), "server/lib/weblogic.jar")));    // NOI18N
                File apiFile = new File(getPlatformRoot(), "server/lib/api.jar"); // NOI18N
                if (apiFile.exists()) {
                    list.add(fileToUrl(apiFile));
                    list.addAll(getJarClassPath(apiFile));
                }

                // file needed for jsp parsing WL9 and WL10
                list.add(fileToUrl(new File(getPlatformRoot(), "server/lib/wls-api.jar")));         // NOI18N

                library.setContent(J2eeLibraryTypeProvider.
                        VOLUME_TYPE_CLASSPATH, list);
               File j2eeDoc = InstalledFileLocator.getDefault().locate(J2EE_API_DOC, null, false);
               if (j2eeDoc != null) {
                   list = new ArrayList();
                   list.add(fileToUrl(j2eeDoc));
                   library.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_JAVADOC, list);
               }
            } catch (MalformedURLException e) {
                Logger.getLogger("global").log(Level.WARNING, null, e);
            }
            
            libraries = new LibraryImplementation[1];
            libraries[0] = library;
        }
        
        /**
         * Gets the platform icon. A platform icon is the one that appears near
         * the libraries attached to j2ee project.
         * 
         * @return the platform icon
         */
        public Image getIcon() {
            return ImageUtilities.loadImage(ICON);
        }
        
        /**
         * Gets the platform display name. This one appears exactly to the 
         * right of the platform icon ;)
         * 
         * @return the platform's display name
         */
        public String getDisplayName() {
            return NbBundle.getMessage(WLJ2eePlatformFactory.class, "PLATFORM_NAME"); // NOI18N
        }
        
        /**
         * Converts a file to the URI in system resources.
         * Copied from the plugin for Sun Appserver 8
         * 
         * @param file a file to be converted
         * 
         * @return the resulting URI
         */
        private URL fileToUrl(File file) throws MalformedURLException {
            URL url = file.toURI().toURL();
            if (FileUtil.isArchiveFile(url)) {
                url = FileUtil.getArchiveRoot(url);
            }
            return url;
        }

        private List<URL> getJarClassPath(File apiFile) {
            List<URL> urls = new ArrayList<URL>();

            try {
                JarFile file = new JarFile(apiFile);
                try {
                    Manifest manifest = file.getManifest();
                    Attributes attrs = manifest.getMainAttributes();
                    String value = attrs.getValue("Class-Path"); //NOI18N
                    if (value != null) {
                        String[] values = value.split("\\s+"); // NOI18N
                        FileObject baseDir = FileUtil.toFileObject(
                                FileUtil.normalizeFile(new File(new File(getPlatformRoot(), "server"), "lib"))); // NOI18N
                        if (baseDir != null) {
                            for (String cpElement : values) {
                                if (!"".equals(cpElement.trim())) { // NOI18N
                                    FileObject fo = baseDir.getFileObject(cpElement);
                                    if (fo == null) {
                                        continue;
                                    }
                                    File fileElem = FileUtil.normalizeFile(FileUtil.toFile(fo));
                                    if (fileElem != null) {
                                        urls.add(fileToUrl(fileElem));
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, "Could not read Weblogic api.jar", ex);
                } finally {
                    file.close();
                }
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, "Could not open Weblogic api.jar", ex);
            }

            return urls;
        }
        
        private String getPlatformRoot() {
            if (platformRoot == null)
                platformRoot = dm.getInstanceProperties().getProperty(WLPluginProperties.SERVER_ROOT_ATTR);
            
            return platformRoot;
        }

        @Override
        public Lookup getLookup() {
            Lookup baseLookup = Lookups.fixed(new File(getPlatformRoot()));
            return LookupProviderSupport.createCompositeLookup(baseLookup, "J2EE/DeploymentPlugins/WebLogic9/Lookup"); //NOI18N
        }


    }
}
