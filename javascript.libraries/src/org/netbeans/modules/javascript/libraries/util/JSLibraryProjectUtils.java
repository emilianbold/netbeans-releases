/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.libraries.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryChooser;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.javascript.libraries.api.JavaScriptLibrarySupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Quy Nguyen <quynguyen@netbeans.org>
 */
public final class JSLibraryProjectUtils {
    private static final String WEB_PROJECT = "org.netbeans.modules.web.project.WebProject"; // NOI18N
    private static final String WEB_PROJECT_DEFAULT_RELATIVE_PATH = "/web/resources"; // NOI18N
    private static final String RUBY_PROJECT = "org.netbeans.modules.ruby.rubyproject.RubyProject"; // NOI18N
    private static final String RUBY_PROJECT_DEFAULT_RELATIVE_PATH = "/public/resources"; // NOI18N
    private static final String PHP_PROJECT = "org.netbeans.modules.php.project"; // NOI18N
    private static final String OTHER_PROJECT_DEFAULT_RELATIVE_PATH = "/javascript/resources"; // NOI18N

    private static final String LIBRARY_LIST_PROP = "javascript-libraries"; // NOI18N
    
    public static LibraryChooser.Filter createDefaultFilter() {
        return new LibraryChooser.Filter() {
            public boolean accept(Library library) {
                return library.getType().equals("javascript"); // NOI18N
            }
        };
    }
    
    public static LibraryChooser.Filter createDefaultFilter(final Set<Library> excludedLibraries) {
        return new LibraryChooser.Filter() {
            public boolean accept(Library library) {
                return library.getType().equals("javascript") && // NOI18N
                        !excludedLibraries.contains(library);
            }
        };
    }
    
    public static LibraryManager getLibraryManager(Project project) {
        JavaScriptLibrarySupport projectSupport = project.getLookup().lookup(JavaScriptLibrarySupport.class);
        LibraryManager manager = null;
        
        if (projectSupport != null) {
            manager = projectSupport.getLibraryManager();
        }
        
        return (manager == null) ? LibraryManager.getDefault() : manager;
    }
    
    public static LibraryChooser.LibraryImportHandler getSharedLibraryHandler(Project project) {
        JavaScriptLibrarySupport projectSupport = project.getLookup().lookup(JavaScriptLibrarySupport.class);
        LibraryChooser.LibraryImportHandler importHandler = null;
        
        if (projectSupport != null) {
            importHandler = projectSupport.getSharedLibraryImportHandler();
        }
        
        return importHandler;
    }    
    
    public static List<Library> getJSLibraries(Project project) {
        Set<String> libNames = getJSLibraryNames(project);
        List<Library> libraryList = new ArrayList<Library>();
        LibraryManager manager = getLibraryManager(project);
        
        for (String libName : libNames) {
            Library lib = manager.getLibrary(libName);
            if (lib == null) {
                // XXX unresolved reference?
            } else {
                libraryList.add(lib);
            }            
        }
        
        return libraryList;
    }
    
    public static String getJSLibrarySourcePath(Project project) {
        JavaScriptLibrarySupport projectSupport = project.getLookup().lookup(JavaScriptLibrarySupport.class);
        
        if (projectSupport != null) {
            return projectSupport.getJavaScriptLibrarySourcePath();
        } else {
            return getDefaultSourcePath(project);
        }
        
    }
    
    public static void addJSLibraries(final Project project, final Library... libraries) {
        modifyJSLibraries(project, false, libraries);
    }
    
    public static void removeJSLibraries(Project project, Library... libraries) {
        modifyJSLibraries(project, true, libraries);
    }

    public static void setJSLibraries(final Project project, final Library... libraries) {
        ProjectManager.mutex().writeAccess(
                new Runnable() {
                    public void run() {
                        Preferences prefs = ProjectUtils.getPreferences(project, JSLibraryProjectUtils.class, true);
                        assert prefs != null;

                        StringBuffer propValue = new StringBuffer();
                        for (Library library : libraries) {
                            String name = library.getName();
                            
                            if (propValue.length() == 0) {
                                propValue.append(name);
                            } else {
                                propValue.append(";");
                                propValue.append(name);
                            }
                        }

                        prefs.put(LIBRARY_LIST_PROP, propValue.toString());
                        try {
                            prefs.flush();
                        } catch (BackingStoreException ex) {
                            Log.getLogger().log(Level.SEVERE, "Could not write to project preferences", ex);
                        }


                    }
                }
        
        );
    }

    private static String getDefaultSourcePath(Project project) {
        Project p = project.getLookup().lookup(Project.class);
        if (p == null) {
            // XXX this really shouldn't happen
            Log.getLogger().warning("project.getLookup().lookup(Project.class) returned null for project: " + project);
            p = project;
        }
        
        String projectClassName = p.getClass().getName();
        if (projectClassName.startsWith(PHP_PROJECT)) {
            return WEB_PROJECT_DEFAULT_RELATIVE_PATH;
        } else if (projectClassName.equals(WEB_PROJECT)) {
            return WEB_PROJECT_DEFAULT_RELATIVE_PATH;
        } else if (projectClassName.equals(RUBY_PROJECT)) {
            return RUBY_PROJECT_DEFAULT_RELATIVE_PATH;
        } else {
            return OTHER_PROJECT_DEFAULT_RELATIVE_PATH;
        }
    }
    
