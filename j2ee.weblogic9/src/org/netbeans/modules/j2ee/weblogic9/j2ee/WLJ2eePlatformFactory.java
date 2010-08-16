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
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule.Type;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency;
import org.netbeans.modules.j2ee.deployment.plugins.spi.support.LookupProviderSupport;
import org.openide.modules.InstalledFileLocator;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibrary;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.modules.j2ee.weblogic9.config.WLServerLibrarySupport;
import org.netbeans.modules.j2ee.weblogic9.config.WLServerLibrarySupport.WLServerLibrary;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
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

    private static final Logger LOGGER = Logger.getLogger(WLJ2eePlatformFactory.class.getName());

    private static final String OPENJPA_JPA_PROVIDER = "org.apache.openjpa.persistence.PersistenceProviderImpl"; // NOI18N

    private static final String ECLIPSELINK_JPA_PROVIDER = "org.eclipse.persistence.jpa.PersistenceProvider"; // NOI18N

    // always prefer JPA 1.0 see #189205
    private static final Pattern JAVAX_PERSISTENCE_PATTERN = Pattern.compile("^javax\\.persistence.*_1-\\d+-\\d+\\.jar$");

    @Override
    public J2eePlatformImpl getJ2eePlatformImpl(DeploymentManager dm) {
        assert WLDeploymentManager.class.isAssignableFrom(dm.getClass()) : this + " cannot create platform for unknown deployment manager:" + dm;
        return new J2eePlatformImplImpl((WLDeploymentManager)dm);
    }
    
    private static class J2eePlatformImplImpl extends J2eePlatformImpl {

        /**
         * The platform icon's URL
         */
        private static final String ICON = "org/netbeans/modules/j2ee/weblogic9/resources/16x16.gif"; // NOI18N

        private static final String J2EE_API_DOC    = "docs/javaee6-doc-api.zip";    // NOI18N

        private static final Set<Type> MODULE_TYPES = new HashSet<Type>();

        static {
            MODULE_TYPES.add(J2eeModule.Type.EAR);
            MODULE_TYPES.add(J2eeModule.Type.WAR);
            MODULE_TYPES.add(J2eeModule.Type.EJB);
        }

        private final Set<Profile> profiles = new HashSet<Profile>();

        private final WLDeploymentManager dm;

        private final ChangeListener domainChangeListener;

        private String platformRoot;
        
        private LibraryImplementation[] libraries = null;

        /** <i>GuardedBy("this")</i> */
        private String defaultJpaProvider;
        
        public J2eePlatformImplImpl(WLDeploymentManager dm) {
            this.dm = dm;
            
            // Allow J2EE 1.4 Projects
            profiles.add(Profile.J2EE_14);
            
            // Check for WebLogic Server 10x to allow Java EE 5 Projects
            String version = WLPluginProperties.getWeblogicDomainVersion(dm.getInstanceProperties().getProperty(WLPluginProperties.DOMAIN_ROOT_ATTR));
            
            if (version != null && version.contains("10")) { // NOI18N
                profiles.add(Profile.JAVA_EE_5);
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

            // shortcut
            if (!"openJpaPersistenceProviderIsDefault".equals(toolName) // NOI18N
                    && !"eclipseLinkPersistenceProviderIsDefault".equals(toolName) // NOI18N
                    && !OPENJPA_JPA_PROVIDER.equals(toolName)
                    && !ECLIPSELINK_JPA_PROVIDER.equals(toolName)) {
                return false;
            }

            // JPA provider part
            String currentDefaultJpaProvider = getDefaultJpaProvider();
            if ("openJpaPersistenceProviderIsDefault".equals(toolName)) { // NOI18N
                return currentDefaultJpaProvider.equals(OPENJPA_JPA_PROVIDER);
            }
            if ("eclipseLinkPersistenceProviderIsDefault".equals(toolName)) { // NOI18N
                return currentDefaultJpaProvider.equals(ECLIPSELINK_JPA_PROVIDER);
            }

            // TODO is both providers supported even when the other one is configured
            if (OPENJPA_JPA_PROVIDER.equals(toolName)) {
                return currentDefaultJpaProvider.equals(OPENJPA_JPA_PROVIDER);
            }
            if (ECLIPSELINK_JPA_PROVIDER.equals(toolName)) {
                return currentDefaultJpaProvider.equals(ECLIPSELINK_JPA_PROVIDER);
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
            return profiles;
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
            File server = new File(getPlatformRoot());
            File domain = new File(dm.getInstanceProperties().getProperty(
                    WLPluginProperties.DOMAIN_ROOT_ATTR));

            assert server.isAbsolute();
            assert domain.isAbsolute();
            
            return new File[] {server, domain};
        }
        
        public LibraryImplementation[] getLibraries() {
            if (libraries == null) {
                initLibraries();
            }
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

            List<LibraryImplementation> serverImpl = new ArrayList<LibraryImplementation>(Arrays.asList(getLibraries()));
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

            return serverImpl.toArray(new LibraryImplementation[serverImpl.size()]);
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
                List<URL> list = new ArrayList<URL>();
                list.add(fileToUrl(new File(getPlatformRoot(), "server/lib/weblogic.jar")));    // NOI18N
                File apiFile = new File(getPlatformRoot(), "server/lib/api.jar"); // NOI18N
                if (apiFile.exists()) {
                    list.add(fileToUrl(apiFile));
                    list.addAll(getJarClassPath(apiFile));
                }

                addPersistenceLibrary(list);

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

        //XXX there seems to be a bug in api.jar - it does not contain link to javax.persistence
        private void addPersistenceLibrary(List<URL> list) throws MalformedURLException {
            File platformRootFile = new File(getPlatformRoot());
            File middleware = null;
            String mwHome = dm.getProductProperties().getMiddlewareHome();
            if (mwHome != null) {
                middleware = new File(mwHome);
            }
            if (middleware == null || !middleware.exists() || !middleware.isDirectory()) {
                middleware = platformRootFile.getParentFile();
            }

            // make guess :(
            if (middleware != null && middleware.exists() && middleware.isDirectory()) {
                File modules = new File(middleware, "modules"); // NOI18N
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
                        FileObject baseDir = null;
                        File serverLib = WLPluginProperties.getServerLibDirectory(dm, false);
                        if (serverLib != null) {
                            baseDir = FileUtil.toFileObject(FileUtil.normalizeFile(serverLib));
                        }

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
            if (platformRoot == null) {
                platformRoot = dm.getInstanceProperties().getProperty(WLPluginProperties.SERVER_ROOT_ATTR);
            }
            return platformRoot;
        }

        @Override
        public Lookup getLookup() {
            Lookup baseLookup = Lookups.fixed(new File(getPlatformRoot()));
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

}
