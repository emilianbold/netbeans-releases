/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.findbugs.installer;

import java.awt.event.ActionEvent;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.api.autoupdate.OperationSupport.Restarter;
import org.netbeans.api.options.OptionsDisplayer;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.Dialog;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.openide.DialogDisplayer;
import java.io.IOException;
import java.util.List;
import javax.swing.JScrollPane;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.autoupdate.ui.api.PluginManager;
import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import static org.netbeans.modules.findbugs.installer.Bundle.*;

/**
 * Copied and adjusted from the JUnitLibraryDownloader
 */
public class ModuleInstallerSupport  {
    private static RequestProcessor RP = new RequestProcessor(ModuleInstallerSupport.class.getName(), 1);
    private static final Logger LOG = Logger.getLogger(ModuleInstallerSupport.class.getName());

    @Messages({
        "searching_handle=Searching for \"{0}\" library on NetBeans plugin portal...",
        "resolve_title=Resolve \"{0}\" Reference Problem",
        "networkproblem_header=Unable to connect  to the NetBeans plugin portal",
        "networkproblem_message=Check your proxy settings or try again later. "
            + "The server may be unavailable at the moment. "
            + "You may also want to make sure that your firewall is not blocking network traffic.",
        "proxy_button=&Proxy Settings...",
        "tryagain_button=Try &Again",
        "nodownload_header=\"{0}\" module has not been downloaded",
        "nodownload_message=You can try to download \"{0}\" module again",
        "active_handle=Activating {0}"
    })
    
    private JButton tryAgain;
    private JButton proxySettings;

    @SuppressWarnings("SleepWhileInLoop")
    public boolean download(String cnb, final String displayName) throws Exception {
        UpdateUnit unit = findModule(cnb);
        if (unit == null) {
            final ProgressHandle handle = ProgressHandleFactory.createHandle(searching_handle(displayName));
            initButtons();
            final DialogDescriptor searching = new DialogDescriptor(searchingPanel(new JLabel(searching_handle(displayName)),
                    ProgressHandleFactory.createProgressComponent(handle)), resolve_title(displayName), true, null);
            handle.setInitialDelay (0);
            handle.start ();
            searching.setOptions(new Object[] {NotifyDescriptor.CANCEL_OPTION});
            searching.setMessageType(NotifyDescriptor.PLAIN_MESSAGE);
            final Dialog dlg = DialogDisplayer.getDefault().createDialog(searching);
            RP.post(new Runnable() {

                @Override
                public void run() {
                    // May be first start, when no update lists have yet been downloaded.
                    try {
                        for (UpdateUnitProvider p : UpdateUnitProviderFactory.getDefault().getUpdateUnitProviders(true)) {
                            p.refresh(handle, true);
                        }
                        // close searching
                        dlg.dispose();
                    } catch (IOException ex) {
                        LOG.log(Level.FINE, ex.getMessage(), ex);
                        if (! dlg.isVisible()) {
                            LOG.fine("dialog not visible => do nothing");
                            return ;
                        }
                        DialogDescriptor networkProblem = new DialogDescriptor(
                                problemPanel(resolve_title(displayName), networkproblem_message()), // message
                                networkproblem_header(), // title
                                true, // modal
                                null);
                        networkProblem.setOptions(new Object[] {tryAgain, proxySettings, NotifyDescriptor.CANCEL_OPTION});
                        networkProblem.setClosingOptions(new Object[] {tryAgain, NotifyDescriptor.CANCEL_OPTION});
                        networkProblem.setMessageType(NotifyDescriptor.WARNING_MESSAGE);
                        Dialog networkProblemDialog = DialogDisplayer.getDefault().createDialog(networkProblem);
                        networkProblemDialog.setVisible(true);
                        Object answer = networkProblem.getValue();
                        if (NotifyDescriptor.CANCEL_OPTION.equals(answer) || answer.equals(-1) /* escape */ ) {
                            LOG.fine("cancel network problem dialog");
                            searching.setValue(answer);
                            dlg.dispose();
                        } else if (tryAgain.equals(answer)) {
                            LOG.fine("try again searching");
                            RP.post(this);
                            assert false : "Unknown " + answer;
                        }
                    }
                }
            });
            dlg.setVisible(true);
            handle.finish();
            if (NotifyDescriptor.CANCEL_OPTION.equals(searching.getValue()) || searching.getValue().equals(-1) /* escape */) {
                LOG.log(Level.FINE, "user canceled searching for {0}", cnb);
                return showNoDownloadDialog(cnb, displayName);
            }
            unit = findModule(cnb);
            if (unit == null) {
                LOG.log(Level.FINE, "could not find {0} on any update site", cnb);
                return showNoDownloadDialog(cnb, displayName);
            }
        }
        // check if module installed
        if (unit.getInstalled() != null) {
            LOG.fine(unit.getInstalled() + " already installed. Is active? " + unit.getInstalled().isEnabled());
            if (unit.getInstalled().isEnabled()) {
                throw new Exception(unit.getInstalled() + " already installed and active");
            } else {
                // activate it
                OperationContainer<OperationSupport> oc = OperationContainer.createForEnable();
                if (!oc.canBeAdded(unit, unit.getInstalled())) {
                    throw new Exception("could not add " + unit.getInstalled() + " for activation");
                }
                for (UpdateElement req : oc.add(unit.getInstalled()).getRequiredElements()) {
                    oc.add(req);
                }
                ProgressHandle activeHandle = ProgressHandleFactory.createHandle (active_handle(displayName));
                Restarter restarter = oc.getSupport().doOperation(activeHandle);
                assert restarter == null : "No Restater need to make " + unit.getInstalled() + " active";
                // XXX new library & build.properties apparently do not show up immediately... how to listen properly?
                return true;
            }
        }
        List<UpdateElement> updates = unit.getAvailableUpdates();
        if (updates.isEmpty()) {
            throw new Exception("no updates for " + unit);
        }
        OperationContainer<InstallSupport> oc = OperationContainer.createForInstall();
        UpdateElement element = updates.get(0);
        if (!oc.canBeAdded(unit, element)) {
            throw new Exception("could not add " + element + " to updates");
        }
        for (UpdateElement req : oc.add(element).getRequiredElements()) {
            oc.add(req);
        }
        if (!PluginManager.openInstallWizard(oc)) {
            LOG.fine("user canceled PM");
            return showNoDownloadDialog(cnb, displayName);
        }
        return true;
    }

