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
import org.openide.filesystems.FileUtil;

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
    public static boolean isRegularProject(String projectDir) {
        return projectDir != null &&
                isRegularProject(new File(projectDir.trim()));
    }
    
    /**
     * Returns whether there is a valid project in the given
     * <code>projectDir</code>.
     */
    public static boolean isRegularProject(File projectDir) {
        return projectDir != null
                && FileUtil.toFileObject(FileUtil.normalizeFile(projectDir)) != null
                && projectDir.isDirectory()
                && new File(projectDir, EclipseProject.PROJECT_FILE).isFile();
    }
    
    /**
     * Returns whether there is a valid project in the given
     * <code>projectDir</code> and if the project has java nature.
     */
    public static boolean isRegularJavaProject(File projectDir) {
        return isRegularProject(projectDir) &&
                new File(projectDir, EclipseProject.CLASSPATH_FILE).isFile();
    }
    
    /**
     * Returns whether there is a valid project in the given
     * <code>projectDir</code> and if the project has java nature.
     */
    public static boolean isRegularJavaProject(String projectDir) {
        return projectDir != null &&
                isRegularJavaProject(new File(projectDir.trim()));
    }
    
    /**
     * Returns whether there is a valid workspace in the given
     * <code>workspaceDir</code>.
     */
    public static boolean isRegularWorkSpace(String workspaceDir) {
        return workspaceDir != null &&
                isRegularWorkSpace(new File(workspaceDir.trim()));
    }
    
    /**
     * Returns whether there is a valid workspace in the given
     * <code>workspaceDir</code>.
     */
    public static boolean isRegularWorkSpace(File workspaceDir) {
        FileUtil.toFileObject(FileUtil.normalizeFile(workspaceDir));
        return workspaceDir != null
                && FileUtil.toFileObject(FileUtil.normalizeFile(workspaceDir)) != null
                && workspaceDir.isDirectory()
                && new File(workspaceDir, Workspace.CORE_PREFERENCE).isFile()
                && new File(workspaceDir, Workspace.LAUNCHING_PREFERENCES).isFile()
                && new File(workspaceDir, Workspace.RESOURCE_PROJECTS_DIR).isDirectory();
    }
    
    private static final String TMP_NAME =
            "NB___TMP___ENOUGH___UNIQUE___CONSTANT___"; // NOI18N
    
    public static boolean isWritable(String projectDestination) {
        File tmpDir = new File(projectDestination.trim(),
                (TMP_NAME + System.currentTimeMillis()));
        if (tmpDir.mkdirs()) {
            tmpDir.delete();
            return true;
        }
        return false;
    }
}
