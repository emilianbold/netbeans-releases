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

package org.netbeans.modules.j2ee.common;

import java.awt.Component;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JLabel;
import java.awt.Container;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.JComponent;
import java.util.Iterator;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.persistence.spi.server.ServerStatusProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

public class Util {
    
    /*
     * Changes the text of a JLabel in component from oldLabel to newLabel
     */
    public static void changeLabelInComponent(JComponent component, String oldLabel, String newLabel) {
        JLabel label = findLabel(component, oldLabel);
        if(label != null) {
            label.setText(newLabel);
        }
    }
    
    /*
     * Hides a JLabel and the component that it is designated to labelFor, if any
     */
    public static void hideLabelAndLabelFor(JComponent component, String lab) {
        JLabel label = findLabel(component, lab);
        if(label != null) {
            label.setVisible(false);
            Component c = label.getLabelFor();
            if(c != null) {
                c.setVisible(false);
            }
        }
    }
    
    /*
     * Recursively gets all components in the components array and puts it in allComponents
     */
    public static void getAllComponents( Component[] components, Collection<Component> allComponents ) {
        for( int i = 0; i < components.length; i++ ) {
            if( components[i] != null ) {
                allComponents.add( components[i] );
                if( ( ( Container )components[i] ).getComponentCount() != 0 ) {
                    getAllComponents( ( ( Container )components[i] ).getComponents(), allComponents );
                }
            }
        }
    }
    
    /*
     *  Recursively finds a JLabel that has labelText in comp
     */
    public static JLabel findLabel(JComponent comp, String labelText) {
        List<Component> allComponents = new ArrayList<Component>();
        getAllComponents(comp.getComponents(), allComponents);
        Iterator<Component> iterator = allComponents.iterator();
        while(iterator.hasNext()) {
            Component c = iterator.next();
            if(c instanceof JLabel) {
                JLabel label = (JLabel)c;
                if(label.getText().equals(labelText)) {
                    return label;
                }
            }
        }
        return null;
    }
    
    
    public static ClassPath getFullClasspath(FileObject fo) {
        if (fo == null) {
            return null;
        }
        return ClassPathSupport.createProxyClassPath(new ClassPath[]{
            ClassPath.getClassPath(fo, ClassPath.SOURCE),
            ClassPath.getClassPath(fo, ClassPath.BOOT),
            ClassPath.getClassPath(fo, ClassPath.COMPILE)
        });
    }
    
