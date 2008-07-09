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

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryChooser;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.gsfpath.api.classpath.ClassPath;
import org.netbeans.modules.javascript.libraries.api.JSLibraryConstants;
import org.netbeans.modules.javascript.libraries.spi.JSLibrarySharabilityQueryImpl;
import org.netbeans.modules.javascript.libraries.spi.ProjectJSLibraryManager;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 *
 * @author Quy Nguyen <quynguyen@netbeans.org>
 */
public final class JSLibraryProjectUtils {
    private static final String WEB_PROJECT = "org.netbeans.modules.web.project.WebProject"; // NOI18N
    private static final String WEB_PROJECT_DEFAULT_RELATIVE_PATH = "web"; // NOI18N
    private static final String RUBY_PROJECT = "org.netbeans.modules.ruby.railsprojects.RailsProject"; // NOI18N
    private static final String RUBY_PROJECT_DEFAULT_RELATIVE_PATH = "public"; // NOI18N
    private static final String PHP_PROJECT = "org.netbeans.modules.php.project"; // NOI18N
    private static final String OTHER_PROJECT_DEFAULT_RELATIVE_PATH = "javascript"; // NOI18N
        
    private static final String LIBRARY_PROPERTIES = "library.properties"; // NOI18N
    private static final String LIBRARY_PATH_PROP = "LibraryRoot"; // NOI18N

    private static final String LIBRARY_ZIP_VOLUME = "scriptpath"; // NOI18N

    private static enum OverwriteOption { PROMPT, OVERWRITE, SKIP, OVERWRITE_ONCE, SKIP_ONCE };
    
    public static Object displayLibraryOverwriteDialog(Library library) {
        NotifyDescriptor nd = 
                new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(JSLibraryProjectUtils.class, "ExtractLibraries_Overwrite_Msg", library.getDisplayName()), 
                NbBundle.getMessage(JSLibraryProjectUtils.class, "ExtractLibraries_Overwrite_Title"), 
                NotifyDescriptor.YES_NO_OPTION);
        
