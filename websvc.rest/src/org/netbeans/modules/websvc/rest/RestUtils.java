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
package org.netbeans.modules.websvc.rest;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.openide.filesystems.FileObject;

/**
 * REST support utilitities across all project types.
 * 
 * @author Nam Nguyen
 */
public class RestUtils {
    public static final String SWDP_LIBRARY = "swdp"; //NOI18N
    /**
     *  Add SWDP library for given source file on specified class path types.
     * 
     *  @param source source file object for which the libraries is added.
     *  @param classPathTypes types of class path to add ("javac.compile",...)
     */
    public static void addSwdpLibrary(FileObject source, String[] classPathTypes) throws IOException {
        Project project = FileOwnerQuery.getOwner(source);
        if (project == null /*|| jeePlatformAlreadyHasSwdpLibrary(project)*/) {
            return;
        }
        // check if the wsimport class is already present - this means we don't need to add the library
        SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        ClassPath classPath = ClassPath.getClassPath(sgs[0].getRootFolder(),ClassPath.COMPILE);
        FileObject restClass = classPath.findResource("com/sun/ws/rest/api/UriTemplate.class"); // NOI18N
        if (restClass != null) {
            return;
        }
        
        Library swdpLibrary = LibraryManager.getDefault().getLibrary(SWDP_LIBRARY);
        if (swdpLibrary == null) {
            throw new IllegalStateException("SWDP library not found");
        }
        ProjectClassPathExtender pce = project.getLookup().lookup(ProjectClassPathExtender.class);
        ProjectClassPathModifier pcm = project.getLookup().lookup(ProjectClassPathModifier.class);
        if (pcm != null) {
            for (String type : classPathTypes) {
                pcm.addLibraries(new Library[] { swdpLibrary }, source, type);
            }
        } else if (pce != null) {
            pce.addLibrary(swdpLibrary);
        } else{
            throw new IllegalStateException("Current project does not have support " +
                    "for ProjectClassPathModifier or ProjectClassPathExtender");
        }
    }
    
    /*public static boolean jeePlatformAlreadyHasSwdpLibrary(Project project) {
        J2eeModuleProvider j2eeModuleProvider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider == null){
            return false;
        }
        
        J2eePlatform platform  = Deployment.getDefault().getJ2eePlatform(j2eeModuleProvider.getServerInstanceID());
        if (platform == null){
            return false;
        }
        
        for (File file : platform.getClasspathEntries()) {
            if (file.getName().equals("restbeans-impl.jar")) { //NOI18N
                return true;
            }
        }
        return false;
    }*/
}
