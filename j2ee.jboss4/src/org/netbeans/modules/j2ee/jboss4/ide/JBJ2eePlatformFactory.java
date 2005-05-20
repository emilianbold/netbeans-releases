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
package org.netbeans.modules.j2ee.jboss4.ide;

import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginProperties;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        return new J2eePlatformImplImpl(dm);
    }
    
}

class J2eePlatformImplImpl extends J2eePlatformImpl {
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
    
    private String platformRoot = JBPluginProperties.getInstance().getInstallLocation();
    
    private LibraryImplementation[] libraries = null;
    
    private DeploymentManager dm;
    
    public J2eePlatformImplImpl(DeploymentManager dm) {
        this.dm = dm;
    }
    
    public Set getSupportedSpecVersions() {
        return SPEC_VERSIONS;
    }
    
    public Set getSupportedModuleTypes() {
        return MODULE_TYPES;
    }
    
    public File[] getPlatformRoots() {
        return new File[]{new File(platformRoot)};
    }
    
    private void initLibraries() {
        try {
            libraries = new LibraryImplementation[2];

            J2eeLibraryTypeProvider libraryProvider = new J2eeLibraryTypeProvider();

            LibraryImplementation library = libraryProvider.createLibrary();
            library.setName(NbBundle.getMessage(JBJ2eePlatformFactory.class, "TITLE_JBOSS_LIBRARY")); //NOI18N

            List list = new ArrayList();
            list.add(fileToUrl(new File(platformRoot, "client/jboss-j2ee.jar")));  //NOI18N
            library.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH, list);

            File j2eeDoc = InstalledFileLocator.getDefault().locate(J2EE_API_DOC, null, false);
            if (j2eeDoc != null) {
                list = new ArrayList();
                list.add(fileToUrl(j2eeDoc));
                library.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_JAVADOC, list);
            }
            libraries[0] = library;
            
            library = libraryProvider.createLibrary();
            addFiles(new File (platformRoot, "lib"), list); //NOI18N
            
            String domain = null;
            if (dm instanceof JBDeploymentManager) {
                domain = InstanceProperties.getInstanceProperties(((JBDeploymentManager)dm).getUrl()).getProperty("server"); //NOI18N
            }
            if (domain != null) {
                addFiles(new File (platformRoot, "server/" + domain + "/lib"), list); //NOI18N
            }
            library.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH, list);
            libraries[1] = library;

        } catch (MalformedURLException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
    }
    
    
    private void addFiles (File folder, List l) {
        File files [] = folder.listFiles(new FF ());
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
        return false;
    }
    
    public File[] getToolClasspathEntries(String toolName) {
        return new File[0];
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