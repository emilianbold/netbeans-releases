/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit.wizards;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author  Marian Petras
 */
public class Utils {
    
    private Utils() { }
    
    /** */
    static FileObject findTestsRoot(Project project) {
        SourceGroup[] sourceGroups
                = ProjectUtils.getSources(project)
                  .getSourceGroups(EmptyTestCaseWizard.JAVA_SOURCE_GROUPS);
        for (int i = 0; i < sourceGroups.length; i++) {
            FileObject root = sourceGroups[i].getRootFolder();
            if (root.getName().equals(EmptyTestCaseWizard.TESTS_ROOT_NAME)) {
                return root;
            }
        }
        return null;
    }
    
    /** */
    static FileObject getPackageFolder(
            FileObject root,
            String pkgName) throws IOException {
        String relativePathName = pkgName.replace('.', '/');
        FileObject folder = root.getFileObject(relativePathName);
        if (folder == null) {
            folder = FileUtil.createFolder(root, relativePathName);
        }
        return folder;
    }
    
}
