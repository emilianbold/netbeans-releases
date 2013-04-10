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
package org.netbeans.modules.mercurial.ui.pull;

import java.util.ListIterator;
import java.net.URISyntaxException;
import org.netbeans.modules.versioning.spi.VCSContext;
import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.ui.merge.MergeAction;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.ui.queues.QGoToPatchAction;
import org.netbeans.modules.mercurial.ui.queues.QPatch;
import org.netbeans.modules.mercurial.ui.rebase.RebaseAction;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgProjectUtils;
import org.netbeans.modules.mercurial.util.HgRepositoryContextCache;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.DialogDisplayer;
import org.netbeans.modules.mercurial.ui.repository.HgURL;
import org.openide.DialogDescriptor;
import org.openide.nodes.Node;
import static org.netbeans.modules.mercurial.util.HgUtils.isNullOrEmpty;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.openide.util.actions.SystemAction;

/**
 * Pull action for mercurial:
 * hg pull - pull changes from the specified source
 *
 * @author John Rice
 */
public class PullAction extends ContextAction {
    private static final String CHANGESET_FILES_PREFIX = "files:"; //NOI18N

    private static void logCommand (String fromPrjName, OutputLogger logger, HgURL pullSource, String toPrjName, File root) throws MissingResourceException {
        if (fromPrjName != null) {
            logger.outputInRed(NbBundle.getMessage(
                    PullAction.class, "MSG_PULL_FROM", fromPrjName, HgUtils.stripDoubleSlash(pullSource.toString()))); // NOI18N
        } else {
            logger.outputInRed(NbBundle.getMessage(
                    PullAction.class, "MSG_PULL_FROM_NONAME", HgUtils.stripDoubleSlash(pullSource.toString()))); // NOI18N
        }
        if (toPrjName != null) {
            logger.outputInRed(NbBundle.getMessage(
                    PullAction.class, "MSG_PULL_TO", toPrjName, root)); // NOI18N
        } else {
            logger.outputInRed(NbBundle.getMessage(
                    PullAction.class, "MSG_PULL_TO_NONAME", root)); // NOI18N
        }
    }
    
    public enum PullType {

        LOCAL, OTHER
    }
    {
    }

    @Override
    protected boolean enable(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        return HgUtils.isFromHgRepository(context);
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_PullLocal";                                //NOI18N
    }

    @Override
    public String getName(String role, Node[] activatedNodes) {
        VCSContext ctx = HgUtils.getCurrentContext(activatedNodes);
        Set<File> roots = HgUtils.getRepositoryRoots(ctx);
        String name = roots.size() == 1 ? "CTL_MenuItem_PullRoot" : "CTL_MenuItem_PullLocal"; //NOI18N
        return roots.size() == 1 ? NbBundle.getMessage(PullAction.class, name, roots.iterator().next().getName()) : NbBundle.getMessage(PullAction.class, name);
    }
    
