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

package org.netbeans.modules.projectimport.eclipse;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
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
    
    /**
     * Load properties from a given <code>file</code>.
     * <p>
     * <strong>Note: package private for unit tests only.</strong>
     * 
     * @throws IOException when reading file failed
     */
    static Properties loadProperties(File file) throws IOException {
        InputStream propsIS = new BufferedInputStream(new FileInputStream(file));
        Properties properties = new Properties();
        try {
            properties.load(propsIS);
        } finally {
            propsIS.close();
        }
        return properties;
    }
    
}
