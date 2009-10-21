/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.jira;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import javax.swing.JButton;
import javax.swing.JComponent;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.InstallSupport.Installer;
import org.netbeans.api.autoupdate.InstallSupport.Validator;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.OperationSupport.Restarter;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.spi.RepositoryUser;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Cancellable;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Notifies and eventually downloads a missing JIRA plugin from the Update Center
 * @author Tomas Stupka
 */
public class JiraUpdater implements ActionListener {

    private MissingJiraSupportPanel panel;
    private static JiraUpdater instance = new JiraUpdater();
    private JiraProxyConector connector;

    private JiraUpdater() {
    }

    public static JiraUpdater getInstance() {
        return instance;
    }

    public static boolean isJiraInstalled() {
        BugtrackingConnector[] connectors = BugtrackingManager.getInstance().getConnectors();
        for (BugtrackingConnector c : connectors) {
            // XXX hack
            if(c.getClass().getName().startsWith("org.netbeans.modules.jira")) {    // NOI18N
                return true;
            }
        }
        return false;
    }

    public void actionPerformed(ActionEvent e) {
        install();
    }

    /**
     * Returns a fake {@link BugtrackingConnector} to be shown in the create
     * repository dialog. The repository controler panel notifies a the missing
     * JIRA plugin and comes with a button to donload it from the Update Center.
     *
     * @return
     */
    public BugtrackingConnector getConnector() {
        if(connector == null) {
            connector = new JiraProxyConector();
        }
        return connector;
    }

