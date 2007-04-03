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

package org.netbeans.projectopener;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Milan Kubec
 */
public class SavedProjects {
    
    private Collection/*<SavedProjects.OneProject>*/ savedProjects;
    private Collection/*<ProjectType>*/ projectTypes;
    
    public SavedProjects(List sp, Collection pt) {
        savedProjects = sp;
        projectTypes = pt;
    }
    
    /**
     * Returns sorted array of stored project's paths
     * @param mainPrjPath is the path in the zip file, separator is '/',
     * last folder in the path is considered to be project name
     */
    public String[] getSortedProjectsPaths(String mainPrjPath) {
        String transMainPrjPath = mainPrjPath.replace('/', File.separatorChar);
        String mainPrjName = transMainPrjPath.substring(transMainPrjPath.lastIndexOf(File.separatorChar) + 1);
        String prjPaths[] = new String[savedProjects.size()];
        String lastPrjPath = null;
        int index = 0;
        for (Iterator iter = savedProjects.iterator(); iter.hasNext(); ) {
            SavedProjects.OneProject sp = (SavedProjects.OneProject) iter.next();
            String prjPath = sp.getProjectPath();
            if (prjPath.indexOf(transMainPrjPath) != -1 && 
                    sp.getProjectName().equals(mainPrjName)) {
                lastPrjPath = prjPath;
            } else {
                prjPaths[index++] = prjPath;
            }
        }
        if (lastPrjPath != null) {
            prjPaths[index] = lastPrjPath;
        }
        return prjPaths;
    }
    
    public String[] getProjectPaths() {
        String prjPaths[] = new String[savedProjects.size()];
        int index = 0;
        for (Iterator iter = savedProjects.iterator(); iter.hasNext(); ) {
            SavedProjects.OneProject sp = (SavedProjects.OneProject) iter.next();
            prjPaths[index++] = sp.getProjectPath();
        }
        return prjPaths;
    }
    
    public Collection/*<ProjectType>*/ getTypes() {
        return projectTypes;
    }
    
    public static class OneProject {
        
        private File folder;
        
        public OneProject(File dir) {
            folder = dir;
        }
        
        public String getProjectName() {
            return folder.getName();
        }
        
        public String getProjectPath() {
            return folder.getAbsolutePath();
        }
        
        public String toString() {
            return folder.getAbsolutePath();
        }
        
    }
    
}
