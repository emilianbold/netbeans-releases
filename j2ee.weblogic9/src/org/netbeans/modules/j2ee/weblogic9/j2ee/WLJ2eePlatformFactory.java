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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.weblogic9.j2ee;

import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.Image;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.deploy.spi.*;

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.*;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.spi.project.libraries.*;
import org.netbeans.modules.j2ee.deployment.common.api.*;
import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;

import org.netbeans.modules.j2ee.weblogic9.*;

/**
 * A sub-class of the J2eePlatformFactory that is set up to return the 
 * plugin-specific J2eePlatform.
 * 
 * @author Kirill Sorokin
 */
public class WLJ2eePlatformFactory extends J2eePlatformFactory {
    
    public J2eePlatformImpl getJ2eePlatformImpl(DeploymentManager dm) {
        assert WLDeploymentManager.class.isAssignableFrom(dm.getClass()) : this + " cannot create platform for unknown deployment manager:" + dm;
        return new J2eePlatformImplImpl((WLDeploymentManager)dm);
    }
    
    private static class J2eePlatformImplImpl extends J2eePlatformImpl {
        
        private static final String J2EE_API_DOC    = "docs/javaee5-doc-api.zip";    // NOI18N
        private static final Set MODULE_TYPES = new HashSet();
        static {
            MODULE_TYPES.add(J2eeModule.EAR);
            MODULE_TYPES.add(J2eeModule.WAR);
            MODULE_TYPES.add(J2eeModule.EJB);
//            MODULE_TYPES.add(J2eeModule.CONN);
//            MODULE_TYPES.add(J2eeModule.CLIENT);
        }

        private final Set SPEC_VERSIONS = new HashSet();
        
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
        WLDeploymentManager dm;
        
        public J2eePlatformImplImpl(WLDeploymentManager dm) {
            this.dm = dm;
            
            // Allow J2EE 1.4 Projects
            SPEC_VERSIONS.add(J2eeModule.J2EE_14);
            
            // Check for WebLogic Server 10x to allow Java EE 5 Projects
            String version = WLPluginProperties.getWeblogicDomainVersion(dm.getInstanceProperties().getProperty(WLPluginProperties.DOMAIN_ROOT_ATTR));
            
            if (version != null && version.contains("10"))
                SPEC_VERSIONS.add(J2eeModule.JAVA_EE_5);
     
        }
        
        public boolean isToolSupported(String toolName) {
            if (J2eePlatform.TOOL_WSGEN.equals(toolName) || J2eePlatform.TOOL_WSIMPORT.equals(toolName)) {
                return true;
            }
            if (J2eePlatform.TOOL_JSR109.equals(toolName)) {
                return false; // to explicitelly emphasise that JSR 109 is not supported
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
        
        public Set getSupportedSpecVersions() {
            return SPEC_VERSIONS;
        }
        
        public java.util.Set getSupportedModuleTypes() {
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
                list.add(fileToUrl(new File(getPlatformRoot(), "server/lib/api.jar")));         // NOI18N
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
            return Utilities.loadImage(ICON);
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

        private String getPlatformRoot() {
            if (platformRoot == null)
                platformRoot = dm.getInstanceProperties().getProperty(WLPluginProperties.SERVER_ROOT_ATTR);
            
            return platformRoot;
        }
    }
    
}
