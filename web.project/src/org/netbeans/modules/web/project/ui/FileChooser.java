/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project.ui;

import org.openide.filesystems.FileUtil;
import org.netbeans.modules.web.project.ui.FoldersListSettings;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.awt.*;
import java.util.Map;
import java.util.HashMap;

public class FileChooser extends JFileChooser {

    private String key;
    protected String initialPath;

    public FileChooser(String key, String currentDirectoryPath) {
        super(getInitialDirectory(key, currentDirectoryPath));
        this.key = key;
    }

    public FileChooser(String key) {
        this(key, null);
        this.key = key;
    }

    private static File getInitialDirectory(String key, String currentDirectoryPath) {
        return getInitialDirectory(key, currentDirectoryPath == null ? null : new File(currentDirectoryPath));
    }

    private static File getInitialDirectory(String key, File f) {
        while (f != null) {
            if (f.exists() && f.isDirectory()) {
                return f;
            }
            f = f.getParentFile();
        }
        File lastChooserLocation = getLastChooserLocation(key);
        if (lastChooserLocation != null && lastChooserLocation.exists()) {
            return lastChooserLocation;
        } else {
            return new File(System.getProperty("user.home")); //NOI18N
        }
    }

    public int showDialog(Component parent, String approveButtonText) throws HeadlessException {
        FileUtil.preventFileChooserSymlinkTraversal(this, getInitialDirectory(key, getCurrentDirectory()));
        return super.showDialog(parent, approveButtonText);
    }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (SELECTED_FILE_CHANGED_PROPERTY.equals(propertyName)) {
            File f = (File) newValue;
            if (f != null && ".".equals(f.getName())) {
                // display correct path to directory "."
                super.firePropertyChange(propertyName, oldValue, f.getParentFile());
                return;
            }
        }
        super.firePropertyChange(propertyName, oldValue, newValue);
    }

    public void approveSelection() {
        saveCurrentLocation();
        super.approveSelection();
    }

    public void cancelSelection() {
        saveCurrentLocation();
        super.cancelSelection();
    }

    private void saveCurrentLocation() {
        if (!isMultiSelectionEnabled() && isDirectorySelectionEnabled()) {
            // Try to save selected file (if it is an existing single directory
            if (saveLocation(getSelectedFile())) {
                return;
            }
        }
        saveLocation(getCurrentDirectory());
    }

    private boolean saveLocation(File f) {
        if (f != null && f.isDirectory()) {
            setLastChooserLocation(key, f);
            return true;
        } else {
            return false;
        }
    }

    public static File getLastChooserLocation(String key) {
        Map map = FoldersListSettings.getDefault().getLastUsedChooserLocations();
        if (map != null) {
            return (File) map.get(key);
        } else {
            return null;
        }
    }

    public static void setLastChooserLocation(String key, File folder) {
        FoldersListSettings foldersListSettings = FoldersListSettings.getDefault();
        Map map = foldersListSettings.getLastUsedChooserLocations();
        // we should get a different instance of the map to be sure
        // that modification of settings will be detected
        map = map == null ? new HashMap() : new HashMap(map);
        map.put(key, folder);
        foldersListSettings.setLastUsedChooserLocations(map);
    }

    public static FileChooser createDirectoryChooser(String key) {
        return createDirectoryChooser(key, null);
    }

    public static FileChooser createDirectoryChooser(String key, String initialPath) {
        FileChooser chooser = new FileChooser(key, initialPath);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileSystemView(new FileSystemViewDecorator(chooser.getFileSystemView()));
        return chooser;
    }

    public static FileChooser createFileChooser(String key, String initialPath) {
        FileChooser chooser = new FileChooser(key, initialPath);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setMultiSelectionEnabled(true);
        return chooser;
    }

    /**
     * FileSystemViewDecorator decorates existing FileSystemView object to add directory "." (current directory)
     * to result of method getFiles().
     * It is useful for browsing directories. If we browse any directory and then we select (by accident or by mistake)
     * subdirectory of the directory, it is difficult to return selection back to the original directory.
     * Usual procedure is to go to parent directory and then find and select the original directory, that can be often
     * annoying. The added directory "." in list of directories enables to return selection back by one mouse click.
     */
    private static class FileSystemViewDecorator extends FileSystemView {

        FileSystemView delegate;

        public FileSystemViewDecorator(FileSystemView delegate) {
            this.delegate = delegate;
        }

        public File createNewFolder(File containingDir) throws IOException {
            return delegate.createNewFolder(containingDir);
        }

        public boolean isRoot(File f) {
            return delegate.isRoot(f);
        }

        public Boolean isTraversable(File f) {
            return delegate.isTraversable(f);
        }

        public String getSystemDisplayName(File f) {
            return delegate.getSystemDisplayName(f);
        }

        public String getSystemTypeDescription(File f) {
            return delegate.getSystemTypeDescription(f);
        }

        public Icon getSystemIcon(File f) {
            return delegate.getSystemIcon(f);
        }

        public boolean isParent(File folder, File file) {
            return delegate.isParent(folder, file);
        }

        public File getChild(File parent, String fileName) {
            return delegate.getChild(parent, fileName);
        }

        public boolean isFileSystem(File f) {
            return delegate.isFileSystem(f);
        }

        public boolean isHiddenFile(File f) {
            return delegate.isHiddenFile(f);
        }

        public boolean isFileSystemRoot(File dir) {
            return delegate.isFileSystemRoot(dir);
        }

        public boolean isDrive(File dir) {
            return delegate.isDrive(dir);
        }

        public boolean isFloppyDrive(File dir) {
            return delegate.isFloppyDrive(dir);
        }

        public boolean isComputerNode(File dir) {
            return delegate.isComputerNode(dir);
        }

        public File[] getRoots() {
            return delegate.getRoots();
        }

        public File getHomeDirectory() {
            return delegate.getHomeDirectory();
        }

        public File getDefaultDirectory() {
            return delegate.getDefaultDirectory();
        }

        public File createFileObject(File dir, String filename) {
            return delegate.createFileObject(dir, filename);
        }

        public File createFileObject(String path) {
            return delegate.createFileObject(path);
        }

        public File[] getFiles(File dir, boolean useFileHiding) {
            File[] origFiles = delegate.getFiles(dir, useFileHiding);
            if (dir != null && isTraversable(dir).booleanValue()) {
                File[] files;
                files = new File[origFiles.length + 1];
                // Added folder "." makes easier to select current folder
                files[0] = new File(dir, ".");
                for (int i = 0; i < origFiles.length; i++) {
                    files[i + 1] = origFiles[i];
                }
                return files;
            } else {
                return origFiles;
            }
        }

        public File getParentDirectory(File dir) {
            return delegate.getParentDirectory(dir);
        }

        protected File createFileSystemRoot(File f) {
            return createFileSystemRoot(f);
        }
    }
}
