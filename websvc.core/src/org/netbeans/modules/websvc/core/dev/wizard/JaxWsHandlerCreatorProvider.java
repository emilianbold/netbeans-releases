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

package org.netbeans.modules.websvc.core.dev.wizard;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.websvc.core.HandlerCreator;
import org.netbeans.modules.websvc.core.HandlerCreatorProvider;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;


/**
 *
 * @author Milan Kuchtiak
 */
public class JaxWsHandlerCreatorProvider implements HandlerCreatorProvider {

    public JaxWsHandlerCreatorProvider() {
    }
    
    public HandlerCreator getHandlerCreator(Project project, WizardDescriptor wiz) {
        SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        ClassPath classPath = ClassPath.getClassPath(sgs[0].getRootFolder(),ClassPath.COMPILE);
        FileObject wsimportFO = classPath.findResource("com/sun/tools/ws/ant/WsImport.class"); // NOI18N
        ProjectInfo projectInfo = new ProjectInfo(project);
        int projectType = projectInfo.getProjectType();
        if ((projectType == ProjectInfo.JSE_PROJECT_TYPE && Util.isSourceLevel16orHigher(project)) ||
                (projectType == ProjectInfo.JSE_PROJECT_TYPE && Util.getSourceLevel(project).equals("1.5") && wsimportFO != null) || //NOI18N
                (Util.isJavaEE5orHigher(project) && (projectType == ProjectInfo.WEB_PROJECT_TYPE || projectType == ProjectInfo.EJB_PROJECT_TYPE)) || //NOI18N
                (projectInfo.isJwsdpSupported() || (!projectInfo.isJsr109Supported() && projectType == ProjectInfo.WEB_PROJECT_TYPE && !projectInfo.isJsr109oldSupported()))
                ) {
            return new JaxWsHandlerCreator(project, wiz);
        }
        return null;
    }

}
