/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git;

import org.netbeans.modules.git.options.AnnotationColorProvider;
import java.awt.Image;
import java.io.File;
import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.swing.Action;
import org.netbeans.modules.git.FileInformation.Status;
import org.netbeans.modules.git.ui.actions.AddAction;
import org.netbeans.modules.git.ui.checkout.CheckoutPathsAction;
import org.netbeans.modules.git.ui.commit.CommitAction;
import org.netbeans.modules.git.ui.output.OpenOutputAction;
import org.netbeans.modules.git.ui.status.StatusAction;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * TODO: handle annotations
 * @author ondra
 */
public class Annotator extends VCSAnnotator {
    private static final EnumSet<FileInformation.Status> STATUS_IS_IMPORTANT = EnumSet.noneOf(Status.class);
    private static final EnumSet<FileInformation.Status> STATUS_BADGEABLE = EnumSet.of(Status.STATUS_VERSIONED_UPTODATE, Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE,
            Status.STATUS_VERSIONED_MODIFIED_HEAD_WORKING_TREE);
    static {
        STATUS_IS_IMPORTANT.addAll(FileInformation.STATUS_LOCAL_CHANGES);
        STATUS_IS_IMPORTANT.addAll(EnumSet.of(FileInformation.Status.STATUS_VERSIONED_UPTODATE, FileInformation.Status.STATUS_NOTVERSIONED_EXCLUDED));
    }
    private static final Pattern lessThan = Pattern.compile("<");  // NOI18N
    private static final String badgeModified = "org/netbeans/modules/mercurial/resources/icons/modified-badge.png";
    private static final String badgeConflicts = "org/netbeans/modules/mercurial/resources/icons/conflicts-badge.png";
    private static final String toolTipModified = "<img src=\"" + Annotator.class.getClassLoader().getResource(badgeModified) + "\">&nbsp;"
            + NbBundle.getMessage(Annotator.class, "MSG_Contains_Modified_Locally");
    private static final String toolTipConflict = "<img src=\"" + Annotator.class.getClassLoader().getResource(badgeConflicts) + "\">&nbsp;"
            + NbBundle.getMessage(Annotator.class, "MSG_Contains_Conflicts");

    private final FileStatusCache cache;
    private MessageFormat format;
    private String emptyFormat;

    public Annotator() {
        cache = Git.getInstance().getFileStatusCache();
    }

    @Override
    public Action[] getActions (VCSContext context, ActionDestination destination) {
        Set<File> roots = GitUtils.getRepositoryRoots(context);
        boolean noneVersioned = (roots == null || roots.isEmpty());

        List<Action> actions = new LinkedList<Action>();
        if (destination.equals(ActionDestination.MainMenu)) {
            actions.add(SystemAction.get(StatusAction.class));
            actions.add(SystemAction.get(CheckoutPathsAction.class));
            actions.add(SystemAction.get(AddAction.class));
            actions.add(SystemAction.get(CommitAction.class));
            actions.add(null);
            actions.add(SystemAction.get(OpenOutputAction.class));
        } else {
            Lookup lkp = context.getElements();
            if (noneVersioned) {

            } else {
                actions.add(SystemActionBridge.createAction(SystemAction.get(StatusAction.class), NbBundle.getMessage(StatusAction.class, "LBL_StatusAction.popupName"), lkp));
                actions.add(SystemActionBridge.createAction(SystemAction.get(AddAction.class), NbBundle.getMessage(AddAction.class, "LBL_AddAction.popupName"), lkp));
                actions.add(SystemActionBridge.createAction(SystemAction.get(CommitAction.class), NbBundle.getMessage(CommitAction.class, "LBL_CommitAction.popupName"), lkp));
                actions.add(SystemActionBridge.createAction(SystemAction.get(CheckoutPathsAction.class), NbBundle.getMessage(CheckoutPathsAction.class, "LBL_CheckoutPathsAction_PopupName"), lkp));
            }
        }

        return actions.toArray(new Action[actions.size()]);
    }

    @Override
    public Image annotateIcon(Image icon, VCSContext context) {
        boolean folderAnnotation = false;
        for (File file : context.getRootFiles()) {
            if (file.isDirectory()) {
                folderAnnotation = true;
                Utils.addFolderToLog(file);
                break;
            }
        }

        if (folderAnnotation == false && context.getRootFiles().size() > 1) {
            folderAnnotation = !Utils.isFromMultiFileDataObject(context);
        }

        if (folderAnnotation == false) {
            return annotateFileIcon(context, icon);
        } else {
            return annotateFolderIcon(context, icon);
        }
    }

