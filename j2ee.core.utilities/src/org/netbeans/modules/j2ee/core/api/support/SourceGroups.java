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

package org.netbeans.modules.j2ee.core.api.support;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 * This class consists of various static utility methods for working with 
 * <code>SourceGroup</code>s.
 * 
 * @author Andrei Badea, Erno Mononen
 */
public final class SourceGroups {

    private static final Logger LOGGER = Logger.getLogger(SourceGroups.class.getName());
    
    private SourceGroups() {
    }

    /**
     * Gets the Java source groups of the given <code>project</code>.
     * 
     * @param project the project whose source groups are to be get.
     * @return the Java source groups of the given <code>project</code>, 
     * <strong>excluding</strong> test source groups.
     */ 
    public static SourceGroup[] getJavaSourceGroups(Project project) {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
        Set<SourceGroup> testGroups = getTestSourceGroups(sourceGroups);
        List<SourceGroup> result = new ArrayList<SourceGroup>();
        for(SourceGroup sourceGroup : sourceGroups){
            if (!testGroups.contains(sourceGroup)) {
                result.add(sourceGroup);
            }
        }
        return result.toArray(new SourceGroup[result.size()]);
    }

    /**
     * Checks whether the folder identified by the given <code>packageName</code> is
     * writable or is in a writable parent directory but does not exist yet.
     * 
     * @param sourceGroup
     * @param packageName the package to check.
     * @return true if the folder is writable or can be created, false otherwise.
     */ 
    public static boolean isFolderWritable(SourceGroup sourceGroup, String packageName) {
        try {
            FileObject fo = getFolderForPackage(sourceGroup, packageName, false);

            while ((fo == null) && (packageName.lastIndexOf('.') != -1)) {
                packageName = packageName.substring(0, packageName.lastIndexOf('.'));
                fo = getFolderForPackage(sourceGroup, packageName, false);
            }
            return ((fo == null) || fo.canWrite());
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return false;
        }
    }

    /**
     * Gets the <code>SourceGroup</code> of the given <code>folder</code>.
     * 
     * @param sourceGroups the source groups to search.
     * @param folder the folder whose source group is to be get.
     * @return the source group containing the given <code>folder</code> or 
     * null if not found.
     */ 
    public static SourceGroup getFolderSourceGroup(SourceGroup[] sourceGroups, FileObject folder) {
        for (int i = 0; i < sourceGroups.length; i++) {
            if (FileUtil.isParentOf(sourceGroups[i].getRootFolder(), folder)) {
                return sourceGroups[i];
            }
        }
        return null;
    }

    /**
     * Converts the path of the given <code>folder</code> to a package name.
     * 
     * @param sourceGroup the source group for the folder.
     * @param folder the folder to convert.
     * @return the package name of the given <code>folder</code>.
     */ 
    public static String getPackageForFolder(SourceGroup sourceGroup, FileObject folder) {
        String relative = FileUtil.getRelativePath(sourceGroup.getRootFolder(), folder);
        if (relative != null) {
            return relative.replace('/', '.'); // NOI18N
        } else {
            return ""; // NOI18N
        }
    }

    /**
     * Gets the folder representing the given <code>packageName</code>. If the
     * folder does not exists, it will be created.
     * 
     * @param sourceGroup the source group of the package.
     * @param packageName the name of the package.
     * @return the folder representing the given package.
     */
    public static FileObject getFolderForPackage(SourceGroup sourceGroup, String packageName) throws IOException {
        return getFolderForPackage(sourceGroup, packageName, true);
    }

    /**
     * Gets the folder representing the given <code>packageName</code>.
     * 
     * @param sourceGroup the source group of the package.
     * @param packageName the name of the package.
     * @param create specifies whether the folder should be created if it does not exist.
     * @return the folder representing the given package or null if it was not found.
     */
    public static FileObject getFolderForPackage(SourceGroup sourceGroup, String pgkName, boolean create) throws IOException {
        String relativePkgName = pgkName.replace('.', '/');
        FileObject folder = sourceGroup.getRootFolder().getFileObject(relativePkgName);
        if (folder != null) {
            return folder;
        } else if (create) {
            return FileUtil.createFolder(sourceGroup.getRootFolder(), relativePkgName);
        }
        return null;
    }

    /**
     * Gets the <code>SourceGroup</code> of the given <code>project</code> which contains the
     * given <code>fqClassName</code>.
     * 
     * @param project the project
     * @param fqClassName the fully qualified name of the class whose source group is to be get.
     * @return the source group containing the given <code>fqClassName</code>.
     */
    public static SourceGroup getClassSourceGroup(Project project, String fqClassName) {
        String classFile = fqClassName.replace('.', '/') + ".java"; // NOI18N
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        
        for (SourceGroup sourceGroup : sourceGroups) {
            FileObject classFO = sourceGroup.getRootFolder().getFileObject(classFile);
            if (classFO != null) {
                return sourceGroup;
            }
        }
        return null;
    }
    
    private static Map<FileObject, SourceGroup> createFoldersToSourceGroupsMap(final SourceGroup[] sourceGroups) {
        if (sourceGroups.length == 0) {
            return Collections.<FileObject, SourceGroup>emptyMap();
        } 
        Map<FileObject, SourceGroup> result = new HashMap<FileObject, SourceGroup>(2 * sourceGroups.length, .5f);
        for (SourceGroup sourceGroup : sourceGroups){
            result.put(sourceGroup.getRootFolder(), sourceGroup);
        }
        return result;
    }

    private static Set<SourceGroup> getTestSourceGroups(SourceGroup[] sourceGroups) {
        Map<FileObject, SourceGroup> foldersToSourceGroupsMap = createFoldersToSourceGroupsMap(sourceGroups);
        Set<SourceGroup> testGroups = new HashSet<SourceGroup>();
        for (int i = 0; i < sourceGroups.length; i++) {
            testGroups.addAll(getTestTargets(sourceGroups[i], foldersToSourceGroupsMap));
        }
        return testGroups;
    }

    private static List<SourceGroup> getTestTargets(SourceGroup sourceGroup, Map foldersToSourceGroupsMap) {
        final URL[] rootURLs = UnitTestForSourceQuery.findUnitTests(sourceGroup.getRootFolder());
        if (rootURLs.length == 0) {
            Collections.<SourceGroup>emptyList();
        }
        List<SourceGroup> result = new ArrayList<SourceGroup>();
        for (FileObject sourceRoot : getFileObjects(rootURLs)){
            SourceGroup srcGroup = (SourceGroup) foldersToSourceGroupsMap.get(sourceRoot);
            if (srcGroup != null) {
                result.add(srcGroup);
            }
        }
        return result;
    }

    private static List<FileObject> getFileObjects(URL[] urls) {
        List<FileObject> result = new ArrayList<FileObject>();
        for (int i = 0; i < urls.length; i++) {
            FileObject sourceRoot = URLMapper.findFileObject(urls[i]);
            if (sourceRoot != null) {
                result.add(sourceRoot);
            } else {
                LOGGER.log(Level.INFO, null, new IllegalStateException("No FileObject found for the following URL: " + urls[i]));
            }
        }
        return result;
    }
}
