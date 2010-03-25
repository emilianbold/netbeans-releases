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
package org.netbeans.modules.subversion.client;

import org.netbeans.api.autoupdate.OperationSupport.Restarter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.logging.Level;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.InstallSupport.Installer;
import org.netbeans.api.autoupdate.InstallSupport.Validator;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.options.SvnOptionsController;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.util.AccessibleJFileChooser;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Stupka
 */
public class MissingClient implements ActionListener, HyperlinkListener {
    
    private final MissingClientPanel panel;
    
    /** Creates a new instance of MissingSvnClient */
    public MissingClient() {
        panel = new MissingClientPanel();
        panel.browseButton.addActionListener(this);        
        panel.executablePathTextField.setText(SvnModuleConfig.getDefault().getExecutableBinaryPath());
        panel.textPane.addHyperlinkListener(this);
        panel.downloadRadioButton.addActionListener(this);
        panel.cliRadioButton.addActionListener(this);        
        if(Utilities.isWindows() && !SvnUtils.isJava64()) {
            panel.downloadRadioButton.setSelected(true);
        } else {
            panel.cliRadioButton.setSelected(true);
            panel.downloadRadioButton.setEnabled(false);
            panel.forceGlobalCheckBox.setEnabled(false);
            panel.lblBinariesAvailableTip.setEnabled(false);
        }
        radioSwitch();
    }

    public JPanel getPanel() {
        return panel;
    }

