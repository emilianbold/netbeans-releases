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

package org.netbeans.modules.versioning.system.cvss;

import org.openide.filesystems.*;
import org.openide.util.actions.SystemAction;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.util.VersioningEvent;
import org.netbeans.modules.versioning.system.cvss.ui.actions.CvsCommandsMenuItem;
import org.netbeans.modules.versioning.system.cvss.util.FlatFolder;
import org.netbeans.modules.versioning.system.cvss.settings.CvsModuleConfig;
import org.netbeans.modules.masterfs.providers.AnnotationProvider;
import org.netbeans.modules.masterfs.providers.InterceptionListener;
import org.netbeans.api.fileinfo.NonRecursiveFolder;

import javax.swing.*;
import java.util.*;
import java.awt.Image;
import java.io.File;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Contract specific for Filesystem <-> UI interaction, to be replaced later with something more
 * sophisticated (hopefuly).
 *
 * <p>It's registered in default lookup (META-INF/services).
 * 
 * @author Maros Sandor
 */
public class FileStatusProvider extends AnnotationProvider implements VersioningListener, PropertyChangeListener {

    private static final int STATUS_BADGEABLE = FileInformation.STATUS_VERSIONED_UPTODATE | FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;

    private static final Action[] EMPTY_ACTIONS = new Action[0];

    private static FileStatusProvider instance;
    private boolean shutdown; 

    public FileStatusProvider() {
        instance = this;
    }

    public static FileStatusProvider getInstance() {
        return instance;
    }

    public String annotateNameHtml(String name, Set files) {
        if (shutdown) return null;
        if (isManaged(files)) {
            return CvsVersioningSystem.getInstance().getAnnotator().annotateNameHtml(name, files, FileInformation.STATUS_VERSIONED_UPTODATE | FileInformation.STATUS_LOCAL_CHANGE | FileInformation.STATUS_NOTVERSIONED_EXCLUDED);
        } else {
            return null;
        }
    }
    
    public String annotateName(String name, Set files) {
        if (shutdown) return null;
        if (isManaged(files)) {
            return CvsVersioningSystem.getInstance().getAnnotator().annotateName(name, files);
        } else {
            return null;
        }
    }

    /**
     * Adds a badge to folders that contain modified/conflicting files. This badging always happens asynchronously
     * because it can take a long time and cannot be easily predicted. 
     * 
     * @param icon original icon
     * @param iconType size and type of the original icon 
     * @param files set of files to annotate
     * @return badged or original icon based on status of files in folders
     */ 
    public Image annotateIcon(Image icon, int iconType, Set files) {
        if (shutdown) return null;
        Set roots = new HashSet();
        boolean folderAnnotation = false;
        if (files instanceof NonRecursiveFolder) {
            FileObject folder = ((NonRecursiveFolder) files).getFolder();
            roots.add(new FlatFolder(FileUtil.toFile(folder).getAbsolutePath()));
            folderAnnotation = true;
        } else {
            for (Iterator i = files.iterator(); i.hasNext();) {
                FileObject fo = (FileObject) i.next();
                if (fo.isFolder()) {
                    folderAnnotation = true;
                }
                roots.add(FileUtil.toFile(fo));
            }
        }

        if (folderAnnotation == false && files.size() > 1) {
            folderAnnotation = Annotator.looksLikeLogicalFolder(files);
        }

        if (folderAnnotation == false) {
            return null;
        }

        FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();
        boolean isVersioned = false;
        for (Iterator i = roots.iterator(); i.hasNext();) {
            File file = (File) i.next();
            if ((cache.getStatus(file).getStatus() & STATUS_BADGEABLE) != 0) {  
                isVersioned = true;
                break;
            }
        }
        if (!isVersioned) return null;

        return CvsVersioningSystem.getInstance().getAnnotator().annotateFolderIcon(roots, icon);
    }

    public Action[] actions(Set files) {
        if (isManaged(files)) {
            return new Action[] {
                SystemAction.get(CvsCommandsMenuItem.class)
            };
        } else {
            return null;
        }
    }

