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

package org.netbeans.modules.git.remote.ui.shelve;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.progress.FileListener;
import org.netbeans.modules.git.remote.FileInformation;
import org.netbeans.modules.git.remote.Git;
import org.netbeans.modules.git.remote.GitModuleConfig;
import org.netbeans.modules.git.remote.client.GitClient;
import org.netbeans.modules.git.remote.client.GitClientExceptionHandler;
import org.netbeans.modules.git.remote.client.GitProgressSupport;
import org.netbeans.modules.git.remote.ui.actions.GitAction;
import org.netbeans.modules.git.remote.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.remote.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.remote.ui.stash.SaveStashAction;
import org.netbeans.modules.git.remote.ui.stash.Stash;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.modules.versioning.shelve.ShelveChangesActionsRegistry.ShelveChangesActionProvider;
import org.netbeans.modules.versioning.shelve.ShelveChangesSupport;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Actions;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.windows.WindowManager;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.git.remote.ui.shelve.ShelveChangesAction", category = "GitRemote")
@ActionRegistration(displayName = "#CTL_ShelveChanges_Title")
@NbBundle.Messages({
    "CTL_ShelveChanges_Title=&Shelve Changes...",
    "LBL_ShelveChangesAction_Name=&Shelve Changes..."
})
public class ShelveChangesAction extends SingleRepositoryAction {
    private static ShelveChangesActionProvider ACTION_PROVIDER;
    
    @Override
    protected void performAction (VCSFileProxy repository, VCSFileProxy[] roots, VCSContext context) {
        shelve(repository, roots);
    }
    
