/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.versioning.system.cvss;

import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.openide.util.Lookup;
import org.openide.nodes.Node;
import org.netbeans.modules.versioning.system.cvss.ui.actions.status.StatusAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.checkout.CheckoutAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.project.AddToRepositoryAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.log.AnnotationsAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.log.SearchHistoryAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.diff.DiffAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.diff.ResolveConflictsAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.commit.CommitAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.update.GetCleanAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.ChangeCVSRootAction;
import org.netbeans.modules.versioning.system.cvss.ui.history.ViewRevisionAction;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.lib.cvsclient.admin.Entry;

import javax.swing.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.text.MessageFormat;
import java.io.File;
import java.awt.*;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.netbeans.modules.versioning.system.cvss.options.AnnotationColorProvider;
import org.netbeans.modules.versioning.system.cvss.ui.menu.BranchMenu;
import org.netbeans.modules.versioning.system.cvss.ui.menu.IgnoreMenu;
import org.netbeans.modules.versioning.system.cvss.ui.menu.PatchesMenu;
import org.netbeans.modules.versioning.system.cvss.ui.menu.UpdateMenu;
import org.openide.util.ImageUtilities;

/**
 * Annotates names for display in Files and Projects view (and possible elsewhere). Uses
 * Filesystem support for this feature (to be replaced later in Core by something more generic).
 * 
 * @author Maros Sandor
 */
public class Annotator {

    private static String badgeModified = "org/netbeans/modules/versioning/system/cvss/resources/icons/modified-badge.png";
    private static String badgeConflicts = "org/netbeans/modules/versioning/system/cvss/resources/icons/conflicts-badge.png";
    
    private static String toolTipModified = "<img src=\"" + Annotator.class.getClassLoader().getResource(badgeModified) + "\">&nbsp;"
            + NbBundle.getMessage(Annotator.class, "MSG_Contains_Modified_Locally");
    private static String toolTipConflict = "<img src=\"" + Annotator.class.getClassLoader().getResource(badgeConflicts) + "\">&nbsp;"
            + NbBundle.getMessage(Annotator.class, "MSG_Contains_Conflicts");
  
    private static final int STATUS_TEXT_ANNOTABLE = FileInformation.STATUS_NOTVERSIONED_EXCLUDED | 
            FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_VERSIONED_UPTODATE |
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY | FileInformation.STATUS_VERSIONED_CONFLICT | FileInformation.STATUS_VERSIONED_MERGE |
            FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY | FileInformation.STATUS_VERSIONED_DELETEDLOCALLY | 
            FileInformation.STATUS_VERSIONED_ADDEDLOCALLY;

    private static final Pattern lessThan = Pattern.compile("<");  // NOI18N
    public final static String ACTIONS_PATH_PREFIX = "Actions/Repository/"; // NOI18N 
    
    private final FileStatusCache cache;
    
    private String          lastAnnotationsFormat;
    private MessageFormat   lastMessageFormat;
    private String          lastEmptyAnnotation;

    Annotator(CvsVersioningSystem cvs) {
        cache = cvs.getStatusCache();
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
        boolean annotationsVisible = VersioningSupport.getPreferences().getBoolean(VersioningSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, false);
        if (annotationsVisible && file != null && (status & STATUS_TEXT_ANNOTABLE) != 0) {
            textAnnotation = formatAnnotation(info, file);
            if (textAnnotation.equals(lastEmptyAnnotation)) textAnnotation = ""; // NOI18N
        } else {
            textAnnotation = ""; // NOI18N
        }
        if (textAnnotation.length() > 0) {
            textAnnotation = NbBundle.getMessage(Annotator.class, "textAnnotation", textAnnotation); 
        }
        
        switch (status) {
        case FileInformation.STATUS_UNKNOWN:
        case FileInformation.STATUS_NOTVERSIONED_NOTMANAGED:
            return name;
        case FileInformation.STATUS_VERSIONED_UPTODATE:
            return getAnnotationProvider().UP_TO_DATE_FILE.getFormat().format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY:
            return getAnnotationProvider().MODIFIED_LOCALLY_FILE.getFormat().format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY: 
            return getAnnotationProvider().NEW_LOCALLY_FILE.getFormat().format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY:
            return getAnnotationProvider().REMOVED_LOCALLY_FILE.getFormat().format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_VERSIONED_DELETEDLOCALLY:
            return getAnnotationProvider().DELETED_LOCALLY_FILE.getFormat().format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_VERSIONED_NEWINREPOSITORY:
            return getAnnotationProvider().NEW_IN_REPOSITORY_FILE.getFormat().format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY:
            return getAnnotationProvider().MODIFIED_IN_REPOSITORY_FILE.getFormat().format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY:
            return getAnnotationProvider().REMOVED_IN_REPOSITORY_FILE.getFormat().format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_VERSIONED_ADDEDLOCALLY:
            return getAnnotationProvider().ADDED_LOCALLY_FILE.getFormat().format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_VERSIONED_MERGE:
            return getAnnotationProvider().MERGEABLE_FILE.getFormat().format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_VERSIONED_CONFLICT:
            return getAnnotationProvider().CONFLICT_FILE.getFormat().format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_NOTVERSIONED_EXCLUDED:
            return getAnnotationProvider().EXCLUDED_FILE.getFormat().format(new Object [] { name, textAnnotation });
        default:
            throw new IllegalArgumentException("Unknown status: " + status); // NOI18N
        }
    }

