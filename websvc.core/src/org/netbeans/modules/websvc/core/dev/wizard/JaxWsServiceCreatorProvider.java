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
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.core.ServiceCreator;
import org.netbeans.modules.websvc.core.ServiceCreatorProvider;
import org.openide.WizardDescriptor;
import org.netbeans.modules.j2ee.common.Util;

/**
 *
 * @author Milan Kuchtiak
 */
public class JaxWsServiceCreatorProvider implements ServiceCreatorProvider {

    public JaxWsServiceCreatorProvider() {
    }
    
    public ServiceCreator getServiceCreator(Project project, WizardDescriptor wiz) {
        ProjectInfo projectInfo = new ProjectInfo(project);
        int projectType = projectInfo.getProjectType();
        if ((projectType == ProjectInfo.JSE_PROJECT_TYPE && Util.isSourceLevel16orHigher(project)) ||
                ((Util.isJavaEE5orHigher(project) &&
                (projectType == ProjectInfo.WEB_PROJECT_TYPE || projectType == ProjectInfo.EJB_PROJECT_TYPE))) ||
                (projectInfo.isJwsdpSupported())
                ) {
            return new JaxWsServiceCreator(projectInfo, wiz, false);
        } else if (!Util.isJavaEE5orHigher(project) &&
                   (projectType == ProjectInfo.WEB_PROJECT_TYPE || projectType == ProjectInfo.EJB_PROJECT_TYPE) || 
                   projectInfo.isWsitSupported()) {
               if ((!projectInfo.isJsr109Supported() && projectType == ProjectInfo.WEB_PROJECT_TYPE && !projectInfo.isJsr109oldSupported()) || projectInfo.isWsitSupported()) {
                   return new JaxWsServiceCreator(projectInfo, wiz, true);
               }
        }
        return null;
    }
}
