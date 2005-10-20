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
import org.netbeans.modules.versioning.system.cvss.ui.actions.log.AnnotationsAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.log.SearchHistoryAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.diff.DiffAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.diff.ResolveConflictsAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.tag.TagAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.tag.BranchAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.tag.SwitchBranchAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.tag.MergeBranchAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.commit.CommitAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.update.UpdateAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.update.GetCleanAction;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.util.FlatFolder;
import org.netbeans.modules.versioning.system.cvss.settings.CvsModuleConfig;

import javax.swing.*;
import java.util.*;
import java.text.MessageFormat;
import java.io.File;
import java.awt.*;
import java.lang.reflect.Field;

/**
 * Annotates names for display in Files and Projects view (and possible elsewhere). Uses
 * Filesystem support for this feature (to be replaced later in Core by something more generic).
 * 
 * @author Maros Sandor
 */
public class Annotator {

    private static MessageFormat uptodateFormat = getFormat("uptodateFormat");  // NOI18N
    private static MessageFormat newLocallyFormat = getFormat("newLocallyFormat");  // NOI18N
    private static MessageFormat addedLocallyFormat = getFormat("addedLocallyFormat"); // NOI18N
    private static MessageFormat modifiedLocallyFormat = getFormat("modifiedLocallyFormat"); // NOI18N
    private static MessageFormat removedLocallyFormat = getFormat("removedLocallyFormat"); // NOI18N
    private static MessageFormat deletedLocallyFormat = getFormat("deletedLocallyFormat"); // NOI18N
    private static MessageFormat newInRepositoryFormat = getFormat("newInRepositoryFormat"); // NOI18N
    private static MessageFormat modifiedInRepositoryFormat = getFormat("modifiedInRepositoryFormat"); // NOI18N
    private static MessageFormat removedInRepositoryFormat = getFormat("removedInRepositoryFormat"); // NOI18N
    private static MessageFormat conflictFormat = getFormat("conflictFormat"); // NOI18N
    private static MessageFormat mergeableFormat = getFormat("mergeableFormat"); // NOI18N
    private static MessageFormat excludedFormat = getFormat("excludedFormat"); // NOI18N

    private static final int STATUS_TEXT_ANNOTABLE = FileInformation.STATUS_NOTVERSIONED_EXCLUDED | 
            FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_VERSIONED_UPTODATE |
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY | FileInformation.STATUS_VERSIONED_CONFLICT | 
            FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY | FileInformation.STATUS_VERSIONED_DELETEDLOCALLY | 
            FileInformation.STATUS_VERSIONED_ADDEDLOCALLY;
    
    private final FileStatusCache cache;

    Annotator(CvsVersioningSystem cvs) {
        cache = cvs.getStatusCache();
        initDefaults();
    }

