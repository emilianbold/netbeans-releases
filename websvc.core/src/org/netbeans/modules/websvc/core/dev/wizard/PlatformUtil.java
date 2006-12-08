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
/*
 * PlatformUtil.java
 *
 * Created on April 18, 2006, 2:19 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.core.dev.wizard;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author rico
 */
public class PlatformUtil {
    
    /** Creates a new instance of PlatformUtil */
    public PlatformUtil() {
    }
    
    //Factor these methods out and put in a common Util class
    public static J2eePlatform getJ2eePlatform(Project project){
        J2eeModuleProvider provider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
        if(provider != null){
            String serverInstanceID = provider.getServerInstanceID();
            if(serverInstanceID != null && serverInstanceID.length() > 0) {
                return Deployment.getDefault().getJ2eePlatform(serverInstanceID);
            }
        }
        return null;
    }
    
     public static boolean isJWSDPSupported(Project project){
        J2eePlatform j2eePlatform = getJ2eePlatform(project);
        if(j2eePlatform != null){
            return j2eePlatform.isToolSupported(J2eePlatform.TOOL_JWSDP);
        }
        return false;
    }
    
     public static boolean isWsitSupported(Project project){
        J2eePlatform j2eePlatform = getJ2eePlatform(project);
        if(j2eePlatform != null){
            return j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSIT);
        }
        return false;
     }

     public static boolean isJsr109Supported(Project project){
        J2eePlatform j2eePlatform = getJ2eePlatform(project);
        if(j2eePlatform != null){
            return j2eePlatform.isToolSupported(J2eePlatform.TOOL_JSR109);
        }
        return false;
    }
    
    public static boolean isJsr109OldSupported(Project project){
        J2eePlatform j2eePlatform = getJ2eePlatform(project);
        if(j2eePlatform != null){
            return j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSCOMPILE);
        }
        return false;
    }
    
    public static boolean hasJAXWSLibrary(Project project){
        SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        ClassPath classPath = ClassPath.getClassPath(sgs[0].getRootFolder(),ClassPath.COMPILE);
        FileObject wsimportFO = classPath.findResource("com/sun/tools/ws/ant/WsImport.class"); // NOI18N
        return wsimportFO != null;
    }
}


