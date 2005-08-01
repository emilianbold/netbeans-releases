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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.J2eePlatformImpl;
import org.netbeans.modules.tomcat5.util.TomcatProperties;
import org.netbeans.modules.tomcat5.util.Utils;
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
    
    private static final String WSCOMPILE = "wscompile"; // NOI18N    
    private static final String WSCOMPILE_LIBS[] = new String[] {
        "jaxrpc/lib/jaxrpc-api.jar",        // NOI18N
        "jaxrpc/lib/jaxrpc-impl.jar",       // NOI18N
        "jaxrpc/lib/jaxrpc-spi.jar",        // NOI18N
        "saaj/lib/saaj-api.jar",            // NOI18N
        "saaj/lib/saaj-impl.jar",           // NOI18N
        "jwsdp-shared/lib/mail.jar",        // NOI18N
        "jwsdp-shared/lib/activation.jar"   // NOI18N
    };

    
    private static final String ICON = "org/netbeans/modules/tomcat5/resources/tomcat5instance.png"; // NOI18N
    
    private String displayName;    
    private TomcatProperties tp;
    
    private List/*<LibraryImpl>*/ libraries  = new ArrayList();
    
    static {
        MODULE_TYPES.add(J2eeModule.WAR);
        SPEC_VERSIONS.add(J2eeModule.J2EE_13);
        SPEC_VERSIONS.add(J2eeModule.J2EE_14);
    }
    
    /** Creates a new instance of TomcatInstallation */
    public TomcatPlatformImpl(TomcatProperties tp) {
        this.tp = tp;
        displayName = tp.getDisplayName();
        
        J2eeLibraryTypeProvider libProvider = new J2eeLibraryTypeProvider();
        LibraryImplementation lib = libProvider.createLibrary();
        lib.setName(NbBundle.getMessage(TomcatPlatformImpl.class, "LBL_lib_name", displayName));
        loadLibraries(lib);
        libraries.add(lib);
    }
    
    public void notifyLibrariesChanged() {
        LibraryImplementation lib = (LibraryImplementation)libraries.get(0);
        loadLibraries(lib);
        firePropertyChange(PROP_LIBRARIES, null, libraries);
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
        if (tp.getCatalinaBase() != null) {
            return new File[] {tp.getCatalinaHome(), tp.getCatalinaBase()};
        } else {
            return new File[] {tp.getCatalinaHome()};
        }
    }
    
    public File[] getToolClasspathEntries(String toolName) {
        // jwsdp support
        if (WSCOMPILE.equals(toolName)) {
            if (isToolSupported(WSCOMPILE)) {
                File[] retValue = new File[WSCOMPILE_LIBS.length];
                File homeDir = tp.getCatalinaHome();
                for (int i = 0; i < WSCOMPILE_LIBS.length; i++) {
                    retValue[i] = new File(homeDir, WSCOMPILE_LIBS[i]);
                }
                return retValue;
            }
        }
        return null;
    }
    
    public boolean isToolSupported(String toolName) {
        // jwsdp support
        if (WSCOMPILE.equals(toolName)) {
            File homeDir = tp.getCatalinaHome();
            for (int i = 0; i < WSCOMPILE_LIBS.length; i++) {
                if (!new File(homeDir, WSCOMPILE_LIBS[i]).exists()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
        
    public Set/*<Object>*/ getSupportedModuleTypes() {
        return MODULE_TYPES;
    }
    
    public Set/*<String>*/ getSupportedSpecVersions() {
        return SPEC_VERSIONS;
    }
    
    // private helper methods -------------------------------------------------
    
    private void loadLibraries(LibraryImplementation lib) {
        lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH, tp.getClasses());
        lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_JAVADOC, tp.getJavadocs());
        lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_SRC, tp.getSources());        
    }
}
