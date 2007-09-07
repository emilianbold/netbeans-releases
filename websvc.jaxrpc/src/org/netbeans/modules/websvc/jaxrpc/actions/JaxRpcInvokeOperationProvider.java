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

package org.netbeans.modules.websvc.jaxrpc.actions;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.websvc.core.InvokeOperationActionProvider;
import org.netbeans.modules.websvc.core.InvokeOperationCookie;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.netbeans.modules.websvc.core.dev.wizard.ProjectInfo;
import org.openide.filesystems.FileObject;
import org.openide.loaders.*;

public class JaxRpcInvokeOperationProvider implements InvokeOperationActionProvider {
    
    
    public InvokeOperationCookie getInvokeOperationCookie(FileObject targetSource) {
        Project project = FileOwnerQuery.getOwner(targetSource);
        if(supportsJaxrpcOnly(project, targetSource)){
            return new JaxRpcInvokeOperation(project);
        }
        
        return null;
    }
    
    private boolean supportsJaxrpcOnly(Project project, FileObject targetSource){
        ProjectInfo projectInfo = new ProjectInfo(project);
        int projectType = projectInfo.getProjectType();
        if(projectType == ProjectInfo.JSE_PROJECT_TYPE && isJaxWsLibraryOnClasspath(targetSource)) return false;
        if(projectInfo.isJwsdpSupported())return false;
        if(Util.isJavaEE5orHigher(project)) return false;
        if (JaxWsUtils.isEjbJavaEE5orHigher(projectInfo)) return false;
        if (projectType == ProjectInfo.WEB_PROJECT_TYPE && !Util.isJavaEE5orHigher(project) && isJaxWsLibraryOnRuntimeClasspath(targetSource))return false;
        return true;
    }
    
    private boolean isJaxWsLibraryOnRuntimeClasspath(FileObject targetSource){
        ClassPath classPath = ClassPath.getClassPath(targetSource,ClassPath.EXECUTE);
        if (classPath != null) {
            if (classPath.findResource("javax/xml/ws/Service.class")!=null  &&
                    classPath.findResource("javax/xml/rpc/Service.class") == null){
                return true;
            }
        }
        return false;
    }
    
    private boolean isJaxWsLibraryOnClasspath(FileObject targetSource) {
        //test on javax.xml.ws.Service.class
        // checking COMPILE classpath
        ClassPath classPath = ClassPath.getClassPath(targetSource,ClassPath.COMPILE);
        if (classPath != null) {
            if (classPath.findResource("javax/xml/ws/Service.class")!=null) return true;
        }
        //checking BOOT classpath
        classPath = ClassPath.getClassPath(targetSource,ClassPath.BOOT);
        if (classPath != null) {
            if (classPath.findResource("javax/xml/ws/Service.class")!=null) return true;
        }
        return false;
    }
    
}
