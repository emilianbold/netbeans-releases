/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.projectimport.eclipse;

import java.io.File;

/**
 * Serveral helpers for parsing, managing, loading Eclipse projects and
 * workspace metadata.
 *
 * @author mkrauskopf
 */
public class EclipseUtils {
    
    /**
     * Returns whether there is a valid project in the given
     * <code>projectDir</code>.
     */
    public static boolean isRegularProject(File projectDir) {
        return projectDir.isDirectory() &&
                new File(projectDir, EclipseProject.PROJECT_FILE).isFile(); // &&
//                new File(projectDir, EclipseProject.CLASSPATH_FILE).isFile();
    }
    
    /**
     * Returns whether there is a valid workspace in the given
     * <code>workspaceDir</code>.
     */
    public static boolean isRegularWorkSpace(String workspaceDir) {
        return workspaceDir != null &&
                isRegularWorkSpace(new File(workspaceDir));
    }
    
    /**
     * Returns whether there is a valid workspace in the given
     * <code>workspaceDir</code>.
     */
    public static boolean isRegularWorkSpace(File workspaceDir) {
        return workspaceDir != null && workspaceDir.isDirectory() &&
                new File(workspaceDir, Workspace.CORE_PREFERENCE).isFile() &&
                new File(workspaceDir, Workspace.LAUNCHING_PREFERENCES).isFile() &&
                new File(workspaceDir, Workspace.RESOURCE_PROJECTS_DIR).isDirectory();
    }
}
