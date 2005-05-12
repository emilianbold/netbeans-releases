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
package org.netbeans.modules.j2ee.websphere6.j2ee;

import org.netbeans.modules.j2ee.websphere6.WSDeploymentFactory;
import org.netbeans.modules.j2ee.websphere6.WSDeploymentManager;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.api.J2eePlatformFactory;
import org.netbeans.modules.j2ee.deployment.plugins.api.J2eePlatformImpl;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;


/**
 *
 * @author Kirill Sorokin
 */
public class WSJ2eePlatformFactory extends J2eePlatformFactory {
    
    public J2eePlatformImpl getJ2eePlatformImpl(DeploymentManager dm) {
        return new J2eePlatformImplImpl(dm);
    }
    
    private static class J2eePlatformImplImpl extends J2eePlatformImpl {
        
        WSDeploymentManager dm;
        
        public J2eePlatformImplImpl(DeploymentManager dm) {
            this.dm = (WSDeploymentManager) dm;
        }
        
        public boolean isToolSupported(String toolName) {
            return false;
        }
        
        public File[] getToolClasspathEntries(String toolName) {
            return new File[0];
        }
        
        public Set getSupportedSpecVersions() {
            Set result = new HashSet();
            result.add(J2eeModule.J2EE_14);
            return result;
        }
        
        public java.util.Set getSupportedModuleTypes() {
            Set result = new HashSet();
            result.add(J2eeModule.EAR);
            result.add(J2eeModule.WAR);
            result.add(J2eeModule.EJB);
            result.add(J2eeModule.CONN);
            result.add(J2eeModule.CLIENT);
            return result;
        }
        
        public java.io.File[] getPlatformRoots() {
            return new File[]{new File(dm.getInstanceProperties().getProperty(WSDeploymentFactory.SERVER_ROOT_ATTR))};
        }
        
        public LibraryImplementation[] getLibraries() {
            
            LibraryImplementation[] libraries = new LibraryImplementation[1];
            
            LibraryImplementation library = new J2eeLibraryTypeProvider().createLibrary();
            
            library.setName(NbBundle.getMessage(WSJ2eePlatformFactory.class, "LIBRARY_NAME")); // NOI18N
            
            try {
                List list = new ArrayList();
                list.add(fileToUrl(new File(dm.getInstanceProperties().getProperty(WSDeploymentFactory.SERVER_ROOT_ATTR), "/lib/j2ee.jar"))); // NOI18N
                
                library.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH, list);
            } catch (MalformedURLException e) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
            }
            
            libraries[0] = library;
            
            return libraries;
        }
        
        public java.awt.Image getIcon() {
            return null;
            //return Utilities.loadImage("org/netbeans/modules/j2ee/genericserver/resources/GSInstanceIcon.gif");
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(WSJ2eePlatformFactory.class, "PLATFORM_NAME"); // NOI18N
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