    private UpdateUnit findModule(String cnb) throws IOException {
        for (UpdateUnit unit : UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE)) {
            if (unit.getCodeName().equals(cnb)) {
                return unit;
            }
        }
        return null;
    }
    
    private void initButtons() {
        if (tryAgain != null) {
            return ;
        }
        tryAgain = new JButton();
        proxySettings = new JButton();
        Mnemonics.setLocalizedText(tryAgain, tryagain_button());
        Mnemonics.setLocalizedText(proxySettings, proxy_button());
        proxySettings.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LOG.fine("show proxy options");
                OptionsDisplayer.getDefault().open("General"); // NOI18N
            }
        });
    }
    
    private boolean showNoDownloadDialog(String cnb, String displayName) throws Exception {
        DialogDescriptor networkProblem = new DialogDescriptor(
                problemPanel(nodownload_header(displayName), nodownload_message(displayName)), // message
                resolve_title(displayName), // title
                true, // modal
                null);
        initButtons();
        networkProblem.setOptions(new Object[] {tryAgain, NotifyDescriptor.CANCEL_OPTION});
        networkProblem.setClosingOptions(new Object[] {tryAgain, NotifyDescriptor.CANCEL_OPTION});
        networkProblem.setMessageType(NotifyDescriptor.WARNING_MESSAGE);
        Dialog networkProblemDialog = DialogDisplayer.getDefault().createDialog(networkProblem);
        networkProblemDialog.setVisible(true);
        Object answer = networkProblem.getValue();
        if (NotifyDescriptor.CANCEL_OPTION.equals(answer) || answer.equals(-1) /* escape */ ) {
            LOG.fine("cancel no download dialog");
            //throw new InterruptedException("user canceled download & install JUnit");
            return false;
        } else if (tryAgain.equals(answer)) {
            LOG.fine("try again download()");
            return download(cnb, displayName);
        } else {
            assert false : "Unknown " + answer;
        }
        assert false : "Unknown " + answer;
        return false;
    }
    
    private static JPanel searchingPanel(JLabel progressLabel, JComponent progressComponent) {
        JPanel panel = new JPanel();
        progressLabel.setLabelFor(progressComponent);
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(panel);
        panel.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(progressLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(progressComponent, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(96, 96, 96)
                .addComponent(progressLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressComponent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(109, Short.MAX_VALUE))
        );
        return panel;
    }
    
    private static JPanel problemPanel(String header, String message) {
        JPanel panel = new JPanel();
        JLabel jLabel1 = new javax.swing.JLabel();
        JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
        JTextArea jTextArea1 = new javax.swing.JTextArea();

        jLabel1.setFont(jLabel1.getFont().deriveFont(jLabel1.getFont().getStyle() | java.awt.Font.BOLD));
        jLabel1.setText(header);

        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setLineWrap(true);
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setRows(5);
        jTextArea1.setText(message);
        jTextArea1.setOpaque(false);
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(panel);
        panel.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(107, 107, 107)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE)
                .addGap(82, 82, 82))
        );
        return panel;
    }

}