    /**
     * Is J2EE version of a given project JavaEE 5 or higher?
     *
     * @param project J2EE project
     * @return true if J2EE version is JavaEE 5 or higher; otherwise false
     */
    public static boolean isJavaEE5orHigher(Project project) {
        if (project == null) {
            return false;
        }
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider != null) {
            J2eeModule j2eeModule = j2eeModuleProvider.getJ2eeModule();
            if (j2eeModule != null) {
                J2eeModule.Type type = j2eeModule.getType();
                double version = Double.parseDouble(j2eeModule.getModuleVersion());
                if (J2eeModule.Type.EJB.equals(type) && (version > 2.1)) {
                    return true;
                }
                if (J2eeModule.Type.WAR.equals(type) && (version > 2.4)) {
                    return true;
                }
                if (J2eeModule.Type.CAR.equals(type) && (version > 1.4)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Returns source level of a given project
     *
     * @param project Project
     * @return source level string representation, e.g. "1.6"
     */
    public static String getSourceLevel(Project project) {
        SourceLevelQueryImplementation sl = project.getLookup().lookup(SourceLevelQueryImplementation.class);
        return sl.getSourceLevel(project.getProjectDirectory());
    }
    
    /**
     * Is source level of a given project 1.4 or lower?
     *
     * @param project Project
     * @return true if source level is 1.4 or lower; otherwise false
     */
    public static boolean isSourceLevel14orLower(Project project) {
        String srcLevel = getSourceLevel(project);
        if (srcLevel != null) {
            double sourceLevel = Double.parseDouble(srcLevel);
            return (sourceLevel <= 1.4);
        } else
            return false;
    }
    
    /**
     * Is source level of a given project 1.6 or higher?
     *
     * @param project Project
     * @return true if source level is 1.6 or higher; otherwise false
     */
    public static boolean isSourceLevel16orHigher(Project project) {
        String srcLevel = getSourceLevel(project);
        if (srcLevel != null) {
            double sourceLevel = Double.parseDouble(srcLevel);
            return (sourceLevel >= 1.6);
        } else
            return false;
    }
    
    /**
     * Checks whether the given <code>project</code>'s target server instance
     * is present.
     *
     * @param  project the project to check; can not be null.
     * @return true if the target server instance of the given project
     *          exists, false otherwise.
     *
     * @since 1.8
     */
    public static boolean isValidServerInstance(Project project) {
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider == null) {
            return false;
        }
        return isValidServerInstance(j2eeModuleProvider);
    }
    
    /**
     * Checks whether the given <code>provider</code>'s target server instance
     * is present.
     *
     * @param  provider the provider to check; can not be null.
     * @return true if the target server instance of the given provider
     *          exists, false otherwise.
     *
     * @since 1.10
     */
    public static boolean isValidServerInstance(J2eeModuleProvider j2eeModuleProvider) {
        String serverInstanceID = j2eeModuleProvider.getServerInstanceID();
        if (serverInstanceID == null) {
            return false;
        }
        return Deployment.getDefault().getServerID(serverInstanceID) != null;
    }
    
    /**
     * Default implementation of ServerStatusProvider.
     */
    public static ServerStatusProvider createServerStatusProvider(final J2eeModuleProvider j2eeModuleProvider) {
        return new ServerStatusProvider() {
            public boolean validServerInstancePresent() {
                return isValidServerInstance(j2eeModuleProvider);
            }
        };
    }
    
    public static File[] getJ2eePlatformClasspathEntries(Project project) {
        if (project != null) {
            J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
            if (j2eeModuleProvider != null) {
                J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(j2eeModuleProvider.getServerInstanceID());
                if (j2eePlatform != null) {
                    return j2eePlatform.getClasspathEntries();
                }
            }
        }
        return new File[0];
    }
    
    /**
     * Returns true if the specified classpath contains a class of the given name,
     * false otherwise.
     * 
     * @param classpath consists of jar urls and folder urls containing classes
     * @param className the name of the class
     * 
     * @return true if the specified classpath contains a class of the given name,
     *         false otherwise.
     * 
     * @throws IOException if an I/O error has occurred
     * 
     * @since 1.15
     */
    public static boolean containsClass(List<URL> classPath, String className) throws IOException {
        Parameters.notNull("classpath", classPath); // NOI18N
        Parameters.notNull("className", className); // NOI18N
        
        List<File> diskFiles = new ArrayList<File>();
        for (URL url : classPath) {
            URL archiveURL = FileUtil.getArchiveFile(url);
            
            if (archiveURL != null) {
                url = archiveURL;
            }
            
            if ("nbinst".equals(url.getProtocol())) { // NOI18N
                // try to get a file: URL for the nbinst: URL
                FileObject fo = URLMapper.findFileObject(url);
                if (fo != null) {
                    URL localURL = URLMapper.findURL(fo, URLMapper.EXTERNAL);
                    if (localURL != null) {
                        url = localURL;
                    }
                }
            }
            
            FileObject fo = URLMapper.findFileObject(url);
            if (fo != null) {
                File diskFile = FileUtil.toFile(fo);
                if (diskFile != null) {
                    diskFiles.add(diskFile);
                }
            }
        }
        
        return containsClass(diskFiles, className);
    }
    
    /**
     * Returns true if the specified classpath contains a class of the given name,
     * false otherwise.
     * 
     * @param classpath consists of jar files and folders containing classes
     * @param className the name of the class
     * 
     * @return true if the specified classpath contains a class of the given name,
     *         false otherwise.
     * 
     * @throws IOException if an I/O error has occurred
     * 
     * @since 1.15
     */
    public static boolean containsClass(Collection<File> classpath, String className) throws IOException {
        Parameters.notNull("classpath", classpath); // NOI18N
        Parameters.notNull("driverClassName", className); // NOI18N
        String classFilePath = className.replace('.', '/') + ".class"; // NOI18N
        for (File file : classpath) {
            if (file.isFile()) {
                JarFile jf = new JarFile(file);
                try {
                    Enumeration entries = jf.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = (JarEntry) entries.nextElement();
                        if (classFilePath.equals(entry.getName())) {
                            return true;
                        }
                    }
                } finally {
                    jf.close();
                }
            } else {
                if (new File(file, classFilePath).exists()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Returns the label (the name) of the given Java EE specification version.
     * 
     * @param specificationVersion version of the Java EE specification.
     *        Acceptable values are those defined in {@link J2eeModule}:
     * <ul>
     *     <li>{@link J2eeModule.J2EE_13}
     *     <li>{@link J2eeModule.J2EE_14}
     *     <li>{@link J2eeModule.JAVA_EE_5}
     * </ul>
     * 
     * @return true if the specified classpath contains a class of the given name,
     *         false otherwise.
     * 
     * @throws NullPointerException if the specificationVersion is <code>null</code>
     * @throws IllegalArgumentException if the value of the method parameter
     *         is not known specification version constant
     * 
     * @since 1.18
     * @deprecated 
     */    
    public static String getJ2eeSpecificationLabel(String specificationVersion) {
        Parameters.notNull("specificationVersion", specificationVersion); // NOI18N
        
        if (J2eeModule.J2EE_13.equals(specificationVersion)) {
            return NbBundle.getMessage(Util.class, "LBL_J2EESpec_13");
        } else if (J2eeModule.J2EE_14.equals(specificationVersion)) {
            return NbBundle.getMessage(Util.class, "LBL_J2EESpec_14");
        } else if (J2eeModule.JAVA_EE_5.equals(specificationVersion)) {
            return NbBundle.getMessage(Util.class, "LBL_JavaEESpec_5");  
        } else {
            throw new IllegalArgumentException("Unknown specification version: " + specificationVersion); // NOI18N
        }
    }

    public static Set<Profile> getSupportedProfiles(Project project){
        Set<Profile> supportedProfiles = new HashSet<Profile>();
        J2eePlatform j2eePlatform = getPlatform(project);
        if (j2eePlatform != null){
            supportedProfiles = j2eePlatform.getSupportedProfiles();
        }
        return supportedProfiles;
    }

    private static J2eePlatform getPlatform(Project project) {
        try {
            J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
            if (provider != null){
                String instance = provider.getServerInstanceID();
                if (instance != null) {
                    return Deployment.getDefault().getServerInstance(provider.getServerInstanceID()).getJ2eePlatform();
                }
            }
        } catch (InstanceRemovedException ex) {
            // will return null
        }
        return null;
    }

}
