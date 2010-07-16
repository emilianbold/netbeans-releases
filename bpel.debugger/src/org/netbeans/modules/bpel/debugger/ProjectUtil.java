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
package org.netbeans.modules.bpel.debugger;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.api.project.ProjectInformation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public final class ProjectUtil {
    public static Project getProject(String baseDir) throws IOException {
        Project proj = null;
        
        File projFolder = new File(baseDir);
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(projFolder));
        try {
            proj = ProjectManager.getDefault().findProject(fo);
        } catch (IllegalArgumentException ex){
            //log.warning("Unable to get Netbeans Project object for: " + baseDir);
        }
        return proj;
    }
    
    public static Set<String> getSubprojectsBaseDirs(String projBaseDir) throws IOException {
        Project proj = getProject(projBaseDir);
        if (proj != null) {
            return getSubprojectsBaseDirs(proj);
        } else {
            return new HashSet<String>();
        }
    }
    
    public static Set<String> getSubprojectsBaseDirs(Project proj) throws IOException{
        Set<String> ret = new HashSet<String>();
        
        if (proj != null){
            SubprojectProvider sp = (SubprojectProvider) proj.getLookup().lookup(SubprojectProvider.class);
            if (sp != null){
                Set sprjs = sp.getSubprojects();
                Iterator itr = sprjs.iterator();
                while (itr.hasNext()){
                    Project sprj = (Project) itr.next();
                    String baseDir = sprj.getProjectDirectory().getPath();
                    ret.add(baseDir);
                    ret.addAll(getSubprojectsBaseDirs(sprj));
                }
            }
        }
        return ret;
    }
    
    public static String getProjectDisplayName(String baseDir) throws IOException {
        Project proj = getProject(baseDir);
        if (proj != null) {
            ProjectInformation info = (ProjectInformation)proj.getLookup().lookup(ProjectInformation.class);
            return info.getDisplayName();
        } else {
            return null;
        }
    }
}