    private String formatAnnotation(FileInformation info, File file) {
        updateMessageFormat();

        String statusString = "";  // NOI18N
        int status = info.getStatus();
        if (status != FileInformation.STATUS_VERSIONED_UPTODATE) {
            statusString = info.getShortStatusText();
        }

        String revisionString = ""; // NOI18N
        String binaryString = ""; // NOI18N
        Entry entry = info.getEntry(file);
        if (entry != null) {
            revisionString = entry.getRevision();
            binaryString = entry.getOptions();
            if ("-kb".equals(binaryString) == false) { // NOI18N
                binaryString = ""; // NOI18N
            }
        }
        String stickyString = Utils.getSticky(file);
        if (stickyString == null) {
            stickyString = ""; // NOI18N
        }

        Object[] arguments = new Object[] {
            revisionString,
            statusString,
            stickyString,
            binaryString
        };
        return lastMessageFormat.format(arguments, new StringBuffer(), null).toString().trim();
    }

    private void updateMessageFormat() {
        String taf = CvsModuleConfig.getDefault().getPreferences().get(CvsModuleConfig.PROP_ANNOTATIONS_FORMAT, CvsModuleConfig.DEFAULT_ANNOTATIONS_FORMAT);
        if (lastMessageFormat == null || !taf.equals(lastAnnotationsFormat)) {
            lastAnnotationsFormat = taf;
            taf = Utils.createAnnotationFormat(taf);
            if (!Utils.isAnnotationFormatValid(taf)) {
                CvsVersioningSystem.LOG.log(Level.WARNING, "Bad annotation format, switching to defaults");
                taf = org.openide.util.NbBundle.getMessage(Annotator.class, "Annotator.defaultFormat"); // NOI18N
            }
            lastMessageFormat = new MessageFormat(taf);
            lastEmptyAnnotation = lastMessageFormat.format(new Object[]{"", "", "", ""}); // NOI18N
        }
    }

    private String annotateFolderNameHtml(String name, FileInformation info, File file) {
        name = htmlEncode(name);
        int status = info.getStatus();
        String textAnnotation;
        boolean annotationsVisible = VersioningSupport.getPreferences().getBoolean(VersioningSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, false);
        if (annotationsVisible && file != null && (status & FileInformation.STATUS_MANAGED) != 0) {
            textAnnotation = formatAnnotation(info, file);
            if (textAnnotation.equals(lastEmptyAnnotation)) textAnnotation = ""; // NOI18N
        } else {
            textAnnotation = ""; // NOI18N
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
            return getAnnotationProvider().UP_TO_DATE_FILE.getFormat().format(new Object [] { name, textAnnotation });
        case FileInformation.STATUS_NOTVERSIONED_EXCLUDED:
            return getAnnotationProvider().EXCLUDED_FILE.getFormat().format(new Object [] { name, textAnnotation });
        default:
            throw new IllegalArgumentException("Unknown status: " + status); // NOI18N
        }
    }
    
    private String htmlEncode(String name) {
        if (name.indexOf('<') == -1) return name;
        return lessThan.matcher(name).replaceAll("&lt;"); // NOI18N
    }

    public String annotateNameHtml(File file, FileInformation info) {
        return annotateNameHtml(file.getName(), info, file);
    }
    
