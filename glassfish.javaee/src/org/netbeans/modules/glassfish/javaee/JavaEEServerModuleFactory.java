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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.javaee;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.GlassfishModuleFactory;
import org.netbeans.modules.glassfish.spi.RegisteredDerbyServer;
import org.netbeans.modules.glassfish.spi.ServerUtilities;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Peter Williams
 */
public class JavaEEServerModuleFactory implements GlassfishModuleFactory {

    private static JavaEEServerModuleFactory singleton = new JavaEEServerModuleFactory();
    
    private JavaEEServerModuleFactory() {
    }
    
    public static GlassfishModuleFactory getDefault() {
        return singleton;
    }
    
    public boolean isModuleSupported(String glassfishHome, Properties asenvProps) {

        // Do some moderate sanity checking to see if this v3 build looks ok.
        File jar = ServerUtilities.getJarName(glassfishHome, ServerUtilities.GFV3_JAR_MATCHER);


        if (jar==null) {
            return false;
        }
        if (jar.exists()) {
            return true;
        }

        return false;
    }

    public Object createModule(Lookup instanceLookup) {
        // When creating JavaEE support, also ensure this instance is added to j2eeserver
        InstanceProperties ip = null;
        GlassfishModule commonModule = instanceLookup.lookup(GlassfishModule.class);
        if(commonModule != null) {
            Map<String, String> props = commonModule.getInstanceProperties();
            String url = props.get(InstanceProperties.URL_ATTR);
            ip = InstanceProperties.getInstanceProperties(url);
            if(ip == null) {
                String username = props.get(InstanceProperties.USERNAME_ATTR);
                String password = props.get(InstanceProperties.PASSWORD_ATTR);
                String displayName = props.get(InstanceProperties.DISPLAY_NAME_ATTR);
                int fail = 0;
                while (null == ip && fail < 20) {
                    try {
                        ip = InstanceProperties.createInstancePropertiesWithoutUI(
                                url, username, password, displayName, props);
                    } catch (InstanceCreationException ex) {
                        fail++;
                        if (fail >= 20) {
                            Logger.getLogger("glassfish-javaee").log(Level.WARNING, null, ex); // NOI18N
                        }
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ie) {
                            Logger.getLogger("glassfish-javaee").log(Level.INFO, null, ie); // NOI18N
                        }
                    }
                }

                if(ip == null) {
                    Logger.getLogger("glassfish-javaee").log(Level.INFO,
                            "Unable to create/locate J2EE InstanceProperties for " + url);
                }
            }

            final String glassfishRoot = commonModule.getInstanceProperties().get(
                    GlassfishModule.GLASSFISH_FOLDER_ATTR);
            final String installRoot = commonModule.getInstanceProperties().get(
                    GlassfishModule.INSTALL_FOLDER_ATTR);
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    ensureEclipseLinkSupport(glassfishRoot);
                    ensureCometSupport(glassfishRoot);
                    ensureRestLibSupport(glassfishRoot);
                    // lookup the javadb register service here and use it.
                    RegisteredDerbyServer db = Lookup.getDefault().lookup(RegisteredDerbyServer.class);
                    if (null != db) {
                        File ir = new File(installRoot);
                        File f = new File(ir,"javadb");
                        if (f.exists() && f.isDirectory() && f.canRead()) {
                            db.initialize(f.getAbsolutePath());
                        }
                    }
                }
            });
        } else {
            Logger.getLogger("glassfish-javaee").log(Level.WARNING, "commonModule is NULL");
        }

        return (ip != null) ? new JavaEEServerModule(instanceLookup, ip) : null;
    }
    
    private static final String CLASS_LIBRARY_TYPE = "j2se"; // NOI18N
    private static final String CLASSPATH_VOLUME = "classpath"; // NOI18N
    private static final String SOURCE_VOLUME = "src"; // NOI18N
    private static final String JAVADOC_VOLUME = "javadoc"; // NOI18N
    
    private static final String ECLIPSE_LINK_LIB = "EclipseLink-GlassFish-v3-Prelude"; // NOI18N
    private static final String ECLIPSE_LINK_LIB_2 = "EclipseLink-GlassFish-v3"; // NOI18N
    private static final String EL_CORE_JAR_MATCHER = "eclipselink-wrapper" + ServerUtilities.GFV3_VERSION_MATCHER; // NOI18N

    private static final String PERSISTENCE_API_JAR_MATCHER_1 = "javax.javaee" + ServerUtilities.GFV3_VERSION_MATCHER; // NOI18N
    private static final String PERSISTENCE_API_JAR_MATCHER_2 = "javax.persistence" + ServerUtilities.GFV3_VERSION_MATCHER; // NOI18N
        
    private static final String PERSISTENCE_JAVADOC = "javaee6-doc-api.zip"; // NOI18N
    
    private static synchronized boolean ensureEclipseLinkSupport(String installRoot) {
        List<URL> libraryList = new ArrayList<URL>();
        List<URL> docList = new ArrayList<URL>();
        try {
            File f = ServerUtilities.getJarName(installRoot, EL_CORE_JAR_MATCHER);
            if (f != null && f.exists()) {
                libraryList.add(ServerUtilities.fileToUrl(f));
            } else {// we are in the final V3 Prelude jar name structure
                // find the org.eclipse.persistence*.jar files and add them
                for (File candidate : new File(installRoot, "modules").listFiles()) {// NOI18N
                    if (candidate.getName().indexOf("org.eclipse.persistence") != -1) {// NOI18N
                        libraryList.add(ServerUtilities.fileToUrl(candidate));
                    }
                }
            }
            f = ServerUtilities.getJarName(installRoot, PERSISTENCE_API_JAR_MATCHER_1);
            if (f != null && f.exists()) {
                libraryList.add(ServerUtilities.fileToUrl(f));
            } else {
                f = ServerUtilities.getJarName(installRoot, PERSISTENCE_API_JAR_MATCHER_2);
                if (f != null && f.exists()) {
                    libraryList.add(ServerUtilities.fileToUrl(f));
                }
            }

            File j2eeDoc = InstalledFileLocator.getDefault().locate(
                    "docs/" + PERSISTENCE_JAVADOC, null, false); // NOI18N
            if (j2eeDoc != null) {
                docList.add(ServerUtilities.fileToUrl(j2eeDoc));
            } else {
                Logger.getLogger("glassfish-javaee").log(Level.WARNING, "Warning: Java EE documentation not found when registering EclipseLink library.");
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger("glassfish-javaee").log(Level.WARNING, ex.getLocalizedMessage(), ex);
            return false;
        }
        String name = ECLIPSE_LINK_LIB;
        File f = ServerUtilities.getJarName(installRoot, "gmbal" + ServerUtilities.GFV3_VERSION_MATCHER);
        if (f != null && f.exists()) {
            name = ECLIPSE_LINK_LIB_2;
        }
        return addLibrary(name, libraryList, docList);
    }
    private static final String COMET_LIB = "Comet-GlassFish-v3-Prelude"; // NOI18N
    private static final String COMET_LIB_2 = "Comet-GlassFish-v3"; // NOI18N
    private static final String COMET_JAR_MATCHER = "grizzly-module" + ServerUtilities.GFV3_VERSION_MATCHER; // NOI18N
    private static final String COMET_JAR_2_MATCHER = "grizzly-comet" + ServerUtilities.GFV3_VERSION_MATCHER; // NOI18N
    private static final String GRIZZLY_OPTIONAL_JAR_MATCHER = "grizzly-optional" + ServerUtilities.GFV3_VERSION_MATCHER; // NOI18N

    private static synchronized boolean ensureCometSupport(String installRoot) {
        List<URL> libraryList = new ArrayList<URL>();
        String name = COMET_LIB;
        File f = ServerUtilities.getJarName(installRoot, GRIZZLY_OPTIONAL_JAR_MATCHER);
        if (f == null || !f.exists()) {
            f = ServerUtilities.getJarName(installRoot, COMET_JAR_MATCHER);
        }
        if (f == null || !f.exists()) {
            name = COMET_LIB_2;
            f = ServerUtilities.getJarName(installRoot, COMET_JAR_2_MATCHER);
        }
        if (f != null && f.exists()) {
            try {
                libraryList.add(ServerUtilities.fileToUrl(f));
            } catch (MalformedURLException ex) {
                Logger.getLogger("glassfish-javaee").log(Level.WARNING, ex.getLocalizedMessage(), ex);
                return false;
            }
        }

        return addLibrary(name, libraryList, null);
    }

    private static final String[] JAXRS_LIBRARIES =
             {"asm-all-repackaged", "jackson-asl", "jersey-bundle", "jersey-gf-bundle", "jersey-multipart", "jettison", "jsr311-api"}; //NOI18N
    private static final String PRELUDE_RESTLIB = "restlib_gfv3"; // NOI18N
    private static final String V3_RESTLIB = "restlib_gfv3ee6"; // NOI18N

    private static synchronized boolean ensureRestLibSupport(String installRoot) {
        List<URL> libraryList = new ArrayList<URL>();
        String name = PRELUDE_RESTLIB;
        for (String entry : JAXRS_LIBRARIES) {
            File f = ServerUtilities.getJarName(installRoot, entry + ServerUtilities.GFV3_VERSION_MATCHER);
            if ((f != null) && (f.exists())) {
                try {
                    libraryList.add(f.toURI().toURL());
                } catch (MalformedURLException ex) {
                }
            }
        }

        File f = ServerUtilities.getJarName(installRoot, "gmbal" + ServerUtilities.GFV3_VERSION_MATCHER);
        if (f != null && f.exists()) {
            name = V3_RESTLIB;
        }
        return addLibrary(name, libraryList, null);
    }

    private static synchronized boolean addLibrary(String name, List<URL> libraryList, List<URL> docList) {
        LibraryManager lmgr = LibraryManager.getDefault();

        Library lib = lmgr.getLibrary(name);

        // Verify that existing library is still valid.
        if (lib != null) {
            List<URL> libList = lib.getContent(CLASSPATH_VOLUME);
            for (URL libUrl : libList) {
                String libPath = libUrl.getFile();
                if (!new File(libPath).exists()) {
                    Logger.getLogger("glassfish-javaee").log(Level.FINE,
                            "libPath does not exist.  Updating " + name);
                    try {
                        lmgr.removeLibrary(lib);
                    } catch (IOException ex) {
                        Logger.getLogger("glassfish-javaee").log(Level.WARNING, ex.getLocalizedMessage(), ex);
                    } catch (IllegalArgumentException ex) {
                        // Already removed somehow, ignore.
                    }
                    lib = null;
                    break;
                }
            }
        }

        if (lib == null) {
            Map<String, List<URL>> contents;
            try {
                contents = new HashMap<String, List<URL>>();
                if (null != libraryList) {
                    contents.put(CLASSPATH_VOLUME, libraryList);
                }
                if (null != docList) {
                    contents.put(JAVADOC_VOLUME, docList);
                }

                LibraryTypeProvider ltp = LibrariesSupport.getLibraryTypeProvider(CLASS_LIBRARY_TYPE);
                if (null != ltp) {
                    lib = lmgr.createLibrary(CLASS_LIBRARY_TYPE, name, contents);
                    Logger.getLogger("glassfish-javaee").log(Level.FINE, "Created library " + name);
                } else {
                    lmgr.addPropertyChangeListener(new InitializeLibrary(lmgr, name, contents));
                    Logger.getLogger("glassfish-javaee").log(Level.FINE, "schedule to create library " + name);
                }
            } catch (IOException ex) {
                // Someone must have created the library in a parallel thread, try again otherwise fail.
                lib = lmgr.getLibrary(name);
                if (lib == null) {
                    Logger.getLogger("glassfish-javaee").log(Level.WARNING, ex.getLocalizedMessage(), ex);
                }
            } catch (IllegalArgumentException ex) {
                // Someone must have created the library in a parallel thread, try again otherwise fail.
                lib = lmgr.getLibrary(name);
                if (lib == null) {
                    Logger.getLogger("glassfish-javaee").log(Level.WARNING, ex.getLocalizedMessage(), ex);
                }
            }
        }
        return lib != null;
    }

    static class InitializeLibrary implements PropertyChangeListener {

        final private LibraryManager lmgr;
        private String name;
        private Map<String, List<URL>> content;

        InitializeLibrary(LibraryManager lmgr, String name, Map<String, List<URL>> content) {
            this.lmgr = lmgr;
            this.name = name;
            this.content = content;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (null != name) {
                Library l = lmgr.getLibrary(name);
                final PropertyChangeListener pcl = this;
                if (null == l) {
                    try {
                        LibraryTypeProvider ltp = LibrariesSupport.getLibraryTypeProvider(CLASS_LIBRARY_TYPE);
                        if (null != ltp) {
                            lmgr.createLibrary(CLASS_LIBRARY_TYPE, name, content);
                            Logger.getLogger("glassfish-javaee").log(Level.FINE, "Created library " + name);
                            removeFromListenerList(pcl);
                        }
                    } catch (IOException ex) {
                        Logger.getLogger("glassfish-javaee").log(Level.WARNING, ex.getLocalizedMessage(), ex);
                    } catch (IllegalArgumentException iae) {
                        Logger.getLogger("glassfish-javaee").log(Level.WARNING, iae.getLocalizedMessage(), iae);
                    }
                } else {
                    // The library is there... and the listener is still active... hmmm.
                    removeFromListenerList(pcl);
                }
            }
        }

        private void removeFromListenerList(final PropertyChangeListener pcl) {
            RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    if (null != lmgr) {
                        lmgr.removePropertyChangeListener(pcl);
                        content = null;
                        name = null;
                    }
                }
            });
        }
    }
}
