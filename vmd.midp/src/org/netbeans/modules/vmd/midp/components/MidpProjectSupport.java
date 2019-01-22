/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
import org.openide.util.Lookup;

import java.io.IOException;
import java.util.*;

/**
 *
 * 
 */
public final class MidpProjectSupport {
    
    private static final Lookup.Result<ProjectResourceResolver> resolvers = Lookup.getDefault().lookupResult(ProjectResourceResolver.class);
    
    /** Creates a new instance of MidpProjectSupport */
    private MidpProjectSupport() {
    }
    
    public static Collection<? extends ProjectResourceResolver> getAllResolvers() {
        return resolvers.allInstances();
    }
    
    /**
     * Add library to the project based on the supplied names
     * @param document the document
     * @param libraryNames the library names to be added
     */
    public static void addLibraryToProject(final DesignDocument document, final String... libraryNames) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                final Project project = getProjectForDocument(document);
                if (project == null)
                    return;
                ProjectClassPathExtender extender = project.getLookup().lookup(ProjectClassPathExtender.class);
                final LibraryManager libraryManager = LibraryManager.getDefault();
                for (String libraryName : libraryNames) {
                    final Library library = libraryManager.getLibrary(libraryName);
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
     * Add library to the project based on the supplied archive files
     * @param document the document
     * @param archiveFiles the archive files to be added
     */
    public static void addArchiveFileToProject(final DesignDocument document, final FileObject... archiveFiles) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                final Project project = getProjectForDocument(document);
                if (project == null)
                    return;
                ProjectClassPathExtender extender = project.getLookup().lookup(ProjectClassPathExtender.class);
                for (FileObject file : archiveFiles) {
                    if (file == null  ||  ! file.isValid())
                        continue;
                    try {
                        extender.addArchiveFile(file);
                    } catch (IOException e) {
                        Debug.warning(e);
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
    public static Project getProjectForDocument(DesignDocument document) {
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
     * @param document the document
     * @param relativeResourcePath as seen from a MIDlet prespective must start with
     * @return a map of found file-objects
     */
    public static Map<FileObject, FileObject> getFileObjectsForRelativeResourcePath(DesignDocument document, String relativeResourcePath) {
        assert (document != null);
        assert (relativeResourcePath != null);
        
        if (relativeResourcePath.startsWith("/")) { // NOI18N
            relativeResourcePath = relativeResourcePath.substring(1);
        }

        // Fix for 144950 - [65cat] java.util.NoSuchElementException at java.util.WeakHashMap$HashIterator.nextEntry
        Map<FileObject, FileObject> matches = new HashMap<FileObject, FileObject>();
        
        DataObjectContext context = ProjectUtils.getDataObjectContextForDocument(document);
        // document is not leaded yet
        if (context == null) {
            return Collections.emptyMap ();
        }
        
        DataObject dataObject = context.getDataObject();
        
        assert (dataObject != null);
        
        FileObject primaryFile = dataObject.getPrimaryFile();
        
        assert (primaryFile != null);
        
        Project project = getProjectForDocument(document);
        if (project == null || primaryFile == null || !primaryFile.isValid()) {
            return new HashMap<FileObject, FileObject>(0);
        }
        List<ClassPath> classPathList = getClassPath(project, primaryFile);
        
        assert(classPathList != null);
        
        for (ClassPath cp : classPathList) {
            FileObject[] roots = cp.getRoots();
            for (FileObject root : roots) {
                Enumeration<? extends FileObject> children = root.getChildren(true);
                while (children.hasMoreElements()) {
                    FileObject child = children.nextElement();
                    String curRelPath = FileUtil.getRelativePath(root, child);
                    if (relativeResourcePath.equals(curRelPath)) {
                        matches.put(child, root);
                    }
                }
            }
        }
        
        for (ProjectResourceResolver resolver : resolvers.allInstances()) {
            Collection<FileObject> collection = resolver.getResourceRoots(project, document.getDocumentInterface().getProjectType());
            if (collection == null)
                continue;
            for (FileObject root : collection) {
                Enumeration<? extends FileObject> enumeration = root.getChildren(true);
                while (enumeration.hasMoreElements()) {
                    FileObject object = enumeration.nextElement();
                    String curRelPath = FileUtil.getRelativePath(root, object);
                    if (relativeResourcePath.equals(curRelPath))
                        matches.put(object, root);
                }
            }
        }
        
        return matches;
    }
    
    
    /**
     * Returns a Map of all images in the project keyed by FileObjects with their relative resource paths as values.
     * @param document the document
     * @return the map of found file-objects
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
     * @param document the document
     * @return the map of found file-objects
     */
    public static Map<FileObject, String> getAllFilesForProjectByExt(DesignDocument document, Collection<String> fileExtensions) {
        assert (fileExtensions != null);
        assert (document != null);
        
        Map<FileObject, String> matches = new WeakHashMap<FileObject, String>();
        
        DataObjectContext context = ProjectUtils.getDataObjectContextForDocument(document);
        if (context == null) {
            return Collections.EMPTY_MAP;
        }
        
        DataObject dataObject = context.getDataObject();
        if (dataObject == null) {
            return Collections.EMPTY_MAP;
        }
        
        FileObject primaryFile = dataObject.getPrimaryFile();
        if (primaryFile == null) {
            return Collections.EMPTY_MAP;
        }
        
        Project project = getProjectForDocument(document);
        List<ClassPath> classPathList = getClassPath(project, primaryFile);
        if (classPathList == null) {
            return Collections.EMPTY_MAP;
        }
        
        for (ClassPath cp : classPathList) {
            FileObject[] roots = cp.getRoots();
            for (FileObject root : roots) {
                //fill the map <FileObject, String relativePath>
                extractFiles(root, root, matches, fileExtensions);
            }
        }
        
        for (ProjectResourceResolver resolver : resolvers.allInstances()) {
            Collection<FileObject> collection = resolver.getResourceRoots(project, document.getDocumentInterface().getProjectType());
            if (collection == null)
                continue;
            for (FileObject root : collection)
                extractFiles(root, root, matches, fileExtensions);
        }
        
        return matches;
    }
    
    /**
     * Recurses directories looking for files with given extensions. Case insensitive
     */
    private static void extractFiles(FileObject root, FileObject current, final Map<FileObject, String> bank, Collection<String> imgFileExtensions) {
        if (current.isFolder()) {
            FileObject[] children = current.getChildren();
            
            for (FileObject fo : children) {
                extractFiles(root, fo, bank, imgFileExtensions);
            }
        } else {
            String currentExt = current.getExt();
            for(String ext : imgFileExtensions) {
                if (ext.equalsIgnoreCase(currentExt)) {
                    String relativePath = FileUtil.getRelativePath(root, current);
                    bank.put(current, "/" + relativePath); // NOI18N
                }
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
        //Removed because of low performance
        //classPathList.add(cpp.findClassPath(fileObject, ClassPath.BOOT));
        classPathList.add(cpp.findClassPath(fileObject, ClassPath.COMPILE));
        classPathList.add(cpp.findClassPath(fileObject, ClassPath.SOURCE));
        return classPathList;
    }
    
    public static ClasspathInfo getClasspathInfo(Project project) {
        SourceGroup group = getSourceGroup(project);
        if (group == null)
            return null;
        FileObject fileObject = group.getRootFolder();
        return ClasspathInfo.create(fileObject);
    }
    
    public static SourceGroup getSourceGroup(Project project) {
        SourceGroup[] sourceGroups = org.netbeans.api.project.ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sourceGroups == null || sourceGroups.length < 1)
            return null;
        return sourceGroups[0];
    }
    
}
