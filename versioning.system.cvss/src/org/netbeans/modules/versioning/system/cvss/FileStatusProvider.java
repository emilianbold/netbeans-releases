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

package org.netbeans.modules.versioning.system.cvss;

import org.openide.filesystems.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.versioning.spi.VersioningListener;
import org.netbeans.modules.versioning.spi.VersioningEvent;
import org.netbeans.modules.versioning.system.cvss.ui.actions.CvsCommandsMenuItem;
import org.netbeans.modules.versioning.system.cvss.util.FlatFolder;
import org.netbeans.modules.masterfs.providers.AnnotationProvider;
import org.netbeans.modules.masterfs.providers.InterceptionListener;
import org.netbeans.api.fileinfo.NonRecursiveFolder;

import javax.swing.*;
import java.util.*;
import java.awt.Image;
import java.io.File;

/**
 * Contract specific for Filesystem <-> UI interaction, to be replaced later with something more
 * sophisticated (hopefuly).
 *
 * <p>It's registered in default lookup (META-INF/services).
 * 
 * @author Maros Sandor
 */
public class FileStatusProvider extends AnnotationProvider implements VersioningListener {

    private Annotator       annotator;
    private FileStatusCache cache;

    private final Set foldersToCompute = new HashSet(); 
    private RequestProcessor.Task       computeIconsTask;

    private static boolean alreadyCreated;

    public FileStatusProvider() {
        synchronized(FileStatusProvider.class) {
            assert alreadyCreated == false : "It must be singleton, otherwise two paralel computeIconsTasks, ..."; // NOI18N
            alreadyCreated = true;
        }
        computeIconsTask = RequestProcessor.getDefault().create(new ComputeIconTask());
    }

    private Annotator getAnnotator() {
        if (annotator == null) {
            annotator = CvsVersioningSystem.getInstance().getAnnotator();
            cache = CvsVersioningSystem.getInstance().getStatusCache();
            cache.addVersioningListener(this);
        }
        return annotator;
    }

    public String annotateNameHtml(String name, Set files) {
        return getAnnotator().annotateNameHtml(name, files, FileInformation.STATUS_LOCAL_CHANGE | FileInformation.STATUS_NOTVERSIONED_EXCLUDED);
    }
    
    public String annotateName(String name, Set files) {
        return getAnnotator().annotateName(name, files);
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
            return icon;
        }

        roots = Collections.unmodifiableSet(roots);

        try {
            return getAnnotator().annotateFolderIcon(icon, iconType, roots);
        } catch (FileStatusCache.InformationUnavailableException e) {
            // we need to launch a task to scan these directories
            synchronized(foldersToCompute) {
                foldersToCompute.add(roots);
                computeIconsTask.schedule(1000);
                return icon;
            }
        }
    }

    public Action[] actions(Set files) {
        return new Action[] {
            SystemAction.get(CvsCommandsMenuItem.class)
        };
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
        FileObject fo = FileUtil.toFileObject(file);
        if (fo != null) {
            try {
                fireFileStatusChanged(new FileStatusEvent(fo.getFileSystem(), fo, false, true));
            } catch (FileStateInvalidException e) {
                // ignore files in invalid filesystems
            }
        }
        fo = FileUtil.toFileObject(file.getParentFile());
        try {
            for (; fo != null; fo = fo.getParent()) {
                fireFileStatusChanged(new FileStatusEvent(fo.getFileSystem(), fo, true, false));
            }
        } catch (FileStateInvalidException e) {
            // ignore files in invalid filesystems
        }
    }

    private class ComputeIconTask implements Runnable {
        
        public void run() {
            Set toCompute = new HashSet();
            for (;;) {
                synchronized(foldersToCompute) {
                    if (foldersToCompute.size() == 0) return;
                    toCompute.clear();
                    toCompute.addAll(foldersToCompute);
                }
                for (Iterator i = toCompute.iterator(); i.hasNext();) {
                    Set roots = (Set) i.next();
                    CvsVersioningSystem.getInstance().getFileTableModel((File[]) roots.toArray(new File[roots.size()]), FileInformation.STATUS_LOCAL_CHANGE).getNodes();
                }
                synchronized(foldersToCompute) {
                    foldersToCompute.removeAll(toCompute);
                }
                for (Iterator i = toCompute.iterator(); i.hasNext();) {
                    Set roots = (Set) i.next();
                    for (Iterator j = roots.iterator(); j.hasNext();) {
                        File file = (File) j.next();
                        FileObject fo = FileUtil.toFileObject(file);
                        try {
                            fireFileStatusChanged(new FileStatusEvent(fo.getFileSystem(), fo, true, false));
                        } catch (FileStateInvalidException e) {
                            // ignore this state
                        }
                    }
                }
            }
        }
    }
}