    private void initDefaults() {
        Field [] fields = Annotator.class.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            String name = fields[i].getName();
            if (name.endsWith("Format")) {
                initDefaultColor(name.substring(0, name.length() - 6)); 
            }
        }
    }

    private void initDefaultColor(String name) {
        String color = System.getProperty("cvs.color." + name);
        if (color == null) return;
        setAnnotationColor(name, color);
    }

    /**
     * Changes annotation color of files.
     * 
     * @param name name of the color to change. Can be one of:
     * newLocally, addedLocally, modifiedLocally, removedLocally, deletedLocally, newInRepository, modifiedInRepository, 
     * removedInRepository, conflict, mergeable, excluded.
     * @param colorString new color in the format: 4455AA (RGB hexadecimal)
     */ 
    private void setAnnotationColor(String name, String colorString) {
        try {
            Field field = Annotator.class.getDeclaredField(name + "Format");
            MessageFormat format = new MessageFormat("<font color=\"" + colorString + "\">{0}</font><font color=\"#999999\">{1}</font>");
            field.set(null, format);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid color name");
        }
    }
    
    /**
     * Adds rendering attributes to an arbitrary String based on a CVS status. The name is usually a file or folder
     * display name and status is usually its CVS status as reported by FileStatusCache. 
     * 
     * @param name name to annotate
     * @param info status that an object with the given name has
     * @param file file this annotation belongs to. It is used to determine sticky tags for textual annotations. Pass
     * null if you do not want textual annotations to appear in returned markup
     * @return String html-annotated name that can be used in Swing controls that support html rendering. Note: it may
     * also return the original name String
     */ 
    public String annotateNameHtml(String name, FileInformation info, File file) {
        int status = info.getStatus();
        String textAnnotation;
        String textAnnotationFormat = CvsModuleConfig.getDefault().getTextAnnotationsFormat();
        if (textAnnotationFormat != null && file != null && (status & STATUS_TEXT_ANNOTABLE) != 0) {
            String sticky = Utils.getSticky(file);
            if (status == FileInformation.STATUS_VERSIONED_UPTODATE && sticky == null) {
                textAnnotation = "";
            } else if (status == FileInformation.STATUS_VERSIONED_UPTODATE) {
                textAnnotation = " [" + sticky.substring(1) + "]";
            } else  if (sticky == null) {
                textAnnotation = " [" + info.getShortStatusText() + "]";
            } else {
                textAnnotation = " [" + info.getShortStatusText() + "; " + sticky.substring(1) + "]";
            }
        } else {
            textAnnotation = "";
        }
        if (textAnnotation.length() > 0) {
            textAnnotation = NbBundle.getMessage(Annotator.class, "textAnnotation", textAnnotation);
        }
        
        switch (status) {
        case FileInformation.STATUS_UNKNOWN:
        case FileInformation.STATUS_NOTVERSIONED_NOTMANAGED:
            return name;
        case FileInformation.STATUS_VERSIONED_UPTODATE:
            return uptodateFormat.format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY:
            return modifiedLocallyFormat.format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY: 
            return newLocallyFormat.format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY:
            return removedLocallyFormat.format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_VERSIONED_DELETEDLOCALLY:
            return deletedLocallyFormat.format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_VERSIONED_NEWINREPOSITORY:
            return newInRepositoryFormat.format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY:
            return modifiedInRepositoryFormat.format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY:
            return removedInRepositoryFormat.format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_VERSIONED_ADDEDLOCALLY:
            return addedLocallyFormat.format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_VERSIONED_MERGE:
            return mergeableFormat.format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_VERSIONED_CONFLICT:
            return conflictFormat.format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_NOTVERSIONED_EXCLUDED:
            return excludedFormat.format(new Object [] { name, textAnnotation });
        default:
            throw new IllegalArgumentException("Unknown status: " + status);
        }
    }

    private String annotateFolderNameHtml(String name, FileInformation info, File file) {
        int status = info.getStatus();
        String textAnnotation;
        String textAnnotationFormat = CvsModuleConfig.getDefault().getTextAnnotationsFormat();        
        if (textAnnotationFormat != null && file != null && (status & FileInformation.STATUS_MANAGED) != 0) {
            String sticky = Utils.getSticky(file);
            if (status == FileInformation.STATUS_VERSIONED_UPTODATE && sticky == null) {
                textAnnotation = "";
            } else if (status == FileInformation.STATUS_VERSIONED_UPTODATE) {
                textAnnotation = " [" + sticky.substring(1) + "]";
            } else  if (sticky == null) {
                textAnnotation = " [" + info.getShortStatusText() + "]";
            } else {
                textAnnotation = " [" + info.getShortStatusText() + "; " + sticky.substring(1) + "]";
            }
        } else {
            textAnnotation = "";
        }
        if (textAnnotation.length() > 0) {
            textAnnotation = NbBundle.getMessage(Annotator.class, "textAnnotation", textAnnotation);
        }
        
        switch (status) {
        case FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY:
        case FileInformation.STATUS_VERSIONED_DELETEDLOCALLY:
        case FileInformation.STATUS_VERSIONED_NEWINREPOSITORY:
        case FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY:
        case FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY:
        case FileInformation.STATUS_NOTVERSIONED_NOTMANAGED:
        case FileInformation.STATUS_VERSIONED_MERGE:
        case FileInformation.STATUS_UNKNOWN:
        case FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY:
        case FileInformation.STATUS_VERSIONED_CONFLICT:
            return name;
        case FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY: 
        case FileInformation.STATUS_VERSIONED_ADDEDLOCALLY:
        case FileInformation.STATUS_VERSIONED_UPTODATE:
            return uptodateFormat.format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_NOTVERSIONED_EXCLUDED:
            return excludedFormat.format(new Object [] { name, textAnnotation });
        default:
            throw new IllegalArgumentException("Unknown status: " + status);
        }
    }
    
    public String annotateNameHtml(File file, FileInformation info) {
        return annotateNameHtml(file.getName(), info, file);
    }
    
    /**
     * Annotates given name by HTML markup.
     * 
     * @param name original name of the file
     * @param files set of files comprising the name
     * @param includeStatus only files having one of statuses specified will be annotated
     * @return String HTML-annotated name of the file (without HTML prolog)
     */ 
    public String annotateNameHtml(String name, Set files, int includeStatus) {
        if (files.size() == 0) return name;
        
        FileInformation mostImportantInfo = null;
        File mostImportantFile = null;
        boolean folderAnnotation = false;
        
        for (Iterator i = files.iterator(); i.hasNext();) {
            FileObject fo = (FileObject) i.next();
            File file = FileUtil.toFile(fo);
            FileInformation info = cache.getStatus(file);
            int status = info.getStatus();
            if ((status & includeStatus) == 0) continue;
            
            if (isMoreImportant(info, mostImportantInfo)) {
                mostImportantInfo = info;
                mostImportantFile = file;
                folderAnnotation = fo.isFolder();
            }
        }

        if (folderAnnotation == false && files.size() > 1) {
            folderAnnotation = looksLikeLogicalFolder(files);
        }

        if (mostImportantInfo == null) return name;
        return folderAnnotation ? 
                annotateFolderNameHtml(name, mostImportantInfo, mostImportantFile) : 
                annotateNameHtml(name, mostImportantInfo, mostImportantFile);
    }

    private boolean isMoreImportant(FileInformation a, FileInformation b) {
        if (b == null) return true;
        if (a == null) return false;
        return Utils.getComparableStatus(a.getStatus()) < Utils.getComparableStatus(b.getStatus());
    }

    public String annotateName(String name, Set files) {
        return name;
    }

    /**
     * Annotates icon of a node based on its versioning status.
     *
     * @param roots files that the node represents
     * @param icon original node icon
     * @return Image newly annotated icon or the original one
     */
    Image annotateFolderIcon(Set roots, Image icon) {
        CvsModuleConfig config = CvsModuleConfig.getDefault();
        boolean allExcluded = true;
        boolean modified = false;

        Map map = cache.getAllModifiedFiles();
        Map modifiedFiles = new HashMap();
        for (Iterator i = map.keySet().iterator(); i.hasNext();) {
            File file = (File) i.next();
            FileInformation info = (FileInformation) map.get(file);
            if (!info.isDirectory() && (info.getStatus() & FileInformation.STATUS_LOCAL_CHANGE) != 0) modifiedFiles.put(file, info);
        }

        for (Iterator i = roots.iterator(); i.hasNext();) {
            File file = (File) i.next();
            if (file instanceof FlatFolder) {
                for (Iterator j = modifiedFiles.keySet().iterator(); j.hasNext();) {
                    File mf = (File) j.next();
                    if (mf.getParentFile().equals(file)) {
                        FileInformation info = (FileInformation) modifiedFiles.get(mf);
                        if (info.isDirectory()) continue;
                        int status = info.getStatus();
                        if (status == FileInformation.STATUS_VERSIONED_CONFLICT) {
                            Image badge = Utilities.loadImage("org/netbeans/modules/versioning/system/cvss/resources/icons/conflicts-badge.png");
                            return Utilities.mergeImages(icon, badge, 16, 9);
                        }
                        modified = true;
                        allExcluded &= config.isExcludedFromCommit(mf.getAbsolutePath());
                    }
                }
            } else {
                for (Iterator j = modifiedFiles.keySet().iterator(); j.hasNext();) {
                    File mf = (File) j.next();
                    if (Utils.isParentOrEqual(file, mf)) {
                        FileInformation info = (FileInformation) modifiedFiles.get(mf);
                        int status = info.getStatus();
                        if (status == FileInformation.STATUS_VERSIONED_CONFLICT) {
                            Image badge = Utilities.loadImage("org/netbeans/modules/versioning/system/cvss/resources/icons/conflicts-badge.png");
                            return Utilities.mergeImages(icon, badge, 16, 9);
                        }
                        modified = true;
                        allExcluded &= config.isExcludedFromCommit(mf.getAbsolutePath());
                    }
                }
            }
        }

        if (modified && !allExcluded) {
            Image badge = Utilities.loadImage("org/netbeans/modules/versioning/system/cvss/resources/icons/modified-badge.png");
            return Utilities.mergeImages(icon, badge, 16, 9);
        } else {
            return icon;
        }
    }

    /**
     * Returns array of versioning actions that may be used to construct a popup menu. These actions
     * will act on currently activated nodes.
     * 
     * @return Action[] array of versioning actions that may be used to construct a popup menu. These actions
     * will act on currently activated nodes.
     */ 
    public static Action [] getActions() {
        File [] files = Utils.getCurrentContext(null).getFiles();
        ResourceBundle loc = NbBundle.getBundle(Annotator.class);
        if (onlyFolders(files)) {
            return new Action [] {
                new SystemActionBridge(SystemAction.get(StatusAction.class), loc.getString("CTL_PopupMenuItem_Status")),
                new SystemActionBridge(SystemAction.get(DiffAction.class), loc.getString("CTL_PopupMenuItem_Diff")),
                new SystemActionBridge(SystemAction.get(UpdateAction.class), loc.getString("CTL_PopupMenuItem_Update")),
                new SystemActionBridge(SystemAction.get(CommitAction.class), loc.getString("CTL_PopupMenuItem_Commit")),
                null,
                new SystemActionBridge(SystemAction.get(TagAction.class), loc.getString("CTL_PopupMenuItem_Tag")),
                null,
                new SystemActionBridge(SystemAction.get(BranchAction.class), loc.getString("CTL_PopupMenuItem_Branch")),
                new SystemActionBridge(SystemAction.get(SwitchBranchAction.class), loc.getString("CTL_PopupMenuItem_SwitchBranch")),
                new SystemActionBridge(SystemAction.get(MergeBranchAction.class), loc.getString("CTL_PopupMenuItem_MergeBranch")),
                null,
                new SystemActionBridge(SystemAction.get(SearchHistoryAction.class), loc.getString("CTL_PopupMenuItem_SearchHistory")),
                null,
                new SystemActionBridge(SystemAction.get(GetCleanAction.class), loc.getString("CTL_PopupMenuItem_GetClean")),
                new SystemActionBridge(SystemAction.get(ResolveConflictsAction.class), loc.getString("CTL_PopupMenuItem_ResolveConflicts")),
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
                null,
                new SystemActionBridge(SystemAction.get(BranchAction.class), loc.getString("CTL_PopupMenuItem_Branch")),
                new SystemActionBridge(SystemAction.get(SwitchBranchAction.class), loc.getString("CTL_PopupMenuItem_SwitchBranch")),
                new SystemActionBridge(SystemAction.get(MergeBranchAction.class), loc.getString("CTL_PopupMenuItem_MergeBranch")),
                null,
                new SystemActionBridge(SystemAction.get(AnnotationsAction.class), 
                                        ((AnnotationsAction)SystemAction.get(AnnotationsAction.class)).visible(null) ? 
                                        loc.getString("CTL_PopupMenuItem_HideAnnotations") : 
                                        loc.getString("CTL_PopupMenuItem_ShowAnnotations")),
                new SystemActionBridge(SystemAction.get(SearchHistoryAction.class), loc.getString("CTL_PopupMenuItem_SearchHistory")),
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
            if (!files[i].exists() && !cache.getStatus(files[i]).isDirectory()) return false;
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

    private static MessageFormat getFormat(String key) {
        String format = NbBundle.getMessage(Annotator.class, key);
        return new MessageFormat(format);
    }

}
