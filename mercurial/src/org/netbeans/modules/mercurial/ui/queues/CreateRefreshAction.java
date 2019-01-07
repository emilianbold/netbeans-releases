/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.mercurial.ui.queues;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.ui.queues.CreateRefreshAction.Cmd.CreateRefreshPatchCmd;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.hooks.HgQueueHook;
import org.netbeans.modules.versioning.hooks.HgQueueHookContext;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.common.VCSCommitOptions;
import org.netbeans.modules.versioning.util.common.VCSCommitTable;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * 
 */
abstract class CreateRefreshAction extends ContextAction {

    static final String RECENT_COMMIT_MESSAGES = "recentCommitMessage"; // NOI18N
    private final String bundleKeyPostfix;

    public CreateRefreshAction (String bundleKeyPostfix) {
        super();
        this.bundleKeyPostfix = bundleKeyPostfix;
    }
    
    @Override
    protected boolean enable (Node[] nodes) {
        return HgUtils.isFromHgRepository(HgUtils.getCurrentContext(nodes));
    }
    
    @Override
    protected void performContextAction (final Node[] nodes) {
        final VCSContext ctx = HgUtils.getCurrentContext(nodes);
        final File roots[] = HgUtils.getActionRoots(ctx);
        if (roots == null || roots.length == 0) return;
        final File root = Mercurial.getInstance().getRepositoryRoot(roots[0]);
        new HgProgressSupport() {

            @Override
            protected void perform () {
                if (!QUtils.isMQEnabledExtension(root)) {
                    return;
                }
                // show commit dialog
                final QCommitPanel panel = createPanel(root, roots);
                if (panel != null) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run () {
                            performAction(root, roots, panel, ctx);
                        }
                    });
                }
            }
        }.start(Mercurial.getInstance().getRequestProcessor(root), root, NbBundle.getMessage(CreateRefreshAction.class, "LBL_CreateRefreshAction.preparing.progress")); //NOI18N
    }

    abstract QCommitPanel createPanel (File root, File[] roots);
    
    private void performAction (final File root, final File[] roots, final QCommitPanel panel, final VCSContext ctx) {
        VCSCommitTable<QFileNode> table = panel.getCommitTable();
        String contentTitle = Utils.getContextDisplayName(ctx);
        boolean ok = panel.open(ctx, panel.getHelpContext(), NbBundle.getMessage(CreateRefreshAction.class, "CTL_RefreshPatchDialog_Title." + bundleKeyPostfix, contentTitle)); //NOI18N

        if (ok) {
            final List<QFileNode> commitFiles = table.getCommitFiles();
            persistCanceledCommitMessage(panel.getParameters(), "");
            panel.getParameters().storeCommitMessage();
            new HgProgressSupport() {
                @Override
                protected void perform () {
                    String message = panel.getParameters().getCommitMessage();
                    String patchName = panel.getParameters().getPatchName();
                    Set<File> excludedFiles = new HashSet<File>();
                    List<File> addCandidates = new LinkedList<File>();
                    List<File> deleteCandidates = new LinkedList<File>();
                    List<File> commitCandidates = new LinkedList<File>();
                    Collection<HgQueueHook> hooks = panel.getHooks();
                    String user = panel.getParameters().getUser();
                    if (user != null) {
                        HgModuleConfig.getDefault().putRecentCommitAuthors(user);
                    }
                    FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
                    for (QFileNode node : commitFiles) {
                        if (isCanceled()) {
                            return;
                        }
                        VCSCommitOptions option = node.getCommitOptions();
                        if (option != QFileNode.EXCLUDE) {
                            int status = cache.getStatus(node.getFile()).getStatus();
                            if ((status & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) != 0) {
                                addCandidates.add(node.getFile());
                            } else  if ((status & FileInformation.STATUS_VERSIONED_DELETEDLOCALLY) != 0) {
                                addCandidates.add(node.getFile());
                            }
                            commitCandidates.add(node.getFile());
                        } else {
                            excludedFiles.add(node.getFile());
                        }
                    }
                    if (isCanceled()) {
                        return;
                    }
                    
                    OutputLogger logger = getLogger();
                    Set<File> filesToRefresh = new HashSet<File>();
                    try {
                        logger.outputInRed(NbBundle.getMessage(CreateRefreshAction.class, "MSG_CREATE_REFRESH_TITLE." + bundleKeyPostfix)); //NOI18N
                        logger.outputInRed(NbBundle.getMessage(CreateRefreshAction.class, "MSG_CREATE_REFRESH_TITLE_SEP." + bundleKeyPostfix)); //NOI18N
                        logger.output(NbBundle.getMessage(CreateRefreshAction.class, "MSG_CREATE_REFRESH_INFO_SEP." + bundleKeyPostfix, patchName, root.getAbsolutePath())); //NOI18N

                        new Cmd.AddCmd(root, addCandidates, logger, null, "hg add {0} into {1}").handle();
                        new Cmd.RemoveCmd(root, deleteCandidates, logger, null, "hg delete {0} from {1}").handle();

                        File[] hookFiles = null;
                        if (hooks.size() > 0) {
                            hookFiles = commitCandidates.toArray(new File[commitCandidates.size()]);
                        }
                        HgModuleConfig.getDefault().setLastUsedQPatchMessage(patchName, message);
                        HgQueueHookContext context = new HgQueueHookContext(hookFiles, message, patchName);
                        for (HgQueueHook hook : hooks) {
                            try {
                                // XXX handle returned context
                                context = hook.beforePatchRefresh(context);
                                if (context != null) {
                                    message = context.getMessage();
                                }
                            } catch (IOException ex) {
                                // XXX handle veto
                            }
                        }
                        Cmd.CreateRefreshPatchCmd commitCmd = createHgCommand(root, commitCandidates, logger,
                                message, patchName, user,
                                bundleKeyPostfix, Arrays.asList(roots), excludedFiles, filesToRefresh);
                        commitCmd.setCommitHooks(context, hooks, hookFiles);
                        commitCmd.handle();

                    } catch (HgException.HgCommandCanceledException ex) {
                        // canceled by user, do nothing
                    } catch (HgException ex) {
                        HgUtils.notifyException(ex);
                    } finally {
                        Mercurial.getInstance().getFileStatusCache().refreshAllRoots(filesToRefresh);
                        Mercurial.getInstance().getMercurialHistoryProvider().fireHistoryChange(filesToRefresh.toArray(new File[filesToRefresh.size()]));
                        logger.outputInRed(NbBundle.getMessage(CreateRefreshAction.class, "MSG_CREATE_REFRESH_DONE." + bundleKeyPostfix)); // NOI18N
                        logger.output(""); // NOI18N
                    }
                }

            }.start(Mercurial.getInstance().getRequestProcessor(root), root, NbBundle.getMessage(CreateRefreshAction.class, "LBL_CreateRefreshAction.progress." + bundleKeyPostfix)); //NOI18N
        } else if (!panel.getParameters().getCommitMessage().isEmpty()) {
            persistCanceledCommitMessage(panel.getParameters(), panel.getParameters().getCommitMessage());
        }
    }

    abstract CreateRefreshPatchCmd createHgCommand (File root, List<File> commitCandidates, OutputLogger logger,
            String message, String patchName, String user, String bundleKeyPostfix,
            List<File> roots, Set<File> excludedFiles, Set<File> filesToRefresh);

    abstract void persistCanceledCommitMessage (QCreatePatchParameters parameters, String canceledCommitMessage);

    static abstract class Cmd {
        protected final List<File> candidates;
        protected final OutputLogger logger;
        protected final String logMsgFormat;
        protected final String msg;
        protected final File repository;
        public Cmd(File repository, List<File> candidates, OutputLogger logger, String msg, String logMsgFormat) {
            this.repository = repository;
            this.candidates = candidates;
            this.logger = logger;
            this.logMsgFormat = logMsgFormat;
            this.msg = msg;
        }
        void handle() throws HgException {
            if(candidates.isEmpty()) return;
            doCmd();
            for (File f : candidates) {
                logger.output(MessageFormat.format(logMsgFormat, f.getName(), repository));
            }
        }
        abstract void doCmd () throws HgException;
        static class AddCmd extends Cmd {
            public AddCmd(File repository, List<File> m, OutputLogger logger, String msgFormat, String msg) {
                super(repository, m, logger, msgFormat, msg);
            }
            @Override
            void doCmd () throws HgException {
                HgCommand.doAdd(repository, candidates, logger);
            }
        }
        static class RemoveCmd extends Cmd {
            public RemoveCmd(File repository, List<File> m, OutputLogger logger, String msgFormat, String msg) {
                super(repository, m, logger, msgFormat, msg);
            }
            @Override
            void doCmd () throws HgException {
                HgCommand.doRemove(repository, candidates, logger);
            }
        }
        static abstract class CreateRefreshPatchCmd extends Cmd {
            private HgQueueHookContext context;
            private Collection<HgQueueHook> hooks;
            private File[] hookFiles;
            private final List<File> rootFiles;
            private final Set<File> refreshFiles;
            private final Set<File> excludedFiles;
            private final String patchId;
            private final String bundleKeyPostfix;
            private final String user;

            public CreateRefreshPatchCmd(File repository, List<File> m, OutputLogger logger, String commitMessage,
                    String patchId, String user, String bundleKeyPostfix,
                    List<File> rootFiles, Set<File> excludedFiles, Set<File> filesToRefresh) {
                super(repository, m, logger, commitMessage, null);
                this.patchId = patchId;
                this.user = user;
                this.bundleKeyPostfix = bundleKeyPostfix;
                this.rootFiles = rootFiles;
                this.excludedFiles = excludedFiles;
                this.refreshFiles = filesToRefresh;
            }

            public void setCommitHooks (HgQueueHookContext context, Collection<HgQueueHook> hooks, File[] hookFiles) {
                this.context = context;
                this.hooks = hooks;
                this.hookFiles = hookFiles;
            }

            @Override
            void handle() throws HgException {
                doCmd();
            }

            @Override
            void doCmd () throws HgException {
                Set<File> files = new HashSet<File>(candidates);
                files.addAll(excludedFiles); // should be also refreshed because previously included files will now change to modified
                try {                    
                    runHgCommand(repository, candidates, excludedFiles, patchId, msg, user, logger);
                } catch (HgException.HgTooLongArgListException e) {
                    Mercurial.LOG.log(Level.INFO, null, e);
                    List<File> reducedCommitCandidates;
                    String offeredFileNames = "";                       //NOI18N
                    if (rootFiles != null && rootFiles.size() < 5) {
                        reducedCommitCandidates = new ArrayList<File>(rootFiles);
                        files = new HashSet<File>(rootFiles);
                        for (File f : reducedCommitCandidates) {
                            offeredFileNames += "\n" + f.getName();     //NOI18N
                        }
                    } else {
                        reducedCommitCandidates = Collections.<File>emptyList();
                        files = Collections.singleton(repository);
                        offeredFileNames = "\n" + repository.getName(); //NOI18N
                    }
                    NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(NbBundle.getMessage(CreateRefreshAction.class, "MSG_LONG_COMMAND_QUERY." + bundleKeyPostfix, offeredFileNames)); //NOI18N
                    descriptor.setTitle(NbBundle.getMessage(CreateRefreshAction.class, "MSG_LONG_COMMAND_TITLE")); //NOI18N
                    descriptor.setMessageType(JOptionPane.WARNING_MESSAGE);
                    descriptor.setOptionType(NotifyDescriptor.YES_NO_OPTION);

                    Object res = DialogDisplayer.getDefault().notify(descriptor);
                    if (res == NotifyDescriptor.NO_OPTION) {
                        return;
                    }
                    Mercurial.LOG.log(Level.INFO, "QRefresh: refreshing patch with a reduced set of files: {0}", reducedCommitCandidates.toString()); //NOI18N
                    runHgCommand(repository, reducedCommitCandidates, Collections.<File>emptySet(), patchId, msg, user, logger);
                } finally {
                    refreshFiles.addAll(files);
                }

                HgLogMessage tip = HgCommand.doTip(repository, logger);

                context = new HgQueueHookContext(hookFiles, msg, patchId);
                for (HgQueueHook hook : hooks) {
                    hook.afterPatchRefresh(context);
                }

                if (candidates.size() == 1) {
                    logger.output(
                            NbBundle.getMessage(CreateRefreshAction.class,
                            "MSG_PATCH_REFRESH_SEP_ONE." + bundleKeyPostfix, patchId)); //NOI18N
                } else if (!candidates.isEmpty()) {
                    logger.output(
                            NbBundle.getMessage(CreateRefreshAction.class,
                            "MSG_PATCH_REFRESH_SEP." + bundleKeyPostfix, patchId, candidates.size())); //NOI18N
                }
                for (File f : candidates) {
                    logger.output("\t" + f.getAbsolutePath()); //NOI18N
                }
                HgUtils.logHgLog(tip, logger);
            }

            protected abstract void runHgCommand (File repository, List<File> candidates, Set<File> excludedFiles,
                    String patchId, String msg, String user, OutputLogger logger) throws HgException;
        }
    }
}