        return DialogDisplayer.getDefault().notify(nd);
    }
    
    public static Object displayLibraryDeleteConfirm(Library library) {
        NotifyDescriptor nd = 
                new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(JSLibraryProjectUtils.class, "DeleteLibraries_Delete_Msg", library.getDisplayName()), 
                NbBundle.getMessage(JSLibraryProjectUtils.class, "DeleteLibraries_Delete_Title"), 
                NotifyDescriptor.YES_NO_CANCEL_OPTION);
        
        return DialogDisplayer.getDefault().notify(nd);
    }
    
    private static OverwriteOption displayFileOverwriteDialog(String file, Library library) {
        JButton yesToAll = new JButton();
        JButton noToAll = new JButton();
        JButton yes = new JButton();
        JButton no = new JButton();
        
        Mnemonics.setLocalizedText(yesToAll, NbBundle.getMessage(JSLibraryProjectUtils.class, "LBL_YesToAll_Button"));
        Mnemonics.setLocalizedText(noToAll, NbBundle.getMessage(JSLibraryProjectUtils.class, "LBL_NoToAll_Button"));
        Mnemonics.setLocalizedText(yes, NbBundle.getMessage(JSLibraryProjectUtils.class, "LBL_Yes_Button"));
        Mnemonics.setLocalizedText(no, NbBundle.getMessage(JSLibraryProjectUtils.class, "LBL_No_Button"));

        Object[] options = new Object[] { yes, yesToAll, no, noToAll };
        
        DialogDescriptor dd =
                new DialogDescriptor(
                NbBundle.getMessage(JSLibraryProjectUtils.class, "ExtractLibraries_File_Overwrite_Msg", file, library.getDisplayName()),
                NbBundle.getMessage(JSLibraryProjectUtils.class, "ExtractLibraries_File_Overwrite_Title"),
                true, options, no, DialogDescriptor.DEFAULT_ALIGN, null, null);
        
        Object result = DialogDisplayer.getDefault().notify(dd);
        if (result == yes) {
            return OverwriteOption.OVERWRITE_ONCE;
        } else if (result == no) {
            return OverwriteOption.SKIP_ONCE;
        } else if (result == yesToAll) {
            return OverwriteOption.OVERWRITE;
        } else if (result == noToAll) {
            return OverwriteOption.SKIP;
        }
        
        return OverwriteOption.SKIP_ONCE;
    }
    
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
        return LibraryManager.getDefault();
    }
    
    public static List<Library> getJSLibraries(Project project) {
        Set<String> libNames = ProjectJSLibraryManager.getJSLibraryNames(project);
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
        try {
            FileObject fo = project.getProjectDirectory();
            ClassPath cp = ClassPath.getClassPath(fo, JSLibraryConstants.JS_LIBRARY_CLASSPATH);
            return FileUtil.toFile(cp.getRoots()[0]).getAbsolutePath();
        } catch (Exception ex) {
            Log.getLogger().log(Level.WARNING, "Unexpected exception retrieving web source root", ex);
            return getDefaultSourcePath(project);
        }
    }
    
    public static void addJSLibraryMetadata(final Project project, final Collection<Library> libraries) {
        ProjectJSLibraryManager.modifyJSLibraries(project, false, libraries);
    }
    
    public static void removeJSLibraryMetadata(Project project, Collection<Library> libraries) {
        ProjectJSLibraryManager.modifyJSLibraries(project, true, libraries);
    }

    private static String getDefaultSourcePath(Project project) {
        Project p = project.getLookup().lookup(Project.class);
        if (p == null) {
            p = project;
        }
        
        String projectClassName = p.getClass().getName();
        if (projectClassName.startsWith(PHP_PROJECT)) {
            SourceGroup[] groups = ProjectUtils.getSources(p).getSourceGroups("PHPSOURCE"); // XXX key taken from php project
            if (groups.length > 0) {
                File srcDirFile = FileUtil.toFile(groups[0].getRootFolder());
                return srcDirFile.getAbsolutePath();
            } else {
                File projectDirFile = FileUtil.toFile(p.getProjectDirectory());
                return new File(projectDirFile, OTHER_PROJECT_DEFAULT_RELATIVE_PATH).getAbsolutePath();
            }
        } else if (projectClassName.equals(WEB_PROJECT)) {
            File projectDirFile = FileUtil.toFile(p.getProjectDirectory());
            return new File(projectDirFile, WEB_PROJECT_DEFAULT_RELATIVE_PATH).getAbsolutePath();
        } else if (projectClassName.equals(RUBY_PROJECT)) {
            File projectDirFile = FileUtil.toFile(p.getProjectDirectory());
            return new File(projectDirFile, RUBY_PROJECT_DEFAULT_RELATIVE_PATH).getAbsolutePath();
        } else {
            File projectDirFile = FileUtil.toFile(p.getProjectDirectory());
            return new File(projectDirFile, OTHER_PROJECT_DEFAULT_RELATIVE_PATH).getAbsolutePath();
        }
    }

    private static boolean isIncluded(String fileName, String prefix) {
        if (prefix.length() > fileName.length()) {
            return false;
        }
        
        for (int i = 0; i < prefix.length(); i++) {
            char letter = fileName.charAt(i);
            letter = (letter == '\\') ? '/' : letter;
            
            char prefixLetter = prefix.charAt(i);
            prefixLetter = (prefixLetter == '\\') ? '/' : prefixLetter;
            
            if (prefixLetter != letter) {
                return false;
            }
        }
        
        return true;
    }
    
    public static boolean extractLibrariesWithProgress(final Project project, final Collection<Library> libraries, final String path) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Cannot invoke JSLibraryProjectUtils.extractLibrariesWithProgress() outside event dispatch thread");
        }
        
        ResourceBundle bundle = NbBundle.getBundle(JSLibraryProjectUtils.class);
        final ProgressHandle handle = ProgressHandleFactory.createHandle(bundle.getString("LBL_Add_Libraries_progress"));
        final JDialog dialog = createProgressDialog(handle, bundle.getString("LBL_Add_Libraries_Msg"), bundle.getString("LBL_Add_Libraries_Title"));

        final File destination = new File(path);
        try {
            FileUtil.createFolder(destination);
        } catch (IOException ex) {
            Log.getLogger().log(Level.SEVERE, "Unable to find or create root folder", ex);
            return false;
        }
        
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    int totalSize = 0;
                    Map<Library, Collection<ZipFile>> libraryZips = new HashMap<Library, Collection<ZipFile>>();
                    for (Library library : libraries) {
                        Collection<ZipFile> zips = getJSLibraryZips(library);
                        libraryZips.put(library, zips);
                        for (ZipFile zipFile : zips) {
                            totalSize += zipFile.size();
                        }
                    }

                    handle.start(totalSize);

                    int currentSize = 0;
                    for (Library library : libraries) {
                        Collection<ZipFile> zipFiles = libraryZips.get(library);

                        for (ZipFile zip : zipFiles) {
                            String[] libraryDirs = null;
                            try {
                                libraryDirs = getLibraryPropsValue(zip);
                                if (libraryDirs != null) {
                                    for (String libraryDir : libraryDirs) {
                                        addUnsharability(project, libraryDir);
                                    }
                                }
                                
                                currentSize = extractZip(library, destination, zip, handle, currentSize, libraryDirs);
                            } catch (IOException ex) {
                                Log.getLogger().log(Level.SEVERE, "Unable to extract zip file", ex);
                                if (libraryDirs != null) {
                                    for (String libraryDir : libraryDirs) {
                                        removeUnsharability(project, libraryDir);
                                    }
                                }
                            }
                        }
                    }

                    handle.finish();
                } finally {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            dialog.setVisible(false);
                            dialog.dispose();
                        }
                    });
                }
            }
        });

        dialog.setVisible(true);
        return true;
    }
    
    private static void addUnsharability(Project project, String libraryDir) {
        JSLibrarySharabilityQueryImpl sharability = project.getLookup().lookup(JSLibrarySharabilityQueryImpl.class);
        if (sharability != null) {
            sharability.addUnsharablePath(libraryDir);
        }
    }

    private static void removeUnsharability(Project project, String libraryDir) {
        JSLibrarySharabilityQueryImpl sharability = project.getLookup().lookup(JSLibrarySharabilityQueryImpl.class);
        if (sharability != null) {
            sharability.removeUnsharablePath(libraryDir);
        }
    }
    
    public static boolean deleteLibrariesWithProgress(final Project project, final Collection<Library> libraries, final String path) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Cannot invoke JSLibraryProjectUtils.deleteLibrariesWithProgress() outside event dispatch thread");
        }
        
        ResourceBundle bundle = NbBundle.getBundle(JSLibraryProjectUtils.class);
        final ProgressHandle handle = ProgressHandleFactory.createHandle(bundle.getString("LBL_Remove_Libraries_progress"));
        final JDialog dialog = createProgressDialog(handle, bundle.getString("LBL_Remove_Libraries_Msg"), bundle.getString("LBL_Remove_Libraries_Title"));
        
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    int totalSize = 0;
                    Map<Library, Collection<ZipFile>> libraryZips = new HashMap<Library, Collection<ZipFile>>();
                    for (Library library : libraries) {
                        Collection<ZipFile> zips = getJSLibraryZips(library);
                        libraryZips.put(library, zips);
                        for (ZipFile zipFile : zips) {
                            totalSize += zipFile.size();
                        }
                    }

                    handle.start(totalSize);

                    int currentSize = 0;
                    File folderPath = new File(path);
                    FileObject baseFO = FileUtil.toFileObject(folderPath);
                    for (Library library : libraries) {
                        Collection<ZipFile> zipFiles = libraryZips.get(library);
                        List<String> sortedEntries = getSortedFilenamesInZips(zipFiles);
                        Collections.reverse(sortedEntries);

                        for (ZipFile zip : zipFiles) {
                            String[] libraryDirs = null;
                            try {
                                libraryDirs = getLibraryPropsValue(zip);
                                if (libraryDirs != null) {
                                    for (String libraryDir : libraryDirs) {
                                        removeUnsharability(project, libraryDir);
                                    }
                                }
                                
                                currentSize = deleteFiles(baseFO, sortedEntries, handle, currentSize, libraryDirs);
                            } catch (IOException ex) {
                                Log.getLogger().log(Level.SEVERE, "Unable to delete files", ex);
                                if (libraryDirs != null) {
                                    for (String libraryDir : libraryDirs) {
                                        addUnsharability(project, libraryDir);
                                    }
                                }
                            }
                        }
                    }

                    handle.finish();
                } finally {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            dialog.setVisible(false);
                            dialog.dispose();
                        }
                    });
                }
            }
        });

        dialog.setVisible(true);
        return true;
    }
    
    private static JDialog createProgressDialog(ProgressHandle handle, String dialogMsg, String dialogTitle) {
        JComponent component = ProgressHandleFactory.createProgressComponent(handle);
        Frame mainWindow = WindowManager.getDefault().getMainWindow();
        final JDialog dialog = new JDialog(mainWindow, dialogTitle, true);
        
        JSLibraryModificationPanel panel = new JSLibraryModificationPanel(component, dialogMsg);

        dialog.getContentPane().add(panel);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.pack();

        Rectangle bounds = mainWindow.getBounds();
        int middleX = bounds.x + bounds.width / 2;
        int middleY = bounds.y + bounds.height / 2;
        Dimension size = dialog.getPreferredSize();
        dialog.setBounds(middleX - size.width / 2, middleY - size.height / 2, size.width, size.height);
        
        return dialog;
    }
    
    public static boolean isLibraryFolderEmpty(Project project, Library library) {
        String path = getJSLibrarySourcePath(project);
        
        List<String> fileNames = getSortedFilenamesInZips(getJSLibraryZips(library));
        
        for (String fileName : fileNames) {
            File fullFile = new File(path, fileName);
            if (fullFile.exists() && fullFile.isFile()) {
                return false;
            }
        }
        
        return true;
    }
    
    public static Collection<ZipFile> getJSLibraryZips(Library library) {
        ArrayList<ZipFile> result = new ArrayList<ZipFile>();

        try {
            for (URL url : library.getContent(LIBRARY_ZIP_VOLUME)) {
                URL archiveURL = FileUtil.isArchiveFile(url) ? url : FileUtil.getArchiveFile(url);
                FileObject archiveFO = URLMapper.findFileObject(archiveURL);

                File zipFile = (archiveFO != null) ? FileUtil.toFile(archiveFO) : null;
                if (zipFile != null) {
                    result.add(new ZipFile(zipFile));
                }
            }
        } catch (IOException ex) {
            Log.getLogger().log(Level.SEVERE, "Unable to load zip file", ex);
        }

        return result;
    }
    
    private static int deleteFiles(FileObject baseFO, List<String> fileNames, ProgressHandle handle, int currentTotal, String[] includePaths) throws IOException {
        if (baseFO != null) {
            if (includePaths != null) {
                for (int i = 0; i < includePaths.length; i++) {
                    if (!includePaths[i].startsWith("/")) {
                        includePaths[i] = "/" + includePaths[i];
                    }
                }
            }
            
            for (String fileName : fileNames) {
                if (!fileName.startsWith("/")) {
                    fileName = "/" + fileName;
                }
                
                if (includePaths != null) {
                    boolean skip = true;
                    for (String includePath : includePaths) {
                        if (isIncluded(fileName, includePath)) {
                            skip = false;
                            break;
                        } else if (isIncluded(includePath, fileName)) {
                            // check if the current file is a folder and a parent of
                            // a LibraryRoot
                            FileObject toDelete = baseFO.getFileObject(fileName);
                            if (toDelete != null && toDelete.isFolder() 
                                    && toDelete.getChildren().length == 0) {
                                skip = false;
                                break;
                            }
                        }
                    }
                    
                    if (skip) {
                        continue;
                    }
                }
                
                FileObject toDelete = baseFO.getFileObject(fileName);
                if (toDelete != null) {
                    if (toDelete.isFolder() && toDelete.getChildren().length > 0) {
                        Log.getLogger().warning("Skipping attempt to delete non-empty folder");
                        continue;
                    }
                    
                    try {
                        toDelete.delete();
                        handle.progress(++currentTotal);
                    } catch (IOException ex) {
                        Log.getLogger().log(Level.WARNING, "Could not delete file: " + FileUtil.getFileDisplayName(toDelete));
                    }
                }
            }
        }
        
        return currentTotal;
    }
    
    private static int extractZip(Library library, File outDir, ZipFile zipFile, ProgressHandle handle, int currentTotal, String[] includePaths) throws IOException {
        try {
            if (includePaths != null && includePaths.length > 0) {
                for (int i = 0; i < includePaths.length; i++) {
                    if (!includePaths[i].startsWith("/")) {
                        includePaths[i] = "/" + includePaths[i];
                    }
                }
                
            }
            
            OverwriteOption option = OverwriteOption.PROMPT;
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String entryName = zipEntry.getName();
                
                // used to ignore files without a common prefix
                if (includePaths != null) {
                    if (!entryName.startsWith("/")) {
                        entryName = "/" + entryName;
                    }
                    
                    boolean match = false;
                    for (String includePath : includePaths) {
                        if (isIncluded(entryName,includePath)) {
                            match = true;
                            break;
                        }
                    }
                    
                    if (!match) {
                        continue;
                    }
                }
                
                if (zipEntry.isDirectory()) {
                    File newFolder = new File(outDir, entryName);
                    newFolder.mkdirs();
                    
                    handle.progress(++currentTotal);
                } else {
                    File file = new File(outDir, entryName);
                    boolean exists = file.exists();
                    
                    if (exists && file.isDirectory()) {
                        throw new IOException("Cannot write normal file to existing directory with the same path");
                    }
                    
                    if (option == OverwriteOption.PROMPT && exists) {
                        OverwriteOption result = displayFileOverwriteDialog(entryName, library);
                        if (result == OverwriteOption.SKIP_ONCE) {
                            continue;
                        } else if (result == OverwriteOption.SKIP) {
                            option = result;
                            continue;
                        } else if (result == OverwriteOption.OVERWRITE) {
                            option = result;
                        }
                    } else if (exists && option == OverwriteOption.SKIP) {
                        continue;
                    }
                    
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
                        
                        handle.progress(++currentTotal);
                    }
                }
            }
        } finally {
            zipFile.close();
        }

        return currentTotal;
    }

    public static String[] getLibraryPropsValue(ZipFile zipFile) {
        InputStream is = null;
        try {
            ZipEntry zipEntry = zipFile.getEntry(LIBRARY_PROPERTIES);
            if (zipEntry != null) {
                Properties props = new Properties();
                is = zipFile.getInputStream(zipEntry);
                props.load(is);
                
                String propValue = props.getProperty(LIBRARY_PATH_PROP);
                if (propValue != null) {
                    String[] result = removeEmptyStrings(propValue.split(","));
                    if (result.length > 0) {
                        for (int i = 0; i < result.length; i++) {
                            result[i] = result[i].trim();
                        }
                        
                        return result;
                    }
                }
            }
            
            return null;
        } catch (IOException ex) {
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                }catch (IOException ex) {
                }
            }
        }
    }
    
    private static List<String> getSortedFilenamesInZips(Collection<ZipFile> zipFiles) {
        List<String> result = new ArrayList<String>();
        
        for (ZipFile zip : zipFiles) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                result.add(entry.getName());
            }
        }
        
        Collections.sort(result);
        
        return result;
    }
    
    private static String[] removeEmptyStrings(String[] arg) {
        ArrayList<String> strings = new ArrayList<String>();
        for (String component : arg) {
            if (component != null && component.length() > 0) {
                strings.add(component);
            }
        }
        
        return strings.toArray(new String[strings.size()]);
    }
}
