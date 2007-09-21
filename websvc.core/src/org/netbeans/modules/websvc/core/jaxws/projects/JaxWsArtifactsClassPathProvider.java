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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.core.jaxws.projects;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mkuchtiak
 */
public class JaxWsArtifactsClassPathProvider implements ClassPathProvider {
    private Project project;
    private ClassPath cp;
    
    JaxWsArtifactsClassPathProvider(Project project) {
        this.project = project;
    }
    
    public ClassPath findClassPath(FileObject file, String type) {
        if (ClassPath.SOURCE.equals(type)) {
            FileObject clientArtifactsFolder = 
                    project.getProjectDirectory().getFileObject("build/generated/wsimport/client"); //NOI18N
            if (clientArtifactsFolder != null && 
                    (file == clientArtifactsFolder || FileUtil.isParentOf(clientArtifactsFolder,file))) {
                if (cp == null) cp = getClassPath();
                return cp;
            } else {
                FileObject serviceArtifactsFolder = 
                project.getProjectDirectory().getFileObject("build/generated/wsimport/service"); //NOI18N
                if (serviceArtifactsFolder != null && 
                    (file == serviceArtifactsFolder || FileUtil.isParentOf(serviceArtifactsFolder,file))) {
                    if (cp == null) cp = getClassPath();
                    return cp;
                }
            }
            
        }
        return null;
    }
    
    private ClassPath getClassPath() {
        Sources sources = project.getLookup().lookup(Sources.class);
        SourceGroup[] groups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (groups.length > 0) {
            return ClassPath.getClassPath(groups[0].getRootFolder(), ClassPath.SOURCE);
        }
        return null;
    }
    
}