    @NbBundle.Messages({
        "MSG_ShelveAction.noModifications.text=There are no local modifications to shelve.",
        "LBL_ShelveAction.noModifications.title=No Local Modifications"
    })
    public void shelve (VCSFileProxy repository, VCSFileProxy[] roots) {
        if (Git.getInstance().getFileStatusCache().listFiles(roots,
                FileInformation.STATUS_MODIFIED_HEAD_VS_WORKING).length == 0) {
            // no local changes found
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run () {
                    JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                            Bundle.MSG_ShelveAction_noModifications_text(),
                            Bundle.LBL_ShelveAction_noModifications_title(),
                            JOptionPane.INFORMATION_MESSAGE);
                }
            });
            return;
        }
        GitShelveChangesSupport supp = new GitShelveChangesSupport(repository);
        if (supp.open()) {
            RequestProcessor rp = Git.getInstance().getRequestProcessor(repository);
            supp.startAsync(rp, repository, roots);
        }
    }

    private static class GitShelveChangesSupport extends ShelveChangesSupport {
        private ShelveChangesProgressSupport support;
        private VCSFileProxy[] modifications;
        private final VCSFileProxy repository;
        private final JPanel optionsPanel;
        private final JCheckBox revertModificationInIndex;
        private final JCheckBox doPurgeChxBox;
        private boolean doRevertIndex;
        private boolean doPurge;

        @NbBundle.Messages({
            "ShelvePanel.doPurgeChxBox.text=Remove &Newly Added Files and Folders",
            "ShelvePanel.doPurgeChxBox.desc=Shelve Changes will remove newly added files and empty folders from disk",
            "ShelvePanel.doRevertIndexChanges.text=&Revert Uncommitted Changes in Index to HEAD",
            "ShelvePanel.doRevertIndexChanges.desc=Shelve Changes will revert also changes in the Index to the state in the current HEAD"
        })
        public GitShelveChangesSupport (VCSFileProxy repository) {
            this.repository = repository;
            revertModificationInIndex = new JCheckBox();
            org.openide.awt.Mnemonics.setLocalizedText(revertModificationInIndex, Bundle.ShelvePanel_doRevertIndexChanges_text());
            revertModificationInIndex.setToolTipText(Bundle.ShelvePanel_doRevertIndexChanges_desc());
            revertModificationInIndex.getAccessibleContext().setAccessibleDescription(revertModificationInIndex.getToolTipText());
            revertModificationInIndex.setSelected(true);
            doPurgeChxBox = new JCheckBox();
            org.openide.awt.Mnemonics.setLocalizedText(doPurgeChxBox, Bundle.ShelvePanel_doPurgeChxBox_text());
            doPurgeChxBox.setToolTipText(Bundle.ShelvePanel_doPurgeChxBox_desc());
            doPurgeChxBox.getAccessibleContext().setAccessibleDescription(doPurgeChxBox.getToolTipText());
            doPurgeChxBox.setSelected(GitModuleConfig.getDefault().getRemoveWTNew());
            optionsPanel = new JPanel();
            optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
            optionsPanel.add(revertModificationInIndex);
            optionsPanel.add(doPurgeChxBox);
        }
        
//        @Override
//        @NbBundle.Messages({
//            "# {0} - repository name", "MSG_ShelveChanges.progress.exporting=Saving changes in {0}"
//        })
//        protected void exportPatch (VCSFileProxy toFile, VCSFileProxy commonParent) throws IOException {
//            BufferedOutputStream out = null;
//            boolean success = false;
//            try {
//                out = new BufferedOutputStream(new FileOutputStream(toFile));
//                if (support.isCanceled()) {
//                    return;
//                }
//                GitClient client = null;
//                support.setDisplayName(Bundle.MSG_ShelveChanges_progress_exporting(repository.getName()));
//                try {
//                    client = Git.getInstance().getClient(repository);
//                    client.addNotificationListener(support.new DefaultFileListener(modifications));
//                    client.exportDiff(modifications, DiffMode.HEAD_VS_WORKINGTREE, out, support.getProgressMonitor());
//                } catch (GitException ex) {
//                    throw new IOException(ex);
//                } finally {
//                    if (client != null) {
//                        client.release();
//                    }
//                }
//                success = true;
//            } finally {
//                if (out != null) {
//                    try {
//                        out.flush();
//                        out.close();
//                    } catch (IOException ex) {}
//                    if (success && toFile.length() > 0) {
//                        Utils.openFile(toFile);
//                    } else {
//                        toFile.delete();
//                    }
//                }
//            }
//        }
        
        @Override
        protected void exportPatch(File toFile, File commonParent) throws IOException {
            //TODO: bug #249105
            throw new UnsupportedOperationException();
        }

        @Override
        @NbBundle.Messages({
            "# {0} - repository name", "MSG_ShelveChanges.progress.reverting=Reverting local changes - {0}"
        })
        protected void postExportCleanup () {
            final Collection<VCSFileProxy> notifiedFiles = new HashSet<>();
            if (support.isCanceled()) {
                return;
            }
            try {
                GitUtils.runWithoutIndexing(new Callable<Void>() {
                    @Override
                    public Void call () throws Exception {
                        support.setDisplayName(Bundle.MSG_ShelveChanges_progress_reverting(repository.getName()));
                        // init client
                        GitClient client = Git.getInstance().getClient(repository);
                        client.addNotificationListener(new FileListener() {
                            @Override
                            public void notifyFile (VCSFileProxy file, String relativePathToRoot) {
                                notifiedFiles.add(file);
                            }
                        });
                        client.addNotificationListener(support.new DefaultFileListener(modifications));

                        // revert
                        client.checkout(modifications, doRevertIndex ? GitUtils.HEAD : null,
                                true, support.getProgressMonitor());
                        if(doPurge) {
                            client.clean(modifications, support.getProgressMonitor());
                        }
                        return null;
                    }
                }, modifications);
            } catch (GitException ex) {
                GitClientExceptionHandler.notifyException(ex, true);
            } finally {
                // refresh
                support.setDisplayName(NbBundle.getMessage(GitAction.class, "LBL_Progress.RefreshingStatuses")); //NOI18N
                Git.getInstance().getFileStatusCache().refreshAllRoots(notifiedFiles);
            }
        }

        @Override
        protected boolean isCanceled () {
            return support == null ? false : support.isCanceled();
        }
        
        @NbBundle.Messages("LBL_ShelveChanges_Progress=Shelve changes...")
        private void startAsync (RequestProcessor rp, final VCSFileProxy repository, final VCSFileProxy[] roots) {
            support = new ShelveChangesProgressSupport() {
                @Override
                protected void perform () {
                    modifications = Git.getInstance().getFileStatusCache().listFiles(roots, 
                            FileInformation.STATUS_MODIFIED_HEAD_VS_WORKING);
                    // shelve changes builds common root, it must be the repository root folder
                    // because we use export diff action from the git api 
                    VCSFileProxy[] arr = Arrays.copyOf(modifications, modifications.length + 1);
                    arr[modifications.length] = repository;
                    //TODO: bug #249105
                    //shelveChanges(arr);
                }
            };
            support.start(rp, repository, Bundle.LBL_ShelveChanges_Progress());
        }

        private boolean open () {
            boolean retval = prepare(optionsPanel, "org.netbeans.modules.git.remote.ui.shelve.ShelveChangesPanel"); //NOI18N
            if (retval) {
                doRevertIndex = revertModificationInIndex.isSelected();
                doPurge = doPurgeChxBox.isSelected();
                GitModuleConfig.getDefault().putRemoveWTNew(doPurge);
            }
            return retval;
        }
    };
    
    public static synchronized ShelveChangesActionProvider getProvider () {
        if (ACTION_PROVIDER == null) {
            ACTION_PROVIDER = new ShelveChangesActionProvider() {
                @Override
                public Action getAction () {
                    Action a = SystemAction.get(SaveStashAction.class);
                    Utils.setAcceleratorBindings("Actions/Git", a); //NOI18N
                    return a;
                }

//                @Override
//                public JComponent[] getUnshelveActions (VCSContext ctx, boolean popup) {
//                    JComponent[] cont = UnshelveMenu.getInstance().getMenu(ctx, popup);
//                    if (cont == null) {
//                        cont = super.getUnshelveActions(ctx, popup);
//                    }
//                    return cont;
//                }
                
            };
        }
        return ACTION_PROVIDER;
    }
    
    private abstract static class ShelveChangesProgressSupport extends GitProgressSupport {

        @Override
        public void setDisplayName (String displayName) {
            super.setDisplayName(displayName);
        }
    }
    
    @NbBundle.Messages({
        "CTL_UnstashMenu.name=&Git Unstash",
        "CTL_UnstashMenu.name.popup=Git Unstash",
        "# {0} - stash index", "# {1} - stash name", "CTL_UnstashAction.name={0} - {1}"
    })
    private static class UnshelveMenu {

        private static UnshelveMenu instance;
        
        synchronized static UnshelveMenu getInstance () {
            if (instance == null) {
                instance = new UnshelveMenu();
            }
            return instance;
        }

        private JComponent[] getMenu (VCSContext context, boolean popup) {
            final Map.Entry<VCSFileProxy, VCSFileProxy[]> actionRoots = getActionRoots(context);
            if (actionRoots != null) {
                final VCSFileProxy root = actionRoots.getKey();
                RepositoryInfo info = RepositoryInfo.getInstance(root);
                final List<GitRevisionInfo> stashes = info.getStashes();
                if (!stashes.isEmpty()) {
                    JMenu menu = new JMenu(popup ? Bundle.CTL_UnstashMenu_name_popup(): Bundle.CTL_UnstashMenu_name());
                    Mnemonics.setLocalizedText(menu, menu.getText());
                    int i = 0;
                    for (ListIterator<Stash> it = Stash.create(root, stashes).listIterator(); it.hasNext() && i < 10; ++i) {
                        Stash stash = it.next();
                        Action a = stash.getApplyAction();
                        String name = Bundle.CTL_UnstashAction_name(stash.getIndex(), stash.getInfo().getShortMessage());
                        if (name.length() > 40) {
                            name = name.substring(0, 40);
                        }
                        a.putValue(Action.NAME, name);
                        a.putValue(Action.SHORT_DESCRIPTION, stash.getInfo().getShortMessage());
                        JMenuItem item = new JMenuItem(name);
                        if (popup) {
                            Actions.connect(item, a, true);
                        } else {
                            Actions.connect(item, a);
                        }
                        menu.add(item);
                    }
                    return new JComponent[] { menu };
                }
            }
            return null;
        }
        
    }
}
