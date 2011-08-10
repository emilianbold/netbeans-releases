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
package org.netbeans.modules.j2ee.weblogic9.j2ee;


import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule.Type;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency;
import org.netbeans.modules.j2ee.deployment.plugins.spi.support.LookupProviderSupport;
import org.openide.modules.InstalledFileLocator;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.common.ui.BrokenServerLibrarySupport;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibrary;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl2;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerLibraryFactory;
import org.netbeans.modules.j2ee.weblogic9.WLDeploymentFactory;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.modules.j2ee.weblogic9.config.WLServerLibraryManager;
import org.netbeans.modules.j2ee.weblogic9.config.WLServerLibrarySupport;
import org.netbeans.modules.j2ee.weblogic9.config.WLServerLibrarySupport.WLServerLibrary;
import org.netbeans.modules.javaee.specs.support.api.JpaProvider;
import org.netbeans.modules.javaee.specs.support.spi.JaxRsStackSupportImplementation;
import org.netbeans.modules.javaee.specs.support.spi.JaxWsPoliciesSupportImplementation;
import org.netbeans.modules.javaee.specs.support.spi.JpaProviderFactory;
import org.netbeans.modules.javaee.specs.support.spi.JpaSupportImplementation;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A sub-class of the J2eePlatformFactory that is set up to return the 
 * plugin-specific J2eePlatform.
 * 
 * @author Kirill Sorokin
 */
public class WLJ2eePlatformFactory extends J2eePlatformFactory {
    
    
    public static final String ORACLE = "oracle";                                  // NOI18N 
    
    private static final Logger LOGGER = Logger.getLogger(WLJ2eePlatformFactory.class.getName());

    private static final String OPENJPA_JPA_PROVIDER = "org.apache.openjpa.persistence.PersistenceProviderImpl"; // NOI18N

    private static final String ECLIPSELINK_JPA_PROVIDER = "org.eclipse.persistence.jpa.PersistenceProvider"; // NOI18N

    // always prefer JPA 1.0 see #189205
    private static final Pattern JAVAX_PERSISTENCE_PATTERN = Pattern.compile(
            "^.*javax\\.persistence.*_1-\\d+-\\d+\\.jar$");

    // _2.0 is for javax.persistence.dwp_2.0.jar
    private static final Pattern JAVAX_PERSISTENCE_2_PATTERN = Pattern.compile(
            "^.*javax\\.persistence.*((_2-\\d+-\\d+)|(_2.0))\\.jar$");
    
    private static final Pattern OEPE_CONTRIBUTIONS_PATTERN = Pattern.compile("^.*oepe-contributions\\.jar.*$"); // NOI18N
    
    private static final FilenameFilter PATCH_DIR_FILTER = new PrefixesFilter("patch_wls"); // NOI18N    

    private static final Version JDK6_SUPPORTED_SERVER_VERSION = Version.fromJsr277NotationWithFallback("10.3"); // NOI18N

    @Override
    public J2eePlatformImpl getJ2eePlatformImpl(DeploymentManager dm) {
        assert WLDeploymentManager.class.isAssignableFrom(dm.getClass()) : this + " cannot create platform for unknown deployment manager:" + dm;
        return ((WLDeploymentManager) dm).getJ2eePlatformImpl();
    }
    
