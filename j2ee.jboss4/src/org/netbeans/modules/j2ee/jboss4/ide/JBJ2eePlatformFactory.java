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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginProperties;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.api.J2eePlatformFactory;
import org.netbeans.modules.j2ee.deployment.plugins.api.J2eePlatformImpl;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import javax.enterprise.deploy.spi.DeploymentManager;

//****
import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import java.io.FilenameFilter;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.modules.InstalledFileLocator;
 
/**
 *
 * @author Kirill Sorokin <Kirill.Sorokin@Sun.COM>
 */
public class JBJ2eePlatformFactory extends J2eePlatformFactory {
    
    public J2eePlatformImpl getJ2eePlatformImpl(DeploymentManager dm) {
        assert JBDeploymentManager.class.isAssignableFrom(dm.getClass()) : this + " cannot create platform for unknown deployment manager:" + dm;
        return ((JBDeploymentManager)dm).getJBPlatform();
    }
    
    public static class J2eePlatformImplImpl extends J2eePlatformImpl implements PropertyChangeListener {
        private static final String J2EE_API_DOC    = "docs/j2eeri-1_4-doc-api.zip";    // NOI18N
        private static final Set MODULE_TYPES = new HashSet();
        static {
            MODULE_TYPES.add(J2eeModule.EAR);
            MODULE_TYPES.add(J2eeModule.WAR);
            MODULE_TYPES.add(J2eeModule.EJB);
            MODULE_TYPES.add(J2eeModule.CONN);
            MODULE_TYPES.add(J2eeModule.CLIENT);
        }

        private static final Set SPEC_VERSIONS = new HashSet();
        static {
            SPEC_VERSIONS.add(J2eeModule.J2EE_14);
        }

        private String platformRoot;

        private LibraryImplementation[] libraries = null;

        private JBDeploymentManager dm;

        public J2eePlatformImplImpl(JBDeploymentManager dm) {
            this.dm = dm;
        }

        private String getPlatformRoot() {
            if (platformRoot == null)
                platformRoot = InstanceProperties.getInstanceProperties(dm.getUrl()).
                                        getProperty(JBPluginProperties.PROPERTY_ROOT_DIR);
            return platformRoot;
        }
        
        public Set getSupportedSpecVersions() {
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
            return null;
        }

        public File[] getPlatformRoots() {
            return new File[]{new File(getPlatformRoot())};
        }

        private void initLibraries() {
            InstanceProperties ip = InstanceProperties.getInstanceProperties(dm.getUrl());
            try {
                LibraryImplementation libs[] = new LibraryImplementation[2];

                J2eeLibraryTypeProvider libraryProvider = new J2eeLibraryTypeProvider();

                LibraryImplementation library = libraryProvider.createLibrary();
                library.setName(NbBundle.getMessage(JBJ2eePlatformFactory.class, "TITLE_JBOSS_LIBRARY")); //NOI18N

                List list = new ArrayList();
                list.add(fileToUrl(new File(getPlatformRoot(), "client/jboss-j2ee.jar")));  //NOI18N
                library.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH, list);

                File j2eeDoc = InstalledFileLocator.getDefault().locate(J2EE_API_DOC, null, false);
                if (j2eeDoc != null) {
                    list = new ArrayList();
                    list.add(fileToUrl(j2eeDoc));
                    library.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_JAVADOC, list);
                }
                libs[0] = library;

                list = new ArrayList();
                library = libraryProvider.createLibrary();
                addFiles(new File (getPlatformRoot(), "lib"), list); //NOI18N

                String domain = null;
                domain = ip.getProperty("server"); //NOI18N
                if (domain != null) {
                    addFiles(new File (getPlatformRoot(), "server/" + domain + "/lib"), list); //NOI18N
                }
                library.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH, list);
                if (j2eeDoc != null) {
                    list = new ArrayList();
                    list.add(fileToUrl(j2eeDoc));
                    library.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_JAVADOC, list);
                }
                libs[1] = library;

                libraries = libs;
            } catch (MalformedURLException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            ip.addPropertyChangeListener(this);
        }


        private void addFiles (File folder, List l) {
            File files [] = folder.listFiles(new FF ());
            if (files == null)
                return;
            for (int i = 0; i < files.length; i++) {
                if (files [i].isDirectory()) {
                    addFiles (files [i], l);
                } else {
                    try {
                        l.add (fileToUrl(files [i]));
                    } catch (MalformedURLException e) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                   }
                }
           }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if ("server".equals(evt.getPropertyName())) {
                LibraryImplementation old [] = libraries;
                libraries = null;
                initLibraries();
                firePropertyChange(PROP_LIBRARIES, old, libraries);
            }
        }
        
        private static class FF implements FilenameFilter {
            public boolean accept (File dir, String name) {
                return name.endsWith(".jar") || new File (dir, name).isDirectory(); //NOI18N
            }
        }

        public LibraryImplementation[] getLibraries() {
            if (libraries == null) {
                initLibraries();
            }

            return libraries;
        }

        public java.awt.Image getIcon() {
            return null;
            //return Utilities.loadImage("org/netbeans/modules/j2ee/genericserver/resources/GSInstanceIcon.gif");
        }

        public String getDisplayName() {
            return NbBundle.getMessage(JBJ2eePlatformFactory.class, "TITLE_JBOSS_FACTORY"); //NOI18N

        }

        public boolean isToolSupported(String toolName) {
            if ("wscompile".equals(toolName)) {
                return true;
            }
            return false;
        }

        public File[] getToolClasspathEntries(String toolName) {
            if ("wscompile".equals(toolName)) {
                File root = InstalledFileLocator.getDefault().locate("modules/ext/jaxrpc16", null, false);
                return new File[] {
                    new File(root, "saaj-api.jar"),
                    new File(root, "saaj-impl.jar"),
                    new File(root, "jaxrpc-api.jar"),
                    new File(root, "jaxrpc-impl.jar"),
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
    }
}