    @Override
    public String annotateName(String name, VCSContext context) {
        FileInformation mostImportantInfo = null;
        File mostImportantFile = null;
        boolean folderAnnotation = false;
        for (final File file : context.getRootFiles()) {
            FileInformation info = cache.getStatus(file);
            if (!info.containsStatus(STATUS_IS_IMPORTANT)) continue;
            if (isMoreImportant(info, mostImportantInfo)) {
                mostImportantInfo = info;
                mostImportantFile = file;
                folderAnnotation = info.isDirectory();
            }
        }

        if (folderAnnotation == false && context.getRootFiles().size() > 1) {
            folderAnnotation = !Utils.isFromMultiFileDataObject(context);
        }

        if (mostImportantInfo == null) return null;
        return folderAnnotation ?
            annotateFolderNameHtml(name, context, mostImportantInfo, mostImportantFile) :
            annotateNameHtml(name, mostImportantInfo, mostImportantFile);
    }

    private void refresh () {
        // TODO: implement, [status repository branch tags? etc.]
    }

    private static boolean isMoreImportant (FileInformation a, FileInformation b) {
        if (b == null) return true;
        if (a == null) return false;
        return a.getComparableStatus() < b.getComparableStatus();
    }


    private Image annotateFileIcon (VCSContext context, Image icon) throws IllegalArgumentException {
        FileInformation mostImportantInfo = null;
        File mostImportantFile = null;
        for (final File file : context.getRootFiles()) {
            FileInformation info = cache.getStatus(file);
            if (!info.containsStatus(STATUS_IS_IMPORTANT)) {
                continue;
            }
            if (isMoreImportant(info, mostImportantInfo)) {
                mostImportantInfo = info;
                mostImportantFile = file;
            }
        }
        if(mostImportantInfo == null) return null;
        String statusText = null;
        if (mostImportantInfo.containsStatus(Status.STATUS_NOTVERSIONED_EXCLUDED)) {
            statusText = getAnnotationProvider().EXCLUDED_FILE_TOOLTIP.getFormat().format(new Object[]{mostImportantInfo.getStatusText()});
        } else if (mostImportantInfo.containsStatus(Status.STATUS_VERSIONED_REMOVED_IN_WORKING_TREE)) {
            statusText = getAnnotationProvider().REMOVED_LOCALLY_FILE_TOOLTIP.getFormat().format(new Object[]{mostImportantInfo.getStatusText()});
        } else if (mostImportantInfo.containsStatus(Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE)) {
            statusText = getAnnotationProvider().NEW_LOCALLY_FILE_TOOLTIP.getFormat().format(new Object[]{mostImportantInfo.getStatusText()});
        } else if (mostImportantInfo.containsStatus(Status.STATUS_VERSIONED_ADDED_TO_INDEX)) {
            // TODO: copy?
            statusText = getAnnotationProvider().ADDED_LOCALLY_FILE_TOOLTIP.getFormat().format(new Object[]{mostImportantInfo.getStatusText()});
        } else if (mostImportantInfo.containsStatus(Status.STATUS_VERSIONED_MODIFIED_HEAD_WORKING_TREE)) {
            statusText = getAnnotationProvider().MODIFIED_LOCALLY_FILE_TOOLTIP.getFormat().format(new Object[]{mostImportantInfo.getStatusText()});
        } else if (mostImportantInfo.containsStatus(Status.STATUS_VERSIONED_UPTODATE)) {
            statusText = null;
        } else if (mostImportantInfo.containsStatus(Status.STATUS_VERSIONED_CONFLICT)) {
            statusText = getAnnotationProvider().CONFLICT_FILE_TOOLTIP.getFormat().format(new Object[]{mostImportantInfo.getStatusText()});
        } else if (mostImportantInfo.containsStatus(Status.STATUS_NOTVERSIONED_NOTMANAGED)) {
            statusText = null;
        } else if (mostImportantInfo.containsStatus(Status.STATUS_UNKNOWN)) {
            statusText = null;
        } else {
            throw new IllegalArgumentException("Uncomparable status: " + mostImportantInfo.getStatus()); // NOI18N
        }
        return statusText != null ? ImageUtilities.addToolTipToImage(icon, statusText) : null;
    }

    private Image annotateFolderIcon(VCSContext context, Image icon) {
        boolean isVersioned = false;
        for (Iterator i = context.getRootFiles().iterator(); i.hasNext();) {
            File file = (File) i.next();
            // There is an assumption here that annotateName was already
            // called and FileStatusCache.getStatus was scheduled if
            // FileStatusCache.getCachedStatus returned null.
            FileInformation info = cache.getStatus(file);
            if (info.containsStatus(STATUS_BADGEABLE)) {
                isVersioned = true;
                break;
            }
        }
        if (!isVersioned) {
            return null;
        }
        Image badge = null;
        if (cache.containsFiles(context, EnumSet.of(Status.STATUS_VERSIONED_CONFLICT), true)) {
            badge = ImageUtilities.assignToolTipToImage(ImageUtilities.loadImage(badgeConflicts, true), toolTipConflict);
        } else if (cache.containsFiles(context, FileInformation.STATUS_LOCAL_CHANGES, true)) {
            badge = ImageUtilities.assignToolTipToImage(ImageUtilities.loadImage(badgeModified, true), toolTipModified);
        }
        if (badge != null) {
            return ImageUtilities.mergeImages(icon, badge, 16, 9);
        } else {
            return icon;
        }
    }

