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

package org.netbeans.modules.tomcat5;

import java.awt.Image;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.J2eePlatformImpl;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Tomcat's implementation of the J2eePlatformImpl.
 *
 * @author Stepan Herold
 */
public class TomcatPlatformImpl extends J2eePlatformImpl {
    
    private static final Set/*<Object>*/ MODULE_TYPES = new HashSet();
    private static final Set/*<String>*/ SPEC_VERSIONS = new HashSet();
    
    private static final String JSP_API_JAR = "common/lib/jsp-api.jar";             // NOI18N
    private static final String JSP_API_DOC = "webapps/tomcat-docs/jspapi";         // NOI18N
    private static final String SERVLET_API_JAR = "common/lib/servlet-api.jar";     // NOI18N
    private static final String SERVLET_API_DOC = "webapps/tomcat-docs/servletapi"; // NOI18N
    private static final String J2EE_API_DOC    = "docs/j2eeri-1_4-doc-api.zip";    // NOI18N
    
    private static final String ICON = "org/netbeans/modules/tomcat5/resources/tomcat5instance.png"; // NOI18N
    
    private File catalinaHome;
    private File catalinaBase;
    private String displayName;
    
    private List/*<LibraryImpl>*/ libraries  = new ArrayList();
    
    static {
        MODULE_TYPES.add(J2eeModule.WAR);
        SPEC_VERSIONS.add(J2eeModule.J2EE_13);
        SPEC_VERSIONS.add(J2eeModule.J2EE_14);
    }
    
    /** Creates a new instance of TomcatInstallation */
    public TomcatPlatformImpl(File aCatalinaHome, File aCatalinaBase, String aDisplayName) {
        catalinaHome = aCatalinaHome;
        catalinaBase = aCatalinaBase;
        displayName = aDisplayName;
        try {
            J2eeLibraryTypeProvider lp = new J2eeLibraryTypeProvider();
            LibraryImplementation lib = lp.createLibrary();

            lib.setName(NbBundle.getMessage(TomcatPlatformImpl.class, "jsp20")); // NOI18N
            
            List l = new ArrayList();
            l.add(fileToUrl(new File(catalinaHome, JSP_API_JAR)));
            lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH, l);
            
            File jspDoc = new File(catalinaHome, JSP_API_DOC);
            if (jspDoc == null || !jspDoc.exists()) {
                jspDoc = InstalledFileLocator.getDefault().locate(J2EE_API_DOC, null, false);
            }
            if (jspDoc != null) {
                l = new ArrayList();
                l.add(fileToUrl(jspDoc));
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_JAVADOC, l);
            }
            
            libraries.add(lib);

            
            lib = lp.createLibrary();
            lib.setName(NbBundle.getMessage(TomcatPlatformImpl.class, "servlet24")); // NOI18N
            
            l = new ArrayList();
            l.add(fileToUrl(new File(catalinaHome, SERVLET_API_JAR)));
            lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH, l);
            
            File servletDoc = new File(catalinaHome, SERVLET_API_DOC);
            if (servletDoc == null || !servletDoc.exists()) {
                servletDoc = InstalledFileLocator.getDefault().locate(J2EE_API_DOC, null, false);
            }
            if (servletDoc != null) {
                l = new ArrayList();
                l.add(fileToUrl(servletDoc));
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_JAVADOC, l);
            }
            
            libraries.add(lib);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public LibraryImplementation[] getLibraries() {
       return (LibraryImplementation[])libraries.toArray(new LibraryImplementation[libraries.size()]);
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public Image getIcon() {
        return Utilities.loadImage(ICON);
    }
    
    public File[] getPlatformRoots() {
        if (catalinaBase != null) {
            return new File[] {catalinaHome, catalinaBase};
        } else {
            return new File[] {catalinaHome};
        }
    }
    
    public File[] getToolClasspathEntries(String toolName) {
        return null;
    }
    
    public boolean isToolSupported(String toolName) {
        return false;
    }
    
    public Set/*<Object>*/ getSupportedModuleTypes() {
        return MODULE_TYPES;
    }
    
    public Set/*<String>*/ getSupportedSpecVersions() {
        return SPEC_VERSIONS;
    }
    
    private URL fileToUrl(File file) throws MalformedURLException {
        URL url = file.toURI().toURL();
        if (FileUtil.isArchiveFile(url)) {
            url = FileUtil.getArchiveRoot(url);
        }
        return url;
    }
}
