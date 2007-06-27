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
 *
 * Contributor(s): Soot Phengsy
 */

package org.netbeans.swing.dirchooser;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.JFileChooser;
import javax.swing.tree.DefaultMutableTreeNode;
import org.netbeans.api.project.ProjectManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * A directory tree node.
 *
 * @author Soot Phengsy
 */
public class DirectoryNode extends DefaultMutableTreeNode {
    
    public final static int SINGLE_SELECTION = 0;
    
    public final static int DIG_IN_SELECTION = 4;

    /** case insensitive file name's comparator */
    static final FileNameComparator FILE_NAME_COMPARATOR = new FileNameComparator();
    
    private File directory;
    
    private boolean isDir;
    
    private boolean loaded;
    
    private boolean interim;
    
    private boolean isSelected;
    
    private boolean isEditable;
    
    private boolean expanded;
    
    public DirectoryNode(File file) {
        this(file, true, false, false, false);
    }
    
    public DirectoryNode(File file, boolean allowsChildren) {
        this(file, allowsChildren, false, false, false);
    }
    
    public DirectoryNode(File file, boolean allowsChildren, boolean isSelected,
            boolean isChecked, boolean isEditable) {
        super(file, allowsChildren);
        this.directory = file;
        this.isDir = directory.isDirectory();
        this.isSelected = isSelected;
    }
    
    public boolean isLoaded() {
        return this.loaded;
    }
    
    public File getFile() {
        return this.directory;
    }
    
    public void setFile(File file) {
        setUserObject(file);
        this.directory = file;
        this.loaded = false;
    }
    
    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
    
    public boolean isLeaf() {
        return !this.isDir;
    }
    
    public boolean getAllowsChildren() {
        return this.isDir;
    }
    
    public boolean isSelected() {
        return this.isSelected;
    }
    
    public boolean loadChildren(JFileChooser chooser, boolean descend) {
        //fixed bug #97124
        if (loaded == false) {
            
            ArrayList files = getFiles(chooser);
            
            if(files.size() == 0) {
                return false;
            }
            
            for(int i = 0; i < files.size(); i++) {
                File child = (File) files.get(i);
                
                if(chooser.accept(child)) {
                    try {
                        DirectoryNode node = new DirectoryNode(child);
                        if (descend == false) {
                            break;
                        }
                        add(node);
                    } catch (NullPointerException t) {
                        t.printStackTrace();
                    }
                }
            }
            
            if (descend == true ||  (getChildCount() > 0)) {
                loaded = true;
            }
        }
        
        return loaded;
    }
        
    private ArrayList getFiles(JFileChooser chooser) {
        //fixed bug #97124
        ArrayList list = new ArrayList();
        
        File[] files = chooser.getFileSystemView().getFiles(directory, chooser.isFileHidingEnabled());
        int mode = chooser.getFileSelectionMode();
        if(mode == JFileChooser.DIRECTORIES_ONLY) {
            for(int i = 0; i < files.length; i++) {
                File child = files[i];
                if (child.isDirectory()) {
                    list.add(child);
                }
            }
            Collections.sort(list, FILE_NAME_COMPARATOR);
        } else if(mode == JFileChooser.FILES_AND_DIRECTORIES || mode == JFileChooser.FILES_ONLY) {
            ArrayList dirList = new ArrayList();
            ArrayList fileList = new ArrayList();
            for(int i = 0; i < files.length; i++) {
                File child = files[i];
                if (child.isDirectory()) {
                    dirList.add(child);
                } else {
                    fileList.add(child);
                }
            }
            
            Collections.sort(dirList, FILE_NAME_COMPARATOR);
            Collections.sort(fileList, FILE_NAME_COMPARATOR);
            
            list.addAll(dirList);
            list.addAll(fileList);
        }
        
        return list;
    }
    
    public boolean isNetBeansProject() {
        return isNetBeansProject(directory);
    }
    
    public static boolean isNetBeansProject (File directory) {
        boolean retVal = false;
        if (directory != null) {
            FileObject fo = convertToValidDir(directory);
            if (fo != null) {
                if (Utilities.isUnix() && fo.getParent() != null
                        && fo.getParent().getParent() == null) {
                    retVal = false; // Ignore all subfolders of / on unixes
                    // (e.g. /net, /proc)
                } else {
                    retVal = ProjectManager.getDefault().isProject(fo);
                }
            }
        }
        return retVal;
    }
    
    private static FileObject convertToValidDir(File f) {
        FileObject fo;
        File testFile = new File(f.getPath());
        if (testFile == null || testFile.getParent() == null) {
            // BTW this means that roots of file systems can't be project
            // directories.
            return null;
        }
        
        /**
         *
         * ATTENTION: on Windows may occure dir.isDirectory () == dir.isFile () ==
         *
         * true then its used testFile instead of dir.
         *
         */
        if (!testFile.isDirectory()) {
            return null;
        }
        
        fo = FileUtil.toFileObject(FileUtil.normalizeFile(testFile));
        return fo;
    }
    
    private class DirectoryFilter implements FileFilter {
        public boolean accept(File f) {
            return f.isDirectory();
        }
        
        public String getDescription() {
            return "Directory";
        }
    }
    
    /** Compares files ignoring case sensitivity */
    private static class FileNameComparator implements Comparator<File> {

        public int compare(File f1, File f2) {
            return String.CASE_INSENSITIVE_ORDER.compare(f1.getName(), f2.getName());
        }
        
    }
    
}
