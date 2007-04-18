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

package org.netbeans.modules.compapp.projects.jbi;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author jqian
 */
public class CasaHelper {
        
    public static FileObject getCasaFileObject(Project jbiProject, boolean create) {
        ProjectInformation projInfo = (ProjectInformation) jbiProject.getLookup().
                lookup(ProjectInformation.class);
        String jbiProjName = projInfo.getName();
        FileObject confFO = jbiProject.getProjectDirectory().getFileObject("src/conf"); // NO18N
        FileObject casaFO = confFO.getFileObject(jbiProjName + ".casa");   // NO18N
        
        if (casaFO == null && create) {
            try {
                casaFO = FileUtil.copyFile(
                        Repository.getDefault().getDefaultFileSystem().findResource(
                        "org-netbeans-modules-compapp-projects-jbi/project.casa"), // NOI18N
                        confFO, 
                        jbiProjName);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        return casaFO;
    }
        
    public static void saveCasa(Project jbiProject) {
        FileObject casaFO = getCasaFileObject(jbiProject, false);
        if (casaFO != null) {
            try {
                DataObject casaDO = DataObject.find(casaFO);
                
                SaveCookie saveCookie = 
                        (SaveCookie) casaDO.getCookie(SaveCookie.class);
                if (saveCookie != null) {
                    try {
                        saveCookie.save();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            } catch (DataObjectNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }
}
