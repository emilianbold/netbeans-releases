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
import org.netbeans.modules.j2ee.jboss4.util.JBProperties;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import java.io.FilenameFilter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

 
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
        private static final String J2EE_API_DOC    = "docs/javaee5-doc-api.zip";    // NOI18N
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

        public Set<String> getSupportedSpecVersions(Object moduleType) {
            // JavaEE5 app client is not supported for JBoss 5.x
            if (properties.supportsJavaEE5ejb3() && properties.supportsJavaEE5web() && !J2eeModule.CLIENT.equals(moduleType)) {
                return SPEC_VERSIONS_5;
            }
            // JavaEE5 web and app client modules are not supported for JBoss 4.x
            if (properties.supportsJavaEE5ejb3() && !(J2eeModule.WAR.equals(moduleType) || J2eeModule.CLIENT.equals(moduleType))) {
                return SPEC_VERSIONS_5;
            } else {
                return SPEC_VERSIONS;
            }
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
            return libraries;
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
            if (J2eePlatform.TOOL_WSCOMPILE.equals(toolName) 
                    || J2eePlatform.TOOL_APP_CLIENT_RUNTIME.equals(toolName) ) {
                return true;
            }
            if ("org.hibernate.ejb.HibernatePersistence".equals(toolName) ||
                "oracle.toplink.essentials.ejb.cmp3.EntityManagerFactoryProvider".equals(toolName) ||
                "kodo.persistence.PersistenceProviderImpl".equals(toolName))
            {
                return containsPersistenceProvider(toolName);
            }
            if ("hibernatePersistenceProviderIsDefault".equals(toolName)) {
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
                String line;
                while ((line = br.readLine()) != null) {
                    int ci = line.indexOf('#');
                    if (ci >= 0) line = line.substring(0, ci);
                    if (line.trim().equals(serviceImplName)) {
                        return true;
                    }
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
    }
}
