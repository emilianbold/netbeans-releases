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

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import org.netbeans.modules.javascript.libraries.ui.JSLibraryModificationPanel;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryChooser;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.gsfpath.api.classpath.ClassPath;
import org.netbeans.modules.javascript.libraries.provider.JavaScriptLibraryTypeProvider;
import org.netbeans.modules.javascript.libraries.ui.AddLibraryPanel;
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
    
    private static final String LIBRARY_LIST_PROP = "javascript-libraries"; // NOI18N
    private static final String LIBRARY_LOCATION_PREFIX = "jslibs-location-"; // NOI18N
    
    private static final String LIBRARY_LOCATION_TYPE_PREFIX = "jslibs-location-type-"; // NOI18N
    static final int LIBRARY_LOCATION_WEBROOT = 0;
    static final int LIBRARY_LOCATION_PROJECTROOT = 1;
    
    private static final String JS_LIBRARY_CLASSPATH = "js/library"; // NOI18N
    private static final String LIBRARY_PROPERTIES = "library.properties"; // NOI18N
    private static final String LIBRARY_PATH_PROP = "LibraryRoot"; // NOI18N
    private static final String LIBRARY_DEFAULT_NAME_PROP = "DefaultLibraryDir"; // NOI18N

    private static final String LIBRARY_ZIP_VOLUME = "scriptpath"; // NOI18N

    private static enum OverwriteOption { PROMPT, OVERWRITE, SKIP, OVERWRITE_ONCE, SKIP_ONCE };
    
    public static List<JSLibraryData> displayAddLibraryDialog(Project project, LibraryChooser.Filter filter) {
        JButton okButton = new JButton();
        JButton cancelButton = new JButton();
        
        Mnemonics.setLocalizedText(okButton, NbBundle.getMessage(AddLibraryPanel.class, "OK_BUTTON"));
        Mnemonics.setLocalizedText(cancelButton, NbBundle.getMessage(AddLibraryPanel.class, "CANCEL_BUTTON"));
        
        final AddLibraryPanel panel = new AddLibraryPanel(project, filter, okButton);
        DialogDescriptor dd = new DialogDescriptor(
                panel,
                NbBundle.getMessage(AddLibraryPanel.class, "AddLibraryPanel_DialogTitle"),
                true,
                new Object[] { okButton, cancelButton },
                cancelButton,
                DialogDescriptor.DEFAULT_ALIGN, null, null);
        
        dd.setClosingOptions(new Object[] { cancelButton });
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        final boolean[] foldersCreated = { false };
        
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                List<JSLibraryData> libraries = panel.getSelectedLibraries();
                boolean allLocationsCreated = true;
                
                for (JSLibraryData data : libraries) {
                    String location = data.getLibraryLocation();
                    File folder = FileUtil.normalizeFile(new File(location));
                    if (folder.exists() && folder.isFile()) {
                        panel.folderCreationFailed();
                        allLocationsCreated = false;
                        break;
                    } else if (!folder.exists()) {
                        try {
                            FileUtil.createFolder(folder);
                        } catch (IOException ex) {
                            panel.folderCreationFailed();
                            break;
                        }
                    }
                }
                
                foldersCreated[0] = allLocationsCreated;
                if (allLocationsCreated) {
                    dialog.setVisible(false);
                }
            }
        });
        
        try {
            dialog.setVisible(true);
        } finally {
            if (dialog != null) {
                dialog.dispose();
            }
        }
        
        return (foldersCreated[0]) ? panel.getSelectedLibraries() : null;
    }
    
    public static void modifyJSLibraries(final Project project, final boolean remove, final Collection<JSLibraryData> libraries) {
        final Set<String> libNames = getJSLibraryNames(project);

        ProjectManager.mutex().writeAccess(
                new Runnable() {

                    public void run() {
                        Preferences prefs = ProjectUtils.getPreferences(project, JSLibraryProjectUtils.class, true);
                        assert prefs != null;
                        
                        List<JSLibraryData> diffLibraries = new ArrayList<JSLibraryData>();

                        boolean modified = false;
                        for (JSLibraryData data : libraries) {
                            String name = data.getLibraryName();

                            if (remove && libNames.contains(name)) {
                                modified = true;
                                libNames.remove(name);
                                diffLibraries.add(data);
                            } else if (!remove && !libNames.contains(name)) {
                                modified = true;
                                libNames.add(name);
                                diffLibraries.add(data);
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
                            String basePath = getJSLibrarySourcePath(project);
                            String projectDir = FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath();
                            
                            for (JSLibraryData data : diffLibraries) {
                                String pathProp = LIBRARY_LOCATION_PREFIX + data.getLibraryName();
                                String typeProp = LIBRARY_LOCATION_TYPE_PREFIX + data.getLibraryName();
                                
                                String location = data.getLibraryLocation();
                                
                                if (remove || location == null) {
                                    prefs.remove(pathProp);
                                    prefs.remove(typeProp);
                                } else {
                                    int type;
                                    if (location.startsWith(basePath)) {
                                        location = location.substring(basePath.length());
                                        type = LIBRARY_LOCATION_WEBROOT;
                                    } else if (location.startsWith(projectDir)) {
                                        location = location.substring(projectDir.length());
                                        type = LIBRARY_LOCATION_PROJECTROOT;
                                    } else {
                                        Log.getLogger().severe("Invalid path being saved to project store");
                                        location = "";
                                        type = LIBRARY_LOCATION_WEBROOT;
                                    }
                                    
                                    location = location.replaceAll("[\\\\]", "/");
                                    if (location.startsWith("/")) {
                                        location = location.substring(1);
                                    }
                                    
                                    prefs.put(pathProp, location);
                                    prefs.putInt(typeProp, type);
                                }
                            }
                            
                            try {
                                prefs.flush();
                            } catch (BackingStoreException ex) {
                                Log.getLogger().log(Level.SEVERE, "Could not write to project preferences", ex);
                            }
                        }

                    }
                });
    }
    
    public static Set<JSLibraryData> getJSLibraryData(Project project) {
        Preferences prefs = ProjectUtils.getPreferences(project, JSLibraryProjectUtils.class, true);
        assert prefs != null;

        String libraries = prefs.get(LIBRARY_LIST_PROP, "");
        String[] tokens = removeEmptyStrings(libraries.split(";"));

        Set<JSLibraryData> librarySet = new LinkedHashSet<JSLibraryData>();
        String basePath = getJSLibrarySourcePath(project);
        String projectDir = FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath();
        
        for (String libraryName : tokens) {
            String location = prefs.get(LIBRARY_LOCATION_PREFIX + libraryName, null);
            int type = prefs.getInt(LIBRARY_LOCATION_TYPE_PREFIX + libraryName, 0);
            
            String path = (type == 0) ? basePath : projectDir;
            
            location = (location != null) ? FileUtil.normalizeFile(new File(path, location)).getAbsolutePath() : null;
            
            librarySet.add(new JSLibraryData(libraryName, location, type));
        }
        
        return librarySet;
    }
    
    public static Set<String> getJSLibraryNames(Project project) {
        Preferences prefs = ProjectUtils.getPreferences(project, JSLibraryProjectUtils.class, true);
        assert prefs != null;

        String libraries = prefs.get(LIBRARY_LIST_PROP, "");
        String[] tokens = removeEmptyStrings(libraries.split(";"));

        Set<String> librarySet = new LinkedHashSet<String>();
        for (String token : tokens) {
            librarySet.add(token);
        }

        return librarySet;
    }
    
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
    
    private static OverwriteOption displayFileOverwriteDialog(String file, String libraryDisplayName) {
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
                NbBundle.getMessage(JSLibraryProjectUtils.class, "ExtractLibraries_File_Overwrite_Msg", file, libraryDisplayName),
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
                return library.getType().equals(JavaScriptLibraryTypeProvider.LIBRARY_TYPE);
            }
        };
    }
    
    public static LibraryChooser.Filter createDefaultFilter(final Set<Library> excludedLibraries) {
        return new LibraryChooser.Filter() {
            public boolean accept(Library library) {
                return library.getType().equals(JavaScriptLibraryTypeProvider.LIBRARY_TYPE) &&
                        !excludedLibraries.contains(library);
            }
        };
    }
    
    public static LibraryManager getLibraryManager(Project project) {
        return LibraryManager.getDefault();
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
        try {
            FileObject fo = project.getProjectDirectory();
            ClassPath cp = ClassPath.getClassPath(fo, JS_LIBRARY_CLASSPATH);
            return FileUtil.toFile(cp.getRoots()[0]).getAbsolutePath();
        } catch (Exception ex) {
            Log.getLogger().log(Level.WARNING, "Unexpected exception retrieving web source root", ex);
            return getDefaultSourcePath(project);
        }
    }
    public static String getProjectClassName(Project project) {
        Project p = project.getLookup().lookup(Project.class);
        if (p == null) {
            p = project;
        }       
        return p.getClass().getName();        
    }
    
    public static String getDefaultRelativeLibraryPath(Project project, Library library) {
        String projectClassName = getProjectClassName(project);        
        String  resourceDir = "resources"; // NOI18N
        if (projectClassName.equals(RUBY_PROJECT)) {
            resourceDir = "";
        }
        String defaultLocation = getLibraryDefaultDir(library);
        String location;
        if (defaultLocation == null) {
            location = "";
        } else if (defaultLocation.length() == 0) {
            location = resourceDir + File.separator + library.getName(); // NOI18N
        } else {
            location = resourceDir + File.separator + defaultLocation;
        }
        
        return getJSLibrarySourcePath(project) + File.separator + location;
    }
    
    public static void addJSLibraryMetadata(Project project, Collection<JSLibraryData> libraryNames) {
        modifyJSLibraries(project, false, libraryNames);
    }
    
    public static void removeJSLibraryMetadata(Project project, Collection<JSLibraryData> libraryNames) {
        modifyJSLibraries(project, true, libraryNames);
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
    
    public static boolean extractLibrariesWithProgress(final Project project, final Collection<JSLibraryData> libraries) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Cannot invoke JSLibraryProjectUtils.extractLibrariesWithProgress() outside event dispatch thread");
        }
        
        final LibraryManager manager = getLibraryManager(project);
        ResourceBundle bundle = NbBundle.getBundle(JSLibraryProjectUtils.class);
        final ProgressHandle handle = ProgressHandleFactory.createHandle(bundle.getString("LBL_Add_Libraries_progress"));
        final JDialog dialog = createProgressDialog(handle, bundle.getString("LBL_Add_Libraries_Msg"), bundle.getString("LBL_Add_Libraries_Title"));
        
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    Map<JSLibraryData, Collection<ZipFile>> libraryZips = new HashMap<JSLibraryData, Collection<ZipFile>>();
                    int totalSize = initializeZipTable(manager, libraries, libraryZips);

                    handle.start(totalSize);

                    int currentSize = 0;
                    for (JSLibraryData libraryData : libraries) {
                        Collection<ZipFile> zipFiles = libraryZips.get(libraryData);
                        if (libraryData.getLibraryLocation() == null) {
                            Log.getLogger().severe("No location set for library: " + libraryData.getLibraryName());
                        }
                        
                        for (ZipFile zip : zipFiles) {
                            try {
                                String libraryDir = getLibraryRoot(zip);
                                Library library = manager.getLibrary(libraryData.getLibraryName());
                                currentSize = extractZip(library.getDisplayName(), libraryData, zip, handle, currentSize, libraryDir);
                            } catch (IOException ex) {
                                Log.getLogger().log(Level.SEVERE, "Unable to extract zip file", ex);
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
    
    public static boolean deleteLibrariesWithProgress(final Project project, final Collection<JSLibraryData> libraries) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Cannot invoke JSLibraryProjectUtils.deleteLibrariesWithProgress() outside event dispatch thread");
        }
        final LibraryManager manager = getLibraryManager(project);
        
        ResourceBundle bundle = NbBundle.getBundle(JSLibraryProjectUtils.class);
        final ProgressHandle handle = ProgressHandleFactory.createHandle(bundle.getString("LBL_Remove_Libraries_progress"));
        final JDialog dialog = createProgressDialog(handle, bundle.getString("LBL_Remove_Libraries_Msg"), bundle.getString("LBL_Remove_Libraries_Title"));
        
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    
                    Map<JSLibraryData, Collection<ZipFile>> libraryZips = new HashMap<JSLibraryData, Collection<ZipFile>>();
                    int totalSize = initializeZipTable(manager, libraries, libraryZips);
                    
                    handle.start(totalSize);

                    int currentSize = 0;
                    for (JSLibraryData libraryData : libraries) {
                        File folderPath = new File(libraryData.getLibraryLocation());
                        FileObject baseFO = FileUtil.toFileObject(FileUtil.normalizeFile(folderPath));
                        
                        Collection<ZipFile> zipFiles = libraryZips.get(libraryData);
                        List<String> sortedEntries = getSortedFilenamesInZips(zipFiles);
                        Collections.reverse(sortedEntries);
                        
                        try {
                            currentSize = deleteFiles(project, baseFO, sortedEntries, handle, currentSize);
                        } catch (IOException ex) {
                            Log.getLogger().log(Level.SEVERE, "Unable to delete files", ex);
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
    
    private static int initializeZipTable(LibraryManager manager, Collection<JSLibraryData> libraries, Map<JSLibraryData, Collection<ZipFile>> libraryZips) {
        int totalSize = 0;
        
        for (JSLibraryData libraryData : libraries) {
            Library library = manager.getLibrary(libraryData.getLibraryName());
            if (library == null) {
                Log.getLogger().severe("JavaScript library not found: " + libraryData.getLibraryName());
                continue;
            }

            Collection<ZipFile> zips = getJSLibraryZips(library);
            libraryZips.put(libraryData, zips);
            for (ZipFile zipFile : zips) {
                totalSize += zipFile.size();
            }
        }
        
        return totalSize;
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
    
    public static boolean isLibraryFolderEmpty(Project project, Library library, String path) {
        
        List<String> fileNames = getSortedFilenamesInZips(getJSLibraryZips(library));
        
        for (String fileName : fileNames) {
            File fullFile = new File(path, fileName);
            if (fullFile.exists() && fullFile.isFile()) {
                return false;
            }
        }
        
        return true;
    }
    
    public static List<ZipFile> getJSLibraryZips(Library library) {
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
    
    private static int deleteFiles(Project project, FileObject baseFO, List<String> fileNames, ProgressHandle handle, int currentTotal) throws IOException {
        if (baseFO != null) {            
            for (String fileName : fileNames) {                
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

            // also delete empty base directories
            String basePath = getJSLibrarySourcePath(project);
            String projectDir = FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath();
            String pathRoot;
            String absPath = FileUtil.toFile(baseFO).getAbsolutePath();
            if (absPath.startsWith(basePath)) {
                pathRoot = basePath;
            } else if (absPath.startsWith(projectDir)) {
                pathRoot = projectDir;
            } else {
                Log.getLogger().severe("Deleted library path does not correspond to a project folder");
                return currentTotal;
            }

            for (FileObject current = baseFO; current != null && current.getChildren().length == 0 &&
                    FileUtil.getFileDisplayName(baseFO).startsWith(pathRoot); current = current.getParent()) {
                try {
                    current.delete();
                } catch (IOException ex) {
                    Log.getLogger().log(Level.SEVERE, "Could not delete folder: " + FileUtil.getFileDisplayName(current), ex);
                    break;
                }

            }
        }
        return currentTotal;
    }
    
    private static int extractZip(String libraryDisplayName, JSLibraryData libraryData, ZipFile zipFile, ProgressHandle handle, int currentTotal, String rootPath) throws IOException {
        File destination = new File(libraryData.getLibraryLocation());
        try {
            FileUtil.createFolder(destination);
        } catch (IOException ex) {
            Log.getLogger().log(Level.SEVERE, "Unable to find or create root folder", ex);
            return 0;
        }
        
        try {            
            OverwriteOption option = OverwriteOption.PROMPT;
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String entryName = zipEntry.getName();
                String mappedEntryName = entryName;
                
                // used to ignore files without a common prefix
                if (rootPath != null) {
                    if (!entryName.startsWith(rootPath)) {
                        continue;
                    }

                    mappedEntryName = mappedEntryName.substring(rootPath.length());
                }
                
                if (mappedEntryName.length() == 0) {
                    currentTotal++;
                } else if (zipEntry.isDirectory()) {
                    File newFolder = new File(destination, mappedEntryName);
                    FileUtil.createFolder(newFolder);
                    handle.progress(++currentTotal);
                } else {
                    File file = new File(destination, mappedEntryName);
                    boolean exists = file.exists();
                    
                    if (exists && file.isDirectory()) {
                        throw new IOException("Cannot write normal file to existing directory with the same path");
                    }
                    
                    if (option == OverwriteOption.PROMPT && exists) {
                        OverwriteOption result = displayFileOverwriteDialog(file.getAbsolutePath(), libraryDisplayName);
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
                    FileUtil.createData(file); 
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

    public static String getLibraryDefaultDir(Library library) {
        List<ZipFile> zips = getJSLibraryZips(library);
        if (zips.size() == 0) {
            return null;
        }
        
        ZipFile zipFile = zips.get(0);
        InputStream is = null;
        try {
            ZipEntry zipEntry = zipFile.getEntry(LIBRARY_PROPERTIES);
            if (zipEntry != null) {
                Properties props = new Properties();
                is = zipFile.getInputStream(zipEntry);
                props.load(is);
                
                String propValue = props.getProperty(LIBRARY_DEFAULT_NAME_PROP);
                return propValue;
            }
            
            return "";
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
    
    public static String getLibraryRoot(ZipFile zipFile) {
        InputStream is = null;
        try {
            ZipEntry zipEntry = zipFile.getEntry(LIBRARY_PROPERTIES);
            if (zipEntry != null) {
                Properties props = new Properties();
                is = zipFile.getInputStream(zipEntry);
                props.load(is);
                
                String root = props.getProperty(LIBRARY_PATH_PROP);
                if (root == null) {
                    return null;
                } else {
                    if (root.startsWith("/")) {
                        root = root.substring(1);
                    }
                    if (!root.endsWith("/")) {
                        root = root + "/";
                    }
                    
                    return root;
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
            String libraryRoot = getLibraryRoot(zip);
            
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (libraryRoot != null) {                    
                    if (name.startsWith(libraryRoot)) {
                        name = name.substring(libraryRoot.length());
                    } else {
                        name = "";
                    }
                }
                
                if (name.length() > 0) {
                    result.add(name);
                }
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
