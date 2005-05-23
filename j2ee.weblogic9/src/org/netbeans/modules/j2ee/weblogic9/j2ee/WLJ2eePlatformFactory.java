/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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
                "j2ee/weblogic9/resources/16x16.gif";                  // NOI18N
        
        /**
         * The server's deployment manager, to be exact the plugin's wrapper for
         * it
         */
        WLDeploymentManager dm;
        
        /**
         * Creates a new instance of J2eePlatformImplImpl.
         * 
         * @param dm the server's deployment manager
         */
        public J2eePlatformImplImpl(DeploymentManager dm) {
            this.dm = (WLDeploymentManager) dm;
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
            
            // add j2ee 1.4
            result.add(J2eeModule.J2EE_14);
            
            // return
            return result;
        }
        
        /** 
         * Specifies which module types the server supports.
         * 
         * @return a Set the the supported module types
         */
        public java.util.Set getSupportedModuleTypes() {
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
            return new File[]{
                    new File(dm.getInstanceProperties().getProperty(
                            WLDeploymentFactory.SERVER_ROOT_ATTR))
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
            // init the resulting array
            LibraryImplementation[] libraries = new LibraryImplementation[1];
            
            // create a new library
            LibraryImplementation library = new J2eeLibraryTypeProvider().
                    createLibrary();
            
            // set its name
            library.setName(NbBundle.getMessage(WLJ2eePlatformFactory.class, 
                    "LIBRARY_NAME"));                                  // NOI18N
            
            // add the required jars to the library
            try {
                List list = new ArrayList();
                list.add(fileToUrl(new File(dm.getInstanceProperties().
                        getProperty(WLDeploymentFactory.SERVER_ROOT_ATTR), 
                        "server/lib/weblogic.jar")));                  // NOI18N
                
                library.setContent(J2eeLibraryTypeProvider.
                        VOLUME_TYPE_CLASSPATH, list);
            } catch (MalformedURLException e) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
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
            // get the file's absolute URL
            URL url = file.toURI().toURL();
            
            // strip the jar's path and remain with the system resources URI
            if (FileUtil.isArchiveFile(url)) {
                url = FileUtil.getArchiveRoot(url);
            }
            
            // return
            return url;
        }
    }
    
}
