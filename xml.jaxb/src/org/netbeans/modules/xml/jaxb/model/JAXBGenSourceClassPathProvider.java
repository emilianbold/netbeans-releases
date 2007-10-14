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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.xml.jaxb.model;

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
 * @author gpatil
 */
public class JAXBGenSourceClassPathProvider implements ClassPathProvider {
    private static final String JAXB_GEN_SRC_ROOT= 
            "build/generated/addons/jaxb" ;//NOI18N
    
    private Project project;
    private ClassPath sourceCP, compileCP, bootCP;
    
    JAXBGenSourceClassPathProvider(Project project) {
        this.project = project;
    }
    
    public ClassPath findClassPath(FileObject file, String type) {
        FileObject clientArtifactsFolder = 
                project.getProjectDirectory().getFileObject(JAXB_GEN_SRC_ROOT);
        if (clientArtifactsFolder != null && 
                (file.equals(clientArtifactsFolder) || FileUtil.isParentOf(
                clientArtifactsFolder, file))) {
            if (ClassPath.SOURCE.equals(type)) {
                if (sourceCP == null) {
                    sourceCP = getClassPath(ClassPath.SOURCE);
                }
                return sourceCP;
            } else if (ClassPath.COMPILE.equals(type)) {
                if (compileCP == null) {
                    compileCP = getClassPath(ClassPath.COMPILE);
                }
                return compileCP;
            } else if (ClassPath.BOOT.equals(type)) {
                if (bootCP == null) {
                    bootCP = getClassPath(ClassPath.BOOT);
                }
                return bootCP;
            }               
        } 
        
        return null;
    }
    
    private ClassPath getClassPath(String classPathType) {
        Sources sources = project.getLookup().lookup(Sources.class);
        SourceGroup[] groups = ProjectUtils.getSources(project).
                getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (groups.length > 0) {
            return ClassPath.getClassPath(groups[0].getRootFolder(), 
                    classPathType);
        }
        return null;
    }
}