    @Override
    protected void performContextAction(Node[] nodes) {
        final VCSContext context = HgUtils.getCurrentContext(nodes);
        final Set<File> repositoryRoots = HgUtils.getRepositoryRoots(context);
        // run the whole bulk operation in background
        Mercurial.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                for (File repositoryRoot : repositoryRoots) {
                    final File repository = repositoryRoot;
                    // run every repository fetch in its own support with its own output window
                    RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
                    HgProgressSupport support = new HgProgressSupport() {
                        @Override
                        public void perform() {
                            getDefaultAndPerformPull(repository, null, null, this);
                        }
                    };
                    support.start(rp, repository, org.openide.util.NbBundle.getMessage(PullAction.class, "MSG_PULL_PROGRESS")).waitFinished(); //NOI18N
                    if (support.isCanceled()) {
                        break;
                    }
                }
            }
        });
    }

    public static boolean confirmWithLocalChanges(File rootFile, Class bundleLocation, String title, String query, 
        List<String> listIncoming, OutputLogger logger) {
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        File[] roots = HgUtils.splitIntoSeenRoots(rootFile);
        File[] localModNewFiles = cache.listFiles(roots, 
                FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY | 
                FileInformation.STATUS_VERSIONED_CONFLICT | 
                FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        List<String> listIncomingAndLocalMod = new ArrayList<String>();
        Set<String> setFiles = new HashSet<String>();
        String filesStr;
        String[] aFileStr;
        String root = rootFile.getAbsolutePath();
        
        for(String s: listIncoming){
            if(s.indexOf(CHANGESET_FILES_PREFIX) == 0){
                filesStr = (s.substring(CHANGESET_FILES_PREFIX.length())).trim();
                aFileStr = filesStr.split(" ");
                for(String fileStr: aFileStr){
                    setFiles.add(root + File.separator + fileStr);
                    break;
                }
            }
        }
        for(File f : localModNewFiles){
            for(String s : setFiles){
                if( s.equals(f.getAbsolutePath())){
                    listIncomingAndLocalMod.add(s);
                }
            }
        }

        if (listIncomingAndLocalMod.size() > 0) {
            logger.outputInRed(NbBundle.getMessage(PullAction.class, "MSG_PULL_OVERWRITE_LOCAL")); // NOI18N
            logger.output(listIncomingAndLocalMod);
            int response = JOptionPane.showOptionDialog(null, 
                    NbBundle.getMessage(bundleLocation, query), NbBundle.getMessage(bundleLocation, title), 
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

            if (response == JOptionPane.NO_OPTION) {
                return false;
            }
        }
        return true;
    }


    static void annotateChangeSets(List<String> list, Class bundleLocation, String title, OutputLogger logger) {
        logger.outputInRed(NbBundle.getMessage(bundleLocation, title));
        for (String s : list) {
            if (s.indexOf(Mercurial.CHANGESET_STR) == 0) {
                logger.outputInRed(s);
            } else if (!s.equals("")) {
                logger.output(s);
            }
        }
        logger.output("");
    }

    public static void getDefaultAndPerformPull(File root, String revision, 
            String branch, HgProgressSupport supp) {
        OutputLogger logger = supp.getLogger();
        final String pullSourceString = HgRepositoryContextCache.getInstance().getPullDefault(root);
        // If the repository has no default pull path then inform user
        if (isNullOrEmpty(pullSourceString)) {
            notifyDefaultPullUrlNotSpecified(logger);
            return;
        }

        HgURL pullSource;
        try {
            pullSource = new HgURL(pullSourceString);
        } catch (URISyntaxException ex) {
            File sourceRoot = new File(root, pullSourceString);
            if (sourceRoot.isDirectory()) {
                pullSource = new HgURL(FileUtil.normalizeFile(sourceRoot));
            } else {
                notifyDefaultPullUrlInvalid(pullSourceString, ex.getReason(), logger);
                return;
            }
        }

        // We assume that if fromPrjName is null that it is a remote pull.
        // This is not true as a project which is in a subdirectory of a
        // repository will report a project name of null. This does no harm.
        String fromPrjName;
        PullType pullType;

        if (pullSource.isFile()) {
            fromPrjName = HgProjectUtils.getProjectName(new File(pullSource.getPath()));
            pullType = (fromPrjName != null) ? PullType.LOCAL
                                             : PullType.OTHER;
        } else {
            fromPrjName = null;
            pullType = PullType.OTHER;
        }
        final String toPrjName = HgProjectUtils.getProjectName(root);
        performPull(pullType, root, pullSource, fromPrjName, toPrjName, 
                revision, branch, supp);
    }

    private static void notifyDefaultPullUrlNotSpecified(OutputLogger logger) {
        String title = getMessage("MSG_PULL_TITLE");                    //NOI18N

        logger.outputInRed(title);
        logger.outputInRed(getMessage("MSG_PULL_TITLE_SEP"));           //NOI18N
        logger.output     (getMessage("MSG_NO_DEFAULT_PULL_SET_MSG"));  //NOI18N
        logger.outputInRed(getMessage("MSG_PULL_DONE"));                //NOI18N
        logger.output     ("");                                         //NOI18N
        DialogDisplayer.getDefault().notify(
                new DialogDescriptor.Message(
                        getMessage("MSG_NO_DEFAULT_PULL_SET")));        //NOI18N
    }

    private static void notifyDefaultPullUrlInvalid(String pullUrl,
                                                    String reason,
                                                    OutputLogger logger) {
        String title = getMessage("MSG_PULL_TITLE");                    //NOI18N
        String msg = getMessage("MSG_DEFAULT_PULL_INVALID", pullUrl);   //NOI18N

        logger.outputInRed(title);
        logger.outputInRed(getMessage("MSG_PULL_TITLE_SEP"));           //NOI18N
        logger.output     (msg);
        logger.outputInRed(getMessage("MSG_PULL_DONE"));                //NOI18N
        logger.output     ("");                                         //NOI18N
        DialogDisplayer.getDefault().notify(
                new DialogDescriptor.Message(msg));
    }

    /**
     *
     * @param type
     * @param root
     * @param pullSource password is nulled
     * @param fromPrjName
     * @param toPrjName
     * @param logger
     */
    static void performPull(final PullType type, final File root, 
    final HgURL pullSource, final String fromPrjName, final String toPrjName, 
    final String revision, final String branch, final HgProgressSupport supp) {
        if(root == null || pullSource == null) return;
        File bundleFile = null; 
        final OutputLogger logger = supp.getLogger();
        
        try {
            logger.outputInRed(NbBundle.getMessage(PullAction.class, "MSG_PULL_TITLE")); // NOI18N
            logger.outputInRed(NbBundle.getMessage(PullAction.class, "MSG_PULL_TITLE_SEP")); // NOI18N

            logCommand(fromPrjName, logger, pullSource, toPrjName, root);
            final List<String> listIncoming;
            if(type == PullType.LOCAL){
                listIncoming = HgCommand.doIncoming(root, revision, branch, logger);
            }else{
                for (int i = 0; i < 10000; i++) {
                    if (!new File(root.getParentFile(), root.getName() + "_bundle" + i).exists()) { // NOI18N
                        bundleFile = new File(root.getParentFile(), root.getName() + "_bundle" + i); // NOI18N
                        break;
                    }
                }
                listIncoming = HgCommand.doIncoming(root, pullSource, revision, branch, bundleFile, logger, false);
            }
            if (listIncoming == null || listIncoming.isEmpty()) return;
            
            boolean bNoChanges = false;
            for (ListIterator<String> it = listIncoming.listIterator(listIncoming.size()); it.hasPrevious(); ) {
                bNoChanges = HgCommand.isNoChanges(it.previous());
                if (bNoChanges) {
                    break;
                }
            }

            // Warn User when there are Local Changes present that Pull will overwrite
            if (!bNoChanges && !confirmWithLocalChanges(root, PullAction.class, "MSG_PULL_LOCALMODS_CONFIRM_TITLE", "MSG_PULL_LOCALMODS_CONFIRM_QUERY", listIncoming, logger)) { // NOI18N
                logger.outputInRed(NbBundle.getMessage(PullAction.class, "MSG_PULL_LOCALMODS_CANCEL")); // NOI18N
                logger.output(""); // NOI18N
                return;
            }

            // Do Pull if there are changes to be pulled
            if (bNoChanges || supp.isCanceled()) {
                logger.output(HgUtils.replaceHttpPassword(listIncoming));
            } else {
                HgUtils.runWithoutIndexing(new PullImpl(supp, root, type, 
                        revision, branch, bundleFile, listIncoming),
                        root);
            }
        } catch (HgException.HgCommandCanceledException ex) {
            // canceled by user, do nothing
        } catch (HgException ex) {
            HgUtils.notifyException(ex);
        } finally {
            if (bundleFile != null) {
                bundleFile.delete();
            }
            logger.outputInRed(NbBundle.getMessage(PullAction.class, "MSG_PULL_DONE")); // NOI18N
            logger.output(""); // NOI18N
            pullSource.clearPassword();
        }
    }

    private static String getMessage(String msgKey, String... args) {
        return NbBundle.getMessage(PullAction.class, msgKey, args);
    }

    private static boolean isRebaseAllowed (File reposiory, List<HgLogMessage> parents) {
        try {
            if (parents.size() == 1 && parents.get(0).getCSetShortID().equals(
                    HgCommand.getParents(reposiory, null, null).get(0).getCSetShortID())) {
                return true;
            }
        } catch (HgException.HgCommandCanceledException ex) {
            
        } catch (HgException ex) {
            Logger.getLogger(PullAction.class.getName()).log(Level.INFO, null, ex);
        }
        return false;
    }

    @NbBundle.Messages({
        "MSG_PullAction.popingPatches=Popping applied patches",
        "MSG_PullAction.pulling=Pulling changesets",
        "MSG_PullAction.pushingPatches=Pushing patches",
        "# Capitalized letters used intentionally to emphasize the words in "
            + "an output window, should be translated",
        "MSG_PULL_MERGE_DO=INFO: Performing Merge with pulled changes",
        "# Capitalized letters used intentionally to emphasize the words in "
            + "the output window, should be translated",
        "MSG_PULL_REBASE_DO=INFO: Performing Rebase of local commits "
            + "onto pulled changes"
    })
    private static class PullImpl implements Callable<Void> {

        private QPatch topPatch;
        private final HgProgressSupport supp;
        private final File root;
        private final PullType type;
        private final String revision;
        private final String branch;
        private final File fileToUnbundle;
        private final List<String> listIncoming;
        private boolean rebaseAccepted;
        private boolean mergeAccepted;

        public PullImpl (HgProgressSupport supp, File root, 
                PullType type, String revision, String branch,
                File fileToUnbundle, List<String> listIncoming) {
            this.supp = supp;
            this.root = root;
            this.type = type;
            this.revision = revision;
            this.branch = branch;
            this.fileToUnbundle = fileToUnbundle;
            this.listIncoming = listIncoming;
        }

        @Override
        public Void call () throws HgException {
            OutputLogger logger = supp.getLogger();
            topPatch = FetchAction.selectPatch(root);
            if (topPatch != null) {
                supp.setDisplayName(Bundle.MSG_PullAction_popingPatches());
                SystemAction.get(QGoToPatchAction.class).popAllPatches(root, logger);
                supp.setDisplayName(Bundle.MSG_PullAction_pulling());
                logger.output(""); // NOI18N
            }
            if (supp.isCanceled()) {
                return null;
            }
            List<String> list;
            if(type == PullType.LOCAL){
                list = HgCommand.doPull(root, revision, branch, logger);
            }else{
                list = HgCommand.doUnbundle(root, fileToUnbundle, false, logger);
            }
            if (list != null && !list.isEmpty()) {
                annotateChangeSets(HgUtils.replaceHttpPassword(listIncoming), PullAction.class, "MSG_CHANGESETS_TO_PULL", logger); // NOI18N
                handlePulledChangesets(list);
            }

            return null;
        }

        @NbBundle.Messages({
            "MSG_PullAction_mergeNeeded_text=Pull has completed and created "
                + "multiple heads.\n"
                + "Do you want to Merge or Rebase the two heads or Keep them "
                + "unmerged in the repository?",
            "LBL_PullAction_mergeNeeded_title=Pull Created Multiple Heads",
            "CTL_PullAction_mergeButton_text=&Merge",
            "CTL_PullAction_mergeButton_TTtext=Merge the two created heads",
            "CTL_PullAction_rebaseButton_text=&Rebase",
            "CTL_PullAction_rebaseButton_TTtext=Rebase local changesets onto "
                + "the tipmost branch head",
            "CTL_PullAction_keepButton_text=&Keep Heads",
            "CTL_PullAction_keepButton_TTtext=Leaves the heads alone and does "
                + "nothing"
        })
        private void handlePulledChangesets (List<String> list) throws HgException {
            OutputLogger logger = supp.getLogger();
            boolean bMergeNeeded = false;
            boolean updateNeeded = false;
            for (String s : list) {
                logger.output(HgUtils.replaceHttpPassword(s));
                if (HgCommand.isMergeNeededMsg(s)) {
                    bMergeNeeded = true;
                } else if (HgCommand.isUpdateNeededMsg(s) || HgCommand.isHeadsNeededMsg(s)) {
                    updateNeeded = true;
                }
            }
            
            // Handle Merge - both automatic and merge with conflicts
            mergeAccepted = false;
            rebaseAccepted = false;
            boolean warnMoreHeads = true;
            if (!supp.isCanceled()) {
                if (bMergeNeeded) {
                    List<HgLogMessage> parents = HgCommand.getParents(root, null, null);
                    askForMerge(parents);
                    warnMoreHeads = false;
                } else if (updateNeeded) {
                    list = HgCommand.doUpdateAll(root, false, null);
                    logger.output(list);
                }
            }
            boolean finished = !bMergeNeeded;
            try {
                if (rebaseAccepted) {
                    logger.output(""); //NOI18N
                    logger.outputInRed(Bundle.MSG_PULL_REBASE_DO());
                    finished = RebaseAction.doRebase(root, null, null, null, supp);
                } else if (mergeAccepted) {
                    logger.output(""); //NOI18N
                    logger.outputInRed(Bundle.MSG_PULL_MERGE_DO());
                    list = MergeAction.doMergeAction(root, null, logger);
                }
                if (!supp.isCanceled() && finished && topPatch != null) {
                    logger.output(""); //NOI18N
                    supp.setDisplayName(Bundle.MSG_PullAction_pushingPatches());
                    SystemAction.get(QGoToPatchAction.class).applyPatch(root, topPatch.getId(), logger);
                    HgLogMessage parent = HgCommand.getParents(root, null, null).get(0);
                    logger.output(""); //NOI18N
                    HgUtils.logHgLog(parent, logger);
                }
            } finally {
                HgLogMessage[] heads = HgCommand.getHeadRevisionsInfo(root, false, OutputLogger.getLogger(null));
                Map<String, Collection<HgLogMessage>> branchHeads = HgUtils.sortByBranch(heads);
                if (!branchHeads.isEmpty()) {
                    MergeAction.displayMergeWarning(branchHeads, logger, warnMoreHeads && !supp.isCanceled());
                }
                Mercurial.getInstance().refreshOpenedFiles(root);
                HgUtils.notifyUpdatedFiles(root, list);
                HgUtils.forceStatusRefresh(root);
            }
        }

        private void askForMerge (List<HgLogMessage> parents) {
            if (isRebaseAllowed(root, parents)) {
                JButton btnMerge = new JButton();
                Mnemonics.setLocalizedText(btnMerge, Bundle.CTL_PullAction_mergeButton_text());
                btnMerge.setToolTipText(Bundle.CTL_PullAction_mergeButton_TTtext());
                JButton btnRebase = new JButton();
                Mnemonics.setLocalizedText(btnRebase, Bundle.CTL_PullAction_rebaseButton_text());
                btnRebase.setToolTipText(Bundle.CTL_PullAction_rebaseButton_TTtext());
                JButton btnKeep = new JButton();
                Mnemonics.setLocalizedText(btnKeep, Bundle.CTL_PullAction_keepButton_text());
                btnKeep.setToolTipText(Bundle.CTL_PullAction_keepButton_TTtext());
                Object value = DialogDisplayer.getDefault().notify(new NotifyDescriptor(
                        Bundle.MSG_PullAction_mergeNeeded_text(),
                        Bundle.LBL_PullAction_mergeNeeded_title(),
                        NotifyDescriptor.DEFAULT_OPTION,
                        NotifyDescriptor.QUESTION_MESSAGE,
                        new Object[] { btnMerge, btnRebase, btnKeep },
                        btnMerge));
                mergeAccepted = value == btnMerge;
                rebaseAccepted = value == btnRebase;
            } else {
                mergeAccepted = HgUtils.confirmDialog(
                        PullAction.class, "MSG_PULL_MERGE_CONFIRM_TITLE", "MSG_PULL_MERGE_CONFIRM_QUERY"); //NOI18N
            }
        }
    }

}
