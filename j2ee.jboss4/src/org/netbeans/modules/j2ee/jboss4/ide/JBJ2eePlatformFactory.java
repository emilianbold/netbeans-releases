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
package org.netbeans.modules.j2ee.jboss4.ide;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;
import org.netbeans.modules.j2ee.deployment.plugins.spi.support.LookupProviderSupport;
import org.netbeans.modules.j2ee.jboss4.util.JBProperties;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import java.io.FilenameFilter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.lookup.Lookups;

 
/**
 *
 * @author Kirill Sorokin <Kirill.Sorokin@Sun.COM>
 */
public class JBJ2eePlatformFactory extends J2eePlatformFactory {
    
    private static final WeakHashMap<InstanceProperties,J2eePlatformImplImpl> instanceCache = new WeakHashMap<InstanceProperties,J2eePlatformImplImpl>();
    
    public synchronized J2eePlatformImpl getJ2eePlatformImpl(DeploymentManager dm) {
        assert JBDeploymentManager.class.isAssignableFrom(dm.getClass()) : this + " cannot create platform for unknown deployment manager:" + dm;
        // Ensure that for each server instance will be always used the same instance of the J2eePlatformImpl
        JBDeploymentManager manager  = (JBDeploymentManager) dm;
        InstanceProperties ip = manager.getInstanceProperties();
        if (ip == null) {
            throw new RuntimeException("Cannot create J2eePlatformImpl instance for " + manager.getUrl()); // NOI18N
        }
        J2eePlatformImplImpl platform = instanceCache.get(ip);
        if (platform == null) {
            platform = new J2eePlatformImplImpl(manager.getProperties());
            instanceCache.put(ip, platform);
        }
        return platform;
    }
    
    public static class J2eePlatformImplImpl extends J2eePlatformImpl {
        private static final String J2EE_API_DOC    = "docs/javaee6-doc-api.zip";    // NOI18N
        private static final Set MODULE_TYPES = new HashSet();
        static {
            MODULE_TYPES.add(J2eeModule.EAR);
            MODULE_TYPES.add(J2eeModule.WAR);
            MODULE_TYPES.add(J2eeModule.EJB);
            MODULE_TYPES.add(J2eeModule.CONN);
            MODULE_TYPES.add(J2eeModule.CLIENT);
        }

        private static final Set SPEC_VERSIONS = new HashSet();
        private static final Set SPEC_VERSIONS_5 = new HashSet();
        static {
            SPEC_VERSIONS.add(J2eeModule.J2EE_14);
            SPEC_VERSIONS_5.add(J2eeModule.J2EE_14);
            SPEC_VERSIONS_5.add(J2eeModule.JAVA_EE_5);
        }

        private LibraryImplementation[] libraries;

        private final JBProperties properties;

        public J2eePlatformImplImpl(JBProperties properties) {
            this.properties = properties;
        }

        public Set getSupportedSpecVersions() {
            if (properties.supportsJavaEE5ejb3() && properties.supportsJavaEE5web()) {
                return SPEC_VERSIONS_5;
            } else {
                return SPEC_VERSIONS;
            }
        }

        @Override
        public Set<String> getSupportedSpecVersions(Object moduleType) {
            if (properties.supportsJavaEE5web() && J2eeModule.WAR.equals(moduleType)) {
                return SPEC_VERSIONS_5;
            }
            if (properties.supportsJavaEE5ejb3() && J2eeModule.EJB.equals(moduleType)) {
                return SPEC_VERSIONS_5;
            }
            if (properties.supportsJavaEE5ear() && J2eeModule.EAR.equals(moduleType)) {
                return SPEC_VERSIONS_5;
            }

            // paranoid check
            if (properties.supportsJavaEE5ear() && properties.supportsJavaEE5ejb3()
                    && properties.supportsJavaEE5web() && !J2eeModule.CLIENT.equals(moduleType)) {
                return SPEC_VERSIONS_5;
            }
            // JavaEE5 ear, web and app client modules are not supported for JBoss 4.0
            if (properties.supportsJavaEE5ejb3()
                    && !(J2eeModule.EAR.equals(moduleType)
                        || J2eeModule.WAR.equals(moduleType) || J2eeModule.CLIENT.equals(moduleType))) {
                return SPEC_VERSIONS_5;
            }

            return SPEC_VERSIONS;
        }

        public Set getSupportedModuleTypes() {
            return MODULE_TYPES;
        }
        
        public Set/*<String>*/ getSupportedJavaPlatformVersions() {
            Set versions = new HashSet();
            versions.add("1.4"); // NOI18N
            versions.add("1.5"); // NOI18N
            return versions;
        }
        
        public JavaPlatform getJavaPlatform() {
            return properties.getJavaPlatform();
        }

        public File[] getPlatformRoots() {
            return new File[] {
                properties.getRootDir()
            };
        }
        
        private static class FF implements FilenameFilter {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar") || new File(dir, name).isDirectory(); //NOI18N
            }
        }

        public LibraryImplementation[] getLibraries() {
            if (libraries == null) {
                initLibraries();
            }
            return libraries.clone();
        }
    
