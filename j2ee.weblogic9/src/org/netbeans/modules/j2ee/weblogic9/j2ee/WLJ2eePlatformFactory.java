/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.weblogic9.j2ee;

import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.Image;

import javax.enterprise.deploy.spi.*;

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.util.*;
import org.netbeans.spi.project.libraries.*;
import org.netbeans.modules.j2ee.deployment.common.api.*;
import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;

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
        return new J2eePlatformImplImpl(dm);
    }
    
    private static class J2eePlatformImplImpl extends J2eePlatformImpl {
        
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
        
        public J2eePlatformImplImpl(DeploymentManager dm) {
            this.dm = (WLDeploymentManager) dm;
            platformRoot = this.dm.getInstanceProperties().getProperty(WLDeploymentFactory.SERVER_ROOT_ATTR);
        }
        
        public boolean isToolSupported(String toolName) {
            return false;
        }
        
        public File[] getToolClasspathEntries(String toolName) {
            return new File[0];
        }
        
        public Set getSupportedSpecVersions() {
            return SPEC_VERSIONS;
        }
        
        public java.util.Set getSupportedModuleTypes() {
            return MODULE_TYPES;
        }
        
        public java.io.File[] getPlatformRoots() {
            return new File[]{new File(platformRoot)};
        }
        
        public LibraryImplementation[] getLibraries() {
            if (libraries == null) {
                initLibraries();
            }
            return libraries;
        }
        
        private void initLibraries() {
            // init the resulting array
            libraries = new LibraryImplementation[1];
            
            // create a new library
            LibraryImplementation library = new J2eeLibraryTypeProvider().
                    createLibrary();
            
            // set its name
            library.setName(NbBundle.getMessage(WLJ2eePlatformFactory.class, 
                    "LIBRARY_NAME"));                                  // NOI18N
            
            // add the required jars to the library
            try {
                List list = new ArrayList();
                list.add(fileToUrl(new File(platformRoot, "server/lib/weblogic.jar"))); // NOI18N
                
                library.setContent(J2eeLibraryTypeProvider.
                        VOLUME_TYPE_CLASSPATH, list);
            } catch (MalformedURLException e) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
            }
            
            // add the created library to the array
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
    }
    
}