    public String annotateNameHtml(String name, VCSContext context, int includeStatus) {
        FileInformation mostImportantInfo = null;
        File mostImportantFile = null;
        boolean folderAnnotation = false;
        
        for (File file : context.getRootFiles()) {
            FileInformation info = cache.getStatus(file);
            int status = info.getStatus();
            if ((status & includeStatus) == 0) continue;
            
            if (isMoreImportant(info, mostImportantInfo)) {
                mostImportantInfo = info;
                mostImportantFile = file;
                folderAnnotation = file.isDirectory();
            }
        }

        if (folderAnnotation == false && context.getRootFiles().size() > 1) {
            folderAnnotation = !org.netbeans.modules.versioning.util.Utils.shareCommonDataObject(context.getRootFiles().toArray(new File[context.getRootFiles().size()]));
        }

        if (mostImportantInfo == null) return null;
        return folderAnnotation ? 
                annotateFolderNameHtml(name, mostImportantInfo, mostImportantFile) : 
                annotateNameHtml(name, mostImportantInfo, mostImportantFile);
    }
    
    private boolean isMoreImportant(FileInformation a, FileInformation b) {
        if (b == null) return true;
        if (a == null) return false;
        return Utils.getComparableStatus(a.getStatus()) < Utils.getComparableStatus(b.getStatus());
    }

    /**
     * Returns array of versioning actions that may be used to construct a popup menu. These actions
     * will act on the supplied context.
     *
     * @param ctx context similar to {@link org.openide.util.ContextAwareAction#createContextAwareInstance(org.openide.util.Lookup)}   
     * @return Action[] array of versioning actions that may be used to construct a popup menu. These actions
     * will act on currently activated nodes.
     */ 
    public static Action [] getActions(VCSContext ctx, VCSAnnotator.ActionDestination destination) {
        ResourceBundle loc = NbBundle.getBundle(Annotator.class);
        
        List<Action> actions = new ArrayList<Action>(20);
        Lookup context = ctx.getElements();
        File[] files = ctx.getRootFiles().toArray(new File[ctx.getRootFiles().size()]);
        boolean noneVersioned = isNothingVersioned(files);
        if (destination == VCSAnnotator.ActionDestination.MainMenu) {
            if (noneVersioned) {
                actions.add(SystemAction.get(AddToRepositoryAction.class));
            } else {
                actions.add(SystemAction.get(StatusAction.class));
                actions.add(SystemAction.get(DiffAction.class));
                actions.add(SystemAction.get(CommitAction.class));
                actions.add(new UpdateMenu(destination, null));
                actions.add(SystemAction.get(GetCleanAction.class));
                actions.add(SystemAction.get(AnnotationsAction.class));            
                actions.add(SystemAction.get(SearchHistoryAction.class));
                actions.add(SystemAction.get(ResolveConflictsAction.class));
                actions.add(new ViewRevisionAction(ctx));
                actions.add(null);

                actions.add(new IgnoreMenu(null, null, ctx));
                actions.add(new PatchesMenu(destination, null));
                actions.add(null);

                actions.add(new BranchMenu(destination, null));
                actions.add(null);

            }
            actions.add(new ChangeCVSRootAction(loc.getString("CTL_MenuItem_ChangeCVSRoot"), ctx));
            actions.add(SystemAction.get(CheckoutAction.class));
            
            org.netbeans.modules.versioning.util.Utils.setAcceleratorBindings(ACTIONS_PATH_PREFIX, actions.toArray(new Action[actions.size()]));
        } else {
            if (noneVersioned) {
                actions.add(SystemActionBridge.createAction(SystemAction.get(AddToRepositoryAction.class).createContextAwareInstance(context), loc.getString("CTL_PopupMenuItem_Import"), context));
            } else {
                Node[] nodes = ctx.getElements().lookupAll(Node.class).toArray(new Node[0]);
                actions.add(SystemActionBridge.createAction(SystemAction.get(StatusAction.class), loc.getString("CTL_PopupMenuItem_Status"), context));
                actions.add(SystemActionBridge.createAction(SystemAction.get(DiffAction.class), loc.getString("CTL_PopupMenuItem_Diff"), context));
                actions.add(SystemActionBridge.createAction(SystemAction.get(CommitAction.class), loc.getString("CTL_PopupMenuItem_Commit"), context));
                actions.add(new UpdateMenu(destination, context));
                actions.add(SystemActionBridge.createAction(SystemAction.get(GetCleanAction.class), loc.getString("CTL_PopupMenuItem_GetClean"), context));
                actions.add(SystemActionBridge.createAction(SystemAction.get(AnnotationsAction.class),
                        SystemAction.get(AnnotationsAction.class).visible(nodes)
                        ? loc.getString("CTL_PopupMenuItem_HideAnnotations")
                        : loc.getString("CTL_PopupMenuItem_ShowAnnotations"), context));
                actions.add(SystemActionBridge.createAction(SystemAction.get(SearchHistoryAction.class), loc.getString("CTL_PopupMenuItem_SearchHistory"), context));
                actions.add(SystemActionBridge.createAction(SystemAction.get(ResolveConflictsAction.class), loc.getString("CTL_PopupMenuItem_ResolveConflicts"), context));
                actions.add(new ViewRevisionAction(loc.getString("CTL_PopupMenuItem_ViewRevision"), ctx)); // NOI18N
                actions.add(null);
                
                actions.add(new IgnoreMenu(context, nodes, ctx));
                actions.add(new PatchesMenu(destination, context));
                actions.add(null);
                
                actions.add(new BranchMenu(destination, context));
                actions.add(null);
                actions.add(new ChangeCVSRootAction(loc.getString("CTL_MenuItem_ChangeCVSRoot"), ctx));
            }
            actions.add(SystemAction.get(CheckoutAction.class));
        }
        return actions.toArray(new Action[actions.size()]);
    }