    /**
     * Download and install the JIRA plugin from the Update Center
     */
    public void install() {
        final DownloadPlugin dp = new DownloadPlugin();
        dp.show();
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                UpdateElement  jiraLibraryElement = dp.getJiraLibraryElement();
                UpdateElement  jiraElement = dp.getJiraElement();
                if(jiraLibraryElement != null) {
                    install(jiraLibraryElement, jiraElement == null);
                }
                if(jiraElement != null) {
                    install(jiraElement, true);
                }
            }
        });
    }

    public static boolean notifyJiraDownload() {
        JButton ok = new JButton(NbBundle.getMessage(DownloadPlugin.class, "CTL_Action_Download"));     // NOI18N
        JButton cancel = new JButton(NbBundle.getMessage(DownloadPlugin.class, "CTL_Action_Cancel"));   // NOI18N
        MissingJiraSupportPanel panel = JiraUpdater.getInstance().getPanel();

        panel.downloadButton.setVisible(false);
        panel.setMessage(NbBundle.getMessage(FakeJiraSupport.class, "MSG_PROJECT_NEEDS_JIRA"));         // NOI18N

        final DialogDescriptor dd =
            new DialogDescriptor(
                panel,
                NbBundle.getMessage(FakeJiraSupport.class, "CTL_MissingJiraPlugin"),                    // NOI18N
                true,
                new Object[] {ok, cancel},
                ok,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(FakeJiraSupport.class),
                null);
        return DialogDisplayer.getDefault().notify(dd) == ok;
    }

    MissingJiraSupportPanel getPanel() {
        if (panel == null) {
            panel = new MissingJiraSupportPanel();
            panel.downloadButton.addActionListener(this);
        }
        return panel;
    }
    
    private void install(final UpdateElement updateElement, final boolean done) {
        try {
            InstallCancellable ic = new InstallCancellable();
            OperationContainer<InstallSupport> oc = OperationContainer.createForInstall();
            if (oc.canBeAdded(updateElement.getUpdateUnit(), updateElement)) {
                oc.add(updateElement);
            } else if (updateElement.getUpdateUnit().isPending()) {
                notifyInDialog(NbBundle.getMessage(MissingJiraSupportPanel.class, "MSG_MissingClient_RestartNeeded"), //NOI18N
                    NbBundle.getMessage(MissingJiraSupportPanel.class, "LBL_MissingClient_RestartNeeded"), //NOI18N
                    NotifyDescriptor.INFORMATION_MESSAGE, false);
                return;
            } else {
                oc = OperationContainer.createForUpdate();
                if (oc.canBeAdded(updateElement.getUpdateUnit(), updateElement)) {
                    oc.add(updateElement);
                } else {
                    BugtrackingManager.LOG.warning("MissingClient: cannot install " + updateElement.toString());
                    if (updateElement.getUpdateUnit().getInstalled() != null) {
                        BugtrackingManager.LOG.warning("MissingClient: already installed " + updateElement.getUpdateUnit().getInstalled().toString());
                    }
                    notifyInDialog(NbBundle.getMessage(MissingJiraSupportPanel.class, "MSG_MissingClient_InvalidOperation"), //NOI18N
                            NbBundle.getMessage(MissingJiraSupportPanel.class, "LBL_MissingClient_InvalidOperation"), //NOI18N
                            NotifyDescriptor.ERROR_MESSAGE, false);
                    return;
                }
            }
            Validator v = oc.getSupport().doDownload(
                    ProgressHandleFactory.createHandle(
                        NbBundle.getMessage(
                        MissingJiraSupportPanel.class, "LBL_Downloading", updateElement.getDisplayName()),
                        ic),
                    getPanel().forceGlobalCheckBox.isSelected());
            if(ic.cancelled) {
                return;
            }
            Installer i = oc.getSupport().doValidate(
                    v,
                    ProgressHandleFactory.createHandle(
                        NbBundle.getMessage(
                            MissingJiraSupportPanel.class,
                            "LBL_Validating",
                            updateElement.getDisplayName()),
                        ic));
            if(ic.cancelled) {
                return;
            }
            Restarter rest = oc.getSupport().doInstall(
                                    i,
                                    ProgressHandleFactory.createHandle(
                                        NbBundle.getMessage(
                                            MissingJiraSupportPanel.class,
                                            "LBL_Installing",
                                            updateElement.getDisplayName()),
                                    ic));
            if(done && rest != null) {
                JButton restart = new JButton(NbBundle.getMessage(MissingJiraSupportPanel.class, "CTL_Action_Restart"));
                JButton cancel = new JButton(NbBundle.getMessage(MissingJiraSupportPanel.class, "CTL_Action_Cancel"));
                NotifyDescriptor descriptor = new NotifyDescriptor(
                        NbBundle.getMessage(MissingJiraSupportPanel.class, "MSG_NeedsRestart"),
                        NbBundle.getMessage(MissingJiraSupportPanel.class, "LBL_DownloadJira"),
                            NotifyDescriptor.OK_CANCEL_OPTION,
                            NotifyDescriptor.QUESTION_MESSAGE,
                            new Object [] { restart, cancel },
                            restart);
                if(DialogDisplayer.getDefault().notify(descriptor) == restart) {
                    oc.getSupport().doRestart(
                        rest,
                        ProgressHandleFactory.createHandle(NbBundle.getMessage(MissingJiraSupportPanel.class, "LBL_Restarting")));
                }
            }
        } catch (OperationException e) {
            BugtrackingManager.LOG.log(Level.INFO, null, e);
            notifyError(NbBundle.getMessage(MissingJiraSupportPanel.class, "MSG_MissingClient_UC_Unavailable"),   // NOI18N
                    NbBundle.getMessage(MissingJiraSupportPanel.class, "LBL_MissingClient_UC_Unavailable"));      // NOI18N
        }
    }

    private static void notifyError (final String message, final String title) {
        notifyInDialog(message, title, NotifyDescriptor.ERROR_MESSAGE, true);
    }

    private static void notifyInDialog (final String message, final String title, int messageType, boolean cancelVisible) {
        NotifyDescriptor nd = new NotifyDescriptor(message, title, NotifyDescriptor.DEFAULT_OPTION, messageType,
                cancelVisible ? new Object[] {NotifyDescriptor.OK_OPTION, NotifyDescriptor.CANCEL_OPTION} : new Object[] {NotifyDescriptor.OK_OPTION},
                NotifyDescriptor.OK_OPTION);
        DialogDisplayer.getDefault().notifyLater(nd);
    }

    private class InstallCancellable implements Cancellable {
        private boolean cancelled;
        public boolean cancel() {
            cancelled = true;
            return true;
        }
    }

    private class JiraProxyConector extends BugtrackingConnector {
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(FakeJiraSupport.class, "LBL_FakeJiraName");              // NOI18N
        }
        @Override
        public String getTooltip() {
            return NbBundle.getMessage(FakeJiraSupport.class, "LBL_FakeJiraNameTooltip");       // NOI18N
        }
        @Override
        public Repository createRepository() {
            return new JiraProxyRepository();
        }
        @Override
        public Repository[] getRepositories() {
            return new Repository[0];
        }
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }
    }

    private class JiraProxyRepository extends Repository {
        @Override
        public Image getIcon() {
            return null;
        }
        @Override
        public String getDisplayName() {
            throw new UnsupportedOperationException("Not supported yet.");      // NOI18N
        }
        @Override
        public String getTooltip() {
            throw new UnsupportedOperationException("Not supported yet.");      // NOI18N
        }
        @Override
        public String getID() {
            throw new UnsupportedOperationException("Not supported yet.");      // NOI18N
        }
        @Override
        public String getUrl() {
            throw new UnsupportedOperationException("Not supported yet.");      // NOI18N
        }
        @Override
        public Issue getIssue(String id) {
            throw new UnsupportedOperationException("Not supported yet.");      // NOI18N
        }
        @Override
        public void remove() { }
        @Override
        public BugtrackingController getController() {
            return new JiraProxyController();
        }
        @Override
        public Query createQuery() {
            throw new UnsupportedOperationException("Not supported yet.");      // NOI18N
        }
        @Override
        public Issue createIssue() {
            throw new UnsupportedOperationException("Not supported yet.");      // NOI18N
        }

        @Override
        public Query[] getQueries() {
            return new Query[0];
        }
        @Override
        public Collection<RepositoryUser> getUsers() {
            return Collections.EMPTY_LIST;
        }
        @Override
        public Issue[] simpleSearch(String criteria) {
            return new Issue[0];
        }
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }
    }

    private class JiraProxyController extends BugtrackingController {
        @Override
        public JComponent getComponent() {
            MissingJiraSupportPanel panel = getPanel();
            panel.setMessage(NbBundle.getMessage(FakeJiraSupport.class, "MSG_NOT_YET_INSTALLED")); // NOI18N
            panel.downloadButton.setVisible(true);
            return panel;
        }
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(this.getClass());
        }
        @Override
        public boolean isValid() {
            return false;
        }
        @Override
        public void applyChanges() throws IOException {

        }
    }
}
