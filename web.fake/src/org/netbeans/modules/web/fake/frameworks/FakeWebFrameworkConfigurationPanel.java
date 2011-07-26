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

package org.netbeans.modules.web.fake.frameworks;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.web.api.webmodule.WebFrameworks;
import org.netbeans.modules.web.fake.modules.ModulesInstaller;
import org.netbeans.modules.web.fake.modules.ProgressMonitor;
import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.openide.awt.Mnemonics;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.TaskListener;

/**
 * Provider for fake web module extenders. Able to download and enable the proper module
 * as well as delegate to the proper configuration panel.
 * @author Tomas Mysik
 */
public class FakeWebFrameworkConfigurationPanel extends JPanel {
    private static final long serialVersionUID = 27938464212508L;

    final ProgressMonitor progressMonitor = new DownloadProgressMonitor();
    final FakeWebModuleExtender fakeExtender;
    private final String name;
    private final String codeNameBase;
    private JComponent panel;

    FakeWebFrameworkConfigurationPanel(FakeWebModuleExtender fakeExtender, final String name, final String codeNameBase) {
        assert fakeExtender != null;
        assert name != null;
        assert codeNameBase != null;

        this.fakeExtender = fakeExtender;
        this.name = name;
        this.codeNameBase = codeNameBase;

        initComponents();

        String lblMsg = null;
        String btnMsg = null;
        if (fakeExtender.isModulePresent()) {
            lblMsg = NbBundle.getMessage(FakeWebFrameworkConfigurationPanel.class, "LBL_EnableInfo", name);
            btnMsg = NbBundle.getMessage(FakeWebFrameworkConfigurationPanel.class, "LBL_Enable");
        } else {
            lblMsg = NbBundle.getMessage(FakeWebFrameworkConfigurationPanel.class, "LBL_DownloadInfo", name);
            btnMsg = NbBundle.getMessage(FakeWebFrameworkConfigurationPanel.class, "LBL_Download");
        }

        infoLabel.setText(lblMsg);
        downloadButton.setText(btnMsg);
        setError(" "); // NOI18N
    }

    void setError(String msg) {
        assert SwingUtilities.isEventDispatchThread ();
        errorLabel.setText(msg);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        errorLabel = new JLabel();
        infoLabel = new JLabel();
        downloadButton = new JButton();
        progressPanel = new JPanel();

        errorLabel.setForeground(UIManager.getDefaults().getColor("nb.errorForeground"));
        Mnemonics.setLocalizedText(errorLabel, "dummy");
        Mnemonics.setLocalizedText(infoLabel, "dummy");
        Mnemonics.setLocalizedText(downloadButton, "dummy");
        downloadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                downloadButtonActionPerformed(evt);
            }
        });

        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.PAGE_AXIS));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(progressPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                    .addComponent(infoLabel)
                    .addComponent(downloadButton)
                    .addComponent(errorLabel))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(errorLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(infoLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(downloadButton)
                .addGap(18, 18, 18)
                .addComponent(progressPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void downloadButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_downloadButtonActionPerformed
        downloadButton.setEnabled(false);
        final boolean[] success = new boolean[1];
        Task task = RequestProcessor.getDefault().create(new Runnable() {
            public void run() {
                success[0] = ModulesInstaller.installModules(progressMonitor, codeNameBase);
            }
        });
        task.addTaskListener(new TaskListener() {
            public void taskFinished(org.openide.util.Task task) {
                WebFrameworkProvider webFrameworkProvider = null;
                if (success[0]) {
                    // can be a bit dangerous ;)
                    int i = 0;
                    while (webFrameworkProvider == null) {
                        if (++i > 10) {
                            // no success?! this should never happen but will try to handle it
                            break;
                        }
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        webFrameworkProvider = getWebFrameworkProvider();
                    }
                }
                if (webFrameworkProvider != null) {
                    setRealWebFrameworkProvider(webFrameworkProvider);
                    setRealWebFrameworkConfigurationPanel();

                    // update & fire events
                    fakeExtender.update();
                    fakeExtender.stateChanged(null);
                } else {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            String msg = null;
                            if (fakeExtender.isModulePresent()) {
                                msg = NbBundle.getMessage(FakeWebFrameworkConfigurationPanel.class, "MSG_EnableFailed");
                            } else {
                                msg = NbBundle.getMessage(FakeWebFrameworkConfigurationPanel.class, "MSG_DownloadFailed");
                            }
                            setError(msg);
                            downloadButton.setEnabled(true);
                            progressPanel.removeAll();
                            progressPanel.revalidate();
                            progressPanel.repaint();
                        }
                    });
                }
            }
        });
        task.schedule(0);
    }//GEN-LAST:event_downloadButtonActionPerformed

    private void setRealWebFrameworkProvider(WebFrameworkProvider webFrameworkProvider) {
        assert webFrameworkProvider != null : String.format("Web framework provider must be found for %s (%s)", name, codeNameBase);
        assert !(webFrameworkProvider instanceof FakeWebFrameworkProvider.FakeWebFrameworkProviderImpl) : "Fake web framework provider found";
        fakeExtender.setWebFrameworkProvider(webFrameworkProvider);
    }

    private WebFrameworkProvider getWebFrameworkProvider() {
        for (WebFrameworkProvider provider : WebFrameworks.getFrameworks()) {
            if (provider.getClass().getName().equals(fakeExtender.getFrameworkProviderClassName())) {
                return provider;
            }
        }
        return null;
    }

    private void setRealWebFrameworkConfigurationPanel() {
        WebModuleExtender realExtender = fakeExtender.getDelegate();
        assert realExtender != null : String.format("Real web module extender must be found for %s (%s)", name, codeNameBase);
        panel = realExtender.getComponent();
        removeAll();
        if (panel != null) {
            setLayout(new BorderLayout());
            add(panel, BorderLayout.NORTH);
        }
        revalidate();
        repaint();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton downloadButton;
    private JLabel errorLabel;
    private JLabel infoLabel;
    private JPanel progressPanel;
    // End of variables declaration//GEN-END:variables

    private final class DownloadProgressMonitor implements ProgressMonitor {

        public void onDownload(ProgressHandle progressHandle) {
            updateProgress(progressHandle);
        }

        public void onValidate(ProgressHandle progressHandle) {
            updateProgress(progressHandle);
        }

        public void onInstall(ProgressHandle progressHandle) {
            updateProgress(progressHandle);
        }

        public void onEnable(ProgressHandle progressHandle) {
            updateProgress(progressHandle);
        }

        private void updateProgress(final ProgressHandle progressHandle) {
            final JLabel tmpMainLabel = ProgressHandleFactory.createMainLabelComponent(progressHandle);
            final JComponent tmpProgressPanel = ProgressHandleFactory.createProgressComponent(progressHandle);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progressPanel.removeAll();
                    progressPanel.add(tmpMainLabel);
                    progressPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                    progressPanel.add(tmpProgressPanel);
                    progressPanel.revalidate();
                    progressPanel.repaint();
                }
            });
        }
    }
}
