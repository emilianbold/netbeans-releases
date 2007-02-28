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

package org.netbeans.modules.vmd.midp.components;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;

import java.io.IOException;
import java.util.*;

/**
 *
 * @author Karol Harezlak
 */
public final class MidpProjectSupport {
    
    /** Creates a new instance of MidpProjectSupport */
    private MidpProjectSupport() {
    }
    
    /**
     * Add library to the project based on the supplied names
     * @param document
     * @param libraryNames
     */
    public static void addLibraryToProject(final DesignDocument document, final String[] libraryNames) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                if (libraryNames == null || libraryNames.length <= 0)
                    return;
                final Project project = getProjectForDocument(document);
                if (project == null)
                    return;
                ProjectClassPathExtender extender = project.getLookup().lookup(ProjectClassPathExtender.class);
                final LibraryManager libraryManager = LibraryManager.getDefault();
                for (int i = 0; i < libraryNames.length; i++) {
                    final Library library = libraryManager.getLibrary(libraryNames[i]);
                    if (library != null) {
                        try {
                            extender.addLibrary(library);
                        } catch (IOException e) {
                            Debug.warning(e);
                        }
                    }
                }
            }
        });
    }
    
    /**
     * Gets project for document.
     * @param document the document
     * @return the project
     */
    private static Project getProjectForDocument(DesignDocument document) {
        if (document == null)
            return null;
        
        DataObjectContext context = ProjectUtils.getDataObjectContextForDocument(document);
        if (context == null)
            return null;
        
        return ProjectUtils.getProject(context);
    }
    
    /**
     * Returns a Map keyed by a FileObject matching the relative resource path
     * while the value is the FileObject representing the classpath root containing
     * the key FileObject.
     * @param relativeResourcePath as seen from a MIDlet prespective must start with
     */
    public static Map<FileObject, FileObject> getFileObjectsForRelativeResourcePath(DesignDocument document, String relativeResourcePath) {
        assert (document != null);
        assert (relativeResourcePath != null);
        
        if (relativeResourcePath.startsWith("/")) { // NOI18N
            relativeResourcePath = relativeResourcePath.substring(1);
        }
        
        Map<FileObject, FileObject> matches = new HashMap<FileObject, FileObject>();
        
        DataObjectContext context = ProjectUtils.getDataObjectContextForDocument(document);
        DataObject dataObject = context.getDataObject();
        
        assert (dataObject != null);
        
        FileObject primaryFile = dataObject.getPrimaryFile();
        
        assert (primaryFile != null);
        
        Project project = getProjectForDocument(document);
        List<ClassPath> classPathList = getClassPath(project, primaryFile);
        
        assert(classPathList != null);
        
        for (ClassPath cp : classPathList) {
            FileObject[] roots = cp.getRoots();
            for (int k = 0; k < roots.length; k++) {
                Enumeration<? extends FileObject> children = roots[k].getChildren(true);
                while (children.hasMoreElements()) {
                    FileObject child = children.nextElement();
                    String curRelPath = FileUtil.getRelativePath(roots[k], child);
                    if (relativeResourcePath.equals(curRelPath)) {
                        matches.put(child, roots[k]);
                    }
                }
            }
        }
        return matches;
    }
    
    
    /**
     * Returns a Map of all images in the project keyed by FileObjects with their relative resource paths as values.
     */
    public static Map<FileObject, String> getImagesForProject(DesignDocument document, boolean pngOnly) {
        String EXTENSION_JPEG = "jpeg"; // NOI18N
        String EXTENSION_JPG = "jpg"; // NOI18N
        String EXTENSION_GIF = "gif"; // NOI18N
        String EXTENSION_PNG = "png"; // NOI18N
        
        assert (document != null);
        
        Map<FileObject, String> imageFileObjects = null;
        if (pngOnly) {
            imageFileObjects = getAllFilesForProjectByExt(document, Arrays.asList(
                    EXTENSION_PNG
                    ));
        } else {
            imageFileObjects = getAllFilesForProjectByExt(document, Arrays.asList(
                    EXTENSION_JPEG,
                    EXTENSION_JPG,
                    EXTENSION_GIF,
                    EXTENSION_PNG
                    ));
        }
        return imageFileObjects;
    }
    
    /**
     * Returns a Map of all files matching any of the provided file edxtensions
     * keyed by FileObjects with their relative resource paths as values.
     */
    public static Map<FileObject, String> getAllFilesForProjectByExt(DesignDocument document, Collection<String> fileExtensions) {
        assert (fileExtensions != null);
        assert (document != null);
        
        Map<FileObject, String> matches = new HashMap<FileObject, String>();
        
        DataObjectContext context = ProjectUtils.getDataObjectContextForDocument(document);
        DataObject dataObject = context.getDataObject();
        
        assert (dataObject != null);
        
        FileObject primaryFile = dataObject.getPrimaryFile();
        
        assert (primaryFile != null);
        
        Project project = getProjectForDocument(document);
        List<ClassPath> classPathList = getClassPath(project, primaryFile);
        
        assert (classPathList != null);
        
        for (ClassPath cp : classPathList) {
            FileObject[] roots = cp.getRoots();
            for (int k = 0; k < roots.length; k++) {
                //fill the map <FileObject, String relativePath>
                extractFiles(roots[k], roots[k], matches, fileExtensions);
            }
        }
        return matches;
    }
    
    /**
     * Recurses directories looking for files with given extensions. Extensions must be in lowercase.
     */
    private static void extractFiles(FileObject root, FileObject current, final Map<FileObject, String> bank, Collection imgFileExtensions) {
        if (current.isFolder()) {
            FileObject[] children = current.getChildren();
            for (int i = 0; i < children.length; i++) {
                extractFiles(root, children[i], bank, imgFileExtensions);
            }
        } else {
            String ext = current.getExt();
            if (imgFileExtensions.contains(ext)) {
                String relativePath = FileUtil.getRelativePath(root, current);
                bank.put(current, "/" + relativePath); // NOI18N
                if (false) System.out.println(current.getPath() + " -> " + "/" + relativePath); // NOI18N
            }
        }
    }
    
    /**
     * Gets classpath for given project and fileobject.
     * @param project the project
     * @param fileObject the file object within the project
     * @return the list of classpaths
     */
    private static List<ClassPath> getClassPath(Project project, FileObject fileObject) {
        ArrayList<ClassPath> classPathList = new ArrayList<ClassPath>();
        ClassPathProvider cpp = project.getLookup().lookup(ClassPathProvider.class);
        classPathList.add(cpp.findClassPath(fileObject, ClassPath.BOOT));
        classPathList.add(cpp.findClassPath(fileObject, ClassPath.COMPILE));
        classPathList.add(cpp.findClassPath(fileObject, ClassPath.SOURCE));
        return classPathList;
    }
    
    public static ClasspathInfo getClasspathInfo(Project project) {
        SourceGroup group = getSourceGroup (project);
        if (group == null)
            return null;
        FileObject fileObject = group.getRootFolder ();
        return ClasspathInfo.create (fileObject);
    }

    public static SourceGroup getSourceGroup (Project project) {
        SourceGroup[] sourceGroups = org.netbeans.api.project.ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sourceGroups == null || sourceGroups.length < 1)
            return null;
        return sourceGroups[0];
    }

}
