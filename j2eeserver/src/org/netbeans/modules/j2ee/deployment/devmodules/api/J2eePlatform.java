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

package org.netbeans.modules.j2ee.deployment.devmodules.api;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.plugins.api.J2eePlatformImpl;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Utilities;


/**
 * J2eePlatform describes the target environment J2EE applications are build against 
 * and subsequently deployed to. Each server instance defines its own J2EE platform.
 * 
 * @author Stepan Herold
 * @since 1.5
 */
public final class J2eePlatform {
    
    /** Display name property */
    public static final String PROP_DISPLAY_NAME = "displayName";       //NOI18N
    /** Libraries property */
    public static final String PROP_LIBRARIES = "libraries";            //NOI18N
    /** Classpath property */
    public static final String PROP_CLASSPATH = "classpath";            //NOI18N
    /** Platform roots property */
    public static final String PROP_PLATFORM_ROOTS = "platformRoots";   //NOI18N
    
    private static final String DEFAULT_ICON = "org/netbeans/modules/j2ee/deployment/impl/ui/resources/Servers.png"; // NOI18N
    
    private J2eePlatformImpl impl;
    private File[] classpathCache;
    private String currentClasspath;
    private ServerInstance serverInstance;
    
    // listens to libraries content changes
    private PropertyChangeListener librariesChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(LibraryImplementation.PROP_CONTENT)) {
                classpathCache = null;
                String newClassPath = getClasspathAsString();
                if (currentClasspath == null || !currentClasspath.equals(newClassPath)) {
                    currentClasspath = newClassPath;
                    impl.firePropertyChange(PROP_CLASSPATH, null, null);   
                }
            }
        }
    };
    
    /** 
     * Creates a new instance of J2eePlatform.
     * 
     * @param aImpl instance of <code>J2eePlatformImpl</code>.
     */
    private J2eePlatform(ServerInstance aServerInstance, J2eePlatformImpl aImpl) {
        impl = aImpl;
        serverInstance = aServerInstance;
        // listens to libraries changes
        addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(PROP_LIBRARIES)) {
                    LibraryImplementation[] libs = getLibraries();
                    for (int i = 0; i < libs.length; i++) {
                        libs[i].removePropertyChangeListener(librariesChangeListener);
                        libs[i].addPropertyChangeListener(librariesChangeListener);
                    }
                    
                    classpathCache = null;
                    String newClassPath = getClasspathAsString();
                    if (currentClasspath == null || !currentClasspath.equals(newClassPath)) {
                        currentClasspath = newClassPath;
                        impl.firePropertyChange(PROP_CLASSPATH, null, null);   
                    }
                }
            }
        });
        LibraryImplementation[] libs = getLibraries();
        for (int i = 0; i < libs.length; i++) {
            libs[i].addPropertyChangeListener(librariesChangeListener);
        }
        currentClasspath = getClasspathAsString();
    }
    
    static J2eePlatform create(ServerInstance serInst) {
        J2eePlatform result = serInst.getJ2eePlatform();
        if (result == null) {
            J2eePlatformImpl platformImpl = serInst.getJ2eePlatformImpl();
            if (platformImpl != null) {
                result = new J2eePlatform(serInst, platformImpl);
                serInst.setJ2eePlatform(result);
            }
        }
        return result;
    }
    
    /**
     * Return classpath entries.
     *
     * @return classpath entries.
     */
    public File[] getClasspathEntries() {
        if (classpathCache == null) {
            LibraryImplementation[] libraries = impl.getLibraries();
            List/*<String>*/ classpath = new ArrayList();
            for (int i = 0; i < libraries.length; i++) {
                List classpathList = libraries[i].getContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH);
                for (Iterator iter = classpathList.iterator(); iter.hasNext();) {
                    URL url = (URL)iter.next();
                    if ("jar".equals(url.getProtocol())) { //NOI18N
                        url = FileUtil.getArchiveFile(url);
                    }
                    FileObject fo = URLMapper.findFileObject(url);
                    if (fo != null) {
                        File f = FileUtil.toFile(fo);
                        if (f != null) {
                            classpath.add(f);
                        }   
                    }
                }
            }
            classpathCache = (File[])classpath.toArray(new File[classpath.size()]);
        }
        return classpathCache;
    }
    
    /**
     * Return classpath for the specified tool.
     *
     * @param  toolName tool's name e.g. "wscompile".
     * @return classpath for the specified tool.
     */
    public File[] getToolClasspathEntries(String toolName) {
        return impl.getToolClasspathEntries(toolName);
    }
    
    /**
     * Specifies whether a tool of the given name is supported by this platform.
     *
     * @param  toolName tool's name e.g. "wscompile".
     * @return <code>true</code> if platform supports tool of the given name, 
     *         <code>false</code> otherwise.
     */
    public boolean isToolSupported(String toolName) {
        return impl.isToolSupported(toolName);
    }
    
    // this will be made public and will return Library
    private LibraryImplementation[] getLibraries() {
        return impl.getLibraries();
    }
    
    /**
     * Return platform's display name.
     *
     * @return platform's display name.
     */
    public String getDisplayName() {
        // return impl.getDisplayName();
        // AB: for now return server instance's display name
        return serverInstance.getDisplayName();
    }
    
    /**
     * Return platform's icon.
     *
     * @return platform's icon.
     * @since 1.6
     */
    public Image getIcon() {
        Image result = impl.getIcon();
        if (result == null) 
            result = Utilities.loadImage(DEFAULT_ICON);
        
        return result;
    }
    
    /**
     * Return platform's root directories. This will be mostly server's installation
     * directory.
     *
     * @return platform's root directories.
     */
    public File[] getPlatformRoots() {
        return impl.getPlatformRoots();
    }
    
    /**
     * Return a list of supported J2EE specification versions. Use J2EE specification 
     * versions defined in the {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule}
     * class.
     *
     * @return list of supported J2EE specification versions.
     */
    public Set/*<String>*/ getSupportedSpecVersions() {
        return impl.getSupportedSpecVersions();
    }
    
    /**
     * Return a list of supported J2EE module types. Use module types defined in the 
     * {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule}
     * class.
     *
     * @return list of supported J2EE module types.
     */
    public Set/*<Object>*/ getSupportedModuleTypes() {
        return impl.getSupportedModuleTypes();
    }
    
    /**
     * Return a set of J2SE platform versions this J2EE platform can run with.
     * Versions should be specified as strings i.g. ("1.3", "1.4", etc.)
     * 
     * @return set of J2SE platform versions this J2EE platform can run with.
     *
     * @since 1.9
     */
    public Set getSupportedJavaPlatformVersions() {
        return impl.getSupportedJavaPlatformVersions();
    }
    
    /**
     * Is profiling supported by this J2EE platform?
     *
     * @return true, if profiling is supported, false otherwise.
     *
     * @since 1.9
     */
    public boolean supportsProfiling() {
        return true;
    }
    
    /**
     * Return server J2SE platform or null if the platform is unknown, not 
     * registered in the IDE.
     *
     * @return server J2SE platform or null if the platform is unknown, not 
     *         registered in the IDE.
     *
     * @since 1.9
     */
    public JavaPlatform getJavaPlatform() {
        return impl.getJavaPlatform();
    }
    
    /**
     * Register a listener which will be notified when some of the platform's properties
     * change.
     * 
     * @param l listener which should be added.
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        impl.addPropertyChangeListener(l);
    }
    
    /**
     * Remove a listener registered previously.
     *
     * @param l listener which should be removed.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        impl.removePropertyChangeListener(l);
    }
    
    public String toString() {
        return impl.getDisplayName() + " [" + getClasspathAsString() + "]"; // NOI18N
    }
    
    private String getClasspathAsString() {
        File[] classpathEntr = getClasspathEntries();
        StringBuffer classpath = new StringBuffer();
        final String PATH_SEPARATOR = System.getProperty("path.separator"); // NOI18N
        for (int i = 0; i < classpathEntr.length; i++) {
            classpath.append(classpathEntr[i].getAbsolutePath());
            if (i + 1 < classpathEntr.length) {
                classpath.append(PATH_SEPARATOR);
            }
        }
        return classpath.toString();
    }
}