    private static boolean isNothingVersioned(File[] files) {
        for (File file : files) {
            if (CvsVersioningSystem.isManaged(file)) return false;
        }
        return true;
    }

    private static final int STATUS_BADGEABLE = FileInformation.STATUS_VERSIONED_UPTODATE | FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    
    public Image annotateIcon(Image icon, VCSContext context, int includeStatus) {
        boolean folderAnnotation = false;
        for (File file : context.getRootFiles()) {
            if (file.isDirectory()) {
                folderAnnotation = true;
                break;
            }
        }
        
        if (folderAnnotation == false && context.getRootFiles().size() > 1) {
            folderAnnotation = !org.netbeans.modules.versioning.util.Utils.shareCommonDataObject(context.getRootFiles().toArray(new File[context.getRootFiles().size()]));
        }

        if (folderAnnotation == false) {
            return annotateFileIcon(context, icon, includeStatus);
        } else {
            return annotateFolderIcon(context, icon);
        }
    }

    private Image annotateFileIcon(VCSContext context, Image icon, int includeStatus) {
        FileInformation mostImportantInfo = null;
        for (File file : context.getRootFiles()) {
            FileInformation info = cache.getStatus(file);
            int status = info.getStatus();
            if ((status & includeStatus) == 0) continue;
            if (isMoreImportant(info, mostImportantInfo)) {
                mostImportantInfo = info;
            }
        }
        if(mostImportantInfo == null) return null; 
        String statusText = null;
        int status = mostImportantInfo.getStatus();
        switch (status) {
            case FileInformation.STATUS_UNKNOWN:
                break;
            case FileInformation.STATUS_NOTVERSIONED_NOTMANAGED:
                statusText = null;
                break;
            case FileInformation.STATUS_VERSIONED_UPTODATE:
                statusText = null;
                break;
            case FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY:
                statusText = getAnnotationProvider().MODIFIED_LOCALLY_FILE_TOOLTIP.getFormat().format(new Object [] { mostImportantInfo.getStatusText() });
                break;
            case FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY:
                statusText = getAnnotationProvider().NEW_LOCALLY_FILE_TOOLTIP.getFormat().format(new Object [] { mostImportantInfo.getStatusText() });
                break;
            case FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY:
                statusText = getAnnotationProvider().REMOVED_LOCALLY_FILE_TOOLTIP.getFormat().format(new Object [] { mostImportantInfo.getStatusText() });
                break;
            case FileInformation.STATUS_VERSIONED_DELETEDLOCALLY:
                statusText = getAnnotationProvider().DELETED_LOCALLY_FILE_TOOLTIP.getFormat().format(new Object [] { mostImportantInfo.getStatusText() });
                break;
            case FileInformation.STATUS_VERSIONED_NEWINREPOSITORY:
                statusText = getAnnotationProvider().NEW_IN_REPOSITORY_FILE_TOOLTIP.getFormat().format(new Object [] { mostImportantInfo.getStatusText() });
                break;
            case FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY:
                statusText = getAnnotationProvider().MODIFIED_IN_REPOSITORY_FILE_TOOLTIP.getFormat().format(new Object [] { mostImportantInfo.getStatusText() });
                break;
            case FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY:
                statusText = getAnnotationProvider().REMOVED_IN_REPOSITORY_FILE_TOOLTIP.getFormat().format(new Object [] { mostImportantInfo.getStatusText() });
                break;
            case FileInformation.STATUS_VERSIONED_ADDEDLOCALLY:
                statusText = getAnnotationProvider().ADDED_LOCALLY_FILE_TOOLTIP.getFormat().format(new Object [] { mostImportantInfo.getStatusText() });
                break;
            case FileInformation.STATUS_VERSIONED_MERGE:
                statusText = getAnnotationProvider().MERGEABLE_FILE_TOOLTIP.getFormat().format(new Object [] { mostImportantInfo.getStatusText() });
                break;
            case FileInformation.STATUS_VERSIONED_CONFLICT:
                statusText = getAnnotationProvider().CONFLICT_FILE_TOOLTIP.getFormat().format(new Object [] { mostImportantInfo.getStatusText() });
                break;
            case FileInformation.STATUS_NOTVERSIONED_EXCLUDED:
                statusText = getAnnotationProvider().EXCLUDED_FILE_TOOLTIP.getFormat().format(new Object [] { mostImportantInfo.getStatusText() });
                break;
            default:
                throw new IllegalArgumentException("Unknown status: " + status); // NOI18N
        }
        return statusText != null ? ImageUtilities.addToolTipToImage(icon, statusText) : null;
    }

