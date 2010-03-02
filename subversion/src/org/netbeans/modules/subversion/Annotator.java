/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.subversion;

import org.netbeans.modules.subversion.ui.copy.*;
import org.netbeans.modules.subversion.ui.ignore.IgnoreAction;
import org.netbeans.modules.subversion.ui.status.StatusAction;
import org.netbeans.modules.subversion.ui.commit.CommitAction;
import org.netbeans.modules.subversion.ui.update.*;
import org.netbeans.modules.subversion.ui.diff.DiffAction;
import org.netbeans.modules.subversion.ui.diff.ExportDiffAction;
import org.netbeans.modules.subversion.ui.blame.BlameAction;
import org.netbeans.modules.subversion.ui.history.SearchHistoryAction;
import org.netbeans.modules.subversion.ui.project.ImportAction;
import org.netbeans.modules.subversion.ui.checkout.CheckoutAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.openide.util.Lookup;
import org.openide.nodes.Node;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.api.project.Project;
import javax.swing.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.text.MessageFormat;
import java.io.File;
import java.awt.*;
import java.util.logging.Level;
import org.netbeans.modules.subversion.ui.properties.SvnPropertiesAction;
import org.netbeans.modules.subversion.ui.relocate.RelocateAction;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.netbeans.modules.diff.PatchAction;
import org.netbeans.modules.subversion.client.SvnClientFactory;
import org.netbeans.modules.subversion.options.AnnotationColorProvider;
import org.netbeans.modules.subversion.ui.cleanup.CleanupAction;
import org.netbeans.modules.subversion.ui.commit.ExcludeFromCommitAction;
import org.netbeans.modules.subversion.ui.properties.VersioningInfoAction;
import org.openide.util.ImageUtilities;

/**
 * Annotates names for display in Files and Projects view (and possible elsewhere). Uses
 * Filesystem support for this feature (to be replaced later in Core by something more generic).
 *
 * @author Maros Sandor
 */
public class Annotator {
    private static String badgeModified = "org/netbeans/modules/subversion/resources/icons/modified-badge.png";
    private static String badgeConflicts = "org/netbeans/modules/subversion/resources/icons/conflicts-badge.png";

    private static String toolTipModified = "<img src=\"" + Annotator.class.getClassLoader().getResource(badgeModified) + "\">&nbsp;"
            + NbBundle.getMessage(Annotator.class, "MSG_Contains_Modified_Locally");
    private static String toolTipConflict = "<img src=\"" + Annotator.class.getClassLoader().getResource(badgeConflicts) + "\">&nbsp;"
            + NbBundle.getMessage(Annotator.class, "MSG_Contains_Conflicts");

    private static final int STATUS_TEXT_ANNOTABLE = FileInformation.STATUS_NOTVERSIONED_EXCLUDED |
            FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_VERSIONED_UPTODATE |
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY | FileInformation.STATUS_VERSIONED_CONFLICT |
            FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY | FileInformation.STATUS_VERSIONED_DELETEDLOCALLY |
            FileInformation.STATUS_VERSIONED_ADDEDLOCALLY;

    private static final Pattern lessThan = Pattern.compile("<");  // NOI18N

    public static String ANNOTATION_REVISION    = "revision";
    public static String ANNOTATION_STATUS      = "status";
    public static String ANNOTATION_FOLDER      = "folder";
    public static String ANNOTATION_MIME_TYPE   = "mime_type";

    public static String[] LABELS = new String[] {ANNOTATION_REVISION, ANNOTATION_STATUS, ANNOTATION_FOLDER, ANNOTATION_MIME_TYPE};

    private final FileStatusCache cache;
    private MessageFormat format;
    private String emptyFormat;

    private boolean mimeTypeFlag;

    Annotator(Subversion svn) {
        this.cache = svn.getStatusCache();
        initDefaults();
    }

    private void initDefaults() {
        refresh();
    }

