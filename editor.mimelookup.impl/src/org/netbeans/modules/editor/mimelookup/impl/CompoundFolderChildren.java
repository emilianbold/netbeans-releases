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

package org.netbeans.modules.editor.mimelookup.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository;

/**
 *
 * @author vita, Jesse Glick
 */
public final class CompoundFolderChildren implements FileChangeListener {

    public static final String PROP_CHILDREN = "FolderChildren.PROP_CHILDREN"; //NOI18N

    private static final String HIDDEN_ATTR_NAME = "hidden"; //NOI18N
    
    private static final Logger LOG = Logger.getLogger(CompoundFolderChildren.class.getName());
    
    private final String LOCK = new String("CompoundFolderChildren.LOCK"); //NOI18N
    private final List<String> prefixes;
    private final boolean includeSubfolders;
    private List<FileObject> children;
    private FileObject mergedLayers; // just hold a strong ref so listeners remain active
    private final FileChangeListener weakFCL = FileUtil.weakFileChangeListener(this, null);
    
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public CompoundFolderChildren(String [] paths) {
        this(paths, true);
    }
    
    public CompoundFolderChildren(String [] paths, boolean includeSubfolders) {
        prefixes = new ArrayList<String>();
        for (String path : paths) {
            prefixes.add(path.endsWith("/") ? path : path + "/"); // NOI18N
        }
        this.includeSubfolders = includeSubfolders;
        rebuild();
    }
    
    public List<FileObject> getChildren() {
        synchronized (LOCK) {
            return children;
        }
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    private void rebuild() {
        PropertyChangeEvent event = null;
        synchronized (LOCK) {
            List<FileObject> folders = new ArrayList<FileObject>(prefixes.size());
            List<FileSystem> layers = new ArrayList<FileSystem>(prefixes.size());
            FileSystem sfs = Repository.getDefault().getDefaultFileSystem();
            for (final String prefix : prefixes) {
                FileObject layer = sfs.findResource(prefix);
                if (layer != null && layer.isFolder()) {
                    folders.add(layer);
                    layers.add(new MultiFileSystem(new FileSystem[] {sfs}) {
                        protected @Override FileObject findResourceOn(FileSystem fs, String res) {
                            return fs.findResource(prefix + res);
                        }
                    });
                } else {
                    // Listen to nearest enclosing parent, in case it is created.
                    // XXX would be simpler to use FileChangeSupport but that is in ant/project for now.
                    String parentPath = prefix;
                    while (true) {
                        assert parentPath.length() > 0;
                        parentPath = parentPath.substring(0, Math.max(0, parentPath.lastIndexOf('/')));
                        FileObject parent = sfs.findResource(parentPath);
                        if (parent != null) {
                            parent.removeFileChangeListener(weakFCL);
                            parent.addFileChangeListener(weakFCL);
                            break;
                        }
                    }
                }
            }
            mergedLayers = new MultiFileSystem(layers.toArray(new FileSystem[layers.size()])).getRoot();
            mergedLayers.addFileChangeListener(this); // need not be weak since only we hold this FS
            List<FileObject> unsorted = new ArrayList<FileObject>();
            for (FileObject f : mergedLayers.getChildren()) {
                if ((includeSubfolders || f.isData()) && !Boolean.TRUE.equals(f.getAttribute(HIDDEN_ATTR_NAME))) {
                    f.addFileChangeListener(this);
                    unsorted.add(f);
                }
            }
            List<FileObject> sorted = new ArrayList<FileObject>(unsorted.size());
            for (FileObject merged : FileUtil.getOrder(unsorted, true)) {
                String name = merged.getNameExt();
                FileObject original = null;
                for (FileObject folder : folders) {
                    original = folder.getFileObject(name);
                    if (original != null) {
                        break;
                    }
                }
                assert original != null : "Should have equivalent to " + name + " among " + folders;
                sorted.add(original);
            }
            if (children != null && !sorted.equals(children)) {
                event = new PropertyChangeEvent(this, PROP_CHILDREN, children, sorted);
            }
            children = sorted;
        }
        if (event != null) {
            pcs.firePropertyChange(event);
        }
    }

    public void fileFolderCreated(FileEvent fe) {
        rebuild();
    }

    public void fileDataCreated(FileEvent fe) {
        rebuild();
    }

    public void fileChanged(FileEvent fe) {
        pcs.firePropertyChange(PROP_CHILDREN, null, null);
    }

    public void fileDeleted(FileEvent fe) {
        rebuild();
    }

    public void fileRenamed(FileRenameEvent fe) {
        rebuild();
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
        if (FileUtil.affectsOrder(fe)) {
            rebuild();
        }
    }

}
