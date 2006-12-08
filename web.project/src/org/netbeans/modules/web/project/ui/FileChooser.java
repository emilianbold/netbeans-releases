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

package org.netbeans.modules.web.project.ui;

import org.openide.filesystems.FileUtil;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.awt.*;

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
            String pathname = System.getProperty("user.home"); //NOI18N
            if(pathname != null) {
                File file = new File(pathname).getAbsoluteFile();
                if(file.exists()) {
                    return file;
                }
            }
            File file = new File("").getAbsoluteFile(); //NOI18N
            assert file.exists() : "Default directory '" + file.getAbsolutePath() + "' does not exist"; //NOI18N
            return f;
        }
    }

    public int showDialog(Component parent, String approveButtonText) throws HeadlessException {
        FileUtil.preventFileChooserSymlinkTraversal(this, getInitialDirectory(key, getCurrentDirectory()));
        return super.showDialog(parent, approveButtonText);
    }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (SELECTED_FILE_CHANGED_PROPERTY.equals(propertyName)) {
            newValue = correctFile((File) newValue);
        }
        super.firePropertyChange(propertyName, oldValue, newValue);
    }

    public void approveSelection() {
        saveCurrentLocation();
        super.approveSelection();
    }

    private static File correctFile(File f) {
        while(f != null && ".".equals(f.getName())) { //NOI18N
            f = f.getParentFile();
        }
        return f;
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
        String path = FoldersListSettings.getPreferences().get(FoldersListSettings.LAST_USED_CHOOSER_LOCATIONS+key, null);
        return path != null ? new File(path) : null;
    }

    public static void setLastChooserLocation(String key, File folder) {
        FoldersListSettings.getPreferences().put(FoldersListSettings.LAST_USED_CHOOSER_LOCATIONS+key, folder.getPath());
    }

    public static FileChooser createDirectoryChooser(String key) {
        return createDirectoryChooser(key, null);
    }

    public static FileChooser createDirectoryChooser(String key, String initialPath) {
        FileChooser chooser = new FileChooser(key, initialPath);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setAcceptAllFileFilterUsed(false);
        return chooser;
    }

    public static FileChooser createFileChooser(String key, String dialogTitle, FileFilter fileFilter) {
        FileChooser chooser = new FileChooser(key);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setMultiSelectionEnabled(true);
        chooser.setDialogTitle(dialogTitle);
        //#61789 on old macosx (jdk 1.4.1) these two method need to be called in this order.
        chooser.setAcceptAllFileFilterUsed( false );
        chooser.setFileFilter(fileFilter);
        return chooser;
    }
}
