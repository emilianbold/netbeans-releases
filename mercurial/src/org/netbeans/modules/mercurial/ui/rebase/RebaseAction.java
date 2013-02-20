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
package org.netbeans.modules.mercurial.ui.rebase;

import java.io.File;
import java.util.concurrent.Callable;
import javax.swing.JButton;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.WorkingCopyInfo;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.ui.rebase.RebaseResult.State;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.RequestProcessor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Mnemonics;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * 
 * @author Ondrej Vrabec
 */
@ActionID(id = "org.netbeans.modules.mercurial.ui.rebase.RebaseAction", category = "Mercurial")
@ActionRegistration(displayName = "#CTL_MenuItem_RebaseAction")
@NbBundle.Messages({
    "CTL_MenuItem_RebaseAction=Rebase..."
})
public class RebaseAction extends ContextAction {
    
    @Override
    protected boolean enable(Node[] nodes) {
        return HgUtils.isFromHgRepository(HgUtils.getCurrentContext(nodes));
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_RebaseAction"; // NOI18N
    }

    @Override
    @NbBundle.Messages({
        "MSG_Rebase_Progress=Rebasing...",
        "# Capitalized letters used intentionally to emphasize the words in an output window, should be translated",
        "MSG_Rebase_Finished=INFO: End of Rebase",
        "MSG_Rebase_Info_Sep=Rebase blablabla",
        "MSG_Rebase_Abort=Aborting an interrupted rebase",
        "MSG_Rebase_Aborted=Rebase Aborted",
        "MSG_Rebase_Merging_Failed=Rebase interrupted because of a failed merge.\nResolve the conflicts and run the rebase again.",
        "MSG_Rebase_Continue=Continuing an interrupted rebase",
        "MSG_Rebase_Title_Sep=----------------",
        "MSG_Rebase_Title=Mercurial Rebase",
        "MSG_Rebase.unfinishedMerge=Cannot rebase because of an unfinished merge.",
        "CTL_RebaseAction.continueButton.text=C&ontinue",
        "CTL_RebaseAction.continueButton.TTtext=Continue the interrupted rebase",
        "CTL_RebaseAction.abortButton.text=Abo&rt",
        "CTL_RebaseAction.abortButton.TTtext=Abort the interrupted rebase",
        "LBL_Rebase.rebasingState.title=Unfinished Rebase",
        "# {0} - repository name", "MSG_Rebase.rebasingState.text=Repository {0} is in the middle of an unfinished rebase.\n"
            + "Do you want to continue or abort the unfinished rebase?"
    })
    protected void performContextAction(Node[] nodes) {
        VCSContext ctx = HgUtils.getCurrentContext(nodes);
        final File roots[] = HgUtils.getActionRoots(ctx);
        if (roots == null || roots.length == 0) return;
        final File root = Mercurial.getInstance().getRepositoryRoot(roots[0]);
        
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
        HgProgressSupport support = new HgProgressSupport() {
            @Override
            public void perform() {
                if (HgUtils.isRebasing(root)) {
                    // abort or continue?
                    JButton btnContinue = new JButton();
                    Mnemonics.setLocalizedText(btnContinue, Bundle.CTL_RebaseAction_continueButton_text());
                    btnContinue.setToolTipText(Bundle.CTL_RebaseAction_continueButton_TTtext());
                    JButton btnAbort = new JButton();
                    Mnemonics.setLocalizedText(btnAbort, Bundle.CTL_RebaseAction_abortButton_text());
                    btnAbort.setToolTipText(Bundle.CTL_RebaseAction_abortButton_TTtext());
                    Object value = DialogDisplayer.getDefault().notify(new NotifyDescriptor(
                            Bundle.MSG_Rebase_rebasingState_text(root.getName()),
                            Bundle.LBL_Rebase_rebasingState_title(),
                            NotifyDescriptor.YES_NO_CANCEL_OPTION,
                            NotifyDescriptor.QUESTION_MESSAGE,
                            new Object[] { btnContinue, btnAbort, NotifyDescriptor.CANCEL_OPTION }, 
                            btnContinue));
                    if (value == btnAbort || value == btnContinue) {
                        final boolean cont = btnContinue == value;
                        final OutputLogger logger = getLogger();
                        try {
                            logger.outputInRed(Bundle.MSG_Rebase_Title());
                            logger.outputInRed(Bundle.MSG_Rebase_Title_Sep());
                            logger.output(cont
                                    ? Bundle.MSG_Rebase_Continue()
                                    : Bundle.MSG_Rebase_Abort());
                            HgUtils.runWithoutIndexing(new Callable<Void>() {
                                @Override
                                public Void call () throws Exception {
                                    RebaseResult rebaseResult = HgCommand.finishRebase(root, cont, logger);
                                    HgUtils.forceStatusRefresh(root);
                                    handleRebaseResult(rebaseResult, logger);
                                    return null;
                                }
                            }, root);
                        } catch (HgException.HgCommandCanceledException ex) {
                            // canceled by user, do nothing
                        } catch (HgException ex) {
                            HgUtils.notifyException(ex);
                        }
                        logger.outputInRed(Bundle.MSG_Rebase_Finished());
                        logger.output(""); // NOI18N
                    }
                } else if (WorkingCopyInfo.getInstance(root).getWorkingCopyParents().length > 1) {
                    // inside a merge
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                Bundle.MSG_Rebase_unfinishedMerge(),
                                NotifyDescriptor.ERROR_MESSAGE));
                } else {
                    // select and rebase
                }
            }
        };
        support.start(rp, root, Bundle.MSG_Rebase_Progress());
    }

    public static void handleRebaseResult (RebaseResult rebaseResult, OutputLogger logger) {
        for (File f : rebaseResult.getTouchedFiles()) {
            Mercurial.getInstance().notifyFileChanged(f);
        }
        logger.output(rebaseResult.getOutput());
        if (rebaseResult.getState() == State.ABORTED) {
            logger.outputInRed(Bundle.MSG_Rebase_Aborted());
        } else if (rebaseResult.getState() == State.MERGING) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        Bundle.MSG_Rebase_Merging_Failed(),
                        NotifyDescriptor.ERROR_MESSAGE));
            logger.outputInRed(Bundle.MSG_Rebase_Merging_Failed());
        }
        logger.output("");
    }
}