    private String annotateFolderNameHtml (String name, VCSContext context, FileInformation mostImportantInfo, File mostImportantFile) {
        String nameHtml = htmlEncode(name);
        if (mostImportantInfo.containsStatus(Status.STATUS_NOTVERSIONED_EXCLUDED)) {
            return getAnnotationProvider().EXCLUDED_FILE.getFormat().format(new Object [] { nameHtml, ""}); // NOI18N
        }
        MessageFormat uptodateFormat = getAnnotationProvider().UP_TO_DATE_FILE.getFormat();
        return uptodateFormat.format(new Object [] { nameHtml, "" }); // NOI18N
    }

    public String annotateNameHtml(String name, FileInformation mostImportantInfo, File mostImportantFile) {
        name = htmlEncode(name);

        String textAnnotation;
        boolean annotationsVisible = VersioningSupport.getPreferences().getBoolean(VersioningSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, false);

        if (annotationsVisible && mostImportantFile != null && mostImportantInfo.containsStatus(STATUS_IS_IMPORTANT)) {
            if (format != null) {
                textAnnotation = formatAnnotation(mostImportantInfo, mostImportantFile);
            } else {
                String statusText = mostImportantInfo.getShortStatusText();
                if(!statusText.isEmpty()) {
                    textAnnotation = " [" + mostImportantInfo.getShortStatusText() + "]"; // NOI18N
                } else {
                    textAnnotation = ""; // NOI18N
                }
            }
        } else {
            textAnnotation = ""; // NOI18N
        }

        if (!textAnnotation.isEmpty()) {
            textAnnotation = NbBundle.getMessage(Annotator.class, "textAnnotation", textAnnotation); // NOI18N
        }

        if (mostImportantInfo.containsStatus(Status.STATUS_NOTVERSIONED_EXCLUDED)) {
            return getAnnotationProvider().EXCLUDED_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (mostImportantInfo.containsStatus(Status.STATUS_VERSIONED_REMOVED_IN_WORKING_TREE)) {
            return getAnnotationProvider().REMOVED_LOCALLY_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (mostImportantInfo.containsStatus(Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE)) {
            return getAnnotationProvider().NEW_LOCALLY_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (mostImportantInfo.containsStatus(Status.STATUS_VERSIONED_ADDED_TO_INDEX)) {
            // what about copy?
            return getAnnotationProvider().ADDED_LOCALLY_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (mostImportantInfo.containsStatus(Status.STATUS_VERSIONED_MODIFIED_HEAD_WORKING_TREE)) {
            return getAnnotationProvider().MODIFIED_LOCALLY_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (mostImportantInfo.containsStatus(Status.STATUS_VERSIONED_UPTODATE)) {
            return getAnnotationProvider().UP_TO_DATE_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (mostImportantInfo.containsStatus(Status.STATUS_VERSIONED_CONFLICT)) {
            return getAnnotationProvider().CONFLICT_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (mostImportantInfo.containsStatus(Status.STATUS_NOTVERSIONED_NOTMANAGED)) {
            return name;
        } else if (mostImportantInfo.containsStatus(Status.STATUS_UNKNOWN)) {
            return name;
        } else {
            throw new IllegalArgumentException("Uncomparable status: " + mostImportantInfo.getStatus()); // NOI18N
        }
    }

    private static String htmlEncode(String name) {
        if (name.indexOf('<') == -1) return name;
        return lessThan.matcher(name).replaceAll("&lt;"); // NOI18N
    }

    private AnnotationColorProvider getAnnotationProvider() {
        return AnnotationColorProvider.getInstance();
    }

    private String formatAnnotation(FileInformation info, File file) {
        String statusString = "";  // NOI18N
        if (info.containsStatus(Status.STATUS_VERSIONED_UPTODATE)) {
            statusString = info.getShortStatusText();
        }

        //String stickyString = SvnUtils.getCopy(file);
        String stickyString = null;
        if (stickyString == null) {
            stickyString = ""; // NOI18N
        }

        Object[] arguments = new Object[] {
            statusString,
            stickyString,
        };

        String annotation = format.format(arguments, new StringBuffer(), null).toString().trim();
        if(annotation.equals(emptyFormat)) {
            return ""; // NOI18N
        } else {
            return " " + annotation; // NOI18N
        }
    }
}