        public void notifyLibrariesChanged() {
            initLibraries();
            firePropertyChange(PROP_LIBRARIES, null, libraries.clone());
        }
        
        public java.awt.Image getIcon() {
            return null;
        }

        public String getDisplayName() {
            return NbBundle.getMessage(JBJ2eePlatformFactory.class, "TITLE_JBOSS_FACTORY");

        }

        public boolean isToolSupported(String toolName) {
            
            if (J2eePlatform.TOOL_JSR109.equals(toolName)) {
                if (containsJaxWsLibraries())
                    return true;
            }            
            
            if (J2eePlatform.TOOL_WSIMPORT.equals(toolName)) {
                if (containsJaxWsLibraries())
                    return true;
            }
            
            if (J2eePlatform.TOOL_WSGEN.equals(toolName)) {
                if (containsJaxWsLibraries())
                    return true;
            }
            
            if ("JaxWs-in-j2ee14-supported".equals(toolName)) { //NOI18N
                if (containsJaxWsLibraries())
                    return true;
            }            
            
            if (!containsJaxWsLibraries() &&
                    (J2eePlatform.TOOL_WSCOMPILE.equals(toolName) || J2eePlatform.TOOL_APP_CLIENT_RUNTIME.equals(toolName)) ) {
                    return true;
            }
            if ("org.hibernate.ejb.HibernatePersistence".equals(toolName)
                    || "oracle.toplink.essentials.ejb.cmp3.EntityManagerFactoryProvider".equals(toolName)
                    || "kodo.persistence.PersistenceProviderImpl".equals(toolName)) {
                return containsPersistenceProvider(toolName);
            }
            if ("hibernatePersistenceProviderIsDefault".equals(toolName)) {
                return true;
            }
            if ("defaultPersistenceProviderJavaEE5".equals(toolName)) {
                return true;
            }

            return false;
        }
        
        private boolean containsJaxWsLibraries() {
            File root = new File(properties.getRootDir(), "client"); // NOI18N
            File jaxWsAPILib = new File(root, "jboss-jaxws.jar"); // NOI18N
            if (jaxWsAPILib.exists()) {
                return true;
            }
            jaxWsAPILib = new File(root, "jbossws-native-jaxws.jar"); // NOI18N
            if (jaxWsAPILib.exists()) {
                return true;
            }
            jaxWsAPILib = new File(root, "jaxws-api.jar"); // NOI18N
            if (jaxWsAPILib.exists()) {
                return true;
            }
            return false;
        }

        private boolean containsPersistenceProvider(String providerName) {
            return containsService(libraries, "javax.persistence.spi.PersistenceProvider", providerName);
        }
        
        private static boolean containsService(LibraryImplementation[] libraries, String serviceName, String serviceImplName) {
            for (LibraryImplementation libImpl : libraries) {
                if (containsService(libImpl, serviceName, serviceImplName)) { //NOI18N
                    return true;
                }
            }
            return false;
        }
                
        private static boolean containsService(LibraryImplementation library, String serviceName, String serviceImplName) {
            List roots = library.getContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH);
            for (Iterator it = roots.iterator(); it.hasNext();) {
                URL rootUrl = (URL) it.next();
                FileObject root = URLMapper.findFileObject(rootUrl);
                if (root != null && "jar".equals(rootUrl.getProtocol())) {  //NOI18N
                    FileObject archiveRoot = FileUtil.getArchiveRoot(FileUtil.getArchiveFile(root));
                    String serviceRelativePath = "META-INF/services/" + serviceName; //NOI18N
                    FileObject serviceFO = archiveRoot.getFileObject(serviceRelativePath);
                    if (serviceFO != null && containsService(serviceFO, serviceName, serviceImplName)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private static boolean containsService(FileObject serviceFO, String serviceName, String serviceImplName) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(serviceFO.getInputStream()));
                try {
                    String line;
                    while ((line = br.readLine()) != null) {
                        int ci = line.indexOf('#');
                        if (ci >= 0) line = line.substring(0, ci);
                        if (line.trim().equals(serviceImplName)) {
                            return true;
                        }
                    }
                } finally {
                    br.close();
                }
            } 
            catch (Exception ex) {
                try {
                    Exceptions.attachLocalizedMessage(ex, serviceFO.getURL().toString());
                } catch (FileStateInvalidException fsie) { 
                    //noop
                }
                Logger.getLogger("global").log(Level.INFO, null, ex);
            }
            return false;
        }
        
        public File[] getToolClasspathEntries(String toolName) {
            if (J2eePlatform.TOOL_WSIMPORT.equals(toolName)) {
                return getJaxWsLibraries();
            }
            if (J2eePlatform.TOOL_WSGEN.equals(toolName)) {
                return getJaxWsLibraries();
            }
            if (J2eePlatform.TOOL_WSCOMPILE.equals(toolName)) {
                File root = InstalledFileLocator.getDefault().locate("modules/ext/jaxrpc16", null, false); // NOI18N
                return new File[] {
                    new File(root, "saaj-api.jar"),     // NOI18N
                    new File(root, "saaj-impl.jar"),    // NOI18N
                    new File(root, "jaxrpc-api.jar"),   // NOI18N
                    new File(root, "jaxrpc-impl.jar"),  // NOI18N
                };
            }
            if (J2eePlatform.TOOL_APP_CLIENT_RUNTIME.equals(toolName)) {
                return new File(properties.getRootDir(), "client").listFiles(new FF()); // NOI18N
            }
            return null;
        }
        
