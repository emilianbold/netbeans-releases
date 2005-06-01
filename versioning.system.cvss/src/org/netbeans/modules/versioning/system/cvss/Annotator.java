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
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.ErrorManager;
import org.netbeans.modules.versioning.system.cvss.ui.actions.status.StatusAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.SystemActionBridge;
import org.netbeans.modules.versioning.system.cvss.ui.actions.ignore.IgnoreAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.log.LogAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.log.AnnotationsAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.diff.DiffAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.diff.ResolveConflictsAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.tag.TagAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.tag.BranchAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.commit.CommitAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.update.UpdateAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.update.GetCleanAction;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.settings.CvsModuleConfig;

import javax.swing.*;
import java.util.*;
import java.awt.Image;
import java.text.MessageFormat;
import java.io.File;

/**
 * Annotates names for display in Files and Projects view (and possible elsewhere). Uses
 * Filesystem support for this feature (to be replaced later in Core by something more generic).
 * 
 * @author Maros Sandor
 */
public class Annotator {

    private static MessageFormat newLocallyFormat = new MessageFormat("<html><font color=\"#007000\">{0}</font></html>");            
    private static MessageFormat addedLocallyFormat = new MessageFormat("<html><font color=\"#007000\">{0}</font></html>");            
    private static MessageFormat modifiedLocallyFormat = new MessageFormat("<html><font color=\"#007000\">{0}</font></html>");
    private static MessageFormat removedLocallyFormat = new MessageFormat("<html><font color=\"#007000\">{0}</font></html>");
    private static MessageFormat deletedLocallyFormat = new MessageFormat("<html><font color=\"#007000\">{0}</font></html>");
    private static MessageFormat newInRepositoryFormat = new MessageFormat("<html><font color=\"#0000A0\">{0}</font></html>");       
    private static MessageFormat modifiedInRepositoryFormat = new MessageFormat("<html><font color=\"#0000A0\">{0}</font></html>");
    private static MessageFormat removedInRepositoryFormat = new MessageFormat("<html><font color=\"#0000A0\">{0}</font></html>");
    private static MessageFormat conflictFormat = new MessageFormat("<html><font color=\"#CC0000\">{0}</font></html>");
    private static MessageFormat mergeableFormat = new MessageFormat("<html><font color=\"#007000\">{0}</font></html>");
    private static MessageFormat excludedFormat = new MessageFormat("<html><font color=\"#A0A0A0\">{0}</font></html>");

    private final FileStatusCache cache;

    Annotator(CvsVersioningSystem cvs) {
        cache = cvs.getStatusCache();
    }
    
    /**
     * Adds rendering attributes to an arbitrary String based on a CVS status. The name is usually a file or folder
     * display name and status is usually its CVS status as reported by FileStatusCache. 
     * 
     * @param name name to annotate
     * @param status status that an object with the given name has
     * @return String html-annotated name that can be used in Swing controls that support html rendering. Note: it may
     * also return the original name String
     */ 
    public static String annotateNameHtml(String name, int status) {
        switch (status) {
        case FileInformation.STATUS_VERSIONED_UPTODATE:
        case FileInformation.STATUS_NOTVERSIONED_NOTMANAGED:
        case FileInformation.STATUS_UNKNOWN:
            return name;
        case FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY:
            return modifiedLocallyFormat.format(new Object [] { name });
        case FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY: 
            return newLocallyFormat.format(new Object [] { name });
        case FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY:
            return removedLocallyFormat.format(new Object [] { name });
        case FileInformation.STATUS_VERSIONED_DELETEDLOCALLY:
            return deletedLocallyFormat.format(new Object [] { name });
        case FileInformation.STATUS_VERSIONED_NEWINREPOSITORY:
            return newInRepositoryFormat.format(new Object [] { name });
        case FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY:
            return modifiedInRepositoryFormat.format(new Object [] { name });
        case FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY:
            return removedInRepositoryFormat.format(new Object [] { name });
        case FileInformation.STATUS_VERSIONED_ADDEDLOCALLY:
            return addedLocallyFormat.format(new Object [] { name });
        case FileInformation.STATUS_VERSIONED_MERGE:
            return mergeableFormat.format(new Object [] { name });
        case FileInformation.STATUS_VERSIONED_CONFLICT:
            return conflictFormat.format(new Object [] { name });
        case FileInformation.STATUS_NOTVERSIONED_EXCLUDED:
            return excludedFormat.format(new Object [] { name });
        default:
            throw new IllegalArgumentException("Unknown status: " + status);
        }
    }

