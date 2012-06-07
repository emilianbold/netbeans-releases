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

    private enum JiraModules {
        ECLIPSE_MYLYN_MONITOR_DUMMY("org.eclipse.mylyn.monitor.ui.dummy"),      // NOI18N    
        JAVAX_WSDL("javax.wsdl"),                                               // NOI18N
        JAVAX_MAIL("javax.mail"),                                               // NOI18N
        JAVAX_ACTIVATION("javax.activation"),                                   // NOI18N
        JAVAX_SERVLET("javax.servlet"),                                         // NOI18N
        JAVAX_XML_SOAP("javax.xml.soap"),                                       // NOI18N
        JAVAX_XML_RPC("javax.xml.rpc"),                                         // NOI18N
        APACHE_COMMONS_DISCOVERY("org.apache.commons.discovery"),               // NOI18N
        APACHE_AXIS("org.apache.axis"),                                         // NOI18N    
        ECLIPSE_MYLYN_COMMONS_SOAP("org.eclipse.mylyn.commons.soap"),           // NOI18N    
        CONNECTOR_COMMON_CORE("com.atlassian.connector.eclipse.commons.core"),  // NOI18N
        CONNECTOR_JIRA_CORE("com.atlassian.connector.eclipse.jira.core"),       // NOI18N
        JIRA("org.netbeans.modules.jira");                                      // NOI18N
        
        String cnb;
        UpdateElement updateElement = null;
        boolean installed = false;
        
        JiraModules(String cnb) {
            this.cnb = cnb;
        }
    }
    
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
            @Override
            public void run() {
                ph.start();
                try {
                    List<UpdateUnit> units = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE);
                    
                    for (UpdateUnit u : units) {
                        
                        for(JiraModules modules : JiraModules.values()) {
                            if(u.getCodeName().equals(modules.cnb)) {       
                                List<UpdateElement> elements = u.getAvailableUpdates();
                                if(elements.isEmpty()) {
                                    modules.installed = true;
                                } else {
                                    modules.updateElement = u.getAvailableUpdates().get(0);
                                }
                            }
                        }
                        if(nullElements(false)) {
                            continue;
                        } else {
                            break;
                        }
                    }
                    if(allInstaled()) {
                        notifyError(NbBundle.getMessage(DownloadPlugin.class, "MSG_AlreadyInstalled"),  // NOI18N
                                    NbBundle.getMessage(DownloadPlugin.class, "LBL_Error"));            // NOI18N
                        //panel.progressLabel.setText();
                        return;
                    }
                } finally {
                    ph.finish();
                }
                if(nullElements(true)) {
                    if(BugtrackingManager.LOG.isLoggable(Level.FINE)) {
                        for(JiraModules module : JiraModules.values()) {
                            BugtrackingManager.LOG.log(Level.FINE, " + {0}, installed : {1}, found: {2}", new Object[]{module.cnb, module.installed, module.updateElement == null ? "false" : "true"});
                        }
                    }
                    notifyError(NbBundle.getMessage(DownloadPlugin.class, "MSG_JiraNotFound"),          // NOI18N
                                NbBundle.getMessage(DownloadPlugin.class, "LBL_Error"));                // NOI18N
                    return;
                }
                panel.licensePanel.setVisible(true);
                StringBuilder sb = new StringBuilder();
                if(JiraModules.JIRA.updateElement != null) sb.append(JiraModules.JIRA.updateElement.getLicence());
                if(JiraModules.JIRA.updateElement != null && JiraModules.JAVAX_WSDL.updateElement != null) sb.append("\n\n");
                if(JiraModules.JAVAX_WSDL.updateElement != null) sb.append(JiraModules.JAVAX_WSDL.updateElement.getLicence());

                panel.licenseTextPane.setText(sb.toString());
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
                    resetElements();
                    return;
                }
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        install();
                    }
                });
            }

            private boolean nullElements(boolean onlyInstalled) {
                for(JiraModules module : JiraModules.values()) {
                    if(!(onlyInstalled && module.installed) && module.updateElement == null) {
                        return true;
                    }
                }
                return false;
            }
            private boolean allInstaled() {
                for(JiraModules module : JiraModules.values()) {
                    if(!module.installed) {
                        return false;
                    }
                }
                return true;
            }
            private boolean resetElements() {
                for(JiraModules module : JiraModules.values()) {
                    module.installed = false;
                    module.updateElement = null;
                }
                return true;
            }
        });
    }

    private void install() {
        Restarter rest = null;
        OperationContainer<InstallSupport> oc = null;
        
        // instal modules
        for(JiraModules module : JiraModules.values()) {
            if(module.installed) {
                continue;
            }
            rest = null;
            try {
                InstallCancellable ic = new InstallCancellable();
                oc = OperationContainer.createForInstall();
                if (oc.canBeAdded(module.updateElement.getUpdateUnit(), module.updateElement)) {
                    oc.add(module.updateElement);
                } else if (module.updateElement.getUpdateUnit().isPending()) {
                    notifyInDialog(NbBundle.getMessage(MissingJiraSupportPanel.class, "MSG_MissingClient_RestartNeeded"), //NOI18N
                        NbBundle.getMessage(MissingJiraSupportPanel.class, "LBL_MissingClient_RestartNeeded"),            //NOI18N
                        NotifyDescriptor.INFORMATION_MESSAGE, false);
                    return;
                } else {
                    oc = OperationContainer.createForUpdate();
                    if (oc.canBeAdded(module.updateElement.getUpdateUnit(), module.updateElement)) {
                        oc.add(module.updateElement);
                    } else {
                        BugtrackingManager.LOG.log(Level.WARNING, "MissingClient: cannot install {0}", module.updateElement.toString());            // NOI18N
                        if (module.updateElement.getUpdateUnit().getInstalled() != null) {
                            BugtrackingManager.LOG.log(Level.WARNING, "MissingClient: already installed {0}", module.updateElement.getUpdateUnit().getInstalled().toString()); // NOI18N
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
                            MissingJiraSupportPanel.class, "LBL_Downloading", module.updateElement.getDisplayName()), // NOI18N
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
                                module.updateElement.getDisplayName()),
                            ic));
                if(ic.cancelled) {
                    return;
                }
                rest = oc.getSupport().doInstall(
                            i,
                            ProgressHandleFactory.createHandle(
                                NbBundle.getMessage(
                                    MissingJiraSupportPanel.class,
                                    "LBL_Installing",                   // NOI18N
                                    module.updateElement.getDisplayName()),
                            ic));
            } catch (OperationException e) {
                BugtrackingManager.LOG.log(Level.INFO, null, e);
                notifyError(NbBundle.getMessage(MissingJiraSupportPanel.class, "MSG_MissingClient_UC_Unavailable"),   // NOI18N
                        NbBundle.getMessage(MissingJiraSupportPanel.class, "LBL_MissingClient_UC_Unavailable"));      // NOI18N
            }
        }    
        
        // restart
        if(rest != null) {
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
                try {
                    oc.getSupport().doRestart(
                        rest,
                        ProgressHandleFactory.createHandle(NbBundle.getMessage(MissingJiraSupportPanel.class, "LBL_Restarting"))); // NOI18N
                } catch (OperationException e) {
                    BugtrackingManager.LOG.log(Level.INFO, null, e);
                    notifyError(NbBundle.getMessage(MissingJiraSupportPanel.class, "MSG_MissingClient_UC_Unavailable"),   // NOI18N
                            NbBundle.getMessage(MissingJiraSupportPanel.class, "LBL_MissingClient_UC_Unavailable"));      // NOI18N
                }
            }
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
        @Override
        public boolean cancel() {
            cancelled = true;
            return true;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == panel.acceptCheckBox) {
            install.setEnabled(panel.acceptCheckBox.isSelected());
        } 
    }

}
