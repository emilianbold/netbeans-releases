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

package org.netbeans.modules.j2ee.ejbjarproject;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.ejbcore.spi.ProjectPropertiesSupport;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarProvider;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarsInProject;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class ProjectEjbJarProvider implements EjbJarProvider, EjbJarsInProject, ProjectPropertiesSupport {
    
    private EjbJarProject project;
    
    public ProjectEjbJarProvider (EjbJarProject project) {
        this.project = project;
    }
    
    public EjbJar findEjbJar (FileObject file) {
        Project project = FileOwnerQuery.getOwner (file);
        if (project != null && project instanceof EjbJarProject) {
            EjbJarProject ep = (EjbJarProject) project;
            // TODO: ma154696: this is changed entry of Pavel Buzek added AFTER branching; have to be checked!!!
            FileObject[] sourceRoots = ep.getSourceRoots().getRoots();
            for (int i = 0; i < sourceRoots.length; i++) {
                FileObject src = sourceRoots[i];
                if (src != null && (src.equals(file) || FileUtil.isParentOf(src, file))) {
                    return ep.getAPIEjbJar();
                }
            }

            FileObject build = ep.getEjbModule().getBuildDirectory();
            if (build != null && (build.equals (file) || FileUtil.isParentOf (build, file))) {
                return ep.getAPIEjbJar();
            }

            FileObject prjdir = ep.getProjectDirectory();
            if (prjdir != null && (prjdir.equals (file) || FileUtil.isParentOf(prjdir, file))) {
                return ep.getAPIEjbJar();
            }
        }
        return null;
    }

    public EjbJar[] getEjbJars() {
        return new EjbJar [] {project.getAPIEjbJar()};
    }

    public void disableSunCmpMappingExclusion() {
        EjbJarProject ejbProject = (EjbJarProject) project;
        PropertyHelper ph = ejbProject.getPropertyHelper();
        
        String metaInfExcludes = ph.getProperty(AntProjectHelper.PROJECT_PROPERTIES_PATH, EjbJarProjectProperties.META_INF_EXCLUDES);
        if (metaInfExcludes == null) {
            return;
        }
        String[] tokens = metaInfExcludes.split(" |,");
        StringBuffer newMetaInfExcludes = new StringBuffer();
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equals("sun-cmp-mappings.xml") || tokens[i].equals("")) // NOI18N
            {
                continue;
            }

            newMetaInfExcludes.append(tokens[i]);
            if (i < tokens.length - 1) {
                newMetaInfExcludes.append(" "); // NOI18N
            }
        }
        ph.saveProperty(AntProjectHelper.PROJECT_PROPERTIES_PATH, EjbJarProjectProperties.META_INF_EXCLUDES, newMetaInfExcludes.toString());
    }
    
}