    private static String annotateFolderNameHtml(String name, int status) {
        switch (status) {
        case FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY:
        case FileInformation.STATUS_VERSIONED_DELETEDLOCALLY:
        case FileInformation.STATUS_VERSIONED_NEWINREPOSITORY:
        case FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY:
        case FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY:
        case FileInformation.STATUS_NOTVERSIONED_NOTMANAGED:
        case FileInformation.STATUS_VERSIONED_UPTODATE:
        case FileInformation.STATUS_VERSIONED_MERGE:
        case FileInformation.STATUS_UNKNOWN:
        case FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY:
        case FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY: 
        case FileInformation.STATUS_VERSIONED_ADDEDLOCALLY:
        case FileInformation.STATUS_VERSIONED_CONFLICT:
            return name;
        case FileInformation.STATUS_NOTVERSIONED_EXCLUDED:
            return excludedFormat.format(new Object [] { name });
        default:
            throw new IllegalArgumentException("Unknown status: " + status);
        }
    }
    
    public static String annotateNameHtml(File file, int status) {
        return annotateNameHtml(file.getName(), status);
    }
    
    /**
     * Annotates given name by HTML markup.
     * 
     * @param name original name of the file
     * @param files set of files comprising the name
     * @param includeStatus only files having one of statuses specified will be annotated
     * @return String HTML-annotated name of the file
     */ 
    public String annotateNameHtml(String name, Set files, int includeStatus) {
        if (files.size() == 0) return name;
        
        int lastStatus = -1;
        boolean folderAnnotation = false;
        
        for (Iterator i = files.iterator(); i.hasNext();) {
            FileObject fo = (FileObject) i.next();
            int status = cache.getStatus(FileUtil.toFile(fo)).getStatus();
            if ((status & includeStatus) == 0) continue;
            
            if (lastStatus == -1) {
                lastStatus = status;
                folderAnnotation = fo.isFolder();
            } else {
                if (status != lastStatus) return name;
            }
        }

        if (folderAnnotation == false && files.size() > 1) {
            folderAnnotation = looksLikeLogicalFolder(files);
        }

        if (lastStatus == -1) return name;
        return folderAnnotation ? annotateFolderNameHtml(name, lastStatus) : annotateNameHtml(name, lastStatus);
    }

    public String annotateName(String name, Set files) {
        return name;
    }

    /**
     * Adds a badge to folders that contain modified/conflicting files. 
     * 
     * @param icon original icon
     * @param iconType size and type of the original icon 
     * @param files set of files to annotate
     * @return badged or original icon based on status of files in folders
     */ 
    Image annotateFolderIcon(Image icon, int iconType, Set files) throws FileStatusCache.InformationUnavailableException {
        File [] rootFiles = (File[]) files.toArray(new File[files.size()]);

        CvsFileNode [] nodes = CvsVersioningSystem.getInstance().getFileTableModel(rootFiles, FileInformation.STATUS_LOCAL_CHANGE).getNodesCached();
        if (nodes.length == 0) {
            return icon;
        }

        boolean allExcluded = true;
        for (int i = 0; i < nodes.length; i++) {
            CvsFileNode node = nodes[i];
            int status = node.getInformation().getStatus();
            if (status == FileInformation.STATUS_VERSIONED_CONFLICT) {
                Image badge = Utilities.loadImage("org/netbeans/modules/versioning/system/cvss/resources/icons/conflicts-badge.png");
                return Utilities.mergeImages(icon, badge, 16, 9);
            }
            CvsModuleConfig config = CvsModuleConfig.getDefault();
            allExcluded &= config.isExcludedFromCommit(node.getFile().getAbsolutePath());
        }
        if (allExcluded) {
            return icon;
        } else {
            Image badge = Utilities.loadImage("org/netbeans/modules/versioning/system/cvss/resources/icons/modified-badge.png");
            return Utilities.mergeImages(icon, badge, 16, 9);
        }
    }

    private static final ResourceBundle loc = NbBundle.getBundle(Annotator.class);
    
