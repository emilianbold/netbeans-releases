/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.subversion;

import org.netbeans.modules.subversion.ui.actions.*;
import org.netbeans.modules.subversion.ui.copy.*;
import org.netbeans.modules.subversion.ui.ignore.IgnoreAction;
import org.netbeans.modules.subversion.ui.status.StatusAction;
import org.netbeans.modules.subversion.ui.commit.CommitAction;
import org.netbeans.modules.subversion.ui.update.UpdateAction;
import org.netbeans.modules.subversion.ui.update.RevertModificationsAction;
import org.netbeans.modules.subversion.ui.update.ResolveConflictsAction;
import org.netbeans.modules.subversion.ui.diff.DiffAction;
import org.netbeans.modules.subversion.ui.blame.BlameAction;
import org.netbeans.modules.subversion.ui.history.SearchHistoryAction;
import org.openide.filesystems.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.Lookup;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.subversion.util.FlatFolder;
import org.netbeans.modules.subversion.settings.SvnModuleConfig;

import javax.swing.*;
import java.util.*;
import java.util.regex.Pattern;
import java.text.MessageFormat;
import java.io.File;
import java.awt.*;
import java.lang.reflect.Field;
import org.tigris.subversion.svnclientadapter.*;

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

    private static final Pattern lessThan = Pattern.compile("<");  // NOI18N
    
    private final FileStatusCache cache;
    private MessageFormat format;

    Annotator(Subversion svn) {
        this.cache = svn.getStatusCache();
        initDefaults();
    }

    private void initDefaults() {
        Field [] fields = Annotator.class.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            String name = fields[i].getName();
            if (name.endsWith("Format")) {  // NOI18N
                initDefaultColor(name.substring(0, name.length() - 6)); 
            }
        }

        String string = System.getProperty("netbeans.experimental.svn.ui.statusLabelFormat");  // NOI18N
        if (string != null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, "SVN status labels use format \"" + string + "\" where:"); // NOI18N
            ErrorManager.getDefault().log(ErrorManager.WARNING, "\t{0} stays for revision"); // NOI18N
            ErrorManager.getDefault().log(ErrorManager.WARNING, "\t{1} stays for status"); // NOI18N
            ErrorManager.getDefault().log(ErrorManager.WARNING, "\t{2} stays for branch or sticky tag"); // NOI18N
            ErrorManager.getDefault().log(ErrorManager.WARNING, "\t{3} stays for binary flag"); // NOI18N
            format = new MessageFormat(string);
        }
    }

    private void initDefaultColor(String name) {
        String color = System.getProperty("svn.color." + name);  // NOI18N
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
            Field field = Annotator.class.getDeclaredField(name + "Format");  // NOI18N
            MessageFormat format = new MessageFormat("<font color=\"" + colorString + "\">{0}</font><font color=\"#999999\">{1}</font>");  // NOI18N
            field.set(null, format);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid color name");  // NOI18N
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
        name = htmlEncode(name);
        int status = info.getStatus();
        String textAnnotation;
        String textAnnotationFormat = SvnModuleConfig.getDefault().getTextAnnotationsFormat();
        if (textAnnotationFormat != null && file != null && (status & STATUS_TEXT_ANNOTABLE) != 0) {
            if (format != null) {
                textAnnotation = formatAnnotation(info, file);
            } else {
                String sticky = SvnUtils.getCopy(file);
                if (status == FileInformation.STATUS_VERSIONED_UPTODATE && sticky == null) {
                    textAnnotation = "";  // NOI18N
                } else if (status == FileInformation.STATUS_VERSIONED_UPTODATE) {
                    textAnnotation = " [" + sticky + "]"; // NOI18N
                } else  if (sticky == null) {
                    textAnnotation = " [" + info.getShortStatusText() + "]"; // NOI18N
                } else {
                    textAnnotation = " [" + info.getShortStatusText() + "; " + sticky + "]"; // NOI18N
                }
            }
        } else {
            textAnnotation = ""; // NOI18N
        }
        if (textAnnotation.length() > 0) {
            textAnnotation = NbBundle.getMessage(Annotator.class, "textAnnotation", textAnnotation); 
        }

        // aligned with SvnUtils.getComparableStatus

        if (0 != (status & FileInformation.STATUS_VERSIONED_CONFLICT)) {
            return conflictFormat.format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MERGE)) {
            return mergeableFormat.format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_DELETEDLOCALLY)) {
            return deletedLocallyFormat.format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) {
            return removedLocallyFormat.format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)) {
            return newLocallyFormat.format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
            return addedLocallyFormat.format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY)) {
            return modifiedLocallyFormat.format(new Object [] { name, textAnnotation });

        // repository changes - lower annotator priority

        } else if (0 != (status & FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY)) {
            return removedInRepositoryFormat.format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_NEWINREPOSITORY)) {
            return newInRepositoryFormat.format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY)) {
            return modifiedInRepositoryFormat.format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_UPTODATE)) {
            return uptodateFormat.format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_EXCLUDED)) {
            return excludedFormat.format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NOTMANAGED)) {
            return name;
        } else if (status == FileInformation.STATUS_UNKNOWN) {
            return name;
        } else {
            throw new IllegalArgumentException("Uncomparable status: " + status); // NOI18N
        }
    }

    /**
     * Applies custom format.
     */
    private String formatAnnotation(FileInformation info, File file) {
        String statusString = "";  // NOI18N
        int status = info.getStatus();
        if (status != FileInformation.STATUS_VERSIONED_UPTODATE) {
            statusString = info.getShortStatusText();
        }

        String revisionString = ""; // NOI18N
        String binaryString = ""; // NOI18N
/*
        Entry entry = info.getEntry(file);
        if (entry != null) {
            revisionString = entry.getRevision();
            binaryString = entry.getOptions();
            if ("-kb".equals(binaryString) == false) { // NOI18N
                binaryString = ""; // NOI18N
            }
        }
*/

        String stickyString = SvnUtils.getCopy(file);
        if (stickyString == null) {
            stickyString = ""; // NOI18N
        }

        Object[] arguments = new Object[] {
            revisionString,
            statusString,
            stickyString,
            binaryString
        };
        return format.format(arguments, new StringBuffer(), null).toString().trim();
    }

    private String annotateFolderNameHtml(String name, FileInformation info, File file) {
        name = htmlEncode(name);
        int status = info.getStatus();
        String textAnnotation;
        String textAnnotationFormat = SvnModuleConfig.getDefault().getTextAnnotationsFormat();        
        if (textAnnotationFormat != null && file != null && (status & FileInformation.STATUS_MANAGED) != 0) {

            String sticky;
            ISVNStatus lstatus = info.getEntry(file);
            if (lstatus != null && lstatus.getUrl() != null) {
                sticky = SvnUtils.getCopy(lstatus.getUrl());
            } else {
                // slower
                sticky = SvnUtils.getCopy(file);
            }

            if (status == FileInformation.STATUS_VERSIONED_UPTODATE && sticky == null) {
                textAnnotation = ""; // NOI18N
            } else if (status == FileInformation.STATUS_VERSIONED_UPTODATE) {
                textAnnotation = " [" + sticky + "]"; // NOI18N
            } else  if (sticky == null) {
                textAnnotation = " [" + info.getShortStatusText() + "]"; // NOI18N
            } else {
                textAnnotation = " [" + info.getShortStatusText() + "; " + sticky + "]"; // NOI18N
            }
        } else {
            textAnnotation = ""; // NOI18N
        }
        if (textAnnotation.length() > 0) {
            textAnnotation = NbBundle.getMessage(Annotator.class, "textAnnotation", textAnnotation);
        }
        
        if (status == FileInformation.STATUS_UNKNOWN) {
            return name;
        } else if (match(status, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) {
            return name;
        } else if (match(status, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)) {
            return uptodateFormat.format(new Object [] { name, textAnnotation });
        } else if (match(status, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
            return uptodateFormat.format(new Object [] { name, textAnnotation });
        } else if (match(status, FileInformation.STATUS_VERSIONED_UPTODATE)) {
            return uptodateFormat.format(new Object [] { name, textAnnotation });
        } else if (match(status, FileInformation.STATUS_NOTVERSIONED_EXCLUDED)) {
            return excludedFormat.format(new Object [] { name, textAnnotation });
        } else if (match(status, FileInformation.STATUS_VERSIONED_DELETEDLOCALLY)) {
            return name;
        } else if (match(status, FileInformation.STATUS_VERSIONED_NEWINREPOSITORY)) {
            return name;
        } else if (match(status, FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY)) {
            return name;
        } else if (match(status, FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY)) {
            return name;
        } else if (match(status, FileInformation.STATUS_NOTVERSIONED_NOTMANAGED)) {
            return name;
        } else if (match(status, FileInformation.STATUS_VERSIONED_MERGE)) {
            return name;
        } else if (match(status, FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY)) {
            return name;
        } else if (match(status, FileInformation.STATUS_VERSIONED_CONFLICT)) {
            return name;
        } else {
            throw new IllegalArgumentException("Unknown status: " + status); // NOI18N
        }
    }

    private static boolean match(int status, int mask) {
        return (status & mask) != 0;
    }

    private String htmlEncode(String name) {
        if (name.indexOf('<') == -1) return name;
        return lessThan.matcher(name).replaceAll("&lt;"); // NOI18N
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
    String annotateNameHtml(String name, Set files, int includeStatus) {
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

        if (mostImportantInfo == null) return null;
        return folderAnnotation ? 
                annotateFolderNameHtml(name, mostImportantInfo, mostImportantFile) : 
                annotateNameHtml(name, mostImportantInfo, mostImportantFile);
    }

    private boolean isMoreImportant(FileInformation a, FileInformation b) {
        if (b == null) return true;
        if (a == null) return false;
        return SvnUtils.getComparableStatus(a.getStatus()) < SvnUtils.getComparableStatus(b.getStatus());
    }

    String annotateName(String name, Set files) {
        return null;
    }

    /**
     * Annotates icon of a node based on its versioning status.
     *
     * @param roots files that the node represents
     * @param icon original node icon
     * @return Image newly annotated icon or the original one
     */
    Image annotateFolderIcon(Set roots, Image icon) {
        SvnModuleConfig config = SvnModuleConfig.getDefault();
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
                            Image badge = Utilities.loadImage("org/netbeans/modules/subversion/resources/icons/conflicts-badge.png", true);  // NOI18N
                            return Utilities.mergeImages(icon, badge, 16, 9);
                        }
                        modified = true;
                        allExcluded &= config.isExcludedFromCommit(mf.getAbsolutePath());
                    }
                }
            } else {
                for (Iterator j = modifiedFiles.keySet().iterator(); j.hasNext();) {
                    File mf = (File) j.next();
                    if (SvnUtils.isParentOrEqual(file, mf)) {
                        FileInformation info = (FileInformation) modifiedFiles.get(mf);
                        int status = info.getStatus();
                        if (status == FileInformation.STATUS_VERSIONED_CONFLICT) {
                            Image badge = Utilities.loadImage("org/netbeans/modules/subversion/resources/icons/conflicts-badge.png", true); // NOI18N
                            return Utilities.mergeImages(icon, badge, 16, 9);
                        }
                        modified = true;
                        allExcluded &= config.isExcludedFromCommit(mf.getAbsolutePath());
                    }
                }
            }
        }

        if (modified && !allExcluded) {
            Image badge = Utilities.loadImage("org/netbeans/modules/subversion/resources/icons/modified-badge.png", true); // NOI18N
            return Utilities.mergeImages(icon, badge, 16, 9);
        } else {
            return icon;
        }
    }

    /**
     * Returns array of versioning actions that may be used to construct a {@link FileSystemAction}
     * popup menu. These actions will act on the supplied Lookup context.
     *
     * @param context context similar to {@link org.openide.util.ContextAwareAction#createContextAwareInstance(org.openide.util.Lookup)}   
     * @return Action[] array of versioning actions that may be used to construct a popup menu. These actions
     * will act on currently activated nodes.
     */ 
    public static Action [] getActions(Lookup context) {
        ResourceBundle loc = NbBundle.getBundle(Annotator.class);
        Node [] nodes = (Node[]) context.lookup(new Lookup.Template(Node.class)).allInstances().toArray(new Node[0]);
        File [] files = SvnUtils.getCurrentContext(nodes).getRootFiles();
        if (onlyFolders(files)) {
            return new Action [] {
                SystemActionBridge.createAction(SystemAction.get(StatusAction.class), loc.getString("CTL_PopupMenuItem_Status"), context),
                SystemActionBridge.createAction(SystemAction.get(DiffAction.class), loc.getString("CTL_PopupMenuItem_Diff"), context),
                SystemActionBridge.createAction(SystemAction.get(UpdateAction.class), loc.getString("CTL_PopupMenuItem_Update"), context),
                SystemActionBridge.createAction(SystemAction.get(CommitAction.class), loc.getString("CTL_PopupMenuItem_Commit"), context),
                null,
                SystemActionBridge.createAction(SystemAction.get(CreateCopyAction.class), loc.getString("CTL_PopupMenuItem_Copy"), context),
                SystemActionBridge.createAction(SystemAction.get(SwitchToAction.class), loc.getString("CTL_PopupMenuItem_Switch"), context),
                SystemActionBridge.createAction(SystemAction.get(MergeAction.class), loc.getString("CTL_PopupMenuItem_Merge"), context),
                null,
                SystemActionBridge.createAction(SystemAction.get(SearchHistoryAction.class), loc.getString("CTL_PopupMenuItem_SearchHistory"), context),
                null,
                SystemActionBridge.createAction(SystemAction.get(RevertModificationsAction.class), loc.getString("CTL_PopupMenuItem_GetClean"), context),
                SystemActionBridge.createAction(SystemAction.get(ResolveConflictsAction.class), loc.getString("CTL_PopupMenuItem_ResolveConflicts"), context),
                SystemActionBridge.createAction(SystemAction.get(IgnoreAction.class),
                                       ((IgnoreAction)SystemAction.get(IgnoreAction.class)).getActionStatus(nodes) == IgnoreAction.UNIGNORING ? 
                                       loc.getString("CTL_PopupMenuItem_Unignore") : 
                                       loc.getString("CTL_PopupMenuItem_Ignore"), context),

            };
        } else {
            return new Action [] {
                SystemActionBridge.createAction(SystemAction.get(StatusAction.class), loc.getString("CTL_PopupMenuItem_Status"), context),
                SystemActionBridge.createAction(SystemAction.get(DiffAction.class), loc.getString("CTL_PopupMenuItem_Diff"), context),
                SystemActionBridge.createAction(SystemAction.get(UpdateAction.class), loc.getString("CTL_PopupMenuItem_Update"), context),
                SystemActionBridge.createAction(SystemAction.get(CommitAction.class), loc.getString("CTL_PopupMenuItem_Commit"), context),
                null,
                SystemActionBridge.createAction(SystemAction.get(CreateCopyAction.class), loc.getString("CTL_PopupMenuItem_Copy"), context),
                SystemActionBridge.createAction(SystemAction.get(SwitchToAction.class), loc.getString("CTL_PopupMenuItem_Switch"), context),
                SystemActionBridge.createAction(SystemAction.get(MergeAction.class), loc.getString("CTL_PopupMenuItem_Merge"), context),
                null,
                SystemActionBridge.createAction(SystemAction.get(BlameAction.class),
//                                        ((AnnotationsAction)SystemAction.get(AnnotationsAction.class)).visible(nodes) ? 
//                                        loc.getString("CTL_PopupMenuItem_HideAnnotations") : 
                                        loc.getString("CTL_PopupMenuItem_ShowAnnotations"), context),
                SystemActionBridge.createAction(SystemAction.get(SearchHistoryAction.class), loc.getString("CTL_PopupMenuItem_SearchHistory"), context),
                null,
                SystemActionBridge.createAction(SystemAction.get(RevertModificationsAction.class), loc.getString("CTL_PopupMenuItem_GetClean"), context),
                SystemActionBridge.createAction(SystemAction.get(ResolveConflictsAction.class), loc.getString("CTL_PopupMenuItem_ResolveConflicts"), context),
                SystemActionBridge.createAction(SystemAction.get(IgnoreAction.class), 
                                       ((IgnoreAction)SystemAction.get(IgnoreAction.class)).getActionStatus(nodes) == IgnoreAction.UNIGNORING ? 
                                       loc.getString("CTL_PopupMenuItem_Unignore") : 
                                       loc.getString("CTL_PopupMenuItem_Ignore"), context)
            };
        }
    }

    private static boolean onlyFolders(File[] files) {
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
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
        return new MessageFormat(format/* + " svn"*/);  // FIXME PETR remove after resolving multi annotator oddities
    }

}