    public void show() {        
        JButton ok = new JButton(NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_Action_OK"));        
        JButton cancel = new JButton(NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_Action_Cancel"));        
        NotifyDescriptor descriptor = new NotifyDescriptor (
                panel, 
                NbBundle.getMessage(SvnClientExceptionHandler.class, "MSG_CommandFailed_Title"), 
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.ERROR_MESSAGE,
                new Object [] { ok, cancel },
                ok);
        if(DialogDisplayer.getDefault().notify(descriptor) == ok) {
            if(panel.downloadRadioButton.isSelected()) {
                onDownload();
            } else {
                SvnModuleConfig.getDefault().setExecutableBinaryPath(panel.executablePathTextField.getText());
                SvnClientFactory.reset();
            }
        }
    }
    
    private void onBrowseClick() {
        File oldFile = getExecutableFile();
        JFileChooser fileChooser = new AccessibleJFileChooser(NbBundle.getMessage(SvnOptionsController.class, "ACSD_BrowseFolder"), oldFile);   // NOI18N
        fileChooser.setDialogTitle(NbBundle.getMessage(SvnOptionsController.class, "Browse_title"));                                            // NOI18N
        fileChooser.setMultiSelectionEnabled(false);       
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.showDialog(panel, NbBundle.getMessage(SvnOptionsController.class, "OK_Button"));                                            // NOI18N
        File f = fileChooser.getSelectedFile();
        if (f != null) {
            panel.executablePathTextField.setText(f.getAbsolutePath());
        }
    }

    private File getExecutableFile() {
        String execPath = panel.executablePathTextField.getText();
        return FileUtil.normalizeFile(new File(execPath));
    }    

    public void actionPerformed(ActionEvent evt) {
        if(evt.getSource() == panel.browseButton) {
            onBrowseClick();
        } else if(evt.getSource() == panel.downloadRadioButton || evt.getSource() == panel.cliRadioButton) {
            radioSwitch();
        }
    }

    public void hyperlinkUpdate(HyperlinkEvent e) {
        if(e.getEventType() != HyperlinkEvent.EventType.ACTIVATED) return;
        URL url = e.getURL();
        assert url != null;
        HtmlBrowser.URLDisplayer displayer = HtmlBrowser.URLDisplayer.getDefault ();
        assert displayer != null : "HtmlBrowser.URLDisplayer found.";
        if (displayer != null) {
            displayer.showURL (url);
        } else {
            Subversion.LOG.info("No URLDisplayer found.");
        }
    }

    private void onDownload() {
        DownloadPlugin dp = new DownloadPlugin();
        dp.show();
        UpdateElement updateElement = dp.getUpdateElement();
        if(updateElement != null) {
            install(updateElement);
        }
    }

    private void install(final UpdateElement updateElement) {
        Subversion.getInstance().getParallelRequestProcessor().post(new Runnable() {
            public void run() {
                try {
                    InstallCancellable ic = new InstallCancellable();
                    OperationContainer<InstallSupport> oc = OperationContainer.createForInstall();
                    if (oc.canBeAdded(updateElement.getUpdateUnit(), updateElement)) {
                        oc.add(updateElement);
                    } else if (updateElement.getUpdateUnit().isPending()) {
                        notifyInDialog(NbBundle.getMessage(MissingClient.class, "MSG_MissingClient_RestartNeeded"), //NOI18N
                            NbBundle.getMessage(MissingClient.class, "LBL_MissingClient_RestartNeeded"), //NOI18N
                            NotifyDescriptor.INFORMATION_MESSAGE, false);
                        return;
                    } else {
                        oc = OperationContainer.createForUpdate();
                        if (oc.canBeAdded(updateElement.getUpdateUnit(), updateElement)) {
                            oc.add(updateElement);
                        } else {
                            Subversion.LOG.warning("MissingClient: cannot install " + updateElement.toString());
                            if (updateElement.getUpdateUnit().getInstalled() != null) {
                                Subversion.LOG.warning("MissingClient: already installed " + updateElement.getUpdateUnit().getInstalled().toString());
                            }
                            notifyInDialog(NbBundle.getMessage(MissingClient.class, "MSG_MissingClient_InvalidOperation"), //NOI18N
                                    NbBundle.getMessage(MissingClient.class, "LBL_MissingClient_InvalidOperation"), //NOI18N
                                    NotifyDescriptor.ERROR_MESSAGE, false);
                            return;
                        }
                    }
                    Validator v = oc.getSupport().doDownload(ProgressHandleFactory.createHandle(NbBundle.getMessage(MissingClient.class, "LBL_Downloading") + updateElement.getDisplayName(), ic), panel.forceGlobalCheckBox.isSelected());
                    if(ic.cancelled) return;
                    Installer i = oc.getSupport().doValidate(v, ProgressHandleFactory.createHandle(NbBundle.getMessage(MissingClient.class, "LBL_Validating") + updateElement.getDisplayName(), ic));
                    if(ic.cancelled) return;
                    Restarter rest = oc.getSupport().doInstall(i, ProgressHandleFactory.createHandle(NbBundle.getMessage(MissingClient.class, "LBL_Installing") + updateElement.getDisplayName(), ic));
                    if(rest != null) {
                        JButton restart = new JButton(NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_Action_Restart"));
                        JButton cancel = new JButton(NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_Action_Cancel"));
                        NotifyDescriptor descriptor = new NotifyDescriptor(
                                NbBundle.getMessage(MissingClient.class, "MSG_NeedsRestart"),
                                NbBundle.getMessage(MissingClient.class, "LBL_DownloadJavahl"),
                                    NotifyDescriptor.OK_CANCEL_OPTION,
                                    NotifyDescriptor.QUESTION_MESSAGE,
                                    new Object [] { restart, cancel },
                                    restart);
                        if(DialogDisplayer.getDefault().notify(descriptor) == restart) {
                            oc.getSupport().doRestart(
                                rest,
                                ProgressHandleFactory.createHandle(NbBundle.getMessage(MissingClient.class, "LBL_Restarting")));
                        }
                    }
                } catch (OperationException e) {
                    Subversion.LOG.log(Level.INFO, null, e);
                    notifyError(NbBundle.getMessage(MissingClient.class, "MSG_MissingClient_UC_Unavailable"),   // NOI18N
                            NbBundle.getMessage(MissingClient.class, "LBL_MissingClient_UC_Unavailable"));      // NOI18N
                }
            }
        });
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

    private void radioSwitch() {
        boolean cliEnabled = panel.cliRadioButton.isSelected();
        panel.browseButton.setEnabled(cliEnabled);
        panel.executablePathTextField.setEnabled(cliEnabled);
    }

    private class InstallCancellable implements Cancellable {
        private boolean cancelled;
        public boolean cancel() {
            cancelled = true;
            return true;
        }
    }
}
