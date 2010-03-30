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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.mercurial.ui.merge;

import java.io.File;
import java.util.List;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.versioning.spi.VCSContext;
import javax.swing.*;
import java.util.Set;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.openide.nodes.Node;
import org.openide.util.Utilities;

/**
 * Merge action for mercurial:
 * hg merge - attempts to merge changes when the repository has 2 heads
 *
 * @author John Rice
 */
public class MergeAction extends ContextAction {

    @Override
    protected boolean enable(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        Set<File> ctxFiles = context != null? context.getRootFiles(): null;
        if(!HgUtils.isFromHgRepository(context) || ctxFiles == null || ctxFiles.isEmpty())
            return false;
        return true; // #121293: Speed up menu display, warn user if nothing to merge when Merge selected
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_Merge";                                    //NOI18N
    }

    @Override
    protected void performContextAction(Node[] nodes) {
        final VCSContext context = HgUtils.getCurrentContext(nodes);
        File roots[] = HgUtils.getActionRoots(context);
        if (roots == null || roots.length == 0) return;
        final File root = Mercurial.getInstance().getRepositoryRoot(roots[0]);
        if (root == null) {
            OutputLogger logger = OutputLogger.getLogger(Mercurial.MERCURIAL_OUTPUT_TAB_TITLE);
            logger.outputInRed( NbBundle.getMessage(MergeAction.class,"MSG_MERGE_TITLE")); // NOI18N
            logger.outputInRed( NbBundle.getMessage(MergeAction.class,"MSG_MERGE_TITLE_SEP")); // NOI18N
            logger.outputInRed(
                    NbBundle.getMessage(MergeAction.class, "MSG_MERGE_NOT_SUPPORTED_INVIEW_INFO")); // NOI18N
            logger.output(""); // NOI18N
            logger.closeLog();
            JOptionPane.showMessageDialog(null,
                    NbBundle.getMessage(MergeAction.class, "MSG_MERGE_NOT_SUPPORTED_INVIEW"),// NOI18N
                    NbBundle.getMessage(MergeAction.class, "MSG_MERGE_NOT_SUPPORTED_INVIEW_TITLE"),// NOI18N
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
        HgProgressSupport support = new HgProgressSupport() {
            @Override
            public void perform() {
                final OutputLogger logger = getLogger();
                try {
                    List<String> headList = HgCommand.getHeadRevisions(root);
                    if (headList.size() <= 1) {
                        logger.outputInRed( NbBundle.getMessage(MergeAction.class,"MSG_MERGE_TITLE")); // NOI18N
                        logger.outputInRed( NbBundle.getMessage(MergeAction.class,"MSG_MERGE_TITLE_SEP")); // NOI18N
                        logger.output( NbBundle.getMessage(MergeAction.class,"MSG_NOTHING_TO_MERGE")); // NOI18N
                        logger.outputInRed( NbBundle.getMessage(MergeAction.class, "MSG_MERGE_DONE")); // NOI18N
                        logger.output(""); // NOI18N
                        JOptionPane.showMessageDialog(null,
                            NbBundle.getMessage(MergeAction.class,"MSG_NOTHING_TO_MERGE"),// NOI18N
                            NbBundle.getMessage(MergeAction.class,"MSG_MERGE_TITLE"),// NOI18N
                            JOptionPane.INFORMATION_MESSAGE);
                         return;
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String revStr = null;
                                MergeRevisions mergeDlg = new MergeRevisions(root, new File[] {root});
                                if (!mergeDlg.showDialog()) {
                                    return;
                                }
                                revStr = mergeDlg.getSelectionRevision();
                                logger.outputInRed(
                                        NbBundle.getMessage(MergeAction.class, "MSG_MERGE_TITLE")); // NOI18N
                                logger.outputInRed(
                                        NbBundle.getMessage(MergeAction.class, "MSG_MERGE_TITLE_SEP")); // NOI18N
                                doMergeAction(root, revStr, logger);
                                HgUtils.forceStatusRefreshProject(context);
                                logger.output(""); // NOI18N
                            } catch (HgException ex) {
                                NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
                                DialogDisplayer.getDefault().notifyLater(e);
                            }
                        }
                    });
                } catch (HgException ex) {
                    NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
                    DialogDisplayer.getDefault().notifyLater(e);
                }
            }
        };
        support.start(rp, root, NbBundle.getMessage(MergeAction.class, "MSG_MERGE_PROGRESS")); // NOI18N
    }

    public static List<String> doMergeAction(File root, String revStr, OutputLogger logger) throws HgException {
        List<String> listMerge = HgCommand.doMerge(root, revStr);
        
        if (listMerge != null && !listMerge.isEmpty()) {
            logger.output(listMerge);
            handleMergeOutput(root, listMerge, true, logger);
        }
        return listMerge;
    }

    public static void handleMergeOutput(File root, List<String> listMerge, boolean bDone, OutputLogger logger) throws HgException {
        if (listMerge == null || listMerge.isEmpty()) return;

        Boolean bConflicts = false;
        Boolean bMergeFailed = false;
        
        for (String line : listMerge) {
            if (HgCommand.isMergeAbortUncommittedMsg(line)) {
                bMergeFailed = true;
                logger.outputInRed(NbBundle.getMessage(MergeAction.class,
                        "MSG_MERGE_FAILED")); // NOI18N
                JOptionPane.showMessageDialog(null,
                        NbBundle.getMessage(MergeAction.class, "MSG_MERGE_UNCOMMITTED"), // NOI18N
                        NbBundle.getMessage(MergeAction.class, "MSG_MERGE_TITLE"), // NOI18N
                        JOptionPane.WARNING_MESSAGE);
                break;
            }

            if (HgCommand.isMergeAbortMultipleHeadsMsg(line)) {
                bMergeFailed = true;
                logger.outputInRed(NbBundle.getMessage(MergeAction.class,
                        "MSG_MERGE_FAILED")); // NOI18N
                break;
            }
            if (HgCommand.isMergeConflictMsg(line)) {
                bConflicts = true;
                String filepath = null;
                if (Utilities.isWindows()) {
                    filepath = line.substring(
                            HgCommand.HG_MERGE_CONFLICT_WIN1_ERR.length(),
                            line.length() - HgCommand.HG_MERGE_CONFLICT_WIN2_ERR.length()).trim().replace("/", "\\"); // NOI18N
                    filepath = root.getAbsolutePath() + File.separator + filepath;
                } else {
                    filepath = line.substring(HgCommand.HG_MERGE_CONFLICT_ERR.length());
                }
                logger.outputInRed(NbBundle.getMessage(MergeAction.class, "MSG_MERGE_CONFLICT", filepath)); // NOI18N
                HgCommand.createConflictFile(filepath);
            }

            if (HgCommand.isMergeUnavailableMsg(line)) {
                JOptionPane.showMessageDialog(null,
                        NbBundle.getMessage(MergeAction.class, "MSG_MERGE_UNAVAILABLE"), // NOI18N
                        NbBundle.getMessage(MergeAction.class, "MSG_MERGE_TITLE"), // NOI18N
                        JOptionPane.WARNING_MESSAGE);
                logger.outputInRed(
                        NbBundle.getMessage(MergeAction.class, "MSG_MERGE_INFO"));// NOI18N            
                logger.outputLink(
                        NbBundle.getMessage(MergeAction.class, "MSG_MERGE_INFO_URL")); // NOI18N 
            }
        }

        if (bConflicts) {
            logger.outputInRed(NbBundle.getMessage(MergeAction.class,
                    "MSG_MERGE_DONE_CONFLICTS")); // NOI18N
        }
        if (!bMergeFailed && !bConflicts && bDone) {
            logger.outputInRed(NbBundle.getMessage(MergeAction.class,
                    "MSG_MERGE_DONE")); // NOI18N
        }
    }

    public static void printMergeWarning(List<String> list, OutputLogger logger){
        if(list == null || list.isEmpty() || list.size() <= 1) return;
        
        if (list.size() == 2) {
            logger.outputInRed(NbBundle.getMessage(MergeAction.class, 
                    "MSG_MERGE_WARN_NEEDED", list)); // NOI18N
            logger.outputInRed(NbBundle.getMessage(MergeAction.class, 
                    "MSG_MERGE_DO_NEEDED")); // NOI18N
        } else {
            logger.outputInRed(NbBundle.getMessage(MergeAction.class, 
                    "MSG_MERGE_WARN_MULTIPLE_HEADS", list.size(), list)); // NOI18N
            logger.outputInRed(NbBundle.getMessage(MergeAction.class, 
                    "MSG_MERGE_DONE_MULTIPLE_HEADS")); // NOI18N
        }
    }

}
