/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui;

import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Stack;
import java.util.TreeSet;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.layers.LayerUtils;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;

/**
 * UI related utility methods for the module.
 *
 * @author Martin Krauskopf
 */
public final class UIUtil {
    
    private UIUtil() {}
    
    /**
     * Calls in turn {@link ProjectChooser#setProjectsFolder} if the
     * <code>folder</code> is not <code>null</code> and is a directory.
     */
    public static void setProjectChooserDir(File folder) {
        if (folder == null || !folder.isDirectory()) {
            return;
        }
        ProjectChooser.setProjectsFolder(folder);
    }
    
    /**
     * Calls {@link #setProjectChooserDir} with the <code>fileOrFolder</code>'s
     * parent if it isn't <code>null</code>. Otherwise fallbacks to
     * <code>fileOrFolder</code> itself if it is a directory.
     */
    public static void setProjectChooserDirParent(File fileOrFolder) {
        if (fileOrFolder == null) {
            return;
        }
        File parent = fileOrFolder.getParentFile();
        setProjectChooserDir(parent != null ? parent :
            (fileOrFolder.isDirectory() ? fileOrFolder : null));
    }
    
    /**
     * Set the <code>text</code> for the <code>textComp</code> and set its
     * carret position to the end of the text.
     */
    public static void setText(JTextComponent textComp, String text) {
        textComp.setText(text);
        textComp.setCaretPosition(text == null ? 0 : text.length());
    }
    
    /**
     * Convenient class for listening to document changes. Use it if you don't
     * care what exact change really happened. {@link #removeUpdate} and {@link
     * #changedUpdate} just delegates to {@link #insertUpdate}. So everything
     * what is need to be notified about document changes is to override {@link
     * #insertUpdate} method.
     */
    public abstract static class DocumentAdapter implements DocumentListener {
        public void removeUpdate(DocumentEvent e) { insertUpdate(null); }
        public void changedUpdate(DocumentEvent e) { insertUpdate(null); }
    }
    
    /**
     * Returns an instance of {@link javax.swing.JFileChooser} permitting
     * selection only a regular <em>icon</em>.
     */
    public static JFileChooser getIconFileChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        FileFilter[] filters = chooser.getChoosableFileFilters();
        for (int i = 0; i < filters.length; i++) {
            chooser.removeChoosableFileFilter(filters[i]);
        }
        chooser.addChoosableFileFilter(new IconFilter());
        chooser.setFileView(new FileView() {
            public Icon getIcon(File f) {
                // Show icons right in the chooser, to make it easier to find
                // the right one.
                if (f.getName().endsWith(".gif") || f.getName().endsWith(".png")) { // NOI18N
                    Icon icon = new ImageIcon(f.getAbsolutePath());
                    if (icon.getIconWidth() == 16 && icon.getIconHeight() == 16) {
                        return icon;
                    }
                }
                return null;
            }
        });
        return chooser;
    }
    
    /**
     * Create combobox containing packages from the given {@link SourceGroup}.
     */
    public static JComboBox createPackageComboBox(SourceGroup srcRoot) {
        JComboBox packagesComboBox = new JComboBox(PackageView.createListView(srcRoot));
        packagesComboBox.setRenderer(PackageView.listRenderer());
        return packagesComboBox;
    }
    
    /**
     * Returns a string suitable for text areas respresenting content of {@link
     * CreatedModifiedFiles} <em>paths</em>.
     *
     * @param relPaths should be either
     *        {@link CreatedModifiedFiles#getCreatedPaths()} or
     *        {@link CreatedModifiedFiles#getModifiedPaths()}.
     */
    public static String generateTextAreaContent(String[] relPaths) {
        StringBuffer sb = new StringBuffer();
        if (relPaths.length > 0) {
            for (int i = 0; i < relPaths.length; i++) {
                if (i > 0) {
                    sb.append('\n');
                }
                sb.append(relPaths[i]);
            }
        }
        return sb.toString();
    }
    
    public static ComboBoxModel createLayerPresenterComboModel(Project project, String sfsRoot) {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        try {
            FileSystem sfs = LayerUtils.getEffectiveSystemFilesystem(project);
            FileObject root = sfs.getRoot().getFileObject(sfsRoot);
            if (root != null) {
                Collection items = new TreeSet();
                for (Enumeration subFolders = root.getFolders(true); subFolders.hasMoreElements(); ) {
                    LayerItemPresenter layerFolder = new LayerItemPresenter(
                            (FileObject) subFolders.nextElement(), root);
                    items.add(layerFolder);
                }
                for (Iterator it = items.iterator(); it.hasNext(); ) {
                    model.addElement(it.next());
                }
            }
        } catch (IOException exc) {
            Util.err.notify(exc);
        }
        return model;
    }
    
    public static class LayerItemPresenter implements Comparable {
        
        private String displayName;
        private FileObject item;
        private FileObject root;
        private boolean contentType;
        
        public LayerItemPresenter(final FileObject item, 
                final FileObject root,
                final boolean contentType) {
            this.item = item;
            this.root = root;
            this.contentType = contentType;
        }
        
        public LayerItemPresenter(final FileObject item, final FileObject root) {
            this(item, root, false);
        }
        
        public FileObject getFileObject() {
            return item;
        }
        
        public String getFullPath() {
            return item.getPath();
        }
        
        public String getDisplayName() {
            if (displayName == null) {
                displayName = computeDisplayName();
            }
            return displayName;
        }
        
        public String toString() {
            return getDisplayName();
        }
        
        public int compareTo(Object o) {
            return Collator.getInstance().compare(getDisplayName(),
                    ((LayerItemPresenter) o).getDisplayName());
        }
        
        private static String getFileObjectName(FileObject fo) {
            String name = null;
            try {
                name = fo.getFileSystem().getStatus().annotateName(
                        fo.getNameExt(), Collections.singleton(fo));
            } catch (FileStateInvalidException ex) {
                // let's use fo.getName()
            }
            return name == null ? fo.getName() : name;
        }
        
        private String computeDisplayName() {
            FileObject displayRoot = contentType ? root.getParent() : root;
            FileObject displayItem = contentType ? item.getParent() : item;
            String displaySeparator = contentType ? "/" : " | "; // NOI18N
            Stack s = new Stack();
            s.push(getFileObjectName(displayItem));
            FileObject parent = displayItem.getParent();
            while (!displayRoot.getPath().equals(parent.getPath())) {
                s.push(getFileObjectName(parent));
                parent = parent.getParent();
            }
            StringBuffer sb = new StringBuffer();
            sb.append(s.pop());
            while (!s.empty()) {
                sb.append(displaySeparator + s.pop());
            }
            return sb.toString();
        }
        
    }
    
    private static final class IconFilter extends FileFilter {
        public boolean accept(File pathname) {
            return pathname.isDirectory() ||
                    pathname.getName().toLowerCase().endsWith("gif") || // NOI18N
                    pathname.getName().toLowerCase().endsWith("png"); // NOI18N
        }
        public String getDescription() {
            return "*.gif, *.png"; // NOI18N
        }
    }
    
}