        private File[] getJaxWsLibraries() {
            File root = new File(properties.getRootDir(), "client"); // NOI18N
            File jaxWsAPILib = new File(root, "jboss-jaxws.jar"); // NOI18N
            // JBoss without jbossws 
            if (jaxWsAPILib.exists()) {
                return new File[] {
                    new File(root, "wstx.jar"),   // NOI18N
                    new File(root, "jaxws-tools.jar"),  // NOI18N
                    new File(root, "jboss-common-client.jar"),  // NOI18N
                    new File(root, "jboss-logging-spi.jar"),  // NOI18N
                    new File(root, "stax-api.jar"),    // NOI18N
                    
                    new File(root, "jbossws-client.jar"),  // NOI18N
                    new File(root, "jboss-jaxws-ext.jar"),    // NOI18N
                    new File(root, "jboss-jaxws.jar"),    // NOI18N
                    new File(root, "jboss-saaj.jar")    // NOI18N
                };
            }
            jaxWsAPILib = new File(root, "jbossws-native-jaxws.jar"); // NOI18N
            // JBoss+jbossws-native
            if (jaxWsAPILib.exists()) {
                return new File[] {
                    new File(root, "wstx.jar"),   // NOI18N
                    new File(root, "jaxws-tools.jar"),  // NOI18N
                    new File(root, "jboss-common-client.jar"),  // NOI18N
                    new File(root, "jboss-logging-spi.jar"),  // NOI18N
                    new File(root, "stax-api.jar"),    // NOI18N

                    new File(root, "jbossws-native-client.jar"),  // NOI18N
                    new File(root, "jbossws-native-jaxws-ext.jar"),    // NOI18N
                    new File(root, "jbossws-native-jaxws.jar"),    // NOI18N
                    new File(root, "jbossws-native-saaj.jar")    // NOI18N
                };
            }
            jaxWsAPILib = new File(root, "jaxws-api.jar"); // NOI18N
            // JBoss+jbossws-metro
            if (jaxWsAPILib.exists()) {
                return new File[] {
                    new File(root, "wstx.jar"),   // NOI18N
                    new File(root, "jaxws-tools.jar"),  // NOI18N
                    new File(root, "jboss-common-client.jar"),  // NOI18N
                    new File(root, "jboss-logging-spi.jar"),  // NOI18N
                    new File(root, "stax-api.jar"),    // NOI18N

                    new File(root, "jbossws-metro-client.jar"),  // NOI18N
                    new File(root, "saaj-api.jar")    // NOI18N
                };
            }
            return null;
        }

        // copied from appserv plugin
        private URL fileToUrl(File file) throws MalformedURLException {
            URL url = file.toURI().toURL();
            if (FileUtil.isArchiveFile(url)) {
                url = FileUtil.getArchiveRoot(url);
            }
            return url;
        }
        
        public String getToolProperty(String toolName, String propertyName) {
            if (J2eePlatform.TOOL_APP_CLIENT_RUNTIME.equals(toolName)) {
                if (J2eePlatform.TOOL_PROP_MAIN_CLASS.equals(propertyName)) {
                    return ""; // NOI18N
                }
                if (J2eePlatform.TOOL_PROP_MAIN_CLASS_ARGS.equals(propertyName)) {
                    return ""; // NOI18N
                }
                if ("j2ee.clientName".equals(propertyName)) { // NOI18N
                    return "${jar.name}"; // NOI18N
                }
                if (J2eePlatform.TOOL_PROP_JVM_OPTS.equals(propertyName)) {
                    return "-Djava.naming.factory.initial=org.jnp.interfaces.NamingContextFactory" // NOI18N
                            + " -Djava.naming.provider.url=jnp://localhost:1099" // NOI18N
                            + " -Djava.naming.factory.url.pkgs=org.jboss.naming.client"; // NOI18N
                }
            }
            return null;
        }
        
            
        // private helper methods -------------------------------------------------

        private void initLibraries() {
            // create library
            LibraryImplementation lib = new J2eeLibraryTypeProvider().createLibrary();
            lib.setName(NbBundle.getMessage(JBJ2eePlatformFactory.class, "TITLE_JBOSS_LIBRARY"));
            lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH, properties.getClasses());
            lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_JAVADOC, properties.getJavadocs());
            lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_SRC, properties.getSources());
            libraries = new LibraryImplementation[] {lib};
        }

        @Override
        public Lookup getLookup() {
            Lookup baseLookup = Lookups.fixed(properties.getRootDir());
            return LookupProviderSupport.createCompositeLookup(baseLookup, "J2EE/DeploymentPlugins/JBoss4/Lookup"); //NOI18N
        }
        
    }
}