    public InterceptionListener getInterceptionListener() {
        return CvsVersioningSystem.getInstance().getFileSystemHandler();
    }

    public void versioningEvent(VersioningEvent event) {
        if (event.getId() == FileStatusCache.EVENT_FILE_STATUS_CHANGED) {
            File file = (File) event.getParams()[0];
            fireFileStatusEvent(file);
        }
    }

    /**
     * Fire name change for given file and icon change
     * for all parents.
     */
    public void fireFileStatusEvent(File file) {
        Map folders = new HashMap();
        for (File parent = file.getParentFile(); parent != null; parent = parent.getParentFile()) {
            try {
                FileObject fo = FileUtil.toFileObject(parent);
                if (fo != null) {
                    FileSystem fs = fo.getFileSystem();
                    Set fsFolders = (Set) folders.get(fs);
                    if (fsFolders == null) {
                        fsFolders = new HashSet();
                        folders.put(fs, fsFolders);
                    }
                    fsFolders.add(fo);
                }
            } catch (FileStateInvalidException e) {
                // ignore files in invalid filesystems
            }
        }
        FileObject fo = FileUtil.toFileObject(file);
        if (fo != null) {
            try {
                fireFileStatusChanged(new FileStatusEvent(fo.getFileSystem(), fo, false, true));
            } catch (FileStateInvalidException e) {
                // ignore files in invalid filesystems
            }
        }
        for (Iterator i = folders.keySet().iterator(); i.hasNext();) {
            FileSystem fs = (FileSystem) i.next();
            Set files = (Set) folders.get(fs);
            fireFileStatusChanged(new FileStatusEvent(fs, files, true, false));
        }
    }

    /**
     * @return true if at least one file is managed (any parent
     * has <tt>.svn/entries</tt> and it is not explicitly marked
     * as unmanaged (future user action feature))
     */
    private static boolean isManaged(Set fileObjects) {
        boolean managed  = false;
        FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();
        Iterator it = fileObjects.iterator();
        while (it.hasNext()) {
            FileObject fo = (FileObject) it.next();
            File file = FileUtil.toFile(fo);
            if (file == null) {
                continue;
            }
        
            if ((cache.getStatus(file).getStatus() & FileInformation.STATUS_MANAGED) != 0) {
                return true;
            }
        }
        return false;
    }

    void shutdown() {
        shutdown = true;
        CvsModuleConfig.getDefault().removePropertyChangeListener(this);        
        refreshAllAnnotations(true, true);
    }

    void init() {
        CvsModuleConfig.getDefault().addPropertyChangeListener(this);        
    }

    /**
     * Called upon startup and shutdown of the module. This is required to show/remove CVS badges and other annotations.
     */ 
    private void refreshModifiedFiles() {
        Map files = CvsVersioningSystem.getInstance().getStatusCache().getAllModifiedFiles();
        for (Iterator i = files.keySet().iterator(); i.hasNext();) {
            File file = (File) i.next();
            fireFileStatusEvent(file);
        }
    }
    
    public void refreshAllAnnotations(boolean icon, boolean text) {
        Set filesystems = new HashSet(1);
        File[] allRoots = File.listRoots();
        for (int i = 0; i < allRoots.length; i++) {
            File root = allRoots[i];
            FileObject fo = FileUtil.toFileObject(root);
            if (fo != null) {
                try {
                    filesystems.add(fo.getFileSystem());
                } catch (FileStateInvalidException e) {
                    // ignore invalid filesystems
                }
            }
        }
        for (Iterator i = filesystems.iterator(); i.hasNext();) {
            FileSystem fileSystem = (FileSystem) i.next();
            fireFileStatusChanged(new FileStatusEvent(fileSystem, icon, text));                
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (CvsModuleConfig.PROP_TEXT_ANNOTATIONS_FORMAT.equals(evt.getPropertyName())) {
            refreshAllAnnotations(false, true);
        }
    }
}