    public void refresh() {
        String string = SvnModuleConfig.getDefault().getAnnotationFormat(); //System.getProperty("netbeans.experimental.svn.ui.statusLabelFormat");  // NOI18N
        if (string != null && !string.trim().equals("")) { // NOI18N
            mimeTypeFlag = string.indexOf("{mime_type}") > -1;
            string = SvnUtils.createAnnotationFormat(string);
            if (!SvnUtils.isAnnotationFormatValid(string)) {
                Subversion.LOG.log(Level.WARNING, "Bad annotation format, switching to defaults");
                string = org.openide.util.NbBundle.getMessage(Annotator.class, "Annotator.defaultFormat"); // NOI18N
                mimeTypeFlag = string.contains("{3}");
            }
            format = new MessageFormat(string);
            emptyFormat = format.format(new String[] {"", "", "", ""} , new StringBuffer(), null).toString().trim();
        }
        cache.getLabelsCache().setMimeTypeFlag(mimeTypeFlag); // mime labels enabled
    }

    /**
     * Adds rendering attributes to an arbitrary String based on a SVN status. The name is usually a file or folder
     * display name and status is usually its SVN status as reported by FileStatusCache.
     *
     * @param name name to annotate
     * @param info status that an object with the given name has
     * @param file file this annotation belongs to. It is used to determine sticky tags for textual annotations. Pass
     * null if you do not want textual annotations to appear in returned markup
     * @return String html-annotated name that can be used in Swing controls that support html rendering. Note: it may
     * also return the original name String
     */
    public String annotateNameHtml(String name, FileInformation info, File file) {
        if(!checkClientAvailable("annotateNameHtml", file == null ? new File[0] : new File[] {file})) {
            return name;
        }
        name = htmlEncode(name);
        int status = info.getStatus();
        String textAnnotation;
        boolean annotationsVisible = VersioningSupport.getPreferences().getBoolean(VersioningSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, false);
        if (annotationsVisible && file != null && (status & STATUS_TEXT_ANNOTABLE) != 0) {
            if (format != null) {
                textAnnotation = formatAnnotation(info, file);
            } else {
                String sticky = cache.getLabelsCache().getLabelInfo(file, false).getStickyString();
                if (status == FileInformation.STATUS_VERSIONED_UPTODATE && "".equals(sticky)) { //NOI18N
                    textAnnotation = "";  // NOI18N
                } else if (status == FileInformation.STATUS_VERSIONED_UPTODATE) {
                    textAnnotation = " [" + sticky + "]"; // NOI18N
                } else if ("".equals(sticky)) {                         //NOI18N
                    String statusText = info.getShortStatusText();
                    if(!statusText.equals("")) {
                        textAnnotation = " [" + info.getShortStatusText() + "]"; // NOI18N
                    } else {
                        textAnnotation = "";
                    }
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
            return getAnnotationProvider().CONFLICT_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MERGE)) {
            return getAnnotationProvider().MERGEABLE_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_DELETEDLOCALLY)) {
            return getAnnotationProvider().DELETED_LOCALLY_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) {
            return getAnnotationProvider().REMOVED_LOCALLY_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)) {
            return getAnnotationProvider().NEW_LOCALLY_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
            return getAnnotationProvider().ADDED_LOCALLY_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY)) {
            return getAnnotationProvider().MODIFIED_LOCALLY_FILE.getFormat().format(new Object [] { name, textAnnotation });

        // repository changes - lower annotator priority

        } else if (0 != (status & FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY)) {
            return getAnnotationProvider().REMOVED_IN_REPOSITORY_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_NEWINREPOSITORY)) {
            return getAnnotationProvider().NEW_IN_REPOSITORY_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY)) {
            return getAnnotationProvider().MODIFIED_IN_REPOSITORY_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_UPTODATE)) {
            return getAnnotationProvider().UP_TO_DATE_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_EXCLUDED)) {
            return getAnnotationProvider().EXCLUDED_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NOTMANAGED)) {
            return name;
        } else if (status == FileInformation.STATUS_UNKNOWN) {
            return name;
        } else {
            throw new IllegalArgumentException("Unknown status: " + status); // NOI18N
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

        FileStatusCache.FileLabelCache.FileLabelInfo labelInfo;
        labelInfo = cache.getLabelsCache().getLabelInfo(file, mimeTypeFlag);
        String revisionString = labelInfo.getRevisionString();
        String binaryString = labelInfo.getBinaryString();
        String stickyString = labelInfo.getStickyString();

        Object[] arguments = new Object[] {
            revisionString,
            statusString,
            stickyString,
            binaryString
        };

        String annotation = format.format(arguments, new StringBuffer(), null).toString().trim();
        if(annotation.equals(emptyFormat)) {
            return "";
        } else {
            return " " + annotation;
        }
    }

    private String annotateFolderNameHtml(String name, FileInformation info, File file) {
        name = htmlEncode(name);
        int status = info.getStatus();
        String textAnnotation;
        boolean annotationsVisible = VersioningSupport.getPreferences().getBoolean(VersioningSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, false);
        if (annotationsVisible && file != null && (status & FileInformation.STATUS_MANAGED) != 0) {

            if (format != null) {
                textAnnotation = formatAnnotation(info, file);
            } else {
                String sticky = cache.getLabelsCache().getLabelInfo(file, false).getStickyString();
                if (status == FileInformation.STATUS_VERSIONED_UPTODATE && "".equals(sticky)) { //NOI18N
                    textAnnotation = ""; // NOI18N
                } else if (status == FileInformation.STATUS_VERSIONED_UPTODATE) {
                    textAnnotation = " [" + sticky + "]"; // NOI18N
                } else if ("".equals(sticky)) {                         //NOI18N
                    String statusText = info.getShortStatusText();
                    if(!statusText.equals("")) { // NOI18N
                        textAnnotation = " [" + info.getShortStatusText() + "]"; // NOI18N
                    } else {
                        textAnnotation = ""; // NOI18N
                    }
                } else {
                    textAnnotation = " [" + info.getShortStatusText() + "; " + sticky + "]"; // NOI18N
                }
            }
        } else {
            textAnnotation = ""; // NOI18N
        }
        if (textAnnotation.length() > 0) {
            textAnnotation = NbBundle.getMessage(Annotator.class, "textAnnotation", textAnnotation); // NOI18N
        }

        if (status == FileInformation.STATUS_UNKNOWN) {
            return name;
        } else if (match(status, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) {
            return name;
        } else if (match(status, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)) {
            return getAnnotationProvider().UP_TO_DATE_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (match(status, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
            return getAnnotationProvider().UP_TO_DATE_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (match(status, FileInformation.STATUS_VERSIONED_UPTODATE)) {
            return getAnnotationProvider().UP_TO_DATE_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (match(status, FileInformation.STATUS_NOTVERSIONED_EXCLUDED)) {
            return getAnnotationProvider().EXCLUDED_FILE.getFormat().format(new Object [] { name, textAnnotation });
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

    public String annotateNameHtml(String name, VCSContext context, int includeStatus) {
        if(!checkClientAvailable("annotateNameHtml", context.getRootFiles().toArray(new File[context.getRootFiles().size()]))) { //NOI18N
            return name;
        }
        FileInformation mostImportantInfo = null;
        File mostImportantFile = null;
        boolean folderAnnotation = false;

        for (File file : context.getRootFiles()) {
            if (SvnUtils.isPartOfSubversionMetadata(file)) {
                // no need to handle .svn files, eliminates some warnings as 'no repository url found for managed file .svn'
                // happens e.g. when annotating a Project folder
                continue;
            }
            FileInformation info = cache.getCachedStatus(file);
            if (info == null) {
                // status not in cache, plan refresh
                File parentFile = file.getParentFile();
                Subversion.LOG.log(Level.FINE, "null cached status for: {0} in {1}", new Object[] {file, parentFile});
                cache.refreshAsync(file);
                info = new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE, false);
            }
            int status = info.getStatus();
            if ((status & includeStatus) == 0) continue;

            if (isMoreImportant(info, mostImportantInfo)) {
                mostImportantInfo = info;
                mostImportantFile = file;
                folderAnnotation = file.isDirectory();
            }
        }

        if (folderAnnotation == false && context.getRootFiles().size() > 1) {
            folderAnnotation = !Utils.isFromMultiFileDataObject(context);
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
     * Returns array of versioning actions that may be used to construct a popup menu. These actions
     * will act on the supplied context.
     *
     * @param ctx context similar to {@link org.openide.util.ContextAwareAction#createContextAwareInstance(org.openide.util.Lookup)}
     * @param destination
     * @return Action[] array of versioning actions that may be used to construct a popup menu. These actions
     * will act on currently activated nodes.
     */
    public static Action [] getActions(VCSContext ctx, VCSAnnotator.ActionDestination destination) {
        List<Action> actions = new ArrayList<Action>(20);
        if (destination == VCSAnnotator.ActionDestination.MainMenu) {
            actions.add(SystemAction.get(CheckoutAction.class));
            actions.add(SystemAction.get(ImportAction.class));
            actions.add(SystemAction.get(RelocateAction.class));
            actions.add(null);
            actions.add(SystemAction.get(UpdateWithDependenciesAction.class));
            actions.add(SystemAction.get(UpdateToAction.class));
            actions.add(null);
            actions.add(SystemAction.get(StatusAction.class));
            actions.add(SystemAction.get(DiffAction.class));
            actions.add(SystemAction.get(UpdateAction.class));
            actions.add(SystemAction.get(CommitAction.class));
            actions.add(null);
            actions.add(SystemAction.get(ExportDiffAction.class));
            actions.add(SystemAction.get(PatchAction.class));
            actions.add(null);
            actions.add(SystemAction.get(CreateCopyAction.class));
            actions.add(SystemAction.get(SwitchToAction.class));
            actions.add(SystemAction.get(MergeAction.class));
            actions.add(null);
            actions.add(SystemAction.get(BlameAction.class));
            actions.add(SystemAction.get(SearchHistoryAction.class));
            actions.add(null);
            actions.add(SystemAction.get(RevertModificationsAction.class));
            actions.add(SystemAction.get(ResolveConflictsAction.class));
            actions.add(SystemAction.get(IgnoreAction.class));
            actions.add(null);
            actions.add(SystemAction.get(CleanupAction.class));
            actions.add(SystemAction.get(VersioningInfoAction.class));
            actions.add(SystemAction.get(SvnPropertiesAction.class));
        } else {
            ResourceBundle loc = NbBundle.getBundle(Annotator.class);
            File[] files = ctx.getRootFiles().toArray(new File[ctx.getRootFiles().size()]);
            Lookup context = ctx.getElements();
            boolean noneVersioned = isNothingVersioned(files);
            if (noneVersioned) {
                actions.add(SystemActionBridge.createAction(SystemAction.get(ImportAction.class).createContextAwareInstance(context), loc.getString("CTL_PopupMenuItem_Import"), context));
            } else {
                Node[] nodes = ctx.getElements().lookupAll(Node.class).toArray(new Node[0]);
                boolean onlyFolders = onlyFolders(files);
                boolean onlyProjects = onlyProjects(nodes);
                actions.add(SystemActionBridge.createAction(SystemAction.get(StatusAction.class), loc.getString("CTL_PopupMenuItem_Status"), context));
                actions.add(SystemActionBridge.createAction(SystemAction.get(DiffAction.class), loc.getString("CTL_PopupMenuItem_Diff"), context));
                actions.add(SystemActionBridge.createAction(SystemAction.get(UpdateAction.class), loc.getString("CTL_PopupMenuItem_Update"), context));
                if (onlyProjects) {
                    actions.add(new SystemActionBridge(SystemAction.get(UpdateWithDependenciesAction.class), loc.getString("CTL_PopupMenuItem_UpdateWithDeps")));
                }
                actions.add(SystemActionBridge.createAction(SystemAction.get(UpdateToAction.class), loc.getString("CTL_PopupMenuItem_UpdateTo"), context));
                actions.add(SystemActionBridge.createAction(SystemAction.get(CommitAction.class), loc.getString("CTL_PopupMenuItem_Commit"), context));
                actions.add(null);
                actions.add(SystemActionBridge.createAction(SystemAction.get(CreateCopyAction.class), loc.getString("CTL_PopupMenuItem_Copy"), context));
                actions.add(SystemActionBridge.createAction(SystemAction.get(SwitchToAction.class), loc.getString("CTL_PopupMenuItem_Switch"), context));
                actions.add(SystemActionBridge.createAction(SystemAction.get(MergeAction.class), loc.getString("CTL_PopupMenuItem_Merge"), context));
                actions.add(null);
                if (!onlyFolders) {
                    actions.add(SystemActionBridge.createAction(SystemAction.get(BlameAction.class),
                                                                ((BlameAction)SystemAction.get(BlameAction.class)).visible(nodes) ?
                                                                        loc.getString("CTL_PopupMenuItem_HideAnnotations") :
                                                                        loc.getString("CTL_PopupMenuItem_ShowAnnotations"), context));
                }
                actions.add(SystemActionBridge.createAction(SystemAction.get(SearchHistoryAction.class), loc.getString("CTL_PopupMenuItem_SearchHistory"), context));
                actions.add(null);
                actions.add(SystemActionBridge.createAction(SystemAction.get(RevertModificationsAction.class), loc.getString("CTL_PopupMenuItem_GetClean"), context));
                actions.add(SystemActionBridge.createAction(SystemAction.get(ResolveConflictsAction.class), loc.getString("CTL_PopupMenuItem_ResolveConflicts"), context));
                if (!onlyProjects) {
                    actions.add(SystemActionBridge.createAction(SystemAction.get(IgnoreAction.class),
                                                                ((IgnoreAction)SystemAction.get(IgnoreAction.class)).getActionStatus(nodes) == IgnoreAction.UNIGNORING ?
                                                                        loc.getString("CTL_PopupMenuItem_Unignore") :
                                                                        loc.getString("CTL_PopupMenuItem_Ignore"), context));
                actions.add(SystemActionBridge.createAction(SystemAction.get(ExcludeFromCommitAction.class),
                        ((ExcludeFromCommitAction) SystemAction.get(ExcludeFromCommitAction.class)).getActionStatus(nodes) == ExcludeFromCommitAction.INCLUDING
                        ? loc.getString("CTL_PopupMenuItem_IncludeInCommit") //NOI18N
                        : loc.getString("CTL_PopupMenuItem_ExcludeFromCommit"), context)); //NOI18N
                }
                actions.add(null);
                actions.add(SystemActionBridge.createAction(
                                SystemAction.get(CleanupAction.class),
                                loc.getString("CTL_PopupMenuItem_Cleanup"), context));
                actions.add(SystemActionBridge.createAction(
                                SystemAction.get(VersioningInfoAction.class),
                                loc.getString("CTL_PopupMenuItem_VersioningInfo"), context));
                actions.add(SystemActionBridge.createAction(
                                SystemAction.get(SvnPropertiesAction.class),
                                loc.getString("CTL_PopupMenuItem_Properties"), context));
            }
        }
        return actions.toArray(new Action[actions.size()]);
    }

    private static boolean isNothingVersioned(File[] files) {
        for (File file : files) {
            if (SvnUtils.isManaged(file)) return false;
        }
        return true;
    }

    private static boolean onlyProjects(Node[] nodes) {
        if (nodes == null || nodes.length == 0) return false;
        for (Node node : nodes) {
            if (node.getLookup().lookup(Project.class) == null) return false;
        }
        return true;
    }

    private static boolean onlyFolders(File[] files) {
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        boolean onlyFolders = true;
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) return false;
            FileInformation status = cache.getCachedStatus(files[i]);
            if (status == null) {
                onlyFolders = false; // be optimistic, this can be a file
            } else if (!files[i].exists() && !status.isDirectory()) {
                onlyFolders = false;
                break;
            }
        }
        return onlyFolders;
    }

    private static MessageFormat getFormat(String key) {
        String format = NbBundle.getMessage(Annotator.class, key);
        return new MessageFormat(format);
    }

    private static final int STATUS_BADGEABLE =
            FileInformation.STATUS_VERSIONED_UPTODATE |
            FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY |
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY;

    public Image annotateIcon(Image icon, VCSContext context, int includeStatus) {
        if(!checkClientAvailable("annotateIcon", context.getRootFiles().toArray(new File[context.getRootFiles().size()]))) { //NOI18N
            return null;
        }
        boolean folderAnnotation = false;
        for (File file : context.getRootFiles()) {
            if (file.isDirectory()) {
                folderAnnotation = true;
                break;
            }
        }

        if (folderAnnotation == false && context.getRootFiles().size() > 1) {
            folderAnnotation = !Utils.isFromMultiFileDataObject(context);
        }

        if (folderAnnotation == false) {
            return annotateFileIcon(context, icon, includeStatus);
        } else {
            return annotateFolderIcon(context, icon);
        }
    }

    private Image annotateFileIcon(VCSContext context, Image icon, int includeStatus) {
        FileInformation mostImportantInfo = null;

        List<File> filesToRefresh = new LinkedList<File>();
        for (File file : context.getRootFiles()) {
            FileInformation info = cache.getCachedStatus(file);
            if (info == null) {
                File parentFile = file.getParentFile();
                Subversion.LOG.log(Level.FINE, "null cached status for: {0} in {1}", new Object[] {file, parentFile});
                filesToRefresh.add(file);
                info = new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE, false);
            }
            int status = info.getStatus();
            if ((status & includeStatus) == 0) continue;

            if (isMoreImportant(info, mostImportantInfo)) {
                mostImportantInfo = info;
            }
        }
        cache.refreshAsync(filesToRefresh);

        if(mostImportantInfo == null) return null;
        String statusText = null;
        int status = mostImportantInfo.getStatus();
        if (0 != (status & FileInformation.STATUS_VERSIONED_CONFLICT)) {
            statusText = getAnnotationProvider().CONFLICT_FILE_TOOLTIP.getFormat().format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MERGE)) {
            statusText = getAnnotationProvider().MERGEABLE_FILE_TOOLTIP.getFormat().format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_DELETEDLOCALLY)) {
            statusText = getAnnotationProvider().DELETED_LOCALLY_FILE_TOOLTIP.getFormat().format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) {
            statusText = getAnnotationProvider().REMOVED_LOCALLY_FILE_TOOLTIP.getFormat().format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)) {
            statusText = getAnnotationProvider().NEW_LOCALLY_FILE_TOOLTIP.getFormat().format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
            statusText = getAnnotationProvider().ADDED_LOCALLY_FILE_TOOLTIP.getFormat().format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY)) {
            statusText = getAnnotationProvider().MODIFIED_LOCALLY_FILE_TOOLTIP.getFormat().format(new Object [] { mostImportantInfo.getStatusText() });

        // repository changes - lower annotator priority

        } else if (0 != (status & FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY)) {
            statusText = getAnnotationProvider().REMOVED_IN_REPOSITORY_FILE_TOOLTIP.getFormat().format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_NEWINREPOSITORY)) {
            statusText = getAnnotationProvider().NEW_IN_REPOSITORY_FILE.getFormat().format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY)) {
            statusText = getAnnotationProvider().MODIFIED_IN_REPOSITORY_FILE_TOOLTIP.getFormat().format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_UPTODATE)) {
            statusText = null;
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_EXCLUDED)) {
            statusText = getAnnotationProvider().EXCLUDED_FILE_TOOLTIP.getFormat().format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NOTMANAGED)) {
            statusText = null;
        } else if (status == FileInformation.STATUS_UNKNOWN) {
            statusText = null;
        } else {
            throw new IllegalArgumentException("Unknown status: " + status); // NOI18N
        }
        return statusText != null ? ImageUtilities.addToolTipToImage(icon, statusText) : null; // NOI18
    }

    private Image annotateFolderIcon(VCSContext context, Image icon) {
        List<File> filesToRefresh = new LinkedList<File>();
        for (Iterator i = context.getRootFiles().iterator(); i.hasNext();) {
            File file = (File) i.next();
            FileInformation info = cache.getCachedStatus(file);
            if (info == null) {
                filesToRefresh.add(file);
            }
            if (file.isDirectory()) {
                Utils.addFolderToLog(file);
            }
        }
        cache.refreshAsync(filesToRefresh);

        if(cache.ready()) {
            if(cache.containsFiles(context.getRootFiles(), FileInformation.STATUS_VERSIONED_CONFLICT, false)) {
                return getBadge(badgeConflicts, icon, toolTipConflict);
            } else if(cache.containsFiles(context.getRootFiles(), FileInformation.STATUS_LOCAL_CHANGE, false)) {
                return getBadge(badgeModified, icon, toolTipModified);
            }
        }

        return icon;
    }

    private Image getBadge(String badgePath, Image origIcon, String toolTip) {
        Image ret = ImageUtilities.assignToolTipToImage(ImageUtilities.loadImage(badgePath, true), toolTip);
        ret = ImageUtilities.mergeImages(origIcon, ret, 16, 9);
        return ret;
    }

    private boolean checkClientAvailable (String methodName, final File[] files) {
        boolean available = true;
        if (!SvnClientFactory.isInitialized()) {
            Subversion.getInstance().getRequestProcessor().post(new Runnable() {
                @Override
                public void run() {
                    if (SvnClientFactory.isInitialized()) {
                        return;
                    }
                    SvnClientFactory.init();
                    Subversion.getInstance().refreshAllAnnotations();
                }
            });
            Subversion.LOG.log(Level.FINE, " skipping {0} due to not yet initialized client", methodName); //NOI18N
            available = false;
        } else if(!SvnClientFactory.isClientAvailable()) {
            Subversion.LOG.log(Level.FINE, " skipping {0} due to missing client", methodName); //NOI18N
            available = false;
        }
        return available;
    }

    private AnnotationColorProvider getAnnotationProvider() {
        return AnnotationColorProvider.getInstance();
    }
}
