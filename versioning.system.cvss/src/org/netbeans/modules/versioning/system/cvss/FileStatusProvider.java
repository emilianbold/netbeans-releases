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
import org.netbeans.modules.versioning.util.FlatFolder;
import org.netbeans.modules.versioning.system.cvss.ui.actions.CvsCommandsMenuItem;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSContext;
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
class FileStatusProvider extends VCSAnnotator {

    private static final int STATUS_BADGEABLE = FileInformation.STATUS_VERSIONED_UPTODATE | FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;

    private static final Action[] EMPTY_ACTIONS = new Action[0];

    private boolean shutdown; 

    public String annotateName(String name, VCSContext context) {
        if (shutdown) return null;
        return CvsVersioningSystem.getInstance().getAnnotator().annotateNameHtml(name, context, FileInformation.STATUS_VERSIONED_UPTODATE | FileInformation.STATUS_LOCAL_CHANGE | FileInformation.STATUS_NOTVERSIONED_EXCLUDED);
    }

    public Image annotateIcon(Image icon, VCSContext context) {
        if (shutdown) return null;
        return CvsVersioningSystem.getInstance().getAnnotator().annotateIcon(icon, context);
    }

    public Action[] getActions(VCSContext context) {
        return new Action[] {
            SystemAction.get(CvsCommandsMenuItem.class)
        };
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
    }
}
