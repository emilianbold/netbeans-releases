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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Utilities;

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
    
    static Collection getSourceSourceGroups(Project project) {
        
        /* 1) get all source groups: */
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] sourceGroups = sources.getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
        
        if (sourceGroups.length == 0) {
            return Collections.EMPTY_LIST;
        }
        
        /* 2) in the beginning consider all source groups to contain sources: */
        Collection srcSourceGroups = new HashSet(sourceGroups.length * 2, .5f);
        srcSourceGroups.addAll(Arrays.asList(sourceGroups));
        
        /* 3) find test SourceGroups and remove them from src. Source Groups: */
        /* test SourceGroups are those that are recognized as test root folders
           for at least one SourceGroup: */
        Iterator i = new ArrayList(Arrays.asList(sourceGroups)).iterator();
        while (i.hasNext()) {
            FileObject rootFolder = ((SourceGroup) i.next()).getRootFolder();
            URL testRootURL = UnitTestForSourceQuery.findUnitTest(rootFolder);
            if (testRootURL != null) {
                FileObject testFolder = URLMapper.findFileObject(testRootURL);
                if (testFolder != null) {
                    SourceGroup testSourceGroup = findSourceGroup(sourceGroups,
                                                                  testFolder);
                    if (testSourceGroup != null) {
                        srcSourceGroups.remove(testSourceGroup);
                    }
                }
            }
        }
        
        if (srcSourceGroups.size() == 0) {
            return Collections.EMPTY_LIST;
        }
        List result = new ArrayList(srcSourceGroups);
        return Collections.unmodifiableList(result);
    }
    
    static Collection getTestSourceGroups(Project project) {
        
        Collection col = getSourceGroupPairs(project);
        Iterator it = col.iterator();
        Collection ret = new ArrayList(col.size());
        while (it.hasNext()) {
            SourceGroup [] pair = (SourceGroup [])it.next();
            ret.add(pair[1]);
        }

        return ret;
    }

    private static Map getFolder2SourceGroupMap(SourceGroup[] sourceGroups) {
        if (sourceGroups.length == 0) {
            return Collections.EMPTY_MAP;
        }
        
        Map map = new HashMap(2 * sourceGroups.length, .5f);
        for (int i = 0; i < sourceGroups.length; i++) {
            SourceGroup sourceGroup = sourceGroups[i];
            map.put(sourceGroup.getRootFolder(), sourceGroup);
        }
        return map;
    }
    
    static Collection getSourceGroupPairs(Project project) {
        
        /* 1) get all source groups: */
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] sourceGroups = sources.getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
        
        if (sourceGroups.length == 0) {
            return Collections.EMPTY_LIST;
        }
        
        Map folder2sourceGroup = getFolder2SourceGroupMap(sourceGroups);
        Collection pairs = new ArrayList(sourceGroups.length / 2);
        Set processed = new HashSet(sourceGroups.length * 2, .5f);
        for (int i = 0; i < sourceGroups.length; i++) {
            SourceGroup sourceGroup = sourceGroups[i];
            if (processed.contains(sourceGroup)) {
                continue;
            }
            FileObject rootFolder = sourceGroup.getRootFolder();
            FileObject opposite;
            URL oppositeURL;
            
            boolean reverse = false;
            SourceGroup oppositeSourceGroup = null;            

            oppositeURL = UnitTestForSourceQuery.findUnitTest(rootFolder);
            if (oppositeURL == null) {
                reverse = true;
                oppositeURL = UnitTestForSourceQuery.findSource(rootFolder);
            } 

            if (oppositeURL != null) {
                //PENDING - more checks should be performed
                opposite = URLMapper.findFileObject(oppositeURL);
                if (opposite != null) {
                    oppositeSourceGroup =(SourceGroup)folder2sourceGroup.get(opposite);
                }
            } 
            
            if (oppositeSourceGroup == null) {
                oppositeSourceGroup = sourceGroup;
            }


            if (sourceGroup!=null && oppositeSourceGroup != null) {
                pairs.add(new SourceGroup[] {
                    reverse ? oppositeSourceGroup : sourceGroup,
                    reverse ? sourceGroup : oppositeSourceGroup});
            }
            processed.add(oppositeSourceGroup);

        }

        if (pairs.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        List result = new ArrayList(pairs);
        return Collections.unmodifiableList(result);
    }
    
    /**
     * Finds a <code>SourceGroup</code> having the specified root folder.
     * If there are more <code>SourceGroup</code>s matching, the first one
     * (according to the order of elements in the array) is returned.
     *
     * @param  sourceGroups  source groups to test
     * @param  rootFolder  root folder of a source group to be found
     * @return  the found <code>SourceGroup</code>;
     *          or <code>null</code> if no matching <code>SourceGroup</code>
     *          was found
     */
    private static SourceGroup findSourceGroup(SourceGroup[] sourceGroups,
                                               FileObject rootFolder) {
        for (int i = 0; i < sourceGroups.length; i++) {
            if (sourceGroups[i].getRootFolder().equals(rootFolder)) {
                return sourceGroups[i];
            }
        }
        return (SourceGroup) null;
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
    
    static boolean isValidClassName(String className) {
        if (className.length() == 0) {
            return false;
        }
        char[] chars = className.toCharArray();
        int segmentStart = 0;
        int i;
        for (i = 0; i < chars.length; i++) {
            if (chars[i] == '.') {
                if (i == segmentStart) {
                    return false;         //empty segment
                }
                if (!Utilities.isJavaIdentifier(
                        className.substring(segmentStart, i))) {
                    return false;         //illegal name of the segment
                }
                segmentStart = i + 1;
            }
        }
        if (i == segmentStart) {
            return false;                 //empty last segment
        }
        if (!Utilities.isJavaIdentifier(
                className.substring(segmentStart, chars.length))) {
            return false;                 //illegal name of the last segment
        }
        return true;
    }
    
}
