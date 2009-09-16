/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.versioning.system.cvss.ui.actions.tag;

import org.netbeans.modules.versioning.system.cvss.ui.actions.AbstractSystemAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.update.UpdateExecutor;
import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.ExecutorSupport;
import org.netbeans.modules.versioning.system.cvss.ExecutorGroup;
import org.netbeans.modules.versioning.system.cvss.util.Context;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;
import org.netbeans.lib.cvsclient.command.tag.RtagCommand;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.HelpCtx;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;

import javax.swing.*;
import java.awt.Dialog;
import java.io.File;
import java.text.MessageFormat;
import org.netbeans.modules.versioning.util.Utils;

/**
 * Performs the CVS 'update -j' command on selected nodes.
 * 
 * @author Maros Sandor
 */
public class MergeBranchAction extends AbstractSystemAction {

    private static final int enabledForStatus = FileInformation.STATUS_VERSIONED_MERGE
                    | FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY 
                    | FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY 
                    | FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY
                    | FileInformation.STATUS_VERSIONED_UPTODATE;
    
    protected String getBaseName(Node [] activatedNodes) {
        return "CTL_MenuItem_MergeBranch"; // NOI18N
    }

    protected int getFileEnabledStatus() {
        return enabledForStatus;
    }

    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    }
    
    public void performCvsAction(Node[] nodes) {
        Context context = getContext(nodes);

        String title = MessageFormat.format(NbBundle.getBundle(MergeBranchAction.class).getString("CTL_MergeBranchDialog_Title"), getContextDisplayName(nodes));  // NOI18N
        
        MergePanel settings = new MergePanel(context.getFiles());

        JButton merge = new JButton(NbBundle.getMessage(MergeBranchAction.class, "CTL_MergeBranchDialog_Action_Merge"));  // NOI18N
        settings.putClientProperty("OKButton", merge);  // NOI18N
        merge.setToolTipText(NbBundle.getMessage(MergeBranchAction.class, "TT_MergeBranchDialog_Action_Merge"));  // NOI18N
        JButton cancel = new JButton(NbBundle.getMessage(MergeBranchAction.class, "CTL_MergeBranchDialog_Action_Cancel"));  // NOI18N
        cancel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MergeBranchAction.class, "ACSD_MergeBranchDialog_Action_Cancel"));  // NOI18N
        DialogDescriptor descriptor = new DialogDescriptor(
                settings,
                title,
                true,
                new Object [] { merge, cancel },
                merge,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(MergeBranchAction.class),
                null);
        descriptor.setClosingOptions(null);

        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MergeBranchAction.class, "ACSD_MergeBranchDialog"));  // NOI18N
        dialog.setVisible(true);
        if (descriptor.getValue() != merge) return;

        settings.saveSettings();
       
        RequestProcessor.getDefault().post(new MergeBranchExecutor(context, settings, getRunningName(nodes), getContextDisplayName(nodes)));
    }

    protected boolean asynchronous() {
        return false;
    }

    /**
     * Runnable that executes actions specified in the MergeBranch settings panel.
     */ 
    private static class MergeBranchExecutor implements Runnable {

        private final Context context;
        private final MergePanel settings;
        private final String contextName;
        private String temporaryTag;
        private String name;

        public MergeBranchExecutor(Context context, MergePanel settings, String name, String contextName) {
            this.context = context;
            this.settings = settings;
            this.name = name;
            this.contextName = contextName;
        }

        public void run() {
            final ExecutorGroup group = new ExecutorGroup(name);
            if (settings.isTaggingAfterMerge()) {
                temporaryTag = settings.getAfterMergeTagName() + "_tempheadmarker";  // NOI18N
                final ExecutorSupport[] tmpTagging = tagHeadTemporary();
                group.addExecutors(tmpTagging);
                Runnable cleanup = new Runnable() {
                    public void run() {
                        if (tmpTagging == null || ExecutorSupport.wait(tmpTagging)) {
                            group.addCleanups(removeTagHeadTemporary());
                        }
                    }
                };
                group.addBarrier(cleanup);
                group.addExecutors(update());
                group.addBarrier(null);
                group.addExecutors(tag());
            } else {
                group.addExecutors(update());
            }
            group.execute();
        }

        /**
         * If user requests the merge to start at a specific tag: 
         *     cvs update -j merge_tag -j my_branch
         * If user requests to merge everything from the common ancestor revision: 
         *     cvs update -j my_branch
         * Branch_tag may be HEAD if we merge from trunk.
         */
        private UpdateExecutor [] update() {
            UpdateCommand cmd = new UpdateCommand();

            String headTag; 
            if (temporaryTag != null) {
                headTag = temporaryTag;
            } else if (settings.isMergingFromTrunk()) {
                headTag = "HEAD";  // NOI18N
            } else if (settings.isMergingFromBranch()) {
                headTag = settings.getBranchName();
            } else {
                headTag = settings.getEndTagName();
            }
            
            GlobalOptions options = CvsVersioningSystem.createGlobalOptions();
            if (context.getExclusions().size() > 0) {
                options.setExclusions((File[]) context.getExclusions().toArray(new File[context.getExclusions().size()]));
            }
            
            if (settings.isUsingMergeTag()) {
                cmd.setMergeRevision1(settings.getMergeTagName());
                cmd.setMergeRevision2(headTag);
            } else {
                cmd.setMergeRevision1(headTag);
            }

            File[][] files = Utils.splitFlatOthers(context.getFiles());
            if (files[0].length > 0) {
                cmd.setRecursive(false);
            }
            cmd.setFiles(context.getRootFiles());
            cmd.setBuildDirectories(true);
            cmd.setPruneDirectories(true);
        
            return UpdateExecutor.splitCommand(cmd, CvsVersioningSystem.getInstance(), options, contextName);
        }

        /**
         * Tags the head of branch we merged from:
         *     cvs rtag -F -r my_branch merge_tag module1 module2 ...
         */
        private RTagExecutor[] tag() {
            RtagCommand cmd = new RtagCommand();
        
            cmd.setOverrideExistingTag(true);
            cmd.setTagByRevision(temporaryTag);
            cmd.setTag(settings.getAfterMergeTagName());
        
            return RTagExecutor.splitCommand(cmd, context.getFiles(), null);
        }

        /**
         * Places or removes a temporary tag at the head of the branch we merge from:
         *     cvs rtag -F -r my_branch temporary_tag module1 module2 ...
         */
        private RTagExecutor[] tagHeadTemporary() {
            RtagCommand cmd = new RtagCommand();
        
            cmd.setOverrideExistingTag(true);
            cmd.setTagByRevision(settings.isMergingFromTrunk() ? "HEAD" : settings.getBranchName());  // NOI18N
            cmd.setTag(temporaryTag);

            return RTagExecutor.splitCommand(cmd, context.getFiles(), null);
        }

        private RTagExecutor [] removeTagHeadTemporary() {
            RtagCommand cmd = new RtagCommand();
                    
            cmd.setDeleteTag(true);
            cmd.setTag(temporaryTag);
        
            return RTagExecutor.splitCommand(cmd, context.getFiles(), null);
        }
    }
}
