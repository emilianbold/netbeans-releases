/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.jira;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JButton;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.InstallSupport.Installer;
import org.netbeans.api.autoupdate.InstallSupport.Validator;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.OperationSupport.Restarter;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
class DownloadPlugin implements ActionListener {

    private DownloadPanel panel;
    private JButton install;
    private JButton cancel;
    private UpdateElement jiraElement;
    private UpdateElement jiraLibraryElement;
    private boolean jiraElementInstalled = false;
    private boolean jiraLibraryElementInstalled = false;

    public DownloadPlugin() {
        panel = new DownloadPanel();
        install = new JButton(NbBundle.getMessage(DownloadPlugin.class, "CTL_Action_Install")); // NOI18N
        cancel = new JButton(NbBundle.getMessage(DownloadPlugin.class, "CTL_Action_Cancel"));   // NOI18N
        install.setEnabled(false);
        panel.licensePanel.setVisible(false);
        panel.acceptCheckBox.addActionListener(this);
    }

    void startDownload() {
        final ProgressHandle ph = ProgressHandleFactory.createHandle(NbBundle.getMessage(DownloadPlugin.class, "MSG_LookingForJira"));
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                ph.start();
                try {
                    List<UpdateUnit> units = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE);
                    for (UpdateUnit u : units) {
                        if(u.getCodeName().equals("org.netbeans.modules.jira")) {       // NOI18N
                            List<UpdateElement> elements = u.getAvailableUpdates();
                            if(elements.size() == 0) {
                                jiraElementInstalled = true;
                            } else {
                                jiraElement = u.getAvailableUpdates().get(0);
                            }
                        } else if(u.getCodeName().equals("org.netbeans.libs.jira")) {   // NOI18N
                            List<UpdateElement> elements = u.getAvailableUpdates();
                            if(elements.size() == 0) {
                                jiraLibraryElementInstalled = true;
                            } else {
                                jiraLibraryElement = u.getAvailableUpdates().get(0);
                            }
                        }
                        if(jiraElement == null || jiraLibraryElement == null) {
                            continue;
                        } else {
                            break;
                        }
                    }
                    if(jiraLibraryElementInstalled && jiraElementInstalled) {
                        notifyError(NbBundle.getMessage(DownloadPlugin.class, "MSG_AlreadyInstalled"),  // NOI18N
                                    NbBundle.getMessage(DownloadPlugin.class, "LBL_Error"));            // NOI18N
                        //panel.progressLabel.setText();
                        return;
                    }
                } finally {
                    ph.finish();
                }
                if(jiraElement == null || jiraLibraryElement == null) {
                    notifyError(NbBundle.getMessage(DownloadPlugin.class, "MSG_JiraNotFound"),          // NOI18N
                                NbBundle.getMessage(DownloadPlugin.class, "LBL_Error"));                // NOI18N
                    return;
                }
                panel.licensePanel.setVisible(true);
                panel.licenseTextPane.setText(jiraElement.getLicence());
                panel.progressPanel.setVisible(false);
                panel.repaint();

                NotifyDescriptor descriptor = new NotifyDescriptor (
                        panel,
                        NbBundle.getMessage(DownloadPlugin.class, "LBL_DownloadJira"),                  // NOI18N
                        NotifyDescriptor.OK_CANCEL_OPTION,
                        NotifyDescriptor.DEFAULT_OPTION,
                        new Object [] { install, cancel },
                        install);

                boolean ret = DialogDisplayer.getDefault().notify(descriptor) == install;
                if(!ret) {
                    jiraElement = null;
                    jiraLibraryElement = null;
                    return;
                }
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        if(jiraLibraryElement != null) {
                            install(jiraLibraryElement, jiraElement == null);
                        }
                        if(jiraElement != null) {
                            install(jiraElement, true);
                        }
                    }
                });
            }
        });
    }

    private void install(final UpdateElement updateElement, final boolean done) {
        try {
            InstallCancellable ic = new InstallCancellable();
            OperationContainer<InstallSupport> oc = OperationContainer.createForInstall();
            if (oc.canBeAdded(updateElement.getUpdateUnit(), updateElement)) {
                oc.add(updateElement);
            } else if (updateElement.getUpdateUnit().isPending()) {
                notifyInDialog(NbBundle.getMessage(MissingJiraSupportPanel.class, "MSG_MissingClient_RestartNeeded"), //NOI18N
                    NbBundle.getMessage(MissingJiraSupportPanel.class, "LBL_MissingClient_RestartNeeded"),            //NOI18N
                    NotifyDescriptor.INFORMATION_MESSAGE, false);
                return;
            } else {
                oc = OperationContainer.createForUpdate();
                if (oc.canBeAdded(updateElement.getUpdateUnit(), updateElement)) {
                    oc.add(updateElement);
                } else {
                    BugtrackingManager.LOG.warning("MissingClient: cannot install " + updateElement.toString());            // NOI18N
                    if (updateElement.getUpdateUnit().getInstalled() != null) {
                        BugtrackingManager.LOG.warning("MissingClient: already installed " + updateElement.getUpdateUnit().getInstalled().toString()); // NOI18N
                    }
                    notifyInDialog(NbBundle.getMessage(MissingJiraSupportPanel.class, "MSG_MissingClient_InvalidOperation"), //NOI18N
                            NbBundle.getMessage(MissingJiraSupportPanel.class, "LBL_MissingClient_InvalidOperation"),        //NOI18N
                            NotifyDescriptor.ERROR_MESSAGE, false);
                    return;
                }
            }
            Validator v = oc.getSupport().doDownload(
                    ProgressHandleFactory.createHandle(
                        NbBundle.getMessage(
                        MissingJiraSupportPanel.class, "LBL_Downloading", updateElement.getDisplayName()), // NOI18N
                        ic),
                    false);
            if(ic.cancelled) {
                return;
            }
            Installer i = oc.getSupport().doValidate(
                    v,
                    ProgressHandleFactory.createHandle(
                        NbBundle.getMessage(
                            MissingJiraSupportPanel.class,
                            "LBL_Validating",                                   // NOI18N
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
                                            "LBL_Installing",                   // NOI18N
                                            updateElement.getDisplayName()),
                                    ic));
            if(done && rest != null) {
                JButton restart = new JButton(NbBundle.getMessage(MissingJiraSupportPanel.class, "CTL_Action_Restart")); // NOI18N
                JButton cancel = new JButton(NbBundle.getMessage(MissingJiraSupportPanel.class, "CTL_Action_Cancel"));   // NOI18N
                NotifyDescriptor descriptor = new NotifyDescriptor(
                        NbBundle.getMessage(MissingJiraSupportPanel.class, "MSG_NeedsRestart"),                          // NOI18N
                        NbBundle.getMessage(MissingJiraSupportPanel.class, "LBL_DownloadJira"),                          // NOI18N
                            NotifyDescriptor.OK_CANCEL_OPTION,
                            NotifyDescriptor.QUESTION_MESSAGE,
                            new Object [] { restart, cancel },
                            restart);
                if(DialogDisplayer.getDefault().notify(descriptor) == restart) {
                    oc.getSupport().doRestart(
                        rest,
                        ProgressHandleFactory.createHandle(NbBundle.getMessage(MissingJiraSupportPanel.class, "LBL_Restarting"))); // NOI18N
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

    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == panel.acceptCheckBox) {
            install.setEnabled(panel.acceptCheckBox.isSelected());
        } 
    }

    public UpdateElement getJiraElement() {
        return jiraElement;
    }

    public UpdateElement getJiraLibraryElement() {
        return jiraLibraryElement;
    }

}
