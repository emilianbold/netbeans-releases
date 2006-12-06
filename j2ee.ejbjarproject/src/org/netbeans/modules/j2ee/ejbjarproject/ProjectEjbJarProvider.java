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

package org.netbeans.modules.j2ee.ejbjarproject;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarProvider;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarsInProject;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;

public class ProjectEjbJarProvider implements EjbJarProvider, EjbJarsInProject/*, ProjectPropertiesSupport*/ {
    
    private EjbJarProject project;
    
    public ProjectEjbJarProvider (EjbJarProject project) {
        this.project = project;
    }
    
    public EjbJar findEjbJar (FileObject file) {
        Project project = FileOwnerQuery.getOwner (file);
        if (project != null && project instanceof EjbJarProject) {
            return ((EjbJarProject) project).getAPIEjbJar();
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