    private Image annotateFolderIcon(VCSContext context, Image icon) {
        boolean isVersioned = false;
        for (Iterator<File> i = context.getRootFiles().iterator(); i.hasNext();) {
            File file = i.next();
            if ((cache.getStatus(file).getStatus() & STATUS_BADGEABLE) != 0) {
                isVersioned = true;
                break;
            }
        }
        if (!isVersioned) {
            return null;
        }
        CvsModuleConfig config = CvsModuleConfig.getDefault();
        boolean allExcluded = true;
        boolean modified = false;
        Map<File, FileInformation> map = cache.getAllModifiedFiles();
        Map<File, FileInformation> modifiedFiles = new HashMap<File, FileInformation>();
        for (Map.Entry<File, FileInformation> entry : map.entrySet()) {
            FileInformation info = entry.getValue();
            if (!info.isDirectory() && (info.getStatus() & FileInformation.STATUS_LOCAL_CHANGE) != 0) {
                modifiedFiles.put(entry.getKey(), info);
            }
        }
        for (Iterator<File> i = context.getRootFiles().iterator(); i.hasNext();) {
            File file = i.next();
            if (VersioningSupport.isFlat(file)) {
                for (Iterator<File> j = modifiedFiles.keySet().iterator(); j.hasNext();) {
                    File mf = j.next();
                    if (mf.getParentFile().equals(file)) {
                        FileInformation info = modifiedFiles.get(mf);
                        if (info.isDirectory()) {
                            continue;
                        }
                        int status = info.getStatus();
                        if (status == FileInformation.STATUS_VERSIONED_CONFLICT) {
                            Image badge = ImageUtilities.assignToolTipToImage(
                                    ImageUtilities.loadImage(badgeConflicts, true), toolTipConflict); // NOI18N
                            return ImageUtilities.mergeImages(icon, badge, 16, 9);
                        }
                        modified = true;
                        allExcluded &= config.isExcludedFromCommit(mf);
                    }
                }
            } else {
                for (Iterator<File> j = modifiedFiles.keySet().iterator(); j.hasNext();) {
                    File mf = j.next();
                    if (Utils.isParentOrEqual(file, mf)) {
                        FileInformation info = modifiedFiles.get(mf);
                        int status = info.getStatus();
                        if (status == FileInformation.STATUS_VERSIONED_CONFLICT) {
                            Image badge = ImageUtilities.assignToolTipToImage(
                                    ImageUtilities.loadImage(badgeConflicts, true), toolTipConflict); // NOI18N
                            return ImageUtilities.mergeImages(icon, badge, 16, 9);
                        }
                        modified = true;
                        allExcluded &= config.isExcludedFromCommit(mf);
                    }
                }
            }
        }
        if (modified && !allExcluded) {
            Image badge = ImageUtilities.assignToolTipToImage(
                    ImageUtilities.loadImage(badgeModified, true), toolTipModified); // NOI18N
            return ImageUtilities.mergeImages(icon, badge, 16, 9);
        } else {
            return null;
        }
    }

    private AnnotationColorProvider getAnnotationProvider() {
        return AnnotationColorProvider.getInstance();
    }
}
