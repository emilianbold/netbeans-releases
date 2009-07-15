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
package org.netbeans.modules.j2ee.websphere6.j2ee;

import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.api.java.platform.JavaPlatform;
import org.openide.*;
import org.openide.filesystems.*;
import org.openide.util.*;
import org.netbeans.spi.project.libraries.*;
import org.netbeans.modules.j2ee.deployment.common.api.*;
import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;
import org.netbeans.modules.j2ee.websphere6.*;
// Dileep - Start compile fix
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;
// Dileep - Start compile fix
import org.netbeans.modules.xml.multiview.Utils;
import org.openide.modules.InstalledFileLocator;

/**
 * A sub-class of the J2eePlatformFactory that is set up to return the
 * plugin-specific J2eePlatform.
 *
 * @author Kirill Sorokin
 */
public class WSJ2eePlatformFactory extends J2eePlatformFactory {
    
    /**
     * Factory method for WSJ2eePlatformImpl. This method is used for
     * constructing the plugin-specific J2eePlatform object.
     *
     * @param dm the server specific deployment manager that can be used as an
     * additional source of information
     */
    public J2eePlatformImpl getJ2eePlatformImpl(DeploymentManager dm) {
        return new J2eePlatformImplImpl(dm);
    }
    
    /**
     * The plugin implementation of the J2eePlatform interface. It is used to
     * provide all kinds of information about the environment that the deployed
     * application will run against, such as the set of .jsr files representing
     * the j2ee implementation, which kinds of application the server may
     * contain, which j2ee specification version the server supports, etc.
     */
    private static class J2eePlatformImplImpl extends J2eePlatformImpl {
        
        /**
         * The platform icon's URL
         */
        private static final String ICON = "org/netbeans/modules/" +   // NOI18N
                "j2ee/websphere6/resources/16x16.gif";                 // NOI18N
        
        /**
         * The server's deployment manager, to be exact the plugin's wrapper for
         * it
         */
        WSDeploymentManager dm;
        
        /**
         * Creates a new instance of J2eePlatformImplImpl.
         *
         * @param dm the server's deployment manager
         */
        public J2eePlatformImplImpl(DeploymentManager dm) {
            // save the prarmeters
            this.dm = (WSDeploymentManager) dm;
        }
        
        /**
         * Defines whether the platform supports the named tool. Since it's
         * unclear what actually a 'tool' is, currently it returns false.
         *
         * @param toolName tool name
         *
         * @return false
         */
        public boolean isToolSupported(String toolName) {
            return false;
        }
        
        /**
         * Gets the classpath entries for the named tool. Since it's
         * unclear what actually a 'tool' is, currently it returns an empty
         * array.
         *
         * @param toolName tool name
         *
         * @return an empty array of File
         */
        public File[] getToolClasspathEntries(String toolName) {
            return new File[0];
        }
        
        /**
         * Specifies which versions of j2ee the server supports.
         *
         * @return a Set with the supported versions
         */
        public Set getSupportedSpecVersions() {
            // init the set
            Set result = new HashSet();
            
            result.add(J2eeModule.J2EE_13);
            result.add(J2eeModule.J2EE_14);
            
            // return
            return result;
        }
        
        
        public Set getSupportedJavaPlatformVersions() {
            Set versions = new HashSet();
            versions.add("1.4"); // NOI18N
            versions.add("1.5"); // NOI18N
            return versions;
            
        }
        
        /**
         * Specifies which module types the server supports.
         *
         * @return a Set the the supported module types
         */
        public Set getSupportedModuleTypes() {
            // init the set
            Set result = new HashSet();
            
            // add supported modules
            result.add(J2eeModule.EAR);
            result.add(J2eeModule.WAR);
            result.add(J2eeModule.EJB);
            result.add(J2eeModule.CONN);
            result.add(J2eeModule.CLIENT);
            
            // return
            return result;
        }
        
        /**
         * Specifies the platform root directories. It's unclear where and why
         * it is used, for now returning the server home directory.
         *
         * @return an array of files with a single entry - the server home
         *      directory
         */
        public java.io.File[] getPlatformRoots() {
            return new File[] {
                new File(dm.getInstanceProperties().getProperty(
                        WSDeploymentFactory.SERVER_ROOT_ATTR))
            };
        }
        
        /**
         * Gets the libraries that will be attached to the project for
         * compilation. A library includes a set of jar files, sources and
         * javadocs. As there may be multiple jars per library we create only
         * one.
         *
         * @return an array of libraries
         */
        public LibraryImplementation[] getLibraries() {
            // TODO cache this
            // init the resulting array
            LibraryImplementation[] libraries = new LibraryImplementation[1];
            
            // create a new library
            LibraryImplementation library = new J2eeLibraryTypeProvider().
                    createLibrary();
            
            // set its name
            library.setName(NbBundle.getMessage(WSJ2eePlatformFactory.class,
                    "TXT_libraryName"));                               // NOI18N
            
            // add the required jars to the library
            try {
                ArrayList list = new ArrayList();
                list.add(fileToUrl(new File(dm.getInstanceProperties().
                        getProperty(WSDeploymentFactory.SERVER_ROOT_ATTR),
                        "/lib/j2ee.jar")));                            // NOI18N
                
                library.setContent(J2eeLibraryTypeProvider.
                        VOLUME_TYPE_CLASSPATH, list);
                
                File j2eeDoc = InstalledFileLocator.getDefault().locate("docs/javaee6-doc-api.zip", null, false); // NOI18N
                if (j2eeDoc != null) {
                    list = new ArrayList();
                    list.add(fileToUrl(j2eeDoc));
                    library.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_JAVADOC, list);
                }                
            } catch (MalformedURLException e) {
                Logger.getLogger("global").log(Level.WARNING, null, e);
            }

            // add the created library to the array
            libraries[0] = library;
            
            // return
            return libraries;
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
            return NbBundle.getMessage(WSJ2eePlatformFactory.class,
                    "TXT_platformName");                               // NOI18N
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
            // get the file's absolute URL
            URL url = file.toURI().toURL();
            
            // strip the jar's path and remain with the system resources URI
            if (FileUtil.isArchiveFile(url)) {
                url = FileUtil.getArchiveRoot(url);
            }
            
            // return
            return url;
        }
        
        
        /**
         * Implements J2eePlatformImpl
         *
         */
        
        public JavaPlatform getJavaPlatform() {
            /* TO DO
            String currentJvm = ip.getProperty(PROP_JAVA_PLATFORM);
            JavaPlatformManager jpm = JavaPlatformManager.getDefault();
            JavaPlatform[] installedPlatforms = jpm.getPlatforms(null, new Specification("J2SE", null)); // NOI18N
            for (int i = 0; i < installedPlatforms.length; i++) {
                String platformName = (String)installedPlatforms[i].getProperties().get(PLAT_PROP_ANT_NAME);
                if (platformName != null && platformName.equals(currentJvm)) {
                    return installedPlatforms[i];
                }
            }
            // return default platform if none was set
            return jpm.getDefaultPlatform();
             */
            return null;
        }
    }
}