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
import java.net.URL;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

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
    
    static FileObject getSrcRoot(Project project) {
        Sources sources = ProjectUtils.getSources(project);
        
        //PENDING:
        // - what about other types of projects?
        SourceGroup[] sourceGroups = sources.getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
        
        //PENDING:
        // - what if the array is empty?
        // - is it OK to return the first element if there are more?
        return sourceGroups[0].getRootFolder();
    }
    
    static FileObject getTestsRoot(Project project) {
        
        //PENDING:
        // - getSrcRoot() returns at most one source root
        //    - what if there are more source roots?
        //    - what if there is no source root?
        FileObject srcRoot = getSrcRoot(project);
        URL testRootURL = UnitTestForSourceQuery.findUnitTest(srcRoot);
        
        //PENDING:
        // - what if the URL is null?
        // - what if the returned FileObject is null?
        return URLMapper.findFileObject(testRootURL);
    }
    
}