    private static void modifyJSLibraries(final Project project, final boolean remove, final Library... libraries) {
        final Set<String> libNames = getJSLibraryNames(project);
        
        ProjectManager.mutex().writeAccess(
                new Runnable() {
                    public void run() {
                        Preferences prefs = ProjectUtils.getPreferences(project, JSLibraryProjectUtils.class, true);
                        assert prefs != null;
                        
                        boolean modified = false;
                        for (Library library : libraries) {
                            String name = library.getName();
                            
                            if (remove && libNames.contains(name)) {
                                modified = true;
                                libNames.remove(name);
                            } else if (!remove && !libNames.contains(name)) {
                                modified = true;
                                libNames.add(name);
                            }
                        }
                        
                        if (modified) {
                            StringBuffer propValue = new StringBuffer();
                            for (String name : libNames) {
                                if (propValue.length() == 0) {
                                    propValue.append(name);
                                } else {
                                    propValue.append(";");
                                    propValue.append(name);
                                }
                            }
                            
                            prefs.put(LIBRARY_LIST_PROP, propValue.toString());
                            try {
                                prefs.flush();
                            } catch (BackingStoreException ex) {
                                Log.getLogger().log(Level.SEVERE, "Could not write to project preferences", ex);
                            }
                        }
                        
                    }
                }
        
        );
    }
    
    private static Set<String> getJSLibraryNames(Project project) {
        Preferences prefs = ProjectUtils.getPreferences(project, JSLibraryProjectUtils.class, true);
        assert prefs != null;
        
        String libraries = prefs.get(LIBRARY_LIST_PROP, "");
        String[] tokens = libraries.split(";");
        
        Set<String> librarySet = new LinkedHashSet<String>();
        for (String token : tokens) {
            librarySet.add(token);
        }
        
        return librarySet;
    }
    
    // TODO Rewrite copy/delete to intelligently detect overlapping files
    public static boolean extractLibraryToProject(Project project, Library library) {
        String relativePath = getJSLibrarySourcePath(project);
        return extractLibrary(project, library, relativePath);
    }
    
    public static boolean deleteLibraryFromProject(Project project ,Library library) {
        // currently no-op
        return true;
    }
    
    public static boolean isLibraryFolderEmpty(Project project, Library library) {
        String relativePath = getJSLibrarySourcePath(project);
        return isLibraryFolderEmpty(project, library, relativePath);        
    }
    
    public static boolean isLibraryFolderEmpty(Project project, Library library, String relativePath) {
        relativePath = relativePath + "/" + library.getName().replaceAll(" ", "_");
        FileObject fo = project.getProjectDirectory().getFileObject(relativePath);

        return fo == null || fo.getChildren().length > 0;
    }

    public static boolean extractLibrary(Project project, Library library, String relativePath) {
        try {
            relativePath = relativePath + "/" + library.getName().replaceAll(" ", "_");
            FileObject jsFolder = FileUtil.createFolder(project.getProjectDirectory(), relativePath);

            for (URL url : library.getContent("scriptpath")) {
                URL archiveURL = FileUtil.isArchiveFile(url) ? url : FileUtil.getArchiveFile(url);
                FileObject archiveFO = URLMapper.findFileObject(archiveURL);
                
                extractZip(FileUtil.toFile(jsFolder), FileUtil.toFile(archiveFO));
                /*
                if (archiveFO != null) {
                    FileObject archiveRootFolder = FileUtil.getArchiveRoot(archiveFO);
                    FileLock lock = null;
                    try {
                        lock = archiveFO.lock();
                        extractArchiveFolder(jsFolder, archiveRootFolder);
                    } finally {
                        if (lock != null) {
                            lock.releaseLock();
                        }
                    }
                }*/
            }
        } catch (IOException ioe) {
            Log.getLogger().log(Level.SEVERE, "Unable to extract javascript library", ioe);
            return false;
        }

        return true;
    }
    
    private static void extractZip(File outDir, File zip) throws IOException {
        ZipFile zipFile = new ZipFile(zip);
        try {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String entryName = zipEntry.getName();
                if (zipEntry.isDirectory()) {
                    File newFolder = new File(outDir, entryName);
                    newFolder.mkdirs();
                } else {
                    File file = new File(outDir, entryName);
                    BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file));
                    InputStream input = zipFile.getInputStream(zipEntry);
                    
                    try {
                        final byte[] buffer = new byte[4096];
                        int len;
                        while ((len = input.read(buffer)) >= 0) {
                            output.write(buffer, 0, len);
                        }
                    } finally {
                        output.close();
                        input.close();
                    }
                }
            }
        } finally {
            zipFile.close();
        }

    }
    
    private static void extractArchiveFolder(FileObject destFolder, FileObject srcFolder) throws IOException {
        FileObject[] children = srcFolder.getChildren();
        for (FileObject child : children) {
            if (child.isFolder()) {
                FileObject destFolderChild = destFolder.createFolder(child.getName());
                extractArchiveFolder(destFolderChild, child);
            } else {
                child.copy(destFolder, child.getName(), child.getExt());
            }
        }
    }
}
