/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.connections.sync;

import java.awt.BorderLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 * Synchronization progress panel.
 */
public class ProgressPanel extends JPanel {

    private static final long serialVersionUID = -46789965146754L;

    final SummaryPanel summaryPanel;

    // @GuardedBy(AWT)
    NotifyDescriptor descriptor = null;
    // @GuardedBy(AWT)
    NotificationLineSupport notificationLineSupport = null;

    volatile boolean error = false;


    public ProgressPanel(SyncPanel.SyncInfo syncInfo) {
        assert SwingUtilities.isEventDispatchThread();
        assert syncInfo != null;

        summaryPanel = new SummaryPanel(syncInfo.upload, syncInfo.download, syncInfo.delete, syncInfo.noop);

        initComponents();
        summaryPanelHolder.add(summaryPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    @NbBundle.Messages("ProgressPanel.title=Synchronization")
    public void showPanel() {
        assert SwingUtilities.isEventDispatchThread();
        descriptor = new NotifyDescriptor(
                this,
                Bundle.ProgressPanel_title(),
                NotifyDescriptor.DEFAULT_OPTION,
                NotifyDescriptor.PLAIN_MESSAGE,
                new Object[] {NotifyDescriptor.OK_OPTION},
                NotifyDescriptor.OK_OPTION);
        descriptor.setValid(false);
        notificationLineSupport = descriptor.createNotificationLineSupport();
        DialogDisplayer.getDefault().notifyLater(descriptor);
    }

    @NbBundle.Messages("ProgressPanel.success=Synchronization successfully finished.")
    public void finish() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                descriptor.setValid(true);
                if (!error) {
                    notificationLineSupport.setInformationMessage(Bundle.ProgressPanel_success());
                }
            }
        });
    }

    @NbBundle.Messages("ProgressPanel.error=Error occurred, review Output window for details.")
    void errorOccurred() {
        if (error) {
            // error already set
            return;
        }
        error = true;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                notificationLineSupport.setErrorMessage(Bundle.ProgressPanel_error());
            }
        });
    }

    public void decreaseUploadNumber() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                summaryPanel.decreaseUploadNumber();
            }
        });
    }

    public void decreaseDownloadNumber() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                summaryPanel.decreaseDownloadNumber();
            }
        });
    }

    public void resetDeleteNumber() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                summaryPanel.resetDeleteNumber();
            }
        });
    }

    public void decreaseNoopNumber() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                summaryPanel.decreaseNoopNumber();
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        infoLabel = new JLabel();
        summaryPanelHolder = new JPanel();
        Mnemonics.setLocalizedText(infoLabel, NbBundle.getMessage(ProgressPanel.class, "ProgressPanel.infoLabel.text")); // NOI18N

        summaryPanelHolder.setLayout(new BorderLayout());

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(infoLabel)
                .addContainerGap()).addComponent(summaryPanelHolder, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(infoLabel)

                .addPreferredGap(ComponentPlacement.RELATED).addComponent(summaryPanelHolder, GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel infoLabel;
    private JPanel summaryPanelHolder;
    // End of variables declaration//GEN-END:variables
}