    /**
     * Returns array of versioning actions that may be used to construct a popup menu. These actions
     * will act on currently activated nodes.
     * 
     * @return Action[] array of versioning actions that may be used to construct a popup menu. These actions
     * will act on currently activated nodes.
     */ 
    public static Action [] getActions() {
        File [] files = Utils.getActivatedFiles();
        if (onlyFolders(files)) {
            return new Action [] {
                new SystemActionBridge(SystemAction.get(StatusAction.class), loc.getString("CTL_PopupMenuItem_Status")),
                new SystemActionBridge(SystemAction.get(DiffAction.class), loc.getString("CTL_PopupMenuItem_Diff")),
                new SystemActionBridge(SystemAction.get(UpdateAction.class), loc.getString("CTL_PopupMenuItem_Update")),
                new SystemActionBridge(SystemAction.get(CommitAction.class), loc.getString("CTL_PopupMenuItem_Commit")),
                null,
                new SystemActionBridge(SystemAction.get(TagAction.class), loc.getString("CTL_PopupMenuItem_Tag")),
                new SystemActionBridge(SystemAction.get(BranchAction.class), loc.getString("CTL_PopupMenuItem_Branch")),
                null,
                new SystemActionBridge(SystemAction.get(IgnoreAction.class), 
                                       ((IgnoreAction)SystemAction.get(IgnoreAction.class)).getActionStatus() == IgnoreAction.UNIGNORING ? 
                                       loc.getString("CTL_PopupMenuItem_Unignore") : 
                                       loc.getString("CTL_PopupMenuItem_Ignore")),
            };
        } else {
            return new Action [] {
                new SystemActionBridge(SystemAction.get(StatusAction.class), loc.getString("CTL_PopupMenuItem_Status")),
                new SystemActionBridge(SystemAction.get(DiffAction.class), loc.getString("CTL_PopupMenuItem_Diff")),
                new SystemActionBridge(SystemAction.get(UpdateAction.class), loc.getString("CTL_PopupMenuItem_Update")),
                new SystemActionBridge(SystemAction.get(CommitAction.class), loc.getString("CTL_PopupMenuItem_Commit")),
                null,
                new SystemActionBridge(SystemAction.get(TagAction.class), loc.getString("CTL_PopupMenuItem_Tag")),
                new SystemActionBridge(SystemAction.get(BranchAction.class), loc.getString("CTL_PopupMenuItem_Branch")),
                null,
                new SystemActionBridge(SystemAction.get(AnnotationsAction.class), loc.getString("CTL_PopupMenuItem_Annotations")),
                new SystemActionBridge(SystemAction.get(LogAction.class), loc.getString("CTL_PopupMenuItem_Log")),
                null,
                new SystemActionBridge(SystemAction.get(GetCleanAction.class), loc.getString("CTL_PopupMenuItem_GetClean")),
                new SystemActionBridge(SystemAction.get(ResolveConflictsAction.class), loc.getString("CTL_PopupMenuItem_ResolveConflicts")),
                new SystemActionBridge(SystemAction.get(IgnoreAction.class), 
                                       ((IgnoreAction)SystemAction.get(IgnoreAction.class)).getActionStatus() == IgnoreAction.UNIGNORING ? 
                                       loc.getString("CTL_PopupMenuItem_Unignore") : 
                                       loc.getString("CTL_PopupMenuItem_Ignore")),
            };
        }
    }

    private static boolean onlyFolders(File[] files) {
        FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) return false;
            if (!files[i].exists() && (cache.getStatus(files[i]).getStatus() & FileInformation.FLAG_DIRECTORY) == 0) return false;
        }
        return true;
    }

    /**
     * try to distinguish between logical containes (e.g. "Important Files"
     * keeping manifest, arch, ..) and multi data objects (.form);
     */
    static boolean looksLikeLogicalFolder(Set files) {
        Iterator it = files.iterator();
        FileObject fo = (FileObject) it.next();
        try {
            DataObject etalon = DataObject.find(fo);
            while (it.hasNext()) {
                FileObject fileObject = (FileObject) it.next();
                if (etalon.equals(DataObject.find(fileObject)) == false) {
                    return true;
                }
            }
        } catch (DataObjectNotFoundException e) {
            ErrorManager err = ErrorManager.getDefault();
            err.annotate(e, "Can not find dataobject, annottaing as logical folder");  // NOI18N
            err.notify(e);
            return true;
        }
        return false;
    }
}