    public static List<URL> getWLSClassPath(@NonNull File platformRoot,
            @NullAllowed File mwHome, @NullAllowed J2eePlatformImplImpl j2eePlatform) {

        List<URL> list = new ArrayList<URL>();
        try {
            // the WLS jar is intentional
            File weblogicFile = new File(platformRoot, WLPluginProperties.WEBLOGIC_JAR);
            if (weblogicFile.exists()) {
                list.add(fileToUrl(weblogicFile));
            }
            File apiFile = new File(platformRoot, "server/lib/api.jar"); // NOI18N
            if (apiFile.exists()) {
                list.add(fileToUrl(apiFile));
                list.addAll(getJarClassPath(apiFile));
            }

            // patches
            // FIXME multiple versions under same middleware
            if (mwHome != null) {
                File[] patchDirCandidates = mwHome.listFiles(PATCH_DIR_FILTER);
                if (patchDirCandidates != null) {
                    for (File candidate : patchDirCandidates) {
                        File jarFile = FileUtil.normalizeFile(new File(candidate,
                                "profiles/default/sys_manifest_classpath/weblogic_patch.jar")); // NOI18N
                        if (jarFile.exists()) {
                            list.add(fileToUrl(jarFile));
                            List<URL> deps = getJarClassPath(jarFile);
                            list.addAll(deps);
                            for (URL dep : deps) {
                                List<URL> innerDeps = getJarClassPath(dep);
                                list.addAll(innerDeps);
                                for (URL innerDep : innerDeps) {
                                    if (innerDep.getPath().contains("patch_jars")) { // NOI18N
                                        list.addAll(getJarClassPath(innerDep));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // oepe contributions
            if (weblogicFile.exists()) {
                List<URL> cp = getJarClassPath(weblogicFile);
                URL oepe = null;
                for (URL cpElem : cp) {
                    if (OEPE_CONTRIBUTIONS_PATTERN.matcher(cpElem.getPath()).matches()) {
                        oepe = cpElem;
                        //list.add(oepe);
                        break;
                    }
                }
                if (oepe != null) {
                    list.addAll(getJarClassPath(oepe));
                }
            }

            addPersistenceLibrary(list, mwHome, j2eePlatform);

            // file needed for jsp parsing WL9 and WL10
            list.add(fileToUrl(new File(platformRoot, "server/lib/wls-api.jar"))); // NOI18N
        } catch (MalformedURLException e) {
            LOGGER.log(Level.WARNING, null, e);
        }
        return list;
    }
    
    /**
     * Converts a file to the URI in system resources.
     * Copied from the plugin for Sun Appserver 8
     * 
     * @param file a file to be converted
     * 
     * @return the resulting URI
     */
    static URL fileToUrl(File file) throws MalformedURLException {
        URL url = file.toURI().toURL();
        if (FileUtil.isArchiveFile(url)) {
            url = FileUtil.getArchiveRoot(url);
        }
        return url;
    }

    // package for tests only
    static List<URL> getJarClassPath(URL url) {
        URL fileUrl = FileUtil.getArchiveFile(url);
        if (fileUrl != null) {
            FileObject fo = URLMapper.findFileObject(fileUrl);
            if (fo != null) {
                File file = FileUtil.toFile(fo);
                if (file != null) {
                    return getJarClassPath(file);
                }
            }
        }
        return Collections.emptyList();
    }

    // package for tests only
    static List<URL> getJarClassPath(File jarFile) {
        List<URL> urls = new ArrayList<URL>();

        try {
            JarFile file = new JarFile(jarFile);
            try {
                Manifest manifest = file.getManifest();
                Attributes attrs = manifest.getMainAttributes();
                String value = attrs.getValue("Class-Path"); //NOI18N
                if (value != null) {
                    String[] values = value.split("\\s+"); // NOI18N
                    File parent = FileUtil.normalizeFile(jarFile).getParentFile();
                    if (parent != null) {
                        for (String cpElement : values) {
                            if (!"".equals(cpElement.trim())) { // NOI18N
                                File f = new File(cpElement);
                                if (!f.isAbsolute()) {
                                    f = new File(parent, cpElement);
                                }
                                f = FileUtil.normalizeFile(f);
                                if (!f.exists()) {
                                    continue;
                                }
                                urls.add(fileToUrl(f));
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, "Could not read WebLogic JAR", ex);
            } finally {
                file.close();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Could not open WebLogic JAR", ex);
        }

        return urls;
    }    
    
    //XXX there seems to be a bug in api.jar - it does not contain link to javax.persistence
    // method checks whether there is already persistence API present in the list
    private static void addPersistenceLibrary(List<URL> list, @NullAllowed File middleware,
            @NullAllowed J2eePlatformImplImpl j2eePlatform) throws MalformedURLException {

        boolean foundJpa2 = false;
        boolean foundJpa1 = false;
        for (Iterator<URL> it = list.iterator(); it.hasNext(); ) {
            URL archiveUrl = FileUtil.getArchiveFile(it.next());
            if (archiveUrl != null) {
                if (JAVAX_PERSISTENCE_2_PATTERN.matcher(archiveUrl.getPath()).matches()) {
                    foundJpa2 = true;
                    break;
                } else if (JAVAX_PERSISTENCE_PATTERN.matcher(archiveUrl.getPath()).matches()) {
                    foundJpa1 = true;
                    break;
                }
            }
        }  

        if (j2eePlatform != null) {
            synchronized (j2eePlatform) {
                j2eePlatform.jpa2Available = foundJpa2;
            }
        }
        if (foundJpa2 || foundJpa1) {
            return;
        }

        if (middleware != null) {
            File modules = getMiddlewareModules(middleware);
            if (modules.exists() && modules.isDirectory()) {
                File[] persistenceCandidates = modules.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return JAVAX_PERSISTENCE_PATTERN.matcher(name).matches();
                    }
                });
                if (persistenceCandidates.length > 0) {
                    for (File candidate : persistenceCandidates) {
                        list.add(fileToUrl(candidate));
                    }
                    if (persistenceCandidates.length > 1) {
                        LOGGER.log(Level.INFO, "Multiple javax.persistence JAR candidates");
                    }
                }
            }
        }
    }     
    
    private static File getMiddlewareModules(File middleware) {
        File modules = new File(middleware, "modules"); // NOI18N
        if (!modules.exists() || !modules.isDirectory()) {
            modules = new File(new File(middleware, "oracle_common"), "modules"); // NOI18N
        }
        return modules;
    }

    public static class J2eePlatformImplImpl extends J2eePlatformImpl2 {

        /**
         * The platform icon's URL
         */
        private static final String ICON = "org/netbeans/modules/j2ee/weblogic9/resources/16x16.gif"; // NOI18N

        private static final String J2EE_API_DOC    = "docs/javaee6-doc-api.zip";    // NOI18N

        private final Set<Type> moduleTypes = new HashSet<Type>();

        private final Set<Profile> profiles = new HashSet<Profile>();

        private final WLDeploymentManager dm;

        private final ChangeListener domainChangeListener;

        private String platformRoot;
        
        /** <i>GuardedBy("this")</i> */
        private LibraryImplementation[] libraries = null;

        /** <i>GuardedBy("this")</i> */
        private String defaultJpaProvider;
        
        /** <i>GuardedBy("this")</i> */
        private boolean jpa2Available;
        
        public J2eePlatformImplImpl(WLDeploymentManager dm) {
            this.dm = dm;

            moduleTypes.add(Type.WAR);
            moduleTypes.add(Type.EJB);
            moduleTypes.add(Type.EAR);

            // Allow J2EE 1.4 Projects
            profiles.add(Profile.J2EE_14);
            
            // Check for WebLogic Server 10x to allow Java EE 5 Projects
            Version version = dm.getDomainVersion();
            
            if (version != null) {
                if (version.isAboveOrEqual(WLDeploymentFactory.VERSION_10)) {
                    profiles.add(Profile.JAVA_EE_5);
                }
                if (version.isAboveOrEqual(WLDeploymentFactory.VERSION_11)) {
                    profiles.add(Profile.JAVA_EE_6_FULL);
                    profiles.add(Profile.JAVA_EE_6_WEB);
                }
            }

            domainChangeListener = new DomainChangeListener(this);
            dm.addDomainChangeListener(WeakListeners.change(domainChangeListener, dm));
        }

        @Override
        public boolean isToolSupported(String toolName) {
            if (J2eePlatform.TOOL_WSGEN.equals(toolName) || J2eePlatform.TOOL_WSIMPORT.equals(toolName)) {
                return true;
            }
            if (J2eePlatform.TOOL_JSR109.equals(toolName)) {
                return false; // to explicitelly emphasise that JSR 109 is not supported
            }

            if ("defaultPersistenceProviderJavaEE5".equals(toolName)) { // NOI18N
                return true;
            }
            
            // this property says whether following two props are taken into account
            if("jpaversionverification".equals(toolName)) { // NOI18N
                return true;
            }
            if("jpa1.0".equals(toolName)) { // NOI18N
                return true;
            }
            if("jpa2.0".equals(toolName)) { // NOI18N
                return isJpa2Available();
            }

            // shortcut
            if (!"openJpaPersistenceProviderIsDefault1.0".equals(toolName) // NOI18N
                    && !"eclipseLinkPersistenceProviderIsDefault".equals(toolName) // NOI18N
                    && !OPENJPA_JPA_PROVIDER.equals(toolName)
                    && !ECLIPSELINK_JPA_PROVIDER.equals(toolName)) {
                return false;
            }

            // JPA provider part
            String currentDefaultJpaProvider = getDefaultJpaProvider();
            if ("openJpaPersistenceProviderIsDefault1.0".equals(toolName)) { // NOI18N
                return currentDefaultJpaProvider.equals(OPENJPA_JPA_PROVIDER);
            }
            if ("eclipseLinkPersistenceProviderIsDefault".equals(toolName)) { // NOI18N
                return currentDefaultJpaProvider.equals(ECLIPSELINK_JPA_PROVIDER);
            }

            // both are supported
            if (OPENJPA_JPA_PROVIDER.equals(toolName) || ECLIPSELINK_JPA_PROVIDER.equals(toolName)) {
                return true;
            }

            return false;
        }
        
        public File[] getToolClasspathEntries(String toolName) {
            File[] cp = new File[0];
            if (J2eePlatform.TOOL_WSGEN.equals(toolName) || J2eePlatform.TOOL_WSIMPORT.equals(toolName)) {
                File weblogicJar = WLPluginProperties.getWeblogicJar(dm);
                if (weblogicJar != null) {
                    cp = new File[] { weblogicJar };
                }
            }
            return cp;
        }

        @Override
        public Set<Profile> getSupportedProfiles() {
            return profiles;
        }

        @Override
        public Set<Type> getSupportedTypes() {
            return moduleTypes;
        }

        public Set/*<String>*/ getSupportedJavaPlatformVersions() {
            Set versions = new HashSet();
            versions.add("1.4"); // NOI18N
            versions.add("1.5"); // NOI18N
            if (dm.getServerVersion() != null
                    && dm.getServerVersion().isAboveOrEqual(JDK6_SUPPORTED_SERVER_VERSION)) {
                versions.add("1.6");
            }
            return versions;
        }
        
        public JavaPlatform getJavaPlatform() {
            return null;
        }
        
        @Override
        public File[] getPlatformRoots() {
            File server = getServerHome();
            File domain = getDomainHome();
            File middleware = getMiddlewareHome();
            
            if (middleware != null) {
                return new File[] {server, domain, middleware};
            }
            return new File[] {server, domain};
        }

        @Override
        public File getDomainHome() {
            File domain = new File(dm.getInstanceProperties().getProperty(
                    WLPluginProperties.DOMAIN_ROOT_ATTR));
            
            assert domain.isAbsolute();
            return domain;
        }

        @Override
        public File getServerHome() {
            File server = new File(getPlatformRoot());
            
            assert server.isAbsolute();
            return server;
        }
        
        @Override
        public File getMiddlewareHome() {
            return WLPluginProperties.getMiddlewareHome(getServerHome());
        }        

        @Override
        public synchronized LibraryImplementation[] getLibraries() {
            if (libraries != null) {
                return libraries;
            }

            initLibrariesForWLS();
            return libraries;
        }

        @Override
        public LibraryImplementation[] getLibraries(Set<ServerLibraryDependency> libraries) {
            // FIXME cache & listen for file changes
            String domainDir = dm.getInstanceProperties().getProperty(WLPluginProperties.DOMAIN_ROOT_ATTR);
            assert domainDir != null;
            String serverDir = dm.getInstanceProperties().getProperty(WLPluginProperties.SERVER_ROOT_ATTR);
            assert serverDir != null;
            WLServerLibrarySupport support = new WLServerLibrarySupport(new File(serverDir), new File(domainDir));

            Map<ServerLibrary, List<File>> serverLibraries =  null;
            try {
                serverLibraries = support.getClasspathEntries(libraries);
            } catch (ConfigurationException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }

            if (serverLibraries == null || serverLibraries.isEmpty()) {
                return getLibraries();
            }

            List<LibraryImplementation> serverImpl = new ArrayList<LibraryImplementation>();
            for (Map.Entry<ServerLibrary, List<File>> entry : serverLibraries.entrySet()) {
                LibraryImplementation library = new J2eeLibraryTypeProvider().
                        createLibrary();
                ServerLibrary lib = entry.getKey();
                // really localized ?
                // FIXME more accurate name needed ?
                if (lib.getSpecificationTitle() == null && lib.getName() == null) {
                    library.setName(NbBundle.getMessage(WLJ2eePlatformFactory.class,
                        "UNKNOWN_SERVER_LIBRARY_NAME"));
                } else {
                    library.setName(NbBundle.getMessage(WLJ2eePlatformFactory.class,
                        "SERVER_LIBRARY_NAME", new Object[] {
                            lib.getSpecificationTitle() == null ? lib.getName() : lib.getSpecificationTitle(),
                            lib.getSpecificationVersion() == null ? "" : lib.getSpecificationVersion()}));
                }

                List<URL> cp = new ArrayList<URL>();
                for (File file : entry.getValue()) {
                    try {
                        cp.add(fileToUrl(file));
                    } catch (MalformedURLException ex) {
                        LOGGER.log(Level.INFO, null, ex);
                    }
                }

                library.setContent(J2eeLibraryTypeProvider.
                        VOLUME_TYPE_CLASSPATH, cp);
                serverImpl.add(library);
            }
            // add the standard server cp as last it is logical and prevents
            // issues like #188753
            serverImpl.addAll(Arrays.asList(getLibraries()));

            return serverImpl.toArray(new LibraryImplementation[serverImpl.size()]);
        }
        
        public void notifyLibrariesChange() {
            synchronized (this) {
                libraries = null;
            }
            firePropertyChange(PROP_LIBRARIES, null, getLibraries());
        }
        
        private void initLibrariesForWLS() {
            LibraryImplementation library = new J2eeLibraryTypeProvider().
                    createLibrary();
            
            // set its name
            library.setName(NbBundle.getMessage(WLJ2eePlatformFactory.class, 
                    "LIBRARY_NAME"));
            
            // add the required jars to the library
            try {
                List<URL> list = new ArrayList<URL>();
                list.addAll(getWLSClassPath(getServerHome(), getMiddlewareHome(), this));

                library.setContent(J2eeLibraryTypeProvider.
                        VOLUME_TYPE_CLASSPATH, list);
                File j2eeDoc = InstalledFileLocator.getDefault().locate(J2EE_API_DOC, null, false);
                if (j2eeDoc != null) {
                    list = new ArrayList();
                    list.add(fileToUrl(j2eeDoc));
                    library.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_JAVADOC, list);
                }
            } catch (MalformedURLException e) {
                LOGGER.log(Level.WARNING, null, e);
            }
            
            synchronized (this) {
                libraries = new LibraryImplementation[1];
                libraries[0] = library;
            }
        }
        
        public synchronized boolean isJpa2Available() {
            if (libraries != null) {
                return jpa2Available;
            }
            
            // initialize and return value
            getLibraries();
            return jpa2Available;
        }

        private String getDefaultJpaProvider() {
            synchronized (this) {
                if (defaultJpaProvider != null) {
                    return defaultJpaProvider;
                }
            }

            // XXX we could use JPAMBean for remote instances
            String newDefaultJpaProvider = null;
            FileObject config = WLPluginProperties.getDomainConfigFileObject(dm);
            if (config != null) {
                try {
                    SAXParserFactory factory = SAXParserFactory.newInstance();
                    SAXParser parser = factory.newSAXParser();
                    JPAHandler handler = new JPAHandler();
                    InputStream is = new BufferedInputStream(config.getInputStream());
                    try {
                        parser.parse(is, handler);
                        newDefaultJpaProvider = handler.getDefaultJPAProvider();
                    } finally {
                        is.close();
                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                } catch (ParserConfigurationException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                } catch (SAXException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                }
            }
            if (newDefaultJpaProvider == null) {
                newDefaultJpaProvider = OPENJPA_JPA_PROVIDER;
            }

            synchronized (this) {
                defaultJpaProvider = newDefaultJpaProvider;
                return defaultJpaProvider;
            }
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
        
        private String getPlatformRoot() {
            if (platformRoot == null) {
                platformRoot = dm.getInstanceProperties().getProperty(WLPluginProperties.SERVER_ROOT_ATTR);
            }
            return platformRoot;
        }

        @Override
        public Lookup getLookup() {
            Lookup baseLookup = Lookups.fixed(new File(getPlatformRoot()), 
                    new JpaSupportImpl(this), new JsxWsPoliciesSupportImpl(this),
                    new JaxRsStackSupportImpl(this));
            return LookupProviderSupport.createCompositeLookup(baseLookup, "J2EE/DeploymentPlugins/WebLogic9/Lookup"); //NOI18N
        }
    }

    private static class DomainChangeListener implements ChangeListener {

        private final J2eePlatformImplImpl platform;

        private Set<WLServerLibrary> oldLibraries = Collections.emptySet();

        public DomainChangeListener(J2eePlatformImplImpl platform) {
            this.platform = platform;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            synchronized (platform) {
                platform.defaultJpaProvider = null;
            }

            Set<WLServerLibrary> tmpNewLibraries =
                    new WLServerLibrarySupport(platform.dm).getDeployedLibraries();
            Set<WLServerLibrary> tmpOldLibraries = null;
            synchronized (this) {
                tmpOldLibraries = new HashSet<WLServerLibrary>(oldLibraries);
                oldLibraries = tmpNewLibraries;
            }

            if (fireChange(tmpOldLibraries, tmpNewLibraries)) {
                LOGGER.log(Level.FINE, "Firing server libraries change");
                platform.firePropertyChange(J2eePlatformImpl.PROP_SERVER_LIBRARIES, null, null);
            }
        }

        private boolean fireChange(Set<WLServerLibrary> paramOldLibraries, Set<WLServerLibrary> paramNewLibraries) {
            if (paramOldLibraries.size() != paramNewLibraries.size()) {
                return true;
            }

            Set<WLServerLibrary> newLibraries = new HashSet<WLServerLibrary>(paramNewLibraries);
            for (Iterator<WLServerLibrary> it = newLibraries.iterator(); it.hasNext();) {
                WLServerLibrary newLib = it.next();
                for (WLServerLibrary oldLib : paramOldLibraries) {
                    if (WLServerLibrarySupport.sameLibraries(newLib, oldLib)) {
                        it.remove();
                        break;
                    }
                }
            }

            return !newLibraries.isEmpty();
        }
    }
    
    private static class JaxRsStackSupportImpl implements JaxRsStackSupportImplementation {
        
        private static final String API = "api";                    // NOI18N
        private static final String JAX_RS = "jax-rs";              // NOI18N
        
        private static final String JERSEY = "jersey";              //NOI18N
        private static final String JSON = "json";                  //NOI18N
        private static final String JETTISON ="jettison";           //NOI18N
        private static final String ROME ="rome";                   //NOI18N
    

        JaxRsStackSupportImpl ( J2eePlatformImplImpl platformImpl){
            this.platformImpl = platformImpl;
        }
        
        @Override
        public boolean addJsr311Api( Project project ) {
            /*
             *  WL has a deployable JSR311 war. But it will appear in the project's
             *  classpath only after specific user action. This is unacceptable 
             *  because generated source code requires classes independently 
             *  of additional explicit user actions. 
             *  
             *  So the following code returns true only if there is already deployed
             *  JSR311 library on the server
             */
            WLServerLibrarySupport support =getLibrarySupport();
            Set<WLServerLibrary> libraries = support.getDeployedLibraries();
            for (WLServerLibrary library : libraries) {
                String title = library.getImplementationTitle();
                if ( title!= null && title.toLowerCase(Locale.ENGLISH).contains(JAX_RS) && 
                        title.toLowerCase(Locale.ENGLISH).contains(API))
                {
                    ServerLibrary apiLib = ServerLibraryFactory.
                            createServerLibrary(library);
                    J2eeModuleProvider provider = project.getLookup().lookup(
                        J2eeModuleProvider.class);
                    try {
                        provider.getConfigSupport().configureLibrary(
                                ServerLibraryDependency.minimalVersion(
                                        apiLib.getName(),
                                        apiLib.getSpecificationVersion(),
                                        apiLib.getImplementationVersion()));
                    } catch (org.netbeans.modules.j2ee.deployment.
                            common.api.ConfigurationException ex) 
                    {
                        Logger.getLogger(JaxRsStackSupportImpl.class.getName()).
                                log(Level.INFO, null, ex);
                        return false;
                    }
                    return true;
                }
            }
            return false;
        }
        
        @Override
        public boolean extendsJerseyProjectClasspath(Project project) {
            J2eeModuleProvider provider = project.getLookup().lookup(
                    J2eeModuleProvider.class);
            Collection<ServerLibrary> serverLibraries = getServerJerseyLibraries();
            if (provider != null && serverLibraries.size() > 0) {
                try {
                    for (ServerLibrary serverLibrary : serverLibraries) {
                        provider.getConfigSupport().configureLibrary(
                                ServerLibraryDependency.minimalVersion(
                                serverLibrary.getName(),
                                serverLibrary.getSpecificationVersion(),
                                serverLibrary.getImplementationVersion()));
                    }
                    Preferences prefs = ProjectUtils.getPreferences(project,
                            ProjectUtils.class, true);
                    prefs.put(BrokenServerLibrarySupport.OFFER_LIBRARY_DEPLOYMENT,
                            Boolean.TRUE.toString());
                    return true;
                } catch (org.netbeans.modules.j2ee.deployment.common.api.
                        ConfigurationException ex) 
                {
                    Logger.getLogger(JaxRsStackSupportImpl.class.getName()).log(
                            Level.INFO,
                            "Exception during extending an web project", ex); //NOI18N
                    return false;
                }
            } else {
                return false;
            }
        }
        
        @Override
        public void removeJaxRsLibraries(Project project) {
            // TODO: is it possible to remove ServerLibrary from project classpath ?
        }
        
        private Collection<ServerLibrary> getServerJerseyLibraries() {
            WLServerLibraryManager manager = getLibraryManager();
            Collection<ServerLibrary> libraries = new LinkedList<ServerLibrary>();  
            libraries.addAll(findJerseyLibraries(manager.getDeployableLibraries()));
            libraries.addAll(findJerseyLibraries(manager.getDeployedLibraries()));
            return libraries;
        }
        
        private Collection<ServerLibrary> findJerseyLibraries(
                Collection<ServerLibrary> collection) {
            Collection<ServerLibrary> result = new ArrayList<ServerLibrary>(
                    collection.size());
            for (Iterator<ServerLibrary> iterator = collection.iterator(); 
                    iterator.hasNext();) 
            {
                ServerLibrary library = iterator.next();
                String title = library.getImplementationTitle();
                if ( title == null ){
                    continue;
                }
                title = title.toLowerCase(Locale.ENGLISH);
                if (title.contains(JERSEY) || title.contains(JSON)
                        || title.contains(ROME) || title.contains(JETTISON)) {
                    result.add(library);
                }
            }
            return result;
        }
        
        private WLServerLibraryManager getLibraryManager(){
            return new WLServerLibraryManager(platformImpl.dm);
        }
        
        private WLServerLibrarySupport getLibrarySupport(){
            return new WLServerLibrarySupport(platformImpl.dm);
        }
        
        private J2eePlatformImplImpl platformImpl;
    }
    
    private static class JsxWsPoliciesSupportImpl implements JaxWsPoliciesSupportImplementation {
        private static final String ORACLE_COMMON_MODULES = "oracle_common/modules/";  // NOI18N
        private static final String ORACLE_WEBSERVICES =    "oracle.webservices";      // NOI18N
        private static final String ORACLE_WEBSERVICES_STANDALONE_CLIENT = 
                ORACLE_WEBSERVICES+".standalone.client";                               // NOI18N
        
        private static final String JAR = "jar";                                       // NOI18N
        
        JsxWsPoliciesSupportImpl(J2eePlatformImplImpl platformImpl){
                this.platformImpl = platformImpl;
        }
        
        public String getId(){
            return ORACLE;
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.javaee.specs.support.spi.JaxWsPoliciesSupportImplementation#getClientPolicyIds()
         */
        @Override
        public List<String> getClientPolicyIds()
        {
            // TODO : filter ids ( keep only client policies )
            return getAllPolicyIds(null);
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.javaee.specs.support.spi.JaxWsPoliciesSupportImplementation#getServicePolicyIds()
         */
        @Override
        public List<String> getServicePolicyIds()
        {
            // TODO : filter ids ( keep only services policies )
            return getAllPolicyIds( null );
        }
        
        @Override
        public Map<String, String> getPolicyDescriptions(){
            Map<String,String> map = new HashMap<String, String>();
            getAllPolicyIds( map );
            return map;
        }
        
        @Override
        public boolean supports(FileObject wsdl , Lookup loookup )
        {
            DefaultHandler handler = loookup.lookup(DefaultHandler.class);
            if ( handler instanceof OraclePolicyHandler ){
                return ((OraclePolicyHandler)handler).hasOraclePolicy;
            }
            return false;
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.javaee.specs.support.spi.JaxWsPoliciesSupportImplementation#extendsProjectClasspath(org.netbeans.modules.javaee.specs.support.spi.Project, java.util.Collection)
         */
        @Override
        public void extendsProjectClasspath( Project project,
                Collection<String> fqns )
        {
            /*
             *  TODO : the current implementation cares ONLY about limited
             *  list of FQNs. The should be changed if <code>fqns</code> 
             *  has more items than expected.
             *  Hardcoding of the selected FQNs is less expensive because only
             *  limited ( two ) jar files are required to check.
             *  Generic algorithm could be created but it will be a performance
             *  problem : one need to scan all jars in each subfolder.  
             *    
             */
            SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(
                    JavaProjectConstants.SOURCES_TYPE_JAVA);
            if (sgs == null || sgs.length < 1) {       
                return ;
            }
            
            FileObject sourceRoot = sgs[0].getRootFolder();
            List<FileObject> roots = getJarRoots(sgs);
            Map<FileObject, URL> archive2Url = new HashMap<FileObject, URL>();
            List<String> foundFqns = new LinkedList<String>(fqns );
            for( FileObject root: roots ){
                if ( foundFqns.isEmpty()){
                    break;
                }
                for(Iterator<String> iterator = foundFqns.iterator(); iterator.hasNext();){
                     if ( hasClassFile( root , iterator.next()) ){
                         iterator.remove();
                     } 
                     if (!archive2Url.containsKey(root)) {
                        try {
                            archive2Url.put( root, root.getURL());
                        } catch (FileStateInvalidException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.INFO, 
                                    "Couldn't extends compile classpath with required jars " +
                                    "for WL policy support", ex);       // NOI18N
                        }
                     }
                }
            }
            List<URL> urls = new LinkedList<URL>(archive2Url.values());
            try {
                ProjectClassPathModifier.addRoots(urls.toArray( new URL[urls.size()]), 
                        sourceRoot, ClassPath.COMPILE);
            }
            catch (IOException ex) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, 
                                    "Couldn't extends compile classpath with required jars " +
                                    "for WL policy support", ex);       // NOI18N
            }
        }
        
        @Override
        public Lookup getLookup( FileObject wsdl ){
            DefaultHandler handler = new OraclePolicyHandler();
            return Lookups.fixed( handler );
        }
        
        protected List<FileObject> getJarRoots( SourceGroup[] sgs){
            File home = platformImpl.getMiddlewareHome();
            FileObject middlewareHome = FileUtil.toFileObject( 
                    FileUtil.normalizeFile(home));
            FileObject modules = middlewareHome.getFileObject(ORACLE_COMMON_MODULES);//NOI18N 
            if ( modules == null ){
                return Collections.emptyList();
            }
            List<FileObject> roots = new LinkedList<FileObject>();
            for( FileObject child : modules.getChildren() ){
                String name = child.getName();
                if ( name.startsWith(ORACLE_WEBSERVICES)){
                    FileObject jar = child.getFileObject(
                            ORACLE_WEBSERVICES_STANDALONE_CLIENT,JAR);
                    if ( jar != null ){
                        addJar(roots, jar);
                    }
                    jar = child.getFileObject("wsclient-rt",JAR); 
                    if ( jar != null ){
                        addJar(roots, jar);
                    }
                }
                else if (name.startsWith("ws.api_") && child.getExt().equals(JAR)){       // NOI18N
                    addJar(roots , child);
                }
            }
            return roots;
        }
        
        protected void addJar(List<FileObject> archiveRoots, FileObject jar) {
            if (FileUtil.isArchiveFile(jar)) {  
                archiveRoots.add(FileUtil.getArchiveRoot(jar));
            }
        }
  
        private List<String> getAllPolicyIds( Map<String,String> descriptions){
            File home = platformImpl.getMiddlewareHome();
            FileObject middlewareHome = FileUtil.toFileObject( 
                    FileUtil.normalizeFile(home));
            FileObject modules = middlewareHome.getFileObject(ORACLE_COMMON_MODULES);//NOI18N 
            if ( modules == null ){
                return Collections.emptyList();
            }
            FileObject policiesFolder =null;
            for ( FileObject folder : modules.getChildren() ){
                if ( folder.getName().startsWith("oracle.wsm.policies")){// NOI18N 
                    policiesFolder = folder;
                    break;
                }
            }
            if ( policiesFolder == null ){
                return Collections.emptyList();
            }
            FileObject[] jars = policiesFolder.getChildren();
            FileObject policies = null;
            for (FileObject jar : jars) {
                FileObject archiveRoot = FileUtil.getArchiveRoot( jar );
                policies = archiveRoot.getFileObject(
                        "META-INF/policies/oracle/");       //      NOI18N 
                if ( policies != null ){
                    break;
                }
            }
            List<String> allIds = new LinkedList<String>();
            if ( policies != null ){
                for (FileObject fileObject : policies.getChildren()) {
                    String name = fileObject.getName();
                    allIds.add( name );
                    if ( descriptions!= null ){
                        descriptions.put( name , readFile(fileObject) );
                    }
                }
            }
            return allIds;
        }
        
        private String readFile( FileObject fileObject ){
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = null;
            try {
                InputStream stream = fileObject.getInputStream();
                reader = new BufferedReader(  
                        new InputStreamReader(stream) );
                String line;
                while( (line = reader.readLine()) != null ){
                    builder.append( line );
                    builder.append( System.getProperty("line.separator"));  // NOI18N
                } 
            }
            catch( IOException e ){
                Logger.getLogger(getClass().getName()).log(Level.INFO, 
                                    null, e);      
            }
            finally {
                if ( reader != null ){
                    try {
                        reader.close();
                    } catch (IOException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.INFO, 
                                    null, ex);   
                    }
                }
            }
            return builder.toString();
        }
        
        private boolean hasClassFile(FileObject root, String fqn) {
            String fileName = fqn.replace('.', '/');
            return root.getFileObject(fileName+ ".class")!=null;       // NOi18N
        }
        
        private J2eePlatformImplImpl platformImpl;
    }
    
    private static final class OraclePolicyHandler extends DefaultHandler {
        
        private static final String POLICY = "Policy";                  // NOI18N

        private static final String COLON_POLICY = ":"+POLICY;          // NOI18N


        /* (non-Javadoc)
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, c)
         */
        @Override
        public void startElement( String uri, String localName, String qName,
                org.xml.sax.Attributes attributes ) throws SAXException
        {
            super.startElement(uri, localName, qName, attributes);
            boolean policy = false;
            if ( localName != null && localName.equals(POLICY)){
                policy = true;
            }
            if ( qName != null && qName.endsWith(COLON_POLICY) ) {
                policy = true;
            }
            if ( !policy ){
                return;
            }
            int count = attributes.getLength();
            for (int i=0; i<count ; i++) {
                String value = attributes.getValue(i);
                if ( value.toLowerCase( Locale.ENGLISH).contains(ORACLE)){
                    hasOraclePolicy = true;
                }
            }
        }
        
        boolean hasOraclePolicy(){
            return hasOraclePolicy;
        }
        
        private boolean hasOraclePolicy;
        
    }

    private static class JpaSupportImpl implements JpaSupportImplementation {

        private final J2eePlatformImplImpl platformImpl;

        public JpaSupportImpl(J2eePlatformImplImpl platformImpl) {
            this.platformImpl = platformImpl;
        }

        @Override
        public JpaProvider getDefaultProvider() {
            String defaultProvider = platformImpl.getDefaultJpaProvider();
            boolean jpa2 = platformImpl.isJpa2Available();
            
            return JpaProviderFactory.createJpaProvider(defaultProvider, true, true, jpa2);
        }

        @Override
        public Set<JpaProvider> getProviders() {
            String defaultProvider = platformImpl.getDefaultJpaProvider();
            boolean jpa2 = platformImpl.isJpa2Available();
            Set<JpaProvider> providers = new HashSet<JpaProvider>();
            providers.add(JpaProviderFactory.createJpaProvider(OPENJPA_JPA_PROVIDER,
                    OPENJPA_JPA_PROVIDER.equals(defaultProvider), true, false));
            providers.add(JpaProviderFactory.createJpaProvider(ECLIPSELINK_JPA_PROVIDER,
                    ECLIPSELINK_JPA_PROVIDER.equals(defaultProvider), true, jpa2));
            return providers;
        }
    }
    
    private static class JPAHandler extends DefaultHandler {

        private String defaultJPAProvider;

        private String value;

        private boolean start;

        public JPAHandler() {
            super();
        }

        @Override
        public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attributes) throws SAXException {
            value = null;
            if ("default-jpa-provider".equals(qName)) { // NOI18N
                start = true;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (!start) {
                return;
            }

            if ("default-jpa-provider".equals(qName)) { // NOI18N
                defaultJPAProvider = value;
                start = false;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            value = new String(ch, start, length);
        }

        public String getDefaultJPAProvider() {
            return defaultJPAProvider;
        }
    }

    private static class PrefixesFilter implements FilenameFilter {

        private final String[] prefixes;

        public PrefixesFilter(String... prefixes) {
            this.prefixes = prefixes;
        }

        @Override
        public boolean accept(File dir, String name) {
            for (String prefix : prefixes) {
                if (name.startsWith(prefix)) {
                    return true;
                }
            }
            return false;
        }
    }

}